package visao;

import dados.ArquivoUsuarios;
import entidades.Usuario;
import util.Seguranca;
import java.util.Scanner;

public class MenuAcesso{

    private Scanner scanner;
    private ArquivoUsuarios arqUsuarios;

    public MenuAcesso(Scanner scanner, ArquivoUsuarios arqUsuarios){
        this.arqUsuarios = arqUsuarios;
        this.scanner = scanner;
    }

    public Usuario exibir(){
        int opcao = -1;
        Usuario usuarioLogado = null;

        while (opcao != 0 && usuarioLogado == null) {
            System.out.println("\n=== SISTEMA DE PERGUNTAS E RESPOSTAS ===");
            System.out.println("1. Acessar (Login)");
            System.out.println("2. Novo usuario (Cadastro)");
            System.out.println("3. Esqueci minha senha");
            System.out.println("0. Sair");
            System.out.print("Escolha uma opcao: ");
            try {
                opcao = Integer.parseInt(scanner.nextLine());
                switch (opcao) {
                    case 1:
                        usuarioLogado = fazerLogin();
                        break;
                    case 2:
                        cadastrarUsuario();
                        break;
                    case 3:
                        recuperarSenha();
                        break;
                    case 0:
                        System.out.println("Saindo do sistema...");
                        break;
                    default:
                        System.out.println("Opcao invalida!");
                }
            } catch (NumberFormatException e) {
                System.out.println("Por favor, digite um numero valido.");
            } catch (Exception e) {
                System.out.println("Erro: " + e.getMessage());
            }
        }

        return usuarioLogado;
    }


    private Usuario fazerLogin() throws Exception{

        System.out.println("\n--- LOGIN ---");
        System.out.print("E-mail: ");
        String email = scanner.nextLine();
        System.out.print("Senha: ");
        String senha = scanner.nextLine();

        Usuario u = arqUsuarios.autenticar(email,senha);

        if(u != null){
            System.out.println("Login realizado com sucesso! Bem-vindo, " + u.getNome() + ".");
            return u;
        }else{
            System.out.println("E-mail ou senha incorretos.");
            return null;
        }

    }


    private void recuperarSenha(){

        System.out.println("\n--- RECUPERACAO DE SENHA ---");

        try {
            System.out.print("Digite seu e-mail: ");
            String email = scanner.nextLine();

            Usuario u = arqUsuarios.readByEmail(email);
            if (u == null) {
                System.out.println("E-mail nao encontrado.");
                return;
            }

            System.out.println("Pergunta Secreta: " + u.getPerguntaSecreta());
            System.out.print("Sua Resposta: ");
            String respostaInput = scanner.nextLine();

            
            String hashRespostaInput = Seguranca.hashSHA256(respostaInput);
            if (u.getHashRespostaSecreta().equals(hashRespostaInput)) {
                System.out.print("Digite a nova senha: ");
                String novaSenha = scanner.nextLine();

                
                u.setHashSenha(Seguranca.hashSHA256(novaSenha));
                arqUsuarios.update(u);

                System.out.println("Senha alterada com sucesso! Agora voce pode fazer login.");
            } else {
                System.out.println("Resposta incorreta.");
            }

        } catch (Exception e) {
            System.out.println("Erro na recuperacao: " + e.getMessage());
        }
    }



    private void cadastrarUsuario() {
        System.out.println("\n--- NOVO CADASTRO ---");
        try {
            System.out.print("Nome completo: ");
            String nome = scanner.nextLine();

            System.out.print("E-mail: ");
            String email = scanner.nextLine();

            System.out.print("Senha: ");
            String senha = scanner.nextLine();

            System.out.print("Pergunta Secreta (para recuperacao): ");
            String pergunta = scanner.nextLine();

            System.out.print("Resposta Secreta: ");
            String resposta = scanner.nextLine();

            
            Usuario novo = new Usuario(-1, nome, email, senha, pergunta, Seguranca.hashSHA256(resposta));
            
            int id = arqUsuarios.create(novo);
            System.out.println("Usuario cadastrado com sucesso! (ID: " + id + ")");

        } catch (Exception e) {
            System.out.println("Erro ao cadastrar: " + e.getMessage());
        }
    }

}

