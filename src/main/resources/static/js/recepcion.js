// Formulario de recepcion: agregar y quitar lineas, total en unidades.

const tabla = document.getElementById("lineas");

function calcular() {
  let total = 0;
  tabla.querySelectorAll(".linea").forEach(fila => {
    const empaque = fila.querySelector(".sel-empaque").value;
    const contenido = fila.querySelector(".contenido");
    if (empaque === "Unidad") contenido.value = 1;
    contenido.readOnly = empaque === "Unidad";
    const unidades = (Number(contenido.value) || 0) * (Number(fila.querySelector(".cantidad").value) || 0);
    fila.querySelector(".total-linea").textContent = unidades;
    total += unidades;
  });
  document.getElementById("total-unidades").textContent = total;
}

// Al elegir un producto se sugiere su empaque, unidades por empaque y costo
function sugerir(fila) {
  const opcion = fila.querySelector(".sel-producto").selectedOptions[0];
  if (!opcion) return;
  const contenido = Number(opcion.dataset.contenido) || 1;
  fila.querySelector(".sel-empaque").value = opcion.dataset.empaque;
  fila.querySelector(".contenido").value = contenido;
  fila.querySelector("input[name=costoEmpaque]").value = (Number(opcion.dataset.costo) * contenido).toFixed(2);
  calcular();
}

function agregarFila() {
  const fila = document.getElementById("fila-modelo").content.firstElementChild.cloneNode(true);
  tabla.appendChild(fila);
  sugerir(fila);
}

tabla.addEventListener("input", calcular);
tabla.addEventListener("change", e => {
  if (e.target.classList.contains("sel-producto")) sugerir(e.target.closest("tr"));
  calcular();
});
tabla.addEventListener("click", e => {
  if (e.target.classList.contains("quitar")) {
    e.target.closest("tr").remove();
    calcular();
  }
});
document.getElementById("agregar").addEventListener("click", agregarFila);

if (tabla.querySelectorAll(".linea").length === 0) agregarFila();
calcular();
