import { ClienteDTO } from '../types/cliente';
import { PageResponse } from '../types/page';
import { authFetch } from './authService';

const API_URL = 'http://localhost:8080/api/catalogos/clientes/buscar';

export class ClienteService {
  /**
   * Clientes paginados en el backend (LIMIT / OFFSET): solo viaja la página pedida.
   * "texto" busca en la razón social (sin importar tildes) o en el CUIT (con o sin guiones).
   */
  static async buscarClientes(texto: string | undefined, pagina: number, tamanio: number): Promise<PageResponse<ClienteDTO>> {
    const params = new URLSearchParams({ pagina: String(pagina), tamanio: String(tamanio) });
    if (texto && texto.trim() !== '') {
      params.append('texto', texto.trim());
    }

    const response = await authFetch(`${API_URL}?${params.toString()}`);
    if (!response.ok) {
      throw new Error(`Error al consultar clientes: ${response.statusText}`);
    }
    return await response.json();
  }
}
