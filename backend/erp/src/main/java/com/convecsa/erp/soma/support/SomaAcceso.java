package com.convecsa.erp.soma.support;

import com.convecsa.erp.construccion.support.AccesoObraService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SomaAcceso {

    private final AccesoObraService accesoObraService;

    public void exigirSomaCorporativo() {
        accesoObraService.exigirSoma();
    }
}
