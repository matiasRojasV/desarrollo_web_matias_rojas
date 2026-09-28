document.addEventListener('DOMContentLoaded', () => {
    const authContainer = document.getElementById('auth-container');

    if (!authContainer) return;

    // Verificar si existe la sesión en localStorage
    const estaRegistrado = localStorage.getItem('voluntarioRegistrado');
    const nombreVoluntario = localStorage.getItem('nombreVoluntario');

    if (estaRegistrado === 'true' && nombreVoluntario) {
        // Si está logueado: muestra el saludo y botón
        authContainer.innerHTML = `
            <span class="user-welcome">Hola, <strong>${nombreVoluntario}</strong></span>
            <button id="btn-logout" class="btn-logout" title="Cerrar Sesión">Cerrar Sesión</button>
        `;

        // Evento para cerrar sesión
        const btnLogout = document.getElementById('btn-logout');
        btnLogout.addEventListener('click', () => {
            localStorage.removeItem('voluntarioRegistrado');
            localStorage.removeItem('nombreVoluntario');
            alert('Sesión cerrada exitosamente.');
            window.location.href = "/";
        });
    } else {
        // Si no está logueado
        authContainer.innerHTML = `<a href="/login" class="auth-link">Registro / Login</a>`;
    }
});