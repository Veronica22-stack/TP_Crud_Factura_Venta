# Sistema de Facturación de Venta

Proyecto para la gestión de facturas de venta, con obtención de **CAE** de AFIP/ARCA (ambiente de **homologación**, solo pruebas).
Backend en Spring Boot (Java 17) y frontend en HTML, CSS y TypeScript con Vite.

## Requisitos
- Java 17 o superior
- MySQL Server 8.0
- Node.js y npm
- Certificado de homologación de AFIP (clave privada `.key` y certificado `.crt`) con el servicio `wsfe` autorizado

## Ejecución

### 1. Configuración
Este archivo **no está en el repositorio** (tiene contraseñas). Crear `backend/src/main/resources/application.properties` con este contenido:
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

### 2. Credenciales de AFIP
Colocar la clave y el certificado en `backend/src/main/resources/afip/` con los nombres de `afip.key.path` y `afip.cert.path`.
Los archivos `.key`, `.crt`, `.pfx`, `.p12` y `ticket-*.properties` están en el `.gitignore` y **nunca deben subirse**. La Clave Fiscal no se guarda en ningún archivo del proyecto.

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
- Alta de nueva factura con cálculo automático de subtotales y total.
- Edición y anulación de facturas existentes.
- Eliminación de facturas que no tienen CAE.
- Obtención del CAE de AFIP desde el listado.
- Impresión de la factura en PDF (solo con CAE).
- Exportación del listado a PDF y Excel.
