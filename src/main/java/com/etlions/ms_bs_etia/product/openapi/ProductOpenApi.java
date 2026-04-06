package com.etlions.ms_bs_etia.product.openapi;

import com.etlions.ms_bs_etia.product.mapper.ProductMapper;
import com.etlions.ms_bs_etia.product.persistence.ProductEntity;
import com.etlions.ms_bs_etia.product.service.ProductService;
import com.etlions.openapicodegen.api.v1.ProductApi;
import com.etlions.openapicodegen.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProductOpenApi implements ProductApi {

    private final ProductService productService;
    private final ProductMapper productMapper;

    @Override
    public ResponseEntity<ActualizarProducto200Response> crearProducto(ProductCreateRequest request) {
        ProductEntity entity = productMapper.toEntity(
                request.getCompanyId(),
                request.getCodigoProducto(),
                request.getDescripcion(),
                request.getUnidadMedida(),
                request.getValorUnitario(),
                null, null
        );
        ProductEntity saved = productService.crearProducto(entity);
        ProductData data = toProductData(saved);
        return ResponseEntity.ok(new ActualizarProducto200Response()
                .isSuccess(true).status("OK")
                .message("Producto registrado exitosamente")
                .timestamp(new Date()).data(data));
    }

    @Override
    public ResponseEntity<ActualizarProducto200Response> actualizarProducto(String productId,
                                                                             ProductUpdateRequest request) {
        ProductEntity updated = ProductEntity.builder()
                .descripcion(request.getDescripcion())
                .codigoProducto(request.getCodigoProducto())
                .unidadMedida(request.getUnidadMedida())
                .valorUnitario(request.getValorUnitario())
                .build();
        ProductEntity saved = productService.actualizarProducto(productId, updated);
        ProductData data = toProductData(saved);
        return ResponseEntity.ok(new ActualizarProducto200Response()
                .isSuccess(true).status("OK")
                .message("Producto actualizado exitosamente")
                .timestamp(new Date()).data(data));
    }

    @Override
    public ResponseEntity<SuccessResponse> eliminarProducto(String productId, Boolean softDelete) {
        productService.eliminarProducto(productId, softDelete != null ? softDelete : true);
        return ResponseEntity.ok(new SuccessResponse()
                .isSuccess(true).status("OK")
                .message("Producto eliminado exitosamente")
                .timestamp(new Date()));
    }

    @Override
    public ResponseEntity<BuscarProductos200Response> buscarProductos(String companyId, String term,
                                                                       String estado, Integer page, Integer size) {
        int p = (page != null) ? page : 0;
        int s = (size != null) ? size : 10;

        Page<ProductEntity> productPage = productService.buscarProductosPaginado(companyId, term, estado, PageRequest.of(p, s));

        List<ProductData> content = productPage.getContent().stream().map(this::toProductData).toList();

        PaginationMeta pagination = new PaginationMeta();
        pagination.setTotalElements(productPage.getTotalElements());
        pagination.setTotalPages(productPage.getTotalPages());
        pagination.setCurrentPage(p);
        pagination.setPageSize(s);

        BuscarProductos200ResponseAllOfData dataObj = new BuscarProductos200ResponseAllOfData();
        dataObj.setContent(content);
        dataObj.setPagination(pagination);

        return ResponseEntity.ok(new BuscarProductos200Response()
                .isSuccess(true).status("OK")
                .message("Operación exitosa")
                .timestamp(new Date()).data(dataObj));
    }

    @Override
    public ResponseEntity<ActualizarProducto200Response> obtenerProducto(String productId) {
        ProductEntity entity = productService.obtenerProducto(productId);
        ProductData data = toProductData(entity);
        return ResponseEntity.ok(new ActualizarProducto200Response()
                .isSuccess(true).status("OK")
                .message("Operación exitosa")
                .timestamp(new Date()).data(data));
    }

    private ProductData toProductData(ProductEntity e) {
        ProductData d = new ProductData();
        d.setId(e.getId());
        d.setCompanyId(e.getCompanyId());
        d.setCodigoProducto(e.getCodigoProducto());
        d.setDescripcion(e.getDescripcion());
        d.setUnidadMedida(e.getUnidadMedida());
        d.setValorUnitario(e.getValorUnitario());
        d.setPrecioUnitario(e.getPrecioUnitario());
        d.setIgv(e.getIgv());
        d.setEstado(e.getEstado() != null ?
                ProductData.EstadoEnum.fromValue(e.getEstado()) : ProductData.EstadoEnum.ACTIVO);
        return d;
    }
}
