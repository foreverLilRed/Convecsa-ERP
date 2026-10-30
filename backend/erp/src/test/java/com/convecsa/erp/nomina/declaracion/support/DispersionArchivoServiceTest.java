package com.convecsa.erp.nomina.declaracion.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.convecsa.erp.construccion.entity.BoletaCc;
import com.convecsa.erp.nomina.declaracion.domain.BancoDispersion;
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
        String txt = service.generar(List.of(fila(personaBcp())), List.of());
        assertTrue(txt.startsWith("documento|nombre|banco|cci|neto"));
        assertTrue(txt.contains("71118318|QUISPE HUAMAN JUAN|BCP|00211100123456789012|5724.46"));
    }

    @Test
    void layoutBcpUsaCuentaOCciYOmiteOtrosBancos() {
        Persona bcp = personaBcp();
        Persona interbank = personaInterbank();
        String txt = service.generar(List.of(fila(bcp), fila(interbank)), List.of(), BancoDispersion.BCP);
        String esperada = "2|01|71118318|QUISPE HUAMAN JUAN|I|00211100123456789012|5724.46|PEN|HABERES RCC";
        assertTrue(txt.contains(esperada));
        assertEquals(9, esperada.split("\\|", -1).length);
        assertFalse(txt.contains("72220022"));
        assertFalse(txt.contains("documento|nombre"));
    }

    @Test
    void layoutInterbankUsaCuentaDelBancoYOmiteBcp() {
        String txt = service.generarBoletas(
                List.of(boleta(personaInterbank()), boleta(personaBcp())),
                BancoDispersion.INTERBANK);
        String esperada = "01|72220022|RAMOS DIAZ ANA|CC|00330011223344|5724.46|HABERES RCC";
        assertTrue(txt.contains(esperada));
        assertEquals(7, esperada.split("\\|", -1).length);
        assertFalse(txt.contains("71118318"));
    }

    private static Persona personaBcp() {
        return persona("QUISPE HUAMAN", "JUAN", "71118318", Banco.BCP, "123456", "00211100123456789012");
    }

    private static Persona personaInterbank() {
        return persona("RAMOS DIAZ", "ANA", "72220022", Banco.INTERBANK, "00330011223344", "00330011223344556677");
    }

    private static Persona persona(
            String apellidos, String nombres, String dni, Banco banco, String cuentaNro, String cci) {
        Persona persona = new Persona();
        persona.setApellidos(apellidos);
        persona.setNombres(nombres);
        persona.setTipoDocumento(TipoDocumento.DNI);
        persona.setNumeroDocumento(dni);
        PersonaCuentaBancaria cuenta = new PersonaCuentaBancaria();
        cuenta.setBanco(banco);
        cuenta.setNumeroCuenta(cuentaNro);
        cuenta.setCci(cci);
        cuenta.setPrincipal(true);
        persona.addCuentaBancaria(cuenta);
        return persona;
    }

    private static ConsolidadoCc fila(Persona persona) {
        ConsolidadoCc fila = new ConsolidadoCc();
        fila.setPersona(persona);
        fila.setNeto(new BigDecimal("5724.46"));
        return fila;
    }

    private static BoletaCc boleta(Persona persona) {
        BoletaCc boleta = new BoletaCc();
        boleta.setPersona(persona);
        boleta.setNeto(new BigDecimal("5724.46"));
        return boleta;
    }
}
