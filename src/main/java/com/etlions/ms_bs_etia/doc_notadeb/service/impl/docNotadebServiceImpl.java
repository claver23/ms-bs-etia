package com.etlions.ms_bs_etia.doc_notadeb.service.impl;

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
import com.etlions.ms_bs_etia.doc_notadeb.mapper.NotaDebitoMapper;
import com.etlions.ms_bs_etia.doc_notadeb.persistence.NotaDebitoEntity;
import com.etlions.ms_bs_etia.doc_notadeb.persistence.NotaDebitoRepository;
import com.etlions.ms_bs_etia.doc_notadeb.service.DocNotadebService;
import com.etlions.ms_bs_etia.doc_notadeb.xml.UblDebitNoteXmlBuilder;
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
public class DocNotadebServiceImpl implements DocNotadebService {

    private final NotaDebitoRepository notaDebitoRepository;
    private final NotaDebitoMapper notaDebitoMapper;
    private final NotificationService notificationService;
    private final ClientRepository clientRepository;
    private final CompanyRepository companyRepository;
    private final UblDebitNoteXmlBuilder ublDebitNoteXmlBuilder;
    private final XmlStorageService xmlStorageService;
    private final DocumentValidatorService documentValidatorService;

    @Override
    @Transactional
    public NotaDebitoResponseDto emitirNotaDebito(NotaDebitoEmitirDto request) {
        NotaDebitoEntity entity = notaDebitoMapper.toEntity(request);
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
            entity.setCodigoMotivoSunat("01"); // Catálogo 10: Intereses por mora
        }

        NotaDebitoEntity saved = notaDebitoRepository.save(entity);
        NotaDebitoData data = notaDebitoMapper.toData(saved);

        notificationService.publicar(saved.getCompanyId(), "EMITIDA", "NOTA_DEBITO",
                saved.getId(),
                saved.getSerie() != null && saved.getNumero() != null
                        ? saved.getSerie() + "-" + String.format("%08d", saved.getNumero())
                        : "",
                "Nota de débito emitida");

