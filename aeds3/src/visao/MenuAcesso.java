package visao;

import dados.ArquivoPergunta;
import dados.ArquivoUsuarios;
import entidades.Pergunta;
import entidades.Usuario;
import java.util.ArrayList;
import java.util.Scanner;
import util.Seguranca;

public class MenuAcesso {

    private ArquivoUsuarios arqUsuarios;
    private ArquivoPergunta arqPerguntas;
    private static Scanner console = new Scanner(System.in);

    public MenuAcesso() throws Exception {
        arqUsuarios = new ArquivoUsuarios();
        arqPerguntas = new ArquivoPergunta();
    }

    public Usuario inicio() throws Exception {
        String opcao = "";
        do {
            System.out.println("\n-----------------------------");
            System.out.println("AJUDA AÍ 1.0");
            System.out.println("-----------------------------");
            System.out.println("1) Acesso ao Sistema (Login)");
            System.out.println("2) Novo Usuário (Primeiro Acesso)");
            System.out.println("S) Sair");
            System.out.print("Opção: ");
            opcao = console.nextLine().trim().toUpperCase();

            try {
                switch (opcao) {
                    case "1":
                        login();
                        break;
                    case "2":
                        novoUsuario();
                        break;
                    case "S":
                        System.out.println("Saindo do sistema...");
                        break;
                    default:
                        System.out.println("Opção inválida!");
                }
            } catch (Exception e) {
                System.out.println("Erro: " + e.getMessage());
            }
        } while (!opcao.equals("S"));

        return null;
    }

    private void novoUsuario() throws Exception {
        System.out.println("\n--- NOVO USUÁRIO ---");
        System.out.print("Email: ");
        String email = console.nextLine().trim();

        if (arqUsuarios.readByEmail(email) != null) {
            System.out.println("Erro: E-mail já cadastrado no sistema!");
            return;
        }

        System.out.print("Nome: ");
        String nome = console.nextLine().trim();
        System.out.print("Senha: ");
        String senha = console.nextLine().trim();
        System.out.print("Pergunta Secreta (para recuperação): ");
        String pergunta = console.nextLine().trim();
        System.out.print("Resposta Secreta: ");
        String resposta = console.nextLine().trim();

        Usuario u = new Usuario(nome, email, senha, pergunta, resposta);
        int id = arqUsuarios.create(u);
        System.out.println("\nUsuário cadastrado com sucesso (ID " + id + ")! Faça o primeiro acesso.");
    }

    private void login() throws Exception {
        System.out.println("\n--- ACESSO AO SISTEMA ---");
        System.out.print("Email: ");
        String email = console.nextLine().trim();
        System.out.print("Senha: ");
        String senha = console.nextLine().trim();

        Usuario usuarioLogado = arqUsuarios.autenticar(email, senha);
        if (usuarioLogado != null) {
            System.out.println("\nLogin realizado com sucesso! Bem-vindo(a), " + usuarioLogado.getNome() + ".");
            menuPrincipal(usuarioLogado);
        } else {
            System.out.println("\nE-mail ou senha incorretos.");
            System.out.print("Deseja tentar recuperar a senha? (S/N): ");
            String op = console.nextLine().trim();
            if (op.equalsIgnoreCase("S")) {
                recuperarSenha(email);
            }
        }
    }

    private void recuperarSenha(String emailInformado) throws Exception {
        System.out.println("\n--- RECUPERAÇÃO DE SENHA ---");
        Usuario u = arqUsuarios.readByEmail(emailInformado);
        if (u == null) {
            System.out.print("Informe o e-mail cadastrado: ");
            String email = console.nextLine().trim();
            u = arqUsuarios.readByEmail(email);
        }

        if (u == null) {
            System.out.println("Erro: E-mail não encontrado.");
            return;
        }

        System.out.println("Pergunta Secreta: " + u.getPerguntaSecreta());
        System.out.print("Sua Resposta: ");
        String respostaDigitada = console.nextLine().trim();

        String hashResp = Seguranca.hashSHA256(respostaDigitada);
        if (u.getHashRespostaSecreta().equals(hashResp)) {
            System.out.print("Resposta correta! Digite a nova senha: ");
            String novaSenha = console.nextLine().trim();
            arqUsuarios.atualizarSenha(u, novaSenha);
            System.out.println("Senha redefinida com sucesso! Faça login novamente.");
        } else {
            System.out.println("Resposta incorreta! Não foi possível redefinir a senha.");
        }
    }

