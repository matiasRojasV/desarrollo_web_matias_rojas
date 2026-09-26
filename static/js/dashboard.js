document.addEventListener('DOMContentLoaded', () => {
    
    // 1. Gráfico de Voluntarios Registrados por Región (Gráfico de Barras)
    const ctxVoluntarios = document.getElementById('chart-voluntarios-region')?.getContext('2d');
    if (ctxVoluntarios) {
        new Chart(ctxVoluntarios, {
            type: 'bar',
            data: {
                labels: [
                    'Metropolitana',
                    'Valparaíso',
                    'Biobío',
                    'Araucanía',
                    'Coquimbo',
                    'Los Lagos'
                ],
                datasets: [{
                    label: 'Voluntarios Registrados',
                    data: [52, 34, 27, 19, 14, 10],
                    backgroundColor: '#3498db',
                    borderColor: '#2980b9',
                    borderWidth: 1,
                    borderRadius: 4
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { display: false }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: { stepSize: 10 }
                    }
                }
            }
        });
    }

    // 2. Gráfico de Avistamientos Registrados (Gráfico de Línea / Histórico)
    const ctxAvistamientos = document.getElementById('chart-avistamientos-registrados')?.getContext('2d');
    if (ctxAvistamientos) {
        new Chart(ctxAvistamientos, {
            type: 'line',
            data: {
                labels: ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio', 'Agosto'],
                datasets: [{
                    label: 'Avistamientos Registrados',
                    data: [15, 28, 42, 35, 50, 68, 75, 90],
                    borderColor: '#2ecc71',
                    backgroundColor: 'rgba(46, 204, 113, 0.15)',
                    fill: true,
                    tension: 0.3,
                    pointRadius: 5,
                    pointBackgroundColor: '#27ae60'
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { display: false }
                },
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });
    }

});