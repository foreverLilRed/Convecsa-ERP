package com.convecsa.erp.soma.domain;

import java.util.List;

public record CausasBasicasEvento(List<String> factoresPersonales, List<String> factoresTrabajo) {

    public CausasBasicasEvento {
        factoresPersonales = limpia(factoresPersonales);
        factoresTrabajo = limpia(factoresTrabajo);
    }

    public static CausasBasicasEvento vacio() {
        return new CausasBasicasEvento(List.of(), List.of());
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
