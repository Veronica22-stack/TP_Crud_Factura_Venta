-- =====================================================================
-- Catálogo igual en TODAS las listas de precios (facturacion_db)
--
-- Problema que corrige: cada lista tenía productos distintos
--   * Lista Contado   -> solo artículos por unidad (Electrónica, Muebles, Alimentos, Bebidas)
--   * Lista Mayorista -> solo bultos (Alimentos, Bebidas)
-- Por eso, al elegir otra lista en "Nueva factura", cambiaban los rubros y los productos.
--
-- Qué hace:
--   1) Agrega productos nuevos a cada categoría (Electrónica, Muebles, Alimentos, Bebidas).
--   2) Completa los precios que faltan para que TODO artículo esté en TODAS las listas:
--        - lista base (la de menor id, "Lista Contado") = precio base
--        - el resto de las listas                        = precio base con 10 % de descuento
--      Si un artículo solo tenía precio en una lista no base (ej. los bultos de la
--      Mayorista), el precio base se calcula como ese precio / 0,9.
--   No modifica precios que ya existen ni toca las facturas.
--
-- Se puede ejecutar más de una vez: no duplica nada.
-- Ejecutar en MySQL Workbench (rayo "Execute") y refrescar el navegador (F5).
-- No hace falta reiniciar el backend.
-- =====================================================================

USE facturacion_db;
SET NAMES utf8mb4;

SET @ahora      := NOW();
SET @usuario    := (SELECT MIN(id) FROM usuario);
SET @lista_base := (SELECT MIN(id) FROM lista_precio);
SET @descuento  := 0.9;   -- precio de las listas que no son la base

-- Ids de las categorías (se buscan por nombre, con o sin tilde)
SET @rubro_ele := (SELECT id FROM rubro WHERE LOWER(denominacion) LIKE 'electr%'  ORDER BY id LIMIT 1);
SET @rubro_mue := (SELECT id FROM rubro WHERE LOWER(denominacion) LIKE 'mueble%'  ORDER BY id LIMIT 1);
SET @rubro_ali := (SELECT id FROM rubro WHERE LOWER(denominacion) LIKE 'aliment%' ORDER BY id LIMIT 1);
SET @rubro_beb := (SELECT id FROM rubro WHERE LOWER(denominacion) LIKE 'bebida%'  ORDER BY id LIMIT 1);

-- 1) Productos nuevos por categoría ------------------------------------
DROP TEMPORARY TABLE IF EXISTS nuevos_productos;
-- Se crea copiando las columnas de articulo y marca para que tenga la misma
-- intercalación (collation) que las tablas reales y se puedan comparar los textos.
CREATE TEMPORARY TABLE nuevos_productos
SELECT a.codigo, a.denominacion, a.rubro_id,
       m.denominacion AS marca,
       CAST(0 AS DOUBLE) AS precio      -- precio de la lista base
FROM articulo a
LEFT JOIN marca m ON m.id = a.marca_id
WHERE 1 = 0;

INSERT INTO nuevos_productos (codigo, denominacion, rubro_id, marca, precio) VALUES
  -- Electrónica
  ('ART-105', 'Smart TV 50" 4K',                  @rubro_ele, 'Samsung',   720000),
  ('ART-106', 'Notebook 15.6" 8GB RAM 512GB SSD', @rubro_ele, 'Samsung',   850000),
  ('ART-107', 'Impresora multifunción WiFi',      @rubro_ele, NULL,        210000),
  ('ART-108', 'Router WiFi 6 doble banda',        @rubro_ele, NULL,         65000),
  ('ART-109', 'Auriculares HyperX Cloud II',      @rubro_ele, 'HyperX',     95000),
  ('ART-110', 'Combo teclado y mouse inalámbrico', @rubro_ele, 'Logitech',  38000),
  -- Muebles
  ('ART-150', 'Escritorio de melamina 120 cm',    @rubro_mue, NULL,        140000),
  ('ART-151', 'Biblioteca 5 estantes',            @rubro_mue, NULL,         95000),
  ('ART-152', 'Mesa ratona de madera',            @rubro_mue, NULL,         70000),
  ('ART-153', 'Silla gamer reclinable',           @rubro_mue, NULL,        210000),
  ('ART-154', 'Cajonera con ruedas 3 cajones',    @rubro_mue, NULL,         65000),
  ('ART-155', 'Sillón de oficina con apoyabrazos', @rubro_mue, NULL,       160000),
  -- Alimentos
  ('ART-203', 'FIDEOS TIRABUZÓN 500g',            @rubro_ali, 'Lucchetti',    650),
  ('ART-204', 'ARROZ INTEGRAL 1kg',               @rubro_ali, 'Gallo',       1900),
  ('ART-205', 'HARINA 000 1kg',                   @rubro_ali, NULL,           900),
  ('ART-206', 'ACEITE DE GIRASOL 1.5L',           @rubro_ali, NULL,          3200),
  ('ART-207', 'AZÚCAR 1kg',                       @rubro_ali, NULL,          1100),
  ('ART-208', 'YERBA MATE 1kg',                   @rubro_ali, NULL,          4800),
  -- Bebidas
  ('ART-302', 'COCA-COLA ZERO 1.5L',              @rubro_beb, 'Coca-Cola',   2100),
  ('ART-303', 'AGUA MINERAL 2L',                  @rubro_beb, NULL,          1200),
  ('ART-304', 'GASEOSA LIMA-LIMÓN 2.25L',         @rubro_beb, NULL,          2300),
  ('ART-305', 'JUGO DE NARANJA 1L',               @rubro_beb, NULL,          1800),
  ('ART-306', 'AGUA SABORIZADA 1.5L',             @rubro_beb, NULL,          1500),
  ('ART-307', 'CERVEZA LATA 473ml',               @rubro_beb, NULL,          1700);

