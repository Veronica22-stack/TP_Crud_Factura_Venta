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
Este archivo **no está en el repositorio** (se decidió así porque tiene contraseñas). Crear `backend/src/main/resources/application.properties` con este contenido:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/facturacion_db
spring.datasource.username=TU_USUARIO
spring.datasource.password=TU_CONTRASEÑA
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update

afip.cuit=TU_CUIT
afip.service=wsfe
afip.produccion=false
afip.key.path=src/main/resources/afip/privada_homo.key
afip.cert.path=src/main/resources/afip/certificado_homo.crt
afip.wsaa.url=https://wsaahomo.afip.gov.ar/ws/services/LoginCms
afip.wsfe.url=https://wswhomo.afip.gov.ar/wsfev1/service.asmx
afip.cbte.tipo=11
afip.concepto=1
afip.alicuota.iva=21.0
```

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
- Listado de facturas con filtros por número, cliente, condición de IVA, estado y rango de fechas.
- Paginación dinámica del listado.
- Alta de nueva factura con cálculo automático de subtotales y total, filtrando los productos que se pueden agregar según la lista de precio seleccionada.
- Obtención del CAE de ARCA desde el listado.
- Edición y anulación de facturas existentes.
- Eliminación de facturas que no tienen CAE.
- Impresión de la factura en PDF (solo con CAE).
- Exportación del listado a PDF y Excel.
