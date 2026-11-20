import {
  FacturaReporteDTO,
  FacturaRequestDTO,
  FacturaFiltrosDTO,
  OpcionesComboDTO,
  ResumenFacturacionDTO,
  MonedaDTO,
  TipoComprobanteDTO
} from '../models/factura.dto';
import { PageResponse } from '../types/page';
import { BusquedaProductosDTO, ListaPrecioArticuloDTO } from '../types/listaPrecioArticulo';
import { authFetch, urlConToken } from './authService';

const API_BASE_URL = 'http://localhost:8080/api/facturas';
const API_URL = 'http://localhost:8080/api';

// Filtros del listado como parámetros de la URL (los usan el listado y las exportaciones)
function paramsFiltros(filtros: FacturaFiltrosDTO): URLSearchParams {
  const params = new URLSearchParams();
  if (filtros.numero) params.append('numero', filtros.numero.toString());
  if (filtros.cliente) params.append('cliente', filtros.cliente);
  if (filtros.clienteId) params.append('clienteId', filtros.clienteId.toString());
  if (filtros.condicionIva) params.append('condicionIva', filtros.condicionIva);
  if (filtros.fechaDesde) params.append('fechaDesde', filtros.fechaDesde);
  if (filtros.fechaHasta) params.append('fechaHasta', filtros.fechaHasta);
  if (filtros.estado) params.append('estado', filtros.estado);
  if (filtros.montoMinimo) params.append('montoMinimo', filtros.montoMinimo.toString());
  if (filtros.listaPrecioId) params.append('listaPrecioId', filtros.listaPrecioId.toString());
  if (filtros.orden) params.append('orden', filtros.orden);
  return params;
}

async function leerError(res: Response, porDefecto: string): Promise<Error> {
  const err = await res.json().catch(() => ({ message: res.statusText }));
  return new Error(err.message || porDefecto);
}

export class FacturaService {
  /**
   * Obtiene facturas con filtros y paginación
   */
  static async obtenerFacturas(filtros: FacturaFiltrosDTO): Promise<PageResponse<FacturaReporteDTO>> {
    const params = new URLSearchParams();

    if (filtros.numero !== undefined && filtros.numero !== null) {
      params.append('numero', filtros.numero.toString());
    }
    if (filtros.cliente) {
      params.append('cliente', filtros.cliente);
    }
    if (filtros.clienteId) {
      params.append('clienteId', filtros.clienteId.toString());
    }
    if (filtros.condicionIva) {
      params.append('condicionIva', filtros.condicionIva);
    }
    if (filtros.fechaDesde) {
      params.append('fechaDesde', filtros.fechaDesde);
    }
    if (filtros.fechaHasta) {
      params.append('fechaHasta', filtros.fechaHasta);
    }
    if (filtros.estado) {
      params.append('estado', filtros.estado);
    }
    if (filtros.montoMinimo !== undefined && filtros.montoMinimo !== null) {
      params.append('montoMinimo', filtros.montoMinimo.toString());
    }
    if (filtros.listaPrecioId !== undefined && filtros.listaPrecioId !== null) {
      params.append('listaPrecioId', filtros.listaPrecioId.toString());
    }

    if (filtros.orden) {
      params.append('orden', filtros.orden);
    }

    params.append('page', (filtros.page ?? 0).toString());
    params.append('size', (filtros.size ?? 5).toString());

    const res = await authFetch(`${API_BASE_URL}?${params.toString()}`);
    if (!res.ok) {
      const errorText = await res.text();
      throw new Error(`Error ${res.status}: ${errorText || res.statusText}`);
    }
    return res.json();
  }

  /**
   * Obtiene una factura por ID con sus detalles
   */
  static async obtenerFacturaPorId(id: number): Promise<FacturaReporteDTO> {
    const res = await authFetch(`${API_BASE_URL}/${id}`);
    if (!res.ok) {
      throw new Error(`Error al obtener factura con ID ${id}`);
    }
    return res.json();
  }

  /**
   * Crea una nueva factura
   */
  static async crearFactura(factura: FacturaRequestDTO): Promise<FacturaReporteDTO> {
    const res = await authFetch(API_BASE_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(factura)
    });

    if (!res.ok) {
      const err = await res.json().catch(() => ({ message: res.statusText }));
      throw new Error(err.message || 'Error al crear la factura');
    }

