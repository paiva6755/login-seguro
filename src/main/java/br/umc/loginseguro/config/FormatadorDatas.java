package br.umc.loginseguro.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Formata datas para as telas no fuso configurado (app.fuso-horario).
 * Uso no Thymeleaf: ${@datas.formatar(registro.dataHora)}
 */
@Component("datas")
public class FormatadorDatas {

    private final DateTimeFormatter formato;

    public FormatadorDatas(@Value("${app.fuso-horario:America/Sao_Paulo}") String fuso) {
        this.formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(ZoneId.of(fuso));
    }

    public String formatar(Instant instante) {
        return instante == null ? "" : formato.format(instante);
    }
}
