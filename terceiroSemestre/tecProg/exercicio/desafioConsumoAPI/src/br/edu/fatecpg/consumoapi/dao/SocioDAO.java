package br.edu.fatecpg.consumoapi.dao;

import br.edu.fatecpg.consumoapi.db.DB;
import br.edu.fatecpg.consumoapi.model.Socio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO responsável pelas operações de persistência da entidade Socio.
 */
public class SocioDAO {

    // ------------------------------------------------------------------
    // INSERT em lote (todos os sócios do QSA de uma vez)
    // ------------------------------------------------------------------

    /**
     * Insere toda a lista de sócios vinculados a uma empresa.
     * Usa a mesma Connection para aproveitar o mesmo contexto transacional.
     *
     * @param socios    Lista de sócios obtida do campo qsa da API.
     * @param empresaId ID gerado pelo banco após inserção da empresa.
     */
    public void inserirLote(List<Socio> socios, int empresaId, Connection conn)
            throws SQLException {

        if (socios == null || socios.isEmpty()) return;

        final String sql =
                "INSERT INTO socio (nome_socio, cnpj_cpf_do_socio, qualificacao_socio, empresa_id) " +
                "VALUES (?, ?, ?, ?) " +
                "ON CONFLICT (cnpj_cpf_do_socio, empresa_id) DO NOTHING"; // idempotente

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Socio s : socios) {
                stmt.setString(1, s.getNomeSocio());
                stmt.setString(2, s.getCnpjCpfDoSocio());
                stmt.setString(3, s.getQualificacaoSocio());
                stmt.setInt(4, empresaId);
                stmt.addBatch(); // acumula para executar de uma vez
            }
            stmt.executeBatch();
        }
    }
}
