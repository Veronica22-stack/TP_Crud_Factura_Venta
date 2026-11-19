import { FacturaService } from './services/facturaService';
import {
  FacturaVentaDTO,
  FacturaVentaDetalleDTO,
  FacturaRequestDTO,
  OpcionesComboDTO
} from './models/factura.dto';
import { ClienteService } from './services/clienteService';
import { ListaPrecioDTO, ListaPrecioArticuloDTO } from './types/listaPrecioArticulo';

// Estado del formulario
let modoEdicion = false;
let facturaIdEdicion: number | null = null;
let numeroFacturaParam = '';
let siguienteDetalleId = 1;
let puntoVentaPorDefectoId: number | undefined;
let listaPrecioSeleccionada: ListaPrecioDTO | null = null;
// Productos de cada lista de precios ya consultada (id de lista -> productos).
// Cada ítem usa el catálogo de SU lista, así se pueden mezclar ítems de listas distintas.
const catalogosPorLista = new Map<number, ListaPrecioArticuloDTO[]>();
let listasDePrecio: ListaPrecioDTO[] = [];
let opcionesDisponibles: OpcionesComboDTO | null = null;
let detalles: FacturaVentaDetalleDTO[] = [];

// Elementos del DOM
const formTitulo = document.getElementById('formTitulo') as HTMLElement;
const facturaForm = document.getElementById('facturaForm') as HTMLFormElement;
const numeroFacturaInput = document.getElementById('numeroFacturaInput') as HTMLInputElement;
const fechaEmisionInput = document.getElementById('fechaEmisionInput') as HTMLInputElement;
const clienteSelect = document.getElementById('clienteSelect') as HTMLSelectElement;
const clienteTexto = document.getElementById('clienteTexto') as HTMLInputElement;
const condicionIvaSelect = document.getElementById('condicionIvaSelect') as HTMLSelectElement;
const tipoComprobanteSelect = document.getElementById('tipoComprobanteSelect') as HTMLSelectElement;
const monedaSelect = document.getElementById('monedaSelect') as HTMLSelectElement;
const ivaResumen = document.getElementById('ivaResumen') as HTMLElement;
const btnAgregarItem = document.getElementById('btnAgregarItem') as HTMLButtonElement;
const tbodyDetalles = document.getElementById('detallesTableBody') as HTMLTableSectionElement;
const importeTotalDisplay = document.getElementById('importeTotalDisplay') as HTMLElement;
const btnCancelar = document.getElementById('btnCancelar') as HTMLButtonElement;

const modalClientes = document.getElementById('modalClientes') as HTMLElement;
const btnAbrirExplorer = document.getElementById('btnAbrirExplorer') as HTMLButtonElement;
const btnCerrarModal = document.getElementById('btnCerrarModal') as HTMLButtonElement;
const inputBuscarCliente = document.getElementById('inputBuscarCliente') as HTMLInputElement;
const tbodyClientes = document.getElementById('clientesTableBody') as HTMLTableSectionElement;

function crearModalListasPrecio(): void {
  document.body.insertAdjacentHTML('beforeend', `
    <div id="modalListasPrecio" class="modal-overlay hidden">
      <div class="modal-dialog">
        <div class="modal-header">
          <h3 class="modal-title">Explorador de Listas de Precios</h3>
          <button type="button" id="btnCerrarModalListas" class="btn-close">&times;</button>
        </div>
        <div class="modal-body">
          <div class="modal-table-container">
            <table class="clientes-table">
              <thead>
                <tr>
                  <th>Código</th>
                  <th>Denominación</th>
                  <th>Acción</th>
                </tr>
              </thead>
              <tbody id="listasPrecioTableBody"></tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  `);
}

crearModalListasPrecio();

const modalListasPrecio = document.getElementById('modalListasPrecio') as HTMLElement;
const btnAbrirExplorerListas = document.getElementById('btnAbrirExplorerListas') as HTMLButtonElement;
const btnCerrarModalListas = document.getElementById('btnCerrarModalListas') as HTMLButtonElement;
const tbodyListasPrecio = document.getElementById('listasPrecioTableBody') as HTMLTableSectionElement;
const listaPrecioTexto = document.getElementById('listaPrecioTexto') as HTMLInputElement;

// Consumidor Final sin cliente
const chkConsumidorFinal = document.getElementById('chkConsumidorFinal') as HTMLInputElement;

// Explorador de productos
const modalProductos = document.getElementById('modalProductos') as HTMLElement;
const tituloModalProductos = document.getElementById('tituloModalProductos') as HTMLElement;
const btnCerrarModalProductos = document.getElementById('btnCerrarModalProductos') as HTMLButtonElement;
const filtroProductoRubro = document.getElementById('filtroProductoRubro') as HTMLSelectElement;
const filtroProductoCodigo = document.getElementById('filtroProductoCodigo') as HTMLInputElement;
const filtroProductoDenominacion = document.getElementById('filtroProductoDenominacion') as HTMLInputElement;
const tbodyProductos = document.getElementById('productosTableBody') as HTMLTableSectionElement;
// Fila que se está cambiando desde el explorador (null = se agrega un ítem nuevo)
let indiceItemEnExplorador: number | null = null;
let catalogoEnExplorador: ListaPrecioArticuloDTO[] = [];

