package br.com.ers.repository;

import br.com.ers.model.Role;
import br.com.ers.model.Usuario;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioRepository {
    private final List<Usuario> usuarios = new ArrayList<>();

    public UsuarioRepository() {
        usuarios.add(new Usuario("admin", "123", Role.ADMIN));
        usuarios.add(new Usuario("user", "123", Role.USER));
    }

    public Optional<Usuario> findByUsername(String username) {
        return usuarios.stream().filter(u -> u.getUsername().equalsIgnoreCase(username)).findFirst();
    }
}
