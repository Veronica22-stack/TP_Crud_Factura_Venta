export interface ListaPrecioDTO {
  id: number;
  codigo: string;
  denominacion: string;
}

export interface ListaPrecioArticuloDTO {
  id: number;
  articuloId: number;
  codigo: string;
  denominacion: string;
  rubro: string;
  precioVenta: number;
  listaPrecioNombre: string;
}
