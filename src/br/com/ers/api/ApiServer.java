package br.com.ers.api;

import br.com.ers.model.Alocacao;
import br.com.ers.model.Colaborador;
import br.com.ers.model.Recurso;
import br.com.ers.service.AlocacaoService;
import br.com.ers.service.ColaboradorService;
import br.com.ers.service.RecursoService;
import br.com.ers.service.AuthService;
import br.com.ers.util.LogService;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiServer {
    private final ColaboradorService colaboradorService;
    private final RecursoService recursoService;
    private final AlocacaoService alocacaoService;
    private final LogService logService;
    private final AuthService authService;
    private final Path webRoot;
    private HttpServer server;

    public ApiServer(ColaboradorService colaboradorService, RecursoService recursoService, AlocacaoService alocacaoService, AuthService authService, LogService logService, Path webRoot) {
        this.colaboradorService = colaboradorService;
        this.recursoService = recursoService;
        this.alocacaoService = alocacaoService;
        this.authService = authService;
        this.logService = logService;
        this.webRoot = webRoot;
    }

    public int start(int port) {
        if (server != null) {
            return server.getAddress().getPort();
        }
        int[] candidates = new int[]{port, port + 1, port + 2};
        IOException lastError = null;
        for (int candidate : candidates) {
            try {
                server = HttpServer.create(new InetSocketAddress(candidate), 0);
                server.createContext("/", this::handleIndex);
                server.createContext("/api/colaboradores", this::handleColaboradores);
                server.createContext("/api/recursos", this::handleRecursos);
                server.createContext("/api/alocacoes", this::handleAlocacoes);
                server.createContext("/api/alocar", this::handleAlocar);
                server.createContext("/api/devolver", this::handleDevolver);
                server.createContext("/api/login", this::handleLogin);
                server.createContext("/api/colaboradores/status", this::handleColaboradorStatus);
                server.createContext("/api/remover-colaborador", this::handleRemoverColaborador);
                server.createContext("/api/remover-recurso", this::handleRemoverRecurso);
                server.start();
                logService.info("API iniciada em http://localhost:" + candidate);
                return candidate;
            } catch (IOException e) {
                lastError = e;
                server = null;
            }
        }
        throw new RuntimeException("Falha ao iniciar API.", lastError);
    }

    private void handleIndex(HttpExchange exchange) throws IOException {
        Path index = webRoot.resolve("index.html");
        byte[] bytes = Files.readAllBytes(index);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void handleColaboradores(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            writeJson(exchange, colaboradoresToJson(colaboradorService.listarTodos()));
            return;
        }
        if ("POST".equals(exchange.getRequestMethod())) {
            Map<String, String> body = parseBody(exchange);
            Colaborador c = colaboradorService.cadastrar(body.get("nome"), body.get("cargo"), Double.parseDouble(body.get("salario")), body.get("data"));
            writeJson(exchange, "{\"status\":\"ok\",\"id\":" + c.getId() + "}");
            return;
        }
        exchange.sendResponseHeaders(405, -1);
    }

    private void handleRecursos(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            writeJson(exchange, recursosToJson(recursoService.listarTodos()));
            return;
        }
        if ("POST".equals(exchange.getRequestMethod())) {
            Map<String, String> body = parseBody(exchange);
            Recurso r = recursoService.cadastrar(body.get("nome"), body.get("categoria"), Double.parseDouble(body.get("valor")));
            writeJson(exchange, "{\"status\":\"ok\",\"id\":" + r.getId() + "}");
            return;
        }
        exchange.sendResponseHeaders(405, -1);
    }

    private void handleAlocacoes(HttpExchange exchange) throws IOException {
        writeJson(exchange, alocacoesToJson(alocacaoService.listarTodas()));
    }



    private void handleLogin(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }
        try {
            Map<String, String> body = parseBody(exchange);
            String username = body.getOrDefault("username", "");
            String password = body.getOrDefault("password", "");
            if (!authService.login(username, password)) {
                writeJsonError(exchange, 401, "Credenciais inválidas.");
                return;
            }
            String role = authService.getUsuarioLogado().getRole().name();
            writeJson(exchange, "{\"status\":\"ok\",\"username\":\"" + escape(username) + "\",\"role\":\"" + role + "\"}");
        } catch (Exception e) {
            writeJsonError(exchange, 400, e.getMessage());
        }
    }

    private void handleColaboradorStatus(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }
        try {
            Map<String, String> body = parseBody(exchange);
            int id = Integer.parseInt(body.get("id"));
            boolean ativo = Boolean.parseBoolean(body.getOrDefault("ativo", "true"));
            Colaborador colaborador = ativo ? colaboradorService.ativar(id) : colaboradorService.desativar(id);
            writeJson(exchange, "{\"status\":\"ok\",\"id\":" + colaborador.getId() + ",\"ativo\":" + colaborador.isAtivo() + "}");
        } catch (Exception e) {
            writeJsonError(exchange, 400, e.getMessage());
        }
    }

    private void handleAlocar(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }
        try {
            Map<String, String> body = parseBody(exchange);
            boolean autorizado = Boolean.parseBoolean(body.getOrDefault("autorizado", "false"));
            Alocacao a = alocacaoService.alocar(
                    Integer.parseInt(body.get("colaboradorId")),
                    Integer.parseInt(body.get("recursoId")),
                    body.getOrDefault("observacao", "Alocação via Web"),
                    autorizado
            );
            writeJson(exchange, "{\"status\":\"ok\",\"id\":" + a.getId() + "}");
        } catch (Exception e) {
            writeJsonError(exchange, 400, e.getMessage());
        }
    }

    private void handleDevolver(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }
        try {
            Map<String, String> body = parseBody(exchange);
            int recursoId = Integer.parseInt(body.get("recursoId"));
            String observacao = body.getOrDefault("observacao", "Devolução via Web");
            alocacaoService.devolver(recursoId, observacao);
            writeJson(exchange, "{\"status\":\"ok\"}");
        } catch (Exception e) {
            writeJsonError(exchange, 400, e.getMessage());
        }
    }


    private void handleRemoverColaborador(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }
        try {
            Map<String, String> body = parseBody(exchange);
            int id = Integer.parseInt(body.get("id"));
            boolean possuiAtiva = alocacaoService.listarPorColaborador(id).stream().anyMatch(Alocacao::isAtiva);
            if (possuiAtiva) {
                throw new IllegalStateException("Colaborador possui alocação ativa. Devolva o recurso antes de remover.");
            }
            colaboradorService.remover(id);
            writeJson(exchange, "{\"status\":\"ok\"}");
        } catch (Exception e) {
            writeJsonError(exchange, 400, e.getMessage());
        }
    }

    private void handleRemoverRecurso(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }
        try {
            Map<String, String> body = parseBody(exchange);
            int id = Integer.parseInt(body.get("id"));
            boolean possuiAtiva = alocacaoService.listarTodas().stream().anyMatch(a -> a.getRecursoId() == id && a.isAtiva());
            if (possuiAtiva) {
                throw new IllegalStateException("Recurso está alocado. Faça a devolução antes de remover.");
            }
            recursoService.remover(id);
            writeJson(exchange, "{\"status\":\"ok\"}");
        } catch (Exception e) {
            writeJsonError(exchange, 400, e.getMessage());
        }
    }

    private Map<String, String> parseBody(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> map = new HashMap<>();
        for (String pair : body.split("&")) {
            if (pair.contains("=")) {
                String[] parts = pair.split("=", 2);
                map.put(URLDecoder.decode(parts[0], StandardCharsets.UTF_8), URLDecoder.decode(parts[1], StandardCharsets.UTF_8));
            }
        }
        return map;
    }

    private void writeJson(HttpExchange exchange, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void writeJsonError(HttpExchange exchange, int statusCode, String message) throws IOException {
        String json = "{\"status\":\"error\",\"message\":\"" + escape(message) + "\"}";
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String colaboradoresToJson(List<Colaborador> colaboradores) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < colaboradores.size(); i++) {
            Colaborador c = colaboradores.get(i);
            if (i > 0) sb.append(',');
            sb.append("{\"id\":").append(c.getId())
                    .append(",\"nome\":\"").append(escape(c.getNome()))
                    .append("\",\"cargo\":\"").append(escape(c.getCargo()))
                    .append("\",\"salario\":").append(c.getSalario())
                    .append(",\"ativo\":").append(c.isAtivo())
                    .append("}");
        }
        return sb.append(']').toString();
    }

    private String recursosToJson(List<Recurso> recursos) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < recursos.size(); i++) {
            Recurso r = recursos.get(i);
            if (i > 0) sb.append(',');
            sb.append("{\"id\":").append(r.getId())
                    .append(",\"nome\":\"").append(escape(r.getNomeDoRecurso()))
                    .append("\",\"categoria\":\"").append(r.getCategoria())
                    .append("\",\"disponivel\":").append(r.isDisponivel())
                    .append(",\"valor\":").append(r.getValorEstimado())
                    .append("}");
        }
        return sb.append(']').toString();
    }

    private String alocacoesToJson(List<Alocacao> alocacoes) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < alocacoes.size(); i++) {
            Alocacao a = alocacoes.get(i);
            if (i > 0) sb.append(',');
            sb.append("{\"id\":").append(a.getId())
                    .append(",\"colaboradorId\":").append(a.getColaboradorId())
                    .append(",\"recursoId\":").append(a.getRecursoId())
                    .append(",\"ativa\":").append(a.isAtiva())
                    .append("}");
        }
        return sb.append(']').toString();
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
