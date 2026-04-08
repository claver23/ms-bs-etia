package com.etlions.ms_bs_etia.company.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "companies")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompanyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true)
    private String ruc;

    @Column(name = "razon_social")
    private String razonSocial;

    @Column(name = "nombre_comercial")
    private String nombreComercial;

    @Column(name = "codigo_ubigeo")
    private String codigoUbigeo;

    private String direccion;

    private String urbanizacion;

    private String departamento;

    private String provincia;

    private String distrito;

    @Column(name = "usuario_sol")
    private String usuarioSol;

    @Column(name = "clave_sol")
    private String claveSol;

    @Column(name = "certificado_path")
    private String certificadoPath;

    @Column(name = "certificado_password")
    private String certificadoPassword;

    @Column(name = "email_contacto")
    private String emailContacto;

    private String telefono;

    private String regimen;

    private String estado;

    @Column(name = "logo_path")
    private String logoPath;

    @Column(name = "emision_en_envio")
    @Builder.Default
    private Boolean emisionEnEnvio = true;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}

