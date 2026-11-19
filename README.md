# Sistema de Facturación de Venta

Proyecto para la gestión de facturas de venta, con obtención de **CAE** de ARCA (ambiente de **homologación**, solo pruebas).
Backend en Spring Boot (Java 17) y frontend en HTML, CSS y TypeScript con Vite.
## Nota
En el proyecto no se encuentran las credenciales de acceso a ARCA por seguridad
## Requisitos
- Java 17 o superior
- MySQL Server 8.0
- Node.js y npm
- Certificado de homologación de ARCA (clave privada `.key` y certificado `.crt`) con el servicio `wsfe` autorizado

## Ejecución

### 1. Configuración
El archivo `backend/src/main/resources/application.properties` ya está incluido en el repositorio con valores de ejemplo. Antes de ejecutar, editarlo con el usuario y contraseña de MySQL (`spring.datasource.username` y `spring.datasource.password`) y con el CUIT (`afip.cuit`). 

### 2. Credenciales de ARCA
Colocar la clave y el certificado en `backend/src/main/resources/afip/` con los nombres de `afip.key.path` y `afip.cert.path`.
Los archivos `.key`, `.crt`, `.pfx`, `.p12` y `ticket-*.properties` están en el `.gitignore`. La Clave Fiscal no está guardada en ningún archivo del proyecto.

### 3. Backend
```bash
cd backend
./gradlew bootRun        # en Windows: .\gradlew bootRun
```
Disponible en `http://localhost:8080`.

### 4. Frontend
Instalar dependencias y levantar el entorno de desarrollo:
```bash
cd frontend
npm install
npm run dev
```
La aplicación web estará disponible en `http://localhost:3000`.

## Funcionalidades
- Inicio de sesión y registro de usuarios (claves guardadas con BCrypt). Todas las rutas `/api/**` requieren sesión, salvo login y registro.
- Listado de facturas con filtros por número, cliente, condición de IVA, estado y rango de fechas.
- Paginación dinámica del listado.
- Alta de nueva factura con cálculo automático de subtotales y total, filtrando los productos que se pueden agregar según la lista de precio seleccionada.
- Obtención del CAE de ARCA desde el listado.
- Edición y anulación de facturas existentes.
- Eliminación de facturas que no tienen CAE.
- Impresión de la factura en PDF (solo con CAE).
- Ordenamiento del listado por fecha, número, importe o cliente (un orden no válido responde 400).
- Exportación del listado a PDF, Excel y TXT (separado por tabulaciones).
- Panel con totales calculados en la base: total facturado, facturas con CAE, pendientes y anuladas.
- Búsqueda de clientes por razón social (sin importar tildes) o por CUIT con o sin guiones.
- Tipo de comprobante por factura (A, B o C), moneda y bonificación % por renglón. En Factura A y B se calcula el neto y el IVA 21% de cada renglón; la Factura A exige un cliente con CUIT.
- Rechazos de ARCA guardados en la factura (resultado y motivo) y mostrados en el listado.

## Integración con ARCA

La propiedad `afip.modo` de `application.properties` define cómo se pide el CAE (si no está, se usa `afip.produccion`):

| Modo | Descripción |
| --- | --- |
| `DESHABILITADO` | No se puede pedir CAE. |
| `SIMULADO` | Genera un CAE ficticio sin conectarse a ARCA (para probar sin certificado ni internet). |
| `HOMOLOGACION` | Ambiente de pruebas de ARCA (por defecto). |
| `PRODUCCION` | Ambiente real. |

Otras propiedades opcionales: `afip.auto-emitir=true` pide el CAE al guardar la factura (por defecto `false`).

Antes de pedir el CAE se valida que la factura no esté anulada y que la fecha de emisión esté entre 5 días antes y 5 días después de hoy, que es lo que acepta ARCA (si no, responde 409). Al comprobante se le envía la fecha de la factura, el CUIT del cliente (si tiene), la condición de IVA del receptor, la moneda y su cotización. Si ARCA rechaza el comprobante, la factura queda guardada con el motivo. Los errores de configuración o de conexión responden 502. La clave privada puede estar en formato PKCS#8 o PKCS#1.

## API agregada

| Método | Ruta | Descripción |
| --- | --- | --- |
| `GET` | `/api/facturas/resumen` | Totales del panel y últimas facturas (`ultimas`, por defecto 5). |
| `POST` | `/api/facturas/{id}/emitir` | Pide el CAE (body opcional: `{"tipoComprobante": 11}`). |
| `GET` | `/api/facturas/export/txt` | Reporte en texto, con los mismos filtros que PDF y Excel. |
| `GET` | `/api/catalogos/productos` | Buscador de productos paginado (`buscar`, `listaPrecioId`, `pagina`, `tamanio`). |
| `GET` | `/api/catalogos/clientes/buscar` | Buscador de clientes paginado (`cuit`, `razonSocial`, `pagina`, `tamanio`). |
| `GET` | `/api/catalogos/monedas` | Monedas (código de ARCA e ISO). |
| `GET` | `/api/catalogos/tipos-comprobante` | Factura A, B y C. |
| `GET` | `/api/afip/estado` | Modo, CUIT y credenciales configuradas. |
| `POST` | `/api/afip/dummy` | Prueba de conexión con WSFEv1 (FEDummy). |
| `POST` | `/api/afip/monedas/sincronizar` | Actualiza las monedas con FEParamGetTiposMonedas. |

El listado de facturas acepta además `clienteId` y busca el texto de `cliente` también en el CUIT.

## Scripts SQL
- `backup_facturacion_db.sql` y `datos_prueba_facturacion.sql`: estructura y datos de prueba.
- `productos_tecnologia.sql`: agrega la categoría "Celulares y Tablets" y productos de tecnología con precio en todas las listas (se puede ejecutar más de una vez).
