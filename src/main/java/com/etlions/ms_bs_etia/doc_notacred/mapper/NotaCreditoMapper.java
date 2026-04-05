package com.etlions.ms_bs_etia.doc_notacred.mapper;

import com.etlions.ms_bs_etia.doc_notacred.persistence.NotaCreditoEntity;
import com.etlions.openapicodegen.dto.DetalleItem;
import com.etlions.openapicodegen.dto.NotaCreditoData;
import com.etlions.openapicodegen.dto.NotaCreditoEmitirRequest;
import com.etlions.openapicodegen.dto.NotaCreditoUpdateRequest;
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
public interface NotaCreditoMapper {

    ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "motivo", expression = "java(request.getMotivo() != null ? request.getMotivo().getValue() : null)")
    @Mapping(target = "tipoDocumento", expression = "java(request.getTipoDocumento() != null ? request.getTipoDocumento().getValue() : null)")
    @Mapping(target = "detalleJson", source = "detalle", qualifiedByName = "detalleToJson")
    NotaCreditoEntity toEntity(NotaCreditoEmitirRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "facturaReferenciaId", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "detalleJson", source = "detalle", qualifiedByName = "detalleToJson")
    @Mapping(target = "motivo", expression = "java(request.getMotivo() != null ? request.getMotivo().getValue() : null)")
    @Mapping(target = "tipoDocumento", ignore = true)
    void updateEntity(NotaCreditoUpdateRequest request, @MappingTarget NotaCreditoEntity entity);

    @Mapping(target = "motivo", expression = "java(entity.getMotivo() != null ? NotaCreditoData.MotivoEnum.fromValue(entity.getMotivo()) : null)")
    @Mapping(target = "tipoDocumento", expression = "java(entity.getTipoDocumento() != null ? NotaCreditoData.TipoDocumentoEnum.fromValue(entity.getTipoDocumento()) : null)")
    @Mapping(target = "estado", expression = "java(entity.getEstado() != null ? NotaCreditoData.EstadoEnum.fromValue(entity.getEstado()) : null)")
    @Mapping(target = "detalle", source = "detalleJson", qualifiedByName = "jsonToDetalle")
    NotaCreditoData toData(NotaCreditoEntity entity);

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

