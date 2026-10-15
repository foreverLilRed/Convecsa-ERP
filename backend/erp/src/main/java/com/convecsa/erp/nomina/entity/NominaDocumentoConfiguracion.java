package com.convecsa.erp.nomina.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "nomina_documento_configuracion")
@Getter
@Setter
@NoArgsConstructor
public class NominaDocumentoConfiguracion {
    public static final long ID_UNICO = 1L;
    @Id private Long id;
    @Column(name = "razon_social", nullable = false, length = 200) private String razonSocial;
    @Column(nullable = false, length = 20) private String ruc;
    @Column(nullable = false, length = 300) private String domicilio;
    @Column(length = 200) private String representante;
    @Column(name = "logo_data_url", columnDefinition = "TEXT") private String logoDataUrl;
    @Column(name = "firma_data_url", columnDefinition = "TEXT") private String firmaDataUrl;
}
