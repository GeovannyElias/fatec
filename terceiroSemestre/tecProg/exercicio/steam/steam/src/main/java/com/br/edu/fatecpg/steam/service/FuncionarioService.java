package com.br.edu.fatecpg.steam.service;

import com.br.edu.fatecpg.steam.model.Funcionario;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FuncionarioService {

    private final List<Funcionario> funcionarios;

    public FuncionarioService() {
        funcionarios = new ArrayList<>();
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
    }

    /** 1. Lista original */
    public List<Funcionario> getTodos() {
        return funcionarios;
    }

    /** 2. Filtragem: salário > 3000 */
    public List<Funcionario> getFiltradosPorSalario() {
        return funcionarios.stream()
                .filter(f -> f.getSalario() > 3000)
                .collect(Collectors.toList());
    }

    /** 3. Mapeamento: aumento de 5% para quem tem > 10 anos */
    public List<Funcionario> getComAumento() {
        return funcionarios.stream()
                .filter(f -> f.getAnosDeServico() > 10)
                .map(f -> new Funcionario(
                        f.getNome(),
                        f.getDepartamento(),
                        f.getSalario() * 1.05,
                        f.getAnosDeServico()
                ))
                .collect(Collectors.toList());
    }

    /** 4. Ordenação alfabética pelo nome */
    public List<Funcionario> getOrdenadosPorNome() {
        return funcionarios.stream()
                .sorted(Comparator.comparing(Funcionario::getNome))
                .collect(Collectors.toList());
    }

    /** 5. Redução: total da folha salarial */
    public double getTotalSalarios() {
        return funcionarios.stream()
                .map(Funcionario::getSalario)
                .reduce(0.0, Double::sum);
    }

    /** 6. Agrupamento: média salarial por departamento */
    public Map<String, Double> getMediaPorDepartamento() {
        return funcionarios.stream()
                .collect(Collectors.groupingBy(
                        Funcionario::getDepartamento,
                        Collectors.averagingDouble(Funcionario::getSalario)
                ));
    }

    /** Desafio 1: funcionário com maior tempo de serviço */
    public Optional<Funcionario> getMaisVeterano() {
        return funcionarios.stream()
                .max(Comparator.comparingInt(Funcionario::getAnosDeServico));
    }
}
