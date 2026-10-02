package br.umc.loginseguro.repository;

import br.umc.loginseguro.model.TokenRecuperacao;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/** Acesso à coleção "tokens_recuperacao". */
public interface TokenRecuperacaoRepository extends MongoRepository<TokenRecuperacao, String> {

    Optional<TokenRecuperacao> findByTokenHash(String tokenHash);

    void deleteByUsuarioId(String usuarioId);
}
