package com.tpgrupal.desarrollo_de_software;

import com.tpgrupal.desarrollo_de_software.service.AfipWsaaClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DesarrolloDeSoftwareApplication {

	public static void main(String[] args) {
		SpringApplication.run(DesarrolloDeSoftwareApplication.class, args);
		System.out.println("\n" +
				" APLICACIÓN INICIADA CON ÉXITO\n" +
				"------------------------------------------------------------------------\n" +
				"Facturas en JSON (API):    http://localhost:8080/api/facturas\n" +
				"Reporte en PDF:             http://localhost:8080/api/facturas/export/pdf\n" +
				"Reporte en Excel:           http://localhost:8080/api/facturas/export/excel");
    }
    @Bean
    public CommandLineRunner probarAfip() {
        return args -> {
            String keyPath = "src/main/resources/afip/privada_test.key";
            String certPath = "src/main/resources/afip/certificado_test.crt";
            try {
                String[] credenciales = AfipWsaaClient.obtenerTokenYSign(keyPath, certPath);
                System.out.println("=== TOKEN Y SIGN OBTENIDOS EXITOSAMENTE ===");
                System.out.println("Token: " + credenciales[0]);
                System.out.println("Sign: " + credenciales[1]);
            } catch (Exception e) {
                System.err.println("Error al conectar con AFIP WSAA:");
                e.printStackTrace();
            }
        };

    }

}
