package com.etlions.ms_bs_etia.app_util.service;

import com.etlions.ms_bs_etia.app_util.persistence.CorrelativoEntity;
import com.etlions.ms_bs_etia.app_util.persistence.CorrelativoRepository;
import com.etlions.ms_bs_etia.auth.persistence.UserRepository;
import com.etlions.ms_bs_etia.company.persistence.CompanyEntity;
import com.etlions.ms_bs_etia.company.persistence.CompanyRepository;
import com.etlions.ms_bs_etia.sede.persistence.SedeEntity;
import com.etlions.ms_bs_etia.sede.persistence.SedeRepository;
import com.etlions.ms_bs_etia.serie_config.persistence.SerieConfigEntity;
import com.etlions.ms_bs_etia.serie_config.persistence.SerieConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OnboardingServiceTest {

    @Mock private CompanyRepository companyRepository;
    @Mock private SedeRepository sedeRepository;
    @Mock private SerieConfigRepository serieConfigRepository;
    @Mock private CorrelativoRepository correlativoRepository;
    @Mock private UserRepository userRepository;

    @Captor private ArgumentCaptor<CompanyEntity> companyCaptor;
    @Captor private ArgumentCaptor<SedeEntity> sedeCaptor;
    @Captor private ArgumentCaptor<SerieConfigEntity> serieCaptor;
    @Captor private ArgumentCaptor<CorrelativoEntity> correlativoCaptor;

    private OnboardingService onboardingService;

    @BeforeEach
    void setUp() {
        onboardingService = new OnboardingService(
                companyRepository, sedeRepository, serieConfigRepository, correlativoRepository, userRepository
        );
    }

    @Test
    @DisplayName("registrarEmpresa — crea company + sede + 4 series + 4 correlativos")
    void registrarEmpresa_createsAllEntities() {
        // Arrange
        when(companyRepository.save(any(CompanyEntity.class))).thenAnswer(inv -> {
            CompanyEntity e = inv.getArgument(0);
            e.setId("company-uuid-001");
            return e;
        });
        when(sedeRepository.save(any(SedeEntity.class))).thenAnswer(inv -> {
            SedeEntity e = inv.getArgument(0);
            e.setId("sede-uuid-001");
            return e;
        });
        when(serieConfigRepository.save(any(SerieConfigEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(correlativoRepository.save(any(CorrelativoEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        OnboardingService.OnboardingResult result = onboardingService.registrarEmpresa(
                "20601234567", "ETLIONS SAC", "Av. Javier Prado 1234",
                "LIMA", "LIMA", "SAN ISIDRO", "150131"
        );

        // Assert
        assertNotNull(result);
        assertEquals("company-uuid-001", result.companyId());
        assertEquals("sede-uuid-001", result.sedeId());
        assertTrue(result.message().contains("F001"));

        // Verificar que se creó la empresa
        verify(companyRepository).save(companyCaptor.capture());
        CompanyEntity savedCompany = companyCaptor.getValue();
        assertEquals("20601234567", savedCompany.getRuc());
        assertEquals("ETLIONS SAC", savedCompany.getRazonSocial());
        assertEquals("ACTIVO", savedCompany.getEstado());
        assertEquals("MODDATOS", savedCompany.getUsuarioSol());
        assertEquals("MODDATOS", savedCompany.getClaveSol());
        assertTrue(savedCompany.getEmisionEnEnvio());

        // Verificar que se creó la sede
        verify(sedeRepository).save(sedeCaptor.capture());
        SedeEntity savedSede = sedeCaptor.getValue();
        assertEquals("company-uuid-001", savedSede.getCompanyId());
        assertEquals("001", savedSede.getCodigo());
        assertEquals("Sede Principal", savedSede.getNombre());
        assertTrue(savedSede.getEsDefault());

        // Verificar 4 series
        verify(serieConfigRepository, times(4)).save(serieCaptor.capture());
        var series = serieCaptor.getAllValues();
        assertEquals(4, series.size());
        assertTrue(series.stream().anyMatch(s -> "F001".equals(s.getSerie()) && "FACTURA".equals(s.getTipoDocumento())));
        assertTrue(series.stream().anyMatch(s -> "B001".equals(s.getSerie()) && "BOLETA".equals(s.getTipoDocumento())));
        assertTrue(series.stream().anyMatch(s -> "FC01".equals(s.getSerie()) && "NOTA_CREDITO".equals(s.getTipoDocumento())));
        assertTrue(series.stream().anyMatch(s -> "FD01".equals(s.getSerie()) && "NOTA_DEBITO".equals(s.getTipoDocumento())));
        series.forEach(s -> {
            assertTrue(s.getEsDefault());
            assertTrue(s.getActiva());
            assertEquals("company-uuid-001", s.getCompanyId());
            assertEquals("sede-uuid-001", s.getSedeId());
        });

        // Verificar 4 correlativos
        verify(correlativoRepository, times(4)).save(correlativoCaptor.capture());
        var correlativos = correlativoCaptor.getAllValues();
        assertEquals(4, correlativos.size());
        correlativos.forEach(c -> {
            assertEquals("company-uuid-001", c.getCompanyId());
            assertEquals(0, c.getUltimoNumero());
        });
    }

    @Test
    @DisplayName("registrarEmpresa — con campos opcionales null no falla")
    void registrarEmpresa_nullOptionalFields_doesNotFail() {
        when(companyRepository.save(any(CompanyEntity.class))).thenAnswer(inv -> {
            CompanyEntity e = inv.getArgument(0);
            e.setId("c1");
            return e;
        });
        when(sedeRepository.save(any(SedeEntity.class))).thenAnswer(inv -> {
            SedeEntity e = inv.getArgument(0);
            e.setId("s1");
            return e;
        });
        when(serieConfigRepository.save(any(SerieConfigEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(correlativoRepository.save(any(CorrelativoEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() ->
                onboardingService.registrarEmpresa("20999999999", "TEST SAC", null, null, null, null, null)
        );
    }
}

