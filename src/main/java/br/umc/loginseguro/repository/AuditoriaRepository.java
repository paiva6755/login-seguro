package br.umc.loginseguro.repository;

import br.umc.loginseguro.model.RegistroAuditoria;
import br.umc.loginseguro.model.TipoEvento;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/** Acesso à coleção "auditoria". */
public interface AuditoriaRepository extends MongoRepository<RegistroAuditoria, String> {

    List<RegistroAuditoria> findTop200ByOrderByDataHoraDesc();

    List<RegistroAuditoria> findTop200ByTipoOrderByDataHoraDesc(TipoEvento tipo);
}