    return res.json();
  }

  /**
   * Actualiza una factura completa existente (cabecera e ítems)
   */
  static async actualizarFactura(id: number, factura: FacturaRequestDTO): Promise<FacturaReporteDTO> {
    const res = await authFetch(`${API_BASE_URL}/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(factura)
    });

    if (!res.ok) {
      const err = await res.json().catch(() => ({ message: res.statusText }));
      throw new Error(err.message || 'Error al actualizar la factura');
    }

    return res.json();
  }

  /**
   * Anula una factura (baja lógica): queda guardada, marcada como ANULADA
   */
  static async anularFactura(id: number): Promise<void> {
    const res = await authFetch(`${API_BASE_URL}/${id}/anular`, { method: 'PUT' });
    if (!res.ok) {
      const err = await res.json().catch(() => ({ message: res.statusText }));
      throw new Error(err.message || 'Error al anular la factura');
    }
  }

  /**
   * Solicita el CAE a ARCA para una factura ya creada (real o simulado según afip.modo).
   * Si ARCA la rechaza, la factura queda guardada con el motivo y se informa como error.
   */
  static async obtenerCae(id: number, tipoComprobante?: number): Promise<FacturaReporteDTO> {
    const res = await authFetch(`${API_BASE_URL}/${id}/emitir`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(tipoComprobante ? { tipoComprobante } : {})
    });
    if (!res.ok) throw await leerError(res, 'No se pudo obtener el CAE');

    const factura: FacturaReporteDTO = await res.json();
    if (!factura.cae) {
      throw new Error(`ARCA rechazó el comprobante: ${factura.motivoRechazo || 'sin detalle'}`);
    }
    return factura;
  }

  /**
   * Totales del panel (total facturado, con CAE, pendientes, anuladas y últimas facturas)
   */
  static async obtenerResumen(ultimas = 5): Promise<ResumenFacturacionDTO> {
    const res = await authFetch(`${API_BASE_URL}/resumen?ultimas=${ultimas}`);
    if (!res.ok) throw await leerError(res, 'No se pudo obtener el resumen');
    return res.json();
  }

  static async listarMonedas(): Promise<MonedaDTO[]> {
    const res = await authFetch(`${API_URL}/catalogos/monedas`);
    if (!res.ok) return [];
    return res.json();
  }

  static async listarTiposComprobante(): Promise<TipoComprobanteDTO[]> {
    const res = await authFetch(`${API_URL}/catalogos/tipos-comprobante`);
    if (!res.ok) return [];
    return res.json();
  }

  // --- Integración ARCA ---
  static async estadoArca(): Promise<any> {
    const res = await authFetch(`${API_URL}/afip/estado`);
    if (!res.ok) throw await leerError(res, 'No se pudo consultar el estado de ARCA');
    return res.json();
  }

  static async probarConexionArca(): Promise<any> {
    const res = await authFetch(`${API_URL}/afip/dummy`, { method: 'POST' });
    if (!res.ok) throw await leerError(res, 'No se pudo probar la conexión con ARCA');
    return res.json();
  }

  static async sincronizarMonedas(): Promise<any> {
    const res = await authFetch(`${API_URL}/afip/monedas/sincronizar`, { method: 'POST' });
    if (!res.ok) throw await leerError(res, 'No se pudieron sincronizar las monedas');
    return res.json();
  }

  /**
   * URL del PDF de una factura con CAE
   */
  static getPdfFacturaUrl(id: number): string {
    return urlConToken(`${API_BASE_URL}/${id}/pdf`);
  }

  /**
   * Elimina una factura por ID
   */
  static async eliminarFactura(id: number): Promise<void> {
    const res = await authFetch(`${API_BASE_URL}/${id}`, {
      method: 'DELETE'
    });

    if (!res.ok) {
      const err = await res.json().catch(() => ({ message: res.statusText }));
      throw new Error(err.message || 'Error al eliminar la factura');
    }
  }

  /**
   * Carga opciones auxiliares para alimentar los selects del formulario
   */
  static async obtenerOpciones(): Promise<OpcionesComboDTO> {
    const res = await authFetch(`${API_BASE_URL}/opciones`);
    if (!res.ok) {
      throw new Error('Error al obtener opciones para formularios');
    }
    return res.json();
  }

  /**
   * URL para exportación a PDF
   */
  static getExportPdfUrl(filtros: FacturaFiltrosDTO): string {
    return urlConToken(`${API_BASE_URL}/export/pdf?${paramsFiltros(filtros).toString()}`);
  }

  /**
   * URL para exportación a Excel
   */
  static getExportExcelUrl(filtros: FacturaFiltrosDTO): string {
    return urlConToken(`${API_BASE_URL}/export/excel?${paramsFiltros(filtros).toString()}`);
  }

  /**
   * URL para exportación a texto separado por tabulaciones (.txt)
   */
  static getExportTxtUrl(filtros: FacturaFiltrosDTO): string {
    return urlConToken(`${API_BASE_URL}/export/txt?${paramsFiltros(filtros).toString()}`);
  }

  /**
   * Explorador de productos paginado en el backend (LIMIT / OFFSET): solo viaja la página pedida.
   * Filtros: lista de precios, rubro (exacto), código y denominación (contienen, sin importar tildes).
   */
  static async buscarProductos(filtros: BusquedaProductosDTO): Promise<PageResponse<ListaPrecioArticuloDTO>> {
    const params = new URLSearchParams({ pagina: String(filtros.pagina), tamanio: String(filtros.tamanio) });
    params.append('listaPrecioId', String(filtros.listaPrecioId));
    if (filtros.rubro) params.append('rubro', filtros.rubro);
    if (filtros.codigo?.trim()) params.append('codigo', filtros.codigo.trim());
    if (filtros.denominacion?.trim()) params.append('denominacion', filtros.denominacion.trim());

    const res = await authFetch(`${API_URL}/catalogos/productos?${params.toString()}`);
    if (!res.ok) throw await leerError(res, 'Error al obtener los productos de la lista de precios');
    return res.json();
  }

  /** Rubros que tienen productos en la lista (para el combo del explorador) */
  static async listarRubros(listaPrecioId: number): Promise<string[]> {
    const res = await authFetch(`${API_URL}/catalogos/rubros?listaPrecioId=${listaPrecioId}`);
    if (!res.ok) return [];
    return res.json();
  }

  static async listarListasPrecio(): Promise<any[]> {
    const res = await authFetch('http://localhost:8080/api/listas-precio');
    if (!res.ok) return [];
    return res.json();
  }
}
