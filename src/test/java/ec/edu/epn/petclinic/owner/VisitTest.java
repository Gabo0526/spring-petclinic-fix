package ec.edu.epn.petclinic.owner;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VisitTest {

    private Visit visit;

    @BeforeEach
    void setUp() {
        // Al instanciar, se ejecuta el constructor que setea la fecha a LocalDate.now()
        visit = new Visit();
    }

    @Test
    void testVisitInit() {
        // Verificamos que el constructor haya funcionado
        assertThat(visit.getDate()).isEqualTo(LocalDate.now());
        assertThat(visit.isNew()).isTrue();
    }

    @Test
    void testVisitProperties() {
        // 1. Probamos setDate explícitamente (Lo que faltaba en la imagen)
        LocalDate oldDate = LocalDate.of(2020, 1, 1);
        visit.setDate(oldDate);
        assertThat(visit.getDate()).isEqualTo(oldDate);

        // 2. Probamos Description
        visit.setDescription("Rabies Shot");
        assertThat(visit.getDescription()).isEqualTo("Rabies Shot");

        // 3. Probamos herencia de BaseEntity (ID)
        visit.setId(10);
        assertThat(visit.getId()).isEqualTo(10);
        assertThat(visit.isNew()).isFalse();
    }
}