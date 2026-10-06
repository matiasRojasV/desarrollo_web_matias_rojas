# Tarea 3 - CC5002 Desarrollo de Aplicaciones Web

Este proyecto corresponde al desarrollo de la **Tarea 2** para la Unión de Ornitólogos de Chile. Consiste en la evolución del prototipo frontend desarrollado en la Tarea 1 hacia una aplicación web dinámica funcional construida con **Python (Flask)**, **SQLAlchemy ORM** y **MySQL**.

La aplicación permite registrar voluntarios, reportar avistamientos de aves acompañados de archivos multimedia, consultar avistamientos de forma paginada y ordenada, e interactuar con un panel de métricas visuales.

---

## Requisitos del Sistema y Configuración

### 1. Requisitos
* **Python:** versión 3.8 o superior.
* **MySQL Server:** en ejecución en `localhost:3306`.
* **Librerías de Python:**
  ```bash
  pip install Flask SQLAlchemy PyMySQL Werkzeug
  ```

### 2. Base de Datos
1. Asegurarse de que el servicio MySQL esté activo en el puerto `3306`.
2. Crear la base de datos `tarea2` y el usuario requerido ejecutando las siguientes credenciales:
   * **Host:** `localhost`
   * **Puerto:** `3306`
   * **Base de datos:** `tarea2`
   * **Usuario:** `cc5002`
   * **Contraseña:** `programacionweb`
3. Cargar los scripts SQL en el siguiente orden estricto:
   1. `tarea2.sql` (Estructura de tablas: `voluntario`, `region`, `comuna`, `ave`, `avistamiento`, `registro`)
   2. `region-comuna.sql` (Poblado de regiones y comunas)
   3. `aves.sql` (Catálogo nacional de aves)

---

## Instrucciones de Ejecución

1. Clonar el repositorio y cambiarse a la rama `Tarea 2`:
   ```bash
   git switch "Tarea-2"
   ```
   
3. creacion de entorno virtual e intalacion de requerimientos:
   ```bash
   python -m venv .venv
   . .venv/bin/activate
   pip install -r requirements.txt
   ```

2. Iniciar la aplicación Flask:
   ```bash
   python app.py
   ```
3. Acceder mediante el navegador a: [http://127.0.0.1:5000](http://127.0.0.1:5000)

---

## Decisiones de Diseño e Implementación

### 1. Registro de Voluntarios y Manejo de Sesión
* **Flujo Post-Registro (`exitoLogin.html`):** Al registrar exitosamente un voluntario en `/login`, la aplicación guarda su ID y nombre en la sesión del servidor (`session['voluntario_id']`) y redirige a una plantilla intermedia `exitoLogin.html`. Esta vista confirma la acción y le da la opción al voluntario de continuar al inicio o registrar un avistamiento directamente.
* **Marca de Tiempo Automática:** La fecha y hora de registro se calculan en el servidor mediante `datetime.now()` al momento de insertar en la tabla `voluntario`.
* **Protección de Rutas:** Para ingresar un avistamiento (`/avistamiento`), la ruta verifica la presencia de la sesión activa. Si no existe, redirige al login con una alerta.

### 2. Doble Validación (Cliente / Servidor)
* **Validación en Cliente (JavaScript):**
  * `singUpVal.js`: Valida los datos del voluntario (formato de correo, número celular de Chile) y consume la API interna `/get_comunas/<region_id>` vía AJAX para cargar las comunas dinámicamente según la región seleccionada.
  * `avistamientoVal.js`: Verifica que se complete el ave, lugar, fecha, hora y archivo adjunto antes de enviar.
* **Validación en Servidor (Python/Flask):**
  * Se re-verifican todos los datos usando expresiones regulares (`re`) para proteger el sistema ante peticiones maliciosas que omitan las validaciones de JS.
  * Ante errores de validación, se preservan los mensajes de error en la vista sin perder el contexto.

### 3. Almacenamiento Multimedia y Manejo Transaccional
* **Seguridad de Archivos:** Las imágenes y videos adjuntados se sanean con `secure_filename` de Werkzeug y se guardan físicamente en `static/uploads/`.
* **Registros Múltiples:** Admite múltiples archivos por avistamiento, creando una entrada en la tabla `registro` por cada archivo subido.
* **Limpieza de Archivos en Fallos (Rollback):** Si ocurre una falla en la base de datos tras haber guardado los archivos en el sistema de archivos, un bloque `try-except` se encarga de eliminar los archivos recién subidos en disco para evitar guardar archivos huérfanos.

### 4. Arquitectura CSS Modular
Para mantener un código limpio, escalable y mantenible (siguiendo estándares de validación W3C), los estilos CSS se dividieron en **módulos dentro de la carpeta `static/css/modules/`**:
* **`layout.css`:** Contiene los estilos estructurales, variables CSS globales, navegación, tarjetas de avistamientos y reglas de maquetación responsive.
* **`forms.css`:** Define el diseño de formularios, selecciones, botones, mensajes y alertas de error.
* **`dashboard.css`:** Modula la grilla y los contenedores de tarjetas para las métricas y gráficos.

### 5. Paginación, Ordenamiento y Dashboard Interactivo
* **Paginación Personalizada:** La clase `Paginador` en `db.py` realiza consultas eficientes a MySQL usando `OFFSET` y `LIMIT`.
* **Filtros de Ordenamiento:** La vista `listAvistamientos.html` permite ordenar los resultados dinámicamente por fecha (ascendente/descendente), lugar o nombre de ave.
* **Métricas (`dashboard.js`):** Integra la librería Chart.js para renderizar gráficos dinámicos e interactivos en la vista `/dashboard` (distribución de voluntarios por región y tendencia de avistamientos).

---

## Estructura del Proyecto

```text
.
├── app.py                     # Controlador Flask con la lógica de rutas y sesiones
├── db.py                      # Modelos SQLAlchemy, conexión a MySQL y funciones CRUD
├── static/
│   ├── css/
│   │   ├── styles.css         # CSS principal que importa los módulos
│   │   └── modules/           # Módulos de diseño estructurados
│   │       ├── layout.css     # Estructura global, header, nav, tarjetas y listados
│   │       ├── forms.css      # Estilos de formularios, inputs, leyendas y mensajes
│   │       └── dashboard.css  # Diseño de tarjetas y layout para métricas
│   ├── js/
│   │   ├── singUpVal.js       # Validación JS de voluntario y carga AJAX de comunas
│   │   ├── avistamientoVal.js # Validación JS de formulario de avistamiento
│   │   └── dashboard.js       # Visualización de gráficos interactivas con Chart.js
│   └── uploads/               # Carpeta para archivos multimedia subidos por los usuarios
└── templates/
    ├── base.html              # Plantilla base con navbar y estado de sesión
    ├── index.html             # Portada con resumen de los últimos 2 avistamientos
    ├── login.html             # Formulario de registro de voluntario
    ├── exitoLogin.html        # Confirmación de registro con opciones de navegación
    ├── avistamiento.html      # Formulario para publicar nuevos avistamientos
    ├── listAvistamientos.html # Listado paginado y ordenable de avistamientos
    ├── detalleAvistamiento.html # Vista detallada del avistamiento con multimedia
    └── dashboard.html         # Panel con métricas y gráficos del proyecto
```