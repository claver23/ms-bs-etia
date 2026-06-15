package com.etlions.ms_bs_etia.product.service;

import com.etlions.openapicodegen.dto.*;

public interface ProductService {

    ProductResponseDto crearProducto(ProductCreateDto request);

    ProductResponseDto actualizarProducto(String productId, ProductUpdateDto request);

    SuccessResponse eliminarProducto(String productId, boolean softDelete);

    ProductListResponseDto buscarProductos(String companyId, String term, String estado, Integer page, Integer size);

    ProductResponseDto obtenerProducto(String productId);
}
