package com.etlions.ms_bs_etia.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CatalogoSunatViewController {

    @GetMapping("/catalogo-sunat")
    public String catalogoSunat() {
        return "catalogo-sunat";
    }
}

