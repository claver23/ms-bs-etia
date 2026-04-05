package com.etlions.ms_bs_etia.client.service;

import com.etlions.openapicodegen.dto.ActualizarCliente200Response;
import com.etlions.openapicodegen.dto.ClientRegisterRequest;
import com.etlions.openapicodegen.dto.ClientUpdateRequest;
import com.etlions.openapicodegen.dto.ListarClientes200Response;
import com.etlions.openapicodegen.dto.SuccessResponse;

public interface clientService {

    ActualizarCliente200Response registrarCliente(ClientRegisterRequest request);

    ActualizarCliente200Response actualizarCliente(String clientId, ClientUpdateRequest request);

    SuccessResponse eliminarCliente(String clientId);

    ListarClientes200Response listarClientes(String companyId, String tipoDocumento,
                                             String numeroDocumento, String denominacion,
                                             Integer page, Integer size);
}
