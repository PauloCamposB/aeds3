package visao;

import dados.ArquivoPergunta;
import entidades.Pergunta;
import entidades.Usuario;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

/**
 * Buscar perguntas / Perguntas mais recentes, com paginação de 5 em 5.
 */
public class MenuBusca {

    private final ArquivoPergunta arqPerguntas;
    private final Usuario usuario;
    private final Scanner console;
    private final SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public MenuBusca(ArquivoPergunta arqPerguntas, Usuario usuario, Scanner console) {
        this.arqPerguntas = arqPerguntas;
        this.usuario = usuario;
        this.console = console;
    }

    public void menu() {
        String opcao = "";
        do {
            System.out.println("\nAJUDA AÍ 1.0");
            System.out.println("------------");
            System.out.println("\n> Início > Buscar perguntas\n");
            System.out.println("(A) Perguntas mais recentes");
            System.out.println("\n(R) Retornar ao menu anterior");
            System.out.print("\nOpção: ");
            opcao = console.nextLine().trim().toUpperCase();

            try {
                switch (opcao) {
                    case "A": perguntasRecentes(); break;
                    case "R": break;
                    default: System.out.println("Opção inválida!");
                }
            } catch (Exception e) {
                System.out.println("Erro: " + e.getMessage());
            }
        } while (!opcao.equals("R"));
    }

    // ------------------------------------------------- PERGUNTAS MAIS RECENTES
    private void perguntasRecentes() throws Exception {
        int pagina = 0;
        while (true) {
            int totalPaginas = arqPerguntas.totalPaginasRecentes();
            if (pagina >= totalPaginas) pagina = totalPaginas - 1; // ex.: pergunta arquivada esvaziou a última página

            ArrayList<Pergunta> perguntas = arqPerguntas.listarRecentes(pagina);

            System.out.println("\nPERGUNTAS MAIS RECENTES");
            System.out.println("Página " + (pagina + 1) + " de " + totalPaginas + "\n");

            if (perguntas.isEmpty()) {
                System.out.println("Ainda não há perguntas cadastradas.");
                System.out.print("\nPressione ENTER para continuar...");
                console.nextLine();
                return;
            }

            // O número mostrado na tela (1 a 5) é associado ao ID real da pergunta
            int numero = 1;
            for (Pergunta p : perguntas) {
                System.out.println("(" + numero + ") " + formato.format(new Date(p.getCriacao())));
                System.out.println(p.getPergunta());
                System.out.println("Palavras chave: " + p.getPalavrasChave());
                System.out.println();
                numero++;
            }

            System.out.println("Digite o número da pergunta para abri-la");
            if (pagina > 0) System.out.print("(A) Página anterior   ");
            if (pagina < totalPaginas - 1) System.out.print("(P) Próxima página   ");
            System.out.println("(R) Retornar");
            System.out.print("\nOpção: ");
            String entrada = console.nextLine().trim().toUpperCase();

            if (entrada.equals("R")) {
                return;
            } else if (entrada.equals("P") && pagina < totalPaginas - 1) {
                pagina++;
            } else if (entrada.equals("A") && pagina > 0) {
                pagina--;
            } else {
                try {
                    int escolha = Integer.parseInt(entrada);
                    if (escolha >= 1 && escolha <= perguntas.size()) {
                        abrirPergunta(perguntas.get(escolha - 1).getId());
                    } else {
                        System.out.println("Número fora da lista.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Opção inválida!");
                }
            }
        }
    }

    /**
     * PONTO DE INTEGRAÇÃO com o Membro 4 (tela da pergunta com respostas).
     * Quando a tela dele existir, é só trocar o corpo por algo como:
     *     new MenuPergunta(..., idPergunta, ...).exibir();
     */
    private void abrirPergunta(int idPergunta) throws Exception {
        Pergunta p = arqPerguntas.read(idPergunta);
        if (p == null) {
            System.out.println("Pergunta não encontrada.");
            return;
        }
        System.out.println("\nPERGUNTA");
        System.out.println(formato.format(new Date(p.getCriacao())));
        System.out.println(p.getPergunta());
        System.out.println("Palavras chave: " + p.getPalavrasChave());
        System.out.print("\nPressione ENTER para continuar...");
        console.nextLine();
    }
}