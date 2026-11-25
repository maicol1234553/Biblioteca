# Microservicio de Prestamos

Microservicio de prestamos, reservas, devoluciones, renovaciones y multas para
el Sistema de Biblioteca. Sigue la misma arquitectura hexagonal, convenciones y
organizacion de paquetes de `auth` y `catalogo`, y comparte la misma base de
datos Neon.

## Arquitectura

```
com.biblioteca.prestamos
├── aplication                 Beans de casos de uso y politica de negocio
├── domain
│   ├── exception              Excepciones de negocio
│   ├── model                  Entidades, enums, value objects y politica
│   ├── model/gateway          Puertos de persistencia e integracion
│   └── usecase                Casos de uso y reglas de negocio
└── infraestructure
    ├── config                 DataSource Neon, RestClient, propiedades
    ├── driver_adapters        Entidades JPA, repositorios y adaptadores
    ├── entry_points           Controllers REST + DTOs + manejador global
    ├── external               Clientes RestClient de Auth, Catalogo y Notificacion
    ├── mapper                 Mapeo dominio <-> entidad JPA
    ├── scheduler              Job de vencimientos y expiracion de reservas
    └── security               Autenticacion contra Auth y roles
```

El dominio no depende de Spring ni de JPA. Las transiciones de estado y las
reglas se concentran en `domain/usecase`.

## Entidades y relaciones

| Entidad | Campos principales |
| --- | --- |
| `Prestamo` | `idPrestamo`, `idUsuario`, `idEjemplar`, `fechaPrestamo`, `fechaVencimiento`, `fechaDevolucion`, `estado` |
| `Reserva` | `idReserva`, `idUsuario`, `idLibro`, `fechaReserva`, `fechaExpiracion`, `estado` |
| `Multa` | `idMulta`, `idPrestamo`, `valor`, `motivo`, `estado`, `fechaGeneracion` |
| `Devolucion` | `idDevolucion`, `idPrestamo`, `fechaDevolucion`, `observaciones`, `estadoEjemplar` |
| `Renovacion` | `idRenovacion`, `idPrestamo`, `fechaSolicitud`, `fechaAnterior`, `fechaNueva`, `estado` |

Relaciones internas con llaves foraneas:

- `devolucion.id_prestamo -> prestamo.id_prestamo` (unica, 1:1).
- `multa.id_prestamo -> prestamo.id_prestamo` (1:N).
- `renovacion.id_prestamo -> prestamo.id_prestamo` (1:N).
- Un usuario puede tener varios prestamos y reservas: `id_usuario` es una
  referencia externa.
- Un libro puede recibir varias reservas: `id_libro` es una referencia externa.

`id_usuario`, `id_libro` e `id_ejemplar` pertenecen a `auth` y `catalogo`.
**No se crean relaciones JPA ni llaves foraneas hacia tablas de otros
microservicios**, solo se almacenan sus UUID.

## Reglas de negocio

- Limite inicial de **3 prestamos activos** por usuario.
- Plazo de prestamo de **15 dias**.
- Reserva vigente por **3 dias**.
- Maximo de **2 renovaciones** por prestamo.
- Multa de **2000 por dia** de retraso (cada dia iniciado cuenta).
- No se presta un ejemplar que no este `DISPONIBLE` en Catalogo.
- No se presta un ejemplar que ya tenga un prestamo activo local.
- No se devuelve dos veces el mismo prestamo.
- No se permiten reservas pendientes duplicadas del mismo libro y usuario.
- Al registrar un prestamo, si el usuario tiene una reserva pendiente del mismo
  libro, la reserva pasa a `CONVERTIDA`.
- No se renueva un prestamo si existen reservas pendientes del libro.
- No se renueva un prestamo vencido ni se supera el maximo de renovaciones.
- Un usuario existente y con rol valido en Auth queda autorizado para operar.
- Solo el propietario o un administrador puede consultar un prestamo.
- La administracion global y las mutaciones de catalogo requieren
  `ADMINISTRADOR`.

Los valores anteriores estan centralizados en `application.properties` y se
mapean a `PoliticaPrestamos`:

```properties
prestamos.limite-prestamos-activos=3
prestamos.dias-prestamo=15
prestamos.dias-reserva=3
prestamos.max-renovaciones=2
prestamos.multa-por-dia=2000.00
```

## Comunicacion entre microservicios

- **Auth:** `GET /api/biblioteca/usuario/id/{id}` para validar que el usuario
  existe y obtener su rol. El filtro de seguridad usa Auth en cada peticion
  que incluya `X-Usuario-Id`.
- **Catalogo:** consulta de ejemplar, disponibilidad de libro y actualizacion
  del estado del ejemplar (`PRESTADO`, `DISPONIBLE`, `MANTENIMIENTO`,
  `PERDIDO`).
