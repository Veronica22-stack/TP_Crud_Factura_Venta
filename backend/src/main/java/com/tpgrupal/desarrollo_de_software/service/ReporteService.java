package com.tpgrupal.desarrollo_de_software.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.tpgrupal.desarrollo_de_software.dto.FacturaReporteDTO;
import com.tpgrupal.desarrollo_de_software.model.FacturaVenta;
import com.tpgrupal.desarrollo_de_software.model.FacturaVentaDetalle;
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
    public byte[] generarPdfFactura(FacturaVenta f) {
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        NumberFormat money = NumberFormat.getNumberInstance(new Locale("es", "AR"));
        money.setMinimumFractionDigits(2);
        money.setMaximumFractionDigits(2);

        Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font seccion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font negrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font chica = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Paragraph t = new Paragraph("FACTURA", titulo);
            t.setAlignment(Element.ALIGN_CENTER);
            document.add(t);
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Datos de la factura", seccion));
            PdfPTable cab = tablaClaveValor(2);
            fila(cab, "N° de factura", String.valueOf(f.getNumero()), negrita, normal);
            fila(cab, "Fecha de emisión", f.getFechaEmision() != null ? sdf.format(f.getFechaEmision()) : "-", negrita, normal);
            fila(cab, "Punto de venta", f.getPuntoVenta() != null ? f.getPuntoVenta().getNumero() + " - " + nz(f.getPuntoVenta().getDescripcion()) : "-", negrita, normal);
            fila(cab, "Condición IVA", f.getCondicionIva() != null ? nz(f.getCondicionIva().getDenominacion()) : "-", negrita, normal);
            fila(cab, "Moneda", f.getTipoMoneda() != null ? nz(f.getTipoMoneda().getDenominacion()) : "-", negrita, normal);
            fila(cab, "Estado", nz(f.getEstado()), negrita, normal);
            document.add(cab);
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Datos del cliente", seccion));
            PdfPTable cli = tablaClaveValor(2);
            if (f.getCliente() != null) {
                fila(cli, "Denominación", nz(f.getCliente().getDenominacion()), negrita, normal);
                fila(cli, "CUIT/CUIL", nz(f.getCliente().getCuitCuil()), negrita, normal);
                if (f.getCliente().getDomicilio() != null) {
                    fila(cli, "Domicilio", (nz(f.getCliente().getDomicilio().getNombreCalle()) + " "
                            + nz(f.getCliente().getDomicilio().getNumeroCalle())).trim(), negrita, normal);
                }
                if (f.getCliente().getContacto() != null) {
                    fila(cli, "Email", nz(f.getCliente().getContacto().getEmail()), negrita, normal);
                    fila(cli, "Teléfono", nz(f.getCliente().getContacto().getTelefono()), negrita, normal);
                }
            } else {
                fila(cli, "Denominación", "Consumidor Final", negrita, normal);
            }
            document.add(cli);
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Detalle", seccion));
            PdfPTable det = new PdfPTable(new float[]{4f, 1.2f, 1.8f, 1.4f, 1.8f});
            det.setWidthPercentage(100);
            det.setSpacingBefore(4);
            for (String h : new String[]{"Descripción", "Cant.", "Precio unit.", "Bonif. %", "Subtotal"}) {
                PdfPCell c = new PdfPCell(new Phrase(h, negrita));
                c.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
                det.addCell(c);
            }
            for (FacturaVentaDetalle d : f.getDetalles()) {
                det.addCell(new Phrase(nz(d.getDescripcion()), normal));
                det.addCell(derecha(String.valueOf(d.getCantidad()), normal));
                det.addCell(derecha("$ " + money.format(d.getPrecioUnitario()), normal));
                det.addCell(derecha(money.format(d.getPorcentajeBonificacion()), normal));
                det.addCell(derecha("$ " + money.format(d.getImporteSubtotal()), normal));
            }
            PdfPCell vacio = new PdfPCell(new Phrase("TOTAL", negrita));
            vacio.setColspan(4);
            vacio.setHorizontalAlignment(Element.ALIGN_RIGHT);
            det.addCell(vacio);
            det.addCell(derecha("$ " + money.format(f.getImporteTotal()), negrita));
            document.add(det);
            if (f.getObservaciones() != null && !f.getObservaciones().isBlank()) {
                document.add(new Paragraph("Observaciones: " + f.getObservaciones(), normal));
            }
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Datos del CAE (AFIP / ARCA)", seccion));
            PdfPTable cae = tablaClaveValor(2);
            fila(cae, "CAE", nz(f.getCae()), negrita, normal);
            fila(cae, "Vencimiento CAE", f.getCaeFechaVencimiento() != null ? sdf.format(f.getCaeFechaVencimiento()) : "-", negrita, normal);
            fila(cae, "Resultado", "A".equals(f.getResultadoAfip()) ? "Aprobado" : nz(f.getResultadoAfip()), negrita, normal);
            if (f.getTipoComprobanteAfip() != null) {
                fila(cae, "Tipo de comprobante", String.valueOf(f.getTipoComprobanteAfip()), negrita, normal);
            }
            if (f.getNroComprobanteAfip() != null) {
                fila(cae, "N° de comprobante AFIP", String.valueOf(f.getNroComprobanteAfip()), negrita, normal);
            }
            document.add(cae);
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Comprobante emitido en ambiente de homologación (pruebas), sin validez fiscal.", chica));

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo generar el PDF de la factura", e);
        }
        return out.toByteArray();
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

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.addCell("N° Factura");
            table.addCell("Fecha");
            table.addCell("Cliente");
            table.addCell("Cond. IVA");
            table.addCell("Total");
            table.addCell("Items");
            table.addCell("Punto Venta");

            for (FacturaReporteDTO f : facturas) {
                table.addCell(f.getNumeroFactura() != null ? String.valueOf(f.getNumeroFactura()) : "-");
                table.addCell(f.getFechaEmision() != null ? sdf.format(f.getFechaEmision()) : "-");
                table.addCell(f.getClienteDenominacion() != null ? f.getClienteDenominacion() : "-");
                table.addCell(f.getCondicionIva() != null ? f.getCondicionIva() : "-");
                table.addCell("$" + f.getImporteTotal());
                table.addCell(String.valueOf(f.getCantidadItems()));
                table.addCell(f.getPuntoVentaDescripcion() != null ? f.getPuntoVentaDescripcion() : "-");
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
