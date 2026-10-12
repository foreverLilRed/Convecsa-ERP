package com.convecsa.erp.usuario.repository;

import com.convecsa.erp.usuario.entity.Usuario;
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
}
