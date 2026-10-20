document.addEventListener('DOMContentLoaded', () => {
    const formComentario = document.getElementById('form-comentario');
    const contenedorErrores = document.getElementById('errores-comentario');
    const listaComentarios = document.getElementById('lista-comentarios');
    const msjSinComentarios = document.getElementById('sin-comentarios');

    if (!formComentario) return;

    formComentario.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        contenedorErrores.innerHTML = ''; // Limpiamos
        const errores = [];

        const nombre = document.getElementById('nombre_comentario').value.trim();
        const texto = document.getElementById('texto_comentario').value.trim();
        const avistamientoId = document.getElementById('avistamiento_id').value;

        if (nombre.length < 3 || nombre.length > 50) {
            errores.push("El nombre debe tener entre 3 y 50 caracteres.");
        }
        if (texto.length < 5) {
            errores.push("El comentario debe tener al menos 5 caracteres.");
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
                // Si Flask nos devolvió un error
                mostrarErrores(data.mensajes || ["Error al procesar el comentario"]);
            } else {
                // Éxito: Ocultamos el mensaje de "sin comentarios"
                if (msjSinComentarios) {
                    msjSinComentarios.style.display = 'none';
                }

                // Creamos el nuevo comentario en el DOM y lo agregamos al final de la lista
                const li = document.createElement('li');
                li.className = 'comentario-item';
                li.innerHTML = `
                    <div class="comentario-header">
                        <strong>${data.comentario.nombre}</strong>
                        <span>${data.comentario.fecha}</span>
                    </div>
                    <p class="comentario-texto">${data.comentario.texto}</p>
                `;
                listaComentarios.appendChild(li);

                // Limpiamos el formulario para futuros comentarios
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