package com.tpgrupal.desarrollo_de_software.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class RutaCredencialTest {

    @TempDir
    Path tmp;

    @Test
    void archivoQueExisteSeDevuelveTalCual() throws Exception {
        Path archivo = Files.writeString(tmp.resolve("privada.key"), "clave");

        assertEquals(archivo.toString(), RutaCredencial.resolver(archivo.toString()));
    }

    @Test
    void rutaVaciaONulaSeDevuelveSinCambios() {
        assertNull(RutaCredencial.resolver(null));
        assertEquals("", RutaCredencial.resolver(""));
    }

    @Test
    void rutaDeSrcMainResourcesSeBuscaEnElClasspath() throws Exception {
        // Arrancando desde el .jar o desde otra carpeta, "src/main/resources/..." no existe en disco:
        // se usa el mismo archivo empaquetado en el classpath
        String resuelta = RutaCredencial.resolver("src/main/resources/credenciales-prueba/archivo.txt");

        File archivo = new File(resuelta);
        assertTrue(archivo.isFile(), "debería existir: " + resuelta);
        assertEquals("contenido de prueba", Files.readString(archivo.toPath()).trim());
    }

    @Test
    void rutaClasspathExplicitaTambienSeResuelve() throws Exception {
        String resuelta = RutaCredencial.resolver("classpath:credenciales-prueba/archivo.txt");

        assertEquals("contenido de prueba", Files.readString(Path.of(resuelta)).trim());
    }

    @Test
    void archivoInexistenteDevuelveLaRutaOriginal() {
        assertEquals("src/main/resources/no-existe/x.key", RutaCredencial.resolver("src/main/resources/no-existe/x.key"));
    }
}
