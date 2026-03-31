package br.edu.fatecpg.rest_countries_pais.controller;

import br.edu.fatecpg.rest_countries_pais.model.Pais;
import br.edu.fatecpg.rest_countries_pais.service.RestCountriesService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PaisController {

    private final RestCountriesService restCountriesService;

    public PaisController(RestCountriesService restCountriesService) {
        this.restCountriesService = restCountriesService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/buscar")
    public String buscar(@RequestParam("pais") String pais, Model model) {
        Pais resultado = restCountriesService.buscarPorPais(pais);
        model.addAttribute("pais", resultado);
        model.addAttribute("encontrado", resultado != null);

        return "resultado";
    }
}