package com.tpgrupal.desarrollo_de_software.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.tpgrupal.desarrollo_de_software.dto.FacturaReporteDTO;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.List;

@Service
public class ReporteService {
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
