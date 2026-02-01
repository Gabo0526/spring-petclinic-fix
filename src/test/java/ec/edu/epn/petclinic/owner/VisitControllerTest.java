package ec.edu.epn.petclinic.owner;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;

import java.time.LocalDate;
import java.util.Optional;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(VisitController.class)
class VisitControllerTest {

    private static final int TEST_OWNER_ID = 1;
    private static final int TEST_PET_ID = 1;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OwnerRepository owners;

    @BeforeEach
    void setup() {
        Owner owner = new Owner();
        owner.setId(TEST_OWNER_ID);
        owner.setFirstName("David");
        owner.setLastName("Valencia");

        Pet pet = new Pet();
        pet.setName("Fido");
        pet.setBirthDate(LocalDate.now());

        // --- CORRECCIÓN CRÍTICA ---
        // 1. Primero agregamos la mascota al dueño (mientras su ID es null / isNew() es true)
        owner.addPet(pet);

        // 2. Luego asignamos el ID para simular que ya existe en base de datos
        pet.setId(TEST_PET_ID);

        // Mock principal
        given(owners.findById(TEST_OWNER_ID)).willReturn(Optional.of(owner));
    }

    // -------------------------------------------------------------------
    // 1. Init Form (GET)
    // -------------------------------------------------------------------

    @Test
    void testInitNewVisitForm() throws Exception {
        mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("pets/createOrUpdateVisitForm"))
                .andExpect(model().attributeExists("visit"))
                .andExpect(model().attributeExists("pet"))
                .andExpect(model().attributeExists("owner"));
    }

    // -------------------------------------------------------------------
    // 2. Process Form (POST)
    // -------------------------------------------------------------------

    @Test
    void testProcessNewVisitFormSuccess() throws Exception {
        mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
                        .param("date", "2024-01-01")
                        .param("description", "Rabies Shot"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/owners/{ownerId}"))
                .andExpect(flash().attribute("message", "Your visit has been booked"));

        verify(owners).save(any(Owner.class));
    }

    @Test
    void testProcessNewVisitFormHasErrors() throws Exception {
        // Descripción vacía -> Error de validación
        mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
                        .param("date", "2024-01-01")
                        .param("description", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("pets/createOrUpdateVisitForm"))
                .andExpect(model().attributeHasErrors("visit"))
                .andExpect(model().attributeHasFieldErrors("visit", "description"));
    }

    // -------------------------------------------------------------------
    // 3. Exceptions & Edge Cases
    // -------------------------------------------------------------------

    @Test
    void testLoadPetWithVisitOwnerNotFound() {
        given(owners.findById(999)).willReturn(Optional.empty());

        Exception exception = assertThrows(ServletException.class, () ->
                mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/visits/new", 999, TEST_PET_ID))
        );

        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
    }

    @Test
    void testLoadPetWithVisitPetNotFound() {
        // Pedimos una mascota (ID 999) que el dueño (ID 1) no tiene en su lista.
        int nonExistentPetId = 999;

        Exception exception = assertThrows(ServletException.class, () ->
                mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, nonExistentPetId))
        );

        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
    }
}