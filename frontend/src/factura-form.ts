import { FacturaService } from './services/facturaService';
import {
  FacturaVentaDTO,
  FacturaVentaDetalleDTO,
  FacturaRequestDTO,
  OpcionesComboDTO,
  ArticuloComboDTO
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
let articulosLista: ListaPrecioArticuloDTO[] = [];
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

// Formato de moneda
function formatearMoneda(monto: number): string {
  return new Intl.NumberFormat('es-AR', {
    style: 'currency',
    currency: 'ARS',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(monto);
}

function calcularSubtotal(detalle: FacturaVentaDetalleDTO): number {
  const cantidad = Number(detalle.cantidad) || 0;
  const precioUnitario = Number(detalle.precioUnitario) || 0;
  return cantidad * precioUnitario;
}

function calcularImporteTotal(factura: FacturaVentaDTO): number {
  return factura.detalles.reduce((acc, d) => acc + (d.subtotal ?? calcularSubtotal(d)), 0);
}

function actualizarImporteTotal(): void {
  const total = detalles.reduce((acc, d) => acc + (d.subtotal ?? calcularSubtotal(d)), 0);
  importeTotalDisplay.textContent = formatearMoneda(total);
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

  // Seleccionar opciones en combos
  if (factura.clienteId) {
    clienteSelect.value = factura.clienteId.toString();
  } else if (factura.clienteDenominacion) {
    const opt = Array.from(clienteSelect.options).find(o => o.text.includes(factura.clienteDenominacion));
    if (opt) clienteSelect.value = opt.value;
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
    clienteId: parseInt(clienteSelect.value, 10) || undefined,
    clienteDenominacion: clienteSelect.options[clienteSelect.selectedIndex]?.text || '',
    condicionIvaId: parseInt(condicionIvaSelect.value, 10) || undefined,
    condicionIva: condicionIvaSelect.options[condicionIvaSelect.selectedIndex]?.text || '',
    puntoVentaId: modoEdicion ? undefined : puntoVentaPorDefectoId,
    puntoVentaDescripcion: '',
    detalles: detalles.map(detalle => ({
      id: detalle.id,
      articuloId: detalle.articuloId,
      productoDescripcion: detalle.productoDescripcion,
      cantidad: Number(detalle.cantidad) || 1,
      precioUnitario: Number(detalle.precioUnitario) || 0,
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

// Rubros distintos de la lista de precios elegida
function obtenerRubros(): string[] {
  return Array.from(new Set(articulosLista.map(a => a.rubro))).sort();
}

// Renderizado dinámico de la tabla de ítems / renglones
function renderizarDetalles(): void {
  if (detalles.length === 0) {
    tbodyDetalles.innerHTML = `
      <tr>
        <td colspan="6" class="text-center" style="padding: 24px; color: #64748b;">
          No hay ítems agregados a la factura. Haga clic en "+ Agregar Ítem" para comenzar.
        </td>
      </tr>
    `;
    actualizarImporteTotal();
    return;
  }

  const rubros = obtenerRubros();
  const hayLista = articulosLista.length > 0;

  tbodyDetalles.innerHTML = detalles.map((d, index) => {
    const opcionesRubro = '<option value="">Seleccionar rubro...</option>' +
      rubros.map(r => `<option value="${r}" ${r === d.rubro ? 'selected' : ''}>${r}</option>`).join('');

    const articulosDelRubro = articulosLista.filter(a => a.rubro === d.rubro);
    const opcionesProducto = '<option value="">Seleccionar producto...</option>' +
      articulosDelRubro.map(a => `
        <option value="${a.articuloId}" ${a.articuloId === d.articuloId ? 'selected' : ''}>
          ${a.denominacion} ($ ${a.precioVenta.toFixed(2)})
        </option>`).join('');

    const selectRubro = hayLista
      ? `<select class="form-control select-rubro" data-index="${index}">${opcionesRubro}</select>`
      : `<select class="form-control" disabled><option>Elija una lista de precios</option></select>`;

    const selectProducto = hayLista
      ? `<select class="form-control select-producto" data-index="${index}" ${d.rubro ? '' : 'disabled'}>${opcionesProducto}</select>`
      : `<select class="form-control" disabled><option>${d.productoDescripcion || 'Elija una lista de precios'}</option></select>`;

    return `
      <tr data-index="${index}">
        <td>${selectRubro}</td>
        <td>${selectProducto}</td>
        <td>
          <input type="number" min="1" step="1" class="form-control input-cantidad" data-index="${index}" value="${d.cantidad}">
        </td>
        <td>
          <input type="text" class="form-control" value="${formatearMoneda(d.precioUnitario)}" readonly>
        </td>
        <td style="font-weight: 600; vertical-align: middle;">
          ${formatearMoneda(d.subtotal ?? calcularSubtotal(d))}
        </td>
        <td class="text-center" style="vertical-align: middle;">
          <button type="button" class="btn btn-sm btn-delete btn-eliminar-item" data-index="${index}">Eliminar</button>
        </td>
      </tr>
    `;
  }).join('');

  // Al cambiar el rubro se limpia el producto elegido
  tbodyDetalles.querySelectorAll('.select-rubro').forEach(select => {
    select.addEventListener('change', (e) => {
      const target = e.target as HTMLSelectElement;
      const index = parseInt(target.getAttribute('data-index') || '0', 10);
      detalles[index].rubro = target.value;
      detalles[index].articuloId = undefined;
      detalles[index].productoDescripcion = '';
      detalles[index].precioUnitario = 0;
      detalles[index].subtotal = 0;
      renderizarDetalles();
    });
  });

  // Al elegir el producto se toma su precio de la lista
  tbodyDetalles.querySelectorAll('.select-producto').forEach(select => {
    select.addEventListener('change', (e) => {
      const target = e.target as HTMLSelectElement;
      const index = parseInt(target.getAttribute('data-index') || '0', 10);
      const art = articulosLista.find(a => a.articuloId === parseInt(target.value, 10));

      detalles[index].articuloId = art?.articuloId;
      detalles[index].productoDescripcion = art?.denominacion || '';
      detalles[index].precioUnitario = art?.precioVenta || 0;
      detalles[index].subtotal = calcularSubtotal(detalles[index]);
      renderizarDetalles();
    });
  });

  tbodyDetalles.querySelectorAll('.input-cantidad').forEach(input => {
    input.addEventListener('input', (e) => {
      const target = e.target as HTMLInputElement;
      const index = parseInt(target.getAttribute('data-index') || '0', 10);
      detalles[index].cantidad = parseFloat(target.value) || 0;
      detalles[index].subtotal = calcularSubtotal(detalles[index]);
      actualizarImporteTotal();
      const row = tbodyDetalles.querySelector(`tr[data-index="${index}"]`);
      if (row) {
        const subtotalCell = row.children[4];
        if (subtotalCell) subtotalCell.textContent = formatearMoneda(detalles[index].subtotal ?? 0);
      }
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

// Agregar nuevo ítem (fila vacía: se elige rubro y después producto)
function agregarItem(): void {
  if (!listaPrecioSeleccionada) {
    alert('Primero seleccione una lista de precios.');
    return;
  }

  detalles.push({
    id: siguienteDetalleId++,
    rubro: '',
    articuloId: undefined,
    productoDescripcion: '',
    cantidad: 1,
    precioUnitario: 0,
    subtotal: 0
  });
  renderizarDetalles();
  actualizarImporteTotal();
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

    // Preseleccionar Consumidor Final si existe
    const clCF = opcionesDisponibles.clientes.find(c => c.descripcion.toLowerCase().includes('consumidor'));
    if (clCF) clienteSelect.value = clCF.id.toString();

    // Llenar Condición IVA
    condicionIvaSelect.innerHTML = '<option value="">Seleccionar...</option>' + opcionesDisponibles.condicionesIva.map(ci => `      <option value="${ci.id}">${ci.descripcion}</option>
    `).join('');

    // Preselección por defecto: si el cliente seleccionado es Consumidor Final, sugerir Consumidor Final; sino Responsable Inscripto
    const ciCF = opcionesDisponibles.condicionesIva.find(c => c.descripcion.toLowerCase().includes('consumidor'));
    const ciResp = opcionesDisponibles.condicionesIva.find(c => c.descripcion.toLowerCase().includes('inscripto'));
    if (clCF && clienteSelect.value === clCF.id.toString() && ciCF) {
      condicionIvaSelect.value = ciCF.id.toString();
    } else if (ciResp) {
      condicionIvaSelect.value = ciResp.id.toString();
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

  if (!(await resolverClienteEscrito())) {
    alert('No se encontró un cliente con ese nombre. Escriba el nombre completo o use el botón Explorar.');
    return;
  }

  if (!listaPrecioSeleccionada) {
    alert('Debe seleccionar una lista de precios con el botón Explorar.');
    return;
  }

  if (detalles.length === 0) {
    alert('Debe agregar al menos un ítem a la factura antes de guardar.');
    return;
  }

    if (detalles.some(d => !d.articuloId)) {
    alert('Todos los ítems deben tener rubro y producto seleccionados.');
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
    detalles: facturaDTO.detalles.map(d => ({
      id: d.id,
      articuloId: d.articuloId,
      productoDescripcion: d.productoDescripcion,
      cantidad: d.cantidad,
      precioUnitario: d.precioUnitario,
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

// Carga los productos de la lista elegida y actualiza los ítems ya cargados
async function aplicarListaPrecio(lista: ListaPrecioDTO, actualizarPrecios: boolean = true): Promise<void> {
  try {
    articulosLista = await FacturaService.listarArticulosPorLista(lista.id);
  } catch (err: any) {
    alert(`No se pudieron cargar los productos de la lista: ${err.message}`);
    return;
  }

  listaPrecioSeleccionada = lista;
  listaPrecioTexto.value = `${lista.codigo} - ${lista.denominacion}`;

  const eliminados: string[] = [];
  detalles = detalles.filter(d => {
    if (d.articuloId === undefined) {
      // fila todavía sin producto: solo se limpia el rubro si no existe en la nueva lista
      if (d.rubro && !articulosLista.some(a => a.rubro === d.rubro)) d.rubro = '';
      return true;
    }
    const art = articulosLista.find(a => a.articuloId === d.articuloId);
    if (!art) {
      if (!actualizarPrecios) return true; // al abrir para editar se conserva el ítem
      eliminados.push(d.productoDescripcion);
      return false;
    }
    d.rubro = art.rubro;
    if (actualizarPrecios) {
      d.productoDescripcion = art.denominacion;
      d.precioUnitario = art.precioVenta;
      d.subtotal = calcularSubtotal(d);
    }
    return true;
  });

  if (eliminados.length > 0) {
    alert(`Estos productos no están en la lista elegida y se quitaron: ${eliminados.join(', ')}`);
  }

  renderizarDetalles();
}

async function cargarListasPrecioEnModal(): Promise<void> {
  tbodyListasPrecio.innerHTML = `<tr><td colspan="3" class="text-center">Cargando listas de precios...</td></tr>`;
  try {
    const listas: ListaPrecioDTO[] = await FacturaService.listarListasPrecio();

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
      numeroFacturaParam = factura.numeroFactura.toString();

      cargarFacturaEnFormulario({
        id: factura.id,
        numeroFactura: factura.numeroFactura,
        fechaEmision: factura.fechaEmision,
        clienteDenominacion: factura.clienteDenominacion,
        condicionIva: factura.condicionIva,
        puntoVentaDescripcion: factura.puntoVentaDescripcion,
        detalles: (factura.detalles || []).map(d => ({
          id: d.id,
          articuloId: d.articuloId,
          productoDescripcion: d.productoDescripcion,
          cantidad: d.cantidad,
          precioUnitario: d.precioUnitario,
          subtotal: d.subtotal
        }))
      });
            // Preselecciona la lista de precios con la que se creó la factura (se puede cambiar)
      if (factura.listaPrecioId) {
        const listas: ListaPrecioDTO[] = await FacturaService.listarListasPrecio();
        const lista = listas.find(l => l.id === factura.listaPrecioId);
        if (lista) await aplicarListaPrecio(lista, false);
      }
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
    const textoCliente = clienteSelect.options[clienteSelect.selectedIndex]?.text.toLowerCase() || '';
    if (textoCliente.includes('consumidor')) {
      const optCF = Array.from(condicionIvaSelect.options).find(o => o.text.toLowerCase().includes('consumidor'));
      if (optCF) {
        condicionIvaSelect.value = optCF.value;
      }
    }
  });

  btnAgregarItem.addEventListener('click', agregarItem);
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

  btnAbrirExplorerListas.addEventListener('click', abrirExplorerListasPrecio);
  listaPrecioTexto.addEventListener('click', abrirExplorerListasPrecio);
  btnCerrarModalListas.addEventListener('click', cerrarExplorerListasPrecio);
  modalListasPrecio.addEventListener('click', (e) => {
    if (e.target === modalListasPrecio) cerrarExplorerListasPrecio();
  });
});
