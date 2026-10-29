package com.convecsa.erp.nomina.declaracion.support;

import com.convecsa.erp.nomina.declaracion.entity.ConsolidadoCc;
import com.convecsa.erp.nomina.domain.TipoComisionAfp;
import com.convecsa.erp.nomina.entity.BoletaPago;
import com.convecsa.erp.persona.entity.Persona;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AfpnetArchivoService {

    public String generar(List<ConsolidadoCc> obreros, List<BoletaPago> administrativos) {
        List<Fila> filas = new ArrayList<>();
        for (ConsolidadoCc fila : obreros) {
            Persona persona = fila.getPersona();
            if (esOnp(persona)) {
                continue;
            }
            filas.add(new Fila(
                    persona,
                    PlameFormato.n(fila.getTotalAfecto()),
                    "C"));
        }
        for (BoletaPago boleta : administrativos) {
            if (boleta.getAfp() != null && boleta.getAfp().isEsOnp()) {
                continue;
            }
            Persona persona = boleta.getPersona();
            if (esOnp(persona)) {
                continue;
            }
            filas.add(new Fila(persona, PlameFormato.n(boleta.getTotalHaberes()), "N"));
        }
        filas.sort(Comparator
                .comparing((Fila f) -> PlameFormato.limpio(f.persona.getApellidos()))
                .thenComparing(f -> PlameFormato.limpio(f.persona.getNombres()))
                .thenComparing(f -> PlameFormato.limpio(f.persona.getNumeroDocumento())));

        List<String> lineas = new ArrayList<>();
        int secuencia = 1;
        for (Fila fila : filas) {
            Persona persona = fila.persona;
            String[] apellidos = partirApellidos(persona.getApellidos());
            lineas.add(String.join(",",
                    String.valueOf(secuencia++),
                    PlameFormato.limpio(persona.getCuspp()),
                    PlameCatalogo.tipoDocumentoAfpnet(persona.getTipoDocumento()),
                    PlameFormato.limpio(persona.getNumeroDocumento()),
                    apellidos[0],
                    apellidos[1],
                    PlameFormato.limpio(persona.getNombres()),
                    "S",
                    "N",
                    "N",
                    "N",
                    PlameFormato.monto(fila.remuneracion),
                    "0.00",
                    "0.00",
                    "0.00",
                    fila.tipoTrabajo,
                    tipoComision(persona)));
        }
        if (lineas.isEmpty()) {
            return "";
        }
        return String.join("\r\n", lineas) + "\r\n";
    }

    private static boolean esOnp(Persona persona) {
        String pension = persona.getPension();
        return pension != null && pension.trim().equalsIgnoreCase("ONP");
    }

    private static String tipoComision(Persona persona) {
        TipoComisionAfp tipo = persona.getTipoComisionAfp();
        if (tipo == TipoComisionAfp.MIXTA) {
            return "M";
        }
        if (tipo == TipoComisionAfp.FLUJO) {
            return "F";
        }
        return "";
    }

    static String[] partirApellidos(String apellidos) {
        String valor = PlameFormato.limpio(apellidos);
        if (valor.isBlank()) {
            return new String[] {"", ""};
        }
        String[] partes = valor.split("\\s+", 2);
        if (partes.length == 1) {
            return new String[] {partes[0], ""};
        }
        return partes;
    }

    private record Fila(Persona persona, BigDecimal remuneracion, String tipoTrabajo) {}
}