INSERT INTO articulo (fecha_alta, fecha_modificacion, usuario_carga_id, usuario_modificacion_id,
                      codigo, denominacion, rubro_id, marca_id)
SELECT @ahora, @ahora, @usuario, @usuario,
       np.codigo, np.denominacion, np.rubro_id,
       (SELECT m.id FROM marca m WHERE m.denominacion = np.marca ORDER BY m.id LIMIT 1)
FROM nuevos_productos np
WHERE NOT EXISTS (SELECT 1 FROM articulo a WHERE a.codigo = np.codigo);

-- Precio en la lista base (el resto de las listas se completa en el paso 2)
INSERT INTO lista_precio_articulo (fecha_alta, fecha_modificacion, usuario_carga_id, usuario_modificacion_id,
                                   lista_precio_id, articulo_id, precio_venta)
SELECT @ahora, @ahora, @usuario, @usuario, @lista_base, a.id, np.precio
FROM nuevos_productos np
JOIN articulo a ON a.codigo = np.codigo
WHERE NOT EXISTS (SELECT 1 FROM lista_precio_articulo x
                  WHERE x.articulo_id = a.id AND x.lista_precio_id = @lista_base);

DROP TEMPORARY TABLE IF EXISTS nuevos_productos;

-- 2) Completar: todo artículo con precio en todas las listas -----------
-- Precio base de cada artículo: el de la lista base o, si no tiene, el de otra lista / 0,9
DROP TEMPORARY TABLE IF EXISTS precio_base;
CREATE TEMPORARY TABLE precio_base AS
SELECT a.id AS articulo_id,
       COALESCE(
         (SELECT x.precio_venta FROM lista_precio_articulo x
           WHERE x.articulo_id = a.id AND x.lista_precio_id = @lista_base
           ORDER BY x.id LIMIT 1),
         ROUND((SELECT MIN(x.precio_venta) FROM lista_precio_articulo x
                 WHERE x.articulo_id = a.id) / @descuento, 0)
       ) AS precio
FROM articulo a
WHERE a.fecha_baja IS NULL;

INSERT INTO lista_precio_articulo (fecha_alta, fecha_modificacion, usuario_carga_id, usuario_modificacion_id,
                                   lista_precio_id, articulo_id, precio_venta)
SELECT @ahora, @ahora, @usuario, @usuario,
       lp.id, pb.articulo_id,
       ROUND(pb.precio * IF(lp.id = @lista_base, 1, @descuento), 2)
FROM precio_base pb
CROSS JOIN lista_precio lp
WHERE pb.precio IS NOT NULL
  AND lp.fecha_baja IS NULL
  AND NOT EXISTS (SELECT 1 FROM lista_precio_articulo x
                  WHERE x.articulo_id = pb.articulo_id AND x.lista_precio_id = lp.id);

DROP TEMPORARY TABLE IF EXISTS precio_base;

-- 3) Control ------------------------------------------------------------
-- a) Productos por categoría en cada lista: los números tienen que ser iguales en todas las listas
SELECT r.denominacion AS categoria, lp.denominacion AS lista, COUNT(*) AS productos
FROM lista_precio_articulo lpa
JOIN articulo a      ON a.id  = lpa.articulo_id
LEFT JOIN rubro r    ON r.id  = a.rubro_id
JOIN lista_precio lp ON lp.id = lpa.lista_precio_id
GROUP BY r.denominacion, lp.denominacion
ORDER BY r.denominacion, lp.denominacion;

-- b) Artículos a los que les falta precio en alguna lista (tiene que salir vacío)
SELECT a.codigo, a.denominacion, lp.denominacion AS lista_sin_precio
FROM articulo a
CROSS JOIN lista_precio lp
WHERE a.fecha_baja IS NULL AND lp.fecha_baja IS NULL
  AND NOT EXISTS (SELECT 1 FROM lista_precio_articulo x
                  WHERE x.articulo_id = a.id AND x.lista_precio_id = lp.id);
