package com.tpgrupal.desarrollo_de_software.auth;

/**
 * Guarda el id del usuario que hizo el pedido HTTP que se está atendiendo.
 * Usa un ThreadLocal: cada pedido corre en su propio hilo, así que cada uno
 * ve solamente su propio usuario. Lo carga AuthInterceptor y lo limpia al terminar.
 */
public final class SesionActual {

    private static final ThreadLocal<Long> USUARIO_ID = new ThreadLocal<>();

    private SesionActual() {
    }

    public static void setUsuarioId(Long usuarioId) {
        USUARIO_ID.set(usuarioId);
    }

    // Devuelve null si el pedido no tiene usuario (por ejemplo, en los tests o al arrancar)
    public static Long getUsuarioId() {
        return USUARIO_ID.get();
    }

    public static void limpiar() {
        USUARIO_ID.remove();
    }
}
