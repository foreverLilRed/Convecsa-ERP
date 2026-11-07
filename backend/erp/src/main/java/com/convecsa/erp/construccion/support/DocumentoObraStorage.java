package com.convecsa.erp.construccion.support;

public interface DocumentoObraStorage {

    String guardar(Long proyectoId, String nombreOriginal, byte[] contenido);

    byte[] leer(String rutaRelativa);

    void borrar(String rutaRelativa);
}
