package com.convecsa.erp.construccion.service;

import com.convecsa.erp.construccion.dto.AsientosObraDTO;
import com.convecsa.erp.construccion.dto.RentabilidadObraDTO;
import java.time.LocalDate;

public interface InterfazRentabilidadObraService {

    RentabilidadObraDTO rentabilidad(Long proyectoId, LocalDate desde, LocalDate hasta);

    AsientosObraDTO asientos(Long proyectoId, LocalDate desde, LocalDate hasta);
}
