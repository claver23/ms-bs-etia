package com.etlions.ms_bs_etia.doc_notadeb.service.impl;

import com.etlions.ms_bs_etia.app_util.service.XmlStorageService;
import com.etlions.ms_bs_etia.app_util.validation.DocumentValidatorService;
import com.etlions.ms_bs_etia.client.persistence.ClientRepository;
import com.etlions.ms_bs_etia.company.persistence.CompanyRepository;
import com.etlions.ms_bs_etia.doc_notadeb.mapper.NotaDebitoMapper;
import com.etlions.ms_bs_etia.doc_notadeb.persistence.NotaDebitoEntity;
import com.etlions.ms_bs_etia.doc_notadeb.persistence.NotaDebitoRepository;
import com.etlions.ms_bs_etia.doc_notadeb.xml.UblDebitNoteXmlBuilder;
import com.etlions.ms_bs_etia.notification.service.NotificationService;
import com.etlions.openapicodegen.dto.NotaDebitoData;
import com.etlions.openapicodegen.dto.NotaDebitoListResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocNotadebPaginationTest {

    @Mock private NotaDebitoRepository notaDebitoRepository;
    @Mock private NotaDebitoMapper notaDebitoMapper;
    @Mock private NotificationService notificationService;
    @Mock private ClientRepository clientRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private UblDebitNoteXmlBuilder ublDebitNoteXmlBuilder;
    @Mock private XmlStorageService xmlStorageService;
    @Mock private DocumentValidatorService documentValidatorService;

    @InjectMocks
    private DocNotadebServiceImpl service;

    @Test
    void listarNotasDebito_debeUsarPaginacionReal() {
        NotaDebitoEntity entity = NotaDebitoEntity.builder().id("ND-1").companyId("COMP-1").serie("FD01").numero(12).build();
        Page<NotaDebitoEntity> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 1), 3);

        when(notaDebitoRepository.buscarConFiltros(eq("COMP-1"), isNull(), isNull(), isNull(), isNull(), isNull(), any()))
                .thenReturn(page);
        when(notaDebitoMapper.toData(any(NotaDebitoEntity.class))).thenReturn(new NotaDebitoData());

        NotaDebitoListResponseDto response = service.listarNotasDebito("COMP-1", null, null, null, null, null, 0, 1);

        assertTrue(response.getIsSuccess());
        assertEquals(1, response.getData().getContent().size());
        assertEquals(3L, response.getData().getPagination().getTotalElements());
        assertEquals(3, response.getData().getPagination().getTotalPages());
    }
}

