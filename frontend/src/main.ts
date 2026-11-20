import { FacturaService } from './services/facturaService';
import { ClienteService } from './services/clienteService';
import { FacturaFiltrosDTO, FacturaReporteDTO } from './models/factura.dto';
import { ClienteDTO } from './types/cliente';
import { PageResponse } from './types/page';
import { renderizarPaginacion as renderizarPaginacionModal, limpiarPaginacion } from './paginacion';

// Filas por página del explorador de clientes: el backend devuelve solo esa página (LIMIT / OFFSET)
const TAMANIO_PAGINA_CLIENTES = 8;

// Estado global de la aplicación
const state: {
  filtros: FacturaFiltrosDTO;
  facturasPage?: PageResponse<FacturaReporteDTO>;
  clientes: ClienteDTO[];
} = {
  filtros: {
    page: 0,
    size: 5,
    orden: 'numero_desc'
  },
  clientes: []
};

// Elementos del DOM
const btnNuevaFactura = document.getElementById('btnNuevaFactura') as HTMLButtonElement;
const tbodyFacturas = document.getElementById('facturasTableBody') as HTMLTableSectionElement;
const paginationInfo = document.getElementById('paginationInfo') as HTMLElement;
const btnPrevPage = document.getElementById('btnPrevPage') as HTMLButtonElement;
const btnNextPage = document.getElementById('btnNextPage') as HTMLButtonElement;
const pageNumbersContainer = document.getElementById('pageNumbers') as HTMLElement;
const selectOrden = document.getElementById('selectOrden') as HTMLSelectElement;
const selectTamanioPagina = document.getElementById('selectTamanioPagina') as HTMLSelectElement;

const inputNumero = document.getElementById('filtroNumero') as HTMLInputElement;
const inputCliente = document.getElementById('filtroCliente') as HTMLInputElement;
const selectCondicionIva = document.getElementById('filtroCondicionIva') as HTMLSelectElement;
const selectEstado = document.getElementById('filtroEstado') as HTMLSelectElement;
const inputFechaDesde = document.getElementById('filtroFechaDesde') as HTMLInputElement;
const inputFechaHasta = document.getElementById('filtroFechaHasta') as HTMLInputElement;

const btnBuscar = document.getElementById('btnBuscar') as HTMLButtonElement;
const btnLimpiar = document.getElementById('btnLimpiar') as HTMLButtonElement;
const btnExportPdf = document.getElementById('btnExportPdf') as HTMLAnchorElement;
const btnExportExcel = document.getElementById('btnExportExcel') as HTMLAnchorElement;
const btnExportTxt = document.getElementById('btnExportTxt') as HTMLAnchorElement;
const btnEstadoArca = document.getElementById('btnEstadoArca') as HTMLButtonElement;

// Modal Explorer de Clientes
const modalClientes = document.getElementById('modalClientes') as HTMLElement;
const btnAbrirExplorer = document.getElementById('btnAbrirExplorer') as HTMLButtonElement;
const btnCerrarModal = document.getElementById('btnCerrarModal') as HTMLButtonElement;
const inputBuscarCliente = document.getElementById('inputBuscarCliente') as HTMLInputElement;
const tbodyClientes = document.getElementById('clientesTableBody') as HTMLTableSectionElement;
const paginacionClientes = document.getElementById('paginacionClientes') as HTMLElement;

