package com.tpgrupal.desarrollo_de_software.model;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
//Spring lo hace todo commit(),rollback(),etc...
import org.springframework.transaction.annotation.Transactional;


@Component
public class ConsultasJPQL implements CommandLineRunner {
    //commandlinerunner es un interfaz de hook de inicio de SpringBoot que lo detecta en su contenedor beans
    @PersistenceContext
    private EntityManager em;

    @Transactional // Permite guardar (persistir) objetos en la base de datos
    @Override
    public void run(String... args) throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat("dd/MM/yyyy");

        Date ahora = new Date();

        // Si la base ya tiene facturas (por ejemplo, cargadas con el script SQL), no se vuelven a insertar
        Long facturasExistentes = em.createQuery("SELECT COUNT(f) FROM FacturaVenta f", Long.class).getSingleResult();
        asegurarCondicionesIva(ahora);
        if (facturasExistentes > 0) {
            System.out.println(">> La base ya tiene " + facturasExistentes + " facturas: se omite la carga inicial de datos.");
            return;
        }

        // --- Instanciamos Usuarios ---
        Usuario usuario1 = new Usuario();
        usuario1.setUsuario("gsanchez");
        usuario1.setClave("pass1234");
        usuario1.setNombre("Gabriel");
        usuario1.setApellido("Sánchez");
        em.persist(usuario1);

// --- Clientes ---
        Cliente cliente1 = new Cliente();
        cliente1.setCuitCuil("27-40876925-9");
        cliente1.setDenominacion("Mayra Muñoz");
        cliente1.setFechaAlta(ahora);
        cliente1.setUsuarioCarga(usuario1);
        cliente1.setUsuarioModificacion(usuario1);

        Contacto contacto1 = new Contacto();
        contacto1.setEmail("mayra@bakery.com");
        contacto1.setTelefono("2614001122");
        contacto1.setCelular("2615001122");
        cliente1.setContacto(contacto1);

        Domicilio domicilio1 = new Domicilio();
        domicilio1.setNombreCalle("Av. San Martín");
        domicilio1.setNumeroCalle("1500");
        cliente1.setDomicilio(domicilio1);
        em.persist(cliente1);

        Cliente cliente2 = new Cliente();
        cliente2.setCuitCuil("27-47169997-1");
        cliente2.setDenominacion("Verónica Silva");
        cliente2.setFechaAlta(ahora);
        cliente2.setUsuarioCarga(usuario1);
        cliente2.setUsuarioModificacion(usuario1);

        Contacto contacto2 = new Contacto();
        contacto2.setEmail("vero@correo.com");
        contacto2.setTelefono("2614334455");
        contacto2.setCelular("2615334455");
        cliente2.setContacto(contacto2);

        Domicilio domicilio2 = new Domicilio();
        domicilio2.setNombreCalle("Belgrano");
        domicilio2.setNumeroCalle("820");
        cliente2.setDomicilio(domicilio2);
        em.persist(cliente2);

        Cliente cliente3 = new Cliente();
        cliente3.setCuitCuil("20-42587631-1");
        cliente3.setDenominacion("Elian Navarro");
        cliente3.setFechaAlta(ahora);
        cliente3.setUsuarioCarga(usuario1);
        cliente3.setUsuarioModificacion(usuario1);

        Contacto contacto3 = new Contacto();
        contacto3.setEmail("eli@correo.com");
        contacto3.setTelefono("2618561472");
        contacto3.setCelular("2618561472");
        cliente3.setContacto(contacto3);

        Domicilio domicilio3 = new Domicilio();
        domicilio3.setNombreCalle("Recoleta");
        domicilio3.setNumeroCalle("111");
        cliente3.setDomicilio(domicilio3);
        em.persist(cliente3);

        Cliente clienteJuan = new Cliente();
        clienteJuan.setCuitCuil("20-28111222-3");
        clienteJuan.setDenominacion("Juan Pérez");
        clienteJuan.setFechaAlta(ahora);
        clienteJuan.setUsuarioCarga(usuario1);
        clienteJuan.setUsuarioModificacion(usuario1);
        Contacto contactoJuan = new Contacto();
        contactoJuan.setEmail("juan.perez@correo.com");
        contactoJuan.setTelefono("2614123456");
        clienteJuan.setContacto(contactoJuan);
        Domicilio domicilioJuan = new Domicilio();
        domicilioJuan.setNombreCalle("San Martín");
        domicilioJuan.setNumeroCalle("123");
        clienteJuan.setDomicilio(domicilioJuan);
        em.persist(clienteJuan);

        Cliente clienteMaria = new Cliente();
        clienteMaria.setCuitCuil("27-33444555-8");
        clienteMaria.setDenominacion("María González");
        clienteMaria.setFechaAlta(ahora);
        clienteMaria.setUsuarioCarga(usuario1);
        clienteMaria.setUsuarioModificacion(usuario1);
        Contacto contactoMaria = new Contacto();
        contactoMaria.setEmail("maria.gonzalez@correo.com");
        contactoMaria.setTelefono("2614654321");
        clienteMaria.setContacto(contactoMaria);
        Domicilio domicilioMaria = new Domicilio();
        domicilioMaria.setNombreCalle("Las Heras");
        domicilioMaria.setNumeroCalle("456");
        clienteMaria.setDomicilio(domicilioMaria);
        em.persist(clienteMaria);

