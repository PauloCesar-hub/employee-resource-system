package br.com.ers;

import br.com.ers.api.ApiServer;
import br.com.ers.model.Colaborador;
import br.com.ers.model.Recurso;
import br.com.ers.persistence.CsvStorage;
import br.com.ers.repository.AlocacaoRepository;
import br.com.ers.repository.ColaboradorRepository;
import br.com.ers.repository.RecursoRepository;
import br.com.ers.repository.UsuarioRepository;
import br.com.ers.service.AlocacaoService;
import br.com.ers.service.AuthService;
import br.com.ers.service.ColaboradorService;
import br.com.ers.service.RelatorioService;
import br.com.ers.service.RecursoService;
import br.com.ers.util.LogService;

import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Path dataPath = Path.of("data");
        LogService logService = new LogService(dataPath);
        CsvStorage storage = new CsvStorage(dataPath);
        ColaboradorRepository colaboradorRepository = new ColaboradorRepository(storage);
        RecursoRepository recursoRepository = new RecursoRepository(storage);
        AlocacaoRepository alocacaoRepository = new AlocacaoRepository(storage);

        ColaboradorService colaboradorService = new ColaboradorService(colaboradorRepository, logService);
        RecursoService recursoService = new RecursoService(recursoRepository, logService);
        AlocacaoService alocacaoService = new AlocacaoService(alocacaoRepository, colaboradorService, recursoService, logService);
        RelatorioService relatorioService = new RelatorioService(colaboradorService, recursoService, alocacaoService);
        AuthService authService = new AuthService(new UsuarioRepository(), logService);
        ApiServer apiServer = new ApiServer(colaboradorService, recursoService, alocacaoService, authService, logService, Path.of("web"));

        seedIfNeeded(colaboradorService, recursoService);
        runConsole(authService, colaboradorService, recursoService, alocacaoService, relatorioService, apiServer);
    }

    private static void seedIfNeeded(ColaboradorService colaboradorService, RecursoService recursoService) {
        if (colaboradorService.listarTodos().isEmpty()) {
            colaboradorService.cadastrar("Ana Souza", "Analista", 5500, "2026-02-10");
            colaboradorService.cadastrar("Bruno Lima", "Dev Jr", 4500, "2026-03-01");
            colaboradorService.cadastrar("Gustavo Panham", "Estagiario", 2800, "2026-02-14");
        }
        if (recursoService.listarTodos().isEmpty()) {
            recursoService.cadastrar("MacBook Pro M5", "Notebook", 12000);
            recursoService.cadastrar("Dell Latitude i7", "Notebook", 4200);
            recursoService.cadastrar("Cadeira Ergonomica", "Cadeira", 1800);
            recursoService.cadastrar("Fone JBL", "Acessorio", 500);
        }
    }

    private static void runConsole(AuthService authService, ColaboradorService colaboradorService, RecursoService recursoService,
                                   AlocacaoService alocacaoService, RelatorioService relatorioService, ApiServer apiServer) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== ERS | Employee Resource System ===");
        System.out.println("Login padrão: admin/123 ou user/123");

        while (!authService.isLogado()) {
            System.out.print("Usuário: ");
            String username = scanner.nextLine();
            System.out.print("Senha: ");
            String password = scanner.nextLine();
            if (!authService.login(username, password)) {
                System.out.println("Credenciais inválidas. Tente novamente.\n");
            }
        }

        int opcao = -1;
        while (opcao != 0) {
            exibirMenu(authService.isAdmin());
            try {
                System.out.print("Escolha uma opção: ");
                opcao = Integer.parseInt(scanner.nextLine());
                switch (opcao) {
                    case 1 -> listar("COLABORADORES", colaboradorService.listarTodos());
                    case 2 -> listar("RECURSOS", recursoService.listarTodos());
                    case 3 -> listar("RECURSOS DISPONÍVEIS", recursoService.listarDisponiveis());
                    case 4 -> listar("COLABORADORES ATIVOS", colaboradorService.listarAtivos());
                    case 5 -> System.out.println(colaboradorService.buscarPorNome(prompt(scanner, "Nome do colaborador: ")));
                    case 6 -> System.out.println(recursoService.buscarPorNome(prompt(scanner, "Nome do recurso: ")));
                    case 7 -> {
                        checkAdmin(authService);
                        Colaborador c = colaboradorService.cadastrar(prompt(scanner, "Nome: "), prompt(scanner, "Cargo: "),
                                Double.parseDouble(prompt(scanner, "Salário: ").replace(',', '.')), prompt(scanner, "Data admissão (AAAA-MM-DD): "));
                        System.out.println("Colaborador criado com ID automático: " + c.getId());
                    }
                    case 8 -> {
                        checkAdmin(authService);
                        Recurso r = recursoService.cadastrar(prompt(scanner, "Nome do recurso: "), prompt(scanner, "Categoria: "),
                                Double.parseDouble(prompt(scanner, "Valor estimado: ").replace(',', '.')));
                        System.out.println("Recurso criado com ID automático: " + r.getId());
                    }
                    case 9 -> {
                        checkAdmin(authService);
                        int id = Integer.parseInt(prompt(scanner, "ID do colaborador para remover: "));
                        if (confirmar(scanner, "Confirmar remoção? (S/N): ")) {
                            colaboradorService.remover(id);
                            System.out.println("Colaborador removido.");
                        }
                    }
                    case 10 -> {
                        checkAdmin(authService);
                        int id = Integer.parseInt(prompt(scanner, "ID do recurso para remover: "));
                        if (confirmar(scanner, "Confirmar remoção? (S/N): ")) {
                            recursoService.remover(id);
                            System.out.println("Recurso removido.");
                        }
                    }
                    case 11 -> {
                        int colaboradorId = Integer.parseInt(prompt(scanner, "ID do colaborador: "));
                        int recursoId = Integer.parseInt(prompt(scanner, "ID do recurso: "));

                        Recurso recurso = recursoService.buscarPorId(recursoId);
                        boolean autorizado = false;

                        if (recurso.exigeAutorizacaoEspecial()) {
                            checkAdmin(authService);
                            autorizado = confirmar(scanner, "Recurso de alto valor. Autorizar alocação especial? (S/N): ");
                            if (!autorizado) {
                                throw new IllegalStateException("Alocação cancelada: recurso de alto valor sem autorização.");
                            }
                        }

                        alocacaoService.alocar(colaboradorId, recursoId, "Alocação via console", autorizado);
                        System.out.println("Recurso alocado com sucesso.");
                    }
                    case 12 -> {
                        int recursoId = Integer.parseInt(prompt(scanner, "ID do recurso: "));
                        String observacao = prompt(scanner, "Observação da devolução: ");
                        alocacaoService.devolver(recursoId, observacao);
                        System.out.println("Recurso devolvido com sucesso.");
                    }
                    case 13 -> listar("ALOCAÇÕES", alocacaoService.listarTodas());
                    case 14 -> {
                        int colaboradorId = Integer.parseInt(prompt(scanner, "ID do colaborador: "));
                        System.out.println("Custo total: R$ " + relatorioService.custoTotalPorColaborador(colaboradorId));
                    }
                    case 15 -> listar("RECURSOS ACIMA DO VALOR", relatorioService.recursosMaisCaros(Double.parseDouble(prompt(scanner, "Valor mínimo: ").replace(',', '.'))));
                    case 16 -> listar("COLABORADORES SEM RECURSOS", relatorioService.colaboradoresSemRecursos());
                    case 17 -> {
                        checkAdmin(authService);
                        int id = Integer.parseInt(prompt(scanner, "ID do colaborador para ativar: "));
                        Colaborador colaborador = colaboradorService.ativar(id);
                        System.out.println("Colaborador ativado: " + colaborador.getNome());
                    }
                    case 18 -> {
                        checkAdmin(authService);
                        int id = Integer.parseInt(prompt(scanner, "ID do colaborador para desativar: "));
                        Colaborador colaborador = colaboradorService.desativar(id);
                        System.out.println("Colaborador desativado: " + colaborador.getNome());
                    }
                    case 19 -> {
                        int port = apiServer.start(8080);
                        System.out.println("API/Web iniciada em http://localhost:" + port);
                    }
                    case 0 -> System.out.println("Saindo do sistema...");
                    default -> System.out.println("Opção inválida.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Erro: digite um número válido.");
            } catch (SecurityException e) {
                System.out.println("Ação permitida apenas para ADMIN.");
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Erro: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Erro inesperado: " + e.getMessage());
            }
        }
    }

    private static void checkAdmin(AuthService authService) {
        if (!authService.isAdmin()) {
            throw new SecurityException();
        }
    }

    private static String prompt(Scanner scanner, String label) {
        System.out.print(label);
        return scanner.nextLine();
    }

    private static boolean confirmar(Scanner scanner, String label) {
        System.out.print(label);
        return scanner.nextLine().trim().equalsIgnoreCase("S");
    }

    private static void exibirMenu(boolean isAdmin) {
        System.out.println("\n========= MENU ERS =========");
        System.out.println("1 - Listar colaboradores");
        System.out.println("2 - Listar recursos");
        System.out.println("3 - Filtrar recursos disponíveis");
        System.out.println("4 - Filtrar colaboradores ativos");
        System.out.println("5 - Buscar colaborador por nome");
        System.out.println("6 - Buscar recurso por nome");
        System.out.println("7 - Adicionar colaborador" + (isAdmin ? "" : " [ADMIN]"));
        System.out.println("8 - Adicionar recurso" + (isAdmin ? "" : " [ADMIN]"));
        System.out.println("9 - Remover colaborador" + (isAdmin ? "" : " [ADMIN]"));
        System.out.println("10 - Remover recurso" + (isAdmin ? "" : " [ADMIN]"));
        System.out.println("11 - Alocar recurso");
        System.out.println("12 - Devolver recurso");
        System.out.println("13 - Listar alocações");
        System.out.println("14 - Relatório de custo por colaborador");
        System.out.println("15 - Relatório de recursos mais caros");
        System.out.println("16 - Relatório de colaboradores sem recursos");
        System.out.println("17 - Ativar colaborador" + (isAdmin ? "" : " [ADMIN]"));
        System.out.println("18 - Desativar colaborador" + (isAdmin ? "" : " [ADMIN]"));
        System.out.println("19 - Iniciar API + interface web");
        System.out.println("0 - Sair");
        System.out.println("============================");
    }

    private static void listar(String titulo, List<?> itens) {
        System.out.println("\n=== " + titulo + " ===");
        if (itens.isEmpty()) {
            System.out.println("(vazio)");
            return;
        }
        for (Object item : itens) {
            System.out.println(item);
        }
    }
}
