package com.etlions.ms_bs_etia.product.service.impl;

import com.etlions.ms_bs_etia.app_config.exception.ResourceNotFoundException;
import com.etlions.ms_bs_etia.product.mapper.ProductMapper;
import com.etlions.ms_bs_etia.product.persistence.ProductEntity;
import com.etlions.ms_bs_etia.product.persistence.ProductRepository;
import com.etlions.ms_bs_etia.product.service.ProductService;
import com.etlions.openapicodegen.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private static final BigDecimal IGV_RATE = new BigDecimal("0.18");

    @Override
    @Transactional
    public ProductResponseDto crearProducto(ProductCreateDto request) {
        ProductEntity entity = productMapper.toEntity(request);
        recalcularIgv(entity);
        if (entity.getUnidadMedida() == null || entity.getUnidadMedida().isBlank()) {
            entity.setUnidadMedida("ZZ");
        }
        entity.setEstado("ACTIVO");
        entity.setFechaCreacion(LocalDateTime.now());
        ProductEntity saved = productRepository.save(entity);
        ProductData data = productMapper.toData(saved);
        return new ProductResponseDto()
                .isSuccess(true).status("OK")
                .message("Producto registrado exitosamente")
                .timestamp(new Date()).data(data);
    }

    @Override
    @Transactional
    public ProductResponseDto actualizarProducto(String productId, ProductUpdateDto request) {
        ProductEntity entity = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", productId));
        productMapper.updateEntity(request, entity);
        if (entity.getValorUnitario() != null) {
            recalcularIgv(entity);
        }
        ProductEntity saved = productRepository.save(entity);
        ProductData data = productMapper.toData(saved);
        return new ProductResponseDto()
                .isSuccess(true).status("OK")
                .message("Producto actualizado exitosamente")
                .timestamp(new Date()).data(data);
    }

    @Override
    @Transactional
    public SuccessResponse eliminarProducto(String productId, boolean softDelete) {
        if (softDelete) {
            ProductEntity entity = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Producto", productId));
            entity.setEstado("INACTIVO");
            productRepository.save(entity);
        } else {
            productRepository.deleteById(productId);
        }
        return new SuccessResponse()
                .isSuccess(true).status("OK")
                .message("Producto eliminado exitosamente")
                .timestamp(new Date());
    }

    @Override
    public ProductListResponseDto buscarProductos(String companyId, String term, String estado, Integer page, Integer size) {
        int p = (page != null) ? page : 0;
        int s = (size != null) ? size : 10;

        Page<ProductEntity> productPage;
        if (term != null && !term.isBlank()) {
            productPage = productRepository
                    .findByCompanyIdAndDescripcionContainingIgnoreCaseOrCompanyIdAndCodigoProductoContainingIgnoreCase(
                            companyId, term, companyId, term, PageRequest.of(p, s));
        } else if ("ACTIVO".equalsIgnoreCase(estado)) {
            productPage = productRepository.findByCompanyIdAndEstado(companyId, "ACTIVO", PageRequest.of(p, s));
        } else {
            productPage = productRepository.findByCompanyId(companyId, PageRequest.of(p, s));
        }

        List<ProductData> content = productPage.getContent().stream().map(productMapper::toData).toList();

        PaginationMeta pagination = new PaginationMeta()
                .currentPage(p)
                .pageSize(s)
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages());

        ProductListResponseDtoAllOfData dataObj = new ProductListResponseDtoAllOfData();
        dataObj.setContent(content);
        dataObj.setPagination(pagination);

        return new ProductListResponseDto()
                .isSuccess(true).status("OK")
                .message("Operación exitosa")
                .timestamp(new Date()).data(dataObj);
    }

    @Override
    public ProductResponseDto obtenerProducto(String productId) {
        ProductEntity entity = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", productId));
        ProductData data = productMapper.toData(entity);
        return new ProductResponseDto()
                .isSuccess(true).status("OK")
                .message("Operación exitosa")
                .timestamp(new Date()).data(data);
    }

    private void recalcularIgv(ProductEntity product) {
        if (product.getValorUnitario() != null) {
            BigDecimal igv = product.getValorUnitario().multiply(IGV_RATE).setScale(2, RoundingMode.HALF_UP);
            product.setIgv(igv);
            product.setPrecioUnitario(product.getValorUnitario().add(igv));
        }
    }
}
