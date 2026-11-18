-- Datos de prueba para facturacion_db (MySQL)


CREATE DATABASE IF NOT EXISTS facturacion_db CHARACTER SET utf8mb4;
USE facturacion_db;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `factura_venta_detalle`;
DROP TABLE IF EXISTS `factura_venta`;
DROP TABLE IF EXISTS `lista_precio_articulo`;
DROP TABLE IF EXISTS `lista_precio`;
DROP TABLE IF EXISTS `articulo`;
DROP TABLE IF EXISTS `marca`;
DROP TABLE IF EXISTS `rubro`;
DROP TABLE IF EXISTS `cliente`;
DROP TABLE IF EXISTS `contacto`;
DROP TABLE IF EXISTS `domicilio`;
DROP TABLE IF EXISTS `punto_venta`;
DROP TABLE IF EXISTS `condicion_iva`;
DROP TABLE IF EXISTS `tipo_moneda`;
DROP TABLE IF EXISTS `usuario`;

-- ---------------------------------------------------------------------
-- 1) ESTRUCTURA (generada por Hibernate desde las entidades)
-- ---------------------------------------------------------------------
CREATE TABLE `articulo` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `fecha_alta` datetime(6) NOT NULL,
  `fecha_baja` datetime(6) DEFAULT NULL,
  `fecha_modificacion` datetime(6) DEFAULT NULL,
  `codigo` varchar(255) NOT NULL,
  `denominacion` varchar(255) NOT NULL,
  `usuario_baja_id` bigint(20) DEFAULT NULL,
  `usuario_carga_id` bigint(20) NOT NULL,
  `usuario_modificacion_id` bigint(20) NOT NULL,
  `marca_id` bigint(20) DEFAULT NULL,
  `rubro_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKk3ae47m9o25c06d2t1tskodln` (`usuario_baja_id`),
  KEY `FK9ynd3ue1ua059ntcynbyj74st` (`usuario_carga_id`),
  KEY `FK6wsqqykc6x0o5emlk7f9kdvnr` (`usuario_modificacion_id`),
  KEY `FKkc7g1efsj3815e1bgrsyl1p6w` (`marca_id`),
  KEY `FKs90nu8meysg24ocgu0t9aod0i` (`rubro_id`),
  CONSTRAINT `FK6wsqqykc6x0o5emlk7f9kdvnr` FOREIGN KEY (`usuario_modificacion_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FK9ynd3ue1ua059ntcynbyj74st` FOREIGN KEY (`usuario_carga_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKk3ae47m9o25c06d2t1tskodln` FOREIGN KEY (`usuario_baja_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKkc7g1efsj3815e1bgrsyl1p6w` FOREIGN KEY (`marca_id`) REFERENCES `marca` (`id`),
  CONSTRAINT `FKs90nu8meysg24ocgu0t9aod0i` FOREIGN KEY (`rubro_id`) REFERENCES `rubro` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `cliente` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `fecha_alta` datetime(6) NOT NULL,
  `fecha_baja` datetime(6) DEFAULT NULL,
  `fecha_modificacion` datetime(6) DEFAULT NULL,
  `cut_cuil` varchar(255) NOT NULL,
  `denominacion` varchar(255) NOT NULL,
  `usuario_baja_id` bigint(20) DEFAULT NULL,
  `usuario_carga_id` bigint(20) NOT NULL,
  `usuario_modificacion_id` bigint(20) NOT NULL,
  `contacto_id` bigint(20) NOT NULL,
  `domicilio_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKqlkmu8wduq25uw6qfpudrmtlh` (`contacto_id`),
  UNIQUE KEY `UK7ob9xg3niaibhhmj5iw311ygb` (`domicilio_id`),
  KEY `FKdolb0jhtluenarupi640vvugc` (`usuario_baja_id`),
  KEY `FK1gx9bkhhm6fbitbylk5gxr6rp` (`usuario_carga_id`),
  KEY `FKhroyidlur9eyyw5epjyyu02ta` (`usuario_modificacion_id`),
  CONSTRAINT `FK1gx9bkhhm6fbitbylk5gxr6rp` FOREIGN KEY (`usuario_carga_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKdolb0jhtluenarupi640vvugc` FOREIGN KEY (`usuario_baja_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKfhaklodto1bt1o9ntt390cw0h` FOREIGN KEY (`contacto_id`) REFERENCES `contacto` (`id`),
  CONSTRAINT `FKhnfwgi8xk38mge49a8twpxtxv` FOREIGN KEY (`domicilio_id`) REFERENCES `domicilio` (`id`),
  CONSTRAINT `FKhroyidlur9eyyw5epjyyu02ta` FOREIGN KEY (`usuario_modificacion_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `condicion_iva` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `fecha_alta` datetime(6) NOT NULL,
  `fecha_baja` datetime(6) DEFAULT NULL,
  `fecha_modificacion` datetime(6) DEFAULT NULL,
  `codigo_afip` int(11) NOT NULL,
  `denominacion` varchar(255) NOT NULL,
  `usuario_baja_id` bigint(20) DEFAULT NULL,
  `usuario_carga_id` bigint(20) NOT NULL,
  `usuario_modificacion_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7k7oo5onf6p2hcy212hnfpsk6` (`usuario_baja_id`),
  KEY `FKo2ue4fdfiupukxrwnmiwbykle` (`usuario_carga_id`),
  KEY `FKgqwpotjhxb32jrq662p8xedsh` (`usuario_modificacion_id`),
  CONSTRAINT `FK7k7oo5onf6p2hcy212hnfpsk6` FOREIGN KEY (`usuario_baja_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKgqwpotjhxb32jrq662p8xedsh` FOREIGN KEY (`usuario_modificacion_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKo2ue4fdfiupukxrwnmiwbykle` FOREIGN KEY (`usuario_carga_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `contacto` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `celular` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `telefono` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `domicilio` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `nombre_calle` varchar(255) DEFAULT NULL,
  `numero_calle` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `factura_venta` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `fecha_alta` datetime(6) NOT NULL,
  `fecha_baja` datetime(6) DEFAULT NULL,
  `fecha_modificacion` datetime(6) DEFAULT NULL,
  `cae` varchar(255) DEFAULT NULL,
  `fecha_vto` datetime(6) DEFAULT NULL,
  `estado` varchar(255) NOT NULL,
  `fecha_anulacion` datetime(6) DEFAULT NULL,
  `fecha_emision` datetime(6) NOT NULL,
  `importe_cobrado` double DEFAULT NULL,
  `importe_saldo` double DEFAULT NULL,
  `importe_total` double NOT NULL,
  `motivo_rechazo` varchar(255) DEFAULT NULL,
  `numero` bigint(20) DEFAULT NULL,
  `observaciones` varchar(255) DEFAULT NULL,
  `resultado_afip` varchar(255) DEFAULT NULL,
  `usuario_baja_id` bigint(20) DEFAULT NULL,
  `usuario_carga_id` bigint(20) NOT NULL,
  `usuario_modificacion_id` bigint(20) NOT NULL,
  `cliente_id` bigint(20) DEFAULT NULL,
  `condicion_iva` bigint(20) NOT NULL,
  `punto_venta_id` bigint(20) NOT NULL,
  `tipo_moneda` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_factura_venta_numero` (`numero`),
  KEY `FKfwpbaat1xn33lappiwa2p5i84` (`usuario_baja_id`),
  KEY `FKpw8efss8127xyrrgckoindo9c` (`usuario_carga_id`),
  KEY `FKcrhgedaviq1utq3ipnt6k1rc1` (`usuario_modificacion_id`),
  KEY `FKhwuwudrv0r4sflktrji97ay8m` (`cliente_id`),
  KEY `FKcuv06th28xyc0ac8q3595gv7q` (`condicion_iva`),
  KEY `FKb6wyq1b7gxxh5p9qbahgww9q5` (`punto_venta_id`),
  KEY `FKj94ivi7m0rvfkf0tq1mqqj6t7` (`tipo_moneda`),
  CONSTRAINT `FKb6wyq1b7gxxh5p9qbahgww9q5` FOREIGN KEY (`punto_venta_id`) REFERENCES `punto_venta` (`id`),
  CONSTRAINT `FKcrhgedaviq1utq3ipnt6k1rc1` FOREIGN KEY (`usuario_modificacion_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKcuv06th28xyc0ac8q3595gv7q` FOREIGN KEY (`condicion_iva`) REFERENCES `condicion_iva` (`id`),
  CONSTRAINT `FKfwpbaat1xn33lappiwa2p5i84` FOREIGN KEY (`usuario_baja_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKhwuwudrv0r4sflktrji97ay8m` FOREIGN KEY (`cliente_id`) REFERENCES `cliente` (`id`),
  CONSTRAINT `FKj94ivi7m0rvfkf0tq1mqqj6t7` FOREIGN KEY (`tipo_moneda`) REFERENCES `tipo_moneda` (`id`),
  CONSTRAINT `FKpw8efss8127xyrrgckoindo9c` FOREIGN KEY (`usuario_carga_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `factura_venta_detalle` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `cantidad` double NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `importe_iva` double DEFAULT NULL,
  `importe_neto` double DEFAULT NULL,
  `subtotal` double NOT NULL,
  `porcentaje_ue_bonificacion` double DEFAULT NULL,
  `precio_unitario` double NOT NULL,
  `factura_venta_id` bigint(20) NOT NULL,
  `lista_precio_articulo_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK980067exbf0ww2xm57et0115` (`factura_venta_id`),
  KEY `FK6eg1d0og3ed334g0hbfjvu9nr` (`lista_precio_articulo_id`),
  CONSTRAINT `FK6eg1d0og3ed334g0hbfjvu9nr` FOREIGN KEY (`lista_precio_articulo_id`) REFERENCES `lista_precio_articulo` (`id`),
  CONSTRAINT `FK980067exbf0ww2xm57et0115` FOREIGN KEY (`factura_venta_id`) REFERENCES `factura_venta` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `lista_precio` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `fecha_alta` datetime(6) NOT NULL,
  `fecha_baja` datetime(6) DEFAULT NULL,
  `fecha_modificacion` datetime(6) DEFAULT NULL,
  `codigo` varchar(255) NOT NULL,
  `denominacion` varchar(255) NOT NULL,
  `usuario_baja_id` bigint(20) DEFAULT NULL,
  `usuario_carga_id` bigint(20) NOT NULL,
  `usuario_modificacion_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK6lrrl28mrcwvowctpth0fmn6q` (`usuario_baja_id`),
  KEY `FKbl4w2f194qlf0p1krt8nxpya0` (`usuario_carga_id`),
  KEY `FKrd09q2iv6nor5k41on0yw112o` (`usuario_modificacion_id`),
  CONSTRAINT `FK6lrrl28mrcwvowctpth0fmn6q` FOREIGN KEY (`usuario_baja_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKbl4w2f194qlf0p1krt8nxpya0` FOREIGN KEY (`usuario_carga_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKrd09q2iv6nor5k41on0yw112o` FOREIGN KEY (`usuario_modificacion_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `lista_precio_articulo` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `fecha_alta` datetime(6) NOT NULL,
  `fecha_baja` datetime(6) DEFAULT NULL,
  `fecha_modificacion` datetime(6) DEFAULT NULL,
  `precio_venta` double NOT NULL,
  `usuario_baja_id` bigint(20) DEFAULT NULL,
  `usuario_carga_id` bigint(20) NOT NULL,
  `usuario_modificacion_id` bigint(20) NOT NULL,
  `articulo_id` bigint(20) NOT NULL,
  `lista_precio_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK5dof4lc7m227bhs6mujk02epl` (`usuario_baja_id`),
  KEY `FK4ce3b8tok8qmw6a36dq6vv08l` (`usuario_carga_id`),
  KEY `FK1yge84lf41hbw2txhtldoxdf3` (`usuario_modificacion_id`),
  KEY `FKnijaepxpdp4tsqsiofhnhbxor` (`articulo_id`),
  KEY `FKnyiurj2peq4m9cpsclr3mcs60` (`lista_precio_id`),
  CONSTRAINT `FK1yge84lf41hbw2txhtldoxdf3` FOREIGN KEY (`usuario_modificacion_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FK4ce3b8tok8qmw6a36dq6vv08l` FOREIGN KEY (`usuario_carga_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FK5dof4lc7m227bhs6mujk02epl` FOREIGN KEY (`usuario_baja_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKnijaepxpdp4tsqsiofhnhbxor` FOREIGN KEY (`articulo_id`) REFERENCES `articulo` (`id`),
  CONSTRAINT `FKnyiurj2peq4m9cpsclr3mcs60` FOREIGN KEY (`lista_precio_id`) REFERENCES `lista_precio` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `marca` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `fecha_alta` datetime(6) NOT NULL,
  `fecha_baja` datetime(6) DEFAULT NULL,
  `fecha_modificacion` datetime(6) DEFAULT NULL,
  `codigo` int(11) NOT NULL,
  `denominacion` varchar(255) NOT NULL,
  `usuario_baja_id` bigint(20) DEFAULT NULL,
  `usuario_carga_id` bigint(20) NOT NULL,
  `usuario_modificacion_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKmy2l2s9ydapwyb1fd5b6xif8o` (`usuario_baja_id`),
  KEY `FKk0e9khknw7cpv7mypf0i2h2bd` (`usuario_carga_id`),
  KEY `FK6soabbak4q5ny1si094y41667` (`usuario_modificacion_id`),
  CONSTRAINT `FK6soabbak4q5ny1si094y41667` FOREIGN KEY (`usuario_modificacion_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKk0e9khknw7cpv7mypf0i2h2bd` FOREIGN KEY (`usuario_carga_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKmy2l2s9ydapwyb1fd5b6xif8o` FOREIGN KEY (`usuario_baja_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `punto_venta` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `fecha_alta` datetime(6) NOT NULL,
  `fecha_baja` datetime(6) DEFAULT NULL,
  `fecha_modificacion` datetime(6) DEFAULT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `domicilio_comercial` varchar(255) DEFAULT NULL,
  `numero` int(11) NOT NULL,
  `tipo_emision` varchar(255) DEFAULT NULL,
  `usuario_baja_id` bigint(20) DEFAULT NULL,
  `usuario_carga_id` bigint(20) NOT NULL,
  `usuario_modificacion_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKld9yelsrq2xx0pp30f34tspbl` (`usuario_baja_id`),
  KEY `FKoyboalyvmy632sb0ysugx3bcn` (`usuario_carga_id`),
  KEY `FKalsyftbbbif42rdd9yf0m9d99` (`usuario_modificacion_id`),
  CONSTRAINT `FKalsyftbbbif42rdd9yf0m9d99` FOREIGN KEY (`usuario_modificacion_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKld9yelsrq2xx0pp30f34tspbl` FOREIGN KEY (`usuario_baja_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKoyboalyvmy632sb0ysugx3bcn` FOREIGN KEY (`usuario_carga_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `rubro` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `fecha_alta` datetime(6) NOT NULL,
  `fecha_baja` datetime(6) DEFAULT NULL,
  `fecha_modificacion` datetime(6) DEFAULT NULL,
  `codigo` int(11) NOT NULL,
  `denominacion` varchar(255) NOT NULL,
  `usuario_baja_id` bigint(20) DEFAULT NULL,
  `usuario_carga_id` bigint(20) NOT NULL,
  `usuario_modificacion_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKom23psn3i0ddd4xrvjquv0trl` (`usuario_baja_id`),
  KEY `FKtqonsj3vx4yxxcp11qelopf7t` (`usuario_carga_id`),
  KEY `FKpauqw1a06mrj6ucyhif5c462a` (`usuario_modificacion_id`),
  CONSTRAINT `FKom23psn3i0ddd4xrvjquv0trl` FOREIGN KEY (`usuario_baja_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKpauqw1a06mrj6ucyhif5c462a` FOREIGN KEY (`usuario_modificacion_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKtqonsj3vx4yxxcp11qelopf7t` FOREIGN KEY (`usuario_carga_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `tipo_moneda` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `fecha_alta` datetime(6) NOT NULL,
  `fecha_baja` datetime(6) DEFAULT NULL,
  `fecha_modificacion` datetime(6) DEFAULT NULL,
  `codigo_afip` varchar(255) NOT NULL,
  `denominacion` varchar(255) NOT NULL,
  `simbolo` varchar(255) NOT NULL,
  `usuario_baja_id` bigint(20) DEFAULT NULL,
  `usuario_carga_id` bigint(20) NOT NULL,
  `usuario_modificacion_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK3g5lcl84d3vj0ick8d98kdhjn` (`usuario_baja_id`),
  KEY `FKnkbd6xmfw9kvsu5prdcapoi2k` (`usuario_carga_id`),
  KEY `FKde9d2lmy9cxyw4x65dxika9h` (`usuario_modificacion_id`),
  CONSTRAINT `FK3g5lcl84d3vj0ick8d98kdhjn` FOREIGN KEY (`usuario_baja_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKde9d2lmy9cxyw4x65dxika9h` FOREIGN KEY (`usuario_modificacion_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKnkbd6xmfw9kvsu5prdcapoi2k` FOREIGN KEY (`usuario_carga_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `usuario` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `apellido` varchar(255) NOT NULL,
  `clave` varchar(255) NOT NULL,
  `nombre` varchar(255) NOT NULL,
  `usuario` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------------
-- 2) DATOS MAESTROS
-- ---------------------------------------------------------------------
-- Usuarios del sistema (auditoria: usuario_carga / usuario_modificacion)
INSERT INTO usuario (id, usuario, clave, nombre, apellido) VALUES
 (1, 'gsanchez', 'pass1234', 'Gabriel', 'Sánchez'),
 (2, 'mmunoz',   'pass1234', 'Mayra',   'Muñoz');

-- Tipos de moneda (codigo AFIP)
INSERT INTO tipo_moneda (id, fecha_alta, fecha_baja, fecha_modificacion, usuario_baja_id, usuario_carga_id, usuario_modificacion_id, codigo_afip, denominacion, simbolo) VALUES
 (1, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, 'PES', 'Peso Argentino', '$'),
 (2, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, 'DOL', 'Dólar Estadounidense', 'US$');

-- Condiciones frente al IVA (codigo AFIP)
INSERT INTO condicion_iva (id, fecha_alta, fecha_baja, fecha_modificacion, usuario_baja_id, usuario_carga_id, usuario_modificacion_id, codigo_afip, denominacion) VALUES
 (1, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, 1, 'IVA Responsable Inscripto'),
 (2, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, 6, 'Monotributo'),
 (3, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, 5, 'Consumidor Final'),
 (4, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, 4, 'IVA Sujeto Exento');

-- Puntos de venta
INSERT INTO punto_venta (id, fecha_alta, fecha_baja, fecha_modificacion, usuario_baja_id, usuario_carga_id, usuario_modificacion_id, numero, descripcion, tipo_emision, domicilio_comercial) VALUES
 (1, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, 1, 'Punto de Venta Central',        'Electronica', 'Av. San Martín 1050'),
 (2, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, 2, 'Punto de Venta Sucursal Norte', 'Electronica', 'Av. Acceso Este 2300'),
 (3, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, 3, 'Punto de Venta Sucursal Sur',   'Electronica', 'Calle Mitre 455'),
 (4, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, 6, 'Punto de Venta 6',              'Electronica', 'Av. Las Heras 780');

-- Clientes: cada uno con su contacto y domicilio (relaciones 1 a 1)
INSERT INTO contacto (id, email, telefono, celular) VALUES
 (1, 'cf@ventas.local', '2610000000', NULL),
 (2, 'mayra@bakery.com', '2614001122', '2615001122'),
 (3, 'vero@correo.com', '2614334455', '2615334455'),
 (4, 'eli@correo.com', '2618561472', '2618561472'),
 (5, 'juan.perez@correo.com', '2614123456', NULL),
 (6, 'maria.gonzalez@correo.com', '2614654321', NULL),
 (7, 'tomas.donoso@correo.com', '2614778899', '2615778899'),
 (8, 'gabriel.sanchez@correo.com', '2614990011', '2615990011');
INSERT INTO domicilio (id, nombre_calle, numero_calle) VALUES
 (1, 'Venta Mostrador', 'S/N'),
 (2, 'Av. San Martín', '1500'),
 (3, 'Belgrano', '820'),
 (4, 'Recoleta', '111'),
 (5, 'San Martín', '123'),
 (6, 'Las Heras', '456'),
 (7, 'Colón', '990'),
 (8, 'Godoy Cruz', '2040');
INSERT INTO cliente (id, fecha_alta, fecha_baja, fecha_modificacion, usuario_baja_id, usuario_carga_id, usuario_modificacion_id, cut_cuil, denominacion, contacto_id, domicilio_id) VALUES
 (1, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, '00-00000000-0', 'Consumidor Final', 1, 1),
 (2, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, '27-40876925-9', 'Mayra Muñoz', 2, 2),
 (3, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, '27-47169997-1', 'Verónica Silva', 3, 3),
 (4, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, '20-42587631-1', 'Elian Navarro', 4, 4),
 (5, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, '20-28111222-3', 'Juan Pérez', 5, 5),
 (6, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, '27-33444555-8', 'María González', 6, 6),
 (7, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, '20-41222333-5', 'Tomás Donoso', 7, 7),
 (8, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, '20-39888777-2', 'Gabriel Sánchez', 8, 8);

-- Rubros y marcas
INSERT INTO rubro (id, fecha_alta, fecha_baja, fecha_modificacion, usuario_baja_id, usuario_carga_id, usuario_modificacion_id, codigo, denominacion) VALUES
 (1, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, 5501, 'Electrónica'),
 (2, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, 5502, 'Muebles'),
 (3, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, 5503, 'Alimentos'),
 (4, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, 5504, 'Bebidas');
INSERT INTO marca (id, fecha_alta, fecha_baja, fecha_modificacion, usuario_baja_id, usuario_carga_id, usuario_modificacion_id, codigo, denominacion) VALUES
 (1, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, 101, 'Samsung'),
 (2, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, 102, 'Logitech'),
 (3, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, 103, 'HyperX'),
 (4, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, 104, 'Lucchetti'),
 (5, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, 105, 'Gallo'),
 (6, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, 106, 'Coca-Cola');

-- Articulos (1-8 se venden por unidad, 9-11 son bultos de la lista mayorista)
INSERT INTO articulo (id, fecha_alta, fecha_baja, fecha_modificacion, usuario_baja_id, usuario_carga_id, usuario_modificacion_id, codigo, denominacion, rubro_id, marca_id) VALUES
 (1, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, 'ART-100', 'Monitor 24" IPS', 1, 1),
 (2, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, 'ART-101', 'Teclado mecánico', 1, 2),
 (3, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, 'ART-102', 'Silla ergonómica', 2, NULL),
 (4, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, 'ART-103', 'Auriculares HyperX', 1, 3),
 (5, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, 'ART-104', 'Mouse inalámbrico', 1, 2),
 (6, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, 'ART-201', 'FIDEOS', 3, 4),
 (7, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, 'ART-202', 'ARROZ', 3, 5),
 (8, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, 'ART-301', 'GASEOSA 2.25L', 4, 6),
 (9, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, 'ART-211', 'FIDEOS BULTO x10', 3, 4),
 (10, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, 'ART-212', 'ARROZ BULTO x10', 3, 5),
 (11, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, 'ART-311', 'GASEOSA 2.25L PACK x6', 4, 6);

-- Listas de precio
INSERT INTO lista_precio (id, fecha_alta, fecha_baja, fecha_modificacion, usuario_baja_id, usuario_carga_id, usuario_modificacion_id, codigo, denominacion) VALUES
 (1, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, 'LP-01', 'Lista Contado'),
 (2, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, 'LP-02', 'Lista Mayorista');

-- Precios por lista: ids 1-8 = Lista Contado (unidad), ids 9-11 = Lista Mayorista (bultos)
INSERT INTO lista_precio_articulo (id, fecha_alta, fecha_baja, fecha_modificacion, usuario_baja_id, usuario_carga_id, usuario_modificacion_id, lista_precio_id, articulo_id, precio_venta) VALUES
 (1, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, 1, 1, 120000.00),
 (2, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, 1, 2, 45000.00),
 (3, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, 1, 3, 85000.00),
 (4, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, 1, 4, 15500.00),
 (5, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, 1, 5, 12000.00),
 (6, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, 1, 6, 500.00),
 (7, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, 1, 7, 1500.00),
 (8, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, 1, 8, 2500.00),
 (9, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, 2, 9, 4250.00),
 (10, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, 2, 10, 12750.00),
 (11, '2026-01-02 09:00:00', NULL, '2026-01-02 09:00:00', NULL, 1, 1, 2, 11, 12750.00);

-- Facturas de venta
INSERT INTO factura_venta (id, fecha_alta, fecha_baja, fecha_modificacion, usuario_baja_id, usuario_carga_id, usuario_modificacion_id, numero, fecha_emision, punto_venta_id, cliente_id, tipo_moneda, condicion_iva, importe_total, importe_cobrado, importe_saldo, cae, fecha_vto, resultado_afip, motivo_rechazo, estado, fecha_anulacion, observaciones) VALUES
 (1, '2026-01-15 10:00:00', NULL, '2026-01-15 10:00:00', NULL, 1, 1, 1001, '2026-01-15 00:00:00', 1, 1, 1, 3, 2500.00, 2500.00, 0.00, '74100000001001', '2026-01-28 00:00:00', 'A', NULL, 'EMITIDA', NULL, NULL),
 (2, '2026-02-03 10:00:00', NULL, '2026-02-03 10:00:00', NULL, 1, 1, 1002, '2026-02-03 00:00:00', 1, 5, 1, 1, 132000.00, 132000.00, 0.00, '74100000001002', '2026-02-28 00:00:00', 'A', NULL, 'EMITIDA', NULL, NULL),
 (3, '2026-02-20 10:00:00', NULL, '2026-02-20 10:00:00', NULL, 1, 1, 1003, '2026-02-20 00:00:00', 2, 6, 1, 2, 100500.00, 100500.00, 0.00, '74100000001003', '2026-02-28 00:00:00', 'A', NULL, 'EMITIDA', NULL, NULL),
 (4, '2026-03-10 10:00:00', NULL, '2026-03-10 10:00:00', NULL, 1, 1, 1004, '2026-03-10 00:00:00', 1, 2, 1, 1, 81000.00, 40500.00, 40500.00, '74100000001004', '2026-03-28 00:00:00', 'A', NULL, 'EMITIDA', NULL, 'Bonificación 10% por cliente frecuente'),
 (5, '2026-03-16 10:00:00', NULL, '2026-03-16 10:00:00', NULL, 1, 1, 1005, '2026-03-16 00:00:00', 2, 3, 1, 2, 29750.00, 29750.00, 0.00, '74100000001005', '2026-03-28 00:00:00', 'A', NULL, 'EMITIDA', NULL, 'Venta mayorista'),
 (6, '2026-04-02 10:00:00', NULL, '2026-04-02 10:00:00', NULL, 1, 1, 1006, '2026-04-02 00:00:00', 3, 4, 1, 1, 240000.00, 0.00, 0.00, '74100000001006', '2026-04-28 00:00:00', 'A', NULL, 'ANULADA', '2026-04-03 10:15:00', 'Anulada por error en los datos del cliente'),
 (7, '2026-05-12 10:00:00', NULL, '2026-05-12 10:00:00', NULL, 1, 1, 1007, '2026-05-12 00:00:00', 4, 1, 1, 3, 15500.00, 15500.00, 0.00, '74100000001007', '2026-05-28 00:00:00', 'A', NULL, 'EMITIDA', NULL, NULL),
 (8, '2026-06-08 10:00:00', NULL, '2026-06-08 10:00:00', NULL, 1, 1, 1008, '2026-06-08 00:00:00', 3, 5, 1, 1, 495000.00, 495000.00, 0.00, '74100000001008', '2026-06-28 00:00:00', 'A', NULL, 'EMITIDA', NULL, 'Venta mayorista'),
 (9, '2026-07-21 10:00:00', NULL, '2026-07-21 10:00:00', NULL, 1, 1, 1009, '2026-07-21 00:00:00', 1, 7, 1, 4, 170000.00, 170000.00, 0.00, '74100000001009', '2026-07-28 00:00:00', 'A', NULL, 'EMITIDA', NULL, NULL),
 (10, '2026-08-30 10:00:00', NULL, '2026-08-30 10:00:00', NULL, 1, 1, 1010, '2026-08-30 00:00:00', 2, 6, 1, 2, 12000.00, 0.00, 0.00, '74100000001010', '2026-08-28 00:00:00', 'A', NULL, 'ANULADA', '2026-08-31 18:40:00', 'Anulada a pedido del cliente'),
 (11, '2026-09-15 10:00:00', NULL, '2026-09-15 10:00:00', NULL, 1, 1, 1011, '2026-09-15 00:00:00', 1, 8, 1, 1, 72500.00, 72500.00, 0.00, '74100000001011', '2026-09-28 00:00:00', 'A', NULL, 'EMITIDA', NULL, NULL),
 (12, '2026-10-05 10:00:00', NULL, '2026-10-05 10:00:00', NULL, 1, 1, 1012, '2026-10-05 00:00:00', 4, 1, 1, 3, 17250.00, 17250.00, 0.00, '74100000001012', '2026-10-28 00:00:00', 'A', NULL, 'EMITIDA', NULL, NULL);

-- Renglones (detalle) de cada factura
INSERT INTO factura_venta_detalle (id, factura_venta_id, lista_precio_articulo_id, descripcion, cantidad, precio_unitario, porcentaje_ue_bonificacion, importe_neto, importe_iva, subtotal) VALUES
 (1, 1, 6, 'FIDEOS', 2.00, 500.00, 0.00, 826.45, 173.55, 1000.00),
 (2, 1, 7, 'ARROZ', 1.00, 1500.00, 0.00, 1239.67, 260.33, 1500.00),
 (3, 2, 1, 'Monitor 24" IPS', 1.00, 120000.00, 0.00, 99173.55, 20826.45, 120000.00),
 (4, 2, 5, 'Mouse inalámbrico', 1.00, 12000.00, 0.00, 9917.36, 2082.64, 12000.00),
 (5, 3, 3, 'Silla ergonómica', 1.00, 85000.00, 0.00, 70247.93, 14752.07, 85000.00),
 (6, 3, 4, 'Auriculares HyperX', 1.00, 15500.00, 0.00, 12809.92, 2690.08, 15500.00),
 (7, 4, 2, 'Teclado mecánico', 2.00, 45000.00, 10.00, 66942.15, 14057.85, 81000.00),
 (8, 5, 9, 'FIDEOS BULTO x10', 1.00, 4250.00, 0.00, 3512.40, 737.60, 4250.00),
 (9, 5, 10, 'ARROZ BULTO x10', 1.00, 12750.00, 0.00, 10537.19, 2212.81, 12750.00),
 (10, 5, 11, 'GASEOSA 2.25L PACK x6', 1.00, 12750.00, 0.00, 10537.19, 2212.81, 12750.00),
 (11, 6, 1, 'Monitor 24" IPS', 2.00, 120000.00, 0.00, 198347.11, 41652.89, 240000.00),
 (12, 7, 4, 'Auriculares HyperX', 1.00, 15500.00, 0.00, 12809.92, 2690.08, 15500.00),
 (13, 8, 1, 'Monitor 24" IPS', 3.00, 120000.00, 0.00, 297520.66, 62479.34, 360000.00),
 (14, 8, 2, 'Teclado mecánico', 3.00, 45000.00, 0.00, 111570.25, 23429.75, 135000.00),
 (15, 9, 3, 'Silla ergonómica', 2.00, 85000.00, 0.00, 140495.87, 29504.13, 170000.00),
 (16, 10, 5, 'Mouse inalámbrico', 1.00, 12000.00, 0.00, 9917.36, 2082.64, 12000.00),
 (17, 11, 2, 'Teclado mecánico', 1.00, 45000.00, 0.00, 37190.08, 7809.92, 45000.00),
 (18, 11, 5, 'Mouse inalámbrico', 1.00, 12000.00, 0.00, 9917.36, 2082.64, 12000.00),
 (19, 11, 4, 'Auriculares HyperX', 1.00, 15500.00, 0.00, 12809.92, 2690.08, 15500.00),
 (20, 12, 7, 'ARROZ', 3.00, 1500.00, 0.00, 3719.01, 780.99, 4500.00),
 (21, 12, 11, 'GASEOSA 2.25L PACK x6', 1.00, 12750.00, 0.00, 10537.19, 2212.81, 12750.00);

-- ---------------------------------------------------------------------
-- 4) VERIFICACION RAPIDA (deberia devolver 12 facturas: 10 EMITIDA, 2 ANULADA)
-- ---------------------------------------------------------------------
SELECT f.id, f.numero, DATE(f.fecha_emision) AS fecha, c.denominacion AS cliente, ci.denominacion AS condicion_iva,
       pv.descripcion AS punto_venta, f.estado, f.importe_total, COUNT(d.id) AS items
FROM factura_venta f
JOIN cliente c ON c.id = f.cliente_id
JOIN condicion_iva ci ON ci.id = f.condicion_iva
JOIN punto_venta pv ON pv.id = f.punto_venta_id
LEFT JOIN factura_venta_detalle d ON d.factura_venta_id = f.id
GROUP BY f.id ORDER BY f.numero;