// Formato de moneda
function formatearMoneda(monto: number): string {
  return new Intl.NumberFormat('es-AR', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(monto);
}

// Formato de fecha
function formatearFecha(fechaStr?: string): string {
  if (!fechaStr) return '-';
  const fecha = new Date(fechaStr);
  if (isNaN(fecha.getTime())) return fechaStr;
  return fecha.toLocaleDateString('es-AR', { timeZone: 'UTC' });
}

// Cargar y renderizar facturas
export async function cargarFacturas() {
  tbodyFacturas.innerHTML = `<tr><td colspan="9" class="text-center" style="padding: 24px;">Cargando facturas...</td></tr>`;

  try {
    const data = await FacturaService.obtenerFacturas(state.filtros);
    state.facturasPage = data;

    renderizarTablaFacturas(data.content);
    renderizarPaginacion(data);
    actualizarEnlacesExportacion();
  } catch (err: any) {
    tbodyFacturas.innerHTML = `
      <tr>
        <td colspan="9" class="text-center" style="padding: 24px; color: #dc2626;">
          Error al conectar con el backend.
        </td>
      </tr>
    `;
    paginationInfo.textContent = 'Error al cargar datos.';
  }
}

const TIPOS_COMPROBANTE: Record<number, string> = { 1: 'A', 6: 'B', 11: 'C' };

function escaparHtml(texto?: string): string {
  return (texto ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c] as string));
}

// Estado de la integración con ARCA: configuración y prueba de conexión (solo informa)
async function mostrarEstadoArca() {
  try {
    const estado = await FacturaService.estadoArca();
    const prueba = await FacturaService.probarConexionArca();
    const servidores = prueba.servidores
      ? `\nServidores: app ${prueba.servidores.appServer}, base ${prueba.servidores.dbServer}, auth ${prueba.servidores.authServer}`
      : '';
    const mensaje = `${estado.mensaje}\n\nModo: ${estado.modo}\nCUIT: ${estado.cuit || '-'}\n`
      + `Credenciales: ${estado.credencialesConfiguradas ? 'configuradas' : 'faltan'}\n`
      + `Comprobante por defecto: ${TIPOS_COMPROBANTE[estado.tipoComprobantePorDefecto] ?? estado.tipoComprobantePorDefecto}\n\n`
      + `Prueba de conexión: ${prueba.mensaje}${servidores}`;
    alert(mensaje);
  } catch (err: any) {
    alert(`No se pudo consultar ARCA: ${err.message}`);
  }
}

