package com.convecsa.erp.soma.service;

import com.convecsa.erp.nomina.declaracion.dto.ArchivoDescargaDTO;

public interface InterfazSomaSunafilService {

    ArchivoDescargaDTO exportar(int formato, Long proyectoId, Integer anio, Integer mes, String tipo);
}
