package com.tpgrupal.desarrollo_de_software.model;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class TestConexion implements CommandLineRunner {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void run(String... args) {
        Object result = em.createNativeQuery("SELECT 1").getSingleResult();
        if (result != null) {
            System.out.println("==========================================");
            System.out.println("¡Conexión exitosa a la Base de Datos desde Spring!");
            System.out.println("==========================================");
        }
    }
}