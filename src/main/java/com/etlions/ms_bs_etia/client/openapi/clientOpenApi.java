package com.etlions.ms_bs_etia.client.openapi;

import com.etlions.ms_bs_etia.client.service.clientService;
import com.etlions.openapicodegen.api.v1.ClientApi;
import com.etlions.openapicodegen.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class clientOpenApi implements ClientApi {

    private final clientService clientService;

    @Override
    public ResponseEntity<ActualizarCliente200Response> registrarCliente(ClientRegisterRequest clientRegisterRequest) {
        return ResponseEntity.ok(clientService.registrarCliente(clientRegisterRequest));
    }

    @Override
    public ResponseEntity<ActualizarCliente200Response> actualizarCliente(String clientId, ClientUpdateRequest clientUpdateRequest) {
        return ResponseEntity.ok(clientService.actualizarCliente(clientId, clientUpdateRequest));
    }

    @Override
    public ResponseEntity<SuccessResponse> eliminarCliente(String clientId) {
        return ResponseEntity.ok(clientService.eliminarCliente(clientId));
    }

    @Override
    public ResponseEntity<ListarClientes200Response> listarClientes(String companyId, String tipoDocumento, String numeroDocumento, String denominacion, Integer page, Integer size) {
        return ResponseEntity.ok(clientService.listarClientes(companyId, tipoDocumento, numeroDocumento, denominacion, page, size));
    }
}
