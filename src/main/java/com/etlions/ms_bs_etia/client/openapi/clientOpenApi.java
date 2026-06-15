package com.etlions.ms_bs_etia.client.openapi;

import com.etlions.ms_bs_etia.client.service.ClientService;
import com.etlions.openapicodegen.api.v1.ClientApi;
import com.etlions.openapicodegen.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ClientOpenApi implements ClientApi {

    private final ClientService clientService;

    @Override
    public ResponseEntity<ClientResponseDto> registrarCliente(ClientRegisterDto clientRegisterRequest) {
        return ResponseEntity.ok(clientService.registrarCliente(clientRegisterRequest));
    }

    @Override
    public ResponseEntity<ClientResponseDto> actualizarCliente(String clientId, ClientUpdateDto clientUpdateRequest) {
        return ResponseEntity.ok(clientService.actualizarCliente(clientId, clientUpdateRequest));
    }

    @Override
    public ResponseEntity<SuccessResponse> eliminarCliente(String clientId) {
        return ResponseEntity.ok(clientService.eliminarCliente(clientId));
    }

    @Override
    public ResponseEntity<ClientListResponseDto> listarClientes(String companyId, String tipoDocumento, String numeroDocumento, String denominacion, Integer page, Integer size) {
        return ResponseEntity.ok(clientService.listarClientes(companyId, tipoDocumento, numeroDocumento, denominacion, page, size));
    }
}

