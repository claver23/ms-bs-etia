package com.etlions.ms_bs_etia.client.mapper;

import com.etlions.ms_bs_etia.client.persistence.ClientEntity;
import com.etlions.openapicodegen.dto.ClientData;
import com.etlions.openapicodegen.dto.ClientRegisterDto;
import com.etlions.openapicodegen.dto.ClientUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "tipoDocumento", expression = "java(request.getTipoDocumento() != null ? request.getTipoDocumento().getValue() : null)")
    ClientEntity toEntity(ClientRegisterDto request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "tipoDocumento", ignore = true)
    @Mapping(target = "numeroDocumento", ignore = true)
    void updateEntity(ClientUpdateDto request, @MappingTarget ClientEntity entity);

    @Mapping(target = "tipoDocumento", expression = "java(mapTipoDocumento(entity.getTipoDocumento()))")
    ClientData toData(ClientEntity entity);

    /**
     * Maps both SUNAT codes ("1","6","4","7") and enum names ("DNI","RUC","CE","PASAPORTE")
     * to the ClientData.TipoDocumentoEnum safely.
     */
    default ClientData.TipoDocumentoEnum mapTipoDocumento(String raw) {
        if (raw == null) return null;
        // Try direct enum match first (e.g. "RUC", "DNI")
        try {
            return ClientData.TipoDocumentoEnum.fromValue(raw);
        } catch (IllegalArgumentException ignored) { }
        // Fallback: SUNAT code mapping
        switch (raw) {
            case "1": return ClientData.TipoDocumentoEnum.DNI;
            case "6": return ClientData.TipoDocumentoEnum.RUC;
            case "4": return ClientData.TipoDocumentoEnum.CE;
            case "7": return ClientData.TipoDocumentoEnum.PASAPORTE;
            default:  return null;
        }
    }
}


