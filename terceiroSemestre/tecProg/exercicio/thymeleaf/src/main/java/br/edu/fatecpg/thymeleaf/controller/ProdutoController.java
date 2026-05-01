package br.edu.fatecpg.thymeleaf.controller;

import br.edu.fatecpg.thymeleaf.model.Produto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ProdutoController {

    private static final List<Produto> produtos = new ArrayList<>();

    @GetMapping("/")
    public String index() {
        return "redirect:/cadastrar";
    }

    @GetMapping("/cadastrar")
    public String cadastrar(Model model) {
        model.addAttribute("produto", new Produto());
        return "cadastrar";
    }

    @PostMapping("/cadastrar")
    public String cadastrarProduto(@ModelAttribute Produto produto, Model model) {
        if (produto.getPreco() <= 0) {
            model.addAttribute("erro", "O preço não pode ser menor ou igual a zero.");
            model.addAttribute("produto", produto); // mantém os dados digitados
            return "cadastrar";
        }
        produtos.add(produto);
        return "redirect:/lista";
    }

    @GetMapping("/lista")
    public String exibirLista(Model model) {
        model.addAttribute("produtos", produtos);
        return "lista";
    }

    @PostMapping("/remover")
    public String removerProduto(@RequestParam int index) {
        if (index >= 0 && index < produtos.size()) {
            produtos.remove(index);
        }
        return "redirect:/lista";
    }
}