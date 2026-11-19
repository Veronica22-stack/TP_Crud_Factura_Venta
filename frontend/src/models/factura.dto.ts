export interface FacturaDetalleReporteDTO {
  id?: number;
  articuloId?: number;
  listaPrecioArticuloId?: number;
  listaPrecioId?: number;
  productoDescripcion: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
  codigo?: string;
  porcentajeBonificacion?: number;
  importeNeto?: number;
  importeIva?: number;
}

export interface FacturaReporteDTO {
  id?: number;
  numeroFactura: number;
  fechaEmision: string;
  clienteId?: number;
  clienteDenominacion: string;
  condicionIva: string;
  puntoVentaDescripcion: string;
  importeTotal: number;
  cantidadItems: number;
  estado?: string;
  observaciones?: string;
  listaPrecioId?: number;
  detalles?: FacturaDetalleReporteDTO[];
  cae?: string;
  caeFechaVencimiento?: string;
  resultadoAfip?: string;
  motivoRechazo?: string;
  nroComprobanteAfip?: number;
  tipoComprobante?: number;      // 1 = A, 6 = B, 11 = C
  clienteCuit?: string;
  condicionIvaId?: number;
  tipoMonedaId?: number;
  monedaCodigoAfip?: string;
  monedaDenominacion?: string;
  importeNetoGravado?: number;
  importeIva?: number;
}

// Totales del panel (GET /api/facturas/resumen)
export interface ResumenFacturacionDTO {
  totalFacturado: number;
  vigentes: number;
  conCae: number;
  pendientes: number;
  anuladas: number;
  ultimasFacturas: FacturaReporteDTO[];
}

export interface MonedaDTO {
  id: number;
  codigoAfip: string;
  codigoIso: string;
  denominacion: string;
  simbolo: string;
}

export interface TipoComprobanteDTO {
  codigo: number;
  nombre: string;
  descripcion: string;
  porDefecto: boolean;
}

export interface FacturaDetalleRequestDTO {
  id?: number;
  articuloId?: number;
  listaPrecioArticuloId?: number;
  productoDescripcion?: string;
  descripcion?: string;
  cantidad: number;
  precioUnitario: number;
  porcentajeBonificacion?: number;
  importeNeto?: number;
  importeIva?: number;
  subtotal?: number;
}

export interface FacturaRequestDTO {
  id?: number;
  numero?: number;
  numeroFactura?: number;
  fechaEmision: string;
  clienteId?: number;
  condicionIvaId?: number;
  puntoVentaId?: number;
  tipoMonedaId?: number;
  listaPrecioId?: number;
  listaPrecioNombre?: string;
  clienteDenominacion?: string;
  condicionIva?: string;
  puntoVentaDescripcion?: string;
  estado?: string;
  observaciones?: string;
  importeTotal?: number;
  cbteTipo?: number;             // 1 = A, 6 = B, 11 = C
  detalles: FacturaDetalleRequestDTO[];
}

export interface FacturaFiltrosDTO {
  numero?: number;
  cliente?: string;
  condicionIva?: string;
  fechaDesde?: string;
  fechaHasta?: string;
  estado?: string;
  montoMinimo?: number;
  listaPrecioId?: number;
  clienteId?: number;
  orden?: string;
  page?: number;
  size?: number;
}

export interface FacturaVentaDetalleDTO {
  id?: number;
  articuloId?: number;
  codigo?: string;
  rubro?: string;
  listaPrecioId?: number;          // lista de precios de este ítem
  listaPrecioArticuloId?: number;  // precio elegido dentro de esa lista
  productoDescripcion: string;
  cantidad: number;
  precioUnitario: number;
  porcentajeBonificacion?: number;
  subtotal?: number;
}

export interface FacturaVentaDTO {
  id?: number;
  numeroFactura?: number;
  fechaEmision: string;
  clienteId?: number;
  clienteDenominacion: string;
  condicionIvaId?: number;
  condicionIva: string;
  puntoVentaId?: number;
  puntoVentaDescripcion: string;
  importeTotal?: number;
  estado?: string;
  observaciones?: string;
  tipoComprobante?: number;
  tipoMonedaId?: number;
  detalles: FacturaVentaDetalleDTO[];
}

// Opciones para combos del formulario
export interface ItemComboDTO {
  id: number;
  descripcion: string;
}

export interface OpcionesComboDTO {
  puntosVenta: ItemComboDTO[];
  condicionesIva: ItemComboDTO[];
  clientes: ItemComboDTO[];
}
