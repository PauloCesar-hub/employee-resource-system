package br.com.ers.service;

import br.com.ers.model.Usuario;
import br.com.ers.repository.UsuarioRepository;
import br.com.ers.util.LogService;

public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final LogService logService;
    private Usuario usuarioLogado;

    public AuthService(UsuarioRepository usuarioRepository, LogService logService) {
        this.usuarioRepository = usuarioRepository;
        this.logService = logService;
    }

    public boolean login(String username, String password) {
        usuarioLogado = usuarioRepository.findByUsername(username)
                .filter(u -> u.getPassword().equals(password))
                .orElse(null);
        if (usuarioLogado != null) {
            logService.info("Login realizado por " + usuarioLogado.getUsername() + " com perfil " + usuarioLogado.getRole());
            return true;
        }
        logService.error("Tentativa de login inválida para usuário " + username);
        return false;
    }

    public Usuario getUsuarioLogado() { return usuarioLogado; }
    public boolean isAdmin() { return usuarioLogado != null && usuarioLogado.getRole().name().equals("ADMIN"); }
    public boolean isLogado() { return usuarioLogado != null; }
}