        return new NotaDebitoResponseDto()
                .isSuccess(true)
                .status("OK")
                .message("Nota de débito emitida exitosamente")
                .timestamp(new Date())
                .data(data);
    }

    @Override
    @Transactional
    public NotaDebitoResponseDto actualizarNotaDebitoBorrador(String notaDebitoId,
                                                                                NotaDebitoUpdateDto request) {
        NotaDebitoEntity entity = notaDebitoRepository.findById(notaDebitoId)
                .orElseThrow(() -> new ResourceNotFoundException("Nota de débito", notaDebitoId));
        SunatValidator.validarEliminacionPermitida(entity.getEstado(), "Nota de débito", notaDebitoId);
        notaDebitoMapper.updateEntity(request, entity);
        NotaDebitoEntity saved = notaDebitoRepository.save(entity);
        NotaDebitoData data = notaDebitoMapper.toData(saved);
        return new NotaDebitoResponseDto()
                .isSuccess(true)
                .status("OK")
                .message("Nota de débito actualizada exitosamente")
                .timestamp(new Date())
                .data(data);
    }

    @Override
    @Transactional
    public SuccessResponse eliminarNotaDebitoBorrador(String notaDebitoId) {
        NotaDebitoEntity entity = notaDebitoRepository.findById(notaDebitoId)
                .orElseThrow(() -> new ResourceNotFoundException("Nota de débito", notaDebitoId));
        SunatValidator.validarEliminacionPermitida(entity.getEstado(), "Nota de débito", notaDebitoId);
        notaDebitoRepository.delete(entity);
        return new SuccessResponse()
                .isSuccess(true)
                .status("OK")
                .message("Borrador eliminado exitosamente")
                .timestamp(new Date());
    }

    @Override
    public NotaDebitoListResponseDto listarNotasDebito(String companyId, String facturaReferenciaId,
                                                           Date desdeFecha, Date hastaFecha,
                                                           String serie, String estado,
                                                           Integer page, Integer size) {
        List<NotaDebitoEntity> entities = notaDebitoRepository.findByCompanyId(companyId);
        List<NotaDebitoData> dataList = entities.stream()
                .map(notaDebitoMapper::toData)
                .toList();
        NotaDebitoListResponseDtoAllOfData responseData = new NotaDebitoListResponseDtoAllOfData();
        responseData.setContent(dataList);
        responseData.setPagination(new PaginationMeta()
                .currentPage(page)
                .pageSize(size)
                .totalElements((long) dataList.size())
                .totalPages(1));
        NotaDebitoListResponseDto response = new NotaDebitoListResponseDto();
        response.setIsSuccess(true);
        response.setStatus("OK");
        response.setMessage("Operación exitosa");
        response.setTimestamp(new Date());
        response.setData(responseData);
        return response;
    }

    @Override
    public NotaDebitoResponseDto obtenerNotaDebito(String notaDebitoId) {
        NotaDebitoEntity entity = notaDebitoRepository.findById(notaDebitoId)
                .orElseThrow(() -> new ResourceNotFoundException("Nota de débito", notaDebitoId));
        NotaDebitoData data = notaDebitoMapper.toData(entity);
        return new NotaDebitoResponseDto()
                .isSuccess(true)
                .status("OK")
                .message("Operación exitosa")
                .timestamp(new Date())
                .data(data);
    }

    // ═══════ VALIDACIÓN INTERNA ═══════

    @Override
    @Transactional
    public DocumentValidationResultDto validarNotaDebito(String notaDebitoId) {
        NotaDebitoEntity entity = notaDebitoRepository.findById(notaDebitoId)
                .orElseThrow(() -> new ResourceNotFoundException("Nota de débito", notaDebitoId));

        if (!"BORRADOR".equals(entity.getEstado()) && !"VALIDADO".equals(entity.getEstado())
                && !"CON_ERROR".equals(entity.getEstado())) {
            throw new SunatValidationException(
                    "Solo se pueden validar notas de débito en estado BORRADOR, VALIDADO o CON_ERROR. Estado actual: " + entity.getEstado());
        }

        DocumentValidationResult result = documentValidatorService.validarNotaDebito(
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
        notaDebitoRepository.save(entity);

        return new DocumentValidationResultDto()
                .valid(result.valid())
                .errores(result.errores())
                .advertencias(result.advertencias());
    }

    // ═══════ EMITIR BORRADOR ═══════

    @Override
    @Transactional
    public NotaDebitoResponseDto emitirNotaDebitoBorrador(String notaDebitoId) {
        NotaDebitoEntity entity = notaDebitoRepository.findById(notaDebitoId)
                .orElseThrow(() -> new ResourceNotFoundException("Nota de débito", notaDebitoId));

        if (!"BORRADOR".equals(entity.getEstado()) && !"VALIDADO".equals(entity.getEstado())
                && !"RECHAZADA".equals(entity.getEstado())) {
            throw new SunatValidationException(
                    "Solo se pueden emitir notas de débito en estado BORRADOR, VALIDADO o RECHAZADA. Estado actual: " + entity.getEstado());
        }

        ClientEntity client = clientRepository.findById(entity.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", entity.getClientId()));
        var company = companyRepository.findById(entity.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", entity.getCompanyId()));

        // ── Generar XML UBL 2.1 DebitNote ──
        String xmlContent = ublDebitNoteXmlBuilder.buildDebitNoteXml(entity, company, client);
        entity.setXmlContent(xmlContent);

        String xmlRelPath = xmlStorageService.guardarXml(
                company.getRuc(), "08", entity.getSerie(), entity.getNumero(), xmlContent);
        entity.setXmlPath(xmlRelPath);

        entity.setEstado("ENVIADA");
        NotaDebitoEntity saved = notaDebitoRepository.save(entity);

        String serieNumero = SunatFormatter.formatNumeroComprobante(saved.getSerie(), saved.getNumero());
        notificationService.publicar(saved.getCompanyId(), "EMITIDA", "NOTA_DEBITO",
                saved.getId(), serieNumero,
                "Nota de débito " + serieNumero + " emitida");

        NotaDebitoData data = notaDebitoMapper.toData(saved);
        return new NotaDebitoResponseDto()
                .isSuccess(true)
                .status("OK")
                .message("Nota de débito emitida exitosamente — " + serieNumero)
                .timestamp(new Date())
                .data(data);
    }

    // ═══════ DESCARGA XML ═══════

    @Override
    public NotaDebitoXmlResult descargarNotaDebitoXml(String notaDebitoId) {
        NotaDebitoEntity nd = notaDebitoRepository.findById(notaDebitoId)
                .orElseThrow(() -> new ResourceNotFoundException("Nota de débito", notaDebitoId));

        var company = companyRepository.findById(nd.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", nd.getCompanyId()));

        String filename = SunatFormatter.formatFilename(
                company.getRuc(), "08", nd.getSerie(), nd.getNumero());

        // 1) Leer desde disco
        var fromDisk = xmlStorageService.leerXml(nd.getXmlPath());
        if (fromDisk.isPresent()) {
            return new NotaDebitoXmlResult(
                    fromDisk.get().getBytes(StandardCharsets.UTF_8), filename, true);
        }

        // 2) Fallback: leer desde BD
        if (nd.getXmlContent() != null && !nd.getXmlContent().isBlank()) {
            return new NotaDebitoXmlResult(
                    nd.getXmlContent().getBytes(StandardCharsets.UTF_8), filename, true);
        }

        // 3) Sin XML — aún no fue emitida
        return new NotaDebitoXmlResult(null, filename, false);
    }
}
