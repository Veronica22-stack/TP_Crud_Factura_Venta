import { ClienteDTO } from '../types/cliente';

const API_BASE_URL = 'http://localhost:8080/api/clientes';

export class ClienteService {
  static async obtenerClientes(buscar?: string): Promise<ClienteDTO[]> {
    const params = new URLSearchParams();
    if (buscar && buscar.trim() !== '') {
      params.append('buscar', buscar.trim());
    }

    const url = `${API_BASE_URL}?${params.toString()}`;
    const response = await fetch(url);
    if (!response.ok) {
      throw new Error(`Error al consultar clientes: ${response.statusText}`);
    }
    return await response.json();
  }
}
