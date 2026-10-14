package com.convecsa.erp.persona.support;

import com.convecsa.erp.persona.domain.TipoDocumento;
import com.convecsa.erp.persona.entity.Persona;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.stereotype.Component;

@Component
public class PersonaExcelExporter {

    private static final int COLUMN_COUNT = 5;

    public byte[] export(List<Persona> personas) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
                ZipOutputStream zip = new ZipOutputStream(output, StandardCharsets.UTF_8)) {
            write(zip, "[Content_Types].xml", contentTypes());
            write(zip, "_rels/.rels", rootRelationships());
            write(zip, "xl/workbook.xml", workbook());
            write(zip, "xl/_rels/workbook.xml.rels", workbookRelationships());
            write(zip, "xl/styles.xml", styles());
            write(zip, "xl/worksheets/sheet1.xml", worksheet(personas));
            zip.finish();
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo generar el archivo Excel de personas", exception);
        }
    }

    private static String worksheet(List<Persona> personas) {
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        xml.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">");
        xml.append("<sheetViews><sheetView workbookViewId=\"0\"/></sheetViews><cols>");
        for (int column = 1; column <= COLUMN_COUNT; column++) {
            xml.append("<col min=\"").append(column).append("\" max=\"").append(column)
                    .append("\" width=\"").append(columnWidth(column)).append("\" customWidth=\"1\"/>");
        }
        xml.append("</cols><sheetData>");
        int row = 1;
        for (Persona persona : personas) {
            String[] apellidos = partirApellidos(persona.getApellidos());
            appendRow(xml, row++, List.of(
                    tipoDocumentoAfpnet(persona.getTipoDocumento()),
                    text(persona.getNumeroDocumento()),
                    apellidos[0],
                    apellidos[1],
                    text(persona.getNombres())));
        }
        xml.append("</sheetData></worksheet>");
        return xml.toString();
    }

    private static String[] partirApellidos(String apellidos) {
        String[] partes = text(apellidos).trim().split("\\s+", 2);
        return new String[] {partes.length == 0 ? "" : partes[0], partes.length < 2 ? "" : partes[1]};
    }

    private static String tipoDocumentoAfpnet(TipoDocumento tipoDocumento) {
        if (tipoDocumento == TipoDocumento.DNI) {
            return "0";
        }
        if (tipoDocumento == TipoDocumento.CE) {
            return "1";
        }
        return "";
    }

    private static int columnWidth(int column) {
        return switch (column) {
            case 1 -> 12;
            case 2 -> 18;
            case 3, 4, 5 -> 22;
            default -> 20;
        };
    }

    private static void appendRow(StringBuilder xml, int row, List<String> values) {
        xml.append("<row r=\"").append(row).append("\">");
        for (int index = 0; index < values.size(); index++) {
            xml.append("<c r=\"").append(columnName(index + 1)).append(row).append("\" t=\"inlineStr\"");
            xml.append("><is><t>").append(escape(values.get(index))).append("</t></is></c>");
        }
        xml.append("</row>");
    }

    private static String columnName(int number) {
        StringBuilder result = new StringBuilder();
        while (number > 0) {
            number--;
            result.insert(0, (char) ('A' + number % 26));
            number /= 26;
        }
        return result.toString();
    }

    private static String text(Object value) {
        return value == null ? "" : value.toString();
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static void write(ZipOutputStream zip, String name, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static String contentTypes() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
                + "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
                + "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/></Types>";
    }

    private static String rootRelationships() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/></Relationships>";
    }

    private static String workbook() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
                + "<sheets><sheet name=\"AFP_NET\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>";
    }

    private static String workbookRelationships() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>"
                + "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/></Relationships>";
    }

    private static String styles() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                + "<fonts count=\"1\"><font><sz val=\"11\"/><name val=\"Calibri\"/></font></fonts>"
                + "<fills count=\"2\"><fill><patternFill patternType=\"none\"/></fill><fill><patternFill patternType=\"gray125\"/></fill></fills>"
                + "<borders count=\"1\"><border/></borders><cellStyleXfs count=\"1\"><xf/></cellStyleXfs>"
                + "<cellXfs count=\"1\"><xf xfId=\"0\"/></cellXfs></styleSheet>";
    }
}
