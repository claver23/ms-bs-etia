package com.etlions.ms_bs_etia.product.openapi;

import com.etlions.ms_bs_etia.product.service.ProductService;
import com.etlions.openapicodegen.api.v1.ProductApi;
import com.etlions.openapicodegen.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProductOpenApi implements ProductApi {

    private final ProductService productService;

    @Override
    public ResponseEntity<ProductResponseDto> crearProducto(ProductCreateDto request) {
        return ResponseEntity.ok(productService.crearProducto(request));
    }

    @Override
    public ResponseEntity<ProductResponseDto> actualizarProducto(String productId, ProductUpdateDto request) {
        return ResponseEntity.ok(productService.actualizarProducto(productId, request));
    }

    @Override
    public ResponseEntity<SuccessResponse> eliminarProducto(String productId, Boolean softDelete) {
        return ResponseEntity.ok(productService.eliminarProducto(productId, softDelete != null ? softDelete : true));
    }

    @Override
    public ResponseEntity<ProductListResponseDto> buscarProductos(String companyId, String term, String estado, Integer page, Integer size) {
        return ResponseEntity.ok(productService.buscarProductos(companyId, term, estado, page, size));
    }

    @Override
    public ResponseEntity<ProductResponseDto> obtenerProducto(String productId) {
        return ResponseEntity.ok(productService.obtenerProducto(productId));
    }
}
