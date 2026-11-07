package com.convecsa.erp.construccion.support;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DiscoDocumentoObraStorage implements DocumentoObraStorage {

    private final Path raiz;

    public DiscoDocumentoObraStorage(@Value("${app.obras.documentos-dir:./data/obras-documentos}") String directorio) {
        this.raiz = Path.of(directorio).toAbsolutePath().normalize();
    }

    @Override
    public String guardar(Long proyectoId, String nombreOriginal, byte[] contenido) {
        String archivo = UUID.randomUUID() + "_" + sanitizar(nombreOriginal);
        String relativa = proyectoId + "/" + archivo;
        Path destino = resolver(relativa);
        try {
            Files.createDirectories(destino.getParent());
            Files.write(destino, contenido);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo guardar el documento", ex);
        }
        return relativa.replace('\\', '/');
    }

    @Override
    public byte[] leer(String rutaRelativa) {
        try {
            return Files.readAllBytes(resolver(rutaRelativa));
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo leer el documento", ex);
        }
    }

    @Override
    public void borrar(String rutaRelativa) {
        try {
            Files.deleteIfExists(resolver(rutaRelativa));
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo eliminar el documento", ex);
        }
    }

    private Path resolver(String rutaRelativa) {
        Path destino = raiz.resolve(rutaRelativa).normalize();
        if (!destino.startsWith(raiz)) {
            throw new IllegalArgumentException("Ruta de documento inválida");
        }
        return destino;
    }

    private static String sanitizar(String nombre) {
        String base = nombre == null || nombre.isBlank() ? "documento" : nombre.trim();
        String limpio = base.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (limpio.isBlank()) {
            limpio = "documento";
        }
        return limpio.length() > 80 ? limpio.substring(limpio.length() - 80) : limpio;
    }
}