// Renderizar tabla
function renderizarTablaFacturas(facturas: FacturaReporteDTO[]) {
  if (facturas.length === 0) {
    tbodyFacturas.innerHTML = `<tr><td colspan="9" class="text-center" style="padding: 24px;">No se encontraron facturas con los filtros seleccionados.</td></tr>`;
    return;
  }

  tbodyFacturas.innerHTML = facturas.map(f => {
    const estado = f.estado || 'EMITIDA';
    const badgeClass = estado === 'ANULADA' ? 'badge-anulada' : 'badge-emitida';
    // Informada a ARCA (con CAE): no se edita ni se elimina, solo se anula (baja lógica)
    const informada = !!f.cae;
    const anulada = estado === 'ANULADA';

    return `
      <tr data-id="${f.id ?? ''}">
        <td><strong>${f.numeroFactura}</strong></td>
        <td>${formatearFecha(f.fechaEmision)}</td>
        <td>${f.clienteDenominacion}</td>
        <td>${f.condicionIva || 'IVA Responsable Inscripto'}</td>
        <td>${f.puntoVentaDescripcion || 'Sucursal Central'}</td>
        <td>${formatearMoneda(f.importeTotal)}</td>
        <td>${f.cantidadItems}</td>
        <td>
          <span class="badge ${badgeClass}">${estado}</span>
          ${f.tipoComprobante ? `<div class="cae-texto">Factura ${TIPOS_COMPROBANTE[f.tipoComprobante] ?? f.tipoComprobante}</div>` : ''}
          ${f.cae ? `<div class="cae-texto">CAE: ${f.cae}</div>` : ''}
          ${!f.cae && f.motivoRechazo ? `<div class="rechazo-texto" title="${escaparHtml(f.motivoRechazo)}">Rechazada por ARCA: ${escaparHtml(f.motivoRechazo)}</div>` : ''}
        </td>
        <td class="text-center">
          <div class="actions-cell">
            ${/* Sin CAE: editar, obtener CAE, anular y eliminar. Sin CAE y anulada: solo eliminar.
                 Con CAE: solo anular (baja lógica) e imprimir. */ ''}
            ${!informada && !anulada ? `<button class="btn btn-sm btn-edit" data-id="${f.id ?? ''}" title="Editar Factura">Editar</button>` : ''}
            ${!informada && !anulada ? `<button class="btn btn-sm btn-cae" data-id="${f.id ?? ''}" data-numero="${f.numeroFactura}" title="Obtener CAE de AFIP">Obtener CAE</button>` : ''}
            ${!anulada ? `<button class="btn btn-sm btn-anular" data-id="${f.id ?? ''}" data-numero="${f.numeroFactura}" data-cae="${informada ? '1' : ''}" title="Anular factura (baja lógica)">Anular</button>` : ''}
            ${!informada ? `<button class="btn btn-sm btn-delete btn-eliminar" data-id="${f.id ?? ''}" data-numero="${f.numeroFactura}" title="Eliminar factura (todavía no fue informada a ARCA)">Eliminar</button>` : ''}
            ${informada ? `<button class="btn btn-sm btn-imprimir" data-id="${f.id ?? ''}" title="Imprimir factura en PDF">Imprimir PDF</button>` : ''}
          </div>
        </td>
      </tr>
    `;
  }).join('');

  // Vincular eventos a botones de acción
    tbodyFacturas.querySelectorAll('.btn-edit').forEach(btn => {
    btn.addEventListener('click', (e) => {
      const target = e.currentTarget as HTMLElement;
      const id = target.getAttribute('data-id');
      window.location.href = `./factura-form.html?id=${id}`;
    });
  });

  tbodyFacturas.querySelectorAll('.btn-anular').forEach(btn => {
    btn.addEventListener('click', async (e) => {
      const target = e.currentTarget as HTMLElement;
      const id = parseInt(target.getAttribute('data-id') || '0', 10);
      const numero = target.getAttribute('data-numero') || '';
      const conCae = target.getAttribute('data-cae') === '1';
      const mensaje = conCae
        ? `¿Está seguro de que desea anular la factura N° ${numero}? Ya fue informada a ARCA: queda guardada como ANULADA (baja lógica).`
        : `¿Está seguro de que desea anular la factura N° ${numero}? Queda guardada como ANULADA (baja lógica) y después solo se podrá eliminar.`;

      if (id && confirm(mensaje)) {
        try {
          await FacturaService.anularFactura(id);
          alert(`Factura N° ${numero} anulada correctamente.`);
          cargarFacturas();
        } catch (err: any) {
          alert(`Error al anular: ${err.message}`);
        }
      }
    });
  });

  tbodyFacturas.querySelectorAll('.btn-eliminar').forEach(btn => {
    btn.addEventListener('click', async (e) => {
      const target = e.currentTarget as HTMLElement;
      const id = parseInt(target.getAttribute('data-id') || '0', 10);
      const numero = target.getAttribute('data-numero') || '';

      if (id && confirm(`¿Eliminar la factura N° ${numero}? Todavía no fue informada a ARCA, así que se borra definitivamente.`)) {
        try {
          await FacturaService.eliminarFactura(id);
          alert(`Factura N° ${numero} eliminada.`);
          cargarFacturas();
        } catch (err: any) {
          alert(`No se pudo eliminar: ${err.message}`);
        }
      }
    });
  });

  tbodyFacturas.querySelectorAll('.btn-cae').forEach(btn => {
    btn.addEventListener('click', async (e) => {
      const target = e.currentTarget as HTMLButtonElement;
      const id = parseInt(target.getAttribute('data-id') || '0', 10);
      const numero = target.getAttribute('data-numero') || '';

      if (id && confirm(`¿Solicitar el CAE a AFIP para la factura N° ${numero}? Una vez generado, la factura no se podrá eliminar.`)) {
        target.disabled = true;
        target.textContent = 'Solicitando...';
        try {
          const r = await FacturaService.obtenerCae(id);
          alert(`CAE obtenido: ${r.cae}\nVencimiento: ${formatearFecha(r.caeFechaVencimiento)}`);
        } catch (err: any) {
          alert(`No se pudo obtener el CAE: ${err.message}`);
        }
        cargarFacturas();
      }
    });
  });

  tbodyFacturas.querySelectorAll('.btn-imprimir').forEach(btn => {
    btn.addEventListener('click', (e) => {
      const id = parseInt((e.currentTarget as HTMLElement).getAttribute('data-id') || '0', 10);
      if (id) window.open(FacturaService.getPdfFacturaUrl(id), '_blank');
    });
  });
}

// Renderizar controles de paginación
function renderizarPaginacion(data: PageResponse<FacturaReporteDTO>) {
  const total = data.totalElements;
  const start = total === 0 ? 0 : data.pageNumber * data.pageSize + 1;
  const end = Math.min((data.pageNumber + 1) * data.pageSize, total);

  paginationInfo.textContent = `Mostrando ${start} a ${end} de ${total} facturas`;

  btnPrevPage.disabled = data.first;
  btnNextPage.disabled = data.last;

  pageNumbersContainer.innerHTML = '';
  for (let i = 0; i < data.totalPages; i++) {
    const btn = document.createElement('button');
    btn.className = `page-btn-num ${i === data.pageNumber ? 'active' : ''}`;
    btn.textContent = (i + 1).toString();
    btn.addEventListener('click', () => {
      state.filtros.page = i;
      cargarFacturas();
    });
    pageNumbersContainer.appendChild(btn);
  }
}

// Actualizar enlaces de exportación PDF y Excel
function actualizarEnlacesExportacion() {
  btnExportPdf.href = FacturaService.getExportPdfUrl(state.filtros);
  btnExportExcel.href = FacturaService.getExportExcelUrl(state.filtros);
  if (btnExportTxt) btnExportTxt.href = FacturaService.getExportTxtUrl(state.filtros);
}

// Aplicar filtros desde el formulario
function aplicarFiltros() {
  if (inputFechaDesde.value && inputFechaHasta.value && inputFechaHasta.value < inputFechaDesde.value) {
    alert('La "Fecha Hasta" no puede ser anterior a la "Fecha Desde".');
    inputFechaHasta.focus();
    return;
  }
  const numVal = inputNumero.value.trim();
  // Si el texto no es un número válido (por ejemplo solo "-"), el input lo deja vacío pero marca badInput
  if (inputNumero.validity.badInput || (numVal && !/^\d+$/.test(numVal)) || (numVal && parseInt(numVal, 10) < 1)) {
    alert('El número de factura debe ser un número entero mayor a cero.');
    inputNumero.focus();
    return;
  }
  state.filtros.numero = numVal ? parseInt(numVal, 10) : undefined;
  state.filtros.cliente = inputCliente.value.trim() || undefined;
  state.filtros.condicionIva = selectCondicionIva.value || undefined;
  state.filtros.estado = selectEstado ? selectEstado.value || undefined : undefined;
  state.filtros.fechaDesde = inputFechaDesde.value || undefined;
  state.filtros.fechaHasta = inputFechaHasta.value || undefined;
  state.filtros.page = 0;

  cargarFacturas();
}

// Limpiar filtros
function limpiarFiltros() {
  inputNumero.value = '';
  inputCliente.value = '';
  selectCondicionIva.value = '';
  if (selectEstado) selectEstado.value = '';
  inputFechaDesde.value = '';
  inputFechaHasta.value = '';

  state.filtros = {
    page: 0,
    size: state.filtros.size ?? 5,
    orden: state.filtros.orden ?? 'numero_desc'
  };

  cargarFacturas();
}

// Modal Explorador de Clientes
async function abrirExplorerClientes() {
  modalClientes.classList.remove('hidden');
  inputBuscarCliente.value = '';
  await cargarClientesEnModal(0);
}

function cerrarExplorerClientes() {
  modalClientes.classList.add('hidden');
}

// Pide al backend la página de clientes (LIMIT / OFFSET) que coincide con lo escrito
let consultaClientes = 0; // descarta respuestas de búsquedas anteriores
async function cargarClientesEnModal(pagina: number) {
  const consulta = ++consultaClientes;
  tbodyClientes.innerHTML = `<tr><td colspan="5" class="text-center">Cargando clientes...</td></tr>`;

  try {
    const resultado = await ClienteService.buscarClientes(inputBuscarCliente.value, pagina, TAMANIO_PAGINA_CLIENTES);
    if (consulta !== consultaClientes) return; // llegó la respuesta de una búsqueda vieja
    const clientes = resultado.content;
    state.clientes = clientes;
    renderizarPaginacionModal(paginacionClientes, resultado, 'clientes', p => cargarClientesEnModal(p));

    if (clientes.length === 0) {
      tbodyClientes.innerHTML = `<tr><td colspan="5" class="text-center">No se encontraron clientes.</td></tr>`;
      return;
    }

    tbodyClientes.innerHTML = clientes.map(c => `
      <tr>
        <td><strong>${escaparHtml(c.cuitCuil || '-')}</strong></td>
        <td>${escaparHtml(c.denominacion)}</td>
        <td>${escaparHtml(c.email || '-')}</td>
        <td>${escaparHtml(c.telefono || '-')}</td>
        <td class="text-center">
          <button class="btn btn-sm btn-primary btn-seleccionar" data-nombre="${escaparHtml(c.denominacion)}">
            Seleccionar
          </button>
        </td>
      </tr>
    `).join('');

    tbodyClientes.querySelectorAll('.btn-seleccionar').forEach(btn => {
      btn.addEventListener('click', (e) => {
        const target = e.currentTarget as HTMLElement;
        const nombreCliente = target.getAttribute('data-nombre');
        if (nombreCliente) {
          inputCliente.value = nombreCliente;
          cerrarExplorerClientes();
          aplicarFiltros();
        }
      });
    });
  } catch (err) {
    if (consulta !== consultaClientes) return;
    tbodyClientes.innerHTML = `<tr><td colspan="5" class="text-center" style="color: red;">Error al consultar clientes.</td></tr>`;
    limpiarPaginacion(paginacionClientes);
  }
}

// Event Listeners
btnNuevaFactura.addEventListener('click', () => {
  window.location.href = './factura-form.html';
});

btnBuscar.addEventListener('click', aplicarFiltros);
// En el número de factura no se pueden escribir signos ni decimales
inputNumero.addEventListener('keydown', (e) => {
  if (['-', '+', 'e', 'E', '.', ','].includes(e.key)) e.preventDefault();
});
if (btnEstadoArca) btnEstadoArca.addEventListener('click', mostrarEstadoArca);

// Ordenar y cantidad por página: vuelven a la primera página
selectOrden.addEventListener('change', () => {
  state.filtros.orden = selectOrden.value;
  state.filtros.page = 0;
  cargarFacturas();
});

selectTamanioPagina.addEventListener('change', () => {
  state.filtros.size = parseInt(selectTamanioPagina.value, 10) || 5;
  state.filtros.page = 0;
  cargarFacturas();
});
btnLimpiar.addEventListener('click', limpiarFiltros);

btnPrevPage.addEventListener('click', () => {
  if (state.filtros.page && state.filtros.page > 0) {
    state.filtros.page--;
    cargarFacturas();
  }
});

btnNextPage.addEventListener('click', () => {
  if (state.facturasPage && !state.facturasPage.last) {
    state.filtros.page = (state.filtros.page ?? 0) + 1;
    cargarFacturas();
  }
});

btnAbrirExplorer.addEventListener('click', abrirExplorerClientes);
btnCerrarModal.addEventListener('click', cerrarExplorerClientes);

modalClientes.addEventListener('click', (e) => {
  if (e.target === modalClientes) {
    cerrarExplorerClientes();
  }
});

let debounceTimer: any;
inputBuscarCliente.addEventListener('input', () => {
  clearTimeout(debounceTimer);
  debounceTimer = setTimeout(() => {
    cargarClientesEnModal(0); // cada búsqueda nueva vuelve a la página 1
  }, 300);
});

// Inicialización
document.addEventListener('DOMContentLoaded', () => {
  cargarFacturas();
});
