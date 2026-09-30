# Login Seguro: Spring Boot + Thymeleaf + MongoDB Atlas

Sistema de autenticação e autorização modular, desenvolvido como atividade da disciplina e
pensado para ser reaproveitado no PFC. Inclui cadastro, login, logout, controle de acesso por
perfil (3 perfis), sessões persistidas no MongoDB Atlas e temas visuais configuráveis.

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 17 |
| Framework | Spring Boot 3.5 (Web, Validation) |
| Segurança | Spring Security 6 (BCrypt, CSRF, controle por perfil) |
| Banco de dados | MongoDB Atlas (Spring Data MongoDB) |
| Sessões | Spring Session MongoDB (coleção `sessoes`) |
| Interface | Thymeleaf + Layout Dialect + Spring Security Extras |
| Build | Maven |

## Funcionalidades

- Cadastro com validação (nome, e-mail válido, senha forte, confirmação de senha, e-mail único).
- Senhas armazenadas apenas como hash **BCrypt** (custo 12).
- Login, logout (POST com CSRF) e sessão com expiração de 30 minutos.
- Bloqueio temporário de 15 minutos após 5 senhas erradas seguidas.
- Três perfis com acesso por rota: **ALUNO**, **PROFESSOR** e **ADMIN**.
- Área administrativa para trocar perfis e ativar/desativar contas.
- Administrador inicial criado automaticamente a partir de variáveis de ambiente.
- Temas visuais trocáveis (`padrao`, `escuro`, `aprendo`) sem alterar Java ou HTML.

## Perfis e rotas

| Rota | Visitante | Aluno | Professor | Admin |
|---|:-:|:-:|:-:|:-:|
| `/`, `/login`, `/cadastro` | ✔ | ✔ | ✔ | ✔ |
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

O banco `login_seguro` e as coleções `usuarios` e `sessoes` são criados automaticamente na
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
| `APP_TEMA` | não | Tema padrão (`padrao`, `escuro`, `aprendo`) |
| `COOKIE_SECURE` | não | `true` em produção com HTTPS |

As variáveis podem vir do `.env` (lido via `spring.config.import`) ou do sistema operacional.
O `.env` está no `.gitignore`: **nenhuma credencial é versionada**.

## Temas

1. Criar `src/main/resources/static/themes/<novo-tema>/theme.css` copiando um tema existente.
2. Alterar apenas os valores das variáveis CSS.
3. Adicionar o nome em `app.tema.disponiveis` no `application.properties`.

O usuário escolhe o tema pelo seletor no cabeçalho; a escolha fica na sessão.

## Documentação complementar

Decisões de arquitetura, fluxo de autenticação e guia de adaptação ao PFC:
[`docs/ARQUITETURA.md`](docs/ARQUITETURA.md).

## Problemas comuns

| Sintoma | Causa provável |
|---|---|
| `Could not resolve placeholder 'MONGODB_URI'` | `.env` ausente ou fora da raiz do projeto |
| Timeout ao conectar | IP não liberado em Network Access |
| `Authentication failed` (Mongo) | Usuário/senha do banco incorretos ou senha sem URL-encoding |
| Admin não criado | `ADMIN_SENHA` com menos de 8 caracteres ou e-mail já existente |
