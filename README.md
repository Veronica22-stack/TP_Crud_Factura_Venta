# Sistema de Facturación de Venta

Proyecto para la gestión de facturas de venta, desarrollado con backend en Spring Boot (Java) y frontend web en HTML, CSS y TypeScript con Vite.

## Requisitos
- Java 17 o superior
- MySQL Server 8.0
- Node.js y npm

## Ejecución

### 1. Backend
Verificar la configuración de base de datos en `backend/src/main/resources/application.properties` y ejecutar:
```bash
cd backend
./gradlew bootRun
```
El backend se iniciará en `http://localhost:8080`.

### 2. Frontend
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
- Eliminación de facturas.
- Exportación del listado a PDF y Excel.