    private void menuPrincipal(Usuario usuarioLogado) throws Exception {
        String opcao = "";
        do {
            System.out.println("\nAJUDA AÍ 1.0");
            System.out.println("> Inicio");
            System.out.println("(A) Minha área");
            System.out.println("(B) Buscar perguntas");
            System.out.println("(S) Sair");
            System.out.print("Opção: ");
            opcao = console.nextLine().trim().toUpperCase();

            switch (opcao) {
                case "A":
                    minhaArea(usuarioLogado);
                    break;
                case "B":
                    System.out.println("Funcionalidade reservada para a próxima etapa (TP02).");
                    break;
                case "S":
                    System.out.println("Saindo da área pessoal...");
                    break;
                default:
                    System.out.println("Opção inválida!");
            }
        } while (!opcao.equals("S"));
    }

    private void minhaArea(Usuario usuarioLogado) throws Exception {
        String opcao = "";
        do {
            System.out.println("\nAJUDA AÍ 1.0");
            System.out.println("> Inicio > Minha área");
            System.out.println("(A) Meus dados");
            System.out.println("(B) Minhas perguntas");
            System.out.println("(C) Minhas respostas");
            System.out.println("(D) Meus votos");
            System.out.println("(R) Retornar ao menu anterior");
            System.out.print("Opção: ");
            opcao = console.nextLine().trim().toUpperCase();

            switch (opcao) {
                case "A":
                    meusDados(usuarioLogado);
                    break;
                case "B":
                    System.out.println("Redirecionando para a área de perguntas...");
                    minhasPerguntas(usuarioLogado);
                    break;
                case "C":
                    System.out.println("Opção não disponível nesta etapa.");
                case "D":
                    System.out.println("Opção não disponível nesta etapa.");
                    break;
                case "R":
                    break;
                default:
                    System.out.println("Opção inválida!");
            }
        } while (!opcao.equals("R"));
    }

    private void meusDados(Usuario usuarioLogado) throws Exception {
        String opcao = "";
        do {
            System.out.println("\nAJUDA AÍ 1.0");
            System.out.println("> Inicio > Minha área > Meus dados");
            System.out.println("(A) Alterar nome");
            System.out.println("(B) Alterar email");
            System.out.println("(C) Alterar senha");
            System.out.println("(D) Alterar pergunta e resposta de recuperação da senha");
            System.out.println("(R) Retornar ao menu anterior");
            System.out.print("Opção: ");
            opcao = console.nextLine().trim().toUpperCase();

            switch (opcao) {
                case "A":
                    System.out.print("Novo Nome: ");
                    String novoNome = console.nextLine().trim();
                    if (!novoNome.isEmpty()) {
                        usuarioLogado.setNome(novoNome);
                        arqUsuarios.update(usuarioLogado);
                        System.out.println("Nome atualizado com sucesso!");
                    }
                    break;

                case "B":
                    System.out.print("Novo E-mail: ");
                    String novoEmail = console.nextLine().trim();
                    if (!novoEmail.isEmpty()) {
                        String emailAntigo = usuarioLogado.getEmail();
                        arqUsuarios.atualizarEmail(usuarioLogado, emailAntigo, novoEmail);
                        System.out.println("E-mail e índice de busca atualizados com sucesso!");
                    }
                    break;

                case "C":
                    System.out.print("Nova Senha: ");
                    String novaSenha = console.nextLine().trim();
                    if (!novaSenha.isEmpty()) {
                        arqUsuarios.atualizarSenha(usuarioLogado, novaSenha);
                        System.out.println("Senha atualizada com sucesso!");
                    }
                    break;

                case "D":
                    System.out.print("Nova Pergunta Secreta: ");
                    String novaP = console.nextLine().trim();
                    System.out.print("Nova Resposta Secreta: ");
                    String novaR = console.nextLine().trim();
                    if (!novaP.isEmpty() && !novaR.isEmpty()) {
                        arqUsuarios.atualizarPerguntaEAntena(usuarioLogado, novaP, novaR);
                        System.out.println("Pergunta e resposta de segurança atualizadas com sucesso!");
                    }
                    break;

                case "R":
                    break;
                default:
                    System.out.println("Opção inválida!");
            }
        } while (!opcao.equals("R"));
    }

