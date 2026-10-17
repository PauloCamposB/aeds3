package visao;

import dados.ArquivoAvaliacao;
import dados.ArquivoPergunta;
import dados.ArquivoResposta;
import dados.ArquivoUsuarios;
import entidades.Avaliacao;
import entidades.Pergunta;
import entidades.Resposta;
import entidades.Usuario;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

/**
 * Visão e controle das respostas e avaliações.
 * Tela de pergunta com a lista de respostas (ordenadas por nota), tela de
 * resposta individual (avaliar/arquivar), Minhas respostas e Minhas avaliações.
 */
public class MenuRespostas {

    private static final int MAX_CARACTERES = 5000; // evita estourar o limite de tamanho do registro

    private final ArquivoUsuarios arqUsuarios;
    private final ArquivoPergunta arqPerguntas;
    private final ArquivoResposta arqRespostas;
    private final ArquivoAvaliacao arqAvaliacoes;
    private final Usuario usuario;
    private final Scanner console;
    private final SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm");

    public MenuRespostas(ArquivoUsuarios arqUsuarios, ArquivoPergunta arqPerguntas, ArquivoResposta arqRespostas,
                         ArquivoAvaliacao arqAvaliacoes, Usuario usuario, Scanner console) {
        this.arqUsuarios = arqUsuarios;
        this.arqPerguntas = arqPerguntas;
        this.arqRespostas = arqRespostas;
        this.arqAvaliacoes = arqAvaliacoes;
        this.usuario = usuario;
        this.console = console;
    }

    // ---------------------------------------------------------------- PERGUNTA

    /** Tela de uma pergunta com suas respostas, aberta a partir da busca. */
    public void abrirPergunta(int idPergunta, String caminho) throws Exception {
        String breadcrumb = caminho + " > Pergunta";
        while (true) {
            // Relê a pergunta a cada volta: pode ter sido arquivada pelo autor
            Pergunta p = arqPerguntas.read(idPergunta);
            if (p == null || !p.isAtiva()) {
                System.out.println("Pergunta não encontrada.");
                return;
            }

            cabecalho(breadcrumb);
            mostrarPergunta(p);
            ArrayList<Resposta> respostas = mostrarRespostas(p);

            if (!respostas.isEmpty()) {
                System.out.println("Digite o número da resposta para abri-la");
            }
            System.out.println("(A) Responder à pergunta");
            System.out.println("\n(R) Retornar");
            System.out.print("\nOpção: ");
            String entrada = console.nextLine().trim().toUpperCase();

            if (entrada.equals("R")) {
                return;
            } else if (entrada.equals("A")) {
                responder(p, breadcrumb);
            } else {
                Resposta r = escolherResposta(entrada, respostas);
                if (r != null) {
                    abrirResposta(p, r, breadcrumb, false);
                }
            }
        }
    }

    /**
     * Mostra as respostas ativas da pergunta em ordem decrescente de nota.
     * Devolve a lista na mesma ordem da tela (número da tela -> resposta).
     */
    public ArrayList<Resposta> mostrarRespostas(Pergunta p) throws Exception {
        ArrayList<Resposta> respostas = arqRespostas.readByPergunta(p.getId(), true);

        // Nota decrescente; em caso de empate, a resposta mais antiga vem primeiro
        respostas.sort((a, b) -> {
            if (a.getNota() != b.getNota()) {
                return Short.compare(b.getNota(), a.getNota());
            }
            return Long.compare(a.getCriacao(), b.getCriacao());
        });

        System.out.println("\nRESPOSTAS");
        System.out.println("---------");

        if (respostas.isEmpty()) {
            System.out.println("Ainda não há respostas para esta pergunta.\n");
            return respostas;
        }

        int numero = 1;
        for (Resposta r : respostas) {
            System.out.println("(" + numero + ") " + r.getResposta());
            System.out.println("Respondido por " + nomeAutor(r.getIdUsuario()) + " em " + formato.format(new Date(r.getCriacao())));
            System.out.println("Nota: " + r.getNota() + "  (" + r.getAvaliacoes() + " avaliações)");
            System.out.println();
            numero++;
        }
        return respostas;
    }

