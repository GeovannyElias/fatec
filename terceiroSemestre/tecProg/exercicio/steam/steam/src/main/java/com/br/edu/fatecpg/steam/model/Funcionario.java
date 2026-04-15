package com.br.edu.fatecpg.steam.model;

public class Funcionario {

    private String nome;
    private String departamento;
    private double salario;
    private int anosDeServico;

    public Funcionario(String nome, String departamento, double salario, int anosDeServico) {
        this.nome = nome;
        this.departamento = departamento;
        this.salario = salario;
        this.anosDeServico = anosDeServico;
    }

    public String getNome() { return nome; }
    public String getDepartamento() { return departamento; }
    public double getSalario() { return salario; }
    public int getAnosDeServico() { return anosDeServico; }

    public void setSalario(double salario) { this.salario = salario; }

    @Override
    public String toString() {
        return String.format("Funcionario{nome='%s', departamento='%s', salario=R$ %.2f, anosDeServico=%d}",
                nome, departamento, salario, anosDeServico);
    }
}
