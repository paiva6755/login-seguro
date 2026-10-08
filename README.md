# Login Seguro: Spring Boot + Thymeleaf + MongoDB Atlas

Sistema de autenticação e autorização modular, desenvolvido como atividade da disciplina e
pensado para ser reaproveitado em outros projetos. Inclui cadastro, login, logout, recuperação de
senha por e-mail, log de auditoria, controle de acesso por perfil (3 perfis), sessões persistidas
no MongoDB Atlas e temas visuais configuráveis.

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 17 |
| Framework | Spring Boot 3.5 (Web, Validation) |
| Segurança | Spring Security 6 (BCrypt, CSRF, controle por perfil) |
| Banco de dados | MongoDB Atlas (Spring Data MongoDB) |
| Sessões | Spring Session MongoDB (coleção `sessoes`) |
| Interface | Thymeleaf + Layout Dialect + Spring Security Extras |
| E-mail | Spring Mail (SMTP, ex.: Gmail) |
| Build | Maven |

## Funcionalidades

- Cadastro com validação (nome, e-mail válido, senha forte, confirmação de senha, e-mail único).
- Senhas armazenadas apenas como hash **BCrypt** (custo 12).
- Login, logout (POST com CSRF) e sessão com expiração de 30 minutos.
- Bloqueio temporário de 15 minutos após 5 senhas erradas seguidas.
- Três perfis com acesso por rota: **ALUNO**, **PROFESSOR** e **ADMIN**.
- Recuperação de senha por e-mail: link de uso único, válido por 30 minutos; o token é
  armazenado apenas como hash SHA-256 e as sessões abertas são encerradas após a troca.
- Log de auditoria (coleção `auditoria`): cadastro, login, falha de login, bloqueio, logout,
  acesso negado, alterações de perfil/status, recuperação e redefinição de senha, com data, IP e
  detalhes. Consulta com filtro em `/admin/auditoria`; retenção automática de 180 dias.
- Área administrativa para trocar perfis e ativar/desativar contas.
- Administrador inicial criado automaticamente a partir de variáveis de ambiente.
- Temas visuais trocáveis (`padrao`, `escuro`, `vibrante`) sem alterar Java ou HTML.

## Perfis e rotas

| Rota | Visitante | Aluno | Professor | Admin |
|---|:-:|:-:|:-:|:-:|
| `/`, `/login`, `/cadastro`, `/esqueci-senha`, `/redefinir-senha` | ✔ | ✔ | ✔ | ✔ |
| `/aluno/**` | | ✔ | ✔ | ✔ |
| `/professor/**` | | | ✔ | ✔ |
| `/admin/**` | | | | ✔ |

Todo cadastro público recebe o perfil **ALUNO**. Somente um administrador promove usuários.

## Estrutura do projeto

```
login-seguro/
├── pom.xml                       dependências (Maven)
├── .env.example                  modelo das variáveis de ambiente
├── docs/ARQUITETURA.md           decisões de design e guia de adaptação
└── src/main/
    ├── java/br/umc/loginseguro/
    │   ├── config/               segurança, tema, admin inicial
    │   ├── controller/           rotas HTTP (sem regra de negócio)
    │   ├── dto/                  dados de formulário + validação
    │   ├── exception/            erros de negócio
    │   ├── model/                Usuario (documento Mongo) e Role (perfis)
    │   ├── repository/           acesso ao MongoDB
    │   ├── security/             integração com Spring Security
    │   └── service/              regras de negócio
    └── resources/
        ├── application.properties
        ├── templates/            telas Thymeleaf (layout + fragmentos)
        └── static/
            ├── css/base.css      estrutura (não muda entre temas)
            └── themes/<tema>/    cores, fontes e formas de cada tema
```

## Pré-requisitos

