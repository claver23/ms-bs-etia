package com.etlions.ms_bs_etia.client.service;

import com.etlions.openapicodegen.dto.ClientResponseDto;
import com.etlions.openapicodegen.dto.ClientRegisterDto;
import com.etlions.openapicodegen.dto.ClientUpdateDto;
import com.etlions.openapicodegen.dto.ClientListResponseDto;
import com.etlions.openapicodegen.dto.SuccessResponse;

public interface ClientService {

    ClientResponseDto registrarCliente(ClientRegisterDto request);

    ClientResponseDto actualizarCliente(String clientId, ClientUpdateDto request);

    SuccessResponse eliminarCliente(String clientId);

    ClientListResponseDto listarClientes(String companyId, String tipoDocumento,
                                             String numeroDocumento, String denominacion,
                                             Integer page, Integer size);
}

