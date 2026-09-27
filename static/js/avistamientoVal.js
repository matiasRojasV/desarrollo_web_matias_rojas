document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('form-avistamiento');
    const contenedorErrores = document.getElementById('mensajes-error');

    if (!form) return;
    
    form.addEventListener('submit', (event) => {
        event.preventDefault();

        contenedorErrores.innerHTML = '';
        const errores = [];

        const aveIdElem = document.getElementById('ave_id');
        const lugarElem = document.getElementById('lugar');
        const fechaElem = document.getElementById('fecha');
        const horaElem = document.getElementById('hora');
        const multimediaElem = document.getElementById('multimedia');

        const aveId = aveIdElem ? aveIdElem.value : '';
        const lugar = lugarElem ? lugarElem.value.trim() : '';
        const fecha = fechaElem ? fechaElem.value : '';
        const hora = horaElem ? horaElem.value : '';
        const multimedia = multimediaElem && multimediaElem.files ? multimediaElem.files[0] : null;

        if (!aveId) {
            errores.push('Debe seleccionar un ave de la lista.');
        }

        if (!lugar) {
            errores.push('El lugar del avistamiento es obligatorio.');
        }

        if (!fecha || !hora) {
            errores.push('Debe seleccionar tanto la fecha como la hora del avistamiento.');
        }

        if (!multimedia) {
            errores.push('Debe adjuntar una foto o video del avistamiento.');
        }

        if (errores.length > 0) {
            const ul = document.createElement('ul');
            ul.style.color = 'red';
            errores.forEach(err => {
                const li = document.createElement('li');
                li.textContent = err;
                ul.appendChild(li);
            });
            contenedorErrores.appendChild(ul);
        } else {
            event.target.submit();
        }
    });
});