package visao;

import dados.ArquivoPergunta;
import entidades.Pergunta;
import entidades.Usuario;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

/**
 * Visão e controle das perguntas do usuário logado.
 * Menu: Listar, Incluir, Alterar e Arquivar.
 */

 
public class MenuPerguntas {

    private static final int MAX_CARACTERES = 5000; // evita estourar o limite de tamanho do registro

    private final ArquivoPergunta arqPerguntas;
    private final Usuario usuario;
    private final Scanner console;
    private final SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    // Associação entre o número mostrado na tela e o ID real da pergunta
    private ArrayList<Integer> idsListados = new ArrayList<>();

    public MenuPerguntas(ArquivoPergunta arqPerguntas, Usuario usuario, Scanner console) {
        this.arqPerguntas = arqPerguntas;
        this.usuario = usuario;
        this.console = console;
    }

    public void menu() {
        String opcao = "";
        do {
            System.out.println("\nAJUDA AÍ 1.0");
            System.out.println("------------");
            System.out.println("\n> Início > Minha área > Minhas perguntas\n");
            System.out.println("(A) Listar");
            System.out.println("(B) Incluir");
            System.out.println("(C) Alterar");
            System.out.println("(D) Arquivar");
            System.out.println("\n(R) Retornar ao menu anterior");
            System.out.print("\nOpção: ");
            opcao = console.nextLine().trim().toUpperCase();

            try {
                switch (opcao) {
                    case "A": listar(); break;
                    case "B": incluir(); break;
                    case "C": alterar(); break;
                    case "D": arquivar(); break;
                    case "R": break;
                    default: System.out.println("Opção inválida!");
                }
            } catch (Exception e) {
                System.out.println("Erro: " + e.getMessage());
            }
        } while (!opcao.equals("R"));
    }

    // ---------------------------------------------------------------- LISTAR
    private void listar() throws Exception {
        if (!mostrarLista()) {
            return;
        }
        System.out.print("\nPressione ENTER para continuar...");
        console.nextLine();
    }

    /**
     * Mostra as perguntas do usuário numeradas e preenche o vetor de associação
     * (número da tela -> ID da pergunta). Retorna false se não houver perguntas.
     */
    private boolean mostrarLista() throws Exception {
        ArrayList<Pergunta> perguntas = arqPerguntas.readByUsuario(usuario.getId());
        idsListados = new ArrayList<>();

        System.out.println("\nMINHAS PERGUNTAS\n");

        if (perguntas.isEmpty()) {
            System.out.println("Você ainda não possui perguntas.");
            return false;
        }

        int numero = 1;
        for (Pergunta p : perguntas) {
            idsListados.add(p.getId());

            System.out.print("(" + numero + ") ");
            if (!p.isAtiva()) {
                System.out.print("ARQUIVADA");
            }
            System.out.println();
            System.out.println(formato.format(new Date(p.getCriacao())));
            System.out.println(p.getPergunta());
            System.out.println("Palavras chave: " + p.getPalavrasChave());
            System.out.println();
            numero++;
        }
        return true;
    }

    // ---------------------------------------------------------------- INCLUIR
    private void incluir() throws Exception {
        System.out.println("\nNOVA PERGUNTA");

        String texto = lerTexto("Pergunta (termine com uma linha vazia): ");
        if (texto.isEmpty()) {
            System.out.println("A pergunta não pode ficar vazia. Inclusão cancelada.");
            return;
        }

        System.out.print("Palavras chave (separadas por ponto-e-vírgula): ");
        String palavras = console.nextLine().trim();
        if (palavras.isEmpty()) {
            System.out.println("Informe ao menos uma palavra chave. Inclusão cancelada.");
            return;
        }

        System.out.print("Confirma a inclusão? (S/N): ");
        if (!console.nextLine().trim().equalsIgnoreCase("S")) {
            System.out.println("Inclusão cancelada.");
            return;
        }

        // A pergunta é vinculada automaticamente ao usuário logado
        Pergunta p = new Pergunta(usuario.getId(), texto, palavras);
        arqPerguntas.create(p);
        System.out.println("Pergunta incluída com sucesso!");
    }

