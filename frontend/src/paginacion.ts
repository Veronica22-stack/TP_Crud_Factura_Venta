import { PageResponse } from './types/page';

/**
 * Barra de paginación para los exploradores (productos y clientes).
 * La página ya viene armada del backend (LIMIT / OFFSET): acá solo se muestra
 * "Mostrando X a Y de Z" y los botones para pedir otra página.
 */
export function renderizarPaginacion<T>(
  contenedor: HTMLElement,
  pagina: PageResponse<T>,
  nombre: string,
  irAPagina: (numero: number) => void
): void {
  const total = pagina.totalElements;
  const desde = total === 0 ? 0 : pagina.pageNumber * pagina.pageSize + 1;
  const hasta = Math.min((pagina.pageNumber + 1) * pagina.pageSize, total);

  contenedor.innerHTML = `
    <div class="pagination-bar pagination-bar-modal">
      <div class="pagination-info">Mostrando ${desde} a ${hasta} de ${total} ${nombre}</div>
      <div class="pagination-controls">
        <button type="button" class="btn btn-page" data-ir="anterior" ${pagina.first ? 'disabled' : ''}>‹ Anterior</button>
        <span class="pagination-info">Página ${pagina.pageNumber + 1} de ${pagina.totalPages}</span>
        <button type="button" class="btn btn-page" data-ir="siguiente" ${pagina.last ? 'disabled' : ''}>Siguiente ›</button>
      </div>
    </div>
  `;

  contenedor.querySelector('[data-ir="anterior"]')?.addEventListener('click', () => irAPagina(pagina.pageNumber - 1));
  contenedor.querySelector('[data-ir="siguiente"]')?.addEventListener('click', () => irAPagina(pagina.pageNumber + 1));
}

export function limpiarPaginacion(contenedor: HTMLElement): void {
  contenedor.innerHTML = '';
}
