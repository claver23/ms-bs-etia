package com.etlions.ms_bs_etia.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WizardViewController {

    @GetMapping("/wizard")
    public String wizard(@RequestParam(defaultValue = "FACTURA") String type) {
        return "wizard/factura";
    }

    @GetMapping("/wizard-nota")
    public String wizardNota(@RequestParam(defaultValue = "NOTA_CREDITO") String type) {
        return "wizard/factura";
    }
}
