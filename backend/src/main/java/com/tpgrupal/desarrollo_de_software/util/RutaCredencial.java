package com.tpgrupal.desarrollo_de_software.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Encuentra los archivos de credenciales de ARCA (clave, certificado o keystore) sin depender
 * de la carpeta desde la que se arrancó el backend.
 *
 * En application.properties las rutas son relativas (ej. src/main/resources/afip/privada_homo.key),
 * así que solo funcionaban con "gradlew bootRun" desde la carpeta backend. Se prueba, en orden:
 *  1. la ruta tal cual (absoluta o relativa a la carpeta actual);
 *  2. la misma ruta dentro de "backend/" (si se arrancó desde la raíz del proyecto);
 *  3. el classpath (prefijo "classpath:" o lo que sigue a "src/main/resources/"): así funciona
 *     también desde el .jar o el IDE. Como la firma necesita un archivo, se copia a uno temporal.
 * Si no se encuentra, se devuelve la ruta original (el mensaje de error la muestra).
 */
public final class RutaCredencial {

    private static final String PREFIJO_CLASSPATH = "classpath:";
    private static final String CARPETA_RECURSOS = "src/main/resources/";

    // Un solo archivo temporal por recurso, aunque se resuelva varias veces
    private static final Map<String, String> COPIAS = new ConcurrentHashMap<>();

    private RutaCredencial() {
    }

    public static String resolver(String ruta) {
        if (ruta == null || ruta.isBlank()) return ruta;

        String normalizada = ruta.trim().replace('\\', '/');
        if (!normalizada.startsWith(PREFIJO_CLASSPATH)) {
            if (new File(normalizada).isFile()) return ruta;
            File enBackend = new File("backend", normalizada);
            if (!new File(normalizada).isAbsolute() && enBackend.isFile()) return enBackend.getAbsolutePath();
        }

        String recurso = recursoClasspath(normalizada);
        if (recurso == null) return ruta;
        String copia = COPIAS.computeIfAbsent(recurso, RutaCredencial::copiarDelClasspath);
        return copia != null ? copia : ruta;
    }

    private static String recursoClasspath(String ruta) {
        if (ruta.startsWith(PREFIJO_CLASSPATH)) return ruta.substring(PREFIJO_CLASSPATH.length()).replaceFirst("^/+", "");
        int i = ruta.indexOf(CARPETA_RECURSOS);
        return i >= 0 ? ruta.substring(i + CARPETA_RECURSOS.length()) : null;
    }

    private static String copiarDelClasspath(String recurso) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) cl = RutaCredencial.class.getClassLoader();
        try (InputStream in = cl.getResourceAsStream(recurso)) {
            if (in == null) return null;
            String nombre = Path.of(recurso).getFileName().toString();
            Path destino = Files.createTempFile("arca-", "-" + nombre);
            Files.copy(in, destino, StandardCopyOption.REPLACE_EXISTING);
            File archivo = destino.toFile();
            // Solo el usuario que corre el backend puede leerlo (puede ser una clave privada)
            archivo.setReadable(false, false);
            archivo.setReadable(true, true);
            archivo.deleteOnExit();
            return archivo.getAbsolutePath();
        } catch (IOException e) {
            return null;
        }
    }
}
