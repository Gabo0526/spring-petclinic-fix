package ec.edu.epn.petclinic.owner;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

class PetValidatorTest {

    private PetValidator validator;
    private Pet pet;
    private Errors errors;

    @BeforeEach
    void setUp() {
        validator = new PetValidator();
        pet = new Pet();
        // Usamos la implementación real de Spring para no tener que mockear Errors
        errors = new BeanPropertyBindingResult(pet, "pet");
    }

    // 1. Test del método supports()
    @Test
    void testSupports() {
        assertThat(validator.supports(Pet.class)).isTrue();
        assertThat(validator.supports(String.class)).isFalse();
    }

    // 2. Camino Feliz: (Mascota Nueva)
    @Test
    void testValidateSuccess() {
        pet.setName("Fido");
        pet.setBirthDate(LocalDate.now());

        PetType type = new PetType();
        type.setName("Dog");
        pet.setType(type);

        // BaseEntity.isNew() es true por defecto
        assertThat(pet.isNew()).isTrue();

        validator.validate(pet, errors);

        assertThat(errors.hasErrors()).isFalse();
    }

    // 3. Validación de Nombre (Empty/Null/Blank)
    @Test
    void testValidateNameFailed() {
        // Caso: Nombre vacío
        pet.setName("");
        // Seteamos otros campos para aislar el error del nombre
        pet.setBirthDate(LocalDate.now());
        pet.setType(new PetType());

        validator.validate(pet, errors);

        assertThat(errors.hasErrors()).isTrue();
        assertThat(errors.getFieldError("name")).isNotNull();
        assertThat(Objects.requireNonNull(errors.getFieldError("name")).getCode()).isEqualTo("required");
    }

    // 4. Validación de Tipo - Rama Crítica 1 (Falla)
    // Condición: pet.isNew() && pet.getType() == null
    @Test
    void testValidateTypeFailedForNewPet() {
        pet.setName("Fido");
        pet.setBirthDate(LocalDate.now());

        // No seteamos tipo (null) y es nueva mascota
        assertThat(pet.isNew()).isTrue();
        assertThat(pet.getType()).isNull();

        validator.validate(pet, errors);

        assertThat(errors.hasErrors()).isTrue();
        assertThat(errors.getFieldError("type")).isNotNull();
    }

    // 5. Validación de Tipo - Rama Crítica 2 (Éxito por Mascota Existente)
    // Condición: !pet.isNew() (Aunque type sea null)
    // Esto cubre la parte FALSA de la primera condición del AND (Short-circuit)
    @Test
    void testValidateTypeIgnoredForExistingPet() {
        pet.setName("Fido");
        pet.setBirthDate(LocalDate.now());
        pet.setId(1); // Al setear ID, BaseEntity.isNew() es FALSE

        // Type es null, PERO como no es nueva, no debería validar el tipo
        assertThat(pet.getType()).isNull();

        validator.validate(pet, errors);

        // No debería haber error en 'type'
        assertThat(errors.getFieldError("type")).isNull();
    }

    // 6. Validación de Fecha de Nacimiento
    @Test
    void testValidateBirthDateFailed() {
        pet.setName("Fido");
        pet.setType(new PetType());
        // No seteamos fecha (null)

        validator.validate(pet, errors);

        assertThat(errors.hasErrors()).isTrue();
        assertThat(errors.getFieldError("birthDate")).isNotNull();
    }
}