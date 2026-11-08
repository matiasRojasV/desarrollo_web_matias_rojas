# Tarea 4 - Spring Boot

La aplicación se implementó con Java 17+, Spring Boot, Thymeleaf, Spring Data JPA y MySQL. Para preparar la base de datos, revisar las rutas disponibles, configurar las credenciales y conocer los flujos de uso, consulta el [README principal](../README.md).

## Ejecutar

Requiere que MySQL esté iniciado y que el esquema `tarea2` tenga las tablas y datos iniciales requeridos. Desde esta carpeta:

```bash
./mvnw spring-boot:run
```

La aplicación queda disponible en [http://localhost:8080](http://localhost:8080). Las contraseñas y rutas de conexión se pueden configurar con `DB_PASSWORD`, `DB_USERNAME` y `DB_URL`.

## Capas

- `src/main/java/com/tarea4/tarea4/model`: entidades JPA.
- `src/main/java/com/tarea4/tarea4/repository`: repositorios y consultas.
- `src/main/java/com/tarea4/tarea4/service`: reglas de negocio y transacciones.
- `src/main/java/com/tarea4/tarea4/controller`: controladores MVC y endpoints JSON.
- `src/main/resources/templates`: vistas Thymeleaf.
- `src/main/resources/static`: JavaScript y CSS.
