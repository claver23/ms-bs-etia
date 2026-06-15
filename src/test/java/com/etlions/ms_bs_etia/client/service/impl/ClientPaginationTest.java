package com.etlions.ms_bs_etia.client.service.impl;

import com.etlions.ms_bs_etia.client.mapper.ClientMapper;
import com.etlions.ms_bs_etia.client.persistence.ClientEntity;
import com.etlions.ms_bs_etia.client.persistence.ClientRepository;
import com.etlions.openapicodegen.dto.ClientData;
import com.etlions.openapicodegen.dto.ClientListResponseDto;
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
class ClientPaginationTest {

    @Mock private ClientRepository clientRepository;
    @Mock private ClientMapper clientMapper;

    @InjectMocks
    private ClientServiceImpl service;

    @Test
    void listarClientes_debeUsarPaginacionRealConFiltros() {
        ClientEntity entity = ClientEntity.builder().id("CLI-1").companyId("COMP-1").denominacion("Cliente Uno").build();
        Page<ClientEntity> page = new PageImpl<>(List.of(entity), PageRequest.of(1, 1), 3);

        when(clientRepository.buscarConFiltros(eq("COMP-1"), eq("6"), isNull(), eq("Cliente"), any()))
                .thenReturn(page);
        when(clientMapper.toData(any(ClientEntity.class))).thenReturn(new ClientData());

        ClientListResponseDto response = service.listarClientes("COMP-1", "6", null, "Cliente", 1, 1);

        assertTrue(response.getIsSuccess());
        assertEquals(1, response.getData().getContent().size());
        assertEquals(3L, response.getData().getPagination().getTotalElements());
        assertEquals(3, response.getData().getPagination().getTotalPages());
        assertEquals(1, response.getData().getPagination().getCurrentPage());
    }
}

