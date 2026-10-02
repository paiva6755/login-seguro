package br.umc.loginseguro.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

/**
 * Configuração central de segurança.
 *
 * - Autorização por URL: cada prefixo de rota exige um perfil.
 * - @EnableMethodSecurity: permite @PreAuthorize nos controllers (defesa extra).
 * - CSRF fica ATIVO (padrão): o Thymeleaf insere o token em todo th:action.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /** Rotas acessíveis sem login. */
    private static final String[] ROTAS_PUBLICAS = {
            "/", "/login", "/cadastro", "/esqueci-senha", "/redefinir-senha",
            "/tema", "/acesso-negado", "/error",
            "/css/**", "/themes/**", "/img/**", "/js/**", "/favicon.ico"
    };

    @Bean
    public SecurityFilterChain filtroSeguranca(HttpSecurity http) throws Exception {
        http
            // ---------- Quem pode acessar o quê ----------
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(ROTAS_PUBLICAS).permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/professor/**").hasAnyRole("PROFESSOR", "ADMIN")
                .requestMatchers("/aluno/**").hasAnyRole("ALUNO", "PROFESSOR", "ADMIN")
                .anyRequest().authenticated())

            // ---------- Login por formulário ----------
            .formLogin(form -> form
                .loginPage("/login")                 // nossa tela Thymeleaf
                .loginProcessingUrl("/login")        // POST tratado pelo Spring
                .usernameParameter("email")
                .passwordParameter("senha")
                .defaultSuccessUrl("/painel", true)  // redireciona conforme o perfil
                .failureUrl("/login?erro")
                .permitAll())

            // ---------- Logout (somente POST, com CSRF) ----------
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?saiu")
                .invalidateHttpSession(true)         // apaga a sessão no MongoDB
                .clearAuthentication(true)
                .deleteCookies("SESSION")            // cookie do Spring Session
                .permitAll())

            // ---------- Acesso negado (usuário logado sem o perfil certo) ----------
            .exceptionHandling(ex -> ex.accessDeniedPage("/acesso-negado"))

            // ---------- Sessão ----------
            // Troca o ID da sessão no login (evita "session fixation")
            .sessionManagement(sessao -> sessao.sessionFixation(f -> f.changeSessionId()))

            // ---------- Cabeçalhos de segurança ----------
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                    "default-src 'self'; img-src 'self' data:; style-src 'self'; "
                  + "script-src 'self'; form-action 'self'; frame-ancestors 'none'"))
                .frameOptions(frame -> frame.deny())
                .referrerPolicy(ref -> ref.policy(
                    ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN)));

        return http.build();
    }

    /**
     * BCrypt com custo 12: cada hash leva frações de segundo, o que torna
     * ataques de força bruta muito lentos. O "sal" é gerado automaticamente.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