- **Notificacion:** `POST {notificacion-url}{notificacion-path}` con:

```json
{
  "tipo": "PRESTAMO_REGISTRADO",
  "idUsuario": "uuid",
  "referenciaId": "uuid-prestamo",
  "mensaje": "Prestamo registrado...",
  "fecha": "2026-01-15T10:00:00"
}
```

El envio de notificaciones no bloquea la operacion: si el servicio no esta
disponible, el adaptador registra el error y continua.

Variables relevantes:

```properties
prestamos.servicios.auth-url=http://localhost:8081
prestamos.servicios.catalogo-url=http://localhost:8082
prestamos.servicios.notificacion-url=http://localhost:8084
prestamos.servicios.notificacion-path=/api/notificaciones/evento
prestamos.servicios.notificacion-habilitada=true
prestamos.servicios.timeout-segundos=5
prestamos.servicios.catalogo-admin-usuario-id=${CATALOGO_ADMIN_USUARIO_ID:}
```

`catalogo-admin-usuario-id` debe ser el UUID de un usuario `ADMINISTRADOR` de
Auth, porque Catalogo exige ese rol para actualizar el estado de un ejemplar.
En ejecucion se puede definir con la variable de entorno
`CATALOGO_ADMIN_USUARIO_ID` o con `--prestamos.servicios.catalogo-admin-usuario-id=...`.

## Seguridad y roles

Todas las rutas de prestamos requieren el encabezado:

```
X-Usuario-Id: <uuid del usuario>
```

El rol se obtiene de Auth; no se confia en un encabezado de rol enviado por el
cliente. Las consultas propias validan que el usuario autenticado sea el
propietario o un administrador. Los listados globales, la consulta de vencidos,
la anulacion de multas y las operaciones administrativas usan
`@PreAuthorize("hasRole('ADMINISTRADOR')")`.

## Endpoints

Los endpoints estan disponibles con dos prefijos equivalentes:

- `/api/biblioteca/prestamos/...` (convencion de los otros microservicios)
- `/prestamos/...` (rutas solicitadas)

### Prestamos

| Metodo | Ruta | Acceso |
| --- | --- | --- |
| POST | `/prestamos/solicitar` | Autenticado |
| POST | `/prestamos/devolver/{id}` | Propietario o admin |
| POST | `/prestamos/reservar/{libroId}` | Autenticado |
| POST | `/prestamos/renovar/{id}` | Propietario o admin |
| GET | `/prestamos/mis-prestamos/{usuarioId}` | Propietario o admin |
| GET | `/prestamos/historial/{usuarioId}` | Propietario o admin |
| GET | `/prestamos/multas/{usuarioId}` | Propietario o admin |
| GET | `/prestamos/vencidos` | ADMINISTRADOR |
| GET | `/prestamos` | ADMINISTRADOR |
| GET | `/prestamos/{id}` | Propietario o admin |
| GET | `/prestamos/{id}/devolucion` | Propietario o admin |
| GET | `/prestamos/{id}/multas` | Propietario o admin |
| GET | `/prestamos/{id}/renovaciones` | Propietario o admin |
| PUT | `/prestamos/{id}/vencimiento` | ADMINISTRADOR |
| PUT | `/prestamos/{id}/cancelar` | ADMINISTRADOR |
| DELETE | `/prestamos/{id}` | ADMINISTRADOR |

### Reservas

| Metodo | Ruta | Acceso |
| --- | --- | --- |
| GET | `/prestamos/reservas` | ADMINISTRADOR |
| GET | `/prestamos/reservas/{id}` | Propietario o admin |
| GET | `/prestamos/reservas/usuario/{usuarioId}` | Propietario o admin |
| GET | `/prestamos/reservas/libro/{libroId}` | ADMINISTRADOR |
| PUT | `/prestamos/reservas/{id}/cancelar` | Propietario o admin |
| POST | `/prestamos/reservas/expirar` | ADMINISTRADOR |
| DELETE | `/prestamos/reservas/{id}` | ADMINISTRADOR |

### Multas

| Metodo | Ruta | Acceso |
| --- | --- | --- |
| GET | `/prestamos/multas` | ADMINISTRADOR |
| GET | `/prestamos/multas/id/{idMulta}` | Propietario o admin |
| GET | `/prestamos/multas/usuario/{usuarioId}` | Propietario o admin |
| GET | `/prestamos/multas/usuario/{usuarioId}/pendientes` | Propietario o admin |
| GET | `/prestamos/multas/prestamo/{idPrestamo}` | Propietario o admin |
| PUT | `/prestamos/multas/{idMulta}/pagar` | Propietario o admin |
| PUT | `/prestamos/multas/{idMulta}/anular` | ADMINISTRADOR |
| DELETE | `/prestamos/multas/{idMulta}` | ADMINISTRADOR |

