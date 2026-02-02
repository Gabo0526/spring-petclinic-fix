package ec.edu.epn.petclinic.vet;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VetsTest {

    private Vets vets;

    @BeforeEach
    void setUp() {
        vets = new Vets();
    }

    // 1. Test de Lazy Initialization (Rama: if vetList == null)
    @Test
    void testGetVetListInitializesWhenNull() {
        // Al instanciar Vets, la lista interna es null.
        // Al llamar al getter, debe entrar al if y crear el ArrayList.
        assertThat(vets.getVetList()).isNotNull();
        assertThat(vets.getVetList()).isEmpty();
    }

    // 2. Test de Persistencia de Estado (Rama: if vetList != null)
    @Test
    void testGetVetListReturnsExistingList() {
        // Primera llamada: Inicializa la lista (Lazy Init)
        vets.getVetList().add(new Vet());

        // Segunda llamada: La lista NO es null, debe retornar la misma instancia con el dato
        assertThat(vets.getVetList()).hasSize(1);

        // Verificamos que sea consistente
        Vet vet = new Vet();
        vet.setFirstName("James");
        vets.getVetList().add(vet);

        assertThat(vets.getVetList()).hasSize(2);
        assertThat(vets.getVetList().get(1).getFirstName()).isEqualTo("James");
    }
}