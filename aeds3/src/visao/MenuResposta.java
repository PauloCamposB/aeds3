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


public class MenuResposta {

    private static final int MAX_CARACTERES = 5000;

    private final ArquivoPergunta arqPerguntas;
    private final ArquivoResposta arqRespostas;
    private final ArquivoAvaliacao arqAvaliacoes;
    private final ArquivoUsuarios arqUsuarios;
    private final Usuario usuario;
    private final Scanner console;
    private final SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm");

    public MenuResposta(ArquivoPergunta arqPerguntas, ArquivoResposta arqRespostas,
                        ArquivoAvaliacao arqAvaliacoes, ArquivoUsuarios arqUsuarios,
                        Usuario usuario, Scanner console) {
        this.arqPerguntas = arqPerguntas;
        this.arqRespostas = arqRespostas;
        this.arqAvaliacoes = arqAvaliacoes;
        this.arqUsuarios = arqUsuarios;
        this.usuario = usuario;
        this.console = console;
    }

    public void abrirPerguntaBusca(int idPergunta, String caminho) {
        String opcao = "";
        do {
            try {
                Pergunta p = arqPerguntas.read(idPergunta);
                if (p == null) {
                    System.out.println("Pergunta não encontrada.");
                    return;
                }
                cabecalho(caminho);
                mostrarPergunta(p);
                ArrayList<Resposta> respostas = imprimirRespostas(idPergunta);

                System.out.println("(A) Responder à pergunta");
                System.out.println("\n(R) Retornar");
                System.out.print("\nOpção: ");
                opcao = console.nextLine().trim().toUpperCase();

                if (opcao.equals("A")) {
                    responder(p, caminho + " > Responder");
                } else if (!opcao.equals("R")) {
                    Resposta escolhida = escolherDaLista(opcao, respostas);
                    if (escolhida != null) {
                        abrirResposta(escolhida, caminho + " > Resposta", false);
                    }
                }
            } catch (Exception e) {
                System.out.println("Erro: " + e.getMessage());
            }
        } while (!opcao.equals("R"));
    }


    public ArrayList<Resposta> imprimirRespostas(int idPergunta) throws Exception {
        ArrayList<Resposta> respostas = arqRespostas.readByPergunta(idPergunta, true);

        System.out.println("\nRESPOSTAS");
        System.out.println("---------");
        if (respostas.isEmpty()) {
            System.out.println("Ainda não há respostas para esta pergunta.\n");
        }
        int numero = 1;
        for (Resposta r : respostas) {
            System.out.println("(" + numero + ") " + r.getResposta());
            System.out.println("Respondido por " + quemRespondeu(r) + " em " + formato.format(new Date(r.getCriacao())));
            System.out.println("Nota: " + r.getNota() + "  (" + r.getAvaliacoes() + " avaliações)\n");
            numero++;
        }
        return respostas;
    }


    private void responder(Pergunta p, String caminho) throws Exception {
        if (!p.isAtiva()) {
            System.out.println("Esta pergunta está arquivada e não aceita novas respostas.");
            return;
        }
        cabecalho(caminho);
        mostrarPergunta(p);

        // O usuário digita apenas o texto; id, usuário, data, nota e avaliações são automáticos
        String texto = lerTexto("\nResposta (termine com uma linha vazia):");
        if (texto.isEmpty()) {
            System.out.println("A resposta não pode ficar vazia. Operação cancelada.");
            return;
        }
        System.out.print("Confirma o envio da resposta? (S/N): ");
        if (!console.nextLine().trim().equalsIgnoreCase("S")) {
            System.out.println("Operação cancelada.");
            return;
        }
        arqRespostas.create(new Resposta(p.getId(), usuario.getId(), texto));
        System.out.println("Resposta registrada com sucesso!");
    }

