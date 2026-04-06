package com.etlions.ms_bs_etia.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NotasCreditoViewController {

    @GetMapping("/notas-credito")
    public String notasCredito() {
        return "notas-credito";
    }
}