        Cliente clienteCF = new Cliente();
        clienteCF.setCuitCuil("00-00000000-0");
        clienteCF.setDenominacion("Consumidor Final");
        clienteCF.setFechaAlta(ahora);
        clienteCF.setUsuarioCarga(usuario1);
        clienteCF.setUsuarioModificacion(usuario1);
        Contacto contactoCF = new Contacto();
        contactoCF.setEmail("cf@ventas.local");
        contactoCF.setTelefono("2610000000");
        clienteCF.setContacto(contactoCF);
        Domicilio domicilioCF = new Domicilio();
        domicilioCF.setNombreCalle("Venta Mostrador");
        domicilioCF.setNumeroCalle("S/N");
        clienteCF.setDomicilio(domicilioCF);
        em.persist(clienteCF);

// --- Puntos de Venta ---
        PuntoVenta pv = new PuntoVenta();
        pv.setNumero(1);
        pv.setDescripcion("Punto de Venta Central");
        pv.setTipoEmision("Electronica");
        pv.setDomicilioComercial("Av. San Martín 1050");
        pv.setFechaAlta(ahora);
        pv.setUsuarioCarga(usuario1);
        pv.setUsuarioModificacion(usuario1);
        em.persist(pv);

        PuntoVenta pv2 = new PuntoVenta();
        pv2.setNumero(2);
        pv2.setDescripcion("Punto de Venta Sucursal Norte");
        pv2.setTipoEmision("Electronica");
        pv2.setDomicilioComercial("Av. Acceso Este 2300");
        pv2.setFechaAlta(ahora);
        pv2.setUsuarioCarga(usuario1);
        pv2.setUsuarioModificacion(usuario1);
        em.persist(pv2);

        PuntoVenta pv3 = new PuntoVenta();
        pv3.setNumero(3);
        pv3.setDescripcion("Punto de Venta Sucursal Sur");
        pv3.setTipoEmision("Manual");
        pv3.setDomicilioComercial("Belgrano 450");
        pv3.setFechaAlta(ahora);
        pv3.setUsuarioCarga(usuario1);
        pv3.setUsuarioModificacion(usuario1);
        em.persist(pv3);

        PuntoVenta pv6 = new PuntoVenta();
        pv6.setNumero(6);
        pv6.setDescripcion("Punto de Venta 6");
        pv6.setTipoEmision("Electronica");
        pv6.setDomicilioComercial("San Martín 200");
        pv6.setFechaAlta(ahora);
        pv6.setUsuarioCarga(usuario1);
        pv6.setUsuarioModificacion(usuario1);
        em.persist(pv6);

// --- Rubros ---
        Rubro rubro1 = new Rubro();
        rubro1.setCodigo(5501);
        rubro1.setDenominacion("Electrónica");
        rubro1.setFechaAlta(ahora);
        rubro1.setUsuarioCarga(usuario1);
        rubro1.setUsuarioModificacion(usuario1);
        em.persist(rubro1);

        Rubro rubro2 = new Rubro();
        rubro2.setCodigo(5502);
        rubro2.setDenominacion("Muebles");
        rubro2.setFechaAlta(ahora);
        rubro2.setUsuarioCarga(usuario1);
        rubro2.setUsuarioModificacion(usuario1);
        em.persist(rubro2);

        Rubro rubroAlimentos = new Rubro();
        rubroAlimentos.setCodigo(5503);
        rubroAlimentos.setDenominacion("Alimentos");
        rubroAlimentos.setFechaAlta(ahora);
        rubroAlimentos.setUsuarioCarga(usuario1);
        rubroAlimentos.setUsuarioModificacion(usuario1);
        em.persist(rubroAlimentos);

// --- Artículos ---
        Articulo articulo1 = new Articulo();
        articulo1.setCodigo("ART-100");
        articulo1.setDenominacion("Monitor 24' IPS");
        articulo1.setFechaAlta(ahora);
        articulo1.setUsuarioCarga(usuario1);
        articulo1.setUsuarioModificacion(usuario1);
        articulo1.setRubro(rubro1);
        em.persist(articulo1);

        Articulo articulo2 = new Articulo();
        articulo2.setCodigo("ART-101");
        articulo2.setDenominacion("Teclado mecánico");
        articulo2.setFechaAlta(ahora);
        articulo2.setUsuarioCarga(usuario1);
        articulo2.setUsuarioModificacion(usuario1);
        articulo2.setRubro(rubro1);
        em.persist(articulo2);

        Articulo articulo3 = new Articulo();
        articulo3.setCodigo("ART-102");
        articulo3.setDenominacion("Silla ergonómica");
        articulo3.setFechaAlta(ahora);
        articulo3.setUsuarioCarga(usuario1);
        articulo3.setUsuarioModificacion(usuario1);
        articulo3.setRubro(rubro2);
        em.persist(articulo3);