// Muestra en el recuadro "Cliente" el nombre del cliente elegido en el combo oculto
function mostrarClienteSeleccionado(): void {
  if (!clienteSelect.value) return;
  const texto = clienteSelect.options[clienteSelect.selectedIndex]?.text.trim() || '';
  if (texto) clienteTexto.value = texto;
}

// Formato de moneda
function formatearMoneda(monto: number): string {
  return new Intl.NumberFormat('es-AR', {
    style: 'currency',
    currency: 'ARS',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(monto);
}

function redondear(valor: number): number {
  return Math.round(valor * 100) / 100;
}

// Igual que el backend: cantidad x precio, menos la bonificación %
function calcularSubtotal(detalle: FacturaVentaDetalleDTO): number {
  const cantidad = Number(detalle.cantidad) || 0;
  const precioUnitario = Number(detalle.precioUnitario) || 0;
  const bonificacion = Number(detalle.porcentajeBonificacion) || 0;
  return redondear(cantidad * precioUnitario * (1 - bonificacion / 100));
}

// Factura A y B discriminan el IVA (21% contenido en el subtotal); la C no
function tipoDiscriminaIva(): boolean {
  const tipo = parseInt(tipoComprobanteSelect.value, 10);
  return tipo === 1 || tipo === 6;
}

function calcularImporteTotal(factura: FacturaVentaDTO): number {
  return factura.detalles.reduce((acc, d) => acc + (d.subtotal ?? calcularSubtotal(d)), 0);
}

function actualizarImporteTotal(): void {
  const total = detalles.reduce((acc, d) => acc + (d.subtotal ?? calcularSubtotal(d)), 0);
  importeTotalDisplay.textContent = formatearMoneda(total);
  if (ivaResumen) {
    if (tipoDiscriminaIva()) {
      // Mismo cálculo que el backend, renglón por renglón
      let neto = 0;
      detalles.forEach(d => { neto += redondear((d.subtotal ?? calcularSubtotal(d)) / 1.21); });
      neto = redondear(neto);
      ivaResumen.textContent = `Neto gravado: ${formatearMoneda(neto)} · IVA 21%: ${formatearMoneda(redondear(total - neto))}`;
    } else {
      ivaResumen.textContent = 'Factura C: no discrimina IVA.';
    }
  }
}

// Cargar datos en el formulario
function cargarFacturaEnFormulario(factura: FacturaVentaDTO): void {
  if (factura.fechaEmision) {
    const d = new Date(factura.fechaEmision);
    fechaEmisionInput.value = isNaN(d.getTime()) ? factura.fechaEmision : d.toISOString().split('T')[0];
  }

  if (factura.numeroFactura) {
    numeroFacturaInput.value = factura.numeroFactura.toString();
  }

  // Seleccionar opciones en combos (sin cliente = factura a Consumidor Final)
  if (factura.clienteId) {
    clienteSelect.value = factura.clienteId.toString();
    marcarConsumidorFinal(false);
  } else {
    marcarConsumidorFinal(true);
  }
  mostrarClienteSeleccionado();

  if (factura.tipoComprobante) {
    tipoComprobanteSelect.value = factura.tipoComprobante.toString();
  }
  if (factura.tipoMonedaId) {
    monedaSelect.value = factura.tipoMonedaId.toString();
  }

  if (factura.condicionIvaId) {
    condicionIvaSelect.value = factura.condicionIvaId.toString();
  } else if (factura.condicionIva) {
    const opt = Array.from(condicionIvaSelect.options).find(o => o.text.includes(factura.condicionIva));
    if (opt) condicionIvaSelect.value = opt.value;
  }

  detalles = (factura.detalles || []).map((detalle) => ({
    ...detalle,
    subtotal: detalle.subtotal ?? calcularSubtotal(detalle)
  }));

  siguienteDetalleId = detalles.length > 0
    ? Math.max(...detalles.map(d => d.id ?? 0)) + 1
    : 1;

  renderizarDetalles();
  actualizarImporteTotal();
}

// Obtener datos del formulario
function leerFacturaDesdeFormulario(): FacturaVentaDTO {
  const factura: FacturaVentaDTO = {
    fechaEmision: fechaEmisionInput.value,
    clienteId: chkConsumidorFinal.checked ? undefined : (parseInt(clienteSelect.value, 10) || undefined),
    clienteDenominacion: chkConsumidorFinal.checked
      ? 'Consumidor Final'
      : (clienteSelect.options[clienteSelect.selectedIndex]?.text || ''),
    condicionIvaId: parseInt(condicionIvaSelect.value, 10) || undefined,
    condicionIva: condicionIvaSelect.options[condicionIvaSelect.selectedIndex]?.text || '',
    puntoVentaId: modoEdicion ? undefined : puntoVentaPorDefectoId,
    puntoVentaDescripcion: '',
    detalles: detalles.map(detalle => ({
      id: detalle.id,
      articuloId: detalle.articuloId,
      listaPrecioArticuloId: detalle.listaPrecioArticuloId,
      productoDescripcion: detalle.productoDescripcion,
      cantidad: Number(detalle.cantidad) || 1,
      precioUnitario: Number(detalle.precioUnitario) || 0,
      porcentajeBonificacion: Number(detalle.porcentajeBonificacion) || 0,
      subtotal: calcularSubtotal(detalle)
    })),
    importeTotal: 0
  };

  factura.importeTotal = calcularImporteTotal(factura);

  if (modoEdicion) {
    factura.numeroFactura = parseInt(numeroFacturaParam, 10);
  }

  return factura;
}

function nombreLista(id?: number): string {
  const lista = listasDePrecio.find(l => l.id === id);
  return lista ? `${lista.codigo} - ${lista.denominacion}` : '';
}

// Trae (una sola vez) los productos de una lista de precios
async function obtenerCatalogo(listaId: number): Promise<ListaPrecioArticuloDTO[]> {
  if (!catalogosPorLista.has(listaId)) {
    catalogosPorLista.set(listaId, await FacturaService.listarArticulosPorLista(listaId));
  }
  return catalogosPorLista.get(listaId) || [];
}

// Rubros distintos de un catálogo
function obtenerRubros(catalogo: ListaPrecioArticuloDTO[]): string[] {
  return Array.from(new Set(catalogo.map(a => a.rubro).filter(r => !!r))).sort();
}

function escaparHtml(texto: string): string {
  return (texto ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c] as string));
}

