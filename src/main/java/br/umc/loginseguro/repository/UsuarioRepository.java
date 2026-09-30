package br.umc.loginseguro.repository;

import br.umc.loginseguro.model.Role;
import br.umc.loginseguro.model.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * Acesso à coleção "usuarios". O Spring Data gera as consultas
 * automaticamente a partir do nome dos métodos.
 */
public interface UsuarioRepository extends MongoRepository<Usuario, String> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Usuario> findByPerfilOrderByNomeAsc(Role perfil);
}
