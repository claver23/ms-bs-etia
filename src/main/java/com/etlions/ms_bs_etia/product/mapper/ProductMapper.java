package com.etlions.ms_bs_etia.product.mapper;

import com.etlions.ms_bs_etia.product.persistence.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    ProductEntity toEntity(String companyId,
                           String codigoProducto,
                           String descripcion,
                           String unidadMedida,
                           BigDecimal valorUnitario,
                           BigDecimal precioUnitario,
                           BigDecimal igv);

    default Map<String, Object> toMap(ProductEntity entity) {
        return Map.of(
                "id", entity.getId(),
                "companyId", entity.getCompanyId() != null ? entity.getCompanyId() : "",
                "descripcion", entity.getDescripcion() != null ? entity.getDescripcion() : "",
                "unidadMedida", entity.getUnidadMedida() != null ? entity.getUnidadMedida() : "ZZ",
                "valorUnitario", entity.getValorUnitario() != null ? entity.getValorUnitario() : BigDecimal.ZERO,
                "precioUnitario", entity.getPrecioUnitario() != null ? entity.getPrecioUnitario() : BigDecimal.ZERO,
                "codigoProducto", entity.getCodigoProducto() != null ? entity.getCodigoProducto() : "",
                "igv", entity.getIgv() != null ? entity.getIgv() : BigDecimal.ZERO,
                "estado", entity.getEstado() != null ? entity.getEstado() : "ACTIVO"
        );
    }
}

