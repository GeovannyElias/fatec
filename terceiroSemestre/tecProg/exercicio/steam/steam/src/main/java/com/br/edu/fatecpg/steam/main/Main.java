package com.br.edu.fatecpg.steam.main;

import com.br.edu.fatecpg.steam.model.Funcionario;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {

    public void executar() {


        List<Funcionario> funcionarios = new ArrayList<>();
        funcionarios.add(new Funcionario("Ana Souza",      "TI",          4500.00, 12));
        funcionarios.add(new Funcionario("Bruno Lima",     "TI",          3200.00,  5));
        funcionarios.add(new Funcionario("Carlos Mendes",  "RH",          2800.00, 15));
        funcionarios.add(new Funcionario("Daniela Costa",  "RH",          3100.00,  8));
        funcionarios.add(new Funcionario("Eduardo Alves",  "Financeiro",  5200.00, 20));
        funcionarios.add(new Funcionario("Fernanda Rocha", "Financeiro",  4100.00,  3));
        funcionarios.add(new Funcionario("Gabriel Torres", "TI",          2600.00, 11));
        funcionarios.add(new Funcionario("Helena Martins", "Financeiro",  3800.00,  7));
        funcionarios.add(new Funcionario("Igor Neves",     "RH",          2400.00,  2));
        funcionarios.add(new Funcionario("Juliana Campos", "TI",          5000.00, 18));

        separator("LISTA ORIGINAL");
        funcionarios.forEach(System.out::println);


        separator("FILTRAGEM — Salário superior a R$ 3.000,00");
        funcionarios.stream()
                .filter(f -> f.getSalario() > 3000)
                .forEach(f -> System.out.println(formatado(f)));


        separator("MAPEAMENTO — Aumento de 5% (> 10 anos de serviço)");
        List<Funcionario> comAumento = funcionarios.stream()
                .map(f -> {
                    if (f.getAnosDeServico() > 10) {
                        return new Funcionario(
                                f.getNome(),
                                f.getDepartamento(),
                                f.getSalario() * 1.05,
                                f.getAnosDeServico()
                        );
                    }
                    return f;
                })
                .collect(Collectors.toList());

        comAumento.stream()
                .filter(f -> f.getAnosDeServico() > 10)
                .forEach(f -> System.out.printf(
                        "%-20s → Novo salário: R$ %.2f%n", f.getNome(), f.getSalario()));


        separator("ORDENAÇÃO — Alfabética pelo nome");
        funcionarios.stream()
                .sorted(Comparator.comparing(Funcionario::getNome))
                .forEach(f -> System.out.println(formatado(f)));


        separator("REDUÇÃO — Total da folha salarial");
        double totalSalarios = funcionarios.stream()
                .map(Funcionario::getSalario)
                .reduce(0.0, Double::sum);
        System.out.printf("Total gasto com salários: R$ %.2f%n", totalSalarios);


        separator("AGRUPAMENTO — Média salarial por departamento");
        Map<String, Double> mediaPorDepto = funcionarios.stream()
                .collect(Collectors.groupingBy(
                        Funcionario::getDepartamento,
                        Collectors.averagingDouble(Funcionario::getSalario)
                ));

        mediaPorDepto.forEach((depto, media) ->
                System.out.printf("%-12s → Média: R$ %.2f%n", depto, media));


        separator("DESAFIO — Maior tempo de serviço");
        funcionarios.stream()
                .max(Comparator.comparingInt(Funcionario::getAnosDeServico))
                .ifPresent(f -> System.out.printf(
                        "Funcionário mais veterano: %s (%d anos)%n",
                        f.getNome(), f.getAnosDeServico()));


        separator("DESAFIO — Saída formatada personalizada");
        funcionarios.stream()
                .sorted(Comparator.comparing(Funcionario::getNome))
                .forEach(f -> System.out.printf(
                        "Funcionário: %s, Departamento: %s, Salário: R$ %.2f%n",
                        f.getNome(), f.getDepartamento(), f.getSalario()));
    }

    private void separator(String titulo) {
        System.out.println("\n" + "═".repeat(55));
        System.out.println("  " + titulo);
        System.out.println("═".repeat(55));
    }

    private String formatado(Funcionario f) {
        return String.format("Funcionário: %-20s | Depto: %-11s | Salário: R$ %.2f",
                f.getNome(), f.getDepartamento(), f.getSalario());
    }
}