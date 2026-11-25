# Microservicio de Notificacion

Microservicio de notificaciones, plantillas HTML, recordatorios y reintentos
para el Sistema de Biblioteca. Sigue la arquitectura hexagonal, convenciones y
paquetes de `auth`, `catalogo` y `prestamos`, y comparte la misma base de datos
Neon.

## Arquitectura

```
com.biblioteca.notificacion
├── aplication                 Beans de casos de uso y politica
├── domain
│   ├── exception              Excepciones de negocio
│   ├── model                  Entidades, enums, plantillas y utilidades HTML
│   ├── model/gateway          Puertos de persistencia, Auth y correo
│   └── usecase                Reglas de negocio y reintentos
└── infraestructure
    ├── config                 DataSource Neon, RestClient, propiedades
    ├── driver_adapters        Entidades JPA, repositorios y adaptadores
    ├── entry_points           Controllers, DTOs y manejo global de errores
    ├── external               Cliente RestClient de Auth
    ├── mail                   Adaptador SMTP con JavaMailSender
    ├── mapper                 Mapeo dominio <-> entidad JPA
    ├── scheduler              Proceso periodico de recordatorios y reintentos
    └── security               Configuracion HTTP interna
```

El dominio no depende de Spring ni de JPA. El envio SMTP se implementa mediante
el puerto `CorreoGateway` y el adaptador `CorreoGatewayImpl`.

## Entidades

| Entidad | Campos |
| --- | --- |
| `Notificacion` | `idNotificacion`, `idUsuario`, `destinatario`, `asunto`, `mensaje`, `tipo`, `fechaCreacion`, `fechaEnvio`, `estado` |
| `PlantillaNotificacion` | `idPlantilla`, `nombre`, `asunto`, `contenido`, `tipo`, `estado` |
| `HistorialEnvio` | `idHistorial`, `idNotificacion`, `fechaIntento`, `resultado`, `mensajeError` |
| `Recordatorio` | `idRecordatorio`, `idUsuario`, `idPrestamo`, `fechaProgramada`, `tipo`, `estado` |

Relaciones internas:

- `historial_envio.id_notificacion -> notificacion.id_notificacion` (1:N).
- `idUsuario` e `idPrestamo` son referencias externas a `auth` y `prestamos`.
  No existen relaciones JPA ni llaves foraneas hacia tablas de otros
  microservicios.

## APIs

Todos los endpoints estan disponibles bajo:

- `/notificaciones/...`
- `/api/biblioteca/notificaciones/...`
- `/api/notificaciones/...` (compatibilidad con el contrato de `prestamos`)

### Notificaciones

| Metodo | Ruta | Descripcion |
| --- | --- | --- |
| POST | `/notificaciones/enviar-correo` | Enviar un correo HTML generico |
| POST | `/notificaciones/recordatorio-devolucion` | Programar recordatorio de devolucion |
| POST | `/notificaciones/prestamo-confirmado` | Enviar confirmacion de prestamo |
| POST | `/notificaciones/libro-disponible` | Avisar que un libro reservado esta disponible |
| GET | `/notificaciones/historial-usuario/{idUsuario}` | Historial de notificaciones del usuario |
| POST | `/notificaciones/aviso-vencido` | Enviar aviso de prestamo vencido |
| POST | `/notificaciones/evento` | Recibir eventos de otros microservicios |
| POST | `/api/notificaciones/evento` | Contrato usado actualmente por `prestamos` |
| POST | `/notificaciones/reintentar/{id}` | Reintentar una notificacion fallida |
| POST | `/notificaciones/reintentar-fallidas` | Reintentar todas las fallidas disponibles |
| GET | `/notificaciones/pendientes` | Notificaciones pendientes o fallidas |
| GET | `/notificaciones` | Listar notificaciones |
| GET | `/notificaciones/{id}` | Consultar una notificacion |
| GET | `/notificaciones/{id}/historial` | Intentos de envio de una notificacion |
| DELETE | `/notificaciones/{id}` | Eliminar notificacion e historial |

### Recordatorios

| Metodo | Ruta | Descripcion |
| --- | --- | --- |
| GET | `/notificaciones/recordatorios` | Listar recordatorios |
| GET | `/notificaciones/recordatorios/{id}` | Consultar recordatorio |
| GET | `/notificaciones/recordatorios/usuario/{idUsuario}` | Recordatorios del usuario |
| GET | `/notificaciones/recordatorios/pendientes` | Recordatorios pendientes |
| PUT | `/notificaciones/recordatorios/{id}/cancelar` | Cancelar recordatorio pendiente |
| POST | `/notificaciones/recordatorios/procesar` | Procesar recordatorios vencidos ahora |
| DELETE | `/notificaciones/recordatorios/{id}` | Eliminar recordatorio no pendiente |

