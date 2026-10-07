document.addEventListener('DOMContentLoaded', () => {
    
    // Llamada asíncrona (fetch) para obtener los datos desde Flask
    fetch('/api/estadisticas')
        .then(response => response.json())
        .then(data => {
            
            // Gráfico de Avistamientos por Día (Líneas)
            const ctxDia = document.getElementById('chart-avistamientos-dia')?.getContext('2d');
            if (ctxDia) {
                new Chart(ctxDia, {
                    type: 'line',
                    data: {
                        labels: data.dias.labels,
                        datasets: [{
                            label: 'Avistamientos',
                            data: data.dias.valores,
                            borderColor: '#2ecc71',
                            backgroundColor: 'rgba(46, 204, 113, 0.15)',
                            fill: true,
                            tension: 0.3
                        }]
                    },
                    options: { responsive: true, scales: { y: { beginAtZero: true, ticks: { stepSize: 1 } } } }
                });
            }

            // Gráfico de Avistamientos por Tipo de Ave (Torta)
            const ctxAve = document.getElementById('chart-avistamientos-ave')?.getContext('2d');
            if (ctxAve) {
                new Chart(ctxAve, {
                    type: 'pie',
                    data: {
                        labels: data.aves.labels,
                        datasets: [{
                            data: data.aves.valores,
                            backgroundColor: ['#e74c3c', '#3498db', '#f1c40f', '#9b59b6', '#e67e22', '#1abc9c']
                        }]
                    },
                    options: { responsive: true }
                });
            }

            // Gráfico de Voluntarios por Comuna (Barras)
            const ctxComuna = document.getElementById('chart-voluntarios-comuna')?.getContext('2d');
            if (ctxComuna) {
                new Chart(ctxComuna, {
                    type: 'bar',
                    data: {
                        labels: data.comunas.labels,
                        datasets: [{
                            label: 'Voluntarios Registrados',
                            data: data.comunas.valores,
                            backgroundColor: '#3498db',
                            borderColor: '#2980b9',
                            borderWidth: 1
                        }]
                    },
                    options: {
                        responsive: true,
                        plugins: { legend: { display: false } },
                        scales: { y: { beginAtZero: true, ticks: { stepSize: 1 } } }
                    }
                });
            }

        })
        .catch(error => {
            console.error("Error al cargar las estadísticas:", error);
        });
});