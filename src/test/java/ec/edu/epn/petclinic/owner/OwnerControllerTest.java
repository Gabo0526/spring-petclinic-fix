package ec.edu.epn.petclinic.owner;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import jakarta.servlet.ServletException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OwnerController.class)
class OwnerControllerTest {

    private static final int TEST_OWNER_ID = 1;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OwnerRepository owners;

    private Owner gabo;

    @BeforeEach
    void setup() {
        gabo = new Owner();
        gabo.setId(TEST_OWNER_ID);
        gabo.setFirstName("Gabo");
        gabo.setLastName("Vasconez");
        gabo.setAddress("Mitad del Mundo");
        gabo.setCity("Quito");
        gabo.setTelephone("0964023909");
    }

    // -------------------------------------------------------------------
    // 1. Pruebas de Creación (Create Form)
    // -------------------------------------------------------------------

    @Test
    void testInitCreationForm() throws Exception {
        mockMvc.perform(get("/owners/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("owners/createOrUpdateOwnerForm"))
                .andExpect(model().attributeExists("owner"));
    }

    @Test
    void testProcessCreationFormSuccess() throws Exception {
        given(owners.save(any(Owner.class))).willAnswer(invocation -> {
            Owner savedOwner = invocation.getArgument(0);
            savedOwner.setId(TEST_OWNER_ID);
            return savedOwner;
        });

        mockMvc.perform(post("/owners/new")
                        .param("firstName", "Gabo")
                        .param("lastName", "Vasconez")
                        .param("address", "Mitad del Mundo")
                        .param("city", "Quito")
                        .param("telephone", "0964023909"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/owners/" + TEST_OWNER_ID))
                .andExpect(flash().attribute("message", "New Owner Created"));
    }

    @Test
    void testProcessCreationFormHasErrors() throws Exception {
        // CORRECCIÓN: Eliminamos .andExpect(flash()...)
        // El controlador retorna una VISTA (200 OK), no un Redirect.
        mockMvc.perform(post("/owners/new")
                        .param("firstName", "Gabo")
                        .param("lastName", "Vasconez"))
                .andExpect(status().isOk())
                .andExpect(view().name("owners/createOrUpdateOwnerForm"))
                .andExpect(model().attributeHasErrors("owner"));
    }

    // -------------------------------------------------------------------
    // 2. Pruebas de Búsqueda (Find Form y Process Find)
    // -------------------------------------------------------------------

    @Test
    void testInitFindForm() throws Exception {
        mockMvc.perform(get("/owners/find"))
                .andExpect(status().isOk())
                .andExpect(view().name("owners/findOwners"))
                .andExpect(model().attributeExists("owner"));
    }

    @Test
    void testProcessFindFormOneOwnerFound() throws Exception {
        Page<@NotNull Owner> pageOne = new PageImpl<>(List.of(gabo));
        given(owners.findByLastNameStartingWith(eq("Vasconez"), any(Pageable.class))).willReturn(pageOne);

        mockMvc.perform(get("/owners").param("lastName", "Vasconez"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/owners/" + TEST_OWNER_ID));
    }

    @Test
    void testProcessFindFormNoOwnersFound() throws Exception {
        Page<@NotNull Owner> pageEmpty = new PageImpl<>(Collections.emptyList());
        given(owners.findByLastNameStartingWith(eq("Unknown"), any(Pageable.class))).willReturn(pageEmpty);

        mockMvc.perform(get("/owners").param("lastName", "Unknown"))
                .andExpect(status().isOk())
                .andExpect(view().name("owners/findOwners"))
                .andExpect(model().attributeHasFieldErrors("owner", "lastName"));
    }

    @Test
    void testProcessFindFormMultipleOwnersFound() throws Exception {
        Page<@NotNull Owner> pageMultiple = new PageImpl<>(List.of(gabo, new Owner()));
        given(owners.findByLastNameStartingWith(anyString(), any(Pageable.class))).willReturn(pageMultiple);

        mockMvc.perform(get("/owners"))
                .andExpect(status().isOk())
                .andExpect(view().name("owners/ownersList"))
                .andExpect(model().attribute("listOwners", hasSize(2)))
                .andExpect(model().attribute("totalPages", 1));
    }

    @Test
    void testProcessFindFormWithNullLastName() throws Exception {
        Page<@NotNull Owner> pageMultiple = new PageImpl<>(List.of(gabo, new Owner()));
        given(owners.findByLastNameStartingWith(eq(""), any(Pageable.class))).willReturn(pageMultiple);

        mockMvc.perform(get("/owners"))
                .andExpect(status().isOk())
                .andExpect(view().name("owners/ownersList"));
    }

    // -------------------------------------------------------------------
    // 3. Pruebas de Edición (Update Form)
    // -------------------------------------------------------------------

    @Test
    void testInitUpdateOwnerForm() throws Exception {
        given(owners.findById(TEST_OWNER_ID)).willReturn(Optional.of(gabo));

        mockMvc.perform(get("/owners/{ownerId}/edit", TEST_OWNER_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("owners/createOrUpdateOwnerForm"))
                .andExpect(model().attribute("owner", hasProperty("lastName", is("Vasconez"))));
    }

    @Test
    void testProcessUpdateOwnerFormSuccess() throws Exception {
        given(owners.findById(TEST_OWNER_ID)).willReturn(Optional.of(gabo));
        given(owners.save(any(Owner.class))).willAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/owners/{ownerId}/edit", TEST_OWNER_ID)
                        .param("firstName", "Gabo Updated")
                        .param("lastName", "Vasconez")
                        .param("address", "New Address 123")
                        .param("city", "Quito")
                        .param("telephone", "0964023909"))
                .andExpect(status().is3xxRedirection())
                // CORRECCIÓN: Esperamos el literal con el placeholder, no el valor expandido
                .andExpect(view().name("redirect:/owners/{ownerId}"))
                .andExpect(flash().attribute("message", "Owner Values Updated"));
    }

    @Test
    void testProcessUpdateOwnerFormHasErrors() throws Exception {
        given(owners.findById(TEST_OWNER_ID)).willReturn(Optional.of(gabo));

        // Igual que en creación: al retornar 200 OK, MockMvc pierde los atributos Flash.
        // Solo validamos que retorne al formulario y marque errores.
        mockMvc.perform(post("/owners/{ownerId}/edit", TEST_OWNER_ID)
                        .param("firstName", "")
                        .param("lastName", "Vasconez")
                        .param("city", "Quito"))
                .andExpect(status().isOk())
                .andExpect(view().name("owners/createOrUpdateOwnerForm"))
                .andExpect(model().attributeHasErrors("owner"));
    }

    @Test
    void testProcessUpdateOwnerFormIdMismatch() throws Exception {
        Owner intruder = new Owner();
        intruder.setId(999);

        // Forzamos mismatch: URL tiene 1, Repositorio devuelve 999
        given(owners.findById(TEST_OWNER_ID)).willReturn(Optional.of(intruder));

        mockMvc.perform(post("/owners/{ownerId}/edit", TEST_OWNER_ID)
                        .param("firstName", "Gabo")
                        .param("lastName", "Vasconez")
                        .param("address", "Address")
                        .param("city", "City")
                        .param("telephone", "1234567890"))
                .andExpect(status().is3xxRedirection())
                // CORRECCIÓN: El controlador retorna "redirect:/owners/{ownerId}/edit" (cadena literal)
                // NO retorna "redirect:/owners/1/edit". Ajustamos el assert al literal.
                .andExpect(view().name("redirect:/owners/{ownerId}/edit"))
                .andExpect(flash().attribute("error", "Owner ID mismatch. Please try again."));
    }

    // -------------------------------------------------------------------
    // 4. Pruebas de Visualización (Show Owner)
    // -------------------------------------------------------------------

    @Test
    void testShowOwnerSuccess() throws Exception {
        given(owners.findById(TEST_OWNER_ID)).willReturn(Optional.of(gabo));

        mockMvc.perform(get("/owners/{ownerId}", TEST_OWNER_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("owners/ownerDetails"));
    }

    @Test
    void testShowOwnerNotFound() {
        given(owners.findById(999)).willReturn(Optional.empty());

        // CORRECCIÓN: MockMvc envuelve la excepción en 'ServletException'.
        // Capturamos ServletException y verificamos que la causa raíz sea IllegalArgumentException.
        Exception exception = assertThrows(ServletException.class, () ->
                mockMvc.perform(get("/owners/{ownerId}", 999))
        );

        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
    }
}