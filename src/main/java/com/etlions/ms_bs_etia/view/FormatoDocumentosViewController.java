package com.etlions.ms_bs_etia.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FormatoDocumentosViewController {

    @GetMapping("/formato-documentos")
    public String formatoDocumentos() {
        return "formato-documentos";
    }
}

