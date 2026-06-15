package com.etlions.ms_bs_etia.doc_boleta.service.impl;

import com.etlions.ms_bs_etia.app_util.service.CorrelativoService;
import com.etlions.ms_bs_etia.app_util.service.PdfGeneratorService;
import com.etlions.ms_bs_etia.app_util.service.XmlStorageService;
import com.etlions.ms_bs_etia.app_util.validation.DocumentValidatorService;
import com.etlions.ms_bs_etia.client.persistence.ClientRepository;
import com.etlions.ms_bs_etia.company.persistence.CompanyRepository;
import com.etlions.ms_bs_etia.doc_boleta.mapper.BoletaMapper;
import com.etlions.ms_bs_etia.doc_boleta.persistence.BoletaEntity;
import com.etlions.ms_bs_etia.doc_boleta.persistence.BoletaRepository;
import com.etlions.ms_bs_etia.doc_factura.persistence.AllowanceChargeRepository;
import com.etlions.ms_bs_etia.doc_factura.persistence.CuotaCreditoRepository;
import com.etlions.ms_bs_etia.doc_factura.xml.UblInvoiceXmlBuilder;
import com.etlions.ms_bs_etia.notification.service.NotificationService;
import com.etlions.openapicodegen.dto.BoletaData;
import com.etlions.openapicodegen.dto.BoletaListResponseDto;
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
class DocBoletaPaginationTest {

    @Mock private BoletaRepository boletaRepository;
    @Mock private BoletaMapper boletaMapper;
    @Mock private CorrelativoService correlativoService;
    @Mock private NotificationService notificationService;
    @Mock private ClientRepository clientRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private CuotaCreditoRepository cuotaCreditoRepository;
    @Mock private AllowanceChargeRepository allowanceChargeRepository;
    @Mock private DocumentValidatorService documentValidatorService;
    @Mock private UblInvoiceXmlBuilder ublInvoiceXmlBuilder;
    @Mock private XmlStorageService xmlStorageService;
    @Mock private PdfGeneratorService pdfGeneratorService;

    @InjectMocks
    private DocBoletaServiceImpl service;

    @Test
    void listarBoletas_debeUsarPaginacionReal() {
        BoletaEntity e1 = BoletaEntity.builder().id("B1").companyId("COMP-1").serie("B001").numero(100).build();
        Page<BoletaEntity> page = new PageImpl<>(List.of(e1), PageRequest.of(1, 1), 4);

        when(boletaRepository.buscarConFiltros(eq("COMP-1"), isNull(), isNull(), isNull(), isNull(), any()))
                .thenReturn(page);
        when(cuotaCreditoRepository.findByBoletaIdOrderByNumeroCuota(anyString())).thenReturn(List.of());
        when(allowanceChargeRepository.findByBoletaId(anyString())).thenReturn(List.of());
        when(boletaMapper.toData(any(BoletaEntity.class))).thenReturn(new BoletaData());

        BoletaListResponseDto response = service.listarBoletas("COMP-1", null, null, null, null, 1, 1);

        assertTrue(response.getIsSuccess());
        assertEquals(1, response.getData().getContent().size());
        assertEquals(4L, response.getData().getPagination().getTotalElements());
        assertEquals(4, response.getData().getPagination().getTotalPages());
        assertEquals(1, response.getData().getPagination().getCurrentPage());
    }
}

