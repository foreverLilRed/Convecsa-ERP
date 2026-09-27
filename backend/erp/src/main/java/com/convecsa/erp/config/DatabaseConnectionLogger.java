package com.convecsa.erp.config;

import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseConnectionLogger {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConnectionLogger.class);

    @Bean
    ApplicationRunner logDatabaseConnection(DataSource dataSource) {
        return args -> {
            try (var connection = dataSource.getConnection();
                    var statement = connection.createStatement();
                    var resultSet = statement.executeQuery("SELECT current_database(), current_user")) {
                resultSet.next();
                log.info(
                        "Conexión a PostgreSQL OK. Base: {}, usuario: {}",
                        resultSet.getString(1),
                        resultSet.getString(2));
            }
        };
    }
}