// Renderizado dinámico de la tabla de ítems / renglones
function renderizarDetalles(): void {
  if (detalles.length === 0) {
    tbodyDetalles.innerHTML = `
      <tr>
        <td colspan="8" class="text-center" style="padding: 24px; color: #64748b;">
          No hay ítems agregados a la factura. Haga clic en "+ Agregar Ítem" para elegir un producto.
        </td>
      </tr>
    `;
    actualizarImporteTotal();
    return;
  }

  tbodyDetalles.innerHTML = detalles.map((d, index) => `
      <tr data-index="${index}">
        <td style="vertical-align: middle;">${escaparHtml(d.codigo || '-')}</td>
        <td>
          <div class="producto-elegido">
            <span>${d.productoDescripcion ? escaparHtml(d.productoDescripcion) : '<em style="color: #b91c1c;">Sin producto</em>'}</span>
            <button type="button" class="btn btn-sm btn-secondary btn-cambiar-producto" data-index="${index}">
              ${d.productoDescripcion ? 'Cambiar' : 'Elegir'}
            </button>
          </div>
          ${d.listaPrecioId !== undefined ? `<div class="producto-lista">Lista: ${escaparHtml(nombreLista(d.listaPrecioId))}</div>` : ''}
        </td>
        <td style="vertical-align: middle;">${escaparHtml(d.rubro || '-')}</td>
        <td>
          <input type="number" min="1" step="1" class="form-control input-cantidad" data-index="${index}" value="${d.cantidad}">
        </td>
        <td>
          <input type="text" class="form-control" value="${formatearMoneda(d.precioUnitario)}" readonly>
        </td>
        <td>
          <input type="number" min="0" max="100" step="0.01" class="form-control input-bonificacion" data-index="${index}" value="${d.porcentajeBonificacion ?? 0}">
        </td>
        <td style="font-weight: 600; vertical-align: middle;">
          ${formatearMoneda(d.subtotal ?? calcularSubtotal(d))}
        </td>
        <td class="text-center" style="vertical-align: middle;">
          <button type="button" class="btn btn-sm btn-delete btn-eliminar-item" data-index="${index}">Eliminar</button>
        </td>
      </tr>
    `).join('');

  // Cambiar el producto de una fila: abre el explorador
  tbodyDetalles.querySelectorAll('.btn-cambiar-producto').forEach(btn => {
    btn.addEventListener('click', (e) => {
      const index = parseInt((e.currentTarget as HTMLElement).getAttribute('data-index') || '0', 10);
      abrirExplorerProductos(index);
    });
  });

  // Cambia la cantidad o la bonificación: se recalcula el subtotal de la fila y el total
  const actualizarFila = (index: number) => {
    detalles[index].subtotal = calcularSubtotal(detalles[index]);
    actualizarImporteTotal();
    const row = tbodyDetalles.querySelector(`tr[data-index="${index}"]`);
    const subtotalCell = row?.children[6];
    if (subtotalCell) subtotalCell.textContent = formatearMoneda(detalles[index].subtotal ?? 0);
  };

  tbodyDetalles.querySelectorAll('.input-cantidad').forEach(input => {
    input.addEventListener('input', (e) => {
      const target = e.target as HTMLInputElement;
      const index = parseInt(target.getAttribute('data-index') || '0', 10);
      detalles[index].cantidad = parseFloat(target.value) || 0;
      actualizarFila(index);
    });
  });

  tbodyDetalles.querySelectorAll('.input-bonificacion').forEach(input => {
    input.addEventListener('input', (e) => {
      const target = e.target as HTMLInputElement;
      const index = parseInt(target.getAttribute('data-index') || '0', 10);
      detalles[index].porcentajeBonificacion = parseFloat(target.value) || 0;
      actualizarFila(index);
    });
  });

  tbodyDetalles.querySelectorAll('.btn-eliminar-item').forEach(btn => {
    btn.addEventListener('click', (e) => {
      const target = e.currentTarget as HTMLElement;
      const index = parseInt(target.getAttribute('data-index') || '0', 10);
      detalles.splice(index, 1);
      renderizarDetalles();
      actualizarImporteTotal();
    });
  });

  actualizarImporteTotal();
}

