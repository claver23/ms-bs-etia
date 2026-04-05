package com.etlions.ms_bs_etia.app_config;

import com.etlions.ms_bs_etia.client.persistence.ClientEntity;
import com.etlions.ms_bs_etia.client.persistence.ClientRepository;
import com.etlions.ms_bs_etia.company.persistence.CompanyEntity;
import com.etlions.ms_bs_etia.company.persistence.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final TestDataProperties testData;
    private final CompanyRepository companyRepository;
    private final ClientRepository clientRepository;

    @Override
    public void run(String... args) {
        if (companyRepository.count() == 0 && testData.getCompany() != null) {
            log.info("🏗️ Cargando datos de prueba...");

            TestDataProperties.CompanyData c = testData.getCompany();
            CompanyEntity company = CompanyEntity.builder()
                    .ruc(c.getRuc())
                    .razonSocial(c.getRazonSocial())
                    .nombreComercial(c.getNombreComercial())
                    .codigoUbigeo(c.getCodigoUbigeo())
                    .direccion(c.getDireccion())
                    .urbanizacion(c.getUrbanizacion())
                    .departamento(c.getDepartamento())
                    .provincia(c.getProvincia())
                    .distrito(c.getDistrito())
                    .usuarioSol(c.getUsuarioSol())
                    .claveSol(c.getClaveSol())
                    .emailContacto(c.getEmailContacto())
                    .telefono(c.getTelefono())
                    .regimen(c.getRegimen())
                    .estado("ACTIVO")
                    .fechaCreacion(LocalDateTime.now())
                    .fechaActualizacion(LocalDateTime.now())
                    .build();
            CompanyEntity savedCompany = companyRepository.save(company);
            log.info("✅ Empresa demo creada: {} (ID: {})", savedCompany.getRazonSocial(), savedCompany.getId());

            if (testData.getClient() != null) {
                TestDataProperties.ClientData cl = testData.getClient();
                ClientEntity client = ClientEntity.builder()
                        .companyId(savedCompany.getId())
                        .tipoDocumento(cl.getTipoDocumento())
                        .numeroDocumento(cl.getNumeroDocumento())
                        .denominacion(cl.getDenominacion())
                        .direccion(cl.getDireccion())
                        .email(cl.getEmail())
                        .telefono(cl.getTelefono())
                        .fechaCreacion(LocalDateTime.now())
                        .build();
                ClientEntity savedClient = clientRepository.save(client);
                log.info("✅ Cliente demo creado: {} (ID: {})", savedClient.getDenominacion(), savedClient.getId());
            }

            log.info("🚀 Datos de prueba cargados. Demo disponible en http://localhost:8080");
        }
    }
}

