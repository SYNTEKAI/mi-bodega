// Funciones comunes de Mi Bodega

// Pide confirmacion antes de enviar los formularios que tienen data-confirmar
document.addEventListener("submit", function (e) {
  const form = e.target;
  if (form.dataset.confirmar && !confirm(form.dataset.confirmar)) {
    e.preventDefault();
  }
});

// Formato de soles: S/ 1,234.50
function soles(monto) {
  return "S/ " + Number(monto || 0).toLocaleString("en-US", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}