        Articulo articuloFideos = new Articulo();
        articuloFideos.setCodigo("ART-201");
        articuloFideos.setDenominacion("FIDEOS");
        articuloFideos.setFechaAlta(ahora);
        articuloFideos.setUsuarioCarga(usuario1);
        articuloFideos.setUsuarioModificacion(usuario1);
        articuloFideos.setRubro(rubroAlimentos);
        em.persist(articuloFideos);

        Articulo articuloArroz = new Articulo();
        articuloArroz.setCodigo("ART-202");
        articuloArroz.setDenominacion("ARROZ");
        articuloArroz.setFechaAlta(ahora);
        articuloArroz.setUsuarioCarga(usuario1);
        articuloArroz.setUsuarioModificacion(usuario1);
        articuloArroz.setRubro(rubroAlimentos);
        em.persist(articuloArroz);

// --- Lista de Precios ---
        ListaPrecio listaPrecio = new ListaPrecio();
        listaPrecio.setCodigo("LP-01");
        listaPrecio.setDenominacion("Lista Contado");
        listaPrecio.setFechaAlta(ahora);
        listaPrecio.setUsuarioCarga(usuario1);
        listaPrecio.setUsuarioModificacion(usuario1);
        em.persist(listaPrecio);

        ListaPrecio listaMayorista = new ListaPrecio();
        listaMayorista.setCodigo("LP-02");
        listaMayorista.setDenominacion("Lista Mayorista");
        listaMayorista.setFechaAlta(ahora);
        listaMayorista.setUsuarioCarga(usuario1);
        listaMayorista.setUsuarioModificacion(usuario1);
        em.persist(listaMayorista);

        ListaPrecioArticulo lpa1 = new ListaPrecioArticulo();
        lpa1.setListaPrecio(listaPrecio);
        lpa1.setArticulo(articulo1);
        lpa1.setPrecioVenta(120000.0);
        lpa1.setFechaAlta(ahora);
        lpa1.setUsuarioCarga(usuario1);
        lpa1.setUsuarioModificacion(usuario1);
        em.persist(lpa1);

        ListaPrecioArticulo lpa2 = new ListaPrecioArticulo();
        lpa2.setListaPrecio(listaPrecio);
        lpa2.setArticulo(articulo2);
        lpa2.setPrecioVenta(45000.0);
        lpa2.setFechaAlta(ahora);
        lpa2.setUsuarioCarga(usuario1);
        lpa2.setUsuarioModificacion(usuario1);
        em.persist(lpa2);

        ListaPrecioArticulo lpa3 = new ListaPrecioArticulo();
        lpa3.setListaPrecio(listaPrecio);
        lpa3.setArticulo(articulo3);
        lpa3.setPrecioVenta(85000.0);
        lpa3.setFechaAlta(ahora);
        lpa3.setUsuarioCarga(usuario1);
        lpa3.setUsuarioModificacion(usuario1);
        em.persist(lpa3);

        ListaPrecioArticulo lpaFideos = new ListaPrecioArticulo();
        lpaFideos.setListaPrecio(listaPrecio);
        lpaFideos.setArticulo(articuloFideos);
        lpaFideos.setPrecioVenta(500.0);
        lpaFideos.setFechaAlta(ahora);
        lpaFideos.setUsuarioCarga(usuario1);
        lpaFideos.setUsuarioModificacion(usuario1);
        em.persist(lpaFideos);

        ListaPrecioArticulo lpaArroz = new ListaPrecioArticulo();
        lpaArroz.setListaPrecio(listaPrecio);
        lpaArroz.setArticulo(articuloArroz);
        lpaArroz.setPrecioVenta(1500.0);
        lpaArroz.setFechaAlta(ahora);
        lpaArroz.setUsuarioCarga(usuario1);
        lpaArroz.setUsuarioModificacion(usuario1);
        em.persist(lpaArroz);

// --- Tipo Moneda ---
        TipoMoneda pesos = new TipoMoneda();
        pesos.setCodigoAfip("PES");
        pesos.setDenominacion("Peso Argentino");
        pesos.setSimbolo("$");
        pesos.setFechaAlta(ahora);
        pesos.setUsuarioCarga(usuario1);
        pesos.setUsuarioModificacion(usuario1);
        em.persist(pesos);

// --- Condición IVA ---
        CondicionIva respInscripto = new CondicionIva();
        respInscripto.setCodigoAfip(1);
        respInscripto.setDenominacion("IVA Responsable Inscripto");
        respInscripto.setFechaAlta(ahora);
        respInscripto.setUsuarioCarga(usuario1);
        respInscripto.setUsuarioModificacion(usuario1);
        em.persist(respInscripto);

        CondicionIva monotributo = new CondicionIva();
        monotributo.setCodigoAfip(6);
        monotributo.setDenominacion("Monotributo");
        monotributo.setFechaAlta(ahora);
        monotributo.setUsuarioCarga(usuario1);
        monotributo.setUsuarioModificacion(usuario1);
        em.persist(monotributo);

