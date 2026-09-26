document.addEventListener('DOMContentLoaded', () => {
    const filtroTipo = document.getElementById('filtro-tipo');
    const ordenarPor = document.getElementById('ordenar-por');
    const contenedorLista = document.querySelector('.lista-avistamientos');
    const tarjetas = Array.from(document.querySelectorAll('.tarjeta-ave'));
    const btnAnterior = document.getElementById('btn-anterior');
    const btnSiguiente = document.getElementById('btn-siguiente');
    const paginacionInfo = document.getElementById('paginacion-info');

    const elementosPorPagina = 3;
    let paginaActual = 1;
    let totalPaginas = 1;

    // Capturar y estructurar la información de cada tarjeta visible
    const datosTarjetas = tarjetas.map((tarjeta) => {
        const parrafos = tarjeta.querySelectorAll('p');
        let tipoText = '', lugarText = '', fechaText = '';

        parrafos.forEach((p) => {
            const texto = p.textContent;
            if (texto.includes('Tipo:')) tipoText = texto.replace('Tipo:', '').trim();
            if (texto.includes('Lugar:')) lugarText = texto.replace('Lugar:', '').trim();
            if (texto.includes('Fecha y Hora:')) fechaText = texto.replace('Fecha y Hora:', '').trim();
        });

        // Extraer componentes de la fecha "DD/MM/YYYY - HH:mm hrs"
        const parteFecha = fechaText.split(' - ')[0]; // "18/08/2026"
        const [dia, mes, anio] = parteFecha.split('/');
        
        // Formato ISO "YYYY-MM-DD" para comparar con el input de tipo date
        const fechaISO = `${anio}-${mes.padStart(2, '0')}-${dia.padStart(2, '0')}`;
        const fechaObj = new Date(`${fechaISO}T00:00:00`);

        return {
            elemento: tarjeta,
            tipo: tipoText.toLowerCase(),
            lugarOriginal: lugarText,
            fechaObj: fechaObj
        };
    });

    // Obtiene la lista filtrada y ordenada según controles del formulario.
    function obtenerFiltradosYOrdenados() {
        const valTipo = filtroTipo?.value.toLowerCase() || '';
        const valOrden = ordenarPor?.value || 'fecha-desc';

        const filtrados = datosTarjetas.filter((item) => {
            const coincideTipo = !valTipo || item.tipo.includes(valTipo);
            return coincideTipo;
        });

        filtrados.sort((a, b) => {
            if (valOrden === 'fecha-desc') {
                return b.fechaObj - a.fechaObj;
            } else if (valOrden === 'fecha-asc') {
                return a.fechaObj - b.fechaObj;
            } else if (valOrden === 'lugar') {
                return a.lugarOriginal.localeCompare(b.lugarOriginal, 'es');
            }
            return 0;
        });

        return filtrados;
    }

    // Renderiza una página del resultado actual y actualiza el estado de los botones.
    function renderizarPagina(filtrados) {
        totalPaginas = Math.max(1, Math.ceil(filtrados.length / elementosPorPagina));

        if (paginaActual > totalPaginas) {
            paginaActual = totalPaginas;
        }

        const inicio = (paginaActual - 1) * elementosPorPagina;
        const fin = inicio + elementosPorPagina;
        const pagina = filtrados.slice(inicio, fin);

        datosTarjetas.forEach((item) => {
            item.elemento.style.display = 'none';
        });

        pagina.forEach((item) => {
            item.elemento.style.display = ''; 
            contenedorLista.appendChild(item.elemento);
        });

        paginacionInfo.textContent = `Pagina ${paginaActual} de ${totalPaginas}`;
        btnAnterior.disabled = paginaActual === 1;
        btnSiguiente.disabled = paginaActual === totalPaginas;

        if (filtrados.length === 0) {
            paginacionInfo.textContent = 'Pagina 0 de 0';
            btnAnterior.disabled = true;
            btnSiguiente.disabled = true;
        }
    }

    function aplicarFiltrosOrdenYPaginacion() {
        const filtrados = obtenerFiltradosYOrdenados();
        renderizarPagina(filtrados);
    }

    filtroTipo?.addEventListener('change', () => {
        paginaActual = 1;
        aplicarFiltrosOrdenYPaginacion();
    });

    ordenarPor?.addEventListener('change', () => {
        paginaActual = 1;
        aplicarFiltrosOrdenYPaginacion();
    });

    btnAnterior?.addEventListener('click', () => {
        if (paginaActual > 1) {
            paginaActual -= 1;
            aplicarFiltrosOrdenYPaginacion();
        }
    });

    btnSiguiente?.addEventListener('click', () => {
        if (paginaActual < totalPaginas) {
            paginaActual += 1;
            aplicarFiltrosOrdenYPaginacion();
        }
    });

    aplicarFiltrosOrdenYPaginacion();
});