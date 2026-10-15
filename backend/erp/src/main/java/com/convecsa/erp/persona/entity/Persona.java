package com.convecsa.erp.persona.entity;

import com.convecsa.erp.persona.domain.AptitudMedica;
import com.convecsa.erp.persona.domain.Genero;
import com.convecsa.erp.persona.domain.GradoInstruccion;
import com.convecsa.erp.persona.domain.TipoDocumento;
import com.convecsa.erp.nomina.domain.TipoComisionAfp;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "persona")
@Getter
@Setter
@NoArgsConstructor
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String apellidos;

    @Column(nullable = false, length = 120)
    private String nombres;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false, length = 20)
    private TipoDocumento tipoDocumento;

    @Column(name = "numero_documento", nullable = false, length = 30)
    private String numeroDocumento;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(name = "fecha_emision_dni")
    private LocalDate fechaEmisionDni;

    @Column(name = "fecha_caducidad_dni")
    private LocalDate fechaCaducidadDni;

    @Column(nullable = false, length = 180)
    private String correo;

    @Column(nullable = true, length = 120)
    private String pension;

    @Column(nullable = true, length = 30)
    private String cuspp;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_comision_afp", nullable = false, length = 20)
    private TipoComisionAfp tipoComisionAfp = TipoComisionAfp.FLUJO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Genero genero = Genero.NO_ESPECIFICA;

    @Column(name = "contacto_emergencia_nombre", length = 160)
    private String contactoEmergenciaNombre;

    @Column(name = "contacto_emergencia_telefono", length = 30)
    private String contactoEmergenciaTelefono;

    @Column(name = "contacto_emergencia_parentesco", length = 80)
    private String contactoEmergenciaParentesco;

    @Enumerated(EnumType.STRING)
    @Column(name = "grado_instruccion", nullable = false, length = 40)
    private GradoInstruccion gradoInstruccion;

    @Column(nullable = false, length = 255)
    private String direccion;

    @Column(nullable = false, length = 80)
    private String departamento;

    @Column(nullable = false, length = 80)
    private String provincia;

    @Column(nullable = false, length = 80)
    private String distrito;

    @Column(nullable = false, length = 120)
    private String residencia;

    @Column(name = "foto_url", length = 500)
    private String fotoUrl;

    @Column(name = "fecha_vigencia_emo")
    private LocalDate fechaVigenciaEmo;

    @Enumerated(EnumType.STRING)
    @Column(name = "aptitud_medica", length = 20)
    private AptitudMedica aptitudMedica;

    @Column(name = "restricciones_medicas", length = 2000)
    private String restriccionesMedicas;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "persona", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PersonaCelular> celulares = new ArrayList<>();

    @OneToMany(mappedBy = "persona", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PersonaCuentaBancaria> cuentasBancarias = new ArrayList<>();

    @OneToMany(mappedBy = "persona", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PersonaExperienciaLaboral> experienciasLaborales = new ArrayList<>();

    @OneToMany(mappedBy = "persona", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PersonaDocumentoChecklist> documentosChecklist = new ArrayList<>();

    public void addCelular(PersonaCelular celular) {
        celular.setPersona(this);
        celulares.add(celular);
    }

    public void addCuentaBancaria(PersonaCuentaBancaria cuenta) {
        cuenta.setPersona(this);
        cuentasBancarias.add(cuenta);
    }

    public void addExperienciaLaboral(PersonaExperienciaLaboral experiencia) {
        experiencia.setPersona(this);
        experienciasLaborales.add(experiencia);
    }

    public void addDocumentoChecklist(PersonaDocumentoChecklist documento) {
        documento.setPersona(this);
        documentosChecklist.add(documento);
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