// Agregar nuevo ítem: se abre el explorador de productos
function agregarItem(): void {
  if (!listaPrecioSeleccionada) {
    alert('Primero seleccione una lista de precios.');
    return;
  }
  abrirExplorerProductos(null);
}

// --- Explorador de productos ---
async function abrirExplorerProductos(indiceItem: number | null): Promise<void> {
  const listaId = indiceItem !== null
    ? (detalles[indiceItem].listaPrecioId ?? listaPrecioSeleccionada?.id)
    : listaPrecioSeleccionada?.id;
  if (listaId === undefined) {
    alert('Primero seleccione una lista de precios.');
    return;
  }

  try {
    catalogoEnExplorador = await obtenerCatalogo(listaId);
  } catch (err: any) {
    alert(`No se pudieron cargar los productos: ${err.message}`);
    return;
  }

  indiceItemEnExplorador = indiceItem;
  tituloModalProductos.textContent = `Explorador de Productos · ${nombreLista(listaId) || 'Lista de precios'}`;
  filtroProductoRubro.innerHTML = '<option value="">Todos los rubros</option>' +
    obtenerRubros(catalogoEnExplorador).map(r => `<option value="${escaparHtml(r)}">${escaparHtml(r)}</option>`).join('');
  filtroProductoRubro.value = '';
  filtroProductoCodigo.value = '';
  filtroProductoDenominacion.value = '';
  modalProductos.classList.remove('hidden');
  renderizarProductosEnModal();
  filtroProductoDenominacion.focus();
}

function cerrarExplorerProductos(): void {
  modalProductos.classList.add('hidden');
  indiceItemEnExplorador = null;
}

// Filtra por rubro, código y denominación (todo a la vez)
function renderizarProductosEnModal(): void {
  const rubro = filtroProductoRubro.value;
  const codigo = filtroProductoCodigo.value.trim().toLowerCase();
  const denominacion = filtroProductoDenominacion.value.trim().toLowerCase();

  const productos = catalogoEnExplorador.filter(a =>
    (!rubro || a.rubro === rubro) &&
    (!codigo || (a.codigo || '').toLowerCase().includes(codigo)) &&
    (!denominacion || (a.denominacion || '').toLowerCase().includes(denominacion))
  );

  if (productos.length === 0) {
    tbodyProductos.innerHTML = `<tr><td colspan="5" class="text-center">No se encontraron productos.</td></tr>`;
    return;
  }

  tbodyProductos.innerHTML = productos.map(a => `
    <tr>
      <td><strong>${escaparHtml(a.codigo)}</strong></td>
      <td>${escaparHtml(a.denominacion)}</td>
      <td>${escaparHtml(a.rubro || '-')}</td>
      <td>${formatearMoneda(a.precioVenta)}</td>
      <td class="text-center">
        <button type="button" class="btn btn-sm btn-primary btn-seleccionar-producto" data-id="${a.id}">Seleccionar</button>
      </td>
    </tr>
  `).join('');

  tbodyProductos.querySelectorAll('.btn-seleccionar-producto').forEach(btn => {
    btn.addEventListener('click', (e) => {
      const id = Number((e.currentTarget as HTMLElement).getAttribute('data-id'));
      const art = catalogoEnExplorador.find(a => a.id === id);
      if (art) seleccionarProducto(art);
    });
  });
}

