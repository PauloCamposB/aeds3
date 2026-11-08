package visao;

import dados.ArquivoPergunta;
import dados.ArquivoUsuarios;
import dados.BuscaPerguntas;
import entidades.Pergunta;
import entidades.Usuario;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import util.ResultadoBusca;

/**
 * Buscar perguntas: perguntas mais recentes (opção A) e busca por
 * palavras-chave com ranking TFxIDF (opção B).
 */
public class MenuBusca {

    private final ArquivoPergunta arqPerguntas;
    private final ArquivoUsuarios arqUsuarios;
    private final MenuResposta menuResposta;
    private final Usuario usuario;
    private final Scanner console;
    private final SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm");

    public MenuBusca(ArquivoPergunta arqPerguntas, ArquivoUsuarios arqUsuarios, MenuResposta menuResposta, Usuario usuario, Scanner console) {
        this.arqPerguntas = arqPerguntas;
        this.arqUsuarios = arqUsuarios;
        this.menuResposta = menuResposta;
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
                    case "B": buscaPorPalavrasChave(); break;
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

            System.out.println("\nAJUDA AÍ 1.0");
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

    
    // busca por palavras-chave (índice invertido + ranking TFxIDF)
    private void buscaPorPalavrasChave() throws Exception {
        BuscaPerguntas busca = new BuscaPerguntas(arqPerguntas.getIndicePalavras(), arqPerguntas);

        while (true) {
            System.out.println("\nAJUDA AÍ 1.0");
            System.out.println("------------");
            System.out.println("\n> Início > Buscar perguntas > Busca por palavras-chave\n");
            System.out.print("Digite os termos de busca (ENTER para retornar): ");
            String consulta = console.nextLine().trim();
            if (consulta.isEmpty()) {
                return;
            }

            ArrayList<ResultadoBusca> resultados = busca.buscar(consulta);
            if (resultados.isEmpty()) {
                System.out.println("\nNenhuma pergunta encontrada para \"" + consulta + "\".");
                System.out.println("(termos muito comuns, como artigos e preposições, são ignorados)");
                continue;
            }
            if (!mostrarResultados(consulta, resultados)) {
                return;
            }
        }
    }

    // Mostra os resultados de 5 em 5. Retorna true para fazer uma nova busca e false para voltar ao menu.
    private boolean mostrarResultados(String consulta, ArrayList<ResultadoBusca> resultados) throws Exception {
        int tamanhoPagina = ArquivoPergunta.TAMANHO_PAGINA;
        int totalPaginas = (resultados.size() + tamanhoPagina - 1) / tamanhoPagina;
        int pagina = 0;

        while (true) {
            int inicio = pagina * tamanhoPagina;
            int fim = Math.min(inicio + tamanhoPagina, resultados.size());
            ArrayList<ResultadoBusca> daPagina = new ArrayList<>(resultados.subList(inicio, fim));

            System.out.println("\nAJUDA AÍ 1.0");
            System.out.println("------------");
            System.out.println("\n> Início > Buscar perguntas > Busca por palavras-chave > Resultados");
            System.out.println("Busca: \"" + consulta + "\" - " + resultados.size() + " resultado(s)");
            System.out.println("Página " + (pagina + 1) + " de " + totalPaginas + "\n");

            int numero = 1;
            for (ResultadoBusca r : daPagina) {
                Pergunta p = r.getPergunta();
                System.out.println("(" + numero + ") " + p.getPergunta());
                System.out.println("    Palavras chave: " + p.getPalavrasChave());
                System.out.println("    Criada por " + nomeAutor(p) + " em " + formato.format(new Date(p.getCriacao()))
                    + " | relevância (TFxIDF): " + String.format("%.3f", r.getScore()));
                System.out.println();
                numero++;
            }

            if (pagina > 0) System.out.println("(A) Página anterior");
            if (pagina < totalPaginas - 1) System.out.println("(B) Próxima página");
            System.out.println("(N) Nova busca");
            System.out.println("\n(R) Retornar");
            System.out.print("\nOpção: ");
            String entrada = console.nextLine().trim().toUpperCase();

            if (entrada.equals("R")) {
                return false;
            } else if (entrada.equals("N")) {
                return true;
            } else if (entrada.equals("B") && pagina < totalPaginas - 1) {
                pagina++;
            } else if (entrada.equals("A") && pagina > 0) {
                pagina--;
            } else {
                try {
                    int escolha = Integer.parseInt(entrada);
                    if (escolha >= 1 && escolha <= daPagina.size()) {
                        menuResposta.abrirPerguntaBusca(daPagina.get(escolha - 1).getId(),
                            "Início > Buscar perguntas > Busca por palavras-chave > Pergunta");
                    } else {
                        System.out.println("Número fora da lista.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Opção inválida!");
                }
            }
        }
    }

    // Nome do autor da pergunta (o ID do usuário é resolvido no arquivo de usuários)
    private String nomeAutor(Pergunta p) throws Exception {
        Usuario autor = arqUsuarios.read(p.getIdUsuario());
        return autor != null ? autor.getNome() : "usuário desconhecido";
    }
}