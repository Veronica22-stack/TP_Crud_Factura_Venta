import { AuthService } from './services/authService';

const loginForm = document.getElementById('loginForm') as HTMLFormElement;
const inputUsuario = document.getElementById('loginUsuario') as HTMLInputElement;
const inputClave = document.getElementById('loginClave') as HTMLInputElement;
const btnIngresar = document.getElementById('btnIngresar') as HTMLButtonElement;
const mensajeError = document.getElementById('loginError') as HTMLElement;
const avisoRegistrar = document.getElementById('loginRegistrar') as HTMLElement;
const linkRegistrar = document.getElementById('linkRegistrarUsuario') as HTMLAnchorElement;

// Si ya hay una sesión abierta, va directo al listado
if (AuthService.estaLogueado()) {
  window.location.replace('./index.html');
}

loginForm.addEventListener('submit', async (e) => {
  e.preventDefault();
  mensajeError.textContent = '';
  avisoRegistrar.style.display = 'none';
  btnIngresar.disabled = true;
  btnIngresar.textContent = 'Ingresando...';

  try {
    await AuthService.login(inputUsuario.value.trim(), inputClave.value);
    window.location.href = './index.html';
  } catch (err: any) {
    mensajeError.textContent = err instanceof TypeError
      ? 'No se pudo conectar con el servidor. ¿Está corriendo el backend?'
      : err.message;
    // Si el usuario no existe, se ofrece ir a registrarlo (con el nombre ya cargado)
    if (err.usuarioInexistente) {
      linkRegistrar.href = `./registro.html?usuario=${encodeURIComponent(inputUsuario.value.trim())}`;
      avisoRegistrar.style.display = 'block';
    }
    inputClave.value = '';
    inputClave.focus();
  } finally {
    btnIngresar.disabled = false;
    btnIngresar.textContent = 'Ingresar';
  }
});