    // ---------------------------------------------------------------- ALTERAR
    private void alterar() throws Exception {
        if (!mostrarLista()) {
            return;
        }

        Pergunta p = escolherPergunta("alterar");
        if (p == null) {
            return;
        }
        if (!p.isAtiva()) {
            System.out.println("Perguntas arquivadas não podem ser alteradas.");
            return;
        }

        System.out.println("\nPergunta atual: " + p.getPergunta());
        String novoTexto = lerTexto("Nova pergunta (linha vazia para finalizar; ENTER direto mantém a atual): ");

        System.out.println("Palavras chave atuais: " + p.getPalavrasChave());
        System.out.print("Novas palavras chave (ENTER mantém as atuais): ");
        String novasPalavras = console.nextLine().trim();

        if (novoTexto.isEmpty() && novasPalavras.isEmpty()) {
            System.out.println("Nada foi alterado.");
            return;
        }

        System.out.print("Confirma a alteração? (S/N): ");
        if (!console.nextLine().trim().equalsIgnoreCase("S")) {
            System.out.println("Alteração cancelada.");
            return;
        }

        // Os setters já atualizam o campo "alteracao"
        if (!novoTexto.isEmpty()) {
            p.setPergunta(novoTexto);
        }
        if (!novasPalavras.isEmpty()) {
            p.setPalavrasChave(novasPalavras);
        }

        if (arqPerguntas.update(p)) {
            System.out.println("Pergunta alterada com sucesso!");
        } else {
            System.out.println("Não foi possível alterar a pergunta.");
        }
    }

    // ---------------------------------------------------------------- ARQUIVAR
    private void arquivar() throws Exception {
        if (!mostrarLista()) {
            return;
        }

        Pergunta p = escolherPergunta("arquivar");
        if (p == null) {
            return;
        }
        if (!p.isAtiva()) {
            System.out.println("Esta pergunta já está arquivada.");
            return;
        }

        System.out.println("\nPergunta: " + p.getPergunta());
        System.out.println("ATENÇÃO: o arquivamento é definitivo e não pode ser desfeito.");
        System.out.print("Confirma o arquivamento? (S/N): ");
        if (!console.nextLine().trim().equalsIgnoreCase("S")) {
            System.out.println("Arquivamento cancelado.");
            return;
        }

        // Usa o ID real (obtido do vetor de associação), nunca o número da tela
        if (arqPerguntas.arquivar(p.getId())) {
            System.out.println("Pergunta arquivada com sucesso!");
        } else {
            System.out.println("Não foi possível arquivar a pergunta.");
        }
    }

    // ---------------------------------------------------------------- AUXILIARES

    /** Pede o número da pergunta na tela e devolve a pergunta correspondente (ou null). */
    private Pergunta escolherPergunta(String acao) throws Exception {
        System.out.print("Número da pergunta que deseja " + acao + " (0 para cancelar): ");
        String entrada = console.nextLine().trim();

        int numero;
        try {
            numero = Integer.parseInt(entrada);
        } catch (NumberFormatException e) {
            System.out.println("Número inválido.");
            return null;
        }

        if (numero == 0) {
            return null;
        }
        if (numero < 1 || numero > idsListados.size()) {
            System.out.println("Número fora da lista.");
            return null;
        }

        // Converte o número da tela no ID real da pergunta
        int idPergunta = idsListados.get(numero - 1);
        Pergunta p = arqPerguntas.read(idPergunta);
        if (p == null || p.getIdUsuario() != usuario.getId()) {
            System.out.println("Pergunta não encontrada.");
            return null;
        }
        return p;
    }

    /** Lê um texto de várias linhas, encerrado por uma linha vazia. */
    private String lerTexto(String mensagem) {
        System.out.println(mensagem);
        StringBuilder sb = new StringBuilder();
        String linha;
        while (!(linha = console.nextLine()).trim().isEmpty()) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(linha);
        }
        String texto = sb.toString().trim();
        if (texto.length() > MAX_CARACTERES) {
            texto = texto.substring(0, MAX_CARACTERES);
            System.out.println("(Texto limitado a " + MAX_CARACTERES + " caracteres.)");
        }
        return texto;
    }
}