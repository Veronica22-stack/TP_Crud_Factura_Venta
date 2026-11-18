import {
  FacturaReporteDTO,
  FacturaRequestDTO,
  FacturaFiltrosDTO,
  OpcionesComboDTO
} from '../models/factura.dto';
import { PageResponse } from '../types/page';
import { ListaPrecioArticuloDTO } from '../types/listaPrecioArticulo';
import { authFetch, urlConToken } from './authService';

const API_BASE_URL = 'http://localhost:8080/api/facturas';

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
   * Solicita el CAE a AFIP para una factura ya creada
   */
  static async obtenerCae(id: number): Promise<{ cae: string; vencimiento: string }> {
    const res = await authFetch(`${API_BASE_URL}/emitir-afip`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ id })
    });

    const texto = await res.text();
    let data: any = null;
    try { data = JSON.parse(texto); } catch { /* respuesta de texto plano */ }

    if (!res.ok) {
      const obs = data?.feDetResp?.FECAEDetResponse?.[0]?.Observaciones?.Obs;
      const detalle = Array.isArray(obs) ? obs.map((o: any) => `[${o.Code}] ${o.Msg}`).join(' | ') : '';
      throw new Error(detalle || data?.message || texto || 'No se pudo obtener el CAE');
    }

    const det = data?.feDetResp?.FECAEDetResponse?.[0];
    return { cae: det?.CAE, vencimiento: det?.CAEFchVto };
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
    const params = new URLSearchParams();
    if (filtros.numero) params.append('numero', filtros.numero.toString());
    if (filtros.cliente) params.append('cliente', filtros.cliente);
    if (filtros.condicionIva) params.append('condicionIva', filtros.condicionIva);
    if (filtros.fechaDesde) params.append('fechaDesde', filtros.fechaDesde);
    if (filtros.fechaHasta) params.append('fechaHasta', filtros.fechaHasta);
    if (filtros.estado) params.append('estado', filtros.estado);
    if (filtros.montoMinimo) params.append('montoMinimo', filtros.montoMinimo.toString());
    if (filtros.listaPrecioId) params.append('listaPrecioId', filtros.listaPrecioId.toString());
    if (filtros.orden) params.append('orden', filtros.orden);
    return urlConToken(`${API_BASE_URL}/export/pdf?${params.toString()}`);
  }

  /**
   * URL para exportación a Excel
   */
  static getExportExcelUrl(filtros: FacturaFiltrosDTO): string {
    const params = new URLSearchParams();
    if (filtros.numero) params.append('numero', filtros.numero.toString());
    if (filtros.cliente) params.append('cliente', filtros.cliente);
    if (filtros.condicionIva) params.append('condicionIva', filtros.condicionIva);
    if (filtros.fechaDesde) params.append('fechaDesde', filtros.fechaDesde);
    if (filtros.fechaHasta) params.append('fechaHasta', filtros.fechaHasta);
    if (filtros.estado) params.append('estado', filtros.estado);
    if (filtros.montoMinimo) params.append('montoMinimo', filtros.montoMinimo.toString());
    if (filtros.listaPrecioId) params.append('listaPrecioId', filtros.listaPrecioId.toString());
    if (filtros.orden) params.append('orden', filtros.orden);
    return urlConToken(`${API_BASE_URL}/export/excel?${params.toString()}`);
  }

  static async listarArticulosPorLista(listaPrecioId: number): Promise<ListaPrecioArticuloDTO[]> {
    const res = await authFetch(`http://localhost:8080/api/lista-precio-articulos?listaPrecioId=${listaPrecioId}`);
    if (!res.ok) {
      throw new Error('Error al obtener los productos de la lista de precios');
    }
    return res.json();
  }

  static async listarListasPrecio(): Promise<any[]> {
    const res = await authFetch('http://localhost:8080/api/listas-precio');
    if (!res.ok) return [];
    return res.json();
  }
}