### Devoluciones

| Metodo | Ruta | Acceso |
| --- | --- | --- |
| GET | `/prestamos/devoluciones` | ADMINISTRADOR |
| GET | `/prestamos/devoluciones/{id}` | Propietario o admin |
| GET | `/prestamos/devoluciones/prestamo/{idPrestamo}` | Propietario o admin |
| DELETE | `/prestamos/devoluciones/{id}` | ADMINISTRADOR |

### Renovaciones

| Metodo | Ruta | Acceso |
| --- | --- | --- |
| GET | `/prestamos/renovaciones` | ADMINISTRADOR |
| GET | `/prestamos/renovaciones/{id}` | Propietario o admin |
| GET | `/prestamos/renovaciones/prestamo/{idPrestamo}` | Propietario o admin |
| DELETE | `/prestamos/renovaciones/{id}` | ADMINISTRADOR |

## Ejemplos

Solicitar un prestamo:

```bash
curl -X POST http://localhost:8083/prestamos/solicitar \
  -H "Content-Type: application/json" \
  -H "X-Usuario-Id: <uuid-usuario>" \
  -d '{
    "idUsuario": "<uuid-usuario>",
    "idEjemplar": "<uuid-ejemplar>"
  }'
```

Devolver un prestamo:

```bash
curl -X POST http://localhost:8083/prestamos/devolver/<uuid-prestamo> \
  -H "Content-Type: application/json" \
  -H "X-Usuario-Id: <uuid-usuario>" \
  -d '{
    "estadoEjemplar": "BUENO",
    "observaciones": "Devuelto en buen estado"
  }'
```

Reservar un libro:

```bash
curl -X POST http://localhost:8083/prestamos/reservar/<uuid-libro> \
  -H "Content-Type: application/json" \
  -H "X-Usuario-Id: <uuid-usuario>" \
  -d '{ "idUsuario": "<uuid-usuario>" }'
```

Renovar:

```bash
curl -X POST http://localhost:8083/prestamos/renovar/<uuid-prestamo> \
  -H "X-Usuario-Id: <uuid-usuario>"
```

Consultar multas pendientes:

```bash
curl http://localhost:8083/prestamos/multas/<uuid-usuario> \
  -H "X-Usuario-Id: <uuid-usuario>"
```

## Base de datos y scripts

Se utiliza la misma base Neon de `auth` y `catalogo`. Las credenciales se leen
de `.env.local` o de `DATABASE_URL_UNPOOLED`, igual que en los otros servicios.

Hibernate usa `spring.jpa.hibernate.ddl-auto=update`: las tablas se crean y
actualizan desde las entidades JPA de `infraestructure/driver_adapters`. No se
usan scripts SQL.

Tablas creadas: `prestamo`, `reserva`, `devolucion`, `multa`, `renovacion`.
Las llaves foraneas solo existen entre estas tablas. `id_usuario`, `id_libro`
e `id_ejemplar` quedan como referencias externas sin FK.

## Job de vencimientos

`PrestamoVencimientoJob` se ejecuta cada
`prestamos.job.intervalo-ms` (1 hora por defecto) y:

- Marca como `VENCIDO` los prestamos `ACTIVO` con fecha de vencimiento pasada.
- Genera o actualiza la multa pendiente de cada prestamo vencido.
- Expira las reservas `PENDIENTE` cuya fecha de expiracion ya paso.
- Envia eventos de notificacion de vencimiento y expiracion.

## Pruebas y cobertura

```bash
cd prestamos
./mvnw verify
```

Resultado verificado:

- **97 pruebas** JUnit 5 + Mockito, todas exitosas.
- Cobertura JaCoCo sobre `domain/usecase` **>= 90 %** (check configurado en
  `pom.xml`, `LINE COVEREDRATIO 0.90`).
- Prueba E2E contra Auth y Catalogo reales con Neon:
  prestamo, renovacion, reserva, devolucion, prestamo vencido, generacion y
  pago de multa, expiracion de reserva y validacion de roles
  (`401` sin identidad, `403` sin permisos, `409` por reglas de negocio).
  Los datos de prueba se eliminaron al finalizar.

## Ejecutar

```bash
cd prestamos
./mvnw spring-boot:run
```

El servicio queda en `http://localhost:8083`. Requiere:

1. `.env.local` con `DATABASE_URL_UNPOOLED` (o la variable de entorno).
2. Auth en `http://localhost:8081`.
3. Catalogo en `http://localhost:8082`.
4. `CATALOGO_ADMIN_USUARIO_ID` con un UUID de un usuario `ADMINISTRADOR`.
5. Opcionalmente Notificacion en `http://localhost:8084`; si no esta, los
   eventos se registran como advertencia y no bloquean las operaciones.
