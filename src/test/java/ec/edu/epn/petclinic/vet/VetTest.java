package ec.edu.epn.petclinic.vet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VetTest {

    private Vet vet;

    @BeforeEach
    void setUp() {
        vet = new Vet();
    }

    // 1. Test de Herencia (Person y BaseEntity)
    @Test
    void testVetInheritanceProperties() {
        vet.setId(1);
        vet.setFirstName("James");
        vet.setLastName("Herriot");

        assertThat(vet.getId()).isEqualTo(1);
        assertThat(vet.getFirstName()).isEqualTo("James");
        assertThat(vet.getLastName()).isEqualTo("Herriot");
        assertThat(vet.isNew()).isFalse();
    }

    // 2. Test de Inicialización Lazy (getSpecialtiesInternal)
    @Test
    void testInternalInitialization() {
        // Al inicio es null internamente, getNrOfSpecialties debe inicializarlo y retornar 0
        assertThat(vet.getNrOfSpecialties()).isZero();

        // Verificamos que la lista pública no sea null pero esté vacía
        assertThat(vet.getSpecialties()).isEmpty();
    }

    // 3. Test de addSpecialty y Ordenamiento
    @Test
    void testAddSpecialtyAndSorting() {
        Specialty s1 = new Specialty();
        s1.setName("radiology");

        Specialty s2 = new Specialty();
        s2.setName("surgery");

        Specialty s3 = new Specialty();
        s3.setName("dentistry");

        // Agregamos en desorden
        vet.addSpecialty(s2);
        vet.addSpecialty(s1);
        vet.addSpecialty(s3);

        // Verificamos tamaño
        assertThat(vet.getNrOfSpecialties()).isEqualTo(3);

        // Verificamos orden alfabético (NamedEntity::getName)
        List<Specialty> sorted = vet.getSpecialties();
        assertThat(sorted).hasSize(3);
        assertThat(sorted.get(0).getName()).isEqualTo("dentistry");
        assertThat(sorted.get(1).getName()).isEqualTo("radiology");
        assertThat(sorted.get(2).getName()).isEqualTo("surgery");
    }

    // 4. Test de Inmutabilidad (.toList() de Java 16+)
    @Test
    void testGetSpecialtiesReturnsUnmodifiableList() {
        Specialty s1 = new Specialty();
        s1.setName("radiology");
        vet.addSpecialty(s1);

        List<Specialty> specs = vet.getSpecialties();

        // Intentar modificar la lista retornada debe lanzar excepción
        Specialty newSpecialty = new Specialty();
        assertThrows(UnsupportedOperationException.class, () -> specs.add(newSpecialty));
    }
}