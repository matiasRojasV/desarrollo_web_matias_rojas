# Tarea 1 - CC5002 Aplicaciones Web

## Estructura del proyecto

Este proyecto fue desarrollado con HTML5, CSS3 y JavaScript, sin backend ni base de datos.

- `html/index.html`: listado de avistamientos con filtro, ordenamiento y paginacion.
- `html/login.html`: formulario de registro de voluntarios.
- `html/avistamiento.html`: formulario para informar avistamientos.
- `html/dashboard.html`: panel de metricas con graficos.
- `js/singUpVal.js`: validacion del registro y carga de regiones/comunas desde JSON.
- `js/avistamientoVal.js`: validacion del formulario de avistamiento y control de acceso por sesion.
- `js/indexFilter.js`: filtro por tipo, ordenamiento y logica de paginacion del listado.
- `js/dashboard.js`: configuracion de graficos usando Chart.js.
- `js/authHeader.js`: renderizado del estado de sesion en cabecera y cierre de sesion.
- `js/regiones.json`: datos de regiones y comunas de Chile.
- `css/styles.css`: archivo de entrada de estilos.
- `css/modules/`: estilos separados por responsabilidad (`base.css`, `layout.css`, `forms.css`, `index.css`, `dashboard.css`).

## Decisiones tecnicas y de diseno

## 1) HTML semantico y codigo legible
Se priorizo el uso de etiquetas semanticas (`header`, `nav`, `main`, `article`, `fieldset`) para mantener una estructura clara y facilitar validacion y mantencion.

## 2) Validaciones en JavaScript
Las reglas de validacion se implementaron en JavaScript, por ejemplo:

- Registro: nombre minimo, formato de correo, formato de celular chileno, region y comuna obligatorias.
- Avistamiento: tipo, nombre y lugar obligatorios, fecha/hora no futura, limite de antiguedad y archivo multimedia obligatorio.

## 3) Sesion local (Web Storage)
Como la tarea es un prototipo estatico, la cuenta/sesion se simula en `localStorage`:

- Al registrarse correctamente, se guardan:
  - `voluntarioRegistrado = "true"`
  - `nombreVoluntario = "<nombre ingresado>"`
- `authHeader.js` lee esos datos para mostrar en el header:
  - estado no autenticado: enlace `Registro / Login`
  - estado autenticado: saludo + boton `Cerrar Sesion`
- Al cerrar sesion, se eliminan ambas claves del `localStorage`.
- `avistamientoVal.js` restringe el acceso al formulario de avistamiento si no existe sesion activa, redirigiendo a `login.html`.

Esta decision permite probar el flujo completo de navegacion y permisos sin necesidad de servidor ni persistencia real.

## 4) CSS modular simple
Se separo el CSS por responsabilidades:

- `base.css`: reset, variables y estilos globales.
- `layout.css`: header, navegacion y contenedor principal.
- `forms.css`: formularios, inputs, botones y mensajes de error.
- `index.css`: estilos del listado de avistamientos y paginacion.
- `dashboard.css`: estilos especificos del dashboard.

`styles.css` actua como agregador mediante `@import`, por lo que cada HTML mantiene un solo `<link>` principal.

## 5) Listado de avistamientos con filtro, orden y paginacion
En `index.html` se implementa:

- filtro por tipo de ave,
- orden por fecha (asc/desc) y lugar,
- paginacion en cliente (`Anterior`, `Siguiente`, indicador de pagina).

La logica en `indexFilter.js` trabaja sobre nodos del DOM ya cargados, evitando recargas de pagina.

## 6) Dashboard de metricas
`dashboard.html` usa Chart.js para mostrar indicadores de voluntarios y avistamientos. Los estilos de esta vista se movieron a `css/modules/dashboard.css` para evitar CSS embebido en HTML.