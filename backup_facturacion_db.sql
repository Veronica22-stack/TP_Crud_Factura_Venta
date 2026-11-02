-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: facturacion_db
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `facturacion_db`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `facturacion_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `facturacion_db`;

--
-- Table structure for table `articulo`
--

DROP TABLE IF EXISTS `articulo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `articulo` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `fecha_alta` datetime(6) NOT NULL,
  `fecha_baja` datetime(6) DEFAULT NULL,
  `fecha_modificacion` datetime(6) DEFAULT NULL,
  `codigo` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `denominacion` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `usuario_baja_id` bigint DEFAULT NULL,
  `usuario_carga_id` bigint NOT NULL,
  `usuario_modificacion_id` bigint NOT NULL,
  `marca_id` bigint DEFAULT NULL,
  `rubro_id` bigint DEFAULT NULL,
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
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `articulo`
--

LOCK TABLES `articulo` WRITE;
/*!40000 ALTER TABLE `articulo` DISABLE KEYS */;
INSERT INTO `articulo` VALUES (1,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000','ART-100','Monitor 24\" IPS',NULL,1,1,1,1),(2,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000','ART-101','Teclado mecánico',NULL,1,1,2,1),(3,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000','ART-102','Silla ergonómica',NULL,1,1,NULL,2),(4,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000','ART-103','Auriculares HyperX',NULL,1,1,3,1),(5,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000','ART-104','Mouse inalámbrico',NULL,1,1,2,1),(6,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000','ART-201','FIDEOS',NULL,1,1,4,3),(7,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000','ART-202','ARROZ',NULL,1,1,5,3),(8,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000','ART-301','GASEOSA 2.25L',NULL,1,1,6,4),(9,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000','ART-211','FIDEOS BULTO x10',NULL,1,1,4,3),(10,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000','ART-212','ARROZ BULTO x10',NULL,1,1,5,3),(11,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000','ART-311','GASEOSA 2.25L PACK x6',NULL,1,1,6,4);
/*!40000 ALTER TABLE `articulo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cliente`
--

DROP TABLE IF EXISTS `cliente`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cliente` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `fecha_alta` datetime(6) NOT NULL,
  `fecha_baja` datetime(6) DEFAULT NULL,
  `fecha_modificacion` datetime(6) DEFAULT NULL,
  `cut_cuil` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `denominacion` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `usuario_baja_id` bigint DEFAULT NULL,
  `usuario_carga_id` bigint NOT NULL,
  `usuario_modificacion_id` bigint NOT NULL,
  `contacto_id` bigint NOT NULL,
  `domicilio_id` bigint NOT NULL,
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
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cliente`
--

LOCK TABLES `cliente` WRITE;
/*!40000 ALTER TABLE `cliente` DISABLE KEYS */;
INSERT INTO `cliente` VALUES (1,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000','00-00000000-0','Consumidor Final',NULL,1,1,1,1),(2,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000','27-40876925-9','Mayra Muñoz',NULL,1,1,2,2),(3,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000','27-47169997-1','Verónica Silva',NULL,1,1,3,3),(4,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000','20-42587631-1','Elian Navarro',NULL,1,1,4,4),(5,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000','20-28111222-3','Juan Pérez',NULL,1,1,5,5),(6,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000','27-33444555-8','María González',NULL,1,1,6,6),(7,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000','20-41222333-5','Tomás Donoso',NULL,1,1,7,7),(8,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000','20-39888777-2','Gabriel Sánchez',NULL,1,1,8,8);
/*!40000 ALTER TABLE `cliente` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `condicion_iva`
--

DROP TABLE IF EXISTS `condicion_iva`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `condicion_iva` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `fecha_alta` datetime(6) NOT NULL,
  `fecha_baja` datetime(6) DEFAULT NULL,
  `fecha_modificacion` datetime(6) DEFAULT NULL,
  `codigo_afip` int NOT NULL,
  `denominacion` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `usuario_baja_id` bigint DEFAULT NULL,
  `usuario_carga_id` bigint NOT NULL,
  `usuario_modificacion_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7k7oo5onf6p2hcy212hnfpsk6` (`usuario_baja_id`),
  KEY `FKo2ue4fdfiupukxrwnmiwbykle` (`usuario_carga_id`),
  KEY `FKgqwpotjhxb32jrq662p8xedsh` (`usuario_modificacion_id`),
  CONSTRAINT `FK7k7oo5onf6p2hcy212hnfpsk6` FOREIGN KEY (`usuario_baja_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKgqwpotjhxb32jrq662p8xedsh` FOREIGN KEY (`usuario_modificacion_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKo2ue4fdfiupukxrwnmiwbykle` FOREIGN KEY (`usuario_carga_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `condicion_iva`
--

LOCK TABLES `condicion_iva` WRITE;
/*!40000 ALTER TABLE `condicion_iva` DISABLE KEYS */;
INSERT INTO `condicion_iva` VALUES (1,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000',1,'IVA Responsable Inscripto',NULL,1,1),(2,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000',6,'Monotributo',NULL,1,1),(3,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000',5,'Consumidor Final',NULL,1,1),(4,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000',4,'IVA Sujeto Exento',NULL,1,1);
/*!40000 ALTER TABLE `condicion_iva` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `contacto`
--

DROP TABLE IF EXISTS `contacto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contacto` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `celular` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `email` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `telefono` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contacto`
--

LOCK TABLES `contacto` WRITE;
/*!40000 ALTER TABLE `contacto` DISABLE KEYS */;
INSERT INTO `contacto` VALUES (1,NULL,'cf@ventas.local','2610000000'),(2,'2615001122','mayra@bakery.com','2614001122'),(3,'2615334455','vero@correo.com','2614334455'),(4,'2618561472','eli@correo.com','2618561472'),(5,NULL,'juan.perez@correo.com','2614123456'),(6,NULL,'maria.gonzalez@correo.com','2614654321'),(7,'2615778899','tomas.donoso@correo.com','2614778899'),(8,'2615990011','gabriel.sanchez@correo.com','2614990011');
/*!40000 ALTER TABLE `contacto` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `domicilio`
--

DROP TABLE IF EXISTS `domicilio`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `domicilio` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre_calle` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `numero_calle` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `domicilio`
--

LOCK TABLES `domicilio` WRITE;
/*!40000 ALTER TABLE `domicilio` DISABLE KEYS */;
INSERT INTO `domicilio` VALUES (1,'Venta Mostrador','S/N'),(2,'Av. San Martín','1500'),(3,'Belgrano','820'),(4,'Recoleta','111'),(5,'San Martín','123'),(6,'Las Heras','456'),(7,'Colón','990'),(8,'Godoy Cruz','2040');
/*!40000 ALTER TABLE `domicilio` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `factura_venta`
--

DROP TABLE IF EXISTS `factura_venta`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `factura_venta` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `fecha_alta` datetime(6) NOT NULL,
  `fecha_baja` datetime(6) DEFAULT NULL,
  `fecha_modificacion` datetime(6) DEFAULT NULL,
  `cae` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `fecha_vto` datetime(6) DEFAULT NULL,
  `estado` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `fecha_anulacion` datetime(6) DEFAULT NULL,
  `fecha_emision` datetime(6) NOT NULL,
  `importe_cobrado` double DEFAULT NULL,
  `importe_saldo` double DEFAULT NULL,
  `importe_total` double NOT NULL,
  `motivo_rechazo` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `numero` bigint DEFAULT NULL,
  `observaciones` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `resultado_afip` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `usuario_baja_id` bigint DEFAULT NULL,
  `usuario_carga_id` bigint NOT NULL,
  `usuario_modificacion_id` bigint NOT NULL,
  `cliente_id` bigint NOT NULL,
  `condicion_iva` bigint NOT NULL,
  `punto_venta_id` bigint NOT NULL,
  `tipo_moneda` bigint NOT NULL,
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
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `factura_venta`
--

LOCK TABLES `factura_venta` WRITE;
/*!40000 ALTER TABLE `factura_venta` DISABLE KEYS */;
INSERT INTO `factura_venta` VALUES (1,'2026-01-15 10:00:00.000000',NULL,'2026-01-15 10:00:00.000000','74100000001001','2026-01-28 00:00:00.000000','EMITIDA',NULL,'2026-01-15 00:00:00.000000',2500,0,2500,NULL,1001,NULL,'A',NULL,1,1,1,3,1,1),(2,'2026-02-03 10:00:00.000000',NULL,'2026-02-03 10:00:00.000000','74100000001002','2026-02-28 00:00:00.000000','EMITIDA',NULL,'2026-02-03 00:00:00.000000',132000,0,132000,NULL,1002,NULL,'A',NULL,1,1,5,1,1,1),(3,'2026-02-20 10:00:00.000000',NULL,'2026-02-20 10:00:00.000000','74100000001003','2026-02-28 00:00:00.000000','EMITIDA',NULL,'2026-02-20 00:00:00.000000',100500,0,100500,NULL,1003,NULL,'A',NULL,1,1,6,2,2,1),(4,'2026-03-10 10:00:00.000000',NULL,'2026-03-10 10:00:00.000000','74100000001004','2026-03-28 00:00:00.000000','EMITIDA',NULL,'2026-03-10 00:00:00.000000',40500,40500,81000,NULL,1004,'Bonificación 10% por cliente frecuente','A',NULL,1,1,2,1,1,1),(5,'2026-03-16 10:00:00.000000',NULL,'2026-03-16 10:00:00.000000','74100000001005','2026-03-28 00:00:00.000000','EMITIDA',NULL,'2026-03-16 00:00:00.000000',29750,0,29750,NULL,1005,'Venta mayorista','A',NULL,1,1,3,2,2,1),(6,'2026-04-02 10:00:00.000000',NULL,'2026-04-02 10:00:00.000000','74100000001006','2026-04-28 00:00:00.000000','ANULADA','2026-04-03 10:15:00.000000','2026-04-02 00:00:00.000000',0,0,240000,NULL,1006,'Anulada por error en los datos del cliente','A',NULL,1,1,4,1,3,1),(7,'2026-05-12 10:00:00.000000',NULL,'2026-05-12 10:00:00.000000','74100000001007','2026-05-28 00:00:00.000000','EMITIDA',NULL,'2026-05-12 00:00:00.000000',15500,0,15500,NULL,1007,NULL,'A',NULL,1,1,1,3,4,1),(8,'2026-06-08 10:00:00.000000',NULL,'2026-06-08 10:00:00.000000','74100000001008','2026-06-28 00:00:00.000000','EMITIDA',NULL,'2026-06-08 00:00:00.000000',495000,0,495000,NULL,1008,'Venta mayorista','A',NULL,1,1,5,1,3,1),(9,'2026-07-21 10:00:00.000000',NULL,'2026-07-21 10:00:00.000000','74100000001009','2026-07-28 00:00:00.000000','EMITIDA',NULL,'2026-07-21 00:00:00.000000',170000,0,170000,NULL,1009,NULL,'A',NULL,1,1,7,4,1,1),(10,'2026-08-30 10:00:00.000000',NULL,'2026-08-30 10:00:00.000000','74100000001010','2026-08-28 00:00:00.000000','ANULADA','2026-08-31 18:40:00.000000','2026-08-30 00:00:00.000000',0,0,12000,NULL,1010,'Anulada a pedido del cliente','A',NULL,1,1,6,2,2,1),(11,'2026-09-15 10:00:00.000000',NULL,'2026-09-15 10:00:00.000000','74100000001011','2026-09-28 00:00:00.000000','EMITIDA',NULL,'2026-09-15 00:00:00.000000',72500,0,72500,NULL,1011,NULL,'A',NULL,1,1,8,1,1,1),(12,'2026-10-05 10:00:00.000000',NULL,'2026-10-05 10:00:00.000000','74100000001012','2026-10-28 00:00:00.000000','EMITIDA',NULL,'2026-10-05 00:00:00.000000',17250,0,17250,NULL,1012,NULL,'A',NULL,1,1,1,3,4,1),(13,'2026-10-19 23:48:04.009000',NULL,'2026-10-19 23:48:12.173000',NULL,NULL,'EMITIDA',NULL,'2026-10-19 00:00:00.000000',65750,0,65750,NULL,1013,NULL,NULL,NULL,1,1,4,1,4,1);
/*!40000 ALTER TABLE `factura_venta` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `factura_venta_detalle`
--

DROP TABLE IF EXISTS `factura_venta_detalle`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `factura_venta_detalle` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cantidad` double NOT NULL,
  `descripcion` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `importe_iva` double DEFAULT NULL,
  `importe_neto` double DEFAULT NULL,
  `subtotal` double NOT NULL,
  `porcentaje_ue_bonificacion` double DEFAULT NULL,
  `precio_unitario` double NOT NULL,
  `factura_venta_id` bigint NOT NULL,
  `lista_precio_articulo_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK980067exbf0ww2xm57et0115` (`factura_venta_id`),
  KEY `FK6eg1d0og3ed334g0hbfjvu9nr` (`lista_precio_articulo_id`),
  CONSTRAINT `FK6eg1d0og3ed334g0hbfjvu9nr` FOREIGN KEY (`lista_precio_articulo_id`) REFERENCES `lista_precio_articulo` (`id`),
  CONSTRAINT `FK980067exbf0ww2xm57et0115` FOREIGN KEY (`factura_venta_id`) REFERENCES `factura_venta` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `factura_venta_detalle`
--

LOCK TABLES `factura_venta_detalle` WRITE;
/*!40000 ALTER TABLE `factura_venta_detalle` DISABLE KEYS */;
INSERT INTO `factura_venta_detalle` VALUES (1,2,'FIDEOS',173.55,826.45,1000,0,500,1,6),(2,1,'ARROZ',260.33,1239.67,1500,0,1500,1,7),(3,1,'Monitor 24\" IPS',20826.45,99173.55,120000,0,120000,2,1),(4,1,'Mouse inalámbrico',2082.64,9917.36,12000,0,12000,2,5),(5,1,'Silla ergonómica',14752.07,70247.93,85000,0,85000,3,3),(6,1,'Auriculares HyperX',2690.08,12809.92,15500,0,15500,3,4),(7,2,'Teclado mecánico',14057.85,66942.15,81000,10,45000,4,2),(8,1,'FIDEOS BULTO x10',737.6,3512.4,4250,0,4250,5,9),(9,1,'ARROZ BULTO x10',2212.81,10537.19,12750,0,12750,5,10),(10,1,'GASEOSA 2.25L PACK x6',2212.81,10537.19,12750,0,12750,5,11),(11,2,'Monitor 24\" IPS',41652.89,198347.11,240000,0,120000,6,1),(12,1,'Auriculares HyperX',2690.08,12809.92,15500,0,15500,7,4),(13,3,'Monitor 24\" IPS',62479.34,297520.66,360000,0,120000,8,1),(14,3,'Teclado mecánico',23429.75,111570.25,135000,0,45000,8,2),(15,2,'Silla ergonómica',29504.13,140495.87,170000,0,85000,9,3),(16,1,'Mouse inalámbrico',2082.64,9917.36,12000,0,12000,10,5),(17,1,'Teclado mecánico',7809.92,37190.08,45000,0,45000,11,2),(18,1,'Mouse inalámbrico',2082.64,9917.36,12000,0,12000,11,5),(19,1,'Auriculares HyperX',2690.08,12809.92,15500,0,15500,11,4),(20,3,'ARROZ',780.99,3719.01,4500,0,1500,12,7),(21,1,'GASEOSA 2.25L PACK x6',2212.81,10537.19,12750,0,12750,12,11),(22,1,'FIDEOS',86.78,413.22,500,0,500,13,6),(28,1,'ARROZ',260.33,1239.67,1500,0,1500,13,7),(29,5,'GASEOSA 2.25L PACK x6',11064.05,52685.95,63750,0,12750,13,11);
/*!40000 ALTER TABLE `factura_venta_detalle` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lista_precio`
--

DROP TABLE IF EXISTS `lista_precio`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lista_precio` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `fecha_alta` datetime(6) NOT NULL,
  `fecha_baja` datetime(6) DEFAULT NULL,
  `fecha_modificacion` datetime(6) DEFAULT NULL,
  `codigo` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `denominacion` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `usuario_baja_id` bigint DEFAULT NULL,
  `usuario_carga_id` bigint NOT NULL,
  `usuario_modificacion_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK6lrrl28mrcwvowctpth0fmn6q` (`usuario_baja_id`),
  KEY `FKbl4w2f194qlf0p1krt8nxpya0` (`usuario_carga_id`),
  KEY `FKrd09q2iv6nor5k41on0yw112o` (`usuario_modificacion_id`),
  CONSTRAINT `FK6lrrl28mrcwvowctpth0fmn6q` FOREIGN KEY (`usuario_baja_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKbl4w2f194qlf0p1krt8nxpya0` FOREIGN KEY (`usuario_carga_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKrd09q2iv6nor5k41on0yw112o` FOREIGN KEY (`usuario_modificacion_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lista_precio`
--

LOCK TABLES `lista_precio` WRITE;
/*!40000 ALTER TABLE `lista_precio` DISABLE KEYS */;
INSERT INTO `lista_precio` VALUES (1,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000','LP-01','Lista Contado',NULL,1,1),(2,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000','LP-02','Lista Mayorista',NULL,1,1);
/*!40000 ALTER TABLE `lista_precio` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lista_precio_articulo`
--

DROP TABLE IF EXISTS `lista_precio_articulo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lista_precio_articulo` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `fecha_alta` datetime(6) NOT NULL,
  `fecha_baja` datetime(6) DEFAULT NULL,
  `fecha_modificacion` datetime(6) DEFAULT NULL,
  `precio_venta` double NOT NULL,
  `usuario_baja_id` bigint DEFAULT NULL,
  `usuario_carga_id` bigint NOT NULL,
  `usuario_modificacion_id` bigint NOT NULL,
  `articulo_id` bigint NOT NULL,
  `lista_precio_id` bigint NOT NULL,
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
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lista_precio_articulo`
--

LOCK TABLES `lista_precio_articulo` WRITE;
/*!40000 ALTER TABLE `lista_precio_articulo` DISABLE KEYS */;
INSERT INTO `lista_precio_articulo` VALUES (1,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000',120000,NULL,1,1,1,1),(2,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000',45000,NULL,1,1,2,1),(3,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000',85000,NULL,1,1,3,1),(4,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000',15500,NULL,1,1,4,1),(5,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000',12000,NULL,1,1,5,1),(6,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000',500,NULL,1,1,6,1),(7,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000',1500,NULL,1,1,7,1),(8,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000',2500,NULL,1,1,8,1),(9,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000',4250,NULL,1,1,9,2),(10,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000',12750,NULL,1,1,10,2),(11,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000',12750,NULL,1,1,11,2);
/*!40000 ALTER TABLE `lista_precio_articulo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `marca`
--

DROP TABLE IF EXISTS `marca`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `marca` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `fecha_alta` datetime(6) NOT NULL,
  `fecha_baja` datetime(6) DEFAULT NULL,
  `fecha_modificacion` datetime(6) DEFAULT NULL,
  `codigo` int NOT NULL,
  `denominacion` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `usuario_baja_id` bigint DEFAULT NULL,
  `usuario_carga_id` bigint NOT NULL,
  `usuario_modificacion_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKmy2l2s9ydapwyb1fd5b6xif8o` (`usuario_baja_id`),
  KEY `FKk0e9khknw7cpv7mypf0i2h2bd` (`usuario_carga_id`),
  KEY `FK6soabbak4q5ny1si094y41667` (`usuario_modificacion_id`),
  CONSTRAINT `FK6soabbak4q5ny1si094y41667` FOREIGN KEY (`usuario_modificacion_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKk0e9khknw7cpv7mypf0i2h2bd` FOREIGN KEY (`usuario_carga_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKmy2l2s9ydapwyb1fd5b6xif8o` FOREIGN KEY (`usuario_baja_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `marca`
--

LOCK TABLES `marca` WRITE;
/*!40000 ALTER TABLE `marca` DISABLE KEYS */;
INSERT INTO `marca` VALUES (1,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000',101,'Samsung',NULL,1,1),(2,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000',102,'Logitech',NULL,1,1),(3,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000',103,'HyperX',NULL,1,1),(4,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000',104,'Lucchetti',NULL,1,1),(5,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000',105,'Gallo',NULL,1,1),(6,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000',106,'Coca-Cola',NULL,1,1);
/*!40000 ALTER TABLE `marca` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `punto_venta`
--

DROP TABLE IF EXISTS `punto_venta`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `punto_venta` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `fecha_alta` datetime(6) NOT NULL,
  `fecha_baja` datetime(6) DEFAULT NULL,
  `fecha_modificacion` datetime(6) DEFAULT NULL,
  `descripcion` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `domicilio_comercial` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `numero` int NOT NULL,
  `tipo_emision` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `usuario_baja_id` bigint DEFAULT NULL,
  `usuario_carga_id` bigint NOT NULL,
  `usuario_modificacion_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKld9yelsrq2xx0pp30f34tspbl` (`usuario_baja_id`),
  KEY `FKoyboalyvmy632sb0ysugx3bcn` (`usuario_carga_id`),
  KEY `FKalsyftbbbif42rdd9yf0m9d99` (`usuario_modificacion_id`),
  CONSTRAINT `FKalsyftbbbif42rdd9yf0m9d99` FOREIGN KEY (`usuario_modificacion_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKld9yelsrq2xx0pp30f34tspbl` FOREIGN KEY (`usuario_baja_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKoyboalyvmy632sb0ysugx3bcn` FOREIGN KEY (`usuario_carga_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `punto_venta`
--

LOCK TABLES `punto_venta` WRITE;
/*!40000 ALTER TABLE `punto_venta` DISABLE KEYS */;
INSERT INTO `punto_venta` VALUES (1,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000','Punto de Venta Central','Av. San Martín 1050',1,'Electronica',NULL,1,1),(2,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000','Punto de Venta Sucursal Norte','Av. Acceso Este 2300',2,'Electronica',NULL,1,1),(3,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000','Punto de Venta Sucursal Sur','Calle Mitre 455',3,'Electronica',NULL,1,1),(4,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000','Punto de Venta 6','Av. Las Heras 780',6,'Electronica',NULL,1,1);
/*!40000 ALTER TABLE `punto_venta` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rubro`
--

DROP TABLE IF EXISTS `rubro`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rubro` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `fecha_alta` datetime(6) NOT NULL,
  `fecha_baja` datetime(6) DEFAULT NULL,
  `fecha_modificacion` datetime(6) DEFAULT NULL,
  `codigo` int NOT NULL,
  `denominacion` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `usuario_baja_id` bigint DEFAULT NULL,
  `usuario_carga_id` bigint NOT NULL,
  `usuario_modificacion_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKom23psn3i0ddd4xrvjquv0trl` (`usuario_baja_id`),
  KEY `FKtqonsj3vx4yxxcp11qelopf7t` (`usuario_carga_id`),
  KEY `FKpauqw1a06mrj6ucyhif5c462a` (`usuario_modificacion_id`),
  CONSTRAINT `FKom23psn3i0ddd4xrvjquv0trl` FOREIGN KEY (`usuario_baja_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKpauqw1a06mrj6ucyhif5c462a` FOREIGN KEY (`usuario_modificacion_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKtqonsj3vx4yxxcp11qelopf7t` FOREIGN KEY (`usuario_carga_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rubro`
--

LOCK TABLES `rubro` WRITE;
/*!40000 ALTER TABLE `rubro` DISABLE KEYS */;
INSERT INTO `rubro` VALUES (1,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000',5501,'Electrónica',NULL,1,1),(2,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000',5502,'Muebles',NULL,1,1),(3,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000',5503,'Alimentos',NULL,1,1),(4,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000',5504,'Bebidas',NULL,1,1);
/*!40000 ALTER TABLE `rubro` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tipo_moneda`
--

DROP TABLE IF EXISTS `tipo_moneda`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tipo_moneda` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `fecha_alta` datetime(6) NOT NULL,
  `fecha_baja` datetime(6) DEFAULT NULL,
  `fecha_modificacion` datetime(6) DEFAULT NULL,
  `codigo_afip` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `denominacion` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `simbolo` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `usuario_baja_id` bigint DEFAULT NULL,
  `usuario_carga_id` bigint NOT NULL,
  `usuario_modificacion_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK3g5lcl84d3vj0ick8d98kdhjn` (`usuario_baja_id`),
  KEY `FKnkbd6xmfw9kvsu5prdcapoi2k` (`usuario_carga_id`),
  KEY `FKde9d2lmy9cxyw4x65dxika9h` (`usuario_modificacion_id`),
  CONSTRAINT `FK3g5lcl84d3vj0ick8d98kdhjn` FOREIGN KEY (`usuario_baja_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKde9d2lmy9cxyw4x65dxika9h` FOREIGN KEY (`usuario_modificacion_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKnkbd6xmfw9kvsu5prdcapoi2k` FOREIGN KEY (`usuario_carga_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tipo_moneda`
--

LOCK TABLES `tipo_moneda` WRITE;
/*!40000 ALTER TABLE `tipo_moneda` DISABLE KEYS */;
INSERT INTO `tipo_moneda` VALUES (1,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000','PES','Peso Argentino','$',NULL,1,1),(2,'2026-01-02 09:00:00.000000',NULL,'2026-01-02 09:00:00.000000','DOL','Dólar Estadounidense','US$',NULL,1,1);
/*!40000 ALTER TABLE `tipo_moneda` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuario`
--

DROP TABLE IF EXISTS `usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `apellido` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `clave` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `nombre` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `usuario` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario`
--

LOCK TABLES `usuario` WRITE;
/*!40000 ALTER TABLE `usuario` DISABLE KEYS */;
INSERT INTO `usuario` VALUES (1,'Sánchez','pass1234','Gabriel','gsanchez'),(2,'Muñoz','pass1234','Mayra','mmunoz');
/*!40000 ALTER TABLE `usuario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'facturacion_db'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-19 21:44:46