function seleccionarProducto(art: ListaPrecioArticuloDTO): void {
  const listaId = indiceItemEnExplorador !== null
    ? detalles[indiceItemEnExplorador].listaPrecioId
    : listaPrecioSeleccionada?.id;

  if (indiceItemEnExplorador !== null) {
    // Cambia el producto de una fila existente
    const d = detalles[indiceItemEnExplorador];
    d.articuloId = art.articuloId;
    d.listaPrecioArticuloId = art.id;
    d.codigo = art.codigo;
    d.rubro = art.rubro;
    d.productoDescripcion = art.denominacion;
    d.precioUnitario = art.precioVenta;
    d.subtotal = calcularSubtotal(d);
  } else {
    // Si el producto ya está en la factura (misma lista), se suma 1 a la cantidad
    const existente = detalles.find(d => d.listaPrecioArticuloId === art.id && !(Number(d.porcentajeBonificacion) > 0));
    if (existente) {
      existente.cantidad = (Number(existente.cantidad) || 0) + 1;
      existente.subtotal = calcularSubtotal(existente);
    } else {
      const nuevo: FacturaVentaDetalleDTO = {
        id: siguienteDetalleId++,
        listaPrecioId: listaId,
        listaPrecioArticuloId: art.id,
        articuloId: art.articuloId,
        codigo: art.codigo,
        rubro: art.rubro,
        productoDescripcion: art.denominacion,
        cantidad: 1,
        precioUnitario: art.precioVenta,
        porcentajeBonificacion: 0,
        subtotal: 0
      };
      nuevo.subtotal = calcularSubtotal(nuevo);
      detalles.push(nuevo);
    }
  }

  cerrarExplorerProductos();
  renderizarDetalles();
}

// --- Consumidor Final sin cliente ---
function marcarConsumidorFinal(marcado: boolean): void {
  chkConsumidorFinal.checked = marcado;
  clienteTexto.disabled = marcado;
  btnAbrirExplorer.disabled = marcado;

  if (marcado) {
    clienteSelect.value = '';
    clienteTexto.value = '';
    clienteTexto.placeholder = 'Consumidor Final (sin cliente)';
    const optCF = Array.from(condicionIvaSelect.options).find(o => o.text.toLowerCase().includes('consumidor'));
    if (optCF) condicionIvaSelect.value = optCF.value;
  } else {
    clienteTexto.placeholder = 'Seleccione un cliente';
  }
}

// Cargar combos auxiliares desde el backend
async function cargarOpciones(): Promise<void> {
  try {
    opcionesDisponibles = await FacturaService.obtenerOpciones();

    // El punto de venta ya no se elige: se usa el primero de la lista
    puntoVentaPorDefectoId = opcionesDisponibles.puntosVenta[0]?.id;

    // Llenar Clientes
    clienteSelect.innerHTML = '<option value="">Seleccionar...</option>' + opcionesDisponibles.clientes.map(cl => `      <option value="${cl.id}">${cl.descripcion}</option>
    `).join('');

    clienteSelect.value = '';

    // Llenar Condición IVA
    condicionIvaSelect.innerHTML = '<option value="">Seleccionar...</option>' + opcionesDisponibles.condicionesIva.map(ci => `      <option value="${ci.id}">${ci.descripcion}</option>
    `).join('');

    // Una factura nueva arranca como Consumidor Final sin cliente (se destilda para elegir uno)
    marcarConsumidorFinal(true);

    // Tipos de comprobante (A, B, C) y monedas
    const tipos = await FacturaService.listarTiposComprobante();
    if (tipos.length > 0) {
      tipoComprobanteSelect.innerHTML = tipos.map(t => `<option value="${t.codigo}" title="${t.descripcion}">${t.nombre}</option>`).join('');
      const porDefecto = tipos.find(t => t.porDefecto);
      if (porDefecto) tipoComprobanteSelect.value = porDefecto.codigo.toString();
    }
    const monedas = await FacturaService.listarMonedas();
    if (monedas.length > 0) {
      monedaSelect.innerHTML = monedas.map(m => `<option value="${m.id}">${m.denominacion} (${m.codigoAfip})</option>`).join('');
      const pesos = monedas.find(m => m.codigoAfip === 'PES');
      if (pesos) monedaSelect.value = pesos.id.toString();
    }

  } catch (err) {
    console.warn('No se pudieron cargar opciones desde el backend. Usando valores por defecto.', err);
    puntoVentaPorDefectoId = 1;
    clienteSelect.innerHTML = `<option value="1">Consumidor Final</option><option value="2">Mayra Muñoz</option>`;
    condicionIvaSelect.innerHTML = `
      <option value="1">IVA Responsable Inscripto</option>
      <option value="2">Monotributo</option>
      <option value="3" selected>Consumidor Final</option>
      <option value="4">IVA Sujeto Exento</option>
    `;
  }
}

