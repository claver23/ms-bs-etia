package com.etlions.ms_bs_etia.doc_factura.service.impl;

import com.etlions.ms_bs_etia.app_config.exception.ResourceNotFoundException;
import com.etlions.ms_bs_etia.app_util.service.CorrelativoService;
import com.etlions.ms_bs_etia.app_util.validation.SunatValidator;
import com.etlions.ms_bs_etia.client.persistence.ClientEntity;
import com.etlions.ms_bs_etia.client.persistence.ClientRepository;
import com.etlions.ms_bs_etia.company.persistence.CompanyRepository;
import com.etlions.ms_bs_etia.doc_factura.mapper.FacturaMapper;
import com.etlions.ms_bs_etia.doc_factura.persistence.FacturaEntity;
import com.etlions.ms_bs_etia.doc_factura.persistence.FacturaRepository;
import com.etlions.ms_bs_etia.doc_factura.service.DocFacturaService;
import com.etlions.ms_bs_etia.notification.service.NotificationService;
import com.etlions.openapicodegen.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
        /* If CONTADO, due date = emission date (date only, no time) */
        if ("CONTADO".equalsIgnoreCase(entity.getFormaPago()) || entity.getFormaPago() == null) {
            entity.setFechaVencimiento(entity.getFechaEmision());
            if (entity.getFormaPago() == null) entity.setFormaPago("CONTADO");
        }
        if (entity.getFechaVencimiento() == null) {
            entity.setFechaVencimiento(entity.getFechaEmision());
        }
        entity.setFechaCreacion(LocalDateTime.now());
        FacturaEntity saved = facturaRepository.save(entity);
        FacturaData data = facturaMapper.toData(saved);
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
        FacturaData data = facturaMapper.toData(saved);
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
                .map(facturaMapper::toData)
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
        FacturaData data = facturaMapper.toData(entity);
        return new FacturaResponseDto()
                .isSuccess(true)
                .status("OK")
                .message("Operación exitosa")
                .timestamp(new Date())
                .data(data);
    }
}

