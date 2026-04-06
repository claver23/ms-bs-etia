package com.etlions.ms_bs_etia.app_config;

import org.springframework.context.annotation.Configuration;

/**
 * Thymeleaf se auto-configura por Spring Boot mediante las propiedades
 * en application.yaml (spring.thymeleaf.cache, etc.).
 *
 * NO definir beans de TemplateResolver/TemplateEngine/ViewResolver aquí,
 * porque anularían la auto-configuración y se perdería el soporte
 * correcto de th:inline="javascript" con Jackson JavaTimeModule.
 */
@Configuration
public class ThymeleafConfig {
    // Spring Boot auto-configura Thymeleaf correctamente
}

