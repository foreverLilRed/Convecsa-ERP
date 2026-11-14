package com.convecsa.erp.soma.domain;

import java.util.List;
import java.util.Map;

public final class ChecklistPreUso {

    private static final Map<String, String> ETIQUETAS = Map.of(
            "frenos", "Frenos",
            "luces", "Luces",
            "alarmaRetroceso", "Alarma de retroceso",
            "extintor", "Extintor",
            "cinturon", "Cinturón",
            "sistemaHidraulico", "Sistema hidráulico",
            "guardasSeguridad", "Guardas de seguridad");

    private ChecklistPreUso() {}

    public static List<String> items() {
        return List.of(
                "frenos",
                "luces",
                "alarmaRetroceso",
                "extintor",
                "cinturon",
                "sistemaHidraulico",
                "guardasSeguridad");
    }

    public static String etiqueta(String clave) {
        if (clave == null) {
            return null;
        }
        return ETIQUETAS.getOrDefault(clave, clave);
    }
}
