package com.br.edu.fatecpg.steam.controller;

import com.br.edu.fatecpg.steam.service.FuncionarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FuncionarioController {

    private final FuncionarioService service;

    public FuncionarioController(FuncionarioService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("funcionarios",   service.getTodos());
        model.addAttribute("filtrados",      service.getFiltradosPorSalario());
        model.addAttribute("comAumento",     service.getComAumento());
        model.addAttribute("ordenados",      service.getOrdenadosPorNome());
        model.addAttribute("totalSalarios",  service.getTotalSalarios());
        model.addAttribute("mediaPorDepto",  service.getMediaPorDepartamento());
        model.addAttribute("veterano",       service.getMaisVeterano().orElse(null));
        return "funcionarios";
    }
}