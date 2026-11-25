# Microservicio de Catalogo

Microservicio de biblioteca construido con la misma arquitectura hexagonal del
microservicio `auth`, sobre la **misma base de datos Neon**. Gestiona libros,
autores, editoriales, categorias y ejemplares fisicos, y consulta informacion
bibliografica en APIs externas.

## Arquitectura

```
com.biblioteca.catalogo
├── aplication                 Configuracion de los casos de uso (beans)
├── domain
│   ├── exception              Excepciones de negocio
│   ├── model                  Modelos de dominio + enums + records
│   ├── model/gateway          Puertos (interfaces) de persistencia y APIs
│   └── usecase                Reglas de negocio
└── infraestructure
    ├── config                 DataSource de Neon
    ├── driver_adapters        Entidades JPA, repositorios y adaptadores
    ├── entry_points           Controllers REST + manejador global de errores
    ├── external               Adaptador de Open Library / Google Books
    ├── mapper                 Mapeo dominio <-> entidad JPA
    └── security               Roles y filtro X-Usuario-Id
```

El flujo es el mismo de `auth`: `Controller -> UseCase -> Gateway -> Adapter JPA -> PostgreSQL`.

## Base de datos compartida

El microservicio usa la misma base de datos que `auth`. Las credenciales se leen
de `.env.local` (o de `DATABASE_URL_UNPOOLED` en el entorno), igual que en
`auth`.

Tablas propias del catalogo:

| Tabla | Descripcion |
| --- | --- |
| `autor` | Autores |
| `editorial` | Editoriales |
| `categoria` | Categorias |
| `libro` | Libros (FK `id_editorial -> editorial`) |
| `libro_autor` | Relacion N:M libro-autor (FK a `libro` y `autor`) |
| `libro_categoria` | Relacion N:M libro-categoria (FK a `libro` y `categoria`) |
| `ejemplar` | Ejemplares fisicos (FK `id_libro -> libro`) |

La tabla `usuario` no se duplica: el catalogo solo consulta `usuario.rol` para
autorizar las operaciones de escritura.

## Roles

- `GET` del catalogo: publico.
- `POST`, `PUT` y `DELETE`: requieren rol `ADMINISTRADOR`.

El cliente debe enviar el encabezado:

```
X-Usuario-Id: <id del usuario registrado por auth>
```

El rol **no** se toma del encabezado: se consulta en la tabla `usuario` de la
base de datos compartida, de modo que un cliente no puede escalar privilegios
enviando un rol falso.

Para obtener el id de un usuario:

```
GET http://localhost:8081/api/biblioteca/usuario/correo/{correo}
```

## Endpoints

### Autores

| Metodo | Ruta | Rol |
| --- | --- | --- |
| GET | `/api/biblioteca/autores` | Publico |
| GET | `/api/biblioteca/autores/{id}` | Publico |
| POST | `/api/biblioteca/autores` | ADMINISTRADOR |
| PUT | `/api/biblioteca/autores/{id}` | ADMINISTRADOR |
| DELETE | `/api/biblioteca/autores/{id}` | ADMINISTRADOR |

### Editoriales

| Metodo | Ruta | Rol |
| --- | --- | --- |
| GET | `/api/biblioteca/editoriales` | Publico |
| GET | `/api/biblioteca/editoriales/{id}` | Publico |
| POST | `/api/biblioteca/editoriales` | ADMINISTRADOR |
| PUT | `/api/biblioteca/editoriales/{id}` | ADMINISTRADOR |
| DELETE | `/api/biblioteca/editoriales/{id}` | ADMINISTRADOR |

### Categorias

| Metodo | Ruta | Rol |
| --- | --- | --- |
| GET | `/api/biblioteca/categorias` | Publico |
| GET | `/api/biblioteca/categorias/{id}` | Publico |
| POST | `/api/biblioteca/categorias` | ADMINISTRADOR |
| PUT | `/api/biblioteca/categorias/{id}` | ADMINISTRADOR |
| DELETE | `/api/biblioteca/categorias/{id}` | ADMINISTRADOR |

