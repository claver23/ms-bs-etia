package com.etlions.ms_bs_etia.doc_notadeb.mapper;

import com.etlions.ms_bs_etia.doc_notadeb.persistence.NotaDebitoEntity;
import com.etlions.openapicodegen.dto.DetalleItem;
import com.etlions.openapicodegen.dto.NotaDebitoData;
import com.etlions.openapicodegen.dto.NotaDebitoEmitirDto;
import com.etlions.openapicodegen.dto.NotaDebitoUpdateDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public interface NotaDebitoMapper {

    ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "motivo", expression = "java(request.getMotivo() != null ? request.getMotivo().getValue() : null)")
    @Mapping(target = "detalleJson", source = "detalle", qualifiedByName = "detalleToJson")
    NotaDebitoEntity toEntity(NotaDebitoEmitirDto request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "facturaReferenciaId", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "detalleJson", source = "detalle", qualifiedByName = "detalleToJson")
    @Mapping(target = "motivo", expression = "java(request.getMotivo() != null ? request.getMotivo().getValue() : null)")
    void updateEntity(NotaDebitoUpdateDto request, @MappingTarget NotaDebitoEntity entity);

    @Mapping(target = "motivo", expression = "java(entity.getMotivo() != null ? NotaDebitoData.MotivoEnum.fromValue(entity.getMotivo()) : null)")
    @Mapping(target = "estado", expression = "java(entity.getEstado() != null ? NotaDebitoData.EstadoEnum.fromValue(entity.getEstado()) : null)")
    @Mapping(target = "detalle", source = "detalleJson", qualifiedByName = "jsonToDetalle")
    NotaDebitoData toData(NotaDebitoEntity entity);

    @Named("detalleToJson")
    default String detalleToJson(List<DetalleItem> detalle) {
        try {
            return OBJECT_MAPPER.writeValueAsString(detalle);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    @Named("jsonToDetalle")
    default List<DetalleItem> jsonToDetalle(String json) {
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<List<DetalleItem>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}

