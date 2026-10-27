// Punto de venta: carrito, busqueda de productos, cliente y vuelto.
// El servidor vuelve a validar el stock y los precios al guardar.

const formVenta = document.getElementById("form-venta");
let carrito = []; // { id, nombre, precio, stock, minimo, cantidad }

function productoDe(div) {
  return { id: Number(div.dataset.id), nombre: div.dataset.nombre, codigo: div.dataset.codigo,
           precio: Number(div.dataset.precio), stock: Number(div.dataset.stock), minimo: Number(div.dataset.minimo) };
}

function agregar(p) {
  if (p.stock <= 0) { alert(p.nombre + ": producto sin stock"); return; }
  const linea = carrito.find(c => c.id === p.id);
  if (linea) {
    if (linea.cantidad + 1 > p.stock) { alert("Stock disponible: " + p.stock); return; }
    linea.cantidad++;
  } else {
    carrito.push({ ...p, cantidad: 1 });
  }
  dibujar();
}

function cambiar(id, cantidad) {
  const linea = carrito.find(c => c.id === id);
  if (!linea) return;
  cantidad = Math.floor(Number(cantidad));
  if (isNaN(cantidad)) return;
  if (cantidad <= 0) carrito = carrito.filter(c => c.id !== id);
  else linea.cantidad = cantidad;
  dibujar();
}

const total = () => Math.round(carrito.reduce((s, c) => s + c.cantidad * c.precio, 0) * 100) / 100;
const metodo = () => formVenta.querySelector("input[name=metodoPago]:checked").value;

function dibujar() {
  const div = document.getElementById("carrito");
  if (carrito.length === 0) {
    div.innerHTML = '<p class="texto-suave">Carrito vacío. Haz clic en un producto para agregarlo.</p>';
  } else {
    let html = "<table><tbody>";
    carrito.forEach(c => {
      const excede = c.cantidad > c.stock;
      const quedan = c.stock - c.cantidad;
      html += `<tr class="${excede ? "linea-excede" : ""}"><td>${c.nombre}`
        + (excede ? `<br><span class="texto-error">Stock disponible: ${c.stock}</span>`
                  : (quedan <= c.minimo ? `<br><span class="aviso-stock-bajo">Stock bajo: quedarán ${quedan}</span>` : ""))
        + `</td><td class="celda-cantidad">
            <button type="button" class="btn btn-mini" onclick="cambiar(${c.id}, ${c.cantidad - 1})">-</button>
            <input type="number" class="input-cantidad" min="1" value="${c.cantidad}" onchange="cambiar(${c.id}, this.value)">
            <button type="button" class="btn btn-mini" onclick="cambiar(${c.id}, ${c.cantidad + 1})">+</button>
          </td><td class="alinear-derecha sin-salto">${soles(c.cantidad * c.precio)}</td></tr>`;
    });
    div.innerHTML = html + "</tbody></table>";
  }
  // Campos ocultos que se envian al servidor
  document.getElementById("campos-carrito").innerHTML = carrito.map(c =>
    `<input type="hidden" name="productoId" value="${c.id}"><input type="hidden" name="cantidad" value="${c.cantidad}">`).join("");
  document.getElementById("total").innerHTML = carrito.length ? "TOTAL: <strong>" + soles(total()) + "</strong>" : "";
  panelEfectivo();
}

function panelEfectivo() {
  const panel = document.getElementById("panel-efectivo");
  const esEfectivo = metodo() === "EFECTIVO";
  panel.style.display = esEfectivo && carrito.length ? "" : "none";
  const t = total();
  document.getElementById("billetes").innerHTML = `<button type="button" class="btn btn-mini" onclick="fijar(${t})">Exacto</button> `
    + [10, 20, 50, 100, 200].filter(b => b >= t).slice(0, 3).map(b => `<button type="button" class="btn btn-mini" onclick="fijar(${b})">S/ ${b}</button>`).join(" ");
  const recibido = Number(document.getElementById("recibido").value) || 0;
  const vuelto = Math.round((recibido - t) * 100) / 100;
  document.getElementById("vuelto").textContent = recibido > 0 && vuelto >= 0 ? soles(vuelto) : "-";
  document.getElementById("error-efectivo").textContent = recibido > 0 && vuelto < 0 ? "El monto recibido es menor al total." : "";

  let valido = carrito.length > 0 && carrito.every(c => c.cantidad <= c.stock);
  if (valido && esEfectivo) valido = recibido >= t;
  document.getElementById("cobrar").disabled = !valido;
}

