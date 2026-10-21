package com.tpgrupal.desarrollo_de_software;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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

}
