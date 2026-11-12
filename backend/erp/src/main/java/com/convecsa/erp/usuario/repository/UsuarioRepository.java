package com.convecsa.erp.usuario.repository;

import com.convecsa.erp.usuario.domain.RolUsuario;
import com.convecsa.erp.usuario.entity.Usuario;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @Query("""
            SELECT u FROM Usuario u
            WHERE LOWER(u.username) = LOWER(:identificador)
               OR LOWER(u.correo) = LOWER(:identificador)
            """)
    Optional<Usuario> findByUsernameOrCorreoIgnoreCase(@Param("identificador") String identificador);

    @Query("""
            SELECT u FROM Usuario u
            WHERE u.activo = true
              AND u.rol NOT IN :gerencia
              AND (:q = ''
                   OR LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(u.correo) LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY u.username ASC
            """)
    List<Usuario> findAsignables(@Param("q") String q, @Param("gerencia") Collection<RolUsuario> gerencia);
}
