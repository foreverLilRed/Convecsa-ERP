package com.convecsa.erp.nomina.declaracion.support;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.convecsa.erp.nomina.declaracion.entity.ConsolidadoCc;
import com.convecsa.erp.persona.domain.Banco;
import com.convecsa.erp.persona.domain.TipoDocumento;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.entity.PersonaCuentaBancaria;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class DispersionArchivoServiceTest {

    private final DispersionArchivoService service = new DispersionArchivoService();

    @Test
    void emiteTxtGenericoConCci() {
        Persona persona = new Persona();
        persona.setApellidos("QUISPE HUAMAN");
        persona.setNombres("JUAN");
        persona.setTipoDocumento(TipoDocumento.DNI);
        persona.setNumeroDocumento("71118318");
        PersonaCuentaBancaria cuenta = new PersonaCuentaBancaria();
        cuenta.setBanco(Banco.BCP);
        cuenta.setCci("00211100123456789012");
        cuenta.setNumeroCuenta("123456");
        cuenta.setPrincipal(true);
        persona.addCuentaBancaria(cuenta);

        ConsolidadoCc fila = new ConsolidadoCc();
        fila.setPersona(persona);
        fila.setNeto(new BigDecimal("5724.46"));

        String txt = service.generar(List.of(fila), List.of());
        assertTrue(txt.startsWith("documento|nombre|banco|cci|neto"));
        assertTrue(txt.contains("71118318|QUISPE HUAMAN JUAN|BCP|00211100123456789012|5724.46"));
    }
}