// Guardar factura (POST o PUT)
async function guardarFactura(e: Event): Promise<void> {
  e.preventDefault();

  if (!fechaEmisionInput.value) {
    alert('Debe ingresar la fecha de emisión.');
    fechaEmisionInput.focus();
    return;
  }

  if (!chkConsumidorFinal.checked) {
    if (!clienteTexto.value.trim() && !clienteSelect.value) {
      alert('Debe seleccionar un cliente, o marcar "Consumidor Final (sin cliente)".');
      return;
    }
    if (!(await resolverClienteEscrito())) {
      alert('No se encontró un cliente con ese nombre. Escriba el nombre completo o use el botón Explorar.');
      return;
    }
  }

  if (!condicionIvaSelect.value) {
    alert('Debe seleccionar la condición de IVA.');
    condicionIvaSelect.focus();
    return;
  }

  if (!listaPrecioSeleccionada && detalles.length === 0) {
    alert('Debe seleccionar una lista de precios con el botón Explorar.');
    return;
  }

  if (detalles.length === 0) {
    alert('Debe agregar al menos un producto a la factura antes de guardar.');
    return;
  }

  if (detalles.some(d => !d.articuloId && !d.listaPrecioArticuloId)) {
    alert('Hay ítems sin producto. Elija un producto o elimine la fila antes de guardar.');
    return;
  }

  if (detalles.some(d => !(Number(d.cantidad) > 0) || !Number.isInteger(Number(d.cantidad)))) {
    alert('La cantidad de cada ítem debe ser un número entero mayor a cero.');
    return;
  }

  if (detalles.some(d => { const b = Number(d.porcentajeBonificacion) || 0; return b < 0 || b > 100; })) {
    alert('La bonificación de cada ítem debe estar entre 0 y 100.');
    return;
  }

  if (tipoComprobanteSelect.value === '1' && chkConsumidorFinal.checked) {
    alert('La Factura A requiere un cliente con CUIT: no puede emitirse a Consumidor Final.');
    return;
  }

  const facturaDTO = leerFacturaDesdeFormulario();

  const request: FacturaRequestDTO = {
    numero: facturaDTO.numeroFactura,
    fechaEmision: facturaDTO.fechaEmision,
    listaPrecioId: listaPrecioSeleccionada?.id,
    clienteId: facturaDTO.clienteId,
    clienteDenominacion: facturaDTO.clienteDenominacion,
    condicionIvaId: facturaDTO.condicionIvaId,
    condicionIva: facturaDTO.condicionIva,
    puntoVentaId: facturaDTO.puntoVentaId,
    puntoVentaDescripcion: facturaDTO.puntoVentaDescripcion,
    cbteTipo: parseInt(tipoComprobanteSelect.value, 10) || undefined,
    tipoMonedaId: parseInt(monedaSelect.value, 10) || undefined,
    detalles: facturaDTO.detalles.map(d => ({
      id: d.id,
      articuloId: d.articuloId,
      listaPrecioArticuloId: d.listaPrecioArticuloId,
      productoDescripcion: d.productoDescripcion,
      cantidad: d.cantidad,
      precioUnitario: d.precioUnitario,
      porcentajeBonificacion: d.porcentajeBonificacion,
      subtotal: d.subtotal
    }))
  };

  // Evitar doble envio
  const btnGuardar = document.getElementById('btnGuardar') as HTMLButtonElement | null;
  if (btnGuardar?.disabled) return;
  if (btnGuardar) {
    btnGuardar.disabled = true;
    btnGuardar.textContent = 'Guardando...';
  }

  try {
    if (modoEdicion && facturaIdEdicion) {
      await FacturaService.actualizarFactura(facturaIdEdicion, request);
      alert('¡Factura actualizada exitosamente!');
    } else {
      const creada = await FacturaService.crearFactura(request);
      alert(`¡Factura N° ${creada.numeroFactura} creada exitosamente!`);
    }
    window.location.href = './index.html';
  } catch (err: any) {
    alert(`Error al guardar la factura: ${err.message}`);
    if (btnGuardar) {
      btnGuardar.disabled = false;
      btnGuardar.textContent = 'Guardar';
    }
  }
}

async function abrirExplorerClientes(): Promise<void> {
  modalClientes.classList.remove('hidden');
  inputBuscarCliente.value = '';
  await cargarClientesEnModal();
}

function cerrarExplorerClientes(): void {
  modalClientes.classList.add('hidden');
}

async function cargarClientesEnModal(buscar?: string): Promise<void> {
  tbodyClientes.innerHTML = `<tr><td colspan="5" class="text-center">Cargando clientes...</td></tr>`;
  try {
    const clientes = await ClienteService.obtenerClientes(buscar);

    if (clientes.length === 0) {
      tbodyClientes.innerHTML = `<tr><td colspan="5" class="text-center">No se encontraron clientes.</td></tr>`;
      return;
    }

    tbodyClientes.innerHTML = clientes.map(c => `
      <tr>
        <td><strong>${c.cuitCuil || '-'}</strong></td>
        <td>${c.denominacion}</td>
        <td>${c.email || '-'}</td>
        <td>${c.telefono || '-'}</td>
        <td class="text-center">
          <button type="button" class="btn btn-sm btn-primary btn-seleccionar" data-id="${c.id}">Seleccionar</button>
        </td>
      </tr>
    `).join('');

    tbodyClientes.querySelectorAll('.btn-seleccionar').forEach(btn => {
      btn.addEventListener('click', (e) => {
        const id = (e.currentTarget as HTMLElement).getAttribute('data-id');
        if (id) {
          clienteSelect.value = id;
          clienteSelect.dispatchEvent(new Event('change')); // actualiza la Condición IVA si es Consumidor Final
          cerrarExplorerClientes();
        }
      });
    });
  } catch (err) {
    tbodyClientes.innerHTML = `<tr><td colspan="5" class="text-center" style="color: red;">Error al consultar clientes.</td></tr>`;
  }
}