    private void minhasPerguntas(Usuario usuarioLogado) {
        String opcao = "";
        do {
            System.out.println("\nAJUDA AÍ 1.0");
            System.out.println("> Inicio > Minha área > Minhas perguntas");
            System.out.println("(A) Listar");
            System.out.println("(B) Incluir");
            System.out.println("(C) Alterar");
            System.out.println("(D) Arquivar");
            System.out.println("");
            System.out.println("(R) Retornar ao menu anterior");
            System.out.println("");
            System.out.print("Opção: ");
            opcao = console.nextLine().trim().toUpperCase();

            switch (opcao) {
                case "A":
                    listagemMinhasPerguntas(usuarioLogado);
                    break;
                case "B":
                    System.out.println("\nAJUDA AÍ 1.0");
                    System.out.println("> Inicio > Minha área > Minhas perguntas > Incluir");
                    System.out.print("Nova Pergunta: ");
                    String novaPergunta = console.nextLine().trim();
                    System.out.print("Palavras Chave (separadas por espaço): ");
                    String palavrasChave = console.nextLine().trim();
                    palavrasChave = palavrasChave.replace(" ", ";").toLowerCase();

                    Pergunta novaPerguntaObj = new Pergunta(usuarioLogado.getId(), novaPergunta, palavrasChave);
                    try {
                        arqPerguntas.create(novaPerguntaObj);
                    } catch (Exception e) {
                        System.out.println("Erro ao incluir a pergunta: " + e.getMessage());
                    }
                    System.out.println("Pergunta incluída com sucesso!");
                    break;
                case "C":
                    alteracaoMinhasPerguntas(usuarioLogado);
                    break;
                case "D":
                    arquivamentoMinhasPerguntas(usuarioLogado);
                    break;
                case "R":

                    break;
                default:
                    System.out.println("Opção inválida!");
            }
        } while (!opcao.equals("R"));
    }

    // Página interna de listagem de perguntas com sistema de paginação
    private void listagemMinhasPerguntas(Usuario usuarioLogado) {
        ArrayList<Pergunta> minhasPerguntas = new ArrayList<>();
        ArrayList<Pergunta> perguntasPaginaAtual = new ArrayList<>();
        int paginaAtual = 0;
        String opcao;

        // Puxa uma lista das perguntas
        try {
            minhasPerguntas = arqPerguntas.findByUsuario(usuarioLogado.getId(), 0);
        } catch (Exception e) {
            System.out.println("Erro ao listar minhas perguntas: " + e.getMessage());
            return;
        }

        // Verifica se existe alguma pergunta cadastrada
        if (minhasPerguntas.size() <= 0) {
            System.out.println("Você não possui perguntas cadastradas!");
            return;
        }

        do {
            int inicio = paginaAtual * 10;
            int fim = Math.min(inicio + 10, minhasPerguntas.size());
            perguntasPaginaAtual = new ArrayList<>(minhasPerguntas.subList(inicio, fim));
            System.out.println("\nAJUDA AÍ 1.0");
            System.out.println("> Inicio > Minha área > Minhas perguntas > Listar");
            int counter = inicio;

            // Lista as perguntas
            for (Pergunta pergunta : perguntasPaginaAtual) {
                System.out.print(counter + " | " + pergunta.getId() + " | ");
                if (pergunta.isAtiva()) {
                    System.out.print("    Ativa | ");
                } else {
                    System.out.print("Arquivada | ");
                }
                System.out.print(pergunta.getPergunta() + "\n");
                counter++;
            }

            System.out.println("");
            System.out.println("(P) Próxima página");
            System.out.println("(A) Página Anterior");
            System.out.println(
                    "Página " + (paginaAtual + 1) + " de " + (int) (Math.floor(minhasPerguntas.size() / 10) + 1));
            System.out.println("");
            System.out.println("(R) Retornar ao menu anterior");
            System.out.println("");
            System.out.print("Opção: ");
            opcao = console.nextLine().trim().toUpperCase();

            switch (opcao) {
                case "P":
                    if (minhasPerguntas.size() < (paginaAtual + 1) * 10) {
                        System.out.println("Não há próxima página!");
                    } else {
                        paginaAtual++;
                    }
                    break;
                case "A":
                    if (paginaAtual <= 0) {
                        System.out.println("Esta é a primeira página!");
                    } else {
                        paginaAtual--;
                    }
                    break;
                case "R":
                    break;
                default:
                    System.out.println("Opção inválida!");
            }

        } while (!opcao.equals("R"));
    }

