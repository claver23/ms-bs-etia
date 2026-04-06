package com.etlions.ms_bs_etia.product.service.impl;

import com.etlions.ms_bs_etia.app_config.exception.ResourceNotFoundException;
import com.etlions.ms_bs_etia.product.persistence.ProductEntity;
import com.etlions.ms_bs_etia.product.persistence.ProductRepository;
import com.etlions.ms_bs_etia.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private static final BigDecimal IGV_RATE = new BigDecimal("0.18");

    @Override
    public List<ProductEntity> listarProductos(String companyId) {
        return productRepository.findByCompanyId(companyId);
    }

    @Override
    public List<ProductEntity> listarProductosActivos(String companyId) {
        return productRepository.findByCompanyIdAndEstado(companyId, "ACTIVO");
    }

    @Override
    public List<ProductEntity> buscarProductos(String companyId, String term) {
        if (term == null || term.isBlank()) {
            return listarProductos(companyId);
        }
        return productRepository
                .findByCompanyIdAndDescripcionContainingIgnoreCase(companyId, term);
    }

    @Override
    public Page<ProductEntity> buscarProductosPaginado(String companyId, String term, String estado, Pageable pageable) {
        if (term != null && !term.isBlank()) {
            return productRepository
                    .findByCompanyIdAndDescripcionContainingIgnoreCaseOrCompanyIdAndCodigoProductoContainingIgnoreCase(
                            companyId, term, companyId, term, pageable);
        } else if ("ACTIVO".equalsIgnoreCase(estado)) {
            return productRepository.findByCompanyIdAndEstado(companyId, "ACTIVO", pageable);
        } else {
            return productRepository.findByCompanyId(companyId, pageable);
        }
    }

    @Override
    @Transactional
    public ProductEntity crearProducto(ProductEntity product) {
        recalcularIgv(product);
        if (product.getUnidadMedida() == null || product.getUnidadMedida().isBlank()) {
            product.setUnidadMedida("ZZ");
        }
        product.setEstado("ACTIVO");
        product.setFechaCreacion(LocalDateTime.now());
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public ProductEntity actualizarProducto(String productId, ProductEntity updated) {
        ProductEntity entity = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + productId));

        if (updated.getDescripcion() != null) entity.setDescripcion(updated.getDescripcion());
        if (updated.getCodigoProducto() != null) entity.setCodigoProducto(updated.getCodigoProducto());
        if (updated.getUnidadMedida() != null) entity.setUnidadMedida(updated.getUnidadMedida());
        if (updated.getValorUnitario() != null) {
            entity.setValorUnitario(updated.getValorUnitario());
            recalcularIgv(entity);
        }

        return productRepository.save(entity);
    }

    @Override
    @Transactional
    public void eliminarProducto(String productId, boolean softDelete) {
        if (softDelete) {
            ProductEntity entity = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + productId));
            entity.setEstado("INACTIVO");
            productRepository.save(entity);
        } else {
            productRepository.deleteById(productId);
        }
    }

    @Override
    public ProductEntity obtenerProducto(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + productId));
    }

    private void recalcularIgv(ProductEntity product) {
        if (product.getValorUnitario() != null) {
            BigDecimal igv = product.getValorUnitario().multiply(IGV_RATE).setScale(2, RoundingMode.HALF_UP);
            product.setIgv(igv);
            product.setPrecioUnitario(product.getValorUnitario().add(igv));
        }
    }
}