// Si el usuario escribió un nombre, busca el cliente para obtener su id
async function resolverClienteEscrito(): Promise<boolean> {
  if (clienteSelect.value) return true; // ya hay un cliente elegido

  const texto = clienteTexto.value.trim();
  if (!texto) return false;

  try {
    const clientes = await ClienteService.obtenerClientes(texto);
    const exacto = clientes.find(c => c.denominacion.toLowerCase() === texto.toLowerCase());
    const elegido = exacto ?? (clientes.length === 1 ? clientes[0] : undefined);

    if (elegido) {
      clienteSelect.value = elegido.id.toString();
      clienteSelect.dispatchEvent(new Event('change'));
      return true;
    }
  } catch (err) {
    console.error('Error al buscar el cliente:', err);
  }
  return false;
}

async function abrirExplorerListasPrecio(): Promise<void> {
  modalListasPrecio.classList.remove('hidden');
  await cargarListasPrecioEnModal();
}

function cerrarExplorerListasPrecio(): void {
  modalListasPrecio.classList.add('hidden');
}

// Elige la lista de precios con la que se van a agregar los PRÓXIMOS ítems.
// Los ítems que ya tienen producto conservan su lista y su precio:
// así una factura puede tener productos de listas de precios distintas.
async function aplicarListaPrecio(lista: ListaPrecioDTO): Promise<void> {
  try {
    await obtenerCatalogo(lista.id);
  } catch (err: any) {
    alert(`No se pudieron cargar los productos de la lista: ${err.message}`);
    return;
  }

  if (!listasDePrecio.some(l => l.id === lista.id)) listasDePrecio.push(lista);
  listaPrecioSeleccionada = lista;
  listaPrecioTexto.value = `${lista.codigo} - ${lista.denominacion}`;

  renderizarDetalles();
}

async function cargarListasPrecioEnModal(): Promise<void> {
  tbodyListasPrecio.innerHTML = `<tr><td colspan="3" class="text-center">Cargando listas de precios...</td></tr>`;
  try {
    const listas: ListaPrecioDTO[] = await FacturaService.listarListasPrecio();
    listasDePrecio = listas;

    if (listas.length === 0) {
      tbodyListasPrecio.innerHTML = `<tr><td colspan="3" class="text-center">No hay listas de precios disponibles.</td></tr>`;
      return;
    }

    tbodyListasPrecio.innerHTML = listas.map(lp => `
      <tr>
        <td><strong>${lp.codigo}</strong></td>
        <td>${lp.denominacion}</td>
        <td class="text-center">
          <button type="button" class="btn btn-sm btn-primary btn-seleccionar-lista" data-id="${lp.id}">Seleccionar</button>
        </td>
      </tr>
    `).join('');

    tbodyListasPrecio.querySelectorAll('.btn-seleccionar-lista').forEach(btn => {
      btn.addEventListener('click', async (e) => {
        const id = Number((e.currentTarget as HTMLElement).getAttribute('data-id'));
        const lista = listas.find(l => l.id === id);
        if (lista) {
          await aplicarListaPrecio(lista);
          cerrarExplorerListasPrecio();
        }
      });
    });
  } catch (err) {
    tbodyListasPrecio.innerHTML = `<tr><td colspan="3" class="text-center" style="color: red;">Error al consultar las listas de precios.</td></tr>`;
  }
}

