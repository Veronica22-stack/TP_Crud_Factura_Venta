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
- Ordenamiento del listado por fecha, importe o cliente.
- Exportación del listado a PDF y Excel.

## Scripts SQL
- `backup_facturacion_db.sql` y `datos_prueba_facturacion.sql`: estructura y datos de prueba.
- `productos_tecnologia.sql`: agrega la categoría "Celulares y Tablets" y productos de tecnología con precio en todas las listas (se puede ejecutar más de una vez).