    private void alteracaoMinhasPerguntas(Usuario usuarioLogado) {
        ArrayList<Pergunta> minhasPerguntas = new ArrayList<>();
        ArrayList<Pergunta> perguntasPaginaAtual = new ArrayList<>();
        int paginaAtual = 0;
        String opcao;

        // Puxa uma lista das perguntas
        try {
            minhasPerguntas = arqPerguntas.findActiveByUsuario(usuarioLogado.getId(), 0);
        } catch (Exception e) {
            System.out.println("Erro ao listar minhas perguntas: " + e.getMessage());
            return;
        }

        // Verifica se existe alguma pergunta cadastrada
        if (minhasPerguntas.size() <= 0) {
            System.out.println("Você não possui perguntas cadastradas!");
            return;
        }

        do {
            int inicio = paginaAtual * 10;
            int fim = Math.min(inicio + 10, minhasPerguntas.size());
            perguntasPaginaAtual = new ArrayList<>(minhasPerguntas.subList(inicio, fim));
            System.out.println("\nAJUDA AÍ 1.0");
            System.out.println("> Inicio > Minha área > Minhas perguntas > Alterar");
            System.out.println("Selecione o número da pergunta que você deseja alterar:");
            int counter = inicio;

            // Lista as perguntas
            for (Pergunta pergunta : perguntasPaginaAtual) {
                System.out.println("(" + (counter + 1) + ") | " + pergunta.getPergunta());
                counter++;
            }

            System.out.println("");
            System.out.println("(P) Próxima página");
            System.out.println("(A) Página Anterior");
            System.out.println(
                    "Página " + (paginaAtual + 1) + " de " + (int) (Math.floor(minhasPerguntas.size() / 10) + 1));
            System.out.println("");
            System.out.println("(R) Retornar ao menu anterior");
            System.out.println("");
            System.out.print("Opção: ");
            opcao = console.nextLine().trim().toUpperCase();

            if (!isInt(opcao)) {
                switch (opcao) {
                    case "P":
                        if (minhasPerguntas.size() < (paginaAtual + 1) * 10) {
                            System.out.println("Não há próxima página!");
                        } else {
                            paginaAtual++;
                        }
                        break;
                    case "A":
                        if (paginaAtual <= 0) {
                            System.out.println("Esta é a primeira página!");
                        } else {
                            paginaAtual--;
                        }
                        break;
                    case "R":
                        break;
                    default:
                        System.out.println("Opção inválida!");
                }
            } else {
                String opcaoAlteracao;
                do {
                    System.out.println("O que deseja alterar?");
                    System.out.println("(1) Alterar Pergunta");
                    System.out.println("(2) Alterar palavras-chave");
                    System.out.println("(R) Retornar ao menu anterior");
                    opcaoAlteracao = console.nextLine().trim().toUpperCase();
                    if (isInt(opcaoAlteracao)) {
                        int index = Integer.parseInt(opcao);
                        if (index >= 0 && index < minhasPerguntas.size()) {
                            Pergunta perguntaSelecionada = minhasPerguntas.get(index);
                            change(perguntaSelecionada, Integer.parseInt(opcaoAlteracao));
                        } else {
                            System.out.println("Número inválido!");
                        }

                    }
                } while (!opcaoAlteracao.equals("R"));
            }

        } while (!opcao.equals("R"));
    }

