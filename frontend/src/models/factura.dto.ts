export interface FacturaDetalleReporteDTO {
  id?: number;
  articuloId?: number;
  listaPrecioArticuloId?: number;
  listaPrecioId?: number;
  productoDescripcion: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
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
