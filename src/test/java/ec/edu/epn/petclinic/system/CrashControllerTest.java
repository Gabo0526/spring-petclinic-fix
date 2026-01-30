package ec.edu.epn.petclinic.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CrashController.class)
class CrashControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testTriggerException() {
        // Ejecutamos la petición y esperamos que "explote" con una ServletException
        // (Spring envuelve las excepciones de controladores en ServletException)
        Exception exception = assertThrows(ServletException.class, () -> mockMvc.perform(get("/oups")));

        // Ahora inspeccionamos la causa raíz del error
        Throwable rootCause = exception.getCause();

        // 1. Verificamos que sea IllegalStateException
        assertInstanceOf(IllegalStateException.class, rootCause);

        // 2. Verificamos el mensaje exacto
        assertEquals("Expected: controller used to showcase what happens when an exception is thrown",
                rootCause.getMessage());
    }
}