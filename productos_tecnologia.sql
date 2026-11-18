-- =====================================================================
-- Productos nuevos de tecnología para facturacion_db
--  * Crea la categoría (rubro) "Celulares y Tablets"
--  * Agrega 3 productos a "Informática" y 3 a "Electrónica"
--  * Agrega 6 productos a "Celulares y Tablets"
--  * Les pone precio en TODAS las listas de precios:
--      la primera lista (la de menor id) con el precio base,
--      el resto con 10 % de descuento.
-- Se puede ejecutar más de una vez: no duplica nada.
-- Ejecutar en MySQL Workbench con el rayo (Execute) y después reiniciar
-- la pantalla del navegador (F5). No hace falta reiniciar el backend.
-- =====================================================================

USE facturacion_db;

SET @ahora      := NOW();
SET @usuario    := (SELECT MIN(id) FROM usuario);
SET @lista_base := (SELECT MIN(id) FROM lista_precio);

-- 1) Nueva categoría ---------------------------------------------------
SET @existe_rubro := (SELECT COUNT(*) FROM rubro WHERE denominacion = 'Celulares y Tablets');
SET @codigo_rubro := (SELECT COALESCE(MAX(codigo), 5500) + 1 FROM rubro);

INSERT INTO rubro (fecha_alta, fecha_modificacion, usuario_carga_id, usuario_modificacion_id, codigo, denominacion)
SELECT @ahora, @ahora, @usuario, @usuario, @codigo_rubro, 'Celulares y Tablets'
FROM DUAL
WHERE @existe_rubro = 0;

-- Ids de las 3 categorías (se buscan por nombre, con o sin tilde)
SET @rubro_inf := (SELECT id FROM rubro WHERE LOWER(denominacion) LIKE 'inform%' ORDER BY id LIMIT 1);
SET @rubro_ele := (SELECT id FROM rubro WHERE LOWER(denominacion) LIKE 'electr%' ORDER BY id LIMIT 1);
SET @rubro_cel := (SELECT id FROM rubro WHERE denominacion = 'Celulares y Tablets' ORDER BY id LIMIT 1);

-- 2) Productos nuevos ---------------------------------------------------
DROP TEMPORARY TABLE IF EXISTS nuevos_productos;
CREATE TEMPORARY TABLE nuevos_productos (
  codigo       VARCHAR(50),
  denominacion VARCHAR(255),
  rubro_id     BIGINT,
  marca        VARCHAR(100),
  precio       DOUBLE
);

INSERT INTO nuevos_productos (codigo, denominacion, rubro_id, marca, precio) VALUES
  -- Informática
  ('TEC-501', 'Disco SSD 1TB NVMe',              @rubro_inf, 'Samsung',  78000),
  ('TEC-502', 'Memoria RAM 16GB DDR4',           @rubro_inf, NULL,       52000),
  ('TEC-503', 'Webcam Full HD 1080p',            @rubro_inf, 'Logitech', 46000),
  -- Electrónica
  ('TEC-511', 'Parlante Bluetooth portátil',     @rubro_ele, NULL,       45000),
  ('TEC-512', 'Smartwatch deportivo',            @rubro_ele, 'Samsung',  98000),
  ('TEC-513', 'Cargador inalámbrico 15W',        @rubro_ele, NULL,       21000),
  -- Celulares y Tablets
  ('TEC-521', 'Smartphone Galaxy A55 128GB',     @rubro_cel, 'Samsung', 650000),
  ('TEC-522', 'Smartphone Moto G84 256GB',       @rubro_cel, NULL,      480000),
  ('TEC-523', 'Tablet 10 pulgadas 128GB',        @rubro_cel, 'Samsung', 320000),
  ('TEC-524', 'Funda protectora para celular',   @rubro_cel, NULL,        9500),
  ('TEC-525', 'Vidrio templado para celular',    @rubro_cel, NULL,        6500),
  ('TEC-526', 'Cargador rápido USB-C 25W',       @rubro_cel, 'Samsung',  28000);

INSERT INTO articulo (fecha_alta, fecha_modificacion, usuario_carga_id, usuario_modificacion_id,
                      codigo, denominacion, rubro_id, marca_id)
SELECT @ahora, @ahora, @usuario, @usuario,
       np.codigo, np.denominacion, np.rubro_id,
       (SELECT m.id FROM marca m WHERE m.denominacion = np.marca ORDER BY m.id LIMIT 1)
FROM nuevos_productos np
WHERE NOT EXISTS (SELECT 1 FROM articulo a WHERE a.codigo = np.codigo);

-- 3) Precios en cada lista ---------------------------------------------
INSERT INTO lista_precio_articulo (fecha_alta, fecha_modificacion, usuario_carga_id, usuario_modificacion_id,
                                   lista_precio_id, articulo_id, precio_venta)
SELECT @ahora, @ahora, @usuario, @usuario,
       lp.id, a.id,
       ROUND(np.precio * IF(lp.id = @lista_base, 1, 0.9), 2)
FROM nuevos_productos np
JOIN articulo a ON a.codigo = np.codigo
CROSS JOIN lista_precio lp
WHERE NOT EXISTS (SELECT 1 FROM lista_precio_articulo x
                  WHERE x.articulo_id = a.id AND x.lista_precio_id = lp.id);

DROP TEMPORARY TABLE IF EXISTS nuevos_productos;

-- 4) Control: cuántos productos con precio tiene cada categoría por lista
SELECT r.denominacion AS categoria, lp.denominacion AS lista, COUNT(*) AS productos
FROM lista_precio_articulo lpa
JOIN articulo a      ON a.id  = lpa.articulo_id
LEFT JOIN rubro r    ON r.id  = a.rubro_id
JOIN lista_precio lp ON lp.id = lpa.lista_precio_id
GROUP BY r.denominacion, lp.denominacion
ORDER BY lp.denominacion, r.denominacion;
