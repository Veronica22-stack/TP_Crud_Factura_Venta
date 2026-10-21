import {
  FacturaReporteDTO,
  FacturaRequestDTO,
  FacturaFiltrosDTO,
  OpcionesComboDTO
} from '../models/factura.dto';
import { PageResponse } from '../types/page';

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

    params.append('page', (filtros.page ?? 0).toString());
    params.append('size', (filtros.size ?? 5).toString());

    const res = await fetch(`${API_BASE_URL}?${params.toString()}`);
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
    const res = await fetch(`${API_BASE_URL}/${id}`);
    if (!res.ok) {
      throw new Error(`Error al obtener factura con ID ${id}`);
    }
    return res.json();
  }

  /**
   * Crea una nueva factura
   */
  static async crearFactura(factura: FacturaRequestDTO): Promise<FacturaReporteDTO> {
    const res = await fetch(API_BASE_URL, {
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
    const res = await fetch(`${API_BASE_URL}/${id}`, {
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
   * Actualiza el estado de una factura
   */
  static async actualizarEstado(id: number, estado: string, observaciones?: string): Promise<FacturaReporteDTO> {
    const body: Partial<FacturaRequestDTO> = { estado, observaciones };

    const res = await fetch(`${API_BASE_URL}/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(body)
    });

    if (!res.ok) {
      const err = await res.json().catch(() => ({ message: res.statusText }));
      throw new Error(err.message || 'Error al actualizar estado de la factura');
    }

    return res.json();
  }

  /**
   * Elimina una factura por ID
   */
  static async eliminarFactura(id: number): Promise<void> {
    const res = await fetch(`${API_BASE_URL}/${id}`, {
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
    const res = await fetch(`${API_BASE_URL}/opciones`);
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
    return `${API_BASE_URL}/export/pdf?${params.toString()}`;
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
    return `${API_BASE_URL}/export/excel?${params.toString()}`;
  }

  static async listarListasPrecio(): Promise<any[]> {
    const res = await fetch('http://localhost:8080/api/listas-precio');
    if (!res.ok) return [];
    return res.json();
  }
}
