# Tarea 4 - Unión de Ornitólogos de Chile

La aplicación de esta entrega está en [`tarea4/`](./tarea4/). Está implementada con **Java 17+, Spring Boot, Thymeleaf, Spring Data JPA y MySQL**. El código Flask de la raíz corresponde a las tareas anteriores; para la Tarea 4 se debe ejecutar exclusivamente el proyecto Spring Boot.

## Requisitos

- Java 17 o superior.
- MySQL iniciado y accesible desde la aplicación.
- Maven Wrapper incluido en `tarea4/`.

## Preparar la base de datos

La aplicación usa el esquema `tarea2` y tiene `spring.jpa.hibernate.ddl-auto=none`: **JPA no crea ni modifica las tablas automáticamente**. Para una base de datos nueva, ejecutar los scripts en este orden:

1. [`SQL/tarea2.sql`](./SQL/tarea2.sql): crea el esquema y las tablas principales.
2. [`SQL/region-comuna.sql`](./SQL/region-comuna.sql): carga regiones y comunas.
3. [`SQL/aves.sql`](./SQL/aves.sql): carga el catálogo de aves.
4. [`SQL/tabla-comentario.sql`](./SQL/tabla-comentario.sql): crea la tabla de comentarios.
5. [`SQL/tabla-nota.sql`](./SQL/tabla-nota.sql): crea la tabla para las evaluaciones.

**Importante:** `tarea2.sql` elimina y vuelve a crear el esquema `tarea2`. No lo ejecutes sobre una base de datos con información que quieras conservar. Si ya tienes las tablas base pero ves un error de que no existe `nota` o `comentario`, ejecuta únicamente el script correspondiente.

El usuario predeterminado de MySQL es `cc5002`, con contraseña `programacionweb`, según la configuración de los scripts SQL. Si tu instalación tiene otros datos, configura estas variables antes de ejecutar:

| Variable | Valor predeterminado |
|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/tarea2?useUnicode=true&characterEncoding=utf8&serverTimezone=America/Santiago` |
| `DB_USERNAME` | `cc5002` |
| `DB_PASSWORD` | `programacionweb` |
| `UPLOAD_DIR` | `../static/uploads` (relativa a `tarea4/`) |

## Ejecutar la aplicación

Desde la raíz del repositorio:

```bash
cd tarea4
./mvnw spring-boot:run
```

En Windows, ejecutar `mvnw.cmd spring-boot:run`. Abre [http://localhost:8080](http://localhost:8080) en el navegador. Si MySQL tiene otra contraseña, define `DB_PASSWORD` en el entorno antes de iniciar Spring Boot.

## Rutas y flujos

### Páginas

| Ruta | Función |
|---|---|
| `/` | Portada y últimos avistamientos. |
| `/login` | Formulario para registrar un voluntario. El registro guarda al voluntario y su sesión; no hay autenticación mediante contraseña. |
| `/logout` | Cierra la sesión del voluntario. |
| `/avistamiento` | Formulario para informar un avistamiento; requiere sesión de voluntario y archivos de imagen o video. |
| `/avistamiento/{id}` | Detalle del avistamiento y comentarios. |
| `/listAvistamientos` | Listado paginado y ordenable. |
| `/buscar` | Búsqueda en vivo por ave, voluntario o comuna y evaluación de avistamientos. |
| `/dashboard` | Gráficos estadísticos. |

### Endpoints asíncronos

| Método y ruta | Función |
|---|---|
| `GET /get_comunas/{regionId}` | Carga las comunas de la región seleccionada en el registro. |
| `GET /api/avistamientos/buscar?q=...` | Busca por nombre de ave, voluntario o comuna y devuelve los datos para los resultados. |
| `POST /api/notas` | Guarda una nota entera entre 1 y 7 y responde con el promedio actualizado. |
| `POST /api/comentarios` | Valida y guarda un comentario en la página de detalle. |
| `GET /api/estadisticas` | Provee los datos para los gráficos del dashboard. |

La búsqueda, las notas, los comentarios, las comunas y las estadísticas usan `fetch` desde JavaScript. Las notas y comentarios se guardan en la base de datos; las notas se vuelven a promediar en cada evaluación. Las validaciones se repiten en el servidor.

## Estructura

- `src/main/java/.../model/`: entidades JPA relacionadas con las tablas de MySQL.
- `src/main/java/.../repository/`: consultas Spring Data JPA.
- `src/main/java/.../service/`: operaciones de persistencia y negocio.
- `src/main/java/.../controller/`: rutas MVC y endpoints JSON.
- `src/main/resources/templates/`: vistas Thymeleaf.
- `src/main/resources/static/`: CSS y JavaScript del navegador.

La configuración específica de Spring Boot está en [`tarea4/src/main/resources/application.properties`](./tarea4/src/main/resources/application.properties).