        CondicionIva consumidorFinal = new CondicionIva();
        consumidorFinal.setCodigoAfip(5);
        consumidorFinal.setDenominacion("Consumidor Final");
        consumidorFinal.setFechaAlta(ahora);
        consumidorFinal.setUsuarioCarga(usuario1);
        consumidorFinal.setUsuarioModificacion(usuario1);
        em.persist(consumidorFinal);

        CondicionIva exento = new CondicionIva();
        exento.setCodigoAfip(4);
        exento.setDenominacion("IVA Sujeto Exento");
        exento.setFechaAlta(ahora);
        exento.setUsuarioCarga(usuario1);
        exento.setUsuarioModificacion(usuario1);
        em.persist(exento);

// --- Facturas ---
        // Factura 1001
        FacturaVenta factura1001 = new FacturaVenta();
        factura1001.setNumero(1001L);
        factura1001.setFechaEmision(sdf.parse("05/10/2026"));
        factura1001.setFechaAlta(ahora);
        factura1001.setPuntoVenta(pv);
        factura1001.setEstado("EMITIDA");
        factura1001.setImporteTotal(1500.0);
        factura1001.setImporteCobrado(1500.0);
        factura1001.setImporteSaldo(0.0);
        factura1001.setUsuarioCarga(usuario1);
        factura1001.setUsuarioModificacion(usuario1);
        factura1001.setCliente(clienteCF); // Consumidor Final
        factura1001.setTipoMoneda(pesos);
        factura1001.setCondicionIva(respInscripto);

        FacturaVentaDetalle det1001_1 = new FacturaVentaDetalle();
        det1001_1.setListaPrecioArticulo(lpa1);
        det1001_1.setDescripcion("Mouse Óptico USB");
        det1001_1.setCantidad(1);
        det1001_1.setPrecioUnitario(750.0);
        det1001_1.setImporteSubtotal(750.0);
        factura1001.addDetalle(det1001_1);

        FacturaVentaDetalle det1001_2 = new FacturaVentaDetalle();
        det1001_2.setListaPrecioArticulo(lpa2);
        det1001_2.setDescripcion("Pad Mouse Gamer");
        det1001_2.setCantidad(1);
        det1001_2.setPrecioUnitario(750.0);
        det1001_2.setImporteSubtotal(750.0);
        factura1001.addDetalle(det1001_2);
        em.persist(factura1001);

        // Factura 1002: 16/3/2026, Juan Pérez, IVA Responsable Inscripto, Sucursal Central, $3250.50, 2 items
        FacturaVenta factura1002 = new FacturaVenta();
        factura1002.setNumero(1002L);
        factura1002.setFechaEmision(sdf.parse("16/03/2026"));
        factura1002.setFechaAlta(ahora);
        factura1002.setPuntoVenta(pv);
        factura1002.setEstado("EMITIDA");
        factura1002.setImporteTotal(3250.50);
        factura1002.setImporteCobrado(3250.50);
        factura1002.setImporteSaldo(0.0);
        factura1002.setUsuarioCarga(usuario1);
        factura1002.setUsuarioModificacion(usuario1);
        factura1002.setCliente(clienteJuan);
        factura1002.setTipoMoneda(pesos);
        factura1002.setCondicionIva(respInscripto);

        FacturaVentaDetalle det1002_1 = new FacturaVentaDetalle();
        det1002_1.setListaPrecioArticulo(lpa1);
        det1002_1.setDescripcion("Pendrive 64GB Kingston");
        det1002_1.setCantidad(1);
        det1002_1.setPrecioUnitario(1625.25);
        det1002_1.setImporteSubtotal(1625.25);
        factura1002.addDetalle(det1002_1);

        FacturaVentaDetalle det1002_2 = new FacturaVentaDetalle();
        det1002_2.setListaPrecioArticulo(lpa2);
        det1002_2.setDescripcion("Cable HDMI 2.0");
        det1002_2.setCantidad(1);
        det1002_2.setPrecioUnitario(1625.25);
        det1002_2.setImporteSubtotal(1625.25);
        factura1002.addDetalle(det1002_2);
        em.persist(factura1002);

        // Factura 1003: 17/3/2026, María González, Monotributo, Sucursal Norte, $8750.00, 2 items
        FacturaVenta factura1003 = new FacturaVenta();
        factura1003.setNumero(1003L);
        factura1003.setFechaEmision(sdf.parse("17/03/2026"));
        factura1003.setFechaAlta(ahora);
        factura1003.setPuntoVenta(pv2);
        factura1003.setEstado("EMITIDA");
        factura1003.setImporteTotal(8750.0);
        factura1003.setImporteCobrado(8750.0);
        factura1003.setImporteSaldo(0.0);
        factura1003.setUsuarioCarga(usuario1);
        factura1003.setUsuarioModificacion(usuario1);
        factura1003.setCliente(clienteMaria);
        factura1003.setTipoMoneda(pesos);
        factura1003.setCondicionIva(monotributo);

