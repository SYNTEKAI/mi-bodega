package pe.mibodega.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;
import pe.mibodega.util.SinPermisoException;

class UsuarioSesionTest {

    @Test
    void usuarioDeEmpresaSoloTieneLosPermisosDeSuRol() {
        UsuarioSesion s = new UsuarioSesion();
        s.setTipo(UsuarioSesion.EMPRESA);
        s.setPermisos(Set.of("ventas.ver", "ventas.crear"));
        assertTrue(s.puede("ventas", "crear"));
        assertFalse(s.puede("ventas", "anular"));
        assertThrows(SinPermisoException.class, () -> s.exigir("usuarios", "ver"));
    }

    @Test
    void internoNoPrincipalSoloVeDentroDeUnaEmpresa() {
        UsuarioSesion s = new UsuarioSesion();
        s.setTipo(UsuarioSesion.INTERNO);
        s.setPermisos(Set.of("empresas.ver", "empresas.entrar"));
        assertTrue(s.puede("productos", "ver"));
        assertFalse(s.puede("productos", "crear"));
        assertTrue(s.puede("empresas", "entrar"));
        assertFalse(s.puede("empresas", "crear"));
    }

    @Test
    void superAdminPuedeTodo() {
        UsuarioSesion s = new UsuarioSesion();
        s.setTipo(UsuarioSesion.INTERNO);
        s.setRolPrincipal(true);
        assertTrue(s.puede("ventas", "anular"));
        assertTrue(s.isAdminEmpresa());
    }

    @Test
    void elCajeroTrabajaEnSuSedeYElAdminEnLaQueElige() {
        UsuarioSesion cajero = new UsuarioSesion();
        cajero.setTipo(UsuarioSesion.EMPRESA);
        cajero.setRolNombre("Vendedor");
        cajero.setIdSedeUsuario(1);
        cajero.setIdSedeVista(2);
        assertEquals(1, cajero.getSedeActual());

        UsuarioSesion admin = new UsuarioSesion();
        admin.setTipo(UsuarioSesion.EMPRESA);
        admin.setRolNombre("Administrador");
        admin.setIdSedeUsuario(1);
        admin.setIdSedeVista(2);
        assertEquals(2, admin.getSedeActual());
    }
}
