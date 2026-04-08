package com.etlions.ms_bs_etia.doc_factura.service.impl;

import com.etlions.ms_bs_etia.app_config.exception.ResourceNotFoundException;
import com.etlions.ms_bs_etia.app_config.exception.SunatValidationException;
import com.etlions.ms_bs_etia.app_util.service.CorrelativoService;
import com.etlions.ms_bs_etia.app_util.service.XmlStorageService;
import com.etlions.ms_bs_etia.app_util.validation.DocumentValidationResult;
import com.etlions.ms_bs_etia.app_util.validation.DocumentValidatorService;
import com.etlions.ms_bs_etia.app_util.validation.SunatFormatter;
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
    private final XmlStorageService xmlStorageService;
    private final DocumentValidatorService documentValidatorService;

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

        // ── 3. Calcular totalCargos / totalDescuentos desde AllowanceCharge items ──
        BigDecimal sumCargos = BigDecimal.ZERO;
        BigDecimal sumDescuentos = BigDecimal.ZERO;
        if (request.getCargosDescuentos() != null) {
            for (AllowanceChargeItem ac : request.getCargosDescuentos()) {
                if (Boolean.TRUE.equals(ac.getChargeIndicator())) {
                    sumCargos = sumCargos.add(ac.getAmount());
                } else {
                    sumDescuentos = sumDescuentos.add(ac.getAmount());
                }
            }
        }

        // ── 4. Validar montos no negativos y luego la suma (con cargos/descuentos) ──
        SunatValidator.validarMontosNoNegativos(request.getGravadas(), request.getIgv(), request.getTotal());
        SunatValidator.validarTotales(request.getGravadas(), request.getIgv(), request.getTotal(),
                sumCargos, sumDescuentos);

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

        // ── Set totalCargos/totalDescuentos (ya calculados arriba) ──
        entity.setTotalCargos(sumCargos);
        entity.setTotalDescuentos(sumDescuentos);

        entity.setFechaCreacion(LocalDateTime.now());
        FacturaEntity saved = facturaRepository.save(entity);

        // ── Persist cuotas de crédito ──
        List<CuotaCreditoEntity> cuotaEntities = persistCuotas(
                request.getCuotas(), saved.getId(), null, saved.getMoneda());

        // ── Persist cargos/descuentos globales ──
        List<AllowanceChargeEntity> acEntities = persistAllowanceCharges(
                request.getCargosDescuentos(), saved.getId(), null, saved.getMoneda());

        // ── Evaluar flag "emitir" (default: true) ──
        boolean debeEmitir = !Boolean.FALSE.equals(request.getEmitir());

        if (debeEmitir) {
            // ── Generar XML UBL 2.1, persistir y notificar ──
            saved = generateXmlAndNotify(saved, client, cuotaEntities, acEntities);
        } else {
            // ── Validación interna: determina estado VALIDADO o CON_ERROR ──
            saved = runInternalValidation(saved);
        }

        FacturaData data = facturaMapper.toData(saved);
        enrichFacturaData(data, cuotaEntities, acEntities);

        String serieNumero = SunatFormatter.formatNumeroComprobante(serie, numero);
        String msg = debeEmitir
                ? "Factura emitida exitosamente — " + serieNumero
                : "Factura guardada como borrador — " + serieNumero;
        return new FacturaResponseDto()
                .isSuccess(true)
                .status("OK")
                .message(msg)
                .timestamp(new Date())
                .data(data);
    }

    @Override
    @Transactional
    public FacturaResponseDto emitirFacturaBorrador(String facturaId) {
        FacturaEntity entity = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new ResourceNotFoundException("Factura", facturaId));

        // Solo se pueden emitir facturas en estado BORRADOR, VALIDADO o RECHAZADA (re-emisión)
        if (!"BORRADOR".equals(entity.getEstado()) && !"VALIDADO".equals(entity.getEstado())
                && !"RECHAZADA".equals(entity.getEstado())) {
            throw new SunatValidationException(
                    "Solo se pueden emitir facturas en estado BORRADOR, VALIDADO o RECHAZADA. Estado actual: " + entity.getEstado());
        }

        ClientEntity client = clientRepository.findById(entity.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", entity.getClientId()));

        List<CuotaCreditoEntity> cuotas = cuotaCreditoRepository
                .findByFacturaIdOrderByNumeroCuota(facturaId);
        List<AllowanceChargeEntity> acs = allowanceChargeRepository
                .findByFacturaId(facturaId);

        // ── Generar XML UBL 2.1, persistir y notificar ──
        FacturaEntity saved = generateXmlAndNotify(entity, client, cuotas, acs);

        FacturaData data = buildFacturaData(saved);
        String serieNumero = SunatFormatter.formatNumeroComprobante(saved.getSerie(), saved.getNumero());
        return new FacturaResponseDto()
                .isSuccess(true)
                .status("OK")
                .message("Factura emitida exitosamente — " + serieNumero)
                .timestamp(new Date())
                .data(data);
    }

    @Override
    @Transactional
    public FacturaResponseDto actualizarBorrador(String facturaId, FacturaEmitirDto request) {
        FacturaEntity entity = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new ResourceNotFoundException("Factura", facturaId));

        // Solo se pueden actualizar facturas en estado BORRADOR, VALIDADO, CON_ERROR o RECHAZADA
        if (!"BORRADOR".equals(entity.getEstado()) && !"VALIDADO".equals(entity.getEstado())
                && !"CON_ERROR".equals(entity.getEstado()) && !"RECHAZADA".equals(entity.getEstado())) {
            throw new SunatValidationException(
                    "Solo se pueden actualizar facturas en estado BORRADOR, VALIDADO, CON_ERROR o RECHAZADA. Estado actual: " + entity.getEstado());
        }

        // ── Calcular totalCargos / totalDescuentos ──
        BigDecimal sumCargos = BigDecimal.ZERO;
        BigDecimal sumDescuentos = BigDecimal.ZERO;
        if (request.getCargosDescuentos() != null) {
            for (AllowanceChargeItem ac : request.getCargosDescuentos()) {
                if (Boolean.TRUE.equals(ac.getChargeIndicator())) {
                    sumCargos = sumCargos.add(ac.getAmount());
                } else {
                    sumDescuentos = sumDescuentos.add(ac.getAmount());
                }
            }
        }

        // ── Validaciones SUNAT ──
        SunatValidator.validarMontosNoNegativos(request.getGravadas(), request.getIgv(), request.getTotal());
        SunatValidator.validarTotales(request.getGravadas(), request.getIgv(), request.getTotal(),
                sumCargos, sumDescuentos);
        SunatValidator.validarDetalleNoVacio(request.getDetalle());
        SunatValidator.validarItemsDetalle(
                request.getDetalle().stream().map(DetalleItem::getDescripcion).toList(),
                request.getDetalle().stream().map(DetalleItem::getCantidad).toList(),
                request.getDetalle().stream().map(DetalleItem::getValorUnitario).toList()
        );

        // ── Validar cliente ──
        if (request.getClientId() != null) {
            clientRepository.findById(request.getClientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente", request.getClientId()));
            entity.setClientId(request.getClientId());
        }

        // ── Moneda USD → tipo de cambio obligatorio ──
        String moneda = request.getMoneda() != null ? request.getMoneda().getValue() : null;
        if (moneda != null) {
            SunatValidator.validarMonedaYTipoCambio(moneda, request.getTipoCambio());
            entity.setMoneda(moneda);
        }

        // ── Actualizar campos mutables (NO serie, numero, estado, fechaEmision, fechaCreacion) ──
        String formaPagoStr = request.getFormaPago() != null ? request.getFormaPago().getValue() : null;
        if (formaPagoStr != null) entity.setFormaPago(formaPagoStr);
        if (request.getTipoCambio() != null) entity.setTipoCambio(request.getTipoCambio());

        LocalDate fechaVenc = request.getFechaVencimiento() != null
                ? new java.sql.Date(request.getFechaVencimiento().getTime()).toLocalDate()
                : null;
        if (fechaVenc != null) entity.setFechaVencimiento(fechaVenc);

        if (request.getTipoOperacion() != null) entity.setTipoOperacion(request.getTipoOperacion());
        if (request.getGravadas() != null) entity.setGravadas(request.getGravadas());
        if (request.getExoneradas() != null) entity.setExoneradas(request.getExoneradas());
        if (request.getInafectas() != null) entity.setInafectas(request.getInafectas());
        if (request.getIgv() != null) entity.setIgv(request.getIgv());
        if (request.getTotal() != null) entity.setTotal(request.getTotal());
        if (request.getSubtotal() != null) entity.setSubtotal(request.getSubtotal());
        if (request.getTotalConImpuestos() != null) entity.setTotalConImpuestos(request.getTotalConImpuestos());
        if (request.getLeyenda() != null) entity.setLeyenda(request.getLeyenda());
        if (request.getCodigoUbigeo() != null) entity.setCodigoUbigeo(request.getCodigoUbigeo());
        entity.setTotalCargos(sumCargos);
        entity.setTotalDescuentos(sumDescuentos);

        // ── Actualizar detalle JSON ──
        if (request.getDetalle() != null) {
            entity.setDetalleJson(facturaMapper.detalleToJson(request.getDetalle()));
        }

        // ── CONTADO → fechaVencimiento = fechaEmision ──
        if ("CONTADO".equalsIgnoreCase(entity.getFormaPago())) {
            entity.setFechaVencimiento(entity.getFechaEmision());
        }
        if (entity.getFechaVencimiento() == null) {
            entity.setFechaVencimiento(entity.getFechaEmision());
        }

        FacturaEntity saved = facturaRepository.save(entity);

        // ── Reemplazar cuotas y cargos/descuentos ──
        cuotaCreditoRepository.deleteByFacturaId(facturaId);
        allowanceChargeRepository.deleteByFacturaId(facturaId);

        List<CuotaCreditoEntity> cuotaEntities = persistCuotas(
                request.getCuotas(), saved.getId(), null, saved.getMoneda());
        List<AllowanceChargeEntity> acEntities = persistAllowanceCharges(
                request.getCargosDescuentos(), saved.getId(), null, saved.getMoneda());

        // ── Validación interna: determina estado VALIDADO o CON_ERROR ──
        saved = runInternalValidation(saved);

        FacturaData data = facturaMapper.toData(saved);
        enrichFacturaData(data, cuotaEntities, acEntities);

        String serieNumero = SunatFormatter.formatNumeroComprobante(saved.getSerie(), saved.getNumero());
        return new FacturaResponseDto()
                .isSuccess(true)
                .status("OK")
                .message("Borrador actualizado exitosamente — " + serieNumero)
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
        String sn = SunatFormatter.formatNumeroComprobante(saved.getSerie(), saved.getNumero());
        notificationService.publicar(saved.getCompanyId(), "ANULADA", "FACTURA",
                saved.getId(), sn,
                "Factura " + sn + " anulada");
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

        // SUNAT filename: {RUC}-{tipoDoc}-{serie}-{numero}.xml
        String filename = SunatFormatter.formatFilename(
                company.getRuc(), "01", factura.getSerie(), factura.getNumero());

        // 1) Intentar leer desde disco (xmlPath) — fuente primaria, inmutable
        var fromDisk = xmlStorageService.leerXml(factura.getXmlPath());
        if (fromDisk.isPresent()) {
            return new FacturaXmlResult(
                    fromDisk.get().getBytes(java.nio.charset.StandardCharsets.UTF_8), filename, true);
        }

        // 2) Fallback: leer desde BD (xmlContent) — copia fiel del XML generado al emitir
        if (factura.getXmlContent() != null && !factura.getXmlContent().isBlank()) {
            return new FacturaXmlResult(
                    factura.getXmlContent().getBytes(java.nio.charset.StandardCharsets.UTF_8), filename, true);
        }

        // 3) Sin XML persistido — la factura aún no fue emitida (BORRADOR) o es legacy.
        //    NO se genera on-the-fly para garantizar que lo descargado sea EXACTAMENTE
        //    lo que se transmitió/transmitirá a SUNAT. El usuario debe emitir primero.
        return new FacturaXmlResult(null, filename, false);
    }

    // ═══════ VALIDACIÓN INTERNA ═══════

    @Override
    @Transactional
    public DocumentValidationResultDto validarFactura(String facturaId) {
        FacturaEntity entity = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new ResourceNotFoundException("Factura", facturaId));

        // Solo se pueden validar facturas editables
        if (!"BORRADOR".equals(entity.getEstado()) && !"VALIDADO".equals(entity.getEstado())
                && !"CON_ERROR".equals(entity.getEstado())) {
            throw new SunatValidationException(
                    "Solo se pueden validar facturas en estado BORRADOR, VALIDADO o CON_ERROR. Estado actual: " + entity.getEstado());
        }

        DocumentValidationResult result = documentValidatorService.validarFactura(
                entity.getSerie(), entity.getNumero(), entity.getClientId(),
                entity.getMoneda(), entity.getTipoCambio(),
                entity.getFormaPago(), entity.getFechaEmision(), entity.getFechaVencimiento(),
                entity.getTipoOperacion(), entity.getLeyenda(),
                entity.getGravadas(), entity.getIgv(), entity.getTotal(),
                entity.getTotalCargos(), entity.getTotalDescuentos(),
                entity.getDetalleJson());

        if (result.valid()) {
            entity.setEstado("VALIDADO");
            entity.setErroresValidacion(null);
        } else {
            entity.setEstado("CON_ERROR");
            entity.setErroresValidacion(documentValidatorService.erroresAJson(result.errores()));
        }
        facturaRepository.save(entity);

        return new DocumentValidationResultDto()
                .valid(result.valid())
                .errores(result.errores())
                .advertencias(result.advertencias());
    }

    /**
     * Ejecuta validación interna sobre una factura y actualiza su estado.
     * Reutilizado por emitirFactura (emitir=false) y actualizarBorrador.
     */
    private FacturaEntity runInternalValidation(FacturaEntity entity) {
        DocumentValidationResult result = documentValidatorService.validarFactura(
                entity.getSerie(), entity.getNumero(), entity.getClientId(),
                entity.getMoneda(), entity.getTipoCambio(),
                entity.getFormaPago(), entity.getFechaEmision(), entity.getFechaVencimiento(),
                entity.getTipoOperacion(), entity.getLeyenda(),
                entity.getGravadas(), entity.getIgv(), entity.getTotal(),
                entity.getTotalCargos(), entity.getTotalDescuentos(),
                entity.getDetalleJson());

        if (result.valid()) {
            entity.setEstado("VALIDADO");
            entity.setErroresValidacion(null);
        } else {
            entity.setEstado("CON_ERROR");
            entity.setErroresValidacion(documentValidatorService.erroresAJson(result.errores()));
        }
        return facturaRepository.save(entity);
    }

    // ═══════ HELPERS ═══════

    /**
     * Genera XML UBL 2.1, lo persiste en disco + BD, y publica notificación.
     * Reutilizado por emitirFactura (emitir=true) y emitirFacturaBorrador.
     */
    private FacturaEntity generateXmlAndNotify(FacturaEntity entity,
                                                ClientEntity client,
                                                List<CuotaCreditoEntity> cuotas,
                                                List<AllowanceChargeEntity> acs) {
        final var company = companyRepository.findById(entity.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", entity.getCompanyId()));

        String xmlContent = ublInvoiceXmlBuilder.buildInvoiceXml(entity, company, client, cuotas, acs);
        entity.setXmlContent(xmlContent);

        String xmlRelPath = xmlStorageService.guardarXml(
                company.getRuc(), "01", entity.getSerie(), entity.getNumero(), xmlContent);
        entity.setXmlPath(xmlRelPath);

        // ── Transición de estado: BORRADOR/RECHAZADA → ENVIADA ──
        entity.setEstado("ENVIADA");

        FacturaEntity saved = facturaRepository.save(entity);

        String serieNumero = SunatFormatter.formatNumeroComprobante(saved.getSerie(), saved.getNumero());
        notificationService.publicar(saved.getCompanyId(), "EMITIDA", "FACTURA",
                saved.getId(), serieNumero,
                "Factura " + serieNumero + " emitida");

        return saved;
    }

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
