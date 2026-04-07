package com.etlions.ms_bs_etia.catalogo_sunat.standard;

import com.etlions.openapicodegen.dto.CatalogoSunatRegistro;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Catálogo No. 25 — UNSPSC v14_0801: Código producto SUNAT.
 * Subconjunto representativo de códigos UNSPSC más usados en facturación electrónica peruana.
 */
@Component
public class CodigoProductoStandardProvider implements CatalogoStandardProvider {

    private static final String CATALOGO_NOMBRE = "Catálogo No. 25: Código producto SUNAT";

    private static final List<CatalogoSunatRegistro> PRODUCTOS = List.of(
            // ── Alimentos y bebidas ──
            reg("50000000", "Alimentos, bebidas y tabaco"),
            reg("50101500", "Frutas frescas"),
            reg("50101700", "Vegetales frescos"),
            reg("50112000", "Carne y aves frescas"),
            reg("50131600", "Productos de panadería"),
            reg("50151500", "Aceites y grasas comestibles"),
            reg("50171500", "Bebidas no alcohólicas"),
            reg("50171700", "Bebidas alcohólicas"),
            reg("50181900", "Productos lácteos"),
            reg("50192100", "Cereales y legumbres"),
            reg("50202300", "Conservas y alimentos procesados"),

            // ── Productos de limpieza y cuidado personal ──
            reg("47131500", "Equipo de lavandería"),
            reg("47131600", "Productos de limpieza y suministros"),
            reg("53131600", "Productos para el cuidado personal"),

            // ── Materiales de oficina ──
            reg("44121600", "Suministros de escritorio"),
            reg("44121700", "Instrumentos de escritura"),
            reg("44121800", "Cintas adhesivas y pegamentos de oficina"),
            reg("44121900", "Carpetas y archivadores"),
            reg("44122000", "Sujetadores de oficina"),
            reg("44122100", "Papel de oficina"),
            reg("14111500", "Papel de impresión y escritura"),
            reg("14111800", "Papel para uso especializado"),

            // ── Equipos de cómputo y accesorios ──
            reg("43211500", "Computadores"),
            reg("43211501", "Computadores de escritorio"),
            reg("43211503", "Computadores portátiles (laptops)"),
            reg("43211507", "Tabletas"),
            reg("43211600", "Accesorios de computador"),
            reg("43211700", "Dispositivos de almacenamiento"),
            reg("43211800", "Monitores y pantallas de computador"),
            reg("43211900", "Impresoras de computador"),
            reg("43212100", "Componentes y suministros de impresora"),

            // ── Redes y comunicaciones ──
            reg("43222600", "Equipos de redes"),
            reg("43222500", "Enrutadores (routers)"),
            reg("43223300", "Infraestructura de red"),
            reg("43231500", "Software funcional específico de la empresa"),
            reg("43231600", "Software de gestión de redes"),
            reg("43232100", "Software de sistema operativo"),
            reg("43232300", "Software de utilidades y dispositivos"),
            reg("43232400", "Software de seguridad y protección"),
            reg("43232500", "Software de ofimática"),
            reg("43232600", "Software de comunicaciones"),
            reg("43232900", "Software de base de datos"),

            // ── Maquinaria y equipo industrial ──
            reg("23000000", "Maquinaria industrial y productos de manufactura"),
            reg("23150000", "Maquinaria y equipo para manufactura"),
            reg("23151500", "Equipo de soldadura y accesorios"),
            reg("26000000", "Maquinaria y accesorios eléctricos"),
            reg("26111500", "Baterías y acumuladores"),
            reg("26111600", "Células de energía"),
            reg("26111700", "Generadores de energía"),
            reg("26121600", "Cables eléctricos"),
            reg("27111500", "Herramientas manuales"),
            reg("27111700", "Herramientas de corte"),

            // ── Productos de construcción ──
            reg("30100000", "Materiales estructurales"),
            reg("30101500", "Bloques y ladrillos"),
            reg("30101700", "Concreto y morteros"),
            reg("30102000", "Madera y productos de madera"),
            reg("30111500", "Acero y hierro"),
            reg("30131500", "Puertas y ventanas"),
            reg("30161500", "Pinturas y recubrimientos"),
            reg("30171500", "Tubería y accesorios"),
            reg("39121000", "Materiales de iluminación"),

            // ── Mobiliario ──
            reg("56101500", "Sillas de oficina"),
            reg("56101700", "Escritorios"),
            reg("56111500", "Estanterías y almacenamiento"),
            reg("56121000", "Mobiliario de dormitorio"),
            reg("56121500", "Mobiliario de sala"),

            // ── Textiles y vestuario ──
            reg("53100000", "Ropa, maletas y productos personales"),
            reg("53101500", "Ropa de caballero"),
            reg("53101600", "Ropa de dama"),
            reg("53102500", "Calzado"),
            reg("53111500", "Maletas y bolsas"),
            reg("53102700", "Uniformes"),

            // ── Vehículos y transporte ──
            reg("25101500", "Vehículos de motor"),
            reg("25101700", "Vehículos de transporte de carga"),
            reg("25101900", "Motocicletas y bicicletas"),
            reg("25172000", "Llantas y cámaras"),
            reg("25181600", "Repuestos de vehículos"),
            reg("78101800", "Transporte de carga"),
            reg("78111800", "Transporte de pasajeros"),

            // ── Servicios profesionales y consultoría ──
            reg("80101500", "Servicios de consultoría de negocios"),
            reg("80101600", "Servicios de asesoría de gestión"),
            reg("80111500", "Servicios de contabilidad"),
            reg("80111600", "Servicios de auditoría"),
            reg("80111700", "Servicios de preparación de impuestos"),
            reg("80121500", "Servicios de abogados"),
            reg("80121600", "Servicios de asesoría legal"),
            reg("80121700", "Servicios legales sobre contratos"),
            reg("80131500", "Servicios de publicidad"),
            reg("80141500", "Servicios de diseño gráfico"),
            reg("80141600", "Servicios de mercadeo"),
            reg("80161500", "Servicios de apoyo gerencial"),

            // ── Servicios de TI ──
            reg("81111500", "Ingeniería de software o hardware"),
            reg("81111600", "Programación de software"),
            reg("81111700", "Sistemas de información y consultoría"),
            reg("81111800", "Servicios de internet"),
            reg("81111900", "Mantenimiento y soporte de software"),
            reg("81112000", "Servicios de datos"),
            reg("81112200", "Mantenimiento y soporte de hardware de computador"),

            // ── Servicios financieros y seguros ──
            reg("84101500", "Servicios de desarrollo empresarial"),
            reg("84111500", "Servicios bancarios"),
            reg("84121500", "Servicios de seguros de vida y salud"),
            reg("84121600", "Servicios de seguros de propiedad y accidentes"),
            reg("84121700", "Servicios de seguros personales"),
            reg("84131500", "Fondos de inversión y mercado de capitales"),

            // ── Servicios educativos ──
            reg("86101700", "Servicios de educación superior"),
            reg("86111600", "Servicios de educación de adultos"),
            reg("86131500", "Servicios de capacitación vocacional"),
            reg("86132000", "Servicios de capacitación tecnológica"),

            // ── Servicios de salud ──
            reg("85101500", "Servicios de hospital"),
            reg("85101600", "Servicios de clínica"),
            reg("85111500", "Servicios de médicos"),
            reg("85111600", "Servicios de enfermería"),
            reg("85121500", "Servicios de farmacia"),
            reg("42000000", "Equipos y suministros médicos"),
            reg("42131500", "Instrumentos quirúrgicos"),
            reg("42142100", "Medicamentos y productos farmacéuticos"),

            // ── Energía, combustibles y minería ──
            reg("15101500", "Petróleo crudo"),
            reg("15101600", "Petróleo refinado — Gasolina"),
            reg("15101700", "Petróleo refinado — Diésel"),
            reg("15111500", "Gas natural"),
            reg("15121500", "Carbón"),
            reg("15131500", "Energía eléctrica"),

            // ── Telecomunicaciones ──
            reg("83111500", "Servicios de telecomunicaciones por cable"),
            reg("83111600", "Servicios de telecomunicaciones inalámbricas"),
            reg("83111800", "Servicios de transmisión de datos"),
            reg("83112200", "Servicios de telefonía IP"),

            // ── Servicios de ingeniería y arquitectura ──
            reg("81101500", "Ingeniería civil"),
            reg("81101600", "Ingeniería mecánica"),
            reg("81101700", "Ingeniería eléctrica y electrónica"),
            reg("81102200", "Servicios de arquitectura"),

            // ── Productos agropecuarios ──
            reg("10101500", "Animales vivos"),
            reg("10151500", "Semillas y plantas de vivero"),
            reg("10171500", "Fertilizantes"),
            reg("10191500", "Pesticidas y productos agroquímicos"),

            // ── Otros servicios generales ──
            reg("72101500", "Servicios de mantenimiento de edificios"),
            reg("72102900", "Servicios de limpieza de edificios"),
            reg("72141100", "Servicios de seguridad"),
            reg("73152100", "Servicios de alquiler de maquinaria"),
            reg("76111500", "Servicios de limpieza industrial"),
            reg("90101600", "Servicios de restaurantes y catering"),
            reg("90111500", "Servicios de hotelería y alojamiento"),
            reg("90121500", "Servicios de guía turística y viajes"),
            reg("92121500", "Servicios de policía y seguridad pública"),
            reg("95121500", "Terrenos y parcelas")
    );

    @Override
    public boolean supports(String nombreCatalogo) {
        return CATALOGO_NOMBRE.equalsIgnoreCase(nombreCatalogo != null ? nombreCatalogo.trim() : "");
    }

    @Override
    public List<CatalogoSunatRegistro> obtenerRegistros() {
        return PRODUCTOS;
    }

    private static CatalogoSunatRegistro reg(String codigo, String descripcion) {
        return new CatalogoSunatRegistro().codigo(codigo).descripcion(descripcion);
    }
}