    public void abrirResposta(Resposta inicial, String caminho, boolean autorDaPergunta) {
        String opcao = "";
        boolean sair = false;
        do {
            try {
                Resposta r = arqRespostas.read(inicial.getId());
                Pergunta p = arqPerguntas.read(r.getIdPergunta());

                cabecalho(caminho);
                mostrarPergunta(p);
                mostrarResposta(r);

                boolean propria = r.getIdUsuario() == usuario.getId();
                boolean podeArquivar = r.isAtiva() && (propria || autorDaPergunta);
                boolean podeAvaliar = r.isAtiva() && !propria && !autorDaPergunta;

                if (podeArquivar) {
                    System.out.println("(A) Arquivar resposta");
                } else if (podeAvaliar) {
                    System.out.println("(A) Concordar com a resposta");
                    System.out.println("(B) Discordar da resposta");
                }
                System.out.println("\n(R) Retornar ao menu anterior");
                System.out.print("\nOpção: ");
                opcao = console.nextLine().trim().toUpperCase();

                if (opcao.equals("R")) {
                    sair = true;
                } else if (opcao.equals("A") && podeArquivar) {
                    sair = arquivarResposta(r);
                } else if (opcao.equals("A") && podeAvaliar) {
                    avaliar(r, true);
                } else if (opcao.equals("B") && podeAvaliar) {
                    avaliar(r, false);
                } else {
                    System.out.println("Opção inválida!");
                }
            } catch (Exception e) {
                System.out.println("Erro: " + e.getMessage());
            }
        } while (!sair);
    }


    private boolean arquivarResposta(Resposta r) throws Exception {
        System.out.println("ATENÇÃO: o arquivamento é definitivo e não pode ser desfeito.");
        System.out.print("Confirma o arquivamento da resposta? (S/N): ");
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
        if (arqAvaliacoes.jaAvaliou(usuario.getId(), r.getId())) {
            System.out.println("Você já avaliou esta resposta. Para mudar a avaliação, cancele-a em");
            System.out.println("Minha área > Minhas avaliações e avalie novamente.");
            return;
        }
        arqAvaliacoes.create(new Avaliacao(usuario.getId(), r.getId(), positiva));
        System.out.println(positiva ? "Você concordou com a resposta." : "Você discordou da resposta.");
    }


    public void minhasRespostas() {
        String opcao = "";
        do {
            try {
                ArrayList<Resposta> lista = new ArrayList<>();
                for (Resposta r : arqRespostas.readByUsuario(usuario.getId())) {
                    if (r.isAtiva()) {
                        lista.add(r);
                    }
                }
                lista.sort((x, y) -> Long.compare(y.getCriacao(), x.getCriacao())); // mais recentes primeiro

                cabecalho("Início > Minha área > Minhas respostas");
                if (lista.isEmpty()) {
                    System.out.println("Você ainda não possui respostas ativas.");
                    System.out.print("\nPressione ENTER para continuar...");
                    console.nextLine();
                    return;
                }
                int numero = 1;
                for (Resposta r : lista) {
                    Pergunta p = arqPerguntas.read(r.getIdPergunta());
                    System.out.println("(" + numero + ") " + resumo(r.getResposta(), 70));
                    System.out.println("    Pergunta: " + (p != null ? resumo(p.getPergunta(), 60) : "(indisponível)"));
                    System.out.println("    Respondido em " + formato.format(new Date(r.getCriacao()))
                            + " - Nota: " + r.getNota() + " (" + r.getAvaliacoes() + " avaliações)\n");
                    numero++;
                }
                System.out.println("(R) Retornar ao menu anterior");
                System.out.print("\nOpção: ");
                opcao = console.nextLine().trim().toUpperCase();

                if (!opcao.equals("R")) {
                    Resposta escolhida = escolherDaLista(opcao, lista);
                    if (escolhida != null) {
                        abrirResposta(escolhida, "Início > Minha área > Minhas respostas > Resposta", false);
                    }
                }
            } catch (Exception e) {
                System.out.println("Erro: " + e.getMessage());
            }
        } while (!opcao.equals("R"));
    }



    public void minhasAvaliacoes() {
        String opcao = "";
        do {
            try {
                ArrayList<Avaliacao> lista = arqAvaliacoes.readByUsuario(usuario.getId());

                cabecalho("Início > Minha área > Minhas avaliações");
                if (lista.isEmpty()) {
                    System.out.println("Você ainda não avaliou nenhuma resposta.");
                    System.out.print("\nPressione ENTER para continuar...");
                    console.nextLine();
                    return;
                }
                int numero = 1;
                for (Avaliacao a : lista) {
                    Resposta r = arqRespostas.read(a.getIdResposta());
                    String texto = r != null ? resumo(r.getResposta(), 70) : "(resposta indisponível)";
                    String arquivada = (r != null && !r.isAtiva()) ? " [ARQUIVADA]" : "";
                    System.out.println("(" + numero + ") " + texto + arquivada);
                    System.out.println("    Sua avaliação: " + (a.isAvaliacao() ? "positiva" : "negativa") + "\n");
                    numero++;
                }
                System.out.println("(R) Retornar ao menu anterior");
                System.out.print("\nOpção: ");
                opcao = console.nextLine().trim().toUpperCase();

                if (!opcao.equals("R")) {
                    int indice = indiceValido(opcao, lista.size());
                    if (indice >= 0) {
                        abrirAvaliacao(lista.get(indice));
                    }
                }
            } catch (Exception e) {
                System.out.println("Erro: " + e.getMessage());
            }
        } while (!opcao.equals("R"));
    }

