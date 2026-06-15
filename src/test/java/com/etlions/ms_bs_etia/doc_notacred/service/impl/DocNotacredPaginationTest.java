package com.etlions.ms_bs_etia.doc_notacred.service.impl;

import com.etlions.ms_bs_etia.app_util.service.XmlStorageService;
import com.etlions.ms_bs_etia.app_util.validation.DocumentValidatorService;
import com.etlions.ms_bs_etia.client.persistence.ClientRepository;
import com.etlions.ms_bs_etia.company.persistence.CompanyRepository;
import com.etlions.ms_bs_etia.doc_notacred.mapper.NotaCreditoMapper;
import com.etlions.ms_bs_etia.doc_notacred.persistence.NotaCreditoEntity;
import com.etlions.ms_bs_etia.doc_notacred.persistence.NotaCreditoRepository;
import com.etlions.ms_bs_etia.doc_notacred.xml.UblCreditNoteXmlBuilder;
import com.etlions.ms_bs_etia.notification.service.NotificationService;
import com.etlions.openapicodegen.dto.NotaCreditoData;
import com.etlions.openapicodegen.dto.NotaCreditoListResponseDto;
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
class DocNotacredPaginationTest {

    @Mock private NotaCreditoRepository notaCreditoRepository;
    @Mock private NotaCreditoMapper notaCreditoMapper;
    @Mock private NotificationService notificationService;
    @Mock private ClientRepository clientRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private UblCreditNoteXmlBuilder ublCreditNoteXmlBuilder;
    @Mock private XmlStorageService xmlStorageService;
    @Mock private DocumentValidatorService documentValidatorService;

    @InjectMocks
    private DocNotacredServiceImpl service;

    @Test
    void listarNotasCredito_debeUsarPaginacionReal() {
        NotaCreditoEntity entity = NotaCreditoEntity.builder().id("NC-1").companyId("COMP-1").serie("FC01").numero(10).build();
        Page<NotaCreditoEntity> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 1), 2);

        when(notaCreditoRepository.buscarConFiltros(eq("COMP-1"), isNull(), isNull(), isNull(), isNull(), isNull(), any()))
                .thenReturn(page);
        when(notaCreditoMapper.toData(any(NotaCreditoEntity.class))).thenReturn(new NotaCreditoData());

        NotaCreditoListResponseDto response = service.listarNotasCredito("COMP-1", null, null, null, null, null, 0, 1);

        assertTrue(response.getIsSuccess());
        assertEquals(1, response.getData().getContent().size());
        assertEquals(2L, response.getData().getPagination().getTotalElements());
        assertEquals(2, response.getData().getPagination().getTotalPages());
    }
}

