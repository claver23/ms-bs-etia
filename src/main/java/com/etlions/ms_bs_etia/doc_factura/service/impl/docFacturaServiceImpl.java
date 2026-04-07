package com.etlions.ms_bs_etia.doc_factura.service.impl;

import com.etlions.ms_bs_etia.app_config.exception.ResourceNotFoundException;
import com.etlions.ms_bs_etia.app_util.service.CorrelativoService;
import com.etlions.ms_bs_etia.app_util.validation.SunatValidator;
import com.etlions.ms_bs_etia.client.persistence.ClientEntity;
import com.etlions.ms_bs_etia.client.persistence.ClientRepository;
import com.etlions.ms_bs_etia.company.persistence.CompanyRepository;
import com.etlions.ms_bs_etia.doc_factura.mapper.FacturaMapper;
import com.etlions.ms_bs_etia.doc_factura.persistence.*;
import com.etlions.ms_bs_etia.doc_factura.service.DocFacturaService;
import com.etlions.ms_bs_etia.doc_factura.xml.UblInvoiceXmlBuilder;
import com.etlions.ms_bs_etia.notification.service.NotificationService;
import com.etlions.openapicodegen.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocFacturaServiceImpl implements DocFacturaService {

    private final FacturaRepository facturaRepository;
    private final FacturaMapper facturaMapper;
    private final CorrelativoService correlativoService;
    private final NotificationService notificationService;
    private final ClientRepository clientRepository;
    private final CompanyRepository companyRepository;
    private final CuotaCreditoRepository cuotaCreditoRepository;
    private final AllowanceChargeRepository allowanceChargeRepository;
    private final UblInvoiceXmlBuilder ublInvoiceXmlBuilder;

    private static final String DEFAULT_SERIE_FACTURA = "F001";

    @Override
    @Transactional
    public FacturaResponseDto emitirFactura(FacturaEmitirDto request) {
        // ── 1. Campos obligatorios ──
        SunatValidator.validarCampoObligatorio(request.getCompanyId(), "companyId");
        SunatValidator.validarCampoObligatorio(request.getClientId(), "clientId");

        // ── 2. Validar serie ──
        String serie = request.getSerie() != null ? request.getSerie().toUpperCase() : DEFAULT_SERIE_FACTURA;
        SunatValidator.validarFormatoSerie(serie);
        SunatValidator.validarSerieFactura(serie);

        // ── 3. Validar montos no negativos y luego la suma ──
        SunatValidator.validarMontosNoNegativos(request.getGravadas(), request.getIgv(), request.getTotal());
        SunatValidator.validarTotales(request.getGravadas(), request.getIgv(), request.getTotal());

        // ── 4. Validar detalle obligatorio con al menos 1 ítem ──
        SunatValidator.validarDetalleNoVacio(request.getDetalle());
        SunatValidator.validarItemsDetalle(
                request.getDetalle().stream().map(DetalleItem::getDescripcion).toList(),
                request.getDetalle().stream().map(DetalleItem::getCantidad).toList(),
                request.getDetalle().stream().map(DetalleItem::getValorUnitario).toList()
        );

        // ── 5. Validar que la empresa (companyId) exista ──
        companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", request.getCompanyId()));

        // ── 6. Validar tipo de documento del cliente: Factura → solo RUC ──
        ClientEntity client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", request.getClientId()));
        SunatValidator.validarTipoDocumentoCliente("FACTURA", client.getTipoDocumento());

        // ── 7. Moneda USD → tipo de cambio obligatorio ──
        String moneda = request.getMoneda() != null ? request.getMoneda().getValue() : null;
        SunatValidator.validarMonedaYTipoCambio(moneda, request.getTipoCambio());

        // ── 8. Fecha de emisión no puede ser futura ──
        LocalDate fechaEmision = request.getFechaEmision() != null
                ? new java.sql.Date(request.getFechaEmision().getTime()).toLocalDate()
                : LocalDate.now();
        SunatValidator.validarFechaEmisionNoFutura(fechaEmision);

        // ── 9. CRÉDITO → requiere fecha de vencimiento posterior ──
        String formaPagoStr = request.getFormaPago() != null ? request.getFormaPago().getValue() : "CONTADO";
        LocalDate fechaVenc = request.getFechaVencimiento() != null
                ? new java.sql.Date(request.getFechaVencimiento().getTime()).toLocalDate()
                : null;
        SunatValidator.validarFormaPagoCredito(formaPagoStr, fechaEmision, fechaVenc);

        // ── 10. Correlativo automático ──
        int numero = correlativoService.siguienteNumero(request.getCompanyId(), serie);

        FacturaEntity entity = facturaMapper.toEntity(request);
        entity.setSerie(serie);
        entity.setNumero(numero);
        entity.setEstado("BORRADOR");
        if (entity.getFechaEmision() == null) {
            entity.setFechaEmision(LocalDate.now());
        }
        // ── Defaults UBL 2.1 ──
        if (entity.getTipoOperacion() == null) entity.setTipoOperacion("0101");
        if (entity.getHoraEmision() == null) entity.setHoraEmision(LocalTime.now());
        if (entity.getExoneradas() == null) entity.setExoneradas(BigDecimal.ZERO);
        if (entity.getInafectas() == null) entity.setInafectas(BigDecimal.ZERO);
        if (entity.getGratuitas() == null) entity.setGratuitas(BigDecimal.ZERO);
        if (entity.getTotalCargos() == null) entity.setTotalCargos(BigDecimal.ZERO);
        if (entity.getTotalDescuentos() == null) entity.setTotalDescuentos(BigDecimal.ZERO);
        if (entity.getSubtotal() == null && entity.getGravadas() != null) {
            entity.setSubtotal(entity.getGravadas());
        }
        if (entity.getTotalConImpuestos() == null && entity.getGravadas() != null && entity.getIgv() != null) {
            entity.setTotalConImpuestos(entity.getGravadas().add(entity.getIgv()));
        }
        /* If CONTADO, due date = emission date (date only, no time) */
        if ("CONTADO".equalsIgnoreCase(entity.getFormaPago()) || entity.getFormaPago() == null) {
            entity.setFechaVencimiento(entity.getFechaEmision());
            if (entity.getFormaPago() == null) entity.setFormaPago("CONTADO");
        }
        if (entity.getFechaVencimiento() == null) {
            entity.setFechaVencimiento(entity.getFechaEmision());
        }

        // ── Recalculate totalCargos/totalDescuentos from AllowanceCharge items ──
        if (request.getCargosDescuentos() != null && !request.getCargosDescuentos().isEmpty()) {
            BigDecimal sumCargos = BigDecimal.ZERO;
            BigDecimal sumDescuentos = BigDecimal.ZERO;
            for (AllowanceChargeItem ac : request.getCargosDescuentos()) {
                if (Boolean.TRUE.equals(ac.getChargeIndicator())) {
                    sumCargos = sumCargos.add(ac.getAmount());
                } else {
                    sumDescuentos = sumDescuentos.add(ac.getAmount());
                }
            }
            entity.setTotalCargos(sumCargos);
            entity.setTotalDescuentos(sumDescuentos);
        }

        entity.setFechaCreacion(LocalDateTime.now());
        FacturaEntity saved = facturaRepository.save(entity);

        // ── Persist cuotas de crédito ──
        List<CuotaCreditoEntity> cuotaEntities = persistCuotas(
                request.getCuotas(), saved.getId(), null, saved.getMoneda());

        // ── Persist cargos/descuentos globales ──
        List<AllowanceChargeEntity> acEntities = persistAllowanceCharges(
                request.getCargosDescuentos(), saved.getId(), null, saved.getMoneda());

        FacturaData data = facturaMapper.toData(saved);
        enrichFacturaData(data, cuotaEntities, acEntities);

        notificationService.publicar(saved.getCompanyId(), "EMITIDA", "FACTURA",
                saved.getId(), serie + "-" + numero,
                "Factura " + serie + "-" + numero + " emitida");
        return new FacturaResponseDto()
                .isSuccess(true)
                .status("OK")
                .message("Factura emitida exitosamente — " + serie + "-" + numero)
                .timestamp(new Date())
                .data(data);
    }

    @Override
    @Transactional
    public FacturaResponseDto anularFactura(String facturaId, AnularFacturaDto request) {
        FacturaEntity entity = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new ResourceNotFoundException("Factura", facturaId));
        // — Validar que la transición de estado sea permitida —
        SunatValidator.validarAnulacionPermitida(entity.getEstado(), "Factura", facturaId);
        entity.setEstado("ANULADA");
        FacturaEntity saved = facturaRepository.save(entity);
        FacturaData data = buildFacturaData(saved);
        notificationService.publicar(saved.getCompanyId(), "ANULADA", "FACTURA",
                saved.getId(), saved.getSerie() + "-" + saved.getNumero(),
                "Factura " + saved.getSerie() + "-" + saved.getNumero() + " anulada");
        return new FacturaResponseDto()
                .isSuccess(true)
                .status("OK")
                .message("Factura anulada exitosamente")
                .timestamp(new Date())
                .data(data);
    }

    @Override
    @Transactional
    public SuccessResponse eliminarFactura(String facturaId) {
        FacturaEntity entity = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new ResourceNotFoundException("Factura", facturaId));
        // — SUNAT: solo se pueden eliminar borradores —
        SunatValidator.validarEliminacionPermitida(entity.getEstado(), "Factura", facturaId);
        cuotaCreditoRepository.deleteByFacturaId(facturaId);
        allowanceChargeRepository.deleteByFacturaId(facturaId);
        facturaRepository.delete(entity);
        return new SuccessResponse()
                .isSuccess(true)
                .status("OK")
                .message("Factura eliminada exitosamente")
                .timestamp(new Date());
    }

    @Override
    public FacturaListResponseDto listarFacturas(String companyId, Date desdeFecha, Date hastaFecha,
                                                     String serie, Integer numero, String estado,
                                                     Integer page, Integer size) {
        List<FacturaEntity> entities = facturaRepository.findByCompanyId(companyId);
        List<FacturaData> dataList = entities.stream()
                .map(this::buildFacturaData)
                .toList();
        FacturaListResponseDtoAllOfData responseData = new FacturaListResponseDtoAllOfData();
        responseData.setContent(dataList);
        responseData.setPagination(new PaginationMeta()
                .currentPage(page)
                .pageSize(size)
                .totalElements((long) dataList.size())
                .totalPages(1));
        FacturaListResponseDto response = new FacturaListResponseDto();
        response.setIsSuccess(true);
        response.setStatus("OK");
        response.setMessage("Operación exitosa");
        response.setTimestamp(new Date());
        response.setData(responseData);
        return response;
    }

    @Override
    public FacturaResponseDto obtenerFactura(String facturaId) {
        FacturaEntity entity = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new ResourceNotFoundException("Factura", facturaId));
        FacturaData data = buildFacturaData(entity);
        return new FacturaResponseDto()
                .isSuccess(true)
                .status("OK")
                .message("Operación exitosa")
                .timestamp(new Date())
                .data(data);
    }

    @Override
    public FacturaXmlResult descargarFacturaXml(String facturaId) {
        FacturaEntity factura = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new ResourceNotFoundException("Factura", facturaId));

        var company = companyRepository.findById(factura.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", factura.getCompanyId()));
        var client = clientRepository.findById(factura.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", factura.getClientId()));

        List<CuotaCreditoEntity> cuotas = cuotaCreditoRepository
                .findByFacturaIdOrderByNumeroCuota(facturaId);
        List<AllowanceChargeEntity> acs = allowanceChargeRepository
                .findByFacturaId(facturaId);

        String xml = ublInvoiceXmlBuilder.buildInvoiceXml(factura, company, client, cuotas, acs);

        // SUNAT filename: {RUC}-{tipoDoc}-{serie}-{numero}.xml
        String filename = company.getRuc() + "-01-" + factura.getSerie() + "-"
                + String.format("%08d", factura.getNumero()) + ".xml";

        byte[] content = xml.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return new FacturaXmlResult(content, filename, true);
    }

    // ═══════ HELPERS ═══════

    /** Build FacturaData enriched with cuotas and cargos/descuentos */
    private FacturaData buildFacturaData(FacturaEntity entity) {
        FacturaData data = facturaMapper.toData(entity);
        List<CuotaCreditoEntity> cuotas = cuotaCreditoRepository.findByFacturaIdOrderByNumeroCuota(entity.getId());
        List<AllowanceChargeEntity> acs = allowanceChargeRepository.findByFacturaId(entity.getId());
        enrichFacturaData(data, cuotas, acs);
        return data;
    }

    /** Enrich FacturaData with cuota and allowance-charge lists */
    private void enrichFacturaData(FacturaData data,
                                   List<CuotaCreditoEntity> cuotas,
                                   List<AllowanceChargeEntity> acs) {
        if (cuotas != null && !cuotas.isEmpty()) {
            data.setCuotas(cuotas.stream().map(c -> new CuotaCredito()
                    .numeroCuota(c.getNumeroCuota())
                    .monto(c.getMonto())
                    .fechaVencimiento(java.sql.Date.valueOf(c.getFechaVencimiento()))
                    .moneda(c.getMoneda())
            ).toList());
        }
        if (acs != null && !acs.isEmpty()) {
            data.setCargosDescuentos(acs.stream().map(a -> new AllowanceChargeItem()
                    .chargeIndicator(a.getChargeIndicator())
                    .reasonCode(a.getReasonCode())
                    .reasonDescription(a.getReasonDescription())
                    .multiplierFactor(a.getMultiplierFactor())
                    .amount(a.getAmount())
                    .baseAmount(a.getBaseAmount())
            ).toList());
        }
    }

    /** Persist cuotas de crédito for factura or boleta */
    private List<CuotaCreditoEntity> persistCuotas(List<CuotaCredito> cuotas,
                                                    String facturaId, String boletaId,
                                                    String moneda) {
        if (cuotas == null || cuotas.isEmpty()) return List.of();
        List<CuotaCreditoEntity> entities = new ArrayList<>();
        for (int i = 0; i < cuotas.size(); i++) {
            CuotaCredito c = cuotas.get(i);
            String numCuota = c.getNumeroCuota() != null ? c.getNumeroCuota()
                    : String.format("Cuota%03d", i + 1);
            LocalDate fv = c.getFechaVencimiento() != null
                    ? new java.sql.Date(c.getFechaVencimiento().getTime()).toLocalDate()
                    : null;
            entities.add(CuotaCreditoEntity.builder()
                    .facturaId(facturaId)
                    .boletaId(boletaId)
                    .numeroCuota(numCuota)
                    .monto(c.getMonto())
                    .fechaVencimiento(fv)
                    .moneda(c.getMoneda() != null ? c.getMoneda() : moneda)
                    .build());
        }
        return cuotaCreditoRepository.saveAll(entities);
    }

    /** Persist cargos/descuentos globales for factura or boleta */
    private List<AllowanceChargeEntity> persistAllowanceCharges(List<AllowanceChargeItem> items,
                                                                 String facturaId, String boletaId,
                                                                 String moneda) {
        if (items == null || items.isEmpty()) return List.of();
        List<AllowanceChargeEntity> entities = new ArrayList<>();
        for (AllowanceChargeItem ac : items) {
            entities.add(AllowanceChargeEntity.builder()
                    .facturaId(facturaId)
                    .boletaId(boletaId)
                    .chargeIndicator(ac.getChargeIndicator())
                    .reasonCode(ac.getReasonCode())
                    .reasonDescription(ac.getReasonDescription())
                    .multiplierFactor(ac.getMultiplierFactor())
                    .amount(ac.getAmount())
                    .baseAmount(ac.getBaseAmount())
                    .moneda(moneda)
                    .build());
        }
        return allowanceChargeRepository.saveAll(entities);
    }
}
