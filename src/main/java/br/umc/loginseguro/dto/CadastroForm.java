package br.umc.loginseguro.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Dados enviados pela tela de cadastro.
 *
 * Usar um DTO (em vez da entidade Usuario) evita que o usuário envie campos
 * que não deveria controlar, como "perfil" ou "ativo" (mass assignment).
 */
public class CadastroForm {

    @NotBlank(message = "Informe seu nome.")
    @Size(min = 3, max = 80, message = "O nome deve ter entre 3 e 80 caracteres.")
    private String nome;

    @NotBlank(message = "Informe seu e-mail.")
    @Email(message = "Informe um e-mail válido.")
    @Size(max = 120, message = "O e-mail deve ter no máximo 120 caracteres.")
    private String email;

    @NotBlank(message = "Crie uma senha.")
    @Size(min = RegrasSenha.MINIMO, max = RegrasSenha.MAXIMO, message = RegrasSenha.MENSAGEM_TAMANHO)
    @Pattern(regexp = RegrasSenha.PADRAO, message = RegrasSenha.MENSAGEM_PADRAO)
    private String senha;

    @NotBlank(message = "Repita a senha.")
    private String confirmacaoSenha;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getConfirmacaoSenha() { return confirmacaoSenha; }
    public void setConfirmacaoSenha(String confirmacaoSenha) { this.confirmacaoSenha = confirmacaoSenha; }
}
