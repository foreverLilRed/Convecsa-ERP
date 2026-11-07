package com.convecsa.erp.construccion.service;

import com.convecsa.erp.construccion.domain.TipoDocumentoObra;
import com.convecsa.erp.construccion.dto.AlertaObraDTO;
import com.convecsa.erp.construccion.dto.DocumentoObraDTO;
import com.convecsa.erp.nomina.declaracion.dto.ArchivoDescargaDTO;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface InterfazTransversalObraService {

    List<DocumentoObraDTO> listarDocumentos(Long proyectoId);

    DocumentoObraDTO subirDocumento(Long proyectoId, TipoDocumentoObra tipo, MultipartFile archivo, String observaciones);

    ArchivoDescargaDTO descargarDocumento(Long proyectoId, Long documentoId);

    void eliminarDocumento(Long proyectoId, Long documentoId);

    List<AlertaObraDTO> listarAlertas(Long proyectoId);
}
