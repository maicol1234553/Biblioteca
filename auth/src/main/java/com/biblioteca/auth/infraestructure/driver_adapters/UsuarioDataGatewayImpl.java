package com.biblioteca.auth.infraestructure.driver_adapters;

import com.biblioteca.auth.domain.model.EstadisticasUsuario;
import com.biblioteca.auth.domain.model.Usuario;
import com.biblioteca.auth.domain.model.gateway.UsuarioGateway;
import com.biblioteca.auth.infraestructure.mapper.UsuarioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.Map;

@RequiredArgsConstructor
@Repository
public class UsuarioDataGatewayImpl implements UsuarioGateway {

    private final UsuarioDataJpaRepository usuarioDataJpaRepository;
    private final UsuarioMapper usuarioMapper;

    @Override
    public Usuario guardarUsuario(Usuario usuario) {
        UsuarioData usuarioMapeado = usuarioMapper.toUsuarioData(usuario);

        return usuarioMapper.toUsuario(usuarioDataJpaRepository.save(usuarioMapeado));
    }

    @Override
    public Usuario buscarUsuarioPorId(String usuarioId) {
        return usuarioDataJpaRepository.findById(usuarioId)
                .map(usuarioMapper::toUsuario)
                .orElse(null);
    }

    @Override
    public Usuario buscarUsuarioPorCorreo(String correo) {
        return usuarioDataJpaRepository.findByCorreo(correo)
                .map(usuarioMapper::toUsuario)
                .orElse(null);
    }

    @Override
    public Usuario actualizarUsuario(Usuario usuario) {
        return usuarioMapper.toUsuario(
                usuarioDataJpaRepository.save(usuarioMapper.toUsuarioData(usuario)));
    }

    @Override
    public void eliminarUsuario(String usuarioId) {
        usuarioDataJpaRepository.deleteById(usuarioId);
    }

    @Override
    public EstadisticasUsuario obtenerEstadisticas() {
        Map<String, Long> usuariosPorRol = new LinkedHashMap<>();

        for (Object[] fila : usuarioDataJpaRepository.contarUsuariosPorRol()) {
            usuariosPorRol.put((String) fila[0], ((Number) fila[1]).longValue());
        }

        Double promedio = usuarioDataJpaRepository.promedioEdad();
        double edadPromedio = promedio == null ? 0.0 : Math.round(promedio * 100.0) / 100.0;

        return new EstadisticasUsuario((int) usuarioDataJpaRepository.count(), usuariosPorRol, edadPromedio);
    }
}
