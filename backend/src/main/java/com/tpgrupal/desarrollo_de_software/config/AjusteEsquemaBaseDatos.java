package com.tpgrupal.desarrollo_de_software.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Ajustes de la base que spring.jpa.hibernate.ddl-auto=update no hace solo.
 *
 * Hibernate agrega columnas nuevas, pero no le quita el NOT NULL a una columna existente.
 * Las facturas a Consumidor Final se guardan sin cliente, así que factura_venta.cliente_id
 * tiene que aceptar NULL. Si la tabla se creó cuando era obligatoria, se corrige al arrancar.
 */
@Component
public class AjusteEsquemaBaseDatos implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AjusteEsquemaBaseDatos.class);

    private final JdbcTemplate jdbcTemplate;

    public AjusteEsquemaBaseDatos(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            List<String> nullable = jdbcTemplate.queryForList(
                    "SELECT IS_NULLABLE FROM information_schema.COLUMNS "
                            + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'factura_venta' "
                            + "AND COLUMN_NAME = 'cliente_id'",
                    String.class);
            if (!nullable.isEmpty() && "NO".equalsIgnoreCase(nullable.get(0))) {
                jdbcTemplate.execute("ALTER TABLE factura_venta MODIFY cliente_id BIGINT NULL");
                log.info("Base de datos: factura_venta.cliente_id ahora acepta NULL (facturas a Consumidor Final).");
            }
        } catch (Exception e) {
            log.warn("No se pudo revisar la columna factura_venta.cliente_id: {}", e.getMessage());
        }
    }
}
