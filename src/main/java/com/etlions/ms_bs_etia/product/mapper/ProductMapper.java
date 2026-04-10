package com.etlions.ms_bs_etia.product.mapper;

import com.etlions.ms_bs_etia.product.persistence.ProductEntity;
import com.etlions.openapicodegen.dto.ProductCreateDto;
import com.etlions.openapicodegen.dto.ProductData;
import com.etlions.openapicodegen.dto.ProductUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "precioUnitario", ignore = true)
    @Mapping(target = "igv", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "esDemo", constant = "false")
    ProductEntity toEntity(ProductCreateDto request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "precioUnitario", ignore = true)
    @Mapping(target = "igv", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "esDemo", ignore = true)
    void updateEntity(ProductUpdateDto request, @MappingTarget ProductEntity entity);

    @Mapping(target = "estado", expression = "java(entity.getEstado() != null ? ProductData.EstadoEnum.fromValue(entity.getEstado()) : ProductData.EstadoEnum.ACTIVO)")
    ProductData toData(ProductEntity entity);
}
