package com.tpgrupal.desarrollo_de_software.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.tpgrupal.desarrollo_de_software.dto.FacturaReporteDTO;
import com.tpgrupal.desarrollo_de_software.model.FacturaVenta;
import com.tpgrupal.desarrollo_de_software.model.FacturaVentaDetalle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.List;

@Service
public class ReporteService {
    // Datos del emisor que se imprimen en la factura (se pueden cambiar en application.properties)
    @Value("${afip.cuit:}")
    private String cuitEmisor;

    @Value("${empresa.razon-social:Grupo 17 - Desarrollo de Software}")
    private String razonSocialEmisor;

    private static final java.awt.Color GRIS_ENCABEZADO = new java.awt.Color(225, 225, 225);

    /**
     * PDF de una factura con el formato de un comprobante electrónico de AFIP:
     * ORIGINAL, emisor | letra del comprobante | número, receptor, ítems, totales y CAE al pie.
     */
    public byte[] generarPdfFactura(FacturaVenta f) {
        Document document = new Document(PageSize.A4, 30, 30, 30, 30);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        NumberFormat money = NumberFormat.getNumberInstance(new Locale("es", "AR"));
        money.setMinimumFractionDigits(2);
        money.setMaximumFractionDigits(2);

        Font fTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font fEmisor = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15);
        Font fLetra = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 30);
        Font fNegrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
        Font fNormal = FontFactory.getFont(FontFactory.HELVETICA, 9);
        Font fChica = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7);
        Font fLeyenda = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 7);
        Font fTotal = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font fAnulada = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, java.awt.Color.RED);

        // Tipo de comprobante AFIP: 1 = A, 6 = B, 11 = C (por defecto C, el que emite el sistema)
        int tipo = f.getTipoComprobanteAfip() != null ? f.getTipoComprobanteAfip() : 11;
        String letra = tipo == 1 ? "A" : tipo == 6 ? "B" : "C";
        String condicionIvaEmisor = tipo == 11 ? "Responsable Monotributo" : "IVA Responsable Inscripto";

        int ptoVta = f.getPuntoVenta() != null ? f.getPuntoVenta().getNumero() : 1;
        long nroComprobante = f.getNroComprobanteAfip() != null ? f.getNroComprobanteAfip()
                : (f.getNumero() != null ? f.getNumero() : 0);
        String domicilioComercial = f.getPuntoVenta() != null ? nz(f.getPuntoVenta().getDomicilioComercial()) : "-";

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // 1. ORIGINAL
            PdfPTable original = new PdfPTable(1);
            original.setWidthPercentage(100);
            PdfPCell cOriginal = new PdfPCell(new Phrase("ORIGINAL", fTotal));
            cOriginal.setHorizontalAlignment(Element.ALIGN_CENTER);
            cOriginal.setPadding(5);
            original.addCell(cOriginal);
            document.add(original);

            // 2. Encabezado: emisor | letra | datos del comprobante
            PdfPTable cab = new PdfPTable(new float[]{45f, 10f, 45f});
            cab.setWidthPercentage(100);

            PdfPCell cEmisor = new PdfPCell();
            cEmisor.setBorder(Rectangle.LEFT | Rectangle.BOTTOM);
            cEmisor.setPadding(8);
            Paragraph pEmisor = new Paragraph(razonSocialEmisor, fEmisor);
            pEmisor.setAlignment(Element.ALIGN_CENTER);
            pEmisor.setSpacingAfter(10);
            cEmisor.addElement(pEmisor);
            cEmisor.addElement(lineaDato("Razón Social: ", razonSocialEmisor, fNegrita, fNormal));
            cEmisor.addElement(lineaDato("Domicilio Comercial: ", domicilioComercial, fNegrita, fNormal));
            cEmisor.addElement(lineaDato("Condición frente al IVA: ", condicionIvaEmisor, fNegrita, fNormal));
            cab.addCell(cEmisor);

            PdfPTable cajaLetra = new PdfPTable(1);
            cajaLetra.setWidthPercentage(100);
            PdfPCell cLetra = new PdfPCell(new Phrase(letra, fLetra));
            cLetra.setHorizontalAlignment(Element.ALIGN_CENTER);
            cLetra.setBorder(Rectangle.LEFT | Rectangle.RIGHT | Rectangle.BOTTOM);
            cLetra.setPaddingBottom(2);
            cajaLetra.addCell(cLetra);
            PdfPCell cCodigo = new PdfPCell(new Phrase("COD. " + String.format("%03d", tipo), fChica));
            cCodigo.setHorizontalAlignment(Element.ALIGN_CENTER);
            cCodigo.setBorder(Rectangle.LEFT | Rectangle.RIGHT | Rectangle.BOTTOM);
            cCodigo.setPaddingBottom(4);
            cajaLetra.addCell(cCodigo);
            PdfPCell cCentro = new PdfPCell(cajaLetra);
            cCentro.setBorder(Rectangle.BOTTOM);
            cCentro.setPadding(0);
            cab.addCell(cCentro);

            PdfPCell cComp = new PdfPCell();
            cComp.setBorder(Rectangle.RIGHT | Rectangle.BOTTOM);
            cComp.setPadding(8);
            cComp.setPaddingLeft(16);
            Paragraph pFactura = new Paragraph("FACTURA", fTitulo);
            pFactura.setSpacingAfter(8);
            cComp.addElement(pFactura);
            Paragraph pNumero = new Paragraph();
            pNumero.add(new Phrase("Punto de Venta: ", fNegrita));
            pNumero.add(new Phrase(String.format("%05d", ptoVta) + "     ", fNormal));
            pNumero.add(new Phrase("Comp. Nro: ", fNegrita));
            pNumero.add(new Phrase(String.format("%08d", nroComprobante), fNormal));
            cComp.addElement(pNumero);
            cComp.addElement(lineaDato("Fecha de Emisión: ",
                    f.getFechaEmision() != null ? sdf.format(f.getFechaEmision()) : "-", fNegrita, fNormal));
            cComp.addElement(lineaDato("CUIT: ", nz(cuitEmisor), fNegrita, fNormal));
            cComp.addElement(lineaDato("N° interno de factura: ", String.valueOf(f.getNumero()), fNegrita, fNormal));
            cab.addCell(cComp);
            document.add(cab);

            if ("ANULADA".equalsIgnoreCase(f.getEstado())) {
                Paragraph pAnulada = new Paragraph("FACTURA ANULADA", fAnulada);
                pAnulada.setAlignment(Element.ALIGN_CENTER);
                pAnulada.setSpacingBefore(4);
                document.add(pAnulada);
            }

            // 3. Datos del receptor
            PdfPTable rec = new PdfPTable(new float[]{22f, 26f, 30f, 22f});
            rec.setWidthPercentage(100);
            rec.setSpacingBefore(6);
            String cuitCliente = "-", nombreCliente = "Consumidor Final", domicilioCliente = "-";
            if (f.getCliente() != null) {
                cuitCliente = nz(f.getCliente().getCuitCuil());
                nombreCliente = nz(f.getCliente().getDenominacion());
                if (f.getCliente().getDomicilio() != null) {
                    domicilioCliente = (nz(f.getCliente().getDomicilio().getNombreCalle()) + " "
                            + nz(f.getCliente().getDomicilio().getNumeroCalle())).trim();
                }
            }
            String condicionIvaCliente = f.getCondicionIva() != null ? nz(f.getCondicionIva().getDenominacion()) : "-";

            celdaReceptor(rec, "CUIT:", cuitCliente, fNegrita, fNormal, Rectangle.LEFT | Rectangle.TOP);
            celdaReceptor(rec, "Apellido y Nombre / Razón Social:", nombreCliente, fNegrita, fNormal, Rectangle.TOP | Rectangle.RIGHT);
            celdaReceptor(rec, "Condición frente al IVA:", condicionIvaCliente, fNegrita, fNormal, Rectangle.LEFT);
            celdaReceptor(rec, "Domicilio:", domicilioCliente, fNegrita, fNormal, Rectangle.RIGHT);
            celdaReceptor(rec, "Moneda:", f.getTipoMoneda() != null ? nz(f.getTipoMoneda().getDenominacion()) : "-",
                    fNegrita, fNormal, Rectangle.LEFT | Rectangle.BOTTOM);
            celdaReceptor(rec, "", "", fNegrita, fNormal, Rectangle.BOTTOM | Rectangle.RIGHT);
            document.add(rec);

            // 4. Ítems
            PdfPTable det = new PdfPTable(new float[]{1.4f, 5.6f, 1.3f, 1.8f, 1.9f});
            det.setWidthPercentage(100);
            det.setSpacingBefore(8);
            for (String h : new String[]{"Código", "Producto / Servicio", "Cantidad",
                    "Precio Unit.", "Subtotal"}) {
                PdfPCell c = new PdfPCell(new Phrase(h, fNegrita));
                c.setBackgroundColor(GRIS_ENCABEZADO);
                c.setHorizontalAlignment(Element.ALIGN_CENTER);
                c.setPadding(4);
                det.addCell(c);
            }
            double netoTotal = 0, ivaTotal = 0;
            for (FacturaVentaDetalle d : f.getDetalles()) {
                String codigo = d.getListaPrecioArticulo() != null && d.getListaPrecioArticulo().getArticulo() != null
                        ? nz(d.getListaPrecioArticulo().getArticulo().getCodigo()) : "-";
                det.addCell(celdaItem(codigo, fNormal, Element.ALIGN_LEFT));
                det.addCell(celdaItem(nz(d.getDescripcion()), fNormal, Element.ALIGN_LEFT));
                det.addCell(celdaItem(money.format(d.getCantidad()), fNormal, Element.ALIGN_RIGHT));
                det.addCell(celdaItem(money.format(d.getPrecioUnitario()), fNormal, Element.ALIGN_RIGHT));
                det.addCell(celdaItem(money.format(d.getImporteSubtotal()), fNormal, Element.ALIGN_RIGHT));
                netoTotal += d.getImporteNeto();
                ivaTotal += d.getImporteIva();
            }
            document.add(det);

            if (f.getObservaciones() != null && !f.getObservaciones().isBlank()) {
                Paragraph pObs = new Paragraph();
                pObs.add(new Phrase("Observaciones: ", fNegrita));
                pObs.add(new Phrase(f.getObservaciones(), fNormal));
                pObs.setSpacingBefore(6);
                document.add(pObs);
            }

            // 5. Totales (la factura A discrimina el IVA; B y C no)
            PdfPTable tot = new PdfPTable(new float[]{70f, 30f});
            tot.setWidthPercentage(100);
            tot.setSpacingBefore(14);
            if (tipo == 1) {
                filaTotal(tot, "Importe Neto Gravado: $", money.format(netoTotal), fNegrita, Rectangle.LEFT | Rectangle.TOP, Rectangle.RIGHT | Rectangle.TOP);
                filaTotal(tot, "IVA 21%: $", money.format(ivaTotal), fNegrita, Rectangle.LEFT, Rectangle.RIGHT);
            } else {
                filaTotal(tot, "Subtotal: $", money.format(f.getImporteTotal()), fNegrita, Rectangle.LEFT | Rectangle.TOP, Rectangle.RIGHT | Rectangle.TOP);
            }
            filaTotal(tot, "Importe Otros Tributos: $", money.format(0), fNegrita, Rectangle.LEFT, Rectangle.RIGHT);
            filaTotal(tot, "Importe Total: $", money.format(f.getImporteTotal()), fTotal,
                    Rectangle.LEFT | Rectangle.BOTTOM, Rectangle.RIGHT | Rectangle.BOTTOM);
            document.add(tot);

            // 6. Pie: CAE
            PdfPTable pie = new PdfPTable(new float[]{60f, 40f});
            pie.setWidthPercentage(100);
            pie.setSpacingBefore(14);
            PdfPCell cAut = new PdfPCell();
            cAut.setBorder(Rectangle.NO_BORDER);
            boolean autorizada = f.getCae() != null && !f.getCae().isBlank();
            cAut.addElement(new Paragraph(autorizada ? "Comprobante Autorizado" : "Comprobante sin autorizar (sin CAE)", fTotal));
            pie.addCell(cAut);
            PdfPCell cCae = new PdfPCell();
            cCae.setBorder(Rectangle.NO_BORDER);
            Paragraph pCae = lineaDato("CAE N°: ", nz(f.getCae()), fNegrita, fNormal);
            pCae.setAlignment(Element.ALIGN_RIGHT);
            cCae.addElement(pCae);
            Paragraph pVto = lineaDato("Fecha de Vto. de CAE: ",
                    f.getCaeFechaVencimiento() != null ? sdf.format(f.getCaeFechaVencimiento()) : "-", fNegrita, fNormal);
            pVto.setAlignment(Element.ALIGN_RIGHT);
            cCae.addElement(pVto);
            pie.addCell(cCae);
            document.add(pie);

            Paragraph homo = new Paragraph("Comprobante emitido en ambiente de homologación (pruebas), sin validez fiscal.", fLeyenda);
            homo.setAlignment(Element.ALIGN_CENTER);
            homo.setSpacingBefore(18);
            document.add(homo);

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo generar el PDF de la factura", e);
        }
        return out.toByteArray();
    }

    // "Etiqueta: valor" con la etiqueta en negrita
    private static Paragraph lineaDato(String etiqueta, String valor, Font fEtiqueta, Font fValor) {
        Paragraph p = new Paragraph();
        p.add(new Phrase(etiqueta, fEtiqueta));
        p.add(new Phrase(valor, fValor));
        p.setSpacingAfter(2);
        return p;
    }

    private static void celdaReceptor(PdfPTable t, String etiqueta, String valor, Font fEtiqueta, Font fValor, int bordes) {
        PdfPCell cEtiqueta = new PdfPCell(new Phrase(etiqueta, fEtiqueta));
        PdfPCell cValor = new PdfPCell(new Phrase(valor, fValor));
        // la etiqueta lleva el borde izquierdo y el valor el derecho; arriba/abajo van en las dos
        int vertical = bordes & (Rectangle.TOP | Rectangle.BOTTOM);
        boolean esIzquierda = (bordes & Rectangle.LEFT) != 0;
        cEtiqueta.setBorder(vertical | (esIzquierda ? Rectangle.LEFT : Rectangle.NO_BORDER));
        cValor.setBorder(vertical | (bordes & Rectangle.RIGHT));
        cEtiqueta.setPadding(5);
        cValor.setPadding(5);
        t.addCell(cEtiqueta);
        t.addCell(cValor);
    }

    private static PdfPCell celdaItem(String texto, Font font, int alineacion) {
        PdfPCell c = new PdfPCell(new Phrase(texto, font));
        c.setHorizontalAlignment(alineacion);
        c.setBorder(Rectangle.NO_BORDER);
        c.setPadding(4);
        return c;
    }

    private static void filaTotal(PdfPTable t, String etiqueta, String valor, Font font, int bordeEtiqueta, int bordeValor) {
        PdfPCell cEtiqueta = new PdfPCell(new Phrase(etiqueta, font));
        cEtiqueta.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cEtiqueta.setBorder(bordeEtiqueta);
        cEtiqueta.setPadding(4);
        PdfPCell cValor = new PdfPCell(new Phrase(valor, font));
        cValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cValor.setBorder(bordeValor);
        cValor.setPadding(4);
        cValor.setPaddingRight(8);
        t.addCell(cEtiqueta);
        t.addCell(cValor);
    }

    private static String nz(String s) {
        return s == null ? "-" : s;
    }

    private static PdfPTable tablaClaveValor(int cols) {
        PdfPTable t = new PdfPTable(new float[]{2f, 5f});
        t.setWidthPercentage(100);
        t.setSpacingBefore(4);
        return t;
    }

    private static void fila(PdfPTable t, String k, String v, Font fk, Font fv) {
        t.addCell(new Phrase(k, fk));
        t.addCell(new Phrase(v, fv));
    }

    private static PdfPCell derecha(String texto, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(texto, f));
        c.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return c;
    }

    // Generar PDF
    public ByteArrayInputStream generarPdf(List<FacturaReporteDTO> facturas) {
        Document document = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Reporte Ejecutivo de Facturas", fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            // 8 columnas: anchos relativos para que el CAE (14 dígitos) entre en una línea
            PdfPTable table = new PdfPTable(new float[]{1.1f, 1.2f, 2f, 1.8f, 1.3f, 0.8f, 2f, 1.8f});
            table.setWidthPercentage(100);
            table.addCell("N° Factura");
            table.addCell("Fecha");
            table.addCell("Cliente");
            table.addCell("Cond. IVA");
            table.addCell("Total");
            table.addCell("Items");
            table.addCell("Punto Venta");
            table.addCell("CAE");

            for (FacturaReporteDTO f : facturas) {
                table.addCell(f.getNumeroFactura() != null ? String.valueOf(f.getNumeroFactura()) : "-");
                table.addCell(f.getFechaEmision() != null ? sdf.format(f.getFechaEmision()) : "-");
                table.addCell(f.getClienteDenominacion() != null ? f.getClienteDenominacion() : "-");
                table.addCell(f.getCondicionIva() != null ? f.getCondicionIva() : "-");
                table.addCell("$" + f.getImporteTotal());
                table.addCell(String.valueOf(f.getCantidadItems()));
                table.addCell(f.getPuntoVentaDescripcion() != null ? f.getPuntoVentaDescripcion() : "-");
                table.addCell(f.getCae() != null && !f.getCae().isBlank() ? f.getCae() : "-");
            }

            document.add(table);
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    // Generar archivo Excel
    public ByteArrayInputStream generarExcelTsv(List<FacturaReporteDTO> facturas) {
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sb.append("Número Factura\tFecha Emisión\tCliente\tCondición IVA\tPunto Venta\tImporte Total\tCantidad Items\n");

        for (FacturaReporteDTO f : facturas) {
            String fechaStr = f.getFechaEmision() != null ? sdf.format(f.getFechaEmision()) : "-";
            sb.append(f.getNumeroFactura() != null ? f.getNumeroFactura() : "").append("\t")
                    .append(fechaStr).append("\t")
                    .append(f.getClienteDenominacion() != null ? f.getClienteDenominacion() : "").append("\t")
                    .append(f.getCondicionIva() != null ? f.getCondicionIva() : "").append("\t")
                    .append(f.getPuntoVentaDescripcion() != null ? f.getPuntoVentaDescripcion() : "").append("\t")
                    .append(f.getImporteTotal()).append("\t")
                    .append(f.getCantidadItems()).append("\n");
        }

        return new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));
    }
}
