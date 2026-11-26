# Despliegue en Render

## Llamadas entre servicios

| Origen | Destino | Variable |
|---|---|---|
| auth | notificacion | `NOTIFICACION_URL` |
| prestamos | auth | `AUTH_URL` |
| prestamos | catalogo | `CATALOGO_URL` |
| prestamos | notificacion | `NOTIFICACION_URL` |
| notificacion | auth | `AUTH_URL` |
| catalogo | ninguno | - |

Las URLs base no incluyen rutas ni puerto en Render.

## Valores para Render

| Servicio | Variable | Valor |
|---|---|---|
| auth | `NOTIFICACION_URL` | `https://biblioteca-notificacion-maicol.onrender.com` |
| auth | `INTERNAL_API_KEY` | el mismo valor en los tres servicios que se comunican |
| catalogo | - | sin URLs entre servicios |
| notificacion | `AUTH_URL` | `https://biblioteca-auth-maicol.onrender.com` |
| notificacion | `INTERNAL_API_KEY` | el mismo valor que auth y prestamos |
| notificacion | `BREVO_API_KEY` | `<TU_CLAVE_BREVO>` |
| notificacion | `MAIL_FROM` | correo verificado en Brevo |
| prestamos | `AUTH_URL` | `https://biblioteca-auth-maicol.onrender.com` |
| prestamos | `CATALOGO_URL` | `https://biblioteca-catalogo-maicol.onrender.com` |
| prestamos | `NOTIFICACION_URL` | `https://biblioteca-notificacion-maicol.onrender.com` |
| prestamos | `INTERNAL_API_KEY` | el mismo valor que auth y notificacion |

Variables compartidas del grupo `biblioteca-config`:

| Variable | Valor |
|---|---|
| `DATABASE_URL` | cadena pooled de Neon |
| `DATABASE_URL_UNPOOLED` | cadena directa de Neon |
| `CATALOGO_ADMIN_USUARIO_ID` | UUID del administrador usado por prestamos |
| `BREVO_API_KEY` | `<TU_CLAVE_BREVO>` |
| `MAIL_FROM` | correo verificado en Brevo |
| `INTERNAL_API_KEY` | valor largo y aleatorio, por ejemplo generado con `openssl rand -hex 32` |

Opcionales para todos los servicios HTTP:

| Variable | Valor por defecto |
|---|---|
| `CONNECT_TIMEOUT_SECONDS` | `10` |
| `READ_TIMEOUT_SECONDS` | `90` |

`PRESTAMOS_URL` existe en `.env.example` por consistencia, pero hoy ningun servicio llama a prestamos.

## Desarrollo local

Los valores por defecto ya apuntan a localhost:

| Variable | Valor local |
|---|---|
| `AUTH_URL` | `http://localhost:8081` |
| `CATALOGO_URL` | `http://localhost:8082` |
| `PRESTAMOS_URL` | `http://localhost:8083` |
| `NOTIFICACION_URL` | `http://localhost:8084` |

`iniciar-microservicios.ps1` carga `.env` y los procesos Java heredan esas variables. Si `INTERNAL_API_KEY` queda vacia en local, las rutas internas de notificacion no exigen el header; en Render debe configurarse siempre.

## Seguridad interna

Auth y prestamos envian el header `X-Internal-Api-Key` al llamar a notificacion. Notificacion lo exige cuando `INTERNAL_API_KEY` tiene valor. `/actuator/health` queda publico y no requiere el header.

## Cold start

Los servicios gratuitos de Render pueden tardar cerca de dos minutos en despertar. Los clientes usan 10 segundos de conexion y 90 segundos de lectura. Un fallo al notificar se registra como advertencia y no interrumpe el registro, prestamo, reserva o devolucion.
