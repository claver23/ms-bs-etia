package com.etlions.ms_bs_etia.doc_notacred.mapper;

import com.etlions.ms_bs_etia.doc_notacred.persistence.NotaCreditoEntity;
import com.etlions.openapicodegen.dto.DetalleItem;
import com.etlions.openapicodegen.dto.NotaCreditoData;
import com.etlions.openapicodegen.dto.NotaCreditoEmitirDto;
import com.etlions.openapicodegen.dto.NotaCreditoUpdateDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface NotaCreditoMapper {

    ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "hashCdr", ignore = true)
    @Mapping(target = "codigoRespuestaSunat", ignore = true)
    @Mapping(target = "descripcionRespuestaSunat", ignore = true)
    @Mapping(target = "xmlContent", ignore = true)
    @Mapping(target = "xmlPath", ignore = true)
    @Mapping(target = "erroresValidacion", ignore = true)
    @Mapping(target = "motivo", expression = "java(request.getMotivo() != null ? request.getMotivo().getValue() : null)")
    @Mapping(target = "tipoDocumentoReferencia", expression = "java(request.getTipoDocumentoReferencia() != null ? request.getTipoDocumentoReferencia().getValue() : \"factura\")")
    @Mapping(target = "moneda", expression = "java(request.getMoneda() != null ? request.getMoneda().getValue() : null)")
    @Mapping(target = "formaPago", expression = "java(request.getFormaPago() != null ? request.getFormaPago().getValue() : null)")
    @Mapping(target = "detalleJson", source = "detalle", qualifiedByName = "detalleToJson")
    NotaCreditoEntity toEntity(NotaCreditoEmitirDto request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "clientId", ignore = true)
    @Mapping(target = "documentoReferenciaId", ignore = true)
    @Mapping(target = "serieDocRef", ignore = true)
    @Mapping(target = "numeroDocRef", ignore = true)
    @Mapping(target = "tipoDocumentoReferencia", ignore = true)
    @Mapping(target = "serie", ignore = true)
    @Mapping(target = "numero", ignore = true)
    @Mapping(target = "fechaEmision", ignore = true)
    @Mapping(target = "moneda", ignore = true)
    @Mapping(target = "codigoUbigeo", ignore = true)
    @Mapping(target = "tipoOperacion", ignore = true)
    @Mapping(target = "horaEmision", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "hashCdr", ignore = true)
    @Mapping(target = "codigoRespuestaSunat", ignore = true)
    @Mapping(target = "descripcionRespuestaSunat", ignore = true)
    @Mapping(target = "xmlContent", ignore = true)
    @Mapping(target = "xmlPath", ignore = true)
    @Mapping(target = "erroresValidacion", ignore = true)
    @Mapping(target = "detalleJson", source = "detalle", qualifiedByName = "detalleToJson")
    @Mapping(target = "motivo", expression = "java(request.getMotivo() != null ? request.getMotivo().getValue() : null)")
    void updateEntity(NotaCreditoUpdateDto request, @MappingTarget NotaCreditoEntity entity);

    @Mapping(target = "motivo", expression = "java(entity.getMotivo() != null ? NotaCreditoData.MotivoEnum.fromValue(entity.getMotivo()) : null)")
    @Mapping(target = "tipoDocumentoReferencia", expression = "java(entity.getTipoDocumentoReferencia() != null ? NotaCreditoData.TipoDocumentoReferenciaEnum.fromValue(entity.getTipoDocumentoReferencia()) : null)")
    @Mapping(target = "estado", expression = "java(entity.getEstado() != null ? NotaCreditoData.EstadoEnum.fromValue(entity.getEstado()) : null)")
    @Mapping(target = "moneda", expression = "java(entity.getMoneda() != null ? NotaCreditoData.MonedaEnum.fromValue(entity.getMoneda()) : null)")
    @Mapping(target = "formaPago", expression = "java(entity.getFormaPago() != null ? NotaCreditoData.FormaPagoEnum.fromValue(entity.getFormaPago()) : null)")
    @Mapping(target = "detalle", source = "detalleJson", qualifiedByName = "jsonToDetalle")
    @Mapping(target = "erroresValidacion", expression = "java(jsonToStringListNullable(entity.getErroresValidacion()))")
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

    default JsonNullable<List<String>> jsonToStringListNullable(String json) {
        if (json == null || json.isBlank()) return JsonNullable.undefined();
        try {
            return JsonNullable.of(OBJECT_MAPPER.readValue(json, new TypeReference<List<String>>() {}));
        } catch (Exception e) {
            return JsonNullable.of(Collections.emptyList());
        }
    }
}

