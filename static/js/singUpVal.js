document.addEventListener('DOMContentLoaded', async () => {
    const regionSelect = document.getElementById('region');
    const comunaSelect = document.getElementById('comuna');
    const form = document.getElementById('form-voluntario');
    const contenedorErrores = document.getElementById('mensajes-error');

    // Evento para actualizar el selector de Comunas
    regionSelect.addEventListener('change', async () => {
        const regionId = regionSelect.value;
        
        // Limpiar select de comuna
        comunaSelect.innerHTML = '<option value=""> Seleccione una comuna </option>';
        comunaSelect.disabled = true;

        if (regionId) {
            try {
                const response = await fetch(`/get_comunas/${regionId}`);
                const data = await response.json();

                if (data.comunas && data.comunas.length > 0) {
                    data.comunas.forEach((comuna) => {
                        const option = document.createElement('option');
                        option.value = comuna.id;
                        option.textContent = comuna.nombre; // Mostramos el nombre
                        comunaSelect.appendChild(option);
                    });
                    comunaSelect.disabled = false;
                }
            } catch (error) {
                console.error("Error al cargar las comunas:", error);
            }
        }
    });

    // Validaciones del formulario
    form.addEventListener('submit', (event) => {
        event.preventDefault();
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
            form.submit();
            // Mensaje de confirmación
            alert(`¡Bienvenido/a ${nombre}! Voluntario registrado e identificado con éxito.`);
        }
    });
});