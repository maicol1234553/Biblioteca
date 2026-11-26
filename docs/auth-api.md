# API del microservicio AUTH — Biblioteca

**Base URL (Render):** `https://biblioteca-auth-maicol.onrender.com`
**Health:** `GET /actuator/health`

## Autenticación

- **No hay JWT.** No existe dependencia `jjwt`/JOSE en el `pom.xml` ni generación de tokens en el código.
- `POST /api/biblioteca/usuario/login` devuelve un **texto plano** (`Logueo EXITOSO`), no un token.
- `SecurityConfig` de auth permite **público** todo `/api/**`; no se requiere cabecera `Authorization`, `X-Usuario-Id` ni token.
- El resto de rutas (fuera de `/api/**`) exigen autenticación de Spring Security, pero no se usan en esta API.

## Formato de respuestas

- Objetos y estadísticas: `application/json`.
- Respuestas `String` (login, errores de negocio): `text/plain`.
- **Importante:** `GlobalExceptionHandler` devuelve **HTTP 200** incluso para errores de negocio (correo duplicado, usuario no encontrado, token inválido, etc.). Solo se obtienen 404/405 de rutas o métodos inexistentes.
- El JSON de usuario **incluye el hash de la contraseña** (`password`). Es un riesgo de seguridad actual del código.

## Resumen de endpoints

| # | Método | Ruta | Función | Auth |
|---|---|---|---|---|
| 1 | POST | `/api/biblioteca/usuario/save` | Registrar usuario | Público |
| 2 | GET | `/api/biblioteca/usuario/id/{id}` | Buscar por ID | Público |
| 3 | GET | `/api/biblioteca/usuario/correo/{correo}` | Buscar por correo | Público |
| 4 | GET | `/api/biblioteca/usuario/existe/{correo}` | Verificar existencia | Público |
| 5 | PUT | `/api/biblioteca/usuario/update` | Actualizar usuario | Público |
| 6 | DELETE | `/api/biblioteca/usuario/{id}` | Eliminar usuario | Público |
| 7 | POST | `/api/biblioteca/usuario/login` | Iniciar sesión | Público |
| 8 | POST | `/api/biblioteca/usuario/cambiar-password` | Cambiar contraseña | Público |
| 9 | POST | `/api/biblioteca/usuario/recuperar-password` | Solicitar recuperación | Público |
| 10 | POST | `/api/biblioteca/usuario/restablecer-password` | Restablecer con token | Público |
| 11 | PUT | `/api/biblioteca/usuario/{id}/rol` | Cambiar rol | Público |
| 12 | GET | `/api/biblioteca/usuario/estadisticas` | Estadísticas agregadas | Público |
| 13 | GET | `/actuator/health` | Estado del servicio | Público |

**No implementado en el código:** listar todos los usuarios y cualquier operación de saldo/recarga. No existen rutas ni entidades para eso.

## Detalle de endpoints

### 1. Registrar usuario — `POST /api/biblioteca/usuario/save`

Body:

```json
{
  "nombre": "Usuario Postman",
  "correo": "usuario@biblioteca.local",
  "password": "Prueba1234",
  "rol": "USUARIO",
  "edad": 25,
  "nTelefono": "3001234567"
}
```

- `idUsuario` es opcional; si se omite se genera un UUID.
- `rol`: `ADMINISTRADOR` | `USUARIO` | `ADMIN` | `USER`; si es nulo se usa `USUARIO`.
- `edad`: 18–122. `nTelefono`: exactamente 10 dígitos. `password`: mínimo 8 caracteres.
- La contraseña se cifra con **BCrypt** (`$2a$...`, longitud 60).

Respuesta `200` (JSON):

```json
{
  "idUsuario": "uuid",
  "nombre": "Usuario Postman",
  "correo": "usuario@biblioteca.local",
  "password": "$2a$10$...",
  "rol": "USUARIO",
  "edad": 25,
  "nTelefono": "3001234567"
}
```

Errores (HTTP 200, texto): `El correo ya está registrado`, `Usuario menor de edad`, `La contraseña debe tener mínimo 8 caracteres`, `El número telefónico debe tener 10 dígitos`, `El correo no es válido`.

### 2. Buscar por ID — `GET /api/biblioteca/usuario/id/{id}`

- Éxito `200` JSON del usuario.
- No existe: `200` + `Usuario no encontrado con id: {id}`.

### 3. Buscar por correo — `GET /api/biblioteca/usuario/correo/{correo}`

- Éxito `200` JSON del usuario.
- No existe: `200` + `Usuerio no encontrado con correo: {correo}` (texto tal cual está en el código).

### 4. Existe por correo — `GET /api/biblioteca/usuario/existe/{correo}`

- `200` con `true` o `false` (JSON boolean).

### 5. Actualizar usuario — `PUT /api/biblioteca/usuario/update`

Body (todos los campos):

```json
{
  "idUsuario": "uuid-existente",
  "nombre": "Nombre actualizado",
  "correo": "usuario@biblioteca.local",
  "password": "Prueba1234",
  "rol": "USUARIO",
  "edad": 26,
  "nTelefono": "3007654321"
}
```

- Si `rol` es nulo/vacío conserva el rol existente.
- Valida todos los campos como en el registro.
- Si el `password` no empieza por `$2`, se vuelve a cifrar con BCrypt.
- Éxito `200` JSON actualizado; id inexistente `200` texto.

### 6. Eliminar usuario — `DELETE /api/biblioteca/usuario/{id}`

- Éxito `200` texto: `Usuario eliminado con éxito`.
- No existe `200` + `Usuario no encontrado con id: ...`.
- **Nota:** no borra en cascada `password_reset_token` (no hay FK); quedan tokens huérfanos si se generaron.

