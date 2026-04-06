package com.etlions.ms_bs_etia.doc_factura.mapper;

import com.etlions.ms_bs_etia.doc_factura.persistence.FacturaEntity;
import com.etlions.openapicodegen.dto.DetalleItem;
import com.etlions.openapicodegen.dto.FacturaData;
import com.etlions.openapicodegen.dto.FacturaEmitirRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public interface FacturaMapper {

    ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "hashCdr", ignore = true)
    @Mapping(target = "codigoRespuestaSunat", ignore = true)
    @Mapping(target = "descripcionRespuestaSunat", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "moneda", expression = "java(request.getMoneda() != null ? request.getMoneda().getValue() : null)")
    @Mapping(target = "detalleJson", source = "detalle", qualifiedByName = "detalleToJson")
    FacturaEntity toEntity(FacturaEmitirRequest request);

    @Mapping(target = "moneda", expression = "java(entity.getMoneda() != null ? FacturaData.MonedaEnum.fromValue(entity.getMoneda()) : null)")
    @Mapping(target = "estado", expression = "java(entity.getEstado() != null ? FacturaData.EstadoEnum.fromValue(entity.getEstado()) : null)")
    @Mapping(target = "detalle", source = "detalleJson", qualifiedByName = "jsonToDetalle")
    FacturaData toData(FacturaEntity entity);

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