### Plantillas

| Metodo | Ruta | Descripcion |
| --- | --- | --- |
| GET | `/notificaciones/plantillas` | Listar plantillas |
| GET | `/notificaciones/plantillas/{id}` | Consultar plantilla |
| POST | `/notificaciones/plantillas` | Crear plantilla HTML |
| PUT | `/notificaciones/plantillas/{id}` | Actualizar plantilla |
| DELETE | `/notificaciones/plantillas/{id}` | Eliminar plantilla |

## Integracion con otros microservicios

### Auth

Cuando una solicitud no incluye `destinatario`, Notificacion consulta:

```
GET {AUTH_URL}/api/biblioteca/usuario/id/{idUsuario}
```

y usa `correo` y `nombre` del usuario para resolver el envio y las variables de
plantilla. Si Auth no esta disponible, la notificacion queda `FALLIDA` y se
registra el intento.

### Prestamos

El contrato ya implementado por `prestamos` es:

```http
POST /api/notificaciones/evento
Content-Type: application/json

{
  "tipo": "PRESTAMO_REGISTRADO",
  "idUsuario": "uuid",
  "referenciaId": "uuid-prestamo",
  "mensaje": "Prestamo registrado...",
  "fecha": "2026-01-15T10:00:00"
}
```

Tipos soportados: `PRESTAMO_REGISTRADO`, `PRESTAMO_CONFIRMADO`,
`DEVOLUCION_REGISTRADA`, `DEVOLUCION_PROXIMA`, `PRESTAMO_VENCIDO`,
`RESERVA_REGISTRADA`, `RESERVA_EXPIRADA`, `RENOVACION_REGISTRADA`,
`MULTA_GENERADA`, `LIBRO_DISPONIBLE`, `RECUPERACION_PASSWORD` y `GENERICA`.
Los tipos desconocidos se tratan como `GENERICA`.

`prestamos` no necesita cambios: su `NotificacionGatewayImpl` ya apunta a este
endpoint.

### Catalogo

Catalogo puede notificar disponibilidad con:

```http
POST /notificaciones/libro-disponible
Content-Type: application/json

{
  "idUsuario": "uuid",
  "idLibro": "uuid",
  "mensaje": "El libro que reservaste ya esta disponible"
}
```

### Recuperacion de contrasena desde Auth

Auth puede usar el endpoint generico:

```http
POST /notificaciones/enviar-correo
Content-Type: application/json

{
  "idUsuario": "uuid",
  "destinatario": "usuario@correo.com",
  "asunto": "Recuperacion de contrasena",
  "mensaje": "Tu codigo de recuperacion es...",
  "tipo": "RECUPERACION_PASSWORD"
}
```

> No se modifico Auth. Actualmente Auth registra la recuperacion por consola;
> para usar este servicio solo debe invocar el endpoint anterior.

## Plantillas HTML

Las plantillas se guardan en `plantilla_notificacion` y se aplican cuando estan
`ACTIVA` para el tipo de notificacion. Variables disponibles:

- `{{nombre}}`
- `{{idUsuario}}`
- `{{mensaje}}`
- `{{fecha}}`

Ejemplo:

```json
{
  "nombre": "Libro disponible",
  "asunto": "Libro disponible para {{nombre}}",
  "contenido": "<html><body><h2>Hola {{nombre}}</h2><p>{{mensaje}}</p></body></html>",
  "tipo": "LIBRO_DISPONIBLE",
  "estado": "ACTIVA"
}
```

Si no existe una plantilla activa, se genera un HTML por defecto con el mismo
estilo visual. Los valores interpolados se escapan para evitar inyeccion HTML.

## Correo SMTP

Configuracion por variables de entorno (sin credenciales en el codigo):

```properties
MAIL_HOST=
MAIL_PORT=
MAIL_USERNAME=
MAIL_PASSWORD=
MAIL_FROM=
MAIL_SMTP_AUTH=false
MAIL_SMTP_STARTTLS=false
```

`application.properties` usa valores por defecto para desarrollo local
(`localhost:1025`). Para un proveedor real configure, por ejemplo:

