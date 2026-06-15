package com.etlions.ms_bs_etia.doc_factura.service.impl;

import com.etlions.ms_bs_etia.app_util.service.CorrelativoService;
import com.etlions.ms_bs_etia.app_util.service.PdfGeneratorService;
import com.etlions.ms_bs_etia.app_util.service.XmlStorageService;
import com.etlions.ms_bs_etia.app_util.sunat.SunatBillingOrchestrator;
import com.etlions.ms_bs_etia.app_util.validation.DocumentValidatorService;
import com.etlions.ms_bs_etia.client.persistence.ClientRepository;
import com.etlions.ms_bs_etia.company.persistence.CompanyRepository;
import com.etlions.ms_bs_etia.doc_factura.mapper.FacturaMapper;
import com.etlions.ms_bs_etia.doc_factura.persistence.AllowanceChargeRepository;
import com.etlions.ms_bs_etia.doc_factura.persistence.CuotaCreditoRepository;
import com.etlions.ms_bs_etia.doc_factura.persistence.FacturaEntity;
import com.etlions.ms_bs_etia.doc_factura.persistence.FacturaRepository;
import com.etlions.ms_bs_etia.doc_factura.xml.UblInvoiceXmlBuilder;
import com.etlions.ms_bs_etia.notification.service.NotificationService;
import com.etlions.openapicodegen.dto.FacturaData;
import com.etlions.openapicodegen.dto.FacturaListResponseDto;
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
class DocFacturaPaginationTest {

    @Mock private FacturaRepository facturaRepository;
    @Mock private FacturaMapper facturaMapper;
    @Mock private CorrelativoService correlativoService;
    @Mock private NotificationService notificationService;
    @Mock private ClientRepository clientRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private CuotaCreditoRepository cuotaCreditoRepository;
    @Mock private AllowanceChargeRepository allowanceChargeRepository;
    @Mock private UblInvoiceXmlBuilder ublInvoiceXmlBuilder;
    @Mock private XmlStorageService xmlStorageService;
    @Mock private DocumentValidatorService documentValidatorService;
    @Mock private SunatBillingOrchestrator sunatBillingOrchestrator;
    @Mock private PdfGeneratorService pdfGeneratorService;

    @InjectMocks
    private DocFacturaServiceImpl service;

    @Test
    void listarFacturas_debeUsarPaginacionReal() {
        FacturaEntity e1 = FacturaEntity.builder().id("F1").companyId("COMP-1").serie("F001").numero(10).build();
        FacturaEntity e2 = FacturaEntity.builder().id("F2").companyId("COMP-1").serie("F001").numero(9).build();
        Page<FacturaEntity> page = new PageImpl<>(List.of(e1, e2), PageRequest.of(0, 2), 5);

        when(facturaRepository.buscarConFiltros(eq("COMP-1"), isNull(), isNull(), isNull(), isNull(), isNull(), any()))
                .thenReturn(page);
        when(cuotaCreditoRepository.findByFacturaIdOrderByNumeroCuota(anyString())).thenReturn(List.of());
        when(allowanceChargeRepository.findByFacturaId(anyString())).thenReturn(List.of());
        when(facturaMapper.toData(any(FacturaEntity.class))).thenReturn(new FacturaData());

        FacturaListResponseDto response = service.listarFacturas("COMP-1", null, null, null, null, null, 0, 2);

        assertTrue(response.getIsSuccess());
        assertEquals(2, response.getData().getContent().size());
        assertEquals(5L, response.getData().getPagination().getTotalElements());
        assertEquals(3, response.getData().getPagination().getTotalPages());
        assertEquals(2, response.getData().getPagination().getPageSize());
    }
}

