package com.etlions.ms_bs_etia.doc_notacred.service.impl;

import com.etlions.ms_bs_etia.app_config.exception.ResourceNotFoundException;
import com.etlions.ms_bs_etia.app_config.exception.SunatValidationException;
import com.etlions.ms_bs_etia.app_util.service.XmlStorageService;
import com.etlions.ms_bs_etia.app_util.validation.DocumentValidationResult;
import com.etlions.ms_bs_etia.app_util.validation.DocumentValidatorService;
import com.etlions.ms_bs_etia.app_util.validation.SunatFormatter;
import com.etlions.ms_bs_etia.app_util.validation.SunatValidator;
import com.etlions.ms_bs_etia.client.persistence.ClientEntity;
import com.etlions.ms_bs_etia.client.persistence.ClientRepository;
import com.etlions.ms_bs_etia.company.persistence.CompanyRepository;
import com.etlions.ms_bs_etia.doc_notacred.mapper.NotaCreditoMapper;
import com.etlions.ms_bs_etia.doc_notacred.persistence.NotaCreditoEntity;
import com.etlions.ms_bs_etia.doc_notacred.persistence.NotaCreditoRepository;
import com.etlions.ms_bs_etia.doc_notacred.service.DocNotacredService;
import com.etlions.ms_bs_etia.doc_notacred.xml.UblCreditNoteXmlBuilder;
import com.etlions.ms_bs_etia.notification.service.NotificationService;
import com.etlions.openapicodegen.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocNotacredServiceImpl implements DocNotacredService {

    private final NotaCreditoRepository notaCreditoRepository;
    private final NotaCreditoMapper notaCreditoMapper;
    private final NotificationService notificationService;
    private final ClientRepository clientRepository;
    private final CompanyRepository companyRepository;
    private final UblCreditNoteXmlBuilder ublCreditNoteXmlBuilder;
    private final XmlStorageService xmlStorageService;
    private final DocumentValidatorService documentValidatorService;

    @Override
    @Transactional
    public NotaCreditoResponseDto emitirNotaCredito(NotaCreditoEmitirDto request) {
        NotaCreditoEntity entity = notaCreditoMapper.toEntity(request);
        entity.setEstado("BORRADOR");
        entity.setFechaCreacion(LocalDateTime.now());

        // Defaults si no se proporcionan
        if (entity.getFechaEmision() == null) {
            entity.setFechaEmision(LocalDate.now());
        }
        if (entity.getHoraEmision() == null) {
            entity.setHoraEmision(LocalTime.now());
        }
        if (entity.getMoneda() == null) {
            entity.setMoneda("PEN");
        }
        if (entity.getTipoOperacion() == null) {
            entity.setTipoOperacion("0101");
        }
        if (entity.getFormaPago() == null) {
            entity.setFormaPago("CONTADO");
        }
        if (entity.getCodigoMotivoSunat() == null) {
            entity.setCodigoMotivoSunat("01"); // Catálogo 09: Anulación de la operación
        }

        NotaCreditoEntity saved = notaCreditoRepository.save(entity);
        NotaCreditoData data = notaCreditoMapper.toData(saved);

        notificationService.publicar(saved.getCompanyId(), "EMITIDA", "NOTA_CREDITO",
                saved.getId(),
                saved.getSerie() != null && saved.getNumero() != null
                        ? saved.getSerie() + "-" + String.format("%08d", saved.getNumero())
                        : "",
                "Nota de crédito emitida");

        return new NotaCreditoResponseDto()
                .isSuccess(true)
                .status("OK")
                .message("Nota de crédito emitida exitosamente")
                .timestamp(new Date())
                .data(data);
    }

    @Override
    @Transactional
    public NotaCreditoResponseDto actualizarNotaCreditoBorrador(String notaCreditoId,
                                                                                  NotaCreditoUpdateDto request) {
        NotaCreditoEntity entity = notaCreditoRepository.findById(notaCreditoId)
                .orElseThrow(() -> new ResourceNotFoundException("Nota de crédito", notaCreditoId));
        SunatValidator.validarEliminacionPermitida(entity.getEstado(), "Nota de crédito", notaCreditoId);
        notaCreditoMapper.updateEntity(request, entity);
        NotaCreditoEntity saved = notaCreditoRepository.save(entity);
        NotaCreditoData data = notaCreditoMapper.toData(saved);
        return new NotaCreditoResponseDto()
                .isSuccess(true)
                .status("OK")
                .message("Nota de crédito actualizada exitosamente")
                .timestamp(new Date())
                .data(data);
    }

    @Override
    @Transactional
    public SuccessResponse eliminarNotaCreditoBorrador(String notaCreditoId) {
        NotaCreditoEntity entity = notaCreditoRepository.findById(notaCreditoId)
                .orElseThrow(() -> new ResourceNotFoundException("Nota de crédito", notaCreditoId));
        SunatValidator.validarEliminacionPermitida(entity.getEstado(), "Nota de crédito", notaCreditoId);
        notaCreditoRepository.delete(entity);
        return new SuccessResponse()
                .isSuccess(true)
                .status("OK")
                .message("Borrador eliminado exitosamente")
                .timestamp(new Date());
    }

    @Override
    public NotaCreditoListResponseDto listarNotasCredito(String companyId, String facturaReferenciaId,
                                                             Date desdeFecha, Date hastaFecha,
                                                             String serie, String estado,
                                                             Integer page, Integer size) {
        List<NotaCreditoEntity> entities = notaCreditoRepository.findByCompanyId(companyId);
        List<NotaCreditoData> dataList = entities.stream()
                .map(notaCreditoMapper::toData)
                .toList();
        NotaCreditoListResponseDtoAllOfData responseData = new NotaCreditoListResponseDtoAllOfData();
        responseData.setContent(dataList);
        responseData.setPagination(new PaginationMeta()
                .currentPage(page)
                .pageSize(size)
                .totalElements((long) dataList.size())
                .totalPages(1));
        NotaCreditoListResponseDto response = new NotaCreditoListResponseDto();
        response.setIsSuccess(true);
        response.setStatus("OK");
        response.setMessage("Operación exitosa");
        response.setTimestamp(new Date());
        response.setData(responseData);
        return response;
    }

    @Override
    public NotaCreditoResponseDto obtenerNotaCredito(String notaCreditoId) {
        NotaCreditoEntity entity = notaCreditoRepository.findById(notaCreditoId)
                .orElseThrow(() -> new ResourceNotFoundException("Nota de crédito", notaCreditoId));
        NotaCreditoData data = notaCreditoMapper.toData(entity);
        return new NotaCreditoResponseDto()
                .isSuccess(true)
                .status("OK")
                .message("Operación exitosa")
                .timestamp(new Date())
                .data(data);
    }

    // ═══════ VALIDACIÓN INTERNA ═══════

    @Override
    @Transactional
    public DocumentValidationResultDto validarNotaCredito(String notaCreditoId) {
        NotaCreditoEntity entity = notaCreditoRepository.findById(notaCreditoId)
                .orElseThrow(() -> new ResourceNotFoundException("Nota de crédito", notaCreditoId));

        if (!"BORRADOR".equals(entity.getEstado()) && !"VALIDADO".equals(entity.getEstado())
                && !"CON_ERROR".equals(entity.getEstado())) {
            throw new SunatValidationException(
                    "Solo se pueden validar notas de crédito en estado BORRADOR, VALIDADO o CON_ERROR. Estado actual: " + entity.getEstado());
        }

        DocumentValidationResult result = documentValidatorService.validarNotaCredito(
                entity.getSerie(), entity.getNumero(), entity.getClientId(),
                entity.getMoneda(), entity.getTipoCambio(),
                entity.getFormaPago(), entity.getFechaEmision(), entity.getFechaVencimiento(),
                entity.getTipoOperacion(), entity.getLeyenda(),
                entity.getGravadas(), entity.getIgv(), entity.getTotal(),
                entity.getTotalCargos(), entity.getTotalDescuentos(),
                entity.getDetalleJson(),
                entity.getSerieDocRef(), entity.getNumeroDocRef(),
                entity.getTipoDocumentoReferencia(),
                entity.getCodigoMotivoSunat(), entity.getMotivo());

        if (result.valid()) {
            entity.setEstado("VALIDADO");
            entity.setErroresValidacion(null);
        } else {
            entity.setEstado("CON_ERROR");
            entity.setErroresValidacion(documentValidatorService.erroresAJson(result.errores()));
        }
        notaCreditoRepository.save(entity);

        return new DocumentValidationResultDto()
                .valid(result.valid())
                .errores(result.errores())
                .advertencias(result.advertencias());
    }

    // ═══════ EMITIR BORRADOR ═══════

    @Override
    @Transactional
    public NotaCreditoResponseDto emitirNotaCreditoBorrador(String notaCreditoId) {
        NotaCreditoEntity entity = notaCreditoRepository.findById(notaCreditoId)
                .orElseThrow(() -> new ResourceNotFoundException("Nota de crédito", notaCreditoId));

        if (!"BORRADOR".equals(entity.getEstado()) && !"VALIDADO".equals(entity.getEstado())
                && !"RECHAZADA".equals(entity.getEstado())) {
            throw new SunatValidationException(
                    "Solo se pueden emitir notas de crédito en estado BORRADOR, VALIDADO o RECHAZADA. Estado actual: " + entity.getEstado());
        }

        ClientEntity client = clientRepository.findById(entity.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", entity.getClientId()));
        var company = companyRepository.findById(entity.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", entity.getCompanyId()));

        // ── Generar XML UBL 2.1 CreditNote ──
        String xmlContent = ublCreditNoteXmlBuilder.buildCreditNoteXml(entity, company, client);
        entity.setXmlContent(xmlContent);

        String xmlRelPath = xmlStorageService.guardarXml(
                company.getRuc(), "07", entity.getSerie(), entity.getNumero(), xmlContent);
        entity.setXmlPath(xmlRelPath);

        entity.setEstado("ENVIADA");
        NotaCreditoEntity saved = notaCreditoRepository.save(entity);

        String serieNumero = SunatFormatter.formatNumeroComprobante(saved.getSerie(), saved.getNumero());
        notificationService.publicar(saved.getCompanyId(), "EMITIDA", "NOTA_CREDITO",
                saved.getId(), serieNumero,
                "Nota de crédito " + serieNumero + " emitida");

        NotaCreditoData data = notaCreditoMapper.toData(saved);
        return new NotaCreditoResponseDto()
                .isSuccess(true)
                .status("OK")
                .message("Nota de crédito emitida exitosamente — " + serieNumero)
                .timestamp(new Date())
                .data(data);
    }

    // ═══════ DESCARGA XML ═══════

    @Override
    public NotaCreditoXmlResult descargarNotaCreditoXml(String notaCreditoId) {
        NotaCreditoEntity nc = notaCreditoRepository.findById(notaCreditoId)
                .orElseThrow(() -> new ResourceNotFoundException("Nota de crédito", notaCreditoId));

        var company = companyRepository.findById(nc.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", nc.getCompanyId()));

        String filename = SunatFormatter.formatFilename(
                company.getRuc(), "07", nc.getSerie(), nc.getNumero());

        // 1) Leer desde disco
        var fromDisk = xmlStorageService.leerXml(nc.getXmlPath());
        if (fromDisk.isPresent()) {
            return new NotaCreditoXmlResult(
                    fromDisk.get().getBytes(StandardCharsets.UTF_8), filename, true);
        }

        // 2) Fallback: leer desde BD
        if (nc.getXmlContent() != null && !nc.getXmlContent().isBlank()) {
            return new NotaCreditoXmlResult(
                    nc.getXmlContent().getBytes(StandardCharsets.UTF_8), filename, true);
        }

        // 3) Sin XML — aún no fue emitida
        return new NotaCreditoXmlResult(null, filename, false);
    }
}