```powershell
$env:MAIL_HOST="smtp.tu-proveedor.com"
$env:MAIL_PORT="587"
$env:MAIL_USERNAME="usuario"
$env:MAIL_PASSWORD="password"
$env:MAIL_FROM="no-reply@tudominio.com"
$env:MAIL_SMTP_AUTH="true"
$env:MAIL_SMTP_STARTTLS="true"
```

El adaptador:

- Valida el destinatario con `InternetAddress` estricto.
- Envia HTML con `MimeMessageHelper.setText(..., true)`.
- Registra cada intento en `historial_envio`.
- Convierte errores SMTP en `EnvioCorreoException` (HTTP 502).
- No almacena credenciales.

## Reintentos y recordatorios

- Cada envio fallido deja la notificacion en `FALLIDA` y un historial `FALLIDO`.
- `POST /notificaciones/reintentar/{id}` reintenta manualmente.
- `POST /notificaciones/reintentar-fallidas` reintenta el lote.
- El maximo de intentos se configura con `notificaciones.reintentos.maximos`
  (3 por defecto). Al alcanzarlo se responde `409`.
- `NotificacionScheduler` procesa recordatorios vencidos y reintentos cada
  `notificaciones.job.intervalo-ms` (60 s por defecto).
- Un recordatorio se marca `PROCESADO` aunque falle el correo, para no enviar
  dos veces el mismo recordatorio. La notificacion queda `FALLIDA` y entra al
  flujo de reintentos.
- Un recordatorio pendiente duplicado para el mismo usuario, prestamo y tipo se
  rechaza con `409`.

## Base de datos

Se utiliza la misma base Neon de los otros servicios. Las credenciales se leen
de `.env.local` o de `DATABASE_URL_UNPOOLED`.

Hibernate usa `spring.jpa.hibernate.ddl-auto=update`: las tablas se crean y
actualizan desde las entidades JPA de `infraestructure/driver_adapters`. No se
usan scripts SQL.

Tablas: `notificacion`, `plantilla_notificacion`, `historial_envio`,
`recordatorio`. La unica FK interna es
`historial_envio.id_notificacion -> notificacion.id_notificacion`.

## Seguridad

Notificacion es un servicio interno invocado por Auth, Catalogo y Prestamos.
Los endpoints estan permitidos sin `X-Usuario-Id` para no romper esas
integraciones, que no envian ese encabezado. En un despliegue real debe
protegerse en la red o en el API Gateway (mTLS, API key o allowlist de
servicios).

## Pruebas

```bash
cd notificacion
./mvnw verify
```

Resultado verificado:

- **58 pruebas** JUnit 5 + Mockito, sin fallos.
- JaCoCo con minimo **90 %** sobre `domain/usecase`:
  - `NotificacionUseCase`: 98.8 % de lineas.
  - `RecordatorioUseCase`: 100 %.
  - `PlantillaUseCase`: 100 %.
- Prueba E2E real con:
  - Auth en `8081` para resolver el correo del usuario.
  - SMTP local real en `1025` para verificar el protocolo, HTML, remitente y destinatario.
  - 9 mensajes SMTP recibidos.
  - Recordatorio programado, duplicado rechazado, procesado y cancelado.
  - Fallo SMTP `502`, notificacion `FALLIDA`, historial `FALLIDO` y reintento
    exitoso con dos intentos registrados.
  - Destinatario invalido `400`, recurso inexistente `404`, duplicado `409`.
  - Contrato `/api/notificaciones/evento` compatible con `prestamos`.
  - Plantilla creada, renderizada, actualizada y eliminada.

No habia credenciales SMTP externas configuradas en el entorno, por lo que el
envio se verifico contra un servidor SMTP local real. Con `MAIL_HOST`,
`MAIL_USERNAME`, `MAIL_PASSWORD` y `MAIL_FROM` configurados, el mismo adaptador
envia por el proveedor externo.

## Ejecutar

```powershell
cd C:\BIBLIOTECA\notificacion
.\mvnw.cmd spring-boot:run
```

Servicio en `http://localhost:8084`.

Requisitos:

1. `.env.local` con `DATABASE_URL_UNPOOLED` (o variable de entorno).
2. Variables SMTP si se desea envio real.
3. Auth en `http://localhost:8081` para resolver destinatarios cuando no se
   envie `destinatario` en la solicitud.

Los archivos `.env` y `.env.local` estan incluidos en `.gitignore`.
