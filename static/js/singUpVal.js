document.addEventListener('DOMContentLoaded', async () => {
    const regionSelect = document.getElementById('region');
    const comunaSelect = document.getElementById('comuna');
    const form = document.getElementById('form-voluntario');
    const contenedorErrores = document.getElementById('mensajes-error');

    let RegionesYcomunas = { regiones: [] };

    // Evento para actualizar el selector de Comunas
    regionSelect.addEventListener('change', () => {
        const nombreRegion = regionSelect.value;
        
        // Limpiar select de comuna
        comunaSelect.innerHTML = '<option value=""> Seleccione una comuna </option>';

        if (nombreRegion) {
            // Buscar el objeto de la región seleccionada
            const regionEncontrada = RegionesYcomunas.regiones.find(
                (reg) => reg.NombreRegion === nombreRegion
            );

            if (regionEncontrada && regionEncontrada.comunas) {
                regionEncontrada.comunas.forEach((comuna) => {
                    const option = document.createElement('option');
                    option.value = comuna;
                    option.textContent = comuna;
                    comunaSelect.appendChild(option);
                });
                comunaSelect.disabled = false;
            } else {
                comunaSelect.disabled = true;
            }
        } else {
            comunaSelect.disabled = true;
        }
    });

    // Validaciones del formulario
    form.addEventListener('submit', (event) => {
        contenedorErrores.innerHTML = '';
        const errores = [];

        const nombre = document.getElementById('nombre').value.trim();
        const email = document.getElementById('email').value.trim();
        const celular = document.getElementById('celular').value.trim();
        const region = regionSelect.value;
        const comuna = comunaSelect.value;

        // Validación Nombre
        if (nombre === '' || nombre.length < 3) {
            errores.push('El nombre completo es obligatorio (mínimo 3 caracteres).');
        }

        // Validación Email
        const regexEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!regexEmail.test(email)) {
            errores.push('Ingrese un formato de correo electrónico válido.');
        }

        // Validación Celular Chile
        const regexCelular = /^(\+?56)?9\d{8}$/;
        if (!regexCelular.test(celular)) {
            errores.push('El celular debe ser válido para Chile (ej: 912345678 o +56912345678).');
        }

        // Validación Región y Comuna
        if (region === '') {
            errores.push('Debe seleccionar una Región.');
        }

        if (comuna === '') {
            errores.push('Debe seleccionar una Comuna.');
        }

        // Renderizado de errores o confirmación
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
            // Mensaje de confirmación
            alert(`¡Bienvenido/a ${nombre}! Voluntario registrado e identificado con éxito.`);

            // Resetear formulario
            form.reset();
            comunaSelect.disabled = true;
            comunaSelect.innerHTML = '<option value=""> Seleccione primero una región </option>';

            // Redirigir a la página de principal
            window.location.href = 'index.html';
        }
    });
});