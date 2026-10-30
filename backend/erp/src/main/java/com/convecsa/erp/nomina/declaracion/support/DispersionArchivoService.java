package com.convecsa.erp.nomina.declaracion.support;

import com.convecsa.erp.construccion.entity.BoletaCc;
import com.convecsa.erp.nomina.declaracion.domain.BancoDispersion;
import com.convecsa.erp.nomina.declaracion.entity.ConsolidadoCc;
import com.convecsa.erp.nomina.entity.BoletaPago;
import com.convecsa.erp.persona.domain.Banco;
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
        return generar(obreros, administrativos, BancoDispersion.CCI);
    }

    public String generar(
            List<ConsolidadoCc> obreros,
            List<BoletaPago> administrativos,
            BancoDispersion banco) {
        List<Fila> filas = new ArrayList<>();
        for (ConsolidadoCc fila : obreros) {
            filas.add(new Fila(fila.getPersona(), PlameFormato.n(fila.getNeto())));
        }
        for (BoletaPago boleta : administrativos) {
            filas.add(new Fila(boleta.getPersona(), PlameFormato.n(boleta.getNeto())));
        }
        return emitir(filas, banco == null ? BancoDispersion.CCI : banco);
    }

    public String generarBoletas(List<BoletaCc> boletas, BancoDispersion banco) {
        List<Fila> filas = new ArrayList<>();
        for (BoletaCc boleta : boletas) {
            filas.add(new Fila(boleta.getPersona(), PlameFormato.n(boleta.getNeto())));
        }
        return emitir(filas, banco == null ? BancoDispersion.CCI : banco);
    }

    private String emitir(List<Fila> filas, BancoDispersion formato) {
        filas.sort(Comparator
                .comparing((Fila f) -> PlameFormato.limpio(f.persona.getApellidos()))
                .thenComparing(f -> PlameFormato.limpio(f.persona.getNombres())));
        List<String> lineas = new ArrayList<>();
        if (formato == BancoDispersion.CCI) {
            lineas.add("documento|nombre|banco|cci|neto");
        }
        for (Fila fila : filas) {
            PersonaCuentaBancaria cuenta = cuentaDe(fila.persona, formato);
            if (cuenta == null) {
                continue;
            }
            String linea = switch (formato) {
                case CCI -> lineaCci(fila, cuenta);
                case BCP -> lineaBcp(fila, cuenta);
                case INTERBANK -> lineaInterbank(fila, cuenta);
            };
            if (linea != null) {
                lineas.add(linea);
            }
        }
        return String.join("\r\n", lineas) + "\r\n";
    }

    private static String lineaCci(Fila fila, PersonaCuentaBancaria cuenta) {
        String cci = PlameFormato.limpio(cuenta.getCci());
        if (cci.isBlank()) {
            return null;
        }
        return String.join("|",
                PlameFormato.limpio(fila.persona.getNumeroDocumento()),
                nombre(fila.persona),
                cuenta.getBanco() == null ? "" : cuenta.getBanco().name(),
                cci,
                PlameFormato.monto(fila.neto));
    }

    private static String lineaBcp(Fila fila, PersonaCuentaBancaria cuenta) {
        String cuentaNro = PlameFormato.limpio(cuenta.getNumeroCuenta());
        String cci = PlameFormato.limpio(cuenta.getCci());
        if (cuentaNro.isBlank() && cci.isBlank()) {
            return null;
        }
        String tipoAbono = cci.isBlank() ? "C" : "I";
        String destino = tipoAbono.equals("I") ? cci : cuentaNro;
        return String.join("|",
                "2",
                "01",
                PlameFormato.limpio(fila.persona.getNumeroDocumento()),
                recortar(nombre(fila.persona), 40),
                tipoAbono,
                destino,
                PlameFormato.monto(fila.neto),
                "PEN",
                "HABERES RCC");
    }

    private static String lineaInterbank(Fila fila, PersonaCuentaBancaria cuenta) {
        String cuentaNro = PlameFormato.limpio(cuenta.getNumeroCuenta());
        if (cuentaNro.isBlank()) {
            cuentaNro = PlameFormato.limpio(cuenta.getCci());
        }
        if (cuentaNro.isBlank()) {
            return null;
        }
        return String.join("|",
                "01",
                PlameFormato.limpio(fila.persona.getNumeroDocumento()),
                recortar(nombre(fila.persona), 40),
                "CC",
                cuentaNro,
                PlameFormato.monto(fila.neto),
                "HABERES RCC");
    }

    private static PersonaCuentaBancaria cuentaDe(Persona persona, BancoDispersion formato) {
        if (persona == null || persona.getCuentasBancarias() == null || persona.getCuentasBancarias().isEmpty()) {
            return null;
        }
        Banco requerido = switch (formato) {
            case BCP -> Banco.BCP;
            case INTERBANK -> Banco.INTERBANK;
            case CCI -> null;
        };
        return persona.getCuentasBancarias().stream()
                .filter(cuenta -> requerido == null || cuenta.getBanco() == requerido)
                .sorted(Comparator.comparing((PersonaCuentaBancaria c) -> !c.isPrincipal()))
                .findFirst()
                .orElse(null);
    }

    private static String nombre(Persona persona) {
        return (PlameFormato.limpio(persona.getApellidos()) + " " + PlameFormato.limpio(persona.getNombres())).trim();
    }

    private static String recortar(String valor, int max) {
        if (valor == null) {
            return "";
        }
        return valor.length() <= max ? valor : valor.substring(0, max);
    }

    private record Fila(Persona persona, BigDecimal neto) {}
}
