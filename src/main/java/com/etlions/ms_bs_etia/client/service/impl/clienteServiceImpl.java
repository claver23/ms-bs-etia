package com.etlions.ms_bs_etia.client.service.impl;

import com.etlions.ms_bs_etia.client.mapper.ClientMapper;
import com.etlions.ms_bs_etia.client.persistence.ClientEntity;
import com.etlions.ms_bs_etia.client.persistence.ClientRepository;
import com.etlions.ms_bs_etia.client.service.clientService;
import com.etlions.openapicodegen.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class clienteServiceImpl implements clientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    @Override
    public ActualizarCliente200Response registrarCliente(ClientRegisterRequest request) {
        ClientEntity entity = clientMapper.toEntity(request);
        entity.setFechaCreacion(LocalDateTime.now());
        ClientEntity saved = clientRepository.save(entity);
        ClientData data = clientMapper.toData(saved);
        return new ActualizarCliente200Response()
                .isSuccess(true)
                .status("OK")
                .message("Cliente registrado exitosamente")
                .timestamp(new Date())
                .data(data);
    }

    @Override
    public ActualizarCliente200Response actualizarCliente(String clientId, ClientUpdateRequest request) {
        ClientEntity entity = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado: " + clientId));
        clientMapper.updateEntity(request, entity);
        ClientEntity saved = clientRepository.save(entity);
        ClientData data = clientMapper.toData(saved);
        return new ActualizarCliente200Response()
                .isSuccess(true)
                .status("OK")
                .message("Cliente actualizado exitosamente")
                .timestamp(new Date())
                .data(data);
    }

    @Override
    public SuccessResponse eliminarCliente(String clientId) {
        clientRepository.deleteById(clientId);
        return new SuccessResponse()
                .isSuccess(true)
                .status("OK")
                .message("Cliente eliminado exitosamente")
                .timestamp(new Date());
    }

    @Override
    public ListarClientes200Response listarClientes(String companyId, String tipoDocumento,
                                                     String numeroDocumento, String denominacion,
                                                     Integer page, Integer size) {
        List<ClientEntity> entities = clientRepository.findByCompanyId(companyId);
        List<ClientData> dataList = entities.stream()
                .map(clientMapper::toData)
                .toList();
        ListarClientes200ResponseAllOfData responseData = new ListarClientes200ResponseAllOfData();
        responseData.setContent(dataList);
        responseData.setPagination(new PaginationMeta()
                .currentPage(page)
                .pageSize(size)
                .totalElements((long) dataList.size())
                .totalPages(1));
        ListarClientes200Response response = new ListarClientes200Response();
        response.setIsSuccess(true);
        response.setStatus("OK");
        response.setMessage("Operación exitosa");
        response.setTimestamp(new Date());
        response.setData(responseData);
        return response;
    }
}
