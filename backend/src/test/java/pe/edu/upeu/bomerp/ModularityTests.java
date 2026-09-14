package pe.edu.upeu.bomerp;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModularityTests {

    @Test
    void verifyModularity() {
        ApplicationModules.of(BomerpBackendApplication.class).verify();
    }
}