    private void abrirAvaliacao(Avaliacao a) throws Exception {
        String opcao = "";
        do {
            Resposta r = arqRespostas.read(a.getIdResposta());
            Pergunta p = r != null ? arqPerguntas.read(r.getIdPergunta()) : null;

            cabecalho("Início > Minha área > Minhas avaliações > Avaliação");
            if (p != null) {
                mostrarPergunta(p);
            }
            if (r != null) {
                mostrarResposta(r);
            }
            System.out.println("Sua avaliação: " + (a.isAvaliacao() ? "positiva (você concordou)" : "negativa (você discordou)"));
            System.out.println("\n(A) Cancelar avaliação");
            System.out.println("\n(R) Retornar ao menu anterior");
            System.out.print("\nOpção: ");
            opcao = console.nextLine().trim().toUpperCase();

            if (opcao.equals("A")) {
                System.out.print("Confirma o cancelamento da avaliação? (S/N): ");
                if (console.nextLine().trim().equalsIgnoreCase("S")) {
  
                    if (arqAvaliacoes.delete(a.getId())) {
                        System.out.println("Avaliação cancelada. A nota da resposta foi ajustada.");
                    } else {
                        System.out.println("Não foi possível cancelar a avaliação.");
                    }
                    return;
                }
                System.out.println("Cancelamento abortado.");
            } else if (!opcao.equals("R")) {
                System.out.println("Opção inválida!");
            }
        } while (!opcao.equals("R"));
    }

 

    private void cabecalho(String caminho) {
        System.out.println("\nAJUDA AÍ 1.0");
        System.out.println("------------");
        System.out.println("\n> " + caminho + "\n");
    }

    private void mostrarPergunta(Pergunta p) throws Exception {
        System.out.println(p.getPergunta());
        System.out.println("Criada por " + nome(p.getIdUsuario()) + " em " + formato.format(new Date(p.getCriacao())));
    }

    private void mostrarResposta(Resposta r) throws Exception {
        System.out.println("\nRESPOSTA");
        System.out.println("--------\n");
        System.out.println(r.getResposta());
        System.out.println("Respondido por " + quemRespondeu(r) + " em " + formato.format(new Date(r.getCriacao())));
        System.out.println("Nota: " + r.getNota() + "  (" + r.getAvaliacoes() + " avaliações)");
        if (!r.isAtiva()) {
            System.out.println("(RESPOSTA ARQUIVADA)");
        }
        System.out.println();
    }

    private String quemRespondeu(Resposta r) throws Exception {
        return r.getIdUsuario() == usuario.getId() ? "você" : nome(r.getIdUsuario());
    }

    private String nome(int idUsuario) throws Exception {
        Usuario u = arqUsuarios.read(idUsuario);
        return u != null ? u.getNome() : "usuário desconhecido";
    }

    /** Converte o número digitado em posição na lista; devolve -1 (com aviso) se for inválido. */
    private int indiceValido(String entrada, int tamanho) {
        try {
            int n = Integer.parseInt(entrada);
            if (n >= 1 && n <= tamanho) {
                return n - 1;
            }
            System.out.println("Número fora da lista.");
        } catch (NumberFormatException e) {
            System.out.println("Opção inválida!");
        }
        return -1;
    }

    private Resposta escolherDaLista(String entrada, ArrayList<Resposta> lista) {
        int i = indiceValido(entrada, lista.size());
        return i >= 0 ? lista.get(i) : null;
    }

    private String resumo(String texto, int max) {
        String t = texto.replace("\n", " ").trim();
        return t.length() > max ? t.substring(0, max - 3) + "..." : t;
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