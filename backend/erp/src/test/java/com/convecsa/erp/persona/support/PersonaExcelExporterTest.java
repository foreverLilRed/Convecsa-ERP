package com.convecsa.erp.persona.support;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.convecsa.erp.persona.domain.TipoDocumento;
import com.convecsa.erp.persona.entity.Persona;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.Test;

class PersonaExcelExporterTest {

    @Test
    void generaUnLibroXlsxConLaHojaYLosDatosDePersona() throws Exception {
        Persona persona = new Persona();
        persona.setId(7L);
        persona.setApellidos("Pérez");
        persona.setNombres("Ana");
        persona.setTipoDocumento(TipoDocumento.DNI);
        persona.setNumeroDocumento("12345678");
        persona.setCorreo("ana@example.com");
        persona.setFechaNacimiento(LocalDate.of(1990, 2, 3));
        persona.setDireccion("Av. Lima 100");
        persona.setDepartamento("Lima");
        persona.setProvincia("Lima");
        persona.setDistrito("Miraflores");

        byte[] file = new PersonaExcelExporter().export(List.of(persona));

        Set<String> entries = new HashSet<>();
        String sheet = "";
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(file))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                entries.add(entry.getName());
                if ("xl/worksheets/sheet1.xml".equals(entry.getName())) {
                    sheet = new String(zip.readAllBytes(), StandardCharsets.UTF_8);
                }
            }
        }

        assertTrue(entries.contains("xl/workbook.xml"));
        assertTrue(entries.contains("xl/worksheets/sheet1.xml"));
        assertTrue(sheet.contains("Pérez"));
        assertTrue(sheet.contains("12345678"));
        assertTrue(sheet.contains("<t>0</t>"));
        assertTrue(sheet.contains("<row r=\"1\""));
        assertTrue(!sheet.contains("<row r=\"2\""));
    }
}
