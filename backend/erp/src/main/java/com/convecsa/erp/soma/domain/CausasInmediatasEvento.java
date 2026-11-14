package com.convecsa.erp.soma.domain;

import java.util.List;

public record CausasInmediatasEvento(List<String> actos, List<String> condiciones) {

    public CausasInmediatasEvento {
        actos = limpia(actos);
        condiciones = limpia(condiciones);
    }

    public static CausasInmediatasEvento vacio() {
        return new CausasInmediatasEvento(List.of(), List.of());
    }

    private static List<String> limpia(List<String> valores) {
        if (valores == null || valores.isEmpty()) {
            return List.of();
        }
        return valores.stream()
                .map(valor -> valor == null ? null : valor.trim())
                .filter(valor -> valor != null && !valor.isEmpty())
                .toList();
    }
}
