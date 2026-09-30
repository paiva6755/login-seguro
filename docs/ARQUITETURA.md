# Arquitetura e decisões de design

## 1. Visão geral em camadas

```
Navegador ──► Spring Security (filtros) ──► Controller ──► Service ──► Repository ──► MongoDB Atlas
                   │                             │
                   │                             └──► Template Thymeleaf ◄── Tema (CSS)
                   └──► Sessão (Spring Session) ──────────────────────────────► coleção "sessoes"
```

| Camada | Responsabilidade | Não faz |
|---|---|---|
| `controller` | Receber requisição, chamar serviço, escolher a tela | Regras de negócio |
| `service` | Regras (hash, bloqueio, perfil padrão, restrições do admin) | Acesso HTTP ou HTML |
| `repository` | Consultas ao MongoDB | Regras |
| `security` | Tradução Usuario ⇄ Spring Security, eventos de login | Telas |
| `config` | Segurança, tema, admin inicial | Regras de domínio |
| `templates` + `static` | Apresentação | Lógica |

## 2. Fluxo de autenticação

1. `POST /login` com `email`, `senha` e token CSRF.
2. O Spring Security chama `UsuarioDetailsService`, que busca o usuário pelo e-mail normalizado.
3. Verifica conta ativa e não bloqueada; compara a senha com o hash via BCrypt.
4. Sucesso: novo ID de sessão (proteção contra *session fixation*), sessão gravada no MongoDB,
   `EventosAutenticacaoListener` zera tentativas e registra o último login. Redireciona para `/painel`,
   que envia cada perfil à sua página inicial (`Role.getPaginaInicial()`).
5. Falha: contador de tentativas incrementado; na 5ª, bloqueio de 15 minutos. A tela mostra
   mensagem genérica, sem revelar se o e-mail existe.

## 3. Modelo de dados

**Coleção `usuarios`**

| Campo | Tipo | Observação |
|---|---|---|
| `_id` | ObjectId | |
| `nome` | string | |
| `email` | string | índice único, sempre minúsculo |
| `senhaHash` | string | BCrypt |
| `perfil` | string | `ADMIN`, `PROFESSOR`, `ALUNO` |
| `ativo` | boolean | |
| `tentativasFalhas`, `bloqueadoAte` | int, date | proteção contra força bruta |
| `ultimoLogin`, `criadoEm`, `atualizadoEm` | date | auditoria |

**Coleção `sessoes`**: gerenciada pelo Spring Session (ID, atributos serializados, expiração).
Permite reiniciar a aplicação ou rodar várias instâncias sem perder sessões.

## 4. Decisões de segurança

| Decisão | Motivo |
|---|---|
| BCrypt custo 12 | Hash lento com sal automático, padrão recomendado pelo Spring Security |
| DTO `CadastroForm` separado da entidade | Impede envio de `perfil`/`ativo` pelo formulário |
| Perfil do cadastro fixo em ALUNO | Ninguém se autopromove a administrador |
| CSRF ativo, logout só via POST | Evita ações forjadas por outros sites |
| Autorização por URL + `@PreAuthorize` | Defesa em camadas |
| Cookie `HttpOnly`, `SameSite=Lax`, `Secure` configurável | Protege o cookie de sessão |
| Content-Security-Policy restritiva | Mitiga XSS; nenhum script ou estilo inline |
| Credenciais apenas em variáveis de ambiente / `.env` | Nada sensível no GitHub |
| Hash da senha removido da sessão após login | Reduz exposição de dados |

**Limitações conhecidas (evolução prevista):** a tela de cadastro informa quando um e-mail já
existe (usabilidade em troca de enumeração de contas); mudança de perfil vale no próximo login;
recuperação de senha por e-mail e verificação de e-mail estão fora do escopo.

## 5. Temas desacoplados

- `static/css/base.css`: somente estrutura, usando variáveis (`var(--cor-primaria)`, etc.).
- `static/themes/<nome>/theme.css`: somente valores dessas variáveis.
- `layouts/base.html`: carrega `base.css` + o tema ativo. Todas as páginas usam `layout:decorate`.
- `TemaService`: tema da sessão > tema padrão (`APP_TEMA`), aceitando só nomes da lista permitida.

Um tema novo exige um arquivo CSS e uma linha de configuração.

## 6. Guia de adaptação ao PFC

| Objetivo | Onde mexer |
|---|---|
| Renomear/criar perfis | `model/Role.java` e regras em `SecurityConfig` |
| Nova área protegida | Novo controller com `@RequestMapping` + regra em `SecurityConfig` |
| Novos dados do usuário | Campo em `Usuario` e, se vier do cadastro, em `CadastroForm` e `cadastro.html` |
| Identidade visual | Novo tema em `static/themes/` |
| Nome do sistema | Variável `APP_NOME` |
| Regras de senha | Anotações em `CadastroForm` |
| Tempo de bloqueio/tentativas | Constantes em `UsuarioService` |
