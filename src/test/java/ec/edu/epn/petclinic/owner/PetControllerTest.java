package ec.edu.epn.petclinic.owner;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PetController.class)
@Import(PetTypeFormatter.class)
class PetControllerTest {

    private static final int TEST_OWNER_ID = 1;
    private static final int TEST_PET_ID = 1;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OwnerRepository owners;

    @MockitoBean
    private PetTypeRepository petTypes;

    private Owner owner;

    @BeforeEach
    void setup() {
        PetType dogType = new PetType();
        dogType.setId(1);
        dogType.setName("Dog");

        owner = new Owner();
        owner.setId(TEST_OWNER_ID);
        owner.setFirstName("David");
        owner.setLastName("Valencia");

        Pet pet = new Pet();
        pet.setName("Fido");
        pet.setBirthDate(LocalDate.now().minusYears(1));
        pet.setType(dogType);

        // 1. Agregamos la mascota mientras isNew() es true
        owner.addPet(pet);
        // 2. Asignamos ID simulando persistencia
        pet.setId(TEST_PET_ID);

        given(owners.findById(TEST_OWNER_ID)).willReturn(Optional.of(owner));
        given(petTypes.findPetTypes()).willReturn(List.of(dogType));
    }

    // -------------------------------------------------------------------
    // 1. Init Forms
    // -------------------------------------------------------------------

    @Test
    void testInitCreationForm() throws Exception {
        mockMvc.perform(get("/owners/{ownerId}/pets/new", TEST_OWNER_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("pets/createOrUpdatePetForm"))
                .andExpect(model().attributeExists("pet"));
    }

    @Test
    void testInitUpdateForm() throws Exception {
        mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("pets/createOrUpdatePetForm"))
                .andExpect(model().attribute("pet", hasProperty("name", is("Fido"))));
    }

    // -------------------------------------------------------------------
    // 2. Process Creation Form
    // -------------------------------------------------------------------

    @Test
    void testProcessCreationFormSuccess() throws Exception {
        given(owners.save(any(Owner.class))).willAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID)
                        .param("name", "Bella")
                        .param("birthDate", "2024-01-01")
                        .param("type", "Dog"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/owners/{ownerId}"))
                .andExpect(flash().attribute("message", "New Pet has been Added"));
    }

