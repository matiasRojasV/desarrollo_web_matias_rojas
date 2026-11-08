document.addEventListener('DOMContentLoaded', () => {
    const input = document.getElementById('texto-busqueda');
    const resultados = document.getElementById('resultados-busqueda');
    const mensaje = document.getElementById('mensaje-busqueda');
    let solicitud;

    input.addEventListener('input', async () => {
        const texto = input.value.trim();
        resultados.replaceChildren();
        mensaje.textContent = '';

        if (!texto) return;

        if (solicitud) solicitud.abort();
        solicitud = new AbortController();

        try {
            const response = await fetch(`/api/avistamientos/buscar?q=${encodeURIComponent(texto)}`, {
                signal: solicitud.signal
            });
            if (!response.ok) throw new Error('No fue posible realizar la búsqueda.');

            const data = await response.json();
            if (!data.avistamientos.length) {
                mensaje.textContent = 'No se encontraron avistamientos para ese patrón.';
                return;
            }

            data.avistamientos.forEach((avistamiento) => {
                resultados.appendChild(crearResultado(avistamiento, texto));
            });
        } catch (error) {
            if (error.name !== 'AbortError') {
                mensaje.textContent = 'No fue posible realizar la búsqueda. Intenta nuevamente.';
            }
        }
    });

    function crearResultado(avistamiento, texto) {
        const article = document.createElement('article');
        article.className = 'resultado-avistamiento';
        article.innerHTML = `
            <h3><a href="${avistamiento.detalle_url}">${resaltar(avistamiento.ave, texto)}</a></h3>
            <p><strong>Voluntario:</strong> ${resaltar(avistamiento.voluntario, texto)}</p>
            <p><strong>Fecha:</strong> ${avistamiento.fecha}</p>
            <p><strong>Comuna:</strong> ${resaltar(avistamiento.comuna, texto)}</p>
            <p><strong>Nota promedio:</strong> <span class="promedio">${avistamiento.promedio ?? '-'}</span></p>
            <button type="button" class="btn-evaluar">Evaluar</button>
            <div class="form-evaluacion" hidden>
                <label>Nota (1 a 7)
                    <select class="selector-nota">
                        <option value="">Selecciona una nota</option>
                        ${[1, 2, 3, 4, 5, 6, 7].map((nota) => `<option value="${nota}">${nota}</option>`).join('')}
                    </select>
                </label>
                <button type="button" class="btn-guardar-nota">Guardar nota</button>
                <p class="mensaje-nota" role="alert"></p>
            </div>
        `;

        article.querySelector('.btn-evaluar').addEventListener('click', () => {
            article.querySelector('.form-evaluacion').hidden = false;
        });
        article.querySelector('.btn-guardar-nota').addEventListener('click', () => guardarNota(article, avistamiento.id));
        return article;
    }

    async function guardarNota(article, avistamientoId) {
        const selector = article.querySelector('.selector-nota');
        const mensajeNota = article.querySelector('.mensaje-nota');
        const valor = Number(selector.value);
        mensajeNota.textContent = '';

        if (!Number.isInteger(valor) || valor < 1 || valor > 7) {
            mensajeNota.textContent = 'Selecciona una nota entera entre 1 y 7.';
            return;
        }

        try {
            const response = await fetch('/api/notas', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({avistamiento_id: avistamientoId, nota: valor})
            });
            const data = await response.json();
            if (!response.ok) {
                mensajeNota.textContent = data.mensaje || 'No fue posible guardar la nota.';
                return;
            }
            article.querySelector('.promedio').textContent = data.promedio;
            mensajeNota.textContent = 'Nota guardada correctamente.';
            selector.value = '';
        } catch (error) {
            mensajeNota.textContent = 'No fue posible guardar la nota. Intenta nuevamente.';
        }
    }

    function resaltar(valor, patron) {
        const valorSeguro = escapeHtml(valor);
        const patronSeguro = escapeHtml(patron);
        const escapado = patronSeguro.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
        return valorSeguro.replace(new RegExp(`(${escapado})`, 'gi'), '<mark>$1</mark>');
    }

    function escapeHtml(valor) {
        return valor.replace(/[&<>"']/g, (caracter) => ({
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#039;'
        })[caracter]);
    }
});
