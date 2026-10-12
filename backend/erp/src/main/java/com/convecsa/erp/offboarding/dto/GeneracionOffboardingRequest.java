package com.convecsa.erp.offboarding.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeneracionOffboardingRequest {

    @NotEmpty
    private List<String> codigos;
}
