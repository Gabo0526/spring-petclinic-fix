package ec.edu.epn.petclinic.owner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PetTypeFormatterTest {

    @Mock
    private PetTypeRepository pets;

    private PetTypeFormatter formatter;

    @BeforeEach
    void setup() {
        // Inyectamos el mock del repositorio en el formateador
        formatter = new PetTypeFormatter(pets);
    }

    // 1. Test de impresión (print) - Camino Feliz
    @Test
    void testPrint() {
        PetType petType = new PetType();
        petType.setName("Hamster");

        String result = formatter.print(petType, Locale.ENGLISH);

        assertThat(result).isEqualTo("Hamster");
    }

    // 2. Test de impresión (print) - Rama Null
    // Cubre la rama "else" del operador ternario: (name != null) ? name : "<null>"
    @Test
    void testPrintWithNullName() {
        PetType petType = new PetType();
        petType.setName(null); // Forzamos el null

        String result = formatter.print(petType, Locale.ENGLISH);

        assertThat(result).isEqualTo("<null>");
    }

    // 3. Test de parseo (parse) - Éxito
    @Test
    void testParse() throws ParseException {
        // Simulamos (Mock) que el repositorio devuelve una lista conocida
        given(pets.findPetTypes()).willReturn(makePetTypes());

        // Ejecutamos el parseo buscando "Bird"
        PetType petType = formatter.parse("Bird", Locale.ENGLISH);

        assertThat(petType.getName()).isEqualTo("Bird");
    }

    // 4. Test de parseo (parse) - No encontrado (Exception)
    @Test
    void testParseNotFound() {
        // Simulamos la lista
        given(pets.findPetTypes()).willReturn(makePetTypes());

        // Buscamos algo que no está en la lista ("Fish")
        ParseException exception = assertThrows(ParseException.class, () -> formatter.parse("Fish", Locale.ENGLISH));

        assertThat(exception.getMessage()).isEqualTo("type not found: Fish");
    }

    // Helper para crear datos de prueba
    private List<PetType> makePetTypes() {
        List<PetType> petTypes = new ArrayList<>();

        PetType bird = new PetType();
        bird.setName("Bird");
        petTypes.add(bird);

        PetType dog = new PetType();
        dog.setName("Dog");
        petTypes.add(dog);

        return petTypes;
    }
}