// Inicialización de la pantalla
document.addEventListener('DOMContentLoaded', async () => {
  // Fecha actual por defecto
  const ahora = new Date();
  const hoy = `${ahora.getFullYear()}-${String(ahora.getMonth() + 1).padStart(2, '0')}-${String(ahora.getDate()).padStart(2, '0')}`;
  fechaEmisionInput.value = hoy;

  await cargarOpciones();

  // Si viene id por URL estamos en modo edición
  const urlParams = new URLSearchParams(window.location.search);
  const idParam = urlParams.get('id');

  if (idParam) {
    modoEdicion = true;
    facturaIdEdicion = parseInt(idParam, 10);
    formTitulo.textContent = `Editar Factura (ID: ${facturaIdEdicion})`;

    try {
      const factura = await FacturaService.obtenerFacturaPorId(facturaIdEdicion);

      // Una factura ya informada a ARCA no se puede editar
      if (factura.cae) {
        alert(`La factura N° ${factura.numeroFactura} ya fue informada a ARCA (CAE ${factura.cae}) y no se puede editar. Solo se puede anular desde el listado.`);
        window.location.href = './index.html';
        return;
      }
      numeroFacturaParam = factura.numeroFactura.toString();

      cargarFacturaEnFormulario({
        id: factura.id,
        numeroFactura: factura.numeroFactura,
        fechaEmision: factura.fechaEmision,
        clienteId: factura.clienteId,
        clienteDenominacion: factura.clienteDenominacion,
        condicionIvaId: factura.condicionIvaId,
        condicionIva: factura.condicionIva,
        puntoVentaDescripcion: factura.puntoVentaDescripcion,
        tipoComprobante: factura.tipoComprobante,
        tipoMonedaId: factura.tipoMonedaId,
        detalles: (factura.detalles || []).map(d => ({
          id: d.id,
          articuloId: d.articuloId,
          listaPrecioId: d.listaPrecioId,
          listaPrecioArticuloId: d.listaPrecioArticuloId,
          productoDescripcion: d.productoDescripcion,
          cantidad: d.cantidad,
          precioUnitario: d.precioUnitario,
          porcentajeBonificacion: d.porcentajeBonificacion,
          subtotal: d.subtotal
        }))
      });
      // Cada ítem trae su propia lista: se cargan los productos de cada una para mostrar código y rubro
      listasDePrecio = await FacturaService.listarListasPrecio();
      for (const d of detalles) {
        if (d.listaPrecioId === undefined) continue;
        const catalogo = await obtenerCatalogo(d.listaPrecioId);
        const art = catalogo.find(a => a.id === d.listaPrecioArticuloId)
          ?? catalogo.find(a => a.articuloId === d.articuloId);
        if (art) {
          d.rubro = art.rubro;
          d.codigo = art.codigo;
        }
      }

      // Preselecciona la lista de la factura para los próximos ítems (se puede cambiar)
      if (factura.listaPrecioId) {
        const lista = listasDePrecio.find(l => l.id === factura.listaPrecioId);
        if (lista) await aplicarListaPrecio(lista);
      }
      renderizarDetalles();
    } catch (err: any) {
      alert(`No se pudo cargar la factura para edición: ${err.message}`);
    }
  } else {
    // Nueva factura: arranca sin ítems
    detalles = [];
    siguienteDetalleId = 1;
    renderizarDetalles();
  }

  // Listeners de eventos
  clienteSelect.addEventListener('change', () => {
    mostrarClienteSeleccionado();
    const textoCliente = clienteSelect.options[clienteSelect.selectedIndex]?.text.toLowerCase() || '';
    if (textoCliente.includes('consumidor')) {
      const optCF = Array.from(condicionIvaSelect.options).find(o => o.text.toLowerCase().includes('consumidor'));
      if (optCF) {
        condicionIvaSelect.value = optCF.value;
      }
    }
  });

  btnAgregarItem.addEventListener('click', agregarItem);
  tipoComprobanteSelect.addEventListener('change', actualizarImporteTotal);
  facturaForm.addEventListener('submit', guardarFactura);
  btnCancelar.addEventListener('click', () => {
    window.location.href = './index.html';
  });

  // --- Explorador de clientes ---
  btnAbrirExplorer.addEventListener('click', abrirExplorerClientes);
  btnCerrarModal.addEventListener('click', cerrarExplorerClientes);
  modalClientes.addEventListener('click', (e) => {
    if (e.target === modalClientes) cerrarExplorerClientes();
  });

  let debounceTimer: any;
  inputBuscarCliente.addEventListener('input', () => {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(() => cargarClientesEnModal(inputBuscarCliente.value), 300);
  });
  clienteTexto.addEventListener('input', () => {
    clienteSelect.value = '';
  });

  // --- Consumidor Final sin cliente ---
  chkConsumidorFinal.addEventListener('change', () => marcarConsumidorFinal(chkConsumidorFinal.checked));

  // --- Explorador de productos ---
  btnCerrarModalProductos.addEventListener('click', cerrarExplorerProductos);
  modalProductos.addEventListener('click', (e) => {
    if (e.target === modalProductos) cerrarExplorerProductos();
  });
  filtroProductoRubro.addEventListener('change', renderizarProductosEnModal);
  filtroProductoCodigo.addEventListener('input', renderizarProductosEnModal);
  filtroProductoDenominacion.addEventListener('input', renderizarProductosEnModal);

  btnAbrirExplorerListas.addEventListener('click', abrirExplorerListasPrecio);
  listaPrecioTexto.addEventListener('click', abrirExplorerListasPrecio);
  btnCerrarModalListas.addEventListener('click', cerrarExplorerListasPrecio);
  modalListasPrecio.addEventListener('click', (e) => {
    if (e.target === modalListasPrecio) cerrarExplorerListasPrecio();
  });
});