    private void change(Pergunta pergunta, int opcaoAlteracao) {
        switch (opcaoAlteracao) {
            case 1:
                System.out.print("Digite a nova pergunta: ");
                String novaPergunta = console.nextLine().trim();
                pergunta.setPergunta(novaPergunta);
                try {
                    arqPerguntas.update(pergunta);
                    System.out.println("Pergunta atualizada com sucesso!");
                } catch (Exception e) {
                    System.out.println("Erro ao atualizar a pergunta: " + e.getMessage());
                }
                break;
            case 2:
                System.out.print("Digite as novas palavras-chave (separadas por espaço): ");
                String novasPalavrasChave = console.nextLine().trim().replace(" ", ";");
                pergunta.setPalavrasChave(novasPalavrasChave);
                try {
                    arqPerguntas.update(pergunta);
                    System.out.println("Palavras-chave atualizadas com sucesso!");
                } catch (Exception e) {
                    System.out.println("Erro ao atualizar as palavras-chave: " + e.getMessage());
                }
                break;
            default:
                System.out.println("Opção inválida!");
        }
    }

    private void arquivamentoMinhasPerguntas(Usuario usuarioLogado) {
        ArrayList<Pergunta> minhasPerguntas = new ArrayList<>();
        ArrayList<Pergunta> perguntasPaginaAtual = new ArrayList<>();
        int paginaAtual = 0;
        String opcao;

        // Puxa uma lista das perguntas
        try {
            minhasPerguntas = arqPerguntas.findActiveByUsuario(usuarioLogado.getId(), 0);
        } catch (Exception e) {
            System.out.println("Erro ao listar minhas perguntas: " + e.getMessage());
            return;
        }

        // Verifica se existe alguma pergunta cadastrada
        if (minhasPerguntas.size() <= 0) {
            System.out.println("Você não possui perguntas cadastradas!");
            return;
        }

        do {
            int inicio = paginaAtual * 10;
            int fim = Math.min(inicio + 10, minhasPerguntas.size());
            perguntasPaginaAtual = new ArrayList<>(minhasPerguntas.subList(inicio, fim));
            System.out.println("\nAJUDA AÍ 1.0");
            System.out.println("> Inicio > Minha área > Minhas perguntas > Alterar");
            System.out.println("Selecione o número da pergunta que você deseja arquivar:");
            int counter = inicio;

            // Lista as perguntas
            for (Pergunta pergunta : perguntasPaginaAtual) {
                System.out.println("(" + (counter + 1) + ") | " + pergunta.getPergunta());
                counter++;
            }

            System.out.println("");
            System.out.println("(P) Próxima página");
            System.out.println("(A) Página Anterior");
            System.out.println(
                    "Página " + (paginaAtual + 1) + " de " + (int) (Math.floor(minhasPerguntas.size() / 10) + 1));
            System.out.println("");
            System.out.println("(R) Retornar ao menu anterior");
            System.out.println("");
            System.out.print("Opção: ");
            opcao = console.nextLine().trim().toUpperCase();

            if (!isInt(opcao)) {
                switch (opcao) {
                    case "P":
                        if (minhasPerguntas.size() < (paginaAtual + 1) * 10) {
                            System.out.println("Não há próxima página!");
                        } else {
                            paginaAtual++;
                        }
                        break;
                    case "A":
                        if (paginaAtual <= 0) {
                            System.out.println("Esta é a primeira página!");
                        } else {
                            paginaAtual--;
                        }
                        break;
                    case "R":
                        break;
                    default:
                        System.out.println("Opção inválida!");
                }
            } else {
                String opcaoArquivamento;
                System.out.println("Tem certez que deseja arquivar a pergunta?");
                System.out.println("Digite \"arquivar\" para prosseguir");
                System.out.println("(R) Cancelar e retornar ao menu anterior");
                opcaoArquivamento = console.nextLine().trim().toUpperCase();
                int index = Integer.parseInt(opcao);
                if (index >= 0 && index < minhasPerguntas.size()) {
                    Pergunta perguntaSelecionada = minhasPerguntas.get(index);
                    switch (opcaoArquivamento) {
                        case "ARQUIVAR":
                            try {
                                arqPerguntas.arquivar(index);
                            } catch (Exception e) {
                                System.out.println("Erro ao arquivar a pergunta: " + e.getMessage());
                            }
                            break;
                        case "R":
                            break;
                        default:
                            System.out.println("Opção inválida!");
                    }
                } else {
                    System.out.println("Número inválido!");
                }
            }

        } while (!opcao.equals("R"));
    }

    private boolean isInt(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static void main(String[] args) {
        try {
            MenuAcesso menu = new MenuAcesso();
            menu.inicio();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}