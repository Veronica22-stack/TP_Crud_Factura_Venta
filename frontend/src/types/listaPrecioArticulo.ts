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
  listaPrecioId?: number;
  marca?: string | null;
}

// Filtros del explorador de productos (GET /api/catalogos/productos)
export interface BusquedaProductosDTO {
  listaPrecioId: number;
  rubro?: string;
  codigo?: string;
  denominacion?: string;
  pagina: number;
  tamanio: number;
}