### Libros

| Metodo | Ruta | Rol |
| --- | --- | --- |
| GET | `/api/biblioteca/libros` | Publico |
| GET | `/api/biblioteca/libros/buscar?texto=&idAutor=&idCategoria=&idEditorial=&idioma=` | Publico |
| GET | `/api/biblioteca/libros/{id}` | Publico |
| GET | `/api/biblioteca/libros/{id}/disponibilidad` | Publico |
| POST | `/api/biblioteca/libros` | ADMINISTRADOR |
| PUT | `/api/biblioteca/libros/{id}` | ADMINISTRADOR |
| DELETE | `/api/biblioteca/libros/{id}` | ADMINISTRADOR |

La busqueda `texto` consulta titulo, ISBN y nombre/apellido de los autores.

### Ejemplares fisicos

| Metodo | Ruta | Rol |
| --- | --- | --- |
| GET | `/api/biblioteca/ejemplares` | Publico |
| GET | `/api/biblioteca/ejemplares/disponibles` | Publico |
| GET | `/api/biblioteca/ejemplares/libro/{idLibro}` | Publico |
| GET | `/api/biblioteca/ejemplares/{id}` | Publico |
| POST | `/api/biblioteca/ejemplares` | ADMINISTRADOR |
| PUT | `/api/biblioteca/ejemplares/{id}` | ADMINISTRADOR |
| DELETE | `/api/biblioteca/ejemplares/{id}` | ADMINISTRADOR |

Estados permitidos: `DISPONIBLE`, `PRESTADO`, `RESERVADO`, `MANTENIMIENTO`, `PERDIDO`.

### APIs externas

| Metodo | Ruta | Rol |
| --- | --- | --- |
| GET | `/api/biblioteca/externo/buscar?q=...` | Publico |
| GET | `/api/biblioteca/externo/isbn/{isbn}` | Publico |
| POST | `/api/biblioteca/externo/importar/{isbn}?idCategoria=` | ADMINISTRADOR |

Se consulta Open Library y, si no hay resultados o falla, Google Books.
La importacion crea automaticamente la editorial, los autores y la categoria
`General` si no existen.

## Ejemplos

Crear una editorial:

```bash
curl -X POST http://localhost:8082/api/biblioteca/editoriales \
  -H "Content-Type: application/json" \
  -H "X-Usuario-Id: <id-admin>" \
  -d '{"nombre":"Sudamericana","pais":"Argentina"}'
```

Crear un libro clasificado:

```bash
curl -X POST http://localhost:8082/api/biblioteca/libros \
  -H "Content-Type: application/json" \
  -H "X-Usuario-Id: <id-admin>" \
  -d '{
    "isbn": "9780307474728",
    "titulo": "Cien anos de soledad",
    "idioma": "es",
    "numeroPaginas": 471,
    "idEditorial": "<id-editorial>",
    "idsCategorias": ["<id-categoria>"],
    "idsAutores": ["<id-autor>"]
  }'
```

Registrar un ejemplar fisico:

```bash
curl -X POST http://localhost:8082/api/biblioteca/ejemplares \
  -H "Content-Type: application/json" \
  -H "X-Usuario-Id: <id-admin>" \
  -d '{
    "idLibro": "<id-libro>",
    "codigoInventario": "INV-0001",
    "ubicacion": "Estante A-1",
    "estado": "DISPONIBLE"
  }'
```

Consultar disponibilidad:

```bash
curl http://localhost:8082/api/biblioteca/libros/<id-libro>/disponibilidad
```

## Ejecutar

```bash
cd catalogo
./mvnw spring-boot:run
```

El servicio queda en `http://localhost:8082`. Requiere `.env.local` con
`DATABASE_URL_UNPOOLED` (o `DATABASE_URL`), igual que `auth`.

> `spring.jpa.hibernate.ddl-auto=update` crea/actualiza las tablas al iniciar,
> manteniendo el mismo criterio del microservicio `auth`.
