package com.etlions.ms_bs_etia.doc_boleta.mapper;

import com.etlions.ms_bs_etia.doc_boleta.persistence.BoletaEntity;
import com.etlions.openapicodegen.dto.BoletaData;
import com.etlions.openapicodegen.dto.BoletaEmitirDto;
import com.etlions.openapicodegen.dto.DetalleItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public interface BoletaMapper {

    ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "erroresValidacion", ignore = true)
    @Mapping(target = "moneda", expression = "java(request.getMoneda() != null ? request.getMoneda().getValue() : null)")
    @Mapping(target = "formaPago", expression = "java(request.getFormaPago() != null ? request.getFormaPago().getValue() : \"CONTADO\")")
    @Mapping(target = "horaEmision", expression = "java(request.getHoraEmision() != null ? java.time.LocalTime.parse(request.getHoraEmision()) : null)")
    @Mapping(target = "detalleJson", source = "detalle", qualifiedByName = "detalleToJson")
    BoletaEntity toEntity(BoletaEmitirDto request);

    @Mapping(target = "moneda", expression = "java(entity.getMoneda() != null ? BoletaData.MonedaEnum.fromValue(entity.getMoneda()) : null)")
    @Mapping(target = "estado", expression = "java(entity.getEstado() != null ? BoletaData.EstadoEnum.fromValue(entity.getEstado()) : null)")
    @Mapping(target = "formaPago", expression = "java(entity.getFormaPago() != null ? BoletaData.FormaPagoEnum.fromValue(entity.getFormaPago()) : null)")
    @Mapping(target = "horaEmision", expression = "java(entity.getHoraEmision() != null ? entity.getHoraEmision().toString() : null)")
    @Mapping(target = "detalle", source = "detalleJson", qualifiedByName = "jsonToDetalle")
    @Mapping(target = "erroresValidacion", expression = "java(jsonToStringList(entity.getErroresValidacion()))")
    @Mapping(target = "cuotas", ignore = true)
    @Mapping(target = "cargosDescuentos", ignore = true)
    BoletaData toData(BoletaEntity entity);

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

    @Named("jsonToStringList")
    default List<String> jsonToStringList(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return null;
        }
    }
}
