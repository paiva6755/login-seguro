package br.umc.loginseguro.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Documento da coleção "auditoria": quem fez o quê, quando e de onde.
 *
 * Os registros são apagados automaticamente pelo MongoDB após 180 dias
 * (índice TTL em dataHora), limitando a retenção de dados pessoais (LGPD).
 */
@Document(collection = "auditoria")
public class RegistroAuditoria {

    /** 180 dias em segundos. */
    public static final int RETENCAO_SEGUNDOS = 180 * 24 * 60 * 60;

    @Id
    private String id;

    @Indexed(expireAfterSeconds = RETENCAO_SEGUNDOS)
    private Instant dataHora;

    @Indexed
    private TipoEvento tipo;

    /** Quem executou a ação (e-mail do usuário, ou o e-mail digitado no login). */
    private String usuario;

    /** Conta afetada pela ação (pode ser a mesma do usuário). */
    private String alvo;

    private String ip;

    private String detalhes;

    public RegistroAuditoria() {
    }

    public RegistroAuditoria(TipoEvento tipo, String usuario, String alvo, String ip, String detalhes) {
        this.dataHora = Instant.now();
        this.tipo = tipo;
        this.usuario = usuario;
        this.alvo = alvo;
        this.ip = ip;
        this.detalhes = detalhes;
    }

    public String getId() { return id; }
    public Instant getDataHora() { return dataHora; }
    public TipoEvento getTipo() { return tipo; }
    public String getUsuario() { return usuario; }
    public String getAlvo() { return alvo; }
    public String getIp() { return ip; }
    public String getDetalhes() { return detalhes; }
}
