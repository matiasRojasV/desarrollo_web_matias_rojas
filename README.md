# Tarea 3 - CC5002 Desarrollo de Aplicaciones Web

Este proyecto corresponde al desarrollo de la **Tarea 3** para la Unión de Ornitólogos de Chile. Consiste en la evolución de las entregas anteriores hacia una aplicación web dinámica funcional construida con **Python (Flask)**, **SQLAlchemy ORM** y **MySQL**.

En esta iteración se incorporan funcionalidades asíncronas (AJAX/Fetch) para la carga de estadísticas dinámicas y un sistema interactivo de comentarios en tiempo real para los avistamientos. La aplicación permite registrar voluntarios, reportar avistamientos acompañados de archivos multimedia, consultar avistamientos, interactuar con comentarios y visualizar métricas.

---

## Requisitos del Sistema y Configuración

### 1. Requisitos
* **Python:** versión 3.8 o superior.
* **MySQL Server:** en ejecución en `localhost:3306`.
* **Librerías de Python:** Flask, SQLAlchemy, PyMySQL y Werkzeug

### 2. Base de Datos
1. Asegurarse de que el servicio MySQL esté activo en el puerto `3306`.
2. Crear la base de datos `tarea2` y el usuario requerido ejecutando las siguientes credenciales:
   * **Host:** `localhost`
   * **Puerto:** `3306`
   * **Base de datos:** `tarea2`
   * **Usuario:** `cc5002`
   * **Contraseña:** `programacionweb`
3. Cargar los scripts SQL en el siguiente orden estricto dentro de la carpeta `SQL/`:
   1. `tarea2.sql` (Estructura de tablas principales)
   2. `region-comuna.sql` (Poblado de regiones y comunas)
   3. `aves.sql` (Catálogo nacional de aves)
   4. `tabla-comentario.sql` (Nueva tabla para el sistema de comentarios)

---

## Instrucciones de Ejecución

1. Clonar el repositorio y cambiarse a la rama `Tarea 3`:
   ```bash
   git switch "Tarea 3"
   ```
   
2. Creación de entorno virtual e instalación de requerimientos:
   ```bash
   python -m venv .venv
   . .venv/bin/activate
   pip install -r requirements.txt
   ```

3. Iniciar la aplicación Flask:
   ```bash
   python app.py
   ```
4. Acceder mediante el navegador a: [http://127.0.0.1:5000](http://127.0.0.1:5000)

---

## Decisiones de Diseño e Implementación

### 1. Sistema Asíncrono de Estadísticas y Gráficos
Para la generación de los gráficos requeridos en el Dashboard, se utilizó la biblioteca externa **Chart.js**. 
* **Obtención de Datos:** En el lado del cliente, `dashboard.js` hace uso de la API `fetch` nativa de JavaScript para realizar una petición HTTP GET a la ruta `/api/estadisticas` del servidor de Flask.
* **Separación de Lógica:** Las consultas complejas (agrupación de avistamientos por día, por tipo de ave y voluntarios por comuna) se mantuvieron estrictamente en `db.py` utilizando las funciones de agregación `func.count` y `func.date` de SQLAlchemy. Flask formatea estos datos y los devuelve como JSON puro para que Chart.js los renderice sin recargar la vista.

### 2. Sistema Interactivo de Comentarios
Se implementó un flujo de comentarios asíncrono en la vista de detalle de los avistamientos.
* **Doble Validación:** Al igual que en los demás formularios, se aplican estrictas validaciones en el lado del cliente (mediante `comentarios.js`) verificando longitudes mínimas y máximas de los textos. Posteriormente, en el servidor (ruta `/api/comentarios`), se repiten las validaciones y se limpian las cadenas con `.strip()` para proteger la base de datos contra peticiones maliciosas.
* **Manipulación del DOM:** Si el comentario supera las validaciones en el backend, la inserción se realiza de forma asíncrona mediante `fetch`. Con la respuesta exitosa, JavaScript inyecta directamente un nuevo elemento `<li>` en el listado de comentarios actual, brindando una experiencia de usuario instantánea y fluida.

### 3. Registro de Voluntarios y Manejo de Sesión
* Al registrar exitosamente un voluntario, se guardan credenciales nativas en `session` y se protege la ruta de avistamientos para usuarios no autenticados.
* La fecha de registro y los comentarios se calculan en el servidor usando `datetime.now()`.

### 4. Almacenamiento Multimedia y Manejo Transaccional
* Admite subida de múltiples archivos validados y saneados con `secure_filename` de Werkzeug. Si ocurre un error en la transacción hacia la base de datos, existe un mecanismo de "Rollback" que se encarga de eliminar físicamente los archivos residuales.

### 5. Arquitectura CSS Modular y Estandarizada
Los estilos CSS están divididos por responsabilidades en `static/css/modules/` (`layout.css`, `forms.css`, `dashboard.css`). Para esta entrega, se universalizó la clase `.btn-submit` y `.alerta-error` logrando que los formularios dinámicos (como el de comentarios) hereden exactamente el mismo diseño que los estáticos.

---

## Estructura del Proyecto

```text
.
├── app.py                     # Controlador Flask
├── db.py                      # Modelos SQLAlchemy, conexión a MySQL
├── README.md                  # Documentación del proyecto
├── requirements.txt           # Dependencias del proyecto Python
├── SQL/                       # Scripts para la base de datos
│   ├── aves.sql
│   ├── region-comuna.sql
│   ├── tabla-comentario.sql   # Modelo físico de la tabla de comentarios
│   └── tarea2.sql
├── static/
│   ├── css/
│   │   ├── styles.css         # CSS principal que agrupa importaciones
│   │   └── modules/           # Módulos de diseño estructurados
│   │       ├── dashboard.css  
│   │       ├── forms.css      
│   │       └── layout.css     
│   ├── js/
│   │   ├── avistamientoVal.js # Validación de formulario de avistamiento
│   │   ├── comentarios.js     # Lógica asíncrona y validaciones para comentarios
│   │   ├── dashboard.js       # Consumo asíncrono para Chart.jsmultimedia
│   │   └── singUpVal.js       # Validación de voluntario y carga AJAX de comunas
│   └── uploads/               # Archivos multimedia subidos por los usuarios
└── templates/
    ├── avistamiento.html      
    ├── base.html              
    ├── dashboard.html         
    ├── detalleAvistamiento.html # Vista detallada con comentarios
    ├── exitoLogin.html        
    ├── index.html             
    ├── listAvistamientos.html 
    └── login.html             
```