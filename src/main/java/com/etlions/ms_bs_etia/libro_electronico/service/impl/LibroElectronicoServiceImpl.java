package com.etlions.ms_bs_etia.libro_electronico.service.impl;

import com.etlions.ms_bs_etia.libro_electronico.mapper.LibroElectronicoMapper;
import com.etlions.ms_bs_etia.libro_electronico.persistence.LibroElectronicoEntity;
import com.etlions.ms_bs_etia.libro_electronico.persistence.LibroElectronicoRepository;
import com.etlions.ms_bs_etia.libro_electronico.service.LibroElectronicoService;
import com.etlions.openapicodegen.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LibroElectronicoServiceImpl implements LibroElectronicoService {

    private final LibroElectronicoRepository libroElectronicoRepository;
    private final LibroElectronicoMapper libroElectronicoMapper;

    @Override
    public LibroElectronicoResponseDto generarLibroElectronico(LibroGenerarDto request) {
        String formato = request.getFormato() != null ? request.getFormato().getValue() : "TXT";
        String tipo = request.getTipo() != null ? request.getTipo().getValue() : "ventas";

        LibroElectronicoEntity entity = LibroElectronicoEntity.builder()
                .companyId(request.getCompanyId())
                .tipo(tipo)
                .periodo(request.getPeriodo())
                .formato(formato)
                .estado("GENERADO")
                .archivoUrl("/libros/" + request.getCompanyId() + "/" + tipo + "-" + request.getPeriodo() + "." + formato.toLowerCase())
                .totalRegistros(0)
                .fechaGeneracion(LocalDateTime.now())
                .fechaCreacion(LocalDateTime.now())
                .build();

        LibroElectronicoEntity saved = libroElectronicoRepository.save(entity);
        LibroElectronicoData data = libroElectronicoMapper.toData(saved);

        return new LibroElectronicoResponseDto()
                .isSuccess(true)
                .status("OK")
                .message("Libro electrónico generado exitosamente")
                .timestamp(new Date())
                .data(data);
    }

    @Override
    public LibroElectronicoListResponseDto listarLibrosElectronicos(String companyId, String tipo, String periodo, Integer page, Integer size) {
        int p = (page != null) ? page : 0;
        int s = (size != null) ? size : 10;
        PageRequest pageable = PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "fechaGeneracion"));

        Page<LibroElectronicoEntity> entityPage;
        if (tipo != null && !tipo.isBlank() && periodo != null && !periodo.isBlank()) {
            entityPage = libroElectronicoRepository.findByCompanyIdAndTipoAndPeriodo(companyId, tipo, periodo, pageable);
        } else if (tipo != null && !tipo.isBlank()) {
            entityPage = libroElectronicoRepository.findByCompanyIdAndTipo(companyId, tipo, pageable);
        } else if (periodo != null && !periodo.isBlank()) {
            entityPage = libroElectronicoRepository.findByCompanyIdAndPeriodo(companyId, periodo, pageable);
        } else {
            entityPage = libroElectronicoRepository.findByCompanyId(companyId, pageable);
        }

        List<LibroElectronicoData> content = entityPage.getContent().stream().map(libroElectronicoMapper::toData).toList();

        PaginationMeta pagination = new PaginationMeta()
                .currentPage(p)
                .pageSize(s)
                .totalElements(entityPage.getTotalElements())
                .totalPages(entityPage.getTotalPages());

        LibroElectronicoListResponseDtoAllOfData dataObj = new LibroElectronicoListResponseDtoAllOfData();
        dataObj.setContent(content);
        dataObj.setPagination(pagination);

        LibroElectronicoListResponseDto response = new LibroElectronicoListResponseDto();
        response.setIsSuccess(true);
        response.setStatus("OK");
        response.setMessage("Operación exitosa");
        response.setTimestamp(new Date());
        response.setData(dataObj);
        return response;
    }
}
