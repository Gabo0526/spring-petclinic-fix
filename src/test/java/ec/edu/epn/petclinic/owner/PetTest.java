package ec.edu.epn.petclinic.owner;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PetTest {

    private Pet pet;

    @BeforeEach
    void setUp() {
        pet = new Pet();
    }

    // 1. Test de Propiedades Básicas (Getters/Setters)
    @Test
    void testPetProperties() {
        LocalDate birthDate = LocalDate.of(2020, 1, 1);
        PetType type = new PetType();
        type.setName("Dog");

        // Setters propios de Pet
        pet.setBirthDate(birthDate);
        pet.setType(type);

        // Setter heredado de NamedEntity
        pet.setName("Fido");

        // Asserts
        assertThat(pet.getBirthDate()).isEqualTo(birthDate);
        assertThat(pet.getType()).isEqualTo(type);
        assertThat(pet.getName()).isEqualTo("Fido");
    }

    // 2. Test de la Colección de Visitas (addVisit y getVisits)
    @Test
    void testVisitsLogic() {
        // Inicialmente vacía
        assertThat(pet.getVisits()).isEmpty();

        Visit visit1 = new Visit();
        visit1.setDescription("Vaccine");

        Visit visit2 = new Visit();
        visit2.setDescription("Checkup");

        // Agregamos visitas
        pet.addVisit(visit1);
        pet.addVisit(visit2);

        Set<Visit> visits = (Set<Visit>) pet.getVisits();

        // Verificamos tamaño y contenido
        assertThat(visits).satisfies(v -> {
            assertThat(visits).hasSize(2);
            assertThat(visits).contains(visit1, visit2);
        });

    }

    // 3. Test de Herencia BaseEntity (isNew)
    @Test
    void testBaseEntityInheritance() {
        // Caso A: ID es null -> isNew() debe ser true
        assertThat(pet.getId()).isNull();
        assertThat(pet.isNew()).isTrue();

        // Caso B: ID tiene valor -> isNew() debe ser false
        pet.setId(123);
        assertThat(pet.getId()).isEqualTo(123);
        assertThat(pet.isNew()).isFalse();
    }

    // 4. Test Crítico: NamedEntity.toString()
    // Este test cubre las RAMAS del operador ternario en NamedEntity:
    @Test
    void testToStringBranches() {
        // Rama 1: Name es null
        pet.setName(null);
        assertThat(pet.toString()).hasToString("<null>");

        // Rama 2: Name tiene valor
        pet.setName("Buddy");
        assertThat(pet.toString()).hasToString("Buddy");
    }
}