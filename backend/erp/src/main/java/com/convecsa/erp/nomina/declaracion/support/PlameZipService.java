package com.convecsa.erp.nomina.declaracion.support;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.stereotype.Service;

@Service
public class PlameZipService {

    public byte[] empaquetar(String baseNombre, String jor, String rem, String snl) {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            try (ZipOutputStream zip = new ZipOutputStream(buffer, StandardCharsets.UTF_8)) {
                escribir(zip, baseNombre + ".jor", jor);
                escribir(zip, baseNombre + ".rem", rem);
                escribir(zip, baseNombre + ".snl", snl);
            }
            return buffer.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo generar el ZIP de PLAME", ex);
        }
    }

    private static void escribir(ZipOutputStream zip, String nombre, String contenido) throws IOException {
        zip.putNextEntry(new ZipEntry(nombre));
        zip.write((contenido == null ? "" : contenido).getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }
}
