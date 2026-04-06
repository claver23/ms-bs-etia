package com.etlions.ms_bs_etia.product.service;

import com.etlions.ms_bs_etia.product.persistence.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {

    List<ProductEntity> listarProductos(String companyId);

    List<ProductEntity> listarProductosActivos(String companyId);

    List<ProductEntity> buscarProductos(String companyId, String term);

    Page<ProductEntity> buscarProductosPaginado(String companyId, String term, String estado, Pageable pageable);

    ProductEntity crearProducto(ProductEntity product);

    ProductEntity actualizarProducto(String productId, ProductEntity product);

    void eliminarProducto(String productId, boolean softDelete);

    ProductEntity obtenerProducto(String productId);
}
