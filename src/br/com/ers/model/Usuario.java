package br.com.ers.model;

public class Usuario {
    private final String username;
    private final String password;
    private final Role role;

    public Usuario(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
}
