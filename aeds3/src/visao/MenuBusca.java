package visao;

import dados.ArquivoPergunta;
import dados.ArquivoUsuarios;
import entidades.Pergunta;
import entidades.Usuario;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

/**
 * Buscar perguntas / Perguntas mais recentes
 */
public class MenuBusca {

    private final ArquivoPergunta arqPerguntas;
<<<<<<< HEAD
    private final ArquivoUsuarios arqUsuarios;
    private final MenuResposta menuResposta;
=======
    private final MenuRespostas menuRespostas;
>>>>>>> origin/main
    private final Usuario usuario;
    private final Scanner console;
    private final SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm");

<<<<<<< HEAD
    public MenuBusca(ArquivoPergunta arqPerguntas, ArquivoUsuarios arqUsuarios, MenuResposta menuResposta, Usuario usuario, Scanner console) {
        this.arqPerguntas = arqPerguntas;
        this.arqUsuarios = arqUsuarios;
        this.menuResposta = menuResposta;
=======
    public MenuBusca(ArquivoPergunta arqPerguntas, MenuRespostas menuRespostas, Usuario usuario, Scanner console) {
        this.arqPerguntas = arqPerguntas;
        this.menuRespostas = menuRespostas;
>>>>>>> origin/main
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
            System.out.println("(B) Busca por palavras-chave");
            System.out.println("\n(R) Retornar");
            System.out.print("\nOpção: ");
            opcao = console.nextLine().trim().toUpperCase();

            try {
                switch (opcao) {
                    case "A": perguntasRecentes(); break;
                    case "B": System.out.println("Busca por palavras-chave ainda não está disponível."); break;
                    case "R": break;
                    default: System.out.println("Opção inválida!");
                }
            } catch (Exception e) {
                System.out.println("Erro: " + e.getMessage());
            }
        } while (!opcao.equals("R"));
    }

    //perguntas recentes
    private void perguntasRecentes() throws Exception {
        int pagina = 0;
        while (true) {
            int totalPaginas = arqPerguntas.totalPaginasRecentes();
            if (pagina >= totalPaginas) pagina = totalPaginas - 1; //  pergunta arquivada esvaziou a última página

            ArrayList<Pergunta> perguntas = arqPerguntas.listarRecentes(pagina);

            System.out.println("------------");
            System.out.println("\n> Início > Buscar perguntas > Perguntas mais recentes");
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
                System.out.println("(" + numero + ") " + p.getPergunta());
                System.out.println("    Criada por " + nomeAutor(p) + " em " + formato.format(new Date(p.getCriacao())));
                System.out.println();
                numero++;
            }

            if (pagina > 0) System.out.println("(A) Página anterior");
            if (pagina < totalPaginas - 1) System.out.println("(B) Próxima página");
            System.out.println("\n(R) Retornar");
            System.out.print("\nOpção: ");
            String entrada = console.nextLine().trim().toUpperCase();

            if (entrada.equals("R")) {
                return;
            } else if (entrada.equals("B") && pagina < totalPaginas - 1) {
                pagina++;
            } else if (entrada.equals("A") && pagina > 0) {
                pagina--;
            } else {
                try {
                    int escolha = Integer.parseInt(entrada);
                    if (escolha >= 1 && escolha <= perguntas.size()) {
                        menuResposta.abrirPerguntaBusca(perguntas.get(escolha - 1).getId(),
                            "Início > Buscar perguntas > Perguntas mais recentes > Pergunta");
                    } else {
                        System.out.println("Número fora da lista.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Opção inválida!");
                }
            }
        }
    }

<<<<<<< HEAD
    
    // Nome do autor da pergunta (o ID do usuário é resolvido no arquivo de usuários)
    private String nomeAutor(Pergunta p) throws Exception {
        Usuario autor = arqUsuarios.read(p.getIdUsuario());
        return autor != null ? autor.getNome() : "usuário desconhecido";
=======
    // Tela da pergunta com as respostas (ordenadas por nota), responder e avaliar
    private void abrirPergunta(int idPergunta) throws Exception {
        menuRespostas.abrirPergunta(idPergunta, "> Início > Buscar perguntas > Perguntas mais recentes");
>>>>>>> origin/main
    }
}