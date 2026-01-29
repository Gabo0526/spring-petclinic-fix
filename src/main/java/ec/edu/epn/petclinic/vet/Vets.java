
package ec.edu.epn.petclinic.vet;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Simple domain object representing a list of veterinarians. Mostly here to be used for
 * the 'vets' {@link org.springframework.web.servlet.view.xml.MarshallingView}.
 *
 */
@XmlRootElement
public class Vets {

    // SOLUCIÓN: Renombramos 'vets' a 'vetList'
    private List<Vet> vetList;

    @XmlElement
    public List<Vet> getVetList() {
        // Actualizamos las referencias dentro del método
        if (vetList == null) {
            vetList = new ArrayList<>();
        }
        return vetList;
    }

}
