package br.umc.loginseguro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Ponto de entrada da aplicação.
 *
 * - @EnableMongoAuditing: preenche automaticamente campos @CreatedDate / @LastModifiedDate.
 * - @ConfigurationPropertiesScan: registra classes de configuração tipadas (ex.: TemaProperties).
 */
@SpringBootApplication
@EnableMongoAuditing
@ConfigurationPropertiesScan
public class LoginSeguroApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoginSeguroApplication.class, args);
    }
}
