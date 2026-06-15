package com.etlions.ms_bs_etia.tabla_sunat.persistence;

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
 * Repositorio in-memory que carga los JSON de tablas SUNAT al inicio de la aplicación.
 * Funciona como "persistencia" basada en archivos JSON estáticos.
 */
@Repository
@Slf4j
public class TablaSunatJsonRepository {

    private List<Map<String, String>> menu = Collections.emptyList();
    private List<Map<String, Object>> tablas = Collections.emptyList();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        cargarMenu();
        cargarTablas();
    }

    private void cargarMenu() {
        try (InputStream is = new ClassPathResource("tablas_sunat/menu_tablas_sunat.json").getInputStream()) {
            menu = objectMapper.readValue(is, new TypeReference<>() {});
            log.info("✅ Menu tablas SUNAT cargado: {} elementos", menu.size());
        } catch (Exception e) {
            log.error("❌ Error cargando menu_tablas_sunat.json", e);
        }
    }

    private void cargarTablas() {
        try (InputStream is = new ClassPathResource("tablas_sunat/tablas_sunat.json").getInputStream()) {
            tablas = objectMapper.readValue(is, new TypeReference<>() {});
            // Normalizar campo "cod" a String en todos los registros
            for (Map<String, Object> tabla : tablas) {
                Object dataObj = tabla.get("data");
                if (dataObj instanceof List<?> dataList) {
                    for (Object item : dataList) {
                        if (item instanceof Map<?, ?> registro) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> reg = (Map<String, Object>) registro;
                            Object cod = reg.get("cod");
                            if (cod != null && !(cod instanceof String)) {
                                reg.put("cod", String.valueOf(cod));
                            }
                        }
                    }
                }
            }
            log.info("✅ Tablas SUNAT cargadas: {} tablas", tablas.size());
        } catch (Exception e) {
            log.error("❌ Error cargando tablas_sunat.json", e);
        }
    }

    public List<Map<String, String>> getMenu() {
        return Collections.unmodifiableList(menu);
    }

    public Optional<Map<String, Object>> findByNombreTabla(String nombreTabla) {
        if (nombreTabla == null) return Optional.empty();
        String normalized = nombreTabla.trim().toUpperCase();
        return tablas.stream()
                .filter(t -> {
                    Object nombre = t.get("nombreTabla");
                    return nombre != null && nombre.toString().toUpperCase().equals(normalized);
                })
                .findFirst();
    }
}

