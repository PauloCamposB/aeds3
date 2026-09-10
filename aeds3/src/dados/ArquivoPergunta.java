package dados;

import entidades.Pergunta;
import estruturas.Arquivo;
import estruturas.ArvoreBMais;
import util.ParIntInt;

import java.io.File;
import java.util.ArrayList;

public class ArquivoPergunta extends Arquivo<Pergunta> {

    private ArvoreBMais<ParIntInt> indiceUsuarioPergunta;

    public ArquivoPergunta() throws Exception {
        super("perguntas", Pergunta.class.getConstructor());
        
        // Inicializa a árvore B+ de grau 5 para o índice 1:N
        // O diretório "dados/perguntas" será criado automaticamente pela classe Arquivo
        indiceUsuarioPergunta = new ArvoreBMais<>(
            ParIntInt.class.getConstructor(),
            5,
            "dados" + File.separator + "perguntas" + File.separator + "perguntas_usuario.btree.db"
        );
    }

    @Override
    public int create(Pergunta p) throws Exception {
        // A classe pai Arquivo.java insere o registro e gera o id
        int id = super.create(p);
        p.setId(id); // garante a att do id

        // Adiciona a associação [idUsuario, idPergunta] na Árvore B+
        indiceUsuarioPergunta.create(new ParIntInt(p.getIdUsuario(), p.getId()));

        return id;
    }

    // perguntas não são excluídas fisicamente, mas arquivadas
    public boolean arquivar(int idPergunta) throws Exception {
        Pergunta p = super.read(idPergunta);
        if (p != null) {
            p.setAtiva(false);
            return super.update(p);
        }
        return false;
    }
    public ArrayList<Pergunta> readByUsuario(int idUsuario) throws Exception {
        ArrayList<Pergunta> lista = new ArrayList<>();
        return lista;
    }
}