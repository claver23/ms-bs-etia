package com.etlions.ms_bs_etia.catalogo_sunat.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repositorio in-memory que carga los JSON de catálogos SUNAT al inicio de la aplicación.
 */
@Repository
@Slf4j
public class CatalogoSunatJsonRepository {

    private List<Map<String, String>> menu = Collections.emptyList();
    private List<Map<String, Object>> catalogos = Collections.emptyList();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        cargarMenu();
        cargarCatalogos();
    }

    private void cargarMenu() {
        try (InputStream is = new ClassPathResource("catalogo_sunat/catalogo_sunat_menu.json").getInputStream()) {
            menu = objectMapper.readValue(is, new TypeReference<>() {});
            log.info("✅ Menu catálogos SUNAT cargado: {} elementos", menu.size());
        } catch (Exception e) {
            log.error("❌ Error cargando catalogo_sunat_menu.json", e);
        }
    }

    private void cargarCatalogos() {
        try (InputStream is = new ClassPathResource("catalogo_sunat/catalogo_sunat.json").getInputStream()) {
            catalogos = objectMapper.readValue(is, new TypeReference<>() {});
            // Normalizar campo "codigo" a String en todos los registros
            for (Map<String, Object> catalogo : catalogos) {
                Object dataObj = catalogo.get("data");
                if (dataObj instanceof List<?> dataList) {
                    for (Object item : dataList) {
                        if (item instanceof Map<?, ?> registro) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> reg = (Map<String, Object>) registro;
                            Object cod = reg.get("codigo");
                            if (cod != null && !(cod instanceof String)) {
                                reg.put("codigo", String.valueOf(cod));
                            }
                        }
                    }
                }
            }
            log.info("✅ Catálogos SUNAT cargados: {} catálogos", catalogos.size());
        } catch (Exception e) {
            log.error("❌ Error cargando catalogo_sunat.json", e);
        }
    }

    public List<Map<String, String>> getMenu() {
        return Collections.unmodifiableList(menu);
    }

    public Optional<Map<String, Object>> findByNombre(String nombreCatalogo) {
        if (nombreCatalogo == null) return Optional.empty();
        String normalized = nombreCatalogo.trim().toLowerCase();
        return catalogos.stream()
                .filter(c -> {
                    Object nombre = c.get("nombre");
                    return nombre != null && nombre.toString().trim().toLowerCase().equals(normalized);
                })
                .findFirst();
    }
}