        FacturaVentaDetalle det1003_1 = new FacturaVentaDetalle();
        det1003_1.setListaPrecioArticulo(lpa3);
        det1003_1.setDescripcion("Teclado Inalámbrico Logitech");
        det1003_1.setCantidad(1);
        det1003_1.setPrecioUnitario(4375.0);
        det1003_1.setImporteSubtotal(4375.0);
        factura1003.addDetalle(det1003_1);

        FacturaVentaDetalle det1003_2 = new FacturaVentaDetalle();
        det1003_2.setListaPrecioArticulo(lpa2);
        det1003_2.setDescripcion("Mouse Inalámbrico Logitech");
        det1003_2.setCantidad(1);
        det1003_2.setPrecioUnitario(4375.0);
        det1003_2.setImporteSubtotal(4375.0);
        factura1003.addDetalle(det1003_2);
        em.persist(factura1003);

        // Factura 1005: 5/10/2026, juan perez, IVA Responsable Inscripto, Sucursal Central, $1233.00, 1 item
        FacturaVenta factura1005 = new FacturaVenta();
        factura1005.setNumero(1005L);
        factura1005.setFechaEmision(sdf.parse("05/10/2026"));
        factura1005.setFechaAlta(ahora);
        factura1005.setPuntoVenta(pv);
        factura1005.setEstado("EMITIDA");
        factura1005.setImporteTotal(1233.0);
        factura1005.setImporteCobrado(1233.0);
        factura1005.setImporteSaldo(0.0);
        factura1005.setUsuarioCarga(usuario1);
        factura1005.setUsuarioModificacion(usuario1);
        factura1005.setCliente(clienteJuan);
        factura1005.setTipoMoneda(pesos);
        factura1005.setCondicionIva(respInscripto);

        FacturaVentaDetalle det1005_1 = new FacturaVentaDetalle();
        det1005_1.setListaPrecioArticulo(lpa1);
        det1005_1.setDescripcion("Filtro Antipolvo Magnético");
        det1005_1.setCantidad(1);
        det1005_1.setPrecioUnitario(1233.0);
        det1005_1.setImporteSubtotal(1233.0);
        factura1005.addDetalle(det1005_1);
        em.persist(factura1005);

        // Facturas adicionales
        FacturaVenta factura1006 = new FacturaVenta();
        factura1006.setNumero(1006L);
        factura1006.setFechaEmision(sdf.parse("18/03/2026"));
        factura1006.setFechaAlta(ahora);
        factura1006.setPuntoVenta(pv);
        factura1006.setEstado("EMITIDA");
        factura1006.setImporteTotal(24000.0);
        factura1006.setImporteCobrado(24000.0);
        factura1006.setImporteSaldo(0.0);
        factura1006.setUsuarioCarga(usuario1);
        factura1006.setUsuarioModificacion(usuario1);
        factura1006.setCliente(cliente1);
        factura1006.setTipoMoneda(pesos);
        factura1006.setCondicionIva(respInscripto);
        FacturaVentaDetalle det1006_1 = new FacturaVentaDetalle();
        det1006_1.setListaPrecioArticulo(lpa1);
        det1006_1.setDescripcion("Monitor Gamer 24'");
        det1006_1.setCantidad(1);
        det1006_1.setPrecioUnitario(24000.0);
        det1006_1.setImporteSubtotal(24000.0);
        factura1006.addDetalle(det1006_1);
        em.persist(factura1006);

        FacturaVenta factura1007 = new FacturaVenta();
        factura1007.setNumero(1007L);
        factura1007.setFechaEmision(sdf.parse("19/03/2026"));
        factura1007.setFechaAlta(ahora);
        factura1007.setPuntoVenta(pv3);
        factura1007.setEstado("EMITIDA");
        factura1007.setImporteTotal(15500.0);
        factura1007.setImporteCobrado(15500.0);
        factura1007.setImporteSaldo(0.0);
        factura1007.setUsuarioCarga(usuario1);
        factura1007.setUsuarioModificacion(usuario1);
        factura1007.setCliente(cliente2);
        factura1007.setTipoMoneda(pesos);
        factura1007.setCondicionIva(monotributo);
        FacturaVentaDetalle det1007_1 = new FacturaVentaDetalle();
        det1007_1.setListaPrecioArticulo(lpa2);
        det1007_1.setDescripcion("Auriculares HyperX");
        det1007_1.setCantidad(1);
        det1007_1.setPrecioUnitario(15500.0);
        det1007_1.setImporteSubtotal(15500.0);
        factura1007.addDetalle(det1007_1);
        em.persist(factura1007);