### 7. Login — `POST /api/biblioteca/usuario/login`

Body:

```json
{ "correo": "usuario@biblioteca.local", "password": "Prueba1234" }
```

Respuestas (siempre `200`, texto plano):

- `Logueo EXITOSO`
- `Credenciales invalidas`
- `Usuario no encontrado`

**No devuelve JWT ni cookie de sesión.**

### 8. Cambiar contraseña — `POST /api/biblioteca/usuario/cambiar-password`

```json
{
  "correo": "usuario@biblioteca.local",
  "passwordActual": "Prueba1234",
  "passwordNueva": "NuevaClave99"
}
```

- Éxito `200`: `Contraseña actualizada correctamente`.
- Errores `200`: `La contraseña actual no es correcta`, `La contraseña debe tener mínimo 8 caracteres`, `Usuario no encontrado con correo: ...`.

### 9. Recuperar contraseña — `POST /api/biblioteca/usuario/recuperar-password`

```json
{ "correo": "usuario@biblioteca.local" }
```

- Respuesta `200`: `Si el correo está registrado, enviamos instrucciones de recuperación`.
- Genera un token aleatorio, guarda su **hash SHA-256** en la tabla `password_reset_token` (vigencia 30 min) y solicita al microservicio de notificación el envío del código por **correo electrónico**.
- La API **no devuelve el token**: llega al correo registrado del usuario.

### 10. Restablecer contraseña — `POST /api/biblioteca/usuario/restablecer-password`

```json
{ "token": "token-real", "passwordNueva": "ResetClave123" }
```

- Éxito `200`: `Contraseña actualizada correctamente`; el token queda `usado=true`.
- Errores `200`: `Token inválido o ya utilizado`, `El token expiró`, `La contraseña debe tener mínimo 8 caracteres`.
- El token se recibe por correo tras ejecutar el endpoint 9. En desarrollo local se puede ver en MailHog (`http://localhost:8025`).

### 11. Cambiar rol — `PUT /api/biblioteca/usuario/{id}/rol`

```json
{ "rol": "ADMIN" }
```

- Éxito `200` JSON con `"rol": "ADMINISTRADOR"`.
- Rol inválido: `200` + `El rol debe ser ADMINISTRADOR o USUARIO`.

### 12. Estadísticas — `GET /api/biblioteca/usuario/estadisticas`

```json
{
  "totalUsuarios": 3,
  "usuariosPorRol": { "ADMINISTRADOR": 2, "USUARIO": 1 },
  "edadPromedio": 26.0
}
```

### 13. Health — `GET /actuator/health`

```json
{ "groups": ["liveness", "readiness"], "status": "UP" }
```

## Verificación real contra Render y Neon

Fecha de pruebas: 2026-11-24 (UTC-5). Resultados comprobados:

| Prueba | Resultado |
|---|---|
| `GET /actuator/health` | `200` `status=UP` |
| Registro de usuario | `200`, UUID generado, `password` con prefijo `$2a$` y longitud 60 |
| Consulta en Neon (`select ... from usuario where correo=?`) | Fila existente con el mismo id, rol y teléfono |
| `GET` por ID y por correo | `200` JSON correcto |
| `GET` existe | `true` |
| Login correcto / incorrecto / inexistente | `Logueo EXITOSO` / `Credenciales invalidas` / `Usuario no encontrado` |
| Actualización | `200` JSON actualizado y persistido |
| Cambio de rol | `200`, rol `ADMINISTRADOR` |
| Cambio de contraseña | `200`; login anterior falla y el nuevo funciona |
| Recuperación | `200`; se creó 1 fila en `password_reset_token` |
| Restablecer con token válido | `200`; token queda `usado=true`; login con la nueva clave funciona |
| Restablecer con token inválido | `200` `Token inválido o ya utilizado` |
| Eliminación | `200`; la fila desaparece de Neon |

**Nota de zona horaria:** Render ejecuta en UTC; los tokens se generan y expiran en UTC. La vigencia es de 30 minutos.

## Observaciones (no bloqueantes)

1. `GlobalExceptionHandler` responde **200** en errores de negocio; para clientes es mejor usar 400/404/409. Actualmente Postman debe validar el **cuerpo**, no solo el status.
2. Las respuestas incluyen el **hash BCrypt** de la contraseña; debería excluirse del JSON.
3. No hay JWT ni sesión; el login no sirve para autorizar otras APIs. Los demás microservicios validan identidad por `X-Usuario-Id` contra auth, no por token.
4. El correo de recuperación se envía a través del microservicio de notificación. En Render Free hay que configurar `SENDGRID_API_KEY` o `BREVO_API_KEY` (Render bloquea los puertos SMTP); en local se puede usar MailHog por SMTP.
5. `auth/src/main/resources/application.properties` tiene un typo en la primera línea (`quieor spring.application.name=auth`), por lo que el nombre de la aplicación no se aplica. No afecta los endpoints.
6. Eliminar un usuario no limpia sus tokens de recuperación (no hay FK).

## Postman

Archivos incluidos:

- `postman/Biblioteca-Auth.postman_collection.json`
- `postman/Biblioteca-Auth-Render.postman_environment.json`

Pasos:

1. Postman → **Import** → selecciona ambos archivos.
2. Selecciona el environment **Biblioteca Auth - Render**.
3. Ejecuta las carpetas en orden: `01 - Salud`, `02 - Registro y consultas`, `03 - Actualizacion`, `04 - Login`, `05 - Contrasena`, `06 - Eliminacion`, `07 - Casos de error`.
4. El request **Registrar usuario** genera un correo único y guarda `idUsuario` automáticamente.
5. Para **Restablecer contrasena**, copia el token desde los logs de Render y pégalo en la variable `tokenRecuperacion`.
