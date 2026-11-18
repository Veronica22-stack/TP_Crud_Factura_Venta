// Servicio de autenticación del frontend.
// Guarda el token en el navegador (localStorage) y lo agrega a cada pedido al backend.

const API_AUTH_URL = 'http://localhost:8080/api/auth';
const CLAVE_TOKEN = 'auth_token';
const CLAVE_USUARIO = 'auth_usuario';

export interface UsuarioSesion {
  token: string;
  usuario: string;
  nombre: string;
  apellido: string;
}

export class AuthService {
  static obtenerToken(): string | null {
    return localStorage.getItem(CLAVE_TOKEN);
  }

  static obtenerUsuario(): UsuarioSesion | null {
    const datos = localStorage.getItem(CLAVE_USUARIO);
    if (!datos) return null;
    try {
      return JSON.parse(datos) as UsuarioSesion;
    } catch {
      return null;
    }
  }

  static estaLogueado(): boolean {
    return !!AuthService.obtenerToken();
  }

  /**
   * POST /api/auth/login con {usuario, clave}. Si es correcto guarda el token y los datos del usuario.
   */
  static async login(usuario: string, clave: string): Promise<UsuarioSesion> {
    const res = await fetch(`${API_AUTH_URL}/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ usuario, clave })
    });

    if (!res.ok) {
      const err = await res.json().catch(() => ({ message: 'No se pudo iniciar sesión.' }));
      const error = new Error(err.message || 'Usuario o contraseña incorrectos.') as Error & { usuarioInexistente?: boolean };
      error.usuarioInexistente = err.usuarioInexistente === true;
      throw error;
    }

    const sesion: UsuarioSesion = await res.json();
    localStorage.setItem(CLAVE_TOKEN, sesion.token);
    localStorage.setItem(CLAVE_USUARIO, JSON.stringify(sesion));
    return sesion;
  }

  /**
   * POST /api/auth/registro con {usuario, clave, nombre, apellido}.
   * Si sale bien, el usuario queda con la sesión iniciada.
   */
  static async registrar(usuario: string, clave: string, nombre: string, apellido: string): Promise<UsuarioSesion> {
    const res = await fetch(`${API_AUTH_URL}/registro`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ usuario, clave, nombre, apellido })
    });

    if (!res.ok) {
      const err = await res.json().catch(() => ({ message: 'No se pudo registrar el usuario.' }));
      throw new Error(err.message || 'No se pudo registrar el usuario.');
    }

    const sesion: UsuarioSesion = await res.json();
    localStorage.setItem(CLAVE_TOKEN, sesion.token);
    localStorage.setItem(CLAVE_USUARIO, JSON.stringify(sesion));
    return sesion;
  }

  /**
   * Avisa al backend que cierre la sesión, borra los datos guardados y vuelve al login.
   */
  static async logout(): Promise<void> {
    const token = AuthService.obtenerToken();
    if (token) {
      try {
        await fetch(`${API_AUTH_URL}/logout`, {
          method: 'POST',
          headers: { Authorization: `Bearer ${token}` }
        });
      } catch {
        // Si el backend no responde igual se cierra la sesión en el navegador
      }
    }
    AuthService.irAlLogin();
  }

  static irAlLogin(): void {
    localStorage.removeItem(CLAVE_TOKEN);
    localStorage.removeItem(CLAVE_USUARIO);
    window.location.href = './login.html';
  }
}

/**
 * Igual que fetch, pero agrega el encabezado "Authorization: Bearer <token>".
 * Si el backend responde 401 (sin sesión o sesión vencida), manda al login.
 */
export async function authFetch(url: string, init: RequestInit = {}): Promise<Response> {
  const headers = new Headers(init.headers);
  const token = AuthService.obtenerToken();
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  const res = await fetch(url, { ...init, headers });
  if (res.status === 401) {
    AuthService.irAlLogin();
    throw new Error('La sesión venció. Inicie sesión nuevamente.');
  }
  return res;
}

/**
 * Agrega el token como parámetro de la URL. Se usa en los links que se abren en otra
 * pestaña (PDF y Excel), porque un link no puede mandar el encabezado Authorization.
 */
export function urlConToken(url: string): string {
  const token = AuthService.obtenerToken();
  if (!token) return url;
  const separador = url.endsWith('?') ? '' : url.includes('?') ? '&' : '?';
  return `${url}${separador}token=${encodeURIComponent(token)}`;
}

// --- Protección de las pantallas ---
// Este módulo lo importan los services, así que corre en todas las pantallas.
// Si no estamos en el login y no hay sesión, se redirige al login.
// Si hay sesión, se muestra el nombre del usuario y el botón "Cerrar sesión" arriba a la derecha.

function mostrarUsuarioEnEncabezado(): void {
  const header = document.querySelector('.header');
  const usuario = AuthService.obtenerUsuario();
  if (!header || !usuario || header.querySelector('.header-acciones')) return;

  // Misma estructura que el encabezado de diseño: nombre, "Cerrar sesión" y el botón que ya estaba
  const acciones = document.createElement('div');
  acciones.className = 'header-acciones';

  const nombre = document.createElement('span');
  nombre.id = 'usuarioNombre';
  nombre.className = 'usuario-nombre';
  nombre.textContent = `${usuario.nombre} ${usuario.apellido}`;

  const btnSalir = document.createElement('button');
  btnSalir.type = 'button';
  btnSalir.id = 'btnCerrarSesion';
  btnSalir.className = 'btn btn-outline';
  btnSalir.textContent = 'Cerrar sesión';
  btnSalir.addEventListener('click', () => AuthService.logout());

  acciones.appendChild(nombre);
  acciones.appendChild(btnSalir);

  // El botón que ya estaba a la derecha del título (Nueva Factura / Volver al Listado) va al final
  const botonExistente = header.lastElementChild;
  if (botonExistente && botonExistente !== header.firstElementChild) {
    acciones.appendChild(botonExistente);
  }

  header.appendChild(acciones);
}

const esPaginaLogin = window.location.pathname.endsWith('login.html')
  || window.location.pathname.endsWith('registro.html');

if (!esPaginaLogin) {
  if (!AuthService.estaLogueado()) {
    window.location.replace('./login.html');
  } else if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', mostrarUsuarioEnEncabezado);
  } else {
    mostrarUsuarioEnEncabezado();
  }
}
