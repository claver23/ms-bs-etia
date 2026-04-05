package com.etlions.ms_bs_etia.app_config;

import com.etlions.ms_bs_etia.client.persistence.ClientEntity;
import com.etlions.ms_bs_etia.client.persistence.ClientRepository;
import com.etlions.ms_bs_etia.company.persistence.CompanyEntity;
import com.etlions.ms_bs_etia.company.persistence.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class DemoController {

    private final TestDataProperties testData;
    private final CompanyRepository companyRepository;
    private final ClientRepository clientRepository;

    @GetMapping({"/", "/index", "/demo"})
    public String index(Model model) {
        List<CompanyEntity> companies = companyRepository.findAll();
        CompanyEntity company = companies.isEmpty() ? null : companies.get(0);

        List<ClientEntity> clients = company != null
                ? clientRepository.findByCompanyId(company.getId())
                : List.of();

        model.addAttribute("company", company);
        model.addAttribute("clients", clients);
        model.addAttribute("client", clients.isEmpty() ? null : clients.get(0));
        model.addAttribute("products", testData.getProducts());
        model.addAttribute("ubigeos", testData.getUbigeos());
        model.addAttribute("testData", testData);
        return "index";
    }
}