function fijar(monto) {
  document.getElementById("recibido").value = Number(monto).toFixed(2);
  panelEfectivo();
}

if (formVenta) {
  // Clic en un producto de la lista
  document.querySelectorAll(".item-producto").forEach(div => div.addEventListener("click", () => agregar(productoDe(div))));

  // Busqueda por nombre o codigo; Enter agrega (lector de codigo de barras)
  const buscar = document.getElementById("buscar");
  buscar.addEventListener("input", () => {
    const texto = buscar.value.toLowerCase();
    document.querySelectorAll(".item-producto").forEach(div => {
      const p = productoDe(div);
      div.style.display = p.nombre.toLowerCase().includes(texto) || p.codigo.includes(texto) ? "" : "none";
    });
  });
  buscar.addEventListener("keydown", e => {
    if (e.key !== "Enter") return;
    e.preventDefault();
    const texto = buscar.value.trim();
    const div = [...document.querySelectorAll(".item-producto")].find(d => d.dataset.codigo === texto)
             || [...document.querySelectorAll(".item-producto")].find(d => d.style.display !== "none");
    if (div) { agregar(productoDe(div)); buscar.value = ""; buscar.dispatchEvent(new Event("input")); }
  });

  // Medio de pago
  formVenta.querySelectorAll("input[name=metodoPago]").forEach(r => r.addEventListener("change", () => {
    formVenta.querySelectorAll(".medios-pago .chip").forEach(ch => ch.classList.remove("activo"));
    r.parentElement.classList.add("activo");
    panelEfectivo();
  }));
  document.getElementById("recibido").addEventListener("input", panelEfectivo);

  // Cliente por DNI/RUC
  const doc = document.getElementById("cliente-doc");
  doc.addEventListener("input", buscarCliente);
  dibujar();
}

function buscarCliente() {
  const documento = document.getElementById("cliente-doc").value.trim();
  const ficha = document.getElementById("cliente-ficha");
  document.getElementById("id-cliente").value = "";
  if (!/^\d{8}$|^\d{11}$/.test(documento)) {
    ficha.innerHTML = documento ? '<p class="texto-suave">DNI 8 dígitos, RUC 11 dígitos.</p>' : "";
    return;
  }
  fetch("ventas/cliente?documento=" + documento).then(r => r.json()).then(c => {
    if (c.encontrado) {
      document.getElementById("id-cliente").value = c.id;
      ficha.innerHTML = `<div class="ficha-cliente-pos"><div><strong>${c.nombre}</strong><br><span class="texto-suave">${c.documento} - ${c.compras} compras</span></div></div>`;
    } else if (c.puedeCrear) {
      ficha.innerHTML = `<div class="ficha-cliente-pos nuevo"><span class="texto-suave">Cliente no registrado.</span>
        <input type="text" id="nuevo-nombre" placeholder="Nombre o razón social">
        <button type="button" class="btn btn-mini" onclick="registrarCliente('${documento}')">Registrar</button></div>`;
    } else {
      ficha.innerHTML = '<p class="texto-suave">Cliente no registrado (venta a cliente varios).</p>';
    }
  });
}

function registrarCliente(documento) {
  const nombre = document.getElementById("nuevo-nombre").value.trim();
  if (!nombre) { alert("Escribe el nombre del cliente."); return; }
  const datos = new URLSearchParams({ documento, nombre });
  fetch("ventas/cliente", { method: "POST", body: datos }).then(r => r.json()).then(c => {
    if (c.error) { alert(c.error); return; }
    buscarCliente();
  });
}
