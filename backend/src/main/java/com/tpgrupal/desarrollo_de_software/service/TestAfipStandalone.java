package com.tpgrupal.desarrollo_de_software.service;

public class TestAfipStandalone {
    public static void main(String[] args) {
        // Rutas absolutas o relativas directas a tus archivos en resources
        //String keyPath = "backend/src/main/resources/afip/privada_test.key";
        //String certPath = "backend/src/main/resources/afip/certificado_test.crt";
        String keyPath = "C:/utn ciclo electivo 2026/DESARROLLO DE SOFTWARE/proyecto-facturacion(sinAFIP)/proyecto-facturacion/backend/src/main/resources/afip/privada_test.key";
        String certPath = "C:/utn ciclo electivo 2026/DESARROLLO DE SOFTWARE/proyecto-facturacion(sinAFIP)/proyecto-facturacion/backend/src/main/resources/afip/certificado_test.crt";

        // Si ejecutás desde la carpeta backend, ajustá las rutas:
        // String keyPath = "src/main/resources/afip/privada_test.key";
        // String certPath = "src/main/resources/afip/certificado_test.crt";

        try {
            System.out.println("Intentando conectar con AFIP WSAA (Homologación)...");
            String[] credenciales = AfipWsaaClient.obtenerTokenYSign(keyPath, certPath);

            System.out.println("\n=== ¡CONEXIÓN EXITOSA CON AFIP! ===");
            System.out.println("Token: " + credenciales[0]);
            System.out.println("Sign: " + credenciales[1]);
        } catch (Exception e) {
            System.err.println("\nError al conectar con AFIP:");
            e.printStackTrace();
        }
    }
}
