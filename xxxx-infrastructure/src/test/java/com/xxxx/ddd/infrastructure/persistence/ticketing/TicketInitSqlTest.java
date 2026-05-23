package com.xxxx.ddd.infrastructure.persistence.ticketing;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TicketInitSqlTest {

    @Test
    void initSqlCreatesAcceptedTablesAndSeedData() throws Exception {
        Path script = Path.of("..", "environment", "mysql", "init", "ticket_init.sql");

        try (Connection connection = DriverManager.getConnection(
                "jdbc:h2:mem:ticket_init;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE",
                "sa",
                ""
        )) {
            ScriptUtils.executeSqlScript(connection, new FileSystemResource(script));

            assertThat(countRows(connection, "users")).isEqualTo(2);
            assertThat(countRows(connection, "roles")).isEqualTo(2);
            assertThat(countRows(connection, "events")).isEqualTo(2);
            assertThat(countRows(connection, "ticket_types")).isEqualTo(3);
            assertThat(countRows(connection, "orders")).isZero();
            assertThat(countRows(connection, "order_items")).isZero();
            assertThat(countRows(connection, "payments")).isZero();

            assertThatThrownBy(() -> insertInvalidTicketType(connection))
                    .isInstanceOf(SQLException.class);
        }
    }

    private static int countRows(Connection connection, String tableName) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }

    private static void insertInvalidTicketType(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    INSERT INTO ticket_types (event_id, name, price, stock_initial, stock_available)
                    VALUES (999, 'Invalid', 100.00, 10, 10)
                    """);
        }
    }
}