        FacturaVenta factura1008 = new FacturaVenta();
        factura1008.setNumero(1008L);
        factura1008.setFechaEmision(sdf.parse("20/03/2026"));
        factura1008.setFechaAlta(ahora);
        factura1008.setPuntoVenta(pv);
        factura1008.setEstado("EMITIDA");
        factura1008.setImporteTotal(42000.0);
        factura1008.setImporteCobrado(42000.0);
        factura1008.setImporteSaldo(0.0);
        factura1008.setUsuarioCarga(usuario1);
        factura1008.setUsuarioModificacion(usuario1);
        factura1008.setCliente(cliente3);
        factura1008.setTipoMoneda(pesos);
        factura1008.setCondicionIva(respInscripto);
        FacturaVentaDetalle det1008_1 = new FacturaVentaDetalle();
        det1008_1.setListaPrecioArticulo(lpa3);
        det1008_1.setDescripcion("Placa de Video RX 6600");
        det1008_1.setCantidad(1);
        det1008_1.setPrecioUnitario(42000.0);
        det1008_1.setImporteSubtotal(42000.0);
        factura1008.addDetalle(det1008_1);
        em.persist(factura1008);

        em.flush();


        System.out.println("--- EJECUTANDO CONSULTAS JPQL ---");

        try {
            //CONSULTAS JPQL

            //Nivel 1
            List<FacturaVenta> facturasDelSistema = em.createQuery(
                    "SELECT facVta FROM FacturaVenta facVta", FacturaVenta.class
            ).getResultList();
            System.out.println("Ejercicio1");
            for (FacturaVenta f : facturasDelSistema) {
                System.out.println(f.getNumero() + " - " + f.getEstado() + " - " + f.getImporteTotal());
            }

            List<Object[]> facVtaDatosEspecifico = em.createQuery(
                    "SELECT facVta.numero, facVta.fechaEmision, facVta.importeTotal FROM FacturaVenta facVta", Object[].class
            ).getResultList(); //Cada fila es un arreglo de tres posiciones, no el objeto FacturaVenta completa; por me retorna un Object[].class y no una FacturaVenta.class
            System.out.println("Ejercicio2");
            for (Object[] fila : facVtaDatosEspecifico) {
                Long numero = (Long) fila[0];
                Date fechaEmision = (Date) fila[1];
                Double importeTotal = (Double) fila[2];
                System.out.println(numero + " - " + fechaEmision + " - " + importeTotal);
            }

            List<Articulo> articulosMismoRubro = em.createQuery(
                            "SELECT art FROM Articulo art WHERE art.rubro.denominacion =:nombrerubro", Articulo.class
                    ).setParameter("nombrerubro", "electronica")
                    .getResultList();
            System.out.println("Ejercicio3");
            for (Articulo a : articulosMismoRubro) {
                System.out.println(a.getCodigo() + " - " + a.getDenominacion());
            }

            List<FacturaVenta> factVtaRangoFecha = em.createQuery(
                            "SELECT facVta FROM FacturaVenta facVta WHERE facVta.fechaEmision BETWEEN :fecha1 AND :fecha2", FacturaVenta.class
                    ).setParameter("fecha1", sdf.parse("15/02/2026"))
                    .setParameter("fecha2", sdf.parse("15/08/2026"))
                    .getResultList();
            System.out.println("Ejercicio4");
            for (FacturaVenta f : factVtaRangoFecha) {
                System.out.println(f.getNumero() + " - " + f.getFechaEmision() + " - " + f.getImporteTotal());
            }

            //NIVEL2
            List<FacturaVenta> facturasEmitidas = em.createQuery(
                            "SELECT facVta FROM FacturaVenta facVta" +
                                    " WHERE facVta.estado = :estado AND  facVta.importeTotal > :valor AND facVta.fechaAnulacion IS NULL", FacturaVenta.class
                    ).setParameter("estado", "EMITIDA")
                    .setParameter("valor", 10000)
                    .getResultList();
            System.out.println("Ejercicio5");
            for (FacturaVenta f : facturasEmitidas) {
                System.out.println(f.getNumero() + " - " + f.getEstado() + " - " + f.getImporteTotal());
            }

            List<Cliente> clienteMasculino = em.createQuery(
                            "SELECT cli FROM Cliente cli" +
                                    " WHERE LOWER(cli.denominacion) Like :textoParcial OR cli.cuitCuil LIKE '20-%' ", Cliente.class
                    ).setParameter("textoParcial", "%navarro%")
                    .getResultList();
            System.out.println("Ejercicio6");
            for (Cliente cli : clienteMasculino) {
                System.out.println("Datos cliente: " + cli.toString());
            }

            List<String> estadoFactura = em.createQuery(
                    "SELECT DISTINCT facVta.estado FROM FacturaVenta facVta ORDER BY facVta.estado asc", String.class
            ).getResultList(); //es un solo valor, devuelvo ese valor, por eso uso String y no Object[](posicion memoria)
            System.out.println("Ejercicio7");
            System.out.println(estadoFactura);

            Object[] calculosFacEmitidas = em.createQuery(
                    "SELECT COUNT(facVta) , SUM(facVta.importeTotal) , AVG(facVta.importeTotal) FROM FacturaVenta facVta", Object[].class
            ).getSingleResult();
            System.out.println("Ejercicio8");
            System.out.println("cantidad facturas: " + calculosFacEmitidas[0] +
                    "suma acummulada2: " + calculosFacEmitidas[1] +
                    "promedio montos factura: " + calculosFacEmitidas[2]);

            List<PuntoVenta> puntoVentaNumero = em.createQuery(
                            "SELECT ptoVta FROM PuntoVenta ptoVta WHERE ptoVta.numero IN(:arreglo)", PuntoVenta.class
                    ).setParameter("arreglo", List.of(2, 3))
                    .getResultList();
            System.out.println("Ejercicio9");
            for (PuntoVenta p : puntoVentaNumero) {
                System.out.println(p.toString());
            }

            //NIVEL 3

            List<FacturaVenta> facturaUsuarioCarga = em.createQuery(
                            "SELECT facVta FROM FacturaVenta facVta WHERE facVta.usuarioCarga.usuario =:nombre", FacturaVenta.class
                    ).setParameter("nombre", "gsanchez")
                    .getResultList();
            System.out.println("Ejercicio10");
            for (FacturaVenta facVta : facturaUsuarioCarga) {
                System.out.println("nroFactura" + facVta.getNumero() + " estado:" + facVta.getEstado());
            }

            List<FacturaVentaDetalle> detalleFacPtoVta = em.createQuery(
                            "SELECT facVtaDet FROM FacturaVenta facVta " +
                                    "INNER JOIN facVta.detalles facVtaDet " +
                                    "INNER JOIN facVta.puntoVenta ptoVta " +
                                    "WHERE ptoVta.numero = :numeroPuntoVenta", FacturaVentaDetalle.class
                    ).setParameter("numeroPuntoVenta", 2)
                    .getResultList();
            //no hace falta el ON ... porque la relacion me la definen las entidades
            System.out.println("Ejercicio11");
            for (FacturaVentaDetalle d : detalleFacPtoVta) {
                System.out.println(d.getDescripcion() + " - " + d.getCantidad() + " - " + d.getImporteSubtotal());
            }
            List<Object[]> articuloMarca = em.createQuery(

                    "SELECT art.denominacion , marca.denominacion " +

                            "FROM Articulo art LEFT JOIN art.marca marca", Object[].class

            ).getResultList(); //mi clase.atributo de la clase. osea referencio a Marca por ser atributo de articulo!

            System.out.println("Ejercicio12");

            for (Object[] obj : articuloMarca) {

                String denominacionArticulo = (String) obj[0];

                String denominacionMarca = (String) obj[1];

                System.out.println("Articulo: " + denominacionArticulo + " ;Marca: " + denominacionMarca);

            }


            List<FacturaVenta> facturaDetalleMarca = em.createQuery(

                            "SELECT DISTINCT factVta FROM FacturaVenta factVta " +

                                    "INNER JOIN factVta.detalles detalleVta " +

                                    "INNER JOIN detalleVta.listaPrecioArticulo listaPrecio " +

                                    "INNER JOIN listaPrecio.articulo art " +

                                    "INNER JOIN art.marca marca " +

                                    "WHERE marca.denominacion =:nombre", FacturaVenta.class

                    ).setParameter("nombre", "Samsung") //Agrego DISTINCT porque si una factura tiene 2 detalles de la misma marca saldria repetida

                    .getResultList();

            System.out.println("Ejercicio13");

            for (FacturaVenta factVta : facturaDetalleMarca) {

                System.out.println(factVta.getNumero() + " - " + factVta.getEstado() + " - " + factVta.getImporteTotal());

            }


            List<FacturaVenta> facturasSupPromedio = em.createQuery(

                    "SELECT factVta FROM FacturaVenta factVta " +

                            "WHERE factVta.importeTotal > (SELECT AVG(factVta2.importeTotal) FROM FacturaVenta factVta2)"

                    , FacturaVenta.class

            ).getResultList();

            System.out.println("Ejercicio14");

            System.out.println("Ejercicio14");

            for (FacturaVenta factVta : facturasSupPromedio) {

                System.out.println(factVta.getNumero() + " - " + factVta.getEstado() + " - " + factVta.getImporteTotal());

            }


//NIVEL 4

            List<Object[]> puntoVentaMontoVenta = em.createQuery(

                    "SELECT ptoVta.descripcion, COUNT(factVta), SUM(factVta.importeTotal) " +

                            " FROM FacturaVenta factVta " +

                            "JOIN factVta.puntoVenta ptoVta " +

                            "GROUP BY ptoVta.descripcion", Object[].class

            ).getResultList();

            System.out.println("Ejercicio15");

            for (Object[] obj : puntoVentaMontoVenta) {

                String descrip = (String) obj[0];

                Long cantFact = (Long) obj[1]; //COUNT devuelve un long

                Double sumaTotal = (Double) obj[2];

                System.out.println("Punto venta: " + descrip + " , cant de facturas: " + cantFact + " , suma total facturada: " + sumaTotal);

            }

            //en los GROUP BY al no hacer ptoVta.id;atributo
            List<String> nombreUsuarios = em.createQuery(

                    "SELECT usu.nombre FROM FacturaVenta factVta " +

                            "JOIN factVta.usuarioCarga usu " +

                            "GROUP BY usu.nombre " +

                            "HAVING COUNT(factVta) > 2"//ejercicio dice 5 pero tengo 3 facturas

                    , String.class

            ).getResultList();

            System.out.println("Ejercicio16");

            System.out.println(nombreUsuarios);


            List<Object[]> datosMarca = em.createQuery(

                    "SELECT marca.denominacion , SUM(factDt.cantidad), SUM(factDt.importeSubtotal) " +

                            "FROM FacturaVentaDetalle factDt " +

                            "JOIN factDt.listaPrecioArticulo listaprecio " +

                            "JOIN listaprecio.articulo art " +

                            "JOIN art.marca marca " +

                            "GROUP BY marca.denominacion"

                    , Object[].class

            ).getResultList();

            System.out.println("Ejercicio17");

            for (Object[] obj : datosMarca) {

                String denominacionMarca = (String) obj[0];

                Double unidadesVendidas = (Double) obj[1];// SUM de un entero llega como Long

                Double subtotal = (Double) obj[2];// SUM de un double llega como Double

                System.out.println("Marca: " + denominacionMarca

                        + " , unidades vendidas: " + unidadesVendidas

                        + " , subtotal acumulado: " + subtotal);

            }


            List<Marca> marcasFacturadas = em.createQuery(

                    "SELECT marca1 FROM Marca marca1 " +

                            "WHERE EXISTS (SELECT factVtaD FROM FacturaVentaDetalle factVtaD " +

                            "JOIN factVtaD.listaPrecioArticulo lpa " +

                            "JOIN lpa.articulo art " +

                            "WHERE art.marca = marca1)", Marca.class

            ).getResultList();

            System.out.println("Ejercicio18");

            for (Marca m : marcasFacturadas) {

                System.out.println(m.getDenominacion());

            }


            List<Articulo> articulosSinVenta = em.createQuery(

                    "SELECT art FROM Articulo art " +

                            "WHERE NOT EXISTS(SELECT factVtaD FROM FacturaVentaDetalle factVtaD " +

                            "JOIN factVtaD.listaPrecioArticulo lpa " +

                            "WHERE lpa.articulo = art) "

                    , Articulo.class

            ).getResultList();

            System.out.println("Ejercicio19");

            for (Articulo a : articulosSinVenta) {

                System.out.println(a.getCodigo() + " - " + a.getDenominacion());

            }


            List<Object[]> facturasCategorizadas = em.createQuery(

                    "SELECT factVta.numero , factVta.importeTotal, " +

                            "CASE " +

                            "WHEN factVta.importeTotal > 50000 THEN 'ALTO VALOR' " +

                            "WHEN factVta.importeTotal BETWEEN 10000 AND 50000 THEN 'MEDIO VALOR' " +

                            "ELSE 'BAJO VALOR' " +

                            "END AS categoria " +

                            "FROM FacturaVenta factVta " +

                            "ORDER BY factVta.importeTotal desc"

                    , Object[].class

            ).getResultList();

            System.out.println("Ejercicio20");

            for (Object[] obj : facturasCategorizadas) {

                Long numero = (Long) obj[0];

                Double importe = (Double) obj[1];

                String categoria = (String) obj[2];

                System.out.println("Factura: " + numero + " , importe: " + importe + " , categoría: " + categoria);

            }
        } catch (Exception e) {
            System.err.println("Error durante la carga inicial: " + e.getMessage());
            throw e; //Se la re-lanzamos a Spring para que haga el ROLLBACK
            //SIN bloque finally todo lo hace @transactional
        }

    }

    private void asegurarCondicionesIva(Date ahora) {
        asegurarCondicion(1, "IVA Responsable Inscripto", ahora);
        asegurarCondicion(6, "Monotributo", ahora);
        asegurarCondicion(5, "Consumidor Final", ahora);
        asegurarCondicion(4, "IVA Sujeto Exento", ahora);
    }

    private void asegurarCondicion(int codigoAfip, String denominacion, Date ahora) {
        List<CondicionIva> exist = em.createQuery(
                "SELECT c FROM CondicionIva c WHERE LOWER(c.denominacion) = LOWER(:denom)", CondicionIva.class)
                .setParameter("denom", denominacion)
                .getResultList();
        if (exist.isEmpty()) {
            List<Usuario> us = em.createQuery("SELECT u FROM Usuario u", Usuario.class).setMaxResults(1).getResultList();
            Usuario u = us.isEmpty() ? null : us.get(0);
            if (u == null) {
                u = new Usuario();
                u.setUsuario("admin");
                u.setClave("pass1234");
                u.setNombre("Admin");
                u.setApellido("Sistema");
                em.persist(u);
            }
            CondicionIva c = new CondicionIva();
            c.setCodigoAfip(codigoAfip);
            c.setDenominacion(denominacion);
            c.setFechaAlta(ahora != null ? ahora : new Date());
            c.setUsuarioCarga(u);
            c.setUsuarioModificacion(u);
            em.persist(c);
        }
    }
}
