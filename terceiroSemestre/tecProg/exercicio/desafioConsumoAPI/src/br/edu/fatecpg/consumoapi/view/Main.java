package br.edu.fatecpg.consumoapi.view;

import br.edu.fatecpg.consumoapi.dao.EmpresaDAO;
import br.edu.fatecpg.consumoapi.dao.SocioDAO;
import br.edu.fatecpg.consumoapi.db.DB;
import br.edu.fatecpg.consumoapi.model.Empresa;
import br.edu.fatecpg.consumoapi.model.Socio;
import br.edu.fatecpg.consumoapi.service.BrasilApi;
import com.google.gson.Gson;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Gson gson          = new Gson();
        Scanner scan       = new Scanner(System.in);
        EmpresaDAO empresaDAO = new EmpresaDAO();
        SocioDAO   socioDAO   = new SocioDAO();

        System.out.println("================================================");
        System.out.println("   Bem-vindo ao Cadastro de Empresas via CNPJ   ");
        System.out.println("================================================");

        boolean rodando = true;

        while (rodando) {
            System.out.println("\n[1] Cadastrar empresa");
            System.out.println("[2] Listar todas as empresas");
            System.out.println("[3] Excluir empresa");
            System.out.println("[4] Sair");
            System.out.print("Opção: ");

            int opcao;
            try {
                opcao = Integer.parseInt(scan.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Digite um número de 1 a 4.");
                continue;
            }

            switch (opcao) {

                // ----------------------------------------------------------------
                // CASO 1 — CADASTRAR
                // ----------------------------------------------------------------
                case 1 -> {
                    System.out.println("\n--- CADASTRAR EMPRESA ---");
                    System.out.print("Digite o CNPJ: ");
                    String cnpjDigitado = scan.nextLine();

                    // Limpa pontos, traços e barras — a API aceita apenas dígitos
                    String cnpjNormalizado = cnpjDigitado.replaceAll("[^0-9]", "");

                    if (cnpjNormalizado.length() != 14) {
                        System.out.println("CNPJ inválido. Informe os 14 dígitos.");
                        break;
                    }

                    // 1. Consulta a BrasilAPI
                    String json;
                    try {
                        json = BrasilApi.buscaEmpresa(cnpjNormalizado);
                    } catch (IllegalArgumentException e) {
                        System.out.println("Erro: " + e.getMessage());
                        break;
                    } catch (Exception e) {
                        System.out.println("Falha de conexão com a API: " + e.getMessage());
                        break;
                    }

                    // 2. Mapeia o JSON para os objetos
                    Empresa empresa = gson.fromJson(json, Empresa.class);

                    // 3. Persiste empresa + sócios em uma única conexão (transação implícita)
                    try (Connection conn = DB.connection()) {
                        conn.setAutoCommit(false); // inicia transação manual

                        try {
                            // 3a. Insere a empresa e captura o ID gerado
                            final String sqlEmpresa =
                                    "INSERT INTO empresa (cnpj, razao_social, nome_fantasia, logradouro) " +
                                    "VALUES (?, ?, ?, ?)";

                            int empresaId;
                            try (var stmt = conn.prepareStatement(
                                    sqlEmpresa, java.sql.Statement.RETURN_GENERATED_KEYS)) {

                                stmt.setString(1, empresa.getCnpj());
                                stmt.setString(2, empresa.getRazaoSocial());
                                stmt.setString(3, empresa.getNomeFantasia());
                                stmt.setString(4, empresa.getLogradouro());
                                stmt.executeUpdate();

                                try (var keys = stmt.getGeneratedKeys()) {
                                    if (!keys.next())
                                        throw new SQLException("Falha ao obter ID gerado.");
                                    empresaId = keys.getInt(1);
                                    empresa.setId(empresaId);
                                }
                            }

                            // 3b. Insere cada sócio do QSA com o ID real da empresa
                            List<Socio> qsa = empresa.getQsa();
                            socioDAO.inserirLote(qsa, empresaId, conn);

                            conn.commit(); // confirma tudo de uma vez

                            System.out.println("\nEmpresa cadastrada com sucesso! ID: " + empresaId);
                            System.out.println("Razão Social : " + empresa.getRazaoSocial());
                            System.out.println("Nome Fantasia: " + empresa.getNomeFantasia());
                            System.out.println("Logradouro   : " + empresa.getLogradouro());
                            System.out.println("Sócios (QSA) : " + qsa.size() + " registrado(s)");

                        } catch (SQLException e) {
                            conn.rollback(); // desfaz tudo se algo falhar

                            if (e.getMessage().contains("unique") || e.getSQLState().startsWith("23")) {
                                System.out.println("Empresa já está cadastrada no banco de dados.");
                            } else {
                                System.out.println("Erro ao salvar no banco: " + e.getMessage());
                            }
                        }

                    } catch (SQLException e) {
                        System.out.println("Banco de dados indisponível: " + e.getMessage());
                    }
                }

                // ----------------------------------------------------------------
                // CASO 2 — LISTAR
                // ----------------------------------------------------------------
                case 2 -> {
                    System.out.println("\n--- EMPRESAS CADASTRADAS ---");
                    try {
                        List<Empresa> empresas = empresaDAO.listarTodas();

                        if (empresas.isEmpty()) {
                            System.out.println("Nenhuma empresa cadastrada ainda.");
                        } else {
                            for (Empresa emp : empresas) {
                                System.out.println("─".repeat(50));
                                System.out.println("ID           : " + emp.getId());
                                System.out.println("CNPJ         : " + emp.getCnpj());
                                System.out.println("Razão Social : " + emp.getRazaoSocial());
                                System.out.println("Nome Fantasia: " + emp.getNomeFantasia());
                                System.out.println("Logradouro   : " + emp.getLogradouro());

                                List<Socio> socios = emp.getQsa();
                                if (socios.isEmpty()) {
                                    System.out.println("Sócios       : (nenhum)");
                                } else {
                                    System.out.println("Sócios (" + socios.size() + "):");
                                    for (Socio s : socios) {
                                        System.out.printf("  • %-40s  CPF/CNPJ: %-18s  Qualif.: %s%n",
                                                s.getNomeSocio(),
                                                s.getCnpjCpfDoSocio(),
                                                s.getQualificacaoSocio());
                                    }
                                }
                            }
                            System.out.println("─".repeat(50));
                            System.out.println("Total: " + empresas.size() + " empresa(s).");
                        }

                    } catch (SQLException e) {
                        System.out.println("Banco de dados indisponível: " + e.getMessage());
                    }
                }

                // ----------------------------------------------------------------
                // CASO 3 — EXCLUIR
                // ----------------------------------------------------------------
                case 3 -> {
                    System.out.println("\n--- EXCLUIR EMPRESA ---");
                    System.out.print("Digite o CNPJ da empresa a excluir: ");
                    String cnpjExcluir = scan.nextLine().replaceAll("[^0-9]", "");

                    try {
                        boolean removido = empresaDAO.excluir(cnpjExcluir);
                        if (removido) {
                            System.out.println("Empresa (e seus sócios) excluída com sucesso.");
                        } else {
                            System.out.println("CNPJ não encontrado no banco de dados.");
                        }
                    } catch (SQLException e) {
                        System.out.println("Banco de dados indisponível: " + e.getMessage());
                    }
                }

                // ----------------------------------------------------------------
                // CASO 4 — SAIR
                // ----------------------------------------------------------------
                case 4 -> {
                    System.out.println("Encerrando o sistema. Até logo!");
                    rodando = false;
                }

                default -> System.out.println("Opção inválida. Escolha entre 1 e 4.");
            }
        }

        scan.close();
    }
}
