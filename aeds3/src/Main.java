import dados.ArquivoUsuarios;
import entidades.Usuario;
import java.util.Scanner;
import visao.MenuAcesso;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            ArquivoUsuarios arqUsuarios = new ArquivoUsuarios();

            // Passa arqUsuarios e scanner exatamente nesta ordem
           MenuAcesso menuAcesso = new MenuAcesso(scanner, arqUsuarios);
            Usuario usuarioLogado = menuAcesso.exibir();

            if (usuarioLogado != null) {
                System.out.println("\n[SISTEMA] Usuario " + usuarioLogado.getNome() + " conectado com sucesso!");
            }

        } catch (Exception e) {
            System.out.println("Erro no sistema: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}