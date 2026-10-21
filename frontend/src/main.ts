import { FacturaService } from './services/facturaService';
import { ClienteService } from './services/clienteService';
import { FacturaVentaApi } from './services/facturaVentaApi';
import { FacturaFiltrosDTO, FacturaReporteDTO } from './models/factura.dto';
import { ClienteDTO } from './types/cliente';
import { PageResponse } from './types/page';

// Estado global de la aplicación
const state: {
  filtros: FacturaFiltrosDTO;
  facturasPage?: PageResponse<FacturaReporteDTO>;
  clientes: ClienteDTO[];
} = {
  filtros: {
    page: 0,
    size: 5
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

const inputNumero = document.getElementById('filtroNumero') as HTMLInputElement;
const inputCliente = document.getElementById('filtroCliente') as HTMLInputElement;
const selectCondicionIva = document.getElementById('filtroCondicionIva') as HTMLSelectElement;
const selectEstado = document.getElementById('filtroEstado') as HTMLSelectElement;
const selectListaPrecio = document.getElementById('filtroListaPrecio') as HTMLSelectElement;
const inputFechaDesde = document.getElementById('filtroFechaDesde') as HTMLInputElement;
const inputFechaHasta = document.getElementById('filtroFechaHasta') as HTMLInputElement;

const btnBuscar = document.getElementById('btnBuscar') as HTMLButtonElement;
const btnLimpiar = document.getElementById('btnLimpiar') as HTMLButtonElement;
const btnExportPdf = document.getElementById('btnExportPdf') as HTMLAnchorElement;
const btnExportExcel = document.getElementById('btnExportExcel') as HTMLAnchorElement;

// Modal Explorer de Clientes
const modalClientes = document.getElementById('modalClientes') as HTMLElement;
const btnAbrirExplorer = document.getElementById('btnAbrirExplorer') as HTMLButtonElement;
const btnCerrarModal = document.getElementById('btnCerrarModal') as HTMLButtonElement;
const inputBuscarCliente = document.getElementById('inputBuscarCliente') as HTMLInputElement;
const tbodyClientes = document.getElementById('clientesTableBody') as HTMLTableSectionElement;

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

// Renderizar tabla
function renderizarTablaFacturas(facturas: FacturaReporteDTO[]) {
  if (facturas.length === 0) {
    tbodyFacturas.innerHTML = `<tr><td colspan="9" class="text-center" style="padding: 24px;">No se encontraron facturas con los filtros seleccionados.</td></tr>`;
    return;
  }

  tbodyFacturas.innerHTML = facturas.map(f => {
    const estado = f.estado || 'EMITIDA';
    const badgeClass = estado === 'ANULADA' ? 'badge-anulada' : 'badge-emitida';

    return `
      <tr data-id="${f.id ?? ''}">
        <td><strong>${f.numeroFactura}</strong></td>
        <td>${formatearFecha(f.fechaEmision)}</td>
        <td>${f.clienteDenominacion}</td>
        <td>${f.condicionIva || 'IVA Responsable Inscripto'}</td>
        <td>${f.puntoVentaDescripcion || 'Sucursal Central'}</td>
        <td>${formatearMoneda(f.importeTotal)}</td>
        <td>${f.cantidadItems}</td>
        <td><span class="badge ${badgeClass}">${estado}</span></td>
        <td class="text-center">
          <div class="actions-cell">
            <button class="btn btn-sm btn-edit" data-id="${f.id ?? ''}" title="Editar Factura">Editar</button>
            ${estado !== 'ANULADA' ? `<button class="btn btn-sm btn-anular" data-id="${f.id ?? ''}" data-numero="${f.numeroFactura}" title="Anular Factura">Anular</button>` : ''}
            <button class="btn btn-sm btn-delete" data-id="${f.id ?? ''}" data-numero="${f.numeroFactura}" title="Eliminar Factura">Eliminar</button>
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

      if (id && confirm(`¿Está seguro de que desea anular la factura N° ${numero}?`)) {
        try {
          await FacturaService.actualizarEstado(id, 'ANULADA', 'Anulada desde el frontend');
          alert(`Factura N° ${numero} anulada correctamente.`);
          cargarFacturas();
        } catch (err: any) {
          alert(`Error al anular: ${err.message}`);
        }
      }
    });
  });

  tbodyFacturas.querySelectorAll('.btn-delete').forEach(btn => {
    btn.addEventListener('click', async (e) => {
      const target = e.currentTarget as HTMLElement;
      const id = parseInt(target.getAttribute('data-id') || '0', 10);
      const numero = target.getAttribute('data-numero') || '';

      if (id && confirm(`¿Está seguro de que desea eliminar permanentemente la factura N° ${numero}?`)) {
        try {
          await FacturaService.eliminarFactura(id);
          alert(`Factura N° ${numero} eliminada exitosamente.`);
          cargarFacturas();
        } catch (err: any) {
          alert(`Error al eliminar: ${err.message}`);
        }
      }
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
}

// Aplicar filtros desde el formulario
function aplicarFiltros() {
  const numVal = inputNumero.value.trim();
  state.filtros.numero = numVal ? parseInt(numVal, 10) : undefined;
  state.filtros.cliente = inputCliente.value.trim() || undefined;
  state.filtros.condicionIva = selectCondicionIva.value || undefined;
  state.filtros.estado = selectEstado ? selectEstado.value || undefined : undefined;
  state.filtros.fechaDesde = inputFechaDesde.value || undefined;
  state.filtros.fechaHasta = inputFechaHasta.value || undefined;
  state.filtros.listaPrecioId = selectListaPrecio && selectListaPrecio.value ? Number(selectListaPrecio.value) : undefined;
  state.filtros.page = 0;

  cargarFacturas();
}

// Limpiar filtros
function limpiarFiltros() {
  inputNumero.value = '';
  inputCliente.value = '';
  selectCondicionIva.value = '';
  if (selectEstado) selectEstado.value = '';
  if (selectListaPrecio) selectListaPrecio.value = '';
  inputFechaDesde.value = '';
  inputFechaHasta.value = '';

  state.filtros = {
    page: 0,
    size: 5
  };

  cargarFacturas();
}

// Modal Explorador de Clientes
async function abrirExplorerClientes() {
  modalClientes.classList.remove('hidden');
  inputBuscarCliente.value = '';
  await cargarClientesEnModal();
}

function cerrarExplorerClientes() {
  modalClientes.classList.add('hidden');
}

async function cargarClientesEnModal(buscar?: string) {
  tbodyClientes.innerHTML = `<tr><td colspan="5" class="text-center">Cargando clientes...</td></tr>`;

  try {
    const clientes = await ClienteService.obtenerClientes(buscar);
    state.clientes = clientes;

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
          <button class="btn btn-sm btn-primary btn-seleccionar" data-nombre="${c.denominacion}">
            Seleccionar
          </button>
        </td>
      </tr>
    `).join('');

    document.querySelectorAll('.btn-seleccionar').forEach(btn => {
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
    tbodyClientes.innerHTML = `<tr><td colspan="5" class="text-center" style="color: red;">Error al consultar clientes.</td></tr>`;
  }
}

// Event Listeners
btnNuevaFactura.addEventListener('click', () => {
  window.location.href = './factura-form.html';
});

btnBuscar.addEventListener('click', aplicarFiltros);
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
    cargarClientesEnModal(inputBuscarCliente.value);
  }, 300);
});

async function cargarListasPrecioFiltro() {
  try {
    const listas = await FacturaVentaApi.listarListasPrecio();
    if (selectListaPrecio) {
      selectListaPrecio.innerHTML = '<option value="">Todas las Listas</option>' +
        listas.map((lp: any) => `<option value="${lp.id}">${lp.codigo} - ${lp.denominacion}</option>`).join('');
    }
  } catch (err) {
    console.error('Error al cargar listas de precio:', err);
  }
}

// Inicialización
document.addEventListener('DOMContentLoaded', () => {
  cargarListasPrecioFiltro();
  cargarFacturas();
});
