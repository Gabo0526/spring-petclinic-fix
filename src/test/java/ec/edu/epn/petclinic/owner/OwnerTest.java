package ec.edu.epn.petclinic.owner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OwnerTest {

    private Owner owner;

    @BeforeEach
    void setUp() {
        owner = new Owner();
        owner.setId(1);
        owner.setFirstName("Gabo");
        owner.setLastName("Vasconez");
        owner.setAddress("Mitad del Mundo");
        owner.setCity("Quito");
        owner.setTelephone("0964023909");
    }

    // 1. Test para Getters y Setters simples
    @Test
    void testOwnerProperties() {
        assertThat(owner.getAddress()).isEqualTo("Mitad del Mundo");
        assertThat(owner.getCity()).isEqualTo("Quito");
        assertThat(owner.getTelephone()).isEqualTo("0964023909");
        assertThat(owner.getFirstName()).isEqualTo("Gabo");
        assertThat(owner.getLastName()).isEqualTo("Vasconez");

        owner.setAddress("New Address");
        owner.setCity("New City");
        owner.setTelephone("0999999999");

        assertThat(owner.getAddress()).isEqualTo("New Address");
        assertThat(owner.getCity()).isEqualTo("New City");
        assertThat(owner.getTelephone()).isEqualTo("0999999999");
    }

    // 2. Test para addPet - Caso Éxito
    @Test
    void testAddPetSuccess() {
        Pet pet = new Pet();
        pet.setName("Fido");
        owner.addPet(pet);

        assertThat(owner.getPets()).hasSize(1);
        assertThat(owner.getPets().get(0)).isEqualTo(pet);
    }

    // 3. Test para addPet - Caso Fallido
    @Test
    void testAddPetIgnoresExistingPets() {
        Pet pet = new Pet();
        pet.setName("Max");
        pet.setId(10);
        owner.addPet(pet);
        assertThat(owner.getPets()).isEmpty();
    }

    // 4. Test para getPet(String name)
    @Test
    void testGetPetByNameSimple() {
        Pet pet = new Pet();
        pet.setName("Bella");
        owner.addPet(pet);

        assertThat(owner.getPet("Bella")).isEqualTo(pet);
        assertNull(owner.getPet("Unknown"));
    }

    // 5. Test complejo: getPet(String name, boolean ignoreNew)
    @Test
    void testGetPetByNameWithIgnoreNewLogic() {
        Pet newPet = new Pet();
        newPet.setName("NewGuy");
        owner.addPet(newPet);

        Pet savedPet = new Pet();
        savedPet.setName("SavedGuy");
        owner.addPet(savedPet);
        savedPet.setId(5);

        // Caso A: Buscar existente, ignorando nuevas -> Encuentra
        assertThat(owner.getPet("SavedGuy", true)).isEqualTo(savedPet);

        // Caso B: Buscar nueva, ignorando nuevas -> Null
        assertNull(owner.getPet("NewGuy", true));

        // Caso C: Buscar nueva, NO ignorando nuevas -> Encuentra
        assertThat(owner.getPet("NewGuy", false)).isEqualTo(newPet);

        // Caso D: Nombre no coincide
        assertNull(owner.getPet("Ghost", false));
    }

    // 6. ¡NUEVO! Test para cubrir la rama "compName != null"
    // Este test es el que faltaba para el 100% de cobertura
    @Test
    void testGetPetWithNullNameInList() {
        Pet petWithNullName = new Pet();
        petWithNullName.setName(null); // Explícitamente null
        owner.addPet(petWithNullName); // Se agrega porque isNew() es true

        // Al buscar, el bucle encontrará la mascota con nombre null.
        // La condición `compName != null` será false y pasará a la siguiente.
        // Si no falla y retorna null (porque no encuentra "Fido"), la rama está cubierta.
        assertNull(owner.getPet("Fido"));

        // Verificamos que la mascota sí estaba en la lista
        assertThat(owner.getPets()).hasSize(1);
    }

    // 7. Test para getPet(Integer id)
    @Test
    void testGetPetById() {
        Pet newPet = new Pet();
        newPet.setName("New");
        Pet savedPet = new Pet();
        savedPet.setName("Saved");

        owner.addPet(newPet);
        owner.addPet(savedPet);
        savedPet.setId(100);

        assertThat(owner.getPet(100)).isEqualTo(savedPet);
        assertNull(owner.getPet(999));
    }

    // 8. Test para addVisit
    @Test
    void testAddVisit() {
        Pet pet = new Pet();
        pet.setName("Fido");
        owner.addPet(pet);
        pet.setId(1);

        Visit visit = new Visit();
        visit.setDescription("Vaccination");

        owner.addVisit(1, visit);
        assertThat(pet.getVisits()).hasSize(1);
        assertThat(pet.getVisits().iterator().next().getDescription()).isEqualTo("Vaccination");
    }

    // 9. Test para addVisit - Exceptions
    @Test
    void testAddVisitExceptions() {
        Visit visit = new Visit();
        assertThrows(IllegalArgumentException.class, () -> owner.addVisit(null, visit));
        assertThrows(IllegalArgumentException.class, () -> owner.addVisit(1, null));
        assertThrows(IllegalArgumentException.class, () -> owner.addVisit(999, visit));
    }

    // 10. Test toString
    @Test
    void testToString() {
        String result = owner.toString();
        assertThat(result).contains("Gabo", "Vasconez", "Quito", "0964023909");
    }
}