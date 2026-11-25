package com.biblioteca.auth.infraestructure.mapper;

import com.biblioteca.auth.domain.model.Usuario;
import com.biblioteca.auth.infraestructure.driver_adapters.UsuarioData;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public Usuario toUsuario(UsuarioData usuarioData) {
        if (usuarioData == null) {
            return null;
        }

        return new Usuario(
                usuarioData.getIdUsuario(),
                usuarioData.getNombre(),
                usuarioData.getCorreo(),
                usuarioData.getPassword(),
                usuarioData.getRol(),
                usuarioData.getEdad(),
                usuarioData.getNTelefono()
        );
    }

    public UsuarioData toUsuarioData(Usuario usuario) {
        return new UsuarioData(
                usuario.getIdUsuario(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getPassword(),
                usuario.getRol(),
                usuario.getEdad(),
                usuario.getNTelefono()
        );
    }
}
