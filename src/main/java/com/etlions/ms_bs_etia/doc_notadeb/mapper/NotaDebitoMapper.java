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
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface NotaDebitoMapper {

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
    NotaDebitoEntity toEntity(NotaDebitoEmitirDto request);

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
    void updateEntity(NotaDebitoUpdateDto request, @MappingTarget NotaDebitoEntity entity);

    @Mapping(target = "motivo", expression = "java(entity.getMotivo() != null ? NotaDebitoData.MotivoEnum.fromValue(entity.getMotivo()) : null)")
    @Mapping(target = "tipoDocumentoReferencia", expression = "java(entity.getTipoDocumentoReferencia() != null ? NotaDebitoData.TipoDocumentoReferenciaEnum.fromValue(entity.getTipoDocumentoReferencia()) : null)")
    @Mapping(target = "estado", expression = "java(entity.getEstado() != null ? NotaDebitoData.EstadoEnum.fromValue(entity.getEstado()) : null)")
    @Mapping(target = "moneda", expression = "java(entity.getMoneda() != null ? NotaDebitoData.MonedaEnum.fromValue(entity.getMoneda()) : null)")
    @Mapping(target = "formaPago", expression = "java(entity.getFormaPago() != null ? NotaDebitoData.FormaPagoEnum.fromValue(entity.getFormaPago()) : null)")
    @Mapping(target = "detalle", source = "detalleJson", qualifiedByName = "jsonToDetalle")
    @Mapping(target = "erroresValidacion", expression = "java(jsonToStringListNullable(entity.getErroresValidacion()))")
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

    default JsonNullable<List<String>> jsonToStringListNullable(String json) {
        if (json == null || json.isBlank()) return JsonNullable.undefined();
        try {
            return JsonNullable.of(OBJECT_MAPPER.readValue(json, new TypeReference<List<String>>() {}));
        } catch (Exception e) {
            return JsonNullable.of(Collections.emptyList());
        }
    }
}

