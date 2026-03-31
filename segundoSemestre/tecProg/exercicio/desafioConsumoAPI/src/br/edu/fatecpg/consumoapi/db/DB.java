package br.edu.fatecpg.consumoapi.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB {

    private static final String JDBC_URL = "jdbc:postgresql://localhost:5432/consumo_api";
    private static final String USER     = "postgres";
    private static final String PASSWORD = "12345";

    /**
     * Abre e retorna uma conexão com o banco de dados.
     * Lança SQLException para que o chamador possa tratá-la adequadamente,
     * em vez de engolir o erro e retornar null.
     */
    public static Connection connection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
    }
}
