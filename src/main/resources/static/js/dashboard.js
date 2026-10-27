// Graficos del dashboard. Los datos vienen del servidor en la variable "datos".

const AZUL = "#2a78d6";
const COLORES = ["#2a78d6", "#eb6834", "#1baf7a", "#eda100", "#e87ba4", "#7a5cd6"];
const oscuro = document.documentElement.dataset.tema === "oscuro";
Chart.defaults.color = oscuro ? "#9aa3af" : "#6b7280";
const grilla = oscuro ? "#2a2f39" : "#e1e0d9";

function opcionesBase(horizontal) {
  return {
    responsive: true, maintainAspectRatio: false, indexAxis: horizontal ? "y" : "x",
    plugins: { legend: { display: false } },
    scales: { x: { grid: { display: horizontal, color: grilla } }, y: { grid: { display: !horizontal, color: grilla }, beginAtZero: true } }
  };
}

function barras(id, lista, horizontal) {
  new Chart(document.getElementById(id), {
    type: "bar",
    data: { labels: lista.map(x => x.etiqueta), datasets: [{ data: lista.map(x => x.valor), backgroundColor: AZUL, borderRadius: 4, maxBarThickness: 36 }] },
    options: opcionesBase(horizontal)
  });
}

function dona(id, etiquetas, valores, colores) {
  new Chart(document.getElementById(id), {
    type: "doughnut",
    data: { labels: etiquetas, datasets: [{ data: valores, backgroundColor: colores, borderWidth: 2 }] },
    options: { responsive: true, maintainAspectRatio: false, cutout: "62%", plugins: { legend: { position: "bottom" } } }
  });
}

new Chart(document.getElementById("g-evolucion"), {
  type: "line",
  data: {
    labels: datos.evolucion.map(x => x.dia),
    datasets: [{ data: datos.evolucion.map(x => x.total), borderColor: AZUL, backgroundColor: "rgba(42,120,214,0.08)", fill: true, tension: 0.3, pointRadius: 0 }]
  },
  options: opcionesBase(false)
});
barras("g-sedes", datos.sedes, false);
dona("g-categorias", datos.categorias.map(x => x.etiqueta), datos.categorias.map(x => x.valor), COLORES);
barras("g-top", datos.top, true);
dona("g-pagos", datos.pagos.map(x => x.etiqueta), datos.pagos.map(x => x.valor), COLORES);
barras("g-vendedores", datos.vendedores, false);
dona("g-inventario", ["Con stock", "Stock bajo", "Sin stock"], datos.inventario, ["#0ca30c", "#fab219", "#d03b3b"]);
