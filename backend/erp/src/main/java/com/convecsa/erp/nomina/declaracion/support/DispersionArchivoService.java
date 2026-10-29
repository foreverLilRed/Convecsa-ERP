package com.convecsa.erp.nomina.declaracion.support;

import com.convecsa.erp.nomina.declaracion.entity.ConsolidadoCc;
import com.convecsa.erp.nomina.entity.BoletaPago;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.entity.PersonaCuentaBancaria;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DispersionArchivoService {

    public String generar(List<ConsolidadoCc> obreros, List<BoletaPago> administrativos) {
        List<String> lineas = new ArrayList<>();
        lineas.add("documento|nombre|banco|cci|neto");
        List<Fila> filas = new ArrayList<>();
        for (ConsolidadoCc fila : obreros) {
            filas.add(new Fila(fila.getPersona(), PlameFormato.n(fila.getNeto())));
        }
        for (BoletaPago boleta : administrativos) {
            filas.add(new Fila(boleta.getPersona(), PlameFormato.n(boleta.getNeto())));
        }
        filas.sort(Comparator
                .comparing((Fila f) -> PlameFormato.limpio(f.persona.getApellidos()))
                .thenComparing(f -> PlameFormato.limpio(f.persona.getNombres())));
        for (Fila fila : filas) {
            Persona persona = fila.persona;
            PersonaCuentaBancaria cuenta = cuentaPrincipal(persona);
            String nombre = PlameFormato.limpio(persona.getApellidos()) + " "
                    + PlameFormato.limpio(persona.getNombres());
            String banco = cuenta == null ? "" : cuenta.getBanco().name();
            String cci = cuenta == null ? "" : PlameFormato.limpio(cuenta.getCci());
            lineas.add(String.join("|",
                    PlameFormato.limpio(persona.getNumeroDocumento()),
                    nombre.trim(),
                    banco,
                    cci,
                    PlameFormato.monto(fila.neto)));
        }
        return String.join("\r\n", lineas) + "\r\n";
    }

    private static PersonaCuentaBancaria cuentaPrincipal(Persona persona) {
        if (persona.getCuentasBancarias() == null || persona.getCuentasBancarias().isEmpty()) {
            return null;
        }
        return persona.getCuentasBancarias().stream()
                .filter(PersonaCuentaBancaria::isPrincipal)
                .findFirst()
                .orElse(persona.getCuentasBancarias().get(0));
    }

    private record Fila(Persona persona, BigDecimal neto) {}
}