- JDK 17 ou superior (`java -version`)
- Maven 3.9+ (`mvn -version`)
- Git
- Conta gratuita no [MongoDB Atlas](https://www.mongodb.com/cloud/atlas)

## Configuração do MongoDB Atlas

1. Criar conta no Atlas e um cluster gratuito (**M0**).
2. Em **Database Access**, criar um usuário de banco com senha (permissão *Read and write to any database*).
3. Em **Network Access**, adicionar o IP atual (**Add Current IP Address**).
   Para testes, `0.0.0.0/0` libera qualquer IP; não recomendado em produção.
4. Em **Database > Connect > Drivers**, copiar a *connection string*:
   `mongodb+srv://<usuario>:<senha>@cluster0.xxxxx.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0`
5. Substituir `<usuario>` e `<senha>`. Caracteres especiais na senha precisam de URL-encoding
   (ex.: `@` → `%40`, `#` → `%23`).

O banco `login_seguro` e as coleções `usuarios`, `sessoes`, `auditoria` e `tokens_recuperacao`
são criados automaticamente na
primeira execução. A conexão `mongodb+srv` usa TLS por padrão.

## Execução local

1. Clonar o repositório:
   ```bash
   git clone https://github.com/<seu-usuario>/login-seguro.git
   cd login-seguro
   ```
2. Criar o arquivo `.env` a partir do modelo e preencher os valores:
   ```bash
   copy .env.example .env      # Windows
   cp .env.example .env        # Linux/macOS
   ```
3. Executar:
   ```bash
   mvn spring-boot:run
   ```
4. Acessar `http://localhost:8080` e entrar com `ADMIN_EMAIL` / `ADMIN_SENHA`.

Testes unitários: `mvn test`.

### Variáveis de ambiente

| Variável | Obrigatória | Descrição |
|---|:-:|---|
| `MONGODB_URI` | sim | Connection string do Atlas |
| `MONGODB_DATABASE` | não | Nome do banco (padrão `login_seguro`) |
| `ADMIN_EMAIL` / `ADMIN_SENHA` | recomendado | Administrador criado na primeira execução |
| `APP_NOME` | não | Nome exibido no cabeçalho |
| `APP_TEMA` | não | Tema padrão (`padrao`, `escuro`, `vibrante`) |
| `COOKIE_SECURE` | não | `true` em produção com HTTPS |
| `MAIL_HOST` / `MAIL_PORT` | não | Servidor SMTP (ex.: `smtp.gmail.com` / `587`) |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | não | Conta e senha de app do e-mail remetente |
| `APP_URL` | não | Endereço do sistema usado no link do e-mail (padrão `http://localhost:8080`) |

As variáveis podem vir do `.env` (lido via `spring.config.import`) ou do sistema operacional.
O `.env` está no `.gitignore`: **nenhuma credencial é versionada**.

## Recuperação de senha por e-mail (Gmail)

1. Ativar a **verificação em duas etapas** na conta Google que enviará os e-mails.
2. Em `myaccount.google.com/apppasswords`, criar uma senha de app (ex.: "Login Seguro").
3. Preencher no `.env`: `MAIL_HOST=smtp.gmail.com`, `MAIL_PORT=587`, `MAIL_USERNAME` com o
   e-mail e `MAIL_PASSWORD` com a senha de app de 16 letras, **sem espaços**.
4. Reiniciar o sistema e usar **Esqueci minha senha** na tela de login.

**Modo de teste:** com `MAIL_HOST` vazio, nenhum e-mail é enviado e o link de redefinição
aparece no terminal. Não usar em produção.

## Temas

1. Criar `src/main/resources/static/themes/<novo-tema>/theme.css` copiando um tema existente.
2. Alterar apenas os valores das variáveis CSS.
3. Adicionar o nome em `app.tema.disponiveis` no `application.properties`.

O usuário escolhe o tema pelo seletor no cabeçalho; a escolha fica na sessão.

## Documentação complementar

Decisões de arquitetura, fluxo de autenticação e guia de adaptação a outros projetos:
[`docs/ARQUITETURA.md`](docs/ARQUITETURA.md).

## Problemas comuns

| Sintoma | Causa provável |
|---|---|
| `Could not resolve placeholder 'MONGODB_URI'` | `.env` ausente ou fora da raiz do projeto |
| Timeout ao conectar | IP não liberado em Network Access |
| `Authentication failed` (Mongo) | Usuário/senha do banco incorretos ou senha sem URL-encoding |
| Admin não criado | `ADMIN_SENHA` com menos de 8 caracteres ou e-mail já existente |
| `Authentication failed` / `535` no e-mail | Senha normal em vez de senha de app, ou senha de app com espaços |
| E-mail não chega | Verificar spam; conferir no terminal a linha `E-mail de redefinição enviado` |
