// Vista previa de la boleta: cada cambio del formulario pide al servidor
// la boleta con el diseno nuevo (sin guardarlo) y la reemplaza en pantalla.

const formBoleta = document.getElementById("form-boleta");

function actualizarVistaPrevia() {
  fetch("comprobantes/vista-previa", { method: "POST", body: new FormData(formBoleta) })
    .then(r => r.text())
    .then(html => { document.getElementById("vista-previa").outerHTML = html; });
}

formBoleta.addEventListener("change", actualizarVistaPrevia);
formBoleta.addEventListener("input", e => {
  if (e.target.type === "text") actualizarVistaPrevia();
});
