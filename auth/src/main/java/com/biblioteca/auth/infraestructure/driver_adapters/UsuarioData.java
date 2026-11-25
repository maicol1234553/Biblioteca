package com.biblioteca.auth.infraestructure.driver_adapters;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "usuario")
public class UsuarioData {

    @Id
    @Column(name = "id_usuario", length = 36, nullable = false)
    private String idUsuario;

    @Column(name = "nombre", length = 255, nullable = false)
    private String nombre;

    @Column(name = "correo", length = 255, nullable = false, unique = true)
    private String correo;

    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @Column(name = "rol", length = 255, nullable = false)
    private String rol;

    @Column(name = "edad", nullable = false)
    private Integer edad;

    @Column(name = "n_telefono", length = 10, nullable = false)
    private String nTelefono;
}
