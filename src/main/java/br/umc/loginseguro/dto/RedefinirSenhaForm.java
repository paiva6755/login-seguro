package br.umc.loginseguro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Dados da tela "Criar nova senha" (acessada pelo link do e-mail). */
public class RedefinirSenhaForm {

    /** Token recebido no link; vai num campo oculto do formulário. */
    @NotBlank
    private String token;

    @NotBlank(message = "Crie uma senha.")
    @Size(min = RegrasSenha.MINIMO, max = RegrasSenha.MAXIMO, message = RegrasSenha.MENSAGEM_TAMANHO)
    @Pattern(regexp = RegrasSenha.PADRAO, message = RegrasSenha.MENSAGEM_PADRAO)
    private String senha;

    @NotBlank(message = "Repita a senha.")
    private String confirmacaoSenha;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getConfirmacaoSenha() { return confirmacaoSenha; }
    public void setConfirmacaoSenha(String confirmacaoSenha) { this.confirmacaoSenha = confirmacaoSenha; }
}
