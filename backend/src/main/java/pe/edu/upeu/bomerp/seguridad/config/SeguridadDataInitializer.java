package pe.edu.upeu.bomerp.seguridad.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.bomerp.seguridad.usuario.entity.Rol;
import pe.edu.upeu.bomerp.seguridad.usuario.entity.Usuario;
import pe.edu.upeu.bomerp.seguridad.usuario.repository.RolRepository;
import pe.edu.upeu.bomerp.seguridad.usuario.repository.UsuarioRepository;

import java.util.Set;

@Slf4j
@Component
public class SeguridadDataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public SeguridadDataInitializer(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Inicializando roles y usuarios semilla de Seguridad (AgroPeruvianFoods)...");

        Rol rolAdmin = obtenerOCrearRol("ADMIN");
        Rol rolVendedor = obtenerOCrearRol("VENDEDOR");
        Rol rolOperador = obtenerOCrearRol("OPERADOR");
        Rol rolSupervisor = obtenerOCrearRol("SUPERVISOR");
        Rol rolAdminFinanzas = obtenerOCrearRol("ADMIN_FINANZAS");
        Rol rolCajero = obtenerOCrearRol("CAJERO");

        // 1. Usuario Administrador (Rol: ADMIN, ADMIN_FINANZAS)
        crearUsuarioSiNoExiste("admin@agroperu.com", "AgroPeru2026!", Set.of(rolAdmin, rolAdminFinanzas));

        // 2. Usuario Vendedor (Rol: VENDEDOR - rol restringido)
        crearUsuarioSiNoExiste("vendedor@agroperu.com", "AgroPeru2026!", Set.of(rolVendedor));

        // 3. Usuario Operador / Cajero (Rol: CAJERO, OPERADOR - rol restringido)
        crearUsuarioSiNoExiste("cajero@agroperu.com", "AgroPeru2026!", Set.of(rolCajero, rolOperador));
        crearUsuarioSiNoExiste("operador@agroperu.com", "AgroPeru2026!", Set.of(rolOperador));

        log.info("Usuarios semilla inicializados correctamente.");
    }

    private Rol obtenerOCrearRol(String nombre) {
        return rolRepository.findByNombre(nombre).orElseGet(() -> {
            Rol r = new Rol(nombre);
            return rolRepository.save(r);
        });
    }

    private void crearUsuarioSiNoExiste(String email, String rawPassword, Set<Rol> roles) {
        if (!usuarioRepository.existsByEmail(email)) {
            Usuario u = new Usuario();
            u.setEmail(email);
            u.setPassword(passwordEncoder.encode(rawPassword));
            u.setActivo(true);
            u.setRoles(roles);
            usuarioRepository.save(u);
            log.info("Usuario semilla creado: {} con roles {}", email, roles.stream().map(Rol::getNombre).toList());
        }
    }
}
