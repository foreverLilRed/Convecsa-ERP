package com.convecsa.erp.nomina.support;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.stereotype.Service;

@Service
public class BoletaPdfService {

    public byte[] convertir(String html) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html.replaceAll("@import url\\([^;]+;", ""), null);
            builder.toStream(output);
            builder.run();
            return output.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo generar el documento PDF", ex);
        }
    }

    public byte[] combinar(List<byte[]> documentos) {
        if (documentos.isEmpty()) {
            throw new IllegalArgumentException("No hay boletas para generar");
        }
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDFMergerUtility merger = new PDFMergerUtility();
            merger.setDestinationStream(output);
            for (byte[] documento : documentos) {
                merger.addSource(new ByteArrayInputStream(documento));
            }
            merger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
            return output.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo combinar el lote de boletas", ex);
        }
    }
}
