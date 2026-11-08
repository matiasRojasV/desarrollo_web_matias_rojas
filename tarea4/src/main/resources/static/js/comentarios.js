document.addEventListener('DOMContentLoaded', () => {
    const formComentario = document.getElementById('form-comentario');
    let contenedorErrores = document.getElementById('errores-comentario');
    const listaComentarios = document.getElementById('lista-comentarios');
    const msjSinComentarios = document.getElementById('sin-comentarios');

    if (!formComentario) return;
    if (!contenedorErrores) {
        contenedorErrores = document.createElement('div');
        contenedorErrores.id = 'errores-comentario';
        contenedorErrores.setAttribute('role', 'alert');
        formComentario.before(contenedorErrores);
    }
    if (!listaComentarios) return;

    formComentario.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        contenedorErrores.innerHTML = ''; // Limpiamos
        const errores = [];

        const nombre = document.getElementById('nombre_comentario').value.trim();
        const texto = document.getElementById('texto_comentario').value.trim();
        const avistamientoId = Number(document.getElementById('avistamiento_id').value);

        if (nombre.length < 3 || nombre.length > 50) {
            errores.push("El nombre debe tener entre 3 y 50 caracteres.");
        }
        if (texto.length < 5) {
            errores.push("El comentario debe tener al menos 5 caracteres.");
        }
        if (!Number.isInteger(avistamientoId) || avistamientoId < 1) {
            errores.push("No se pudo identificar el avistamiento.");
        }

        if (errores.length > 0) {
            mostrarErrores(errores);
            return;
        }

        // Llamada asíncrona usando Fetch
        try {
            const response = await fetch('/api/comentarios', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    nombre: nombre,
                    texto: texto,
                    avistamiento_id: avistamientoId
                })
            });

            const data = await response.json();

            if (!response.ok) {
                mostrarErrores(data.mensajes || ["Error al procesar el comentario"]);
            } else {
                if (msjSinComentarios) {
                    msjSinComentarios.style.display = 'none';
                }

                const li = document.createElement('li');
                li.className = 'comentario-item';
                const header = document.createElement('div');
                header.className = 'comentario-header';
                const nombreElemento = document.createElement('strong');
                nombreElemento.textContent = data.comentario.nombre;
                const fechaElemento = document.createElement('span');
                fechaElemento.textContent = data.comentario.fecha;
                const textoElemento = document.createElement('p');
                textoElemento.className = 'comentario-texto';
                textoElemento.textContent = data.comentario.texto;
                header.append(nombreElemento, fechaElemento);
                li.append(header, textoElemento);
                listaComentarios.appendChild(li);

                formComentario.reset();
            }
        } catch (error) {
            console.error("Error al enviar comentario:", error);
            mostrarErrores(["Error de conexión. Intente nuevamente."]);
        }
    });

    // Función para renderizar los errores
    function mostrarErrores(erroresArreglo) {
        const div = document.createElement('div');
        div.className = 'alerta-error';

        const ul = document.createElement('ul');
        erroresArreglo.forEach(err => {
            const li = document.createElement('li');
            li.textContent = err;
            ul.appendChild(li);
        });

        div.appendChild(ul);
        contenedorErrores.appendChild(div);
    }
});