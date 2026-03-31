package br.edu.fatecpg.consumoapi.dao;

import br.edu.fatecpg.consumoapi.db.DB;
import br.edu.fatecpg.consumoapi.model.Empresa;
import br.edu.fatecpg.consumoapi.model.Socio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO responsável pelas operações de persistência da entidade Empresa.
 * Toda abertura/fechamento de recursos JDBC é feito via try-with-resources.
 */
public class EmpresaDAO {

    // ------------------------------------------------------------------
    // INSERT
    // ------------------------------------------------------------------

    /**
     * Insere uma empresa e retorna o ID gerado pelo banco.
     * Lança SQLException se o CNPJ já estiver cadastrado (UNIQUE constraint).
     */
    public int inserir(Empresa empresa) throws SQLException {
        final String sql =
                "INSERT INTO empresa (cnpj, razao_social, nome_fantasia, logradouro) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = DB.connection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, empresa.getCnpj());
            stmt.setString(2, empresa.getRazaoSocial());
            stmt.setString(3, empresa.getNomeFantasia());
            stmt.setString(4, empresa.getLogradouro());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    empresa.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Falha ao obter ID gerado para a empresa.");
    }

    // ------------------------------------------------------------------
    // SELECT ALL (com sócios)
    // ------------------------------------------------------------------

    /**
     * Lista todas as empresas com seus respectivos sócios.
     */
    public List<Empresa> listarTodas() throws SQLException {
        final String sqlEmpresa = "SELECT * FROM empresa ORDER BY id";
        final String sqlSocio   =
                "SELECT * FROM socio WHERE empresa_id = ? ORDER BY id";

        List<Empresa> lista = new ArrayList<>();

        try (Connection conn = DB.connection();
             PreparedStatement stmtEmp = conn.prepareStatement(sqlEmpresa);
             ResultSet rsEmp = stmtEmp.executeQuery()) {

            while (rsEmp.next()) {
                Empresa emp = mapearEmpresa(rsEmp);

                // Busca os sócios desta empresa
                try (PreparedStatement stmtSoc = conn.prepareStatement(sqlSocio)) {
                    stmtSoc.setInt(1, emp.getId());
                    try (ResultSet rsSoc = stmtSoc.executeQuery()) {
                        while (rsSoc.next()) {
                            emp.getQsa().add(mapearSocio(rsSoc));
                        }
                    }
                }

                lista.add(emp);
            }
        }
        return lista;
    }

    // ------------------------------------------------------------------
    // DELETE
    // ------------------------------------------------------------------

    /**
     * Exclui uma empresa pelo CNPJ.
     * Os sócios são removidos automaticamente pela cláusula ON DELETE CASCADE.
     *
     * @return true se alguma linha foi afetada, false se o CNPJ não existia.
     */
    public boolean excluir(String cnpj) throws SQLException {
        final String sql = "DELETE FROM empresa WHERE cnpj = ?";

        try (Connection conn = DB.connection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cnpj);
            return stmt.executeUpdate() > 0;
        }
    }

    // ------------------------------------------------------------------
    // Helpers de mapeamento ResultSet → Objeto
    // ------------------------------------------------------------------

    private Empresa mapearEmpresa(ResultSet rs) throws SQLException {
        Empresa emp = new Empresa();
        emp.setId(rs.getInt("id"));
        emp.setCnpj(rs.getString("cnpj"));
        emp.setRazaoSocial(rs.getString("razao_social"));
        emp.setNomeFantasia(rs.getString("nome_fantasia"));
        emp.setLogradouro(rs.getString("logradouro"));
        return emp;
    }

    private Socio mapearSocio(ResultSet rs) throws SQLException {
        Socio s = new Socio();
        s.setId(rs.getInt("id"));
        s.setNomeSocio(rs.getString("nome_socio"));
        s.setCnpjCpfDoSocio(rs.getString("cnpj_cpf_do_socio"));
        s.setQualificacaoSocio(rs.getString("qualificacao_socio"));
        return s;
    }
}
