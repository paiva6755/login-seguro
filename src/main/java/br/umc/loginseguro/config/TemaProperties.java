package br.umc.loginseguro.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

/**
 * Configuração de aparência lida do application.properties (prefixo app.tema).
 *
 * Para criar um tema novo: crie a pasta static/themes/<nome>/theme.css
 * e adicione <nome> em app.tema.disponiveis. Nenhuma classe Java muda.
 */
@ConfigurationProperties(prefix = "app.tema")
public record TemaProperties(
        @DefaultValue("padrao") String nome,
        @DefaultValue("Login Seguro") String nomeSistema,
        @DefaultValue("padrao") List<String> disponiveis) {
}