    @Test
    void testProcessCreationFormHasErrors() throws Exception {
        mockMvc.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID)
                        .param("name", "")
                        .param("birthDate", "2024-01-01"))
                .andExpect(status().isOk())
                .andExpect(view().name("pets/createOrUpdatePetForm"))
                .andExpect(model().attributeHasErrors("pet"));
    }

    @Test
    void testProcessCreationFormDuplicateName() throws Exception {
        mockMvc.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID)
                        .param("name", "Fido")
                        .param("birthDate", "2024-01-01")
                        .param("type", "Dog"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("pet", "name"));
    }

    @Test
    void testProcessCreationFormFutureDate() throws Exception {
        LocalDate future = LocalDate.now().plusDays(1);
        mockMvc.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID)
                        .param("name", "Luna")
                        .param("birthDate", future.toString())
                        .param("type", "Dog"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("pet", "birthDate"));
    }

    @Test
    void testProcessCreationFormNullDate() throws Exception {
        // En creación (objeto nuevo), omitir el parámetro SÍ deja la fecha en null
        mockMvc.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID)
                        .param("name", "Luna")
                        .param("type", "Dog"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("pet", "birthDate"));
    }

    // -------------------------------------------------------------------
    // 3. Process Update Form
    // -------------------------------------------------------------------

    @Test
    void testProcessUpdateFormSuccess() throws Exception {
        mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID)
                        .param("name", "Fido Updated")
                        .param("birthDate", "2023-01-01")
                        .param("type", "Dog"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/owners/{ownerId}"))
                .andExpect(flash().attribute("message", "Pet details has been edited"));
    }

    @Test
    void testProcessUpdateFormDuplicateNameDifferentPet() throws Exception {
        Pet cat = new Pet();
        cat.setName("Kitty");
        owner.addPet(cat);
        cat.setId(2);

        mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, 2)
                        .param("name", "Fido")
                        .param("birthDate", "2023-01-01")
                        .param("type", "Dog"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("pet", "name"));
    }

    @Test
    void testProcessUpdateFormSameNameSamePet() throws Exception {
        mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID)
                        .param("name", "Fido")
                        .param("birthDate", "2023-01-01")
                        .param("type", "Dog"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/owners/{ownerId}"));
    }

    @Test
    void testProcessUpdateFormHasErrors() throws Exception {
        mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID)
                        .param("name", "")
                        .param("type", "Dog"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasErrors("pet"));
    }

    @Test
    void testProcessUpdateFormFutureDate() throws Exception {
        LocalDate future = LocalDate.now().plusDays(5);
        mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID)
                        .param("name", "Fido")
                        .param("birthDate", future.toString())
                        .param("type", "Dog"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("pet", "birthDate"));
    }

    @Test
    void testProcessUpdateFormNullDate() throws Exception {
        // En actualización, el objeto ya tiene fecha. Omitir el param no la borra.
        // Debemos enviar una cadena VACÍA "" para forzar el null.
        mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID)
                        .param("name", "Fido")
                        .param("birthDate", "") // <--- ESTO FUERZA EL NULL
                        .param("type", "Dog"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("pet", "birthDate"));
    }

    // -------------------------------------------------------------------
    // 4. Edge Cases
    // -------------------------------------------------------------------

    @Test
    void testFindOwnerNotFound() {
        given(owners.findById(999)).willReturn(Optional.empty());

        Exception exception = assertThrows(ServletException.class, () ->
                mockMvc.perform(get("/owners/{ownerId}/pets/new", 999))
        );

        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
    }

    // -------------------------------------------------------------------
    // 5. Coverage Boosters (Edge Cases & Malicious Inputs)
    // -------------------------------------------------------------------

    /**
     * Cubre la rama del if en processCreationForm:
     * if (... && pet.isNew() && ...)
     * Escenario: Enviamos un ID ("999") en el formulario de creación.
     * Resultado: pet.isNew() es falso, por lo que se SALTA la validación de duplicados
     * y permite crear la mascota aunque tenga nombre repetido.
     */
    @Test
    void testProcessCreationFormWithIdSkippingDuplicateCheck() throws Exception {
        mockMvc.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID)
                        .param("name", "Fido") // Nombre Duplicado (ya existe en setup)
                        .param("birthDate", "2024-01-01")
                        .param("type", "Dog")
                        .param("id", "999")) // ID malicioso para saltar isNew()
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/owners/{ownerId}"));
    }

    /**
     * Cubre el bloque 'else' en updatePetDetails:
     * else { owner.addPet(pet); }
     * Escenario: Editamos una mascota (URL válida), pero inyectamos un ID ("999")
     * en el formulario que el dueño NO tiene.
     * Spring crea una nueva instancia, le asigna ID 999, y al no encontrarla en la lista,
     * entra al 'else' y la agrega como nueva.
     */
    @Test
    void testProcessUpdateFormWithMaliciousNonExistentId() throws Exception {
        // 1. Creamos un Mock de Owner para manipular su comportamiento interno
        Owner mockOwner = mock(Owner.class);

        // 2. Preparamos la mascota que devolverá findPet (para evitar el NPE)
        Pet p = new Pet();
        p.setId(TEST_PET_ID); // ID 1 original
        p.setName("Original");

        // 3. Sobrescribimos el comportamiento del repositorio para este test
        given(owners.findById(TEST_OWNER_ID)).willReturn(Optional.of(mockOwner));

        // 4. Configuración CLAVE:
        // - Cuando findPet busque el ID 1 (URL), devuelve la mascota (evita NPE).
        given(mockOwner.getPet(TEST_PET_ID)).willReturn(p);
        // - Cuando el controlador busque el ID 999 (Inyectado), devuelve NULL.
        given(mockOwner.getPet(999)).willReturn(null);

        // 5. Ejecutamos: URL válida (1) pero Formulario malicioso (999)
        mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID)
                        .param("name", "Hacker Pet")
                        .param("birthDate", "2023-01-01")
                        .param("type", "Dog")
                        .param("id", "999")) // Inyectamos ID 999
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/owners/{ownerId}"));

        // 6. Verificamos que realmente se llamó a addPet (prueba de que entró al else)
        verify(mockOwner).addPet(p);
        // 7. Verificamos que se guardó
        verify(owners).save(mockOwner);
    }

    /**
     * Cubre la línea de Assert en updatePetDetails:
     * Assert.state(id != null, ...)
     * Escenario: Enviamos una actualización explícitamente sin ID (cadena vacía).
     * Esto fuerza a que pet.getId() sea null, disparando la excepción.
     */
    @Test
    void testProcessUpdateFormWithNullId() {
        Exception exception = assertThrows(ServletException.class, () ->
                mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID)
                        .param("name", "Unnamed")
                        .param("birthDate", "2023-01-01")
                        .param("type", "Dog")
                        .param("id", "")) // Forzamos ID nulo/vacío
        );

        // Verificamos que la causa sea IllegalStateException (lanzada por Assert.state)
        assertInstanceOf(IllegalStateException.class, exception.getCause());
    }
}