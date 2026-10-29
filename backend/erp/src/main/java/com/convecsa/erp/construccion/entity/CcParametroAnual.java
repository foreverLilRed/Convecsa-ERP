package com.convecsa.erp.construccion.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cc_parametro_anual")
@Getter
@Setter
@NoArgsConstructor
public class CcParametroAnual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private int anio;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal uit;

    @Column(name = "horas_dia", nullable = false, precision = 5, scale = 2)
    private BigDecimal horasDia;

    @Column(name = "jornada_lv", nullable = false, precision = 5, scale = 2)
    private BigDecimal jornadaLv;

    @Column(name = "jornada_sabado", nullable = false, precision = 5, scale = 2)
    private BigDecimal jornadaSabado;

    @Column(name = "movilidad_dia", nullable = false, precision = 12, scale = 2)
    private BigDecimal movilidadDia;

    @Column(name = "movilidad_feriado", nullable = false, precision = 12, scale = 2)
    private BigDecimal movilidadFeriado;

    @Column(nullable = false, precision = 8, scale = 4)
    private BigDecimal conafovicer;

    @Column(name = "fondo_capacitacion", nullable = false, precision = 8, scale = 4)
    private BigDecimal fondoCapacitacion;

    @Column(nullable = false, precision = 8, scale = 4)
    private BigDecimal essalud;

    @Column(nullable = false, precision = 8, scale = 4)
    private BigDecimal vacaciones;

    @Column(nullable = false, precision = 8, scale = 4)
    private BigDecimal cts;

    @Column(name = "he60_factor", nullable = false, precision = 8, scale = 4)
    private BigDecimal he60Factor;

    @Column(name = "he100_factor", nullable = false, precision = 8, scale = 4)
    private BigDecimal he100Factor;

    @Column(name = "altura_tramo", nullable = false, precision = 8, scale = 4)
    private BigDecimal alturaTramo;

    @Column(name = "altitud_dia", nullable = false, precision = 12, scale = 2)
    private BigDecimal altitudDia;

    @Column(name = "cota_cero_dia", nullable = false, precision = 12, scale = 2)
    private BigDecimal cotaCeroDia;

    @Column(name = "altas_temp_dia", nullable = false, precision = 12, scale = 2)
    private BigDecimal altasTempDia;

    @Column(nullable = false, precision = 8, scale = 4)
    private BigDecimal nocturno;

    @Column(name = "contacto_agua", nullable = false, precision = 8, scale = 4)
    private BigDecimal contactoAgua;

    @Column(name = "aguas_servidas", nullable = false, precision = 8, scale = 4)
    private BigDecimal aguasServidas;

    @Column(name = "bonif_ley_29351", nullable = false, precision = 8, scale = 4)
    private BigDecimal bonifLey29351;

    @Column(name = "grati_jornales", nullable = false)
    private int gratiJornales;

    @Column(name = "grati_meses_fp", nullable = false)
    private int gratiMesesFp;

    @Column(name = "grati_meses_nav", nullable = false)
    private int gratiMesesNav;

    @Column(name = "escolaridad_jornales", nullable = false)
    private int escolaridadJornales;

    @OneToMany(mappedBy = "parametro", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CcCategoriaJornal> categorias = new ArrayList<>();

    @OneToMany(mappedBy = "parametro", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CcEspecialidadBae> especialidades = new ArrayList<>();
}