    // ---------------------------------------------------------------- RESPONDER
    private void responder(Pergunta p, String caminho) throws Exception {
        cabecalho(caminho + " > Responder");
        mostrarPergunta(p);

        String texto = lerTexto("\nResposta (termine com uma linha vazia): ");
        if (texto.isEmpty()) {
            System.out.println("A resposta não pode ficar vazia. Operação cancelada.");
            return;
        }

        System.out.print("Confirma o envio da resposta? (S/N): ");
        if (!console.nextLine().trim().equalsIgnoreCase("S")) {
            System.out.println("Resposta cancelada.");
            return;
        }

        // Pergunta, autor, data, nota e avaliações são preenchidos automaticamente
        Resposta r = new Resposta(p.getId(), usuario.getId(), texto);
        try {
            arqRespostas.create(r);
            System.out.println("Resposta registrada com sucesso!");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------- RESPOSTA

    /**
     * Tela de uma resposta. Se a resposta for do próprio usuário, a única opção é
     * arquivá-la. Com moderacao = true (Minha área > Minhas perguntas), o autor da
     * pergunta também pode arquivar respostas de outros usuários. Nos demais casos,
     * o usuário pode concordar ou discordar da resposta (uma única vez).
     */
    public void abrirResposta(Pergunta p, Resposta resposta, String caminho, boolean moderacao) throws Exception {
        String breadcrumb = caminho + " > Resposta";
        while (true) {
            // Relê a resposta a cada volta para mostrar a nota atualizada
            Resposta r = arqRespostas.read(resposta.getId());
            if (r == null || !r.isAtiva()) {
                return;
            }

            boolean propria = r.getIdUsuario() == usuario.getId();
            boolean podeArquivar = propria || (moderacao && p.getIdUsuario() == usuario.getId());
            Avaliacao avaliacao = podeArquivar ? null : buscarAvaliacao(r.getId());

            cabecalho(breadcrumb);
            mostrarPergunta(p);
            System.out.println("\nRESPOSTA");
            System.out.println("--------\n");
            mostrarResposta(r);

            if (podeArquivar) {
                System.out.println("\n(A) Arquivar resposta");
            } else if (avaliacao == null) {
                System.out.println("\n(A) Concordar com a resposta");
                System.out.println("(B) Discordar da resposta");
            } else {
                System.out.println("\nVocê já avaliou esta resposta: " + descreverAvaliacao(avaliacao));
                System.out.println("Para mudar, cancele a avaliação em Minha área > Minhas avaliações.");
            }
            System.out.println("\n(R) Retornar ao menu anterior");
            System.out.print("\nOpção: ");
            String opcao = console.nextLine().trim().toUpperCase();

            if (opcao.equals("R")) {
                return;
            } else if (opcao.equals("A") && podeArquivar) {
                if (arquivarResposta(r)) {
                    return; // resposta arquivada não aparece mais
                }
            } else if (opcao.equals("A") && avaliacao == null) {
                avaliar(r, true);
            } else if (opcao.equals("B") && !podeArquivar && avaliacao == null) {
                avaliar(r, false);
            } else {
                System.out.println("Opção inválida!");
            }
        }
    }

    private boolean arquivarResposta(Resposta r) throws Exception {
        System.out.println("ATENÇÃO: o arquivamento é definitivo e não pode ser desfeito.");
        System.out.print("Confirma o arquivamento? (S/N): ");
        if (!console.nextLine().trim().equalsIgnoreCase("S")) {
            System.out.println("Arquivamento cancelado.");
            return false;
        }

        if (arqRespostas.arquivar(r.getId())) {
            System.out.println("Resposta arquivada com sucesso!");
            return true;
        }
        System.out.println("Não foi possível arquivar a resposta.");
        return false;
    }

    private void avaliar(Resposta r, boolean positiva) throws Exception {
        // O ArquivoAvaliacao ajusta a nota e a contagem de avaliações da resposta
        try {
            arqAvaliacoes.create(new Avaliacao(usuario.getId(), r.getId(), positiva));
            System.out.println("Avaliação registrada com sucesso!");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------- MINHAS RESPOSTAS
    public void minhasRespostas() {
        String caminho = "> Início > Minha área > Minhas respostas";
        try {
            while (true) {
                ArrayList<Resposta> respostas = new ArrayList<>();
                for (Resposta r : arqRespostas.readByUsuario(usuario.getId())) {
                    if (r.isAtiva()) {
                        respostas.add(r);
                    }
                }
                // Da mais recente para a mais antiga
                respostas.sort((a, b) -> Long.compare(b.getCriacao(), a.getCriacao()));

                cabecalho(caminho);

                if (respostas.isEmpty()) {
                    System.out.println("Você ainda não respondeu nenhuma pergunta.");
                    System.out.print("\nPressione ENTER para continuar...");
                    console.nextLine();
                    return;
                }

                int numero = 1;
                for (Resposta r : respostas) {
                    Pergunta p = arqPerguntas.read(r.getIdPergunta());
                    System.out.println("(" + numero + ") " + r.getResposta());
                    System.out.println("Pergunta: " + (p != null ? p.getPergunta() : "(indisponível)"));
                    System.out.println("Respondido em " + formato.format(new Date(r.getCriacao())));
                    System.out.println("Nota: " + r.getNota() + "  (" + r.getAvaliacoes() + " avaliações)");
                    System.out.println();
                    numero++;
                }

                System.out.println("Digite o número da resposta para abri-la");
                System.out.println("\n(R) Retornar ao menu anterior");
                System.out.print("\nOpção: ");
                String entrada = console.nextLine().trim().toUpperCase();

                if (entrada.equals("R")) {
                    return;
                }
                Resposta r = escolherResposta(entrada, respostas);
                if (r != null) {
                    Pergunta p = arqPerguntas.read(r.getIdPergunta());
                    if (p == null) {
                        System.out.println("Pergunta não encontrada.");
                    } else {
                        // Como a resposta é do próprio usuário, a única opção será arquivá-la
                        abrirResposta(p, r, caminho, false);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------- MINHAS AVALIAÇÕES
    public void minhasAvaliacoes() {
        String caminho = "> Início > Minha área > Minhas avaliações";
        try {
            while (true) {
                ArrayList<Avaliacao> avaliacoes = arqAvaliacoes.readByUsuario(usuario.getId());

                cabecalho(caminho);

                if (avaliacoes.isEmpty()) {
                    System.out.println("Você ainda não avaliou nenhuma resposta.");
                    System.out.print("\nPressione ENTER para continuar...");
                    console.nextLine();
                    return;
                }

                int numero = 1;
                for (Avaliacao a : avaliacoes) {
                    Resposta r = arqRespostas.read(a.getIdResposta());
                    System.out.print("(" + numero + ") ");
                    if (r == null) {
                        System.out.println("(resposta indisponível)");
                    } else {
                        if (!r.isAtiva()) {
                            System.out.print("ARQUIVADA - ");
                        }
                        System.out.println(r.getResposta());
                        System.out.println("Respondido por " + nomeAutor(r.getIdUsuario()) + " em " + formato.format(new Date(r.getCriacao())));
                    }
                    System.out.println("Sua avaliação: " + descreverAvaliacao(a));
                    System.out.println();
                    numero++;
                }

                System.out.println("Digite o número da avaliação para abri-la");
                System.out.println("\n(R) Retornar ao menu anterior");
                System.out.print("\nOpção: ");
                String entrada = console.nextLine().trim().toUpperCase();

                if (entrada.equals("R")) {
                    return;
                }
                Integer indice = escolherNumero(entrada, avaliacoes.size());
                if (indice != null) {
                    abrirAvaliacao(avaliacoes.get(indice), caminho);
                }
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    /** Mostra a pergunta, a resposta avaliada e a avaliação; a única ação é cancelá-la. */
    private void abrirAvaliacao(Avaliacao a, String caminho) throws Exception {
        Resposta r = arqRespostas.read(a.getIdResposta());
        Pergunta p = (r != null) ? arqPerguntas.read(r.getIdPergunta()) : null;

        String opcao;
        do {
            cabecalho(caminho + " > Avaliação");
            if (p != null) {
                mostrarPergunta(p);
            }
            System.out.println("\nRESPOSTA");
            System.out.println("--------\n");
            if (r != null) {
                mostrarResposta(r);
            } else {
                System.out.println("(resposta indisponível)");
            }
            System.out.println("\nSUA AVALIAÇÃO: " + descreverAvaliacao(a));

            System.out.println("\n(A) Cancelar avaliação");
            System.out.println("\n(R) Retornar ao menu anterior");
            System.out.print("\nOpção: ");
            opcao = console.nextLine().trim().toUpperCase();

            switch (opcao) {
                case "A":
                    if (cancelarAvaliacao(a)) {
                        return;
                    }
                    break;
                case "R": break;
                default: System.out.println("Opção inválida!");
            }
        } while (!opcao.equals("R"));
    }

    private boolean cancelarAvaliacao(Avaliacao a) throws Exception {
        System.out.print("Confirma o cancelamento da avaliação? (S/N): ");
        if (!console.nextLine().trim().equalsIgnoreCase("S")) {
            System.out.println("Cancelamento desfeito.");
            return false;
        }

        // O delete remove a avaliação dos dois índices e desfaz seu efeito na nota da resposta
        if (arqAvaliacoes.delete(a.getId())) {
            System.out.println("Avaliação cancelada! A nota da resposta foi ajustada.");
            return true;
        }
        System.out.println("Não foi possível cancelar a avaliação.");
        return false;
    }

    // ---------------------------------------------------------------- AUXILIARES

    private void cabecalho(String breadcrumb) {
        System.out.println("\nAJUDA AÍ 1.0");
        System.out.println("------------");
        System.out.println("\n" + breadcrumb + "\n");
    }

    private void mostrarPergunta(Pergunta p) throws Exception {
        System.out.println(p.getPergunta());
        System.out.println("Criada por " + nomeAutor(p.getIdUsuario()) + " em " + formato.format(new Date(p.getCriacao())));
    }

    private void mostrarResposta(Resposta r) throws Exception {
        System.out.println(r.getResposta());
        System.out.println("Respondido por " + nomeAutor(r.getIdUsuario()) + " em " + formato.format(new Date(r.getCriacao())));
        System.out.println("Nota: " + r.getNota() + "  (" + r.getAvaliacoes() + " avaliações)");
    }

    private String nomeAutor(int idUsuario) throws Exception {
        if (idUsuario == usuario.getId()) {
            return "você";
        }
        Usuario u = arqUsuarios.read(idUsuario);
        return (u != null) ? u.getNome() : "(usuário desconhecido)";
    }

    private String descreverAvaliacao(Avaliacao a) {
        return a.isAvaliacao() ? "positiva (concordou)" : "negativa (discordou)";
    }

    /** Avaliação do usuário logado para a resposta, ou null se ainda não avaliou. */
    private Avaliacao buscarAvaliacao(int idResposta) throws Exception {
        for (Avaliacao a : arqAvaliacoes.readByUsuario(usuario.getId())) {
            if (a.getIdResposta() == idResposta) {
                return a;
            }
        }
        return null;
    }

    /** Converte o número digitado na tela na resposta correspondente (ou null). */
    public Resposta escolherResposta(String entrada, ArrayList<Resposta> respostas) {
        Integer indice = escolherNumero(entrada, respostas.size());
        return (indice != null) ? respostas.get(indice) : null;
    }

    /** Converte o número digitado (1..total) em um índice da lista (0..total-1), ou null. */
    private Integer escolherNumero(String entrada, int total) {
        int numero;
        try {
            numero = Integer.parseInt(entrada);
        } catch (NumberFormatException e) {
            System.out.println("Opção inválida!");
            return null;
        }
        if (numero < 1 || numero > total) {
            System.out.println("Número fora da lista.");
            return null;
        }
        return numero - 1;
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
