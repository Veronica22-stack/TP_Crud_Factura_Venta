export interface FacturaDetalleDTO {
  id?: number;
  listaPrecioArticuloId?: number;
  productoDescripcion: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
}

export interface FacturaVentaDTO {
  numeroFactura?: number;
  fechaEmision: string;
  clienteDenominacion: string;
  clienteId?: number;
  condicionIva: string;
  puntoVentaDescripcion: string;
  importeTotal?: number;
  detalles: FacturaDetalleDTO[];
}
