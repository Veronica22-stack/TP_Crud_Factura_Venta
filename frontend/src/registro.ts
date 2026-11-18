import { AuthService } from './services/authService';

const registroForm = document.getElementById('registroForm') as HTMLFormElement;
const inputNombre = document.getElementById('registroNombre') as HTMLInputElement;
const inputApellido = document.getElementById('registroApellido') as HTMLInputElement;
const inputUsuario = document.getElementById('registroUsuario') as HTMLInputElement;
const inputClave = document.getElementById('registroClave') as HTMLInputElement;
const inputClave2 = document.getElementById('registroClave2') as HTMLInputElement;
const btnRegistrar = document.getElementById('btnRegistrar') as HTMLButtonElement;
const mensajeError = document.getElementById('registroError') as HTMLElement;

// Si ya hay una sesión abierta, va directo al listado
if (AuthService.estaLogueado()) {
  window.location.replace('./index.html');
}

// Si viene desde el login (?usuario=...), el nombre de usuario ya aparece cargado
const usuarioParam = new URLSearchParams(window.location.search).get('usuario');
if (usuarioParam) inputUsuario.value = usuarioParam;
inputNombre.focus();

registroForm.addEventListener('submit', async (e) => {
  e.preventDefault();
  mensajeError.textContent = '';

  if (inputClave.value !== inputClave2.value) {
    mensajeError.textContent = 'Las contraseñas no coinciden.';
    inputClave2.focus();
    return;
  }

  btnRegistrar.disabled = true;
  btnRegistrar.textContent = 'Registrando...';

  try {
    await AuthService.registrar(
      inputUsuario.value.trim(),
      inputClave.value,
      inputNombre.value.trim(),
      inputApellido.value.trim()
    );
    window.location.href = './index.html';
  } catch (err: any) {
    mensajeError.textContent = err instanceof TypeError
      ? 'No se pudo conectar con el servidor. ¿Está corriendo el backend?'
      : err.message;
  } finally {
    btnRegistrar.disabled = false;
    btnRegistrar.textContent = 'Registrarme';
  }
});
