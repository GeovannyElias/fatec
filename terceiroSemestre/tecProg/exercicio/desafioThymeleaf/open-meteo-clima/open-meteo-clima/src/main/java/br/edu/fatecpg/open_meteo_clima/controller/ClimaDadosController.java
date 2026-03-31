package br.edu.fatecpg.open_meteo_clima.controller;


import br.edu.fatecpg.open_meteo_clima.model.ClimaDados;
import br.edu.fatecpg.open_meteo_clima.service.OpenMeteoClimaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ClimaDadosController {

    private final OpenMeteoClimaService openMeteoClimaService;

    public ClimaDadosController(OpenMeteoClimaService openMeteoClimaService) {
        this.openMeteoClimaService = openMeteoClimaService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/buscar")
    public String buscar(
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude,
            Model model) {

        ClimaDados resultado = openMeteoClimaService.buscarClima(latitude, longitude);
        model.addAttribute("coordenada", resultado);
        model.addAttribute("encontrado", resultado != null);


        if (resultado != null && resultado.getClimaAtual() != null) {
            String descricao = openMeteoClimaService.descricaoClima(resultado.getClimaAtual().getWeathercode());
            model.addAttribute("descricaoClima", descricao);
        }

        return "resultado";
    }
}

