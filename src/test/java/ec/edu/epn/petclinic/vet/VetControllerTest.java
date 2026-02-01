package ec.edu.epn.petclinic.vet;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(VetController.class)
class VetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VetRepository vetRepository;

    private Vet christian;

    @BeforeEach
    void setup() {
        christian = new Vet();
        christian.setId(1);
        christian.setFirstName("Christian");
        christian.setLastName("Valencia");
        // No necesitamos agregar especialidades para probar el controlador,
        // pero asegura que la lista no sea nula.
    }

    // -------------------------------------------------------------------
    // 1. Pruebas de Vista HTML (Paginación)
    // -------------------------------------------------------------------

    @Test
    void testShowVetListHtmlDefaultPage() throws Exception {
        // Simulamos la respuesta paginada del repositorio
        // PageRequest.of(0, 5) corresponde a la página 1 del controlador (1-1 = 0)
        Pageable pageable = PageRequest.of(0, 5);
        Page<@NotNull Vet> page = new PageImpl<>(List.of(christian), pageable, 1);

        given(vetRepository.findAll(any(Pageable.class))).willReturn(page);

        mockMvc.perform(get("/vets.html")) // Sin parámetro 'page', usa default "1"
                .andExpect(status().isOk())
                .andExpect(view().name("vets/vetList"))
                .andExpect(model().attributeExists("listVets"))
                .andExpect(model().attribute("listVets", hasSize(1)))
                .andExpect(model().attribute("currentPage", 1))
                .andExpect(model().attribute("totalPages", 1))
                .andExpect(model().attribute("totalItems", 1L));
    }

    @Test
    void testShowVetListHtmlPage2() throws Exception {
        // Probamos la lógica matemática: si pido page=2, al repo debe llegar page=1
        Pageable pageable = PageRequest.of(1, 5);
        Page<@NotNull Vet> page = new PageImpl<>(Collections.emptyList(), pageable, 10);

        // Aquí somos estrictos: el mock solo responde si le piden la página 1 (índice)
        given(vetRepository.findAll(pageable)).willReturn(page);

        mockMvc.perform(get("/vets.html").param("page", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("vets/vetList"))
                .andExpect(model().attribute("currentPage", 2));
    }

    // -------------------------------------------------------------------
    // 2. Pruebas de Recursos JSON (@ResponseBody)
    // -------------------------------------------------------------------

    @Test
    void testShowResourcesVetList() throws Exception {
        // Simulamos la respuesta de lista completa
        given(vetRepository.findAll()).willReturn(List.of(christian));

        mockMvc.perform(get("/vets")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // Validamos la estructura JSON.
                // Nota: La clase Vets tiene un campo "vetList", por eso $.vetList
                .andExpect(jsonPath("$.vetList").isArray())
                .andExpect(jsonPath("$.vetList[0].id").value(1))
                .andExpect(jsonPath("$.vetList[0].firstName").value("Christian"));
    }
}