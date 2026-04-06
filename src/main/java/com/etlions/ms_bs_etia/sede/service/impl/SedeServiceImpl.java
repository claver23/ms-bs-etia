package com.etlions.ms_bs_etia.sede.service.impl;

import com.etlions.ms_bs_etia.app_config.exception.ResourceNotFoundException;
import com.etlions.ms_bs_etia.sede.persistence.SedeEntity;
import com.etlions.ms_bs_etia.sede.persistence.SedeRepository;
import com.etlions.ms_bs_etia.sede.service.SedeService;
import com.etlions.ms_bs_etia.serie_config.persistence.SerieConfigEntity;
import com.etlions.ms_bs_etia.serie_config.persistence.SerieConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SedeServiceImpl implements SedeService {

    private final SedeRepository sedeRepository;
    private final SerieConfigRepository serieConfigRepository;

    @Override
    public List<SedeEntity> listarPorCompany(String companyId) {
        return sedeRepository.findByCompanyId(companyId);
    }

    @Override
    public List<SedeEntity> listarPorCompanyYEstado(String companyId, String estado) {
        return sedeRepository.findByCompanyIdAndEstado(companyId, estado);
    }

    @Override
    public SedeEntity obtenerSede(String sedeId) {
        return sedeRepository.findById(sedeId)
                .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada: " + sedeId));
    }

    @Override
    @Transactional
    public SedeEntity crearSede(SedeEntity sede, boolean generarSeries) {
        // Auto-generar código correlativo de 3 dígitos
        long count = sedeRepository.countByCompanyId(sede.getCompanyId());
        sede.setCodigo(String.format("%03d", count + 1));

        // Primera sede es default
        if (count == 0) {
            sede.setEsDefault(true);
        }

        sede.setEstado("ACTIVO");
        sede.setFechaCreacion(LocalDateTime.now());
        SedeEntity saved = sedeRepository.save(sede);

        if (generarSeries) {
            generarSeriesParaSede(saved.getId());
        }

        return saved;
    }

    @Override
    @Transactional
    public SedeEntity actualizarSede(String sedeId, SedeEntity updated) {
        SedeEntity entity = obtenerSede(sedeId);
        if (updated.getNombre() != null) entity.setNombre(updated.getNombre());
        if (updated.getDireccion() != null) entity.setDireccion(updated.getDireccion());
        if (updated.getTelefono() != null) entity.setTelefono(updated.getTelefono());
        if (updated.getEmail() != null) entity.setEmail(updated.getEmail());
        return sedeRepository.save(entity);
    }

    @Override
    @Transactional
    public void eliminarSede(String sedeId) {
        SedeEntity entity = obtenerSede(sedeId);
        entity.setEstado("INACTIVO");
        // Desactivar series asociadas
        serieConfigRepository.findBySedeId(sedeId).forEach(s -> {
            s.setActiva(false);
            serieConfigRepository.save(s);
        });
        sedeRepository.save(entity);
    }

    /**
     * Genera las 4 series de comprobantes para una sede:
     * - FACTURA  → F{código}   ej: F001
     * - BOLETA   → B{código}   ej: B001
     * - NOTA_CREDITO → FC{últimos 2 dígitos}  ej: FC01
     * - NOTA_DEBITO  → FD{últimos 2 dígitos}  ej: FD01
     *
     * El correlativo del documento (8 dígitos zero-padded) se maneja en la tabla correlativos,
     * aquí solo se genera el código de la serie.
     */
    @Override
    @Transactional
    public List<SerieConfigEntity> generarSeriesParaSede(String sedeId) {
        SedeEntity sede = obtenerSede(sedeId);
        String cod = sede.getCodigo(); // "001", "002", etc.
        String cod2 = cod.substring(cod.length() - 2); // últimos 2 dígitos: "01", "02"

        boolean esDefault = sede.getEsDefault() != null && sede.getEsDefault();

        // Verificar que no existan series ya para esta sede
        List<SerieConfigEntity> existentes = serieConfigRepository.findBySedeId(sedeId);
        if (!existentes.isEmpty()) {
            return existentes; // Ya tiene series, retornarlas
        }

        List<SerieConfigEntity> series = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // FACTURA → F001
        series.add(SerieConfigEntity.builder()
                .companyId(sede.getCompanyId()).sedeId(sedeId)
                .tipoDocumento("FACTURA").serie("F" + cod)
                .descripcion("Factura — " + sede.getNombre())
                .esDefault(esDefault).activa(true).fechaCreacion(now)
                .build());

        // BOLETA → B001
        series.add(SerieConfigEntity.builder()
                .companyId(sede.getCompanyId()).sedeId(sedeId)
                .tipoDocumento("BOLETA").serie("B" + cod)
                .descripcion("Boleta — " + sede.getNombre())
                .esDefault(esDefault).activa(true).fechaCreacion(now)
                .build());

        // NOTA_CREDITO → FC01
        series.add(SerieConfigEntity.builder()
                .companyId(sede.getCompanyId()).sedeId(sedeId)
                .tipoDocumento("NOTA_CREDITO").serie("FC" + cod2)
                .descripcion("Nota Crédito — " + sede.getNombre())
                .esDefault(esDefault).activa(true).fechaCreacion(now)
                .build());

        // NOTA_DEBITO → FD01
        series.add(SerieConfigEntity.builder()
                .companyId(sede.getCompanyId()).sedeId(sedeId)
                .tipoDocumento("NOTA_DEBITO").serie("FD" + cod2)
                .descripcion("Nota Débito — " + sede.getNombre())
                .esDefault(esDefault).activa(true).fechaCreacion(now)
                .build());

        return serieConfigRepository.saveAll(series);
    }
}

