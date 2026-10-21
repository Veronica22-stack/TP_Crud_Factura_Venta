import { FacturaService } from './services/facturaService';
import {
  FacturaVentaDTO,
  FacturaVentaDetalleDTO,
  FacturaRequestDTO,
  OpcionesComboDTO,
  ArticuloComboDTO
} from './models/factura.dto';

// Estado del formulario
let modoEdicion = false;
let facturaIdEdicion: number | null = null;
let numeroFacturaParam = '';
let siguienteDetalleId = 1;
let opcionesDisponibles: OpcionesComboDTO | null = null;
let detalles: FacturaVentaDetalleDTO[] = [];

// Elementos del DOM
const formTitulo = document.getElementById('formTitulo') as HTMLElement;
const facturaForm = document.getElementById('facturaForm') as HTMLFormElement;
const numeroFacturaInput = document.getElementById('numeroFacturaInput') as HTMLInputElement;
const fechaEmisionInput = document.getElementById('fechaEmisionInput') as HTMLInputElement;
const puntoVentaSelect = document.getElementById('puntoVentaSelect') as HTMLSelectElement;
const clienteSelect = document.getElementById('clienteSelect') as HTMLSelectElement;
const condicionIvaSelect = document.getElementById('condicionIvaSelect') as HTMLSelectElement;
const btnAgregarItem = document.getElementById('btnAgregarItem') as HTMLButtonElement;
const tbodyDetalles = document.getElementById('detallesTableBody') as HTMLTableSectionElement;
const importeTotalDisplay = document.getElementById('importeTotalDisplay') as HTMLElement;
const btnCancelar = document.getElementById('btnCancelar') as HTMLButtonElement;

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

  if (factura.puntoVentaId) {
    puntoVentaSelect.value = factura.puntoVentaId.toString();
  } else if (factura.puntoVentaDescripcion) {
    const opt = Array.from(puntoVentaSelect.options).find(o => o.text.includes(factura.puntoVentaDescripcion));
    if (opt) puntoVentaSelect.value = opt.value;
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
    puntoVentaId: parseInt(puntoVentaSelect.value, 10) || undefined,
    puntoVentaDescripcion: puntoVentaSelect.options[puntoVentaSelect.selectedIndex]?.text || '',
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

// Renderizado dinámico de la tabla de ítems / renglones
function renderizarDetalles(): void {
  if (detalles.length === 0) {
    tbodyDetalles.innerHTML = `
      <tr>
        <td colspan="5" class="text-center" style="padding: 24px; color: #64748b;">
          No hay ítems agregados a la factura. Haga clic en "+ Agregar Ítem" para comenzar.
        </td>
      </tr>
    `;
    actualizarImporteTotal();
    return;
  }

  tbodyDetalles.innerHTML = detalles.map((d, index) => {
    const articulos = opcionesDisponibles?.articulos || [];
    const optionsHtml = articulos.map(art => `
      <option value="${art.id}" data-precio="${art.precioVenta}" data-nombre="${art.denominacion}" ${art.id === d.articuloId || art.denominacion === d.productoDescripcion ? 'selected' : ''}>
        ${art.denominacion} ($ ${art.precioVenta.toFixed(2)})
      </option>
    `).join('');

    return `
      <tr data-index="${index}">
        <td>
          <select class="form-control select-producto" data-index="${index}">
            ${optionsHtml || `<option value="">${d.productoDescripcion || 'Seleccionar producto'}</option>`}
          </select>
        </td>
        <td>
          <input type="number" min="1" step="1" class="form-control input-cantidad" data-index="${index}" value="${d.cantidad}">
        </td>
        <td>
          <input type="number" min="0" step="0.01" class="form-control input-precio" data-index="${index}" value="${d.precioUnitario}">
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

  // Vincular eventos a inputs y botones
  tbodyDetalles.querySelectorAll('.select-producto').forEach(select => {
    select.addEventListener('change', (e) => {
      const target = e.target as HTMLSelectElement;
      const index = parseInt(target.getAttribute('data-index') || '0', 10);
      const opt = target.options[target.selectedIndex];
      const precio = parseFloat(opt.getAttribute('data-precio') || '0');
      const nombre = opt.getAttribute('data-nombre') || opt.text;
      const artId = parseInt(opt.value, 10);

      detalles[index].articuloId = artId;
      detalles[index].productoDescripcion = nombre;
      detalles[index].precioUnitario = precio;
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
      // Actualizar celda subtotal
      const row = tbodyDetalles.querySelector(`tr[data-index="${index}"]`);
      if (row) {
        const subtotalCell = row.children[3];
        if (subtotalCell) subtotalCell.textContent = formatearMoneda(detalles[index].subtotal ?? 0);
      }
    });
  });

  tbodyDetalles.querySelectorAll('.input-precio').forEach(input => {
    input.addEventListener('input', (e) => {
      const target = e.target as HTMLInputElement;
      const index = parseInt(target.getAttribute('data-index') || '0', 10);
      detalles[index].precioUnitario = parseFloat(target.value) || 0;
      detalles[index].subtotal = calcularSubtotal(detalles[index]);
      actualizarImporteTotal();
      const row = tbodyDetalles.querySelector(`tr[data-index="${index}"]`);
      if (row) {
        const subtotalCell = row.children[3];
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

// Agregar nuevo ítem al renglón
function agregarItem(): void {
  const articulos = opcionesDisponibles?.articulos || [];
  let primerArticulo: ArticuloComboDTO | undefined;

  // Si ya tenemos items, elegir el siguiente producto disponible para comodidad
  if (articulos.length > 0) {
    const usados = new Set(detalles.map(d => d.articuloId));
    primerArticulo = articulos.find(a => !usados.has(a.id)) || articulos[0];
  }

  const nuevoDetalle: FacturaVentaDetalleDTO = {
    id: siguienteDetalleId++,
    articuloId: primerArticulo?.id,
    productoDescripcion: primerArticulo?.denominacion || 'Nuevo Ítem',
    cantidad: 1,
    precioUnitario: primerArticulo?.precioVenta || 100,
    subtotal: (primerArticulo?.precioVenta || 100) * 1
  };

  detalles.push(nuevoDetalle);
  renderizarDetalles();
  actualizarImporteTotal();
}

// Cargar combos auxiliares desde el backend
async function cargarOpciones(): Promise<void> {
  try {
    opcionesDisponibles = await FacturaService.obtenerOpciones();

    // Llenar Punto de Venta
    puntoVentaSelect.innerHTML = '<option value="">Seleccionar...</option>' + opcionesDisponibles.puntosVenta.map(pv => `      <option value="${pv.id}">${pv.descripcion}</option>
    `).join('');

    // Preseleccionar PV 6 si existe
    const pv6 = opcionesDisponibles.puntosVenta.find(p => p.descripcion.includes('6'));
    if (pv6) puntoVentaSelect.value = pv6.id.toString();

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
    puntoVentaSelect.innerHTML = `<option value="1">1 - Sucursal Central</option><option value="6" selected>6 - Punto de Venta 6</option>`;
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

  if (detalles.length === 0) {
    alert('Debe agregar al menos un ítem a la factura antes de guardar.');
    return;
  }

  const facturaDTO = leerFacturaDesdeFormulario();

  const request: FacturaRequestDTO = {
    numero: facturaDTO.numeroFactura,
    fechaEmision: facturaDTO.fechaEmision,
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
});
