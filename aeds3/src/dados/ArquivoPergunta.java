package dados;

import entidades.Pergunta;
import estruturas.Arquivo;
import estruturas.ArvoreBMais;
import java.io.File;
import java.util.ArrayList;
import util.ParIntInt;

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

    public boolean desarquivar(int idPergunta) throws Exception {
        Pergunta p = super.read(idPergunta);
        if (p != null) {
            p.setAtiva(true);
            return super.update(p);
        }
        return false;
    }

    public Pergunta getPergunta(int idPergunta) throws Exception {
        return super.read(idPergunta);
    }

    public boolean updatePergunta(Pergunta p) throws Exception {
        return super.update(p);
    }

    public ArrayList<Pergunta> findByUsuario(int idUsuario, int page) throws Exception {
        ArrayList<Pergunta> lista = new ArrayList<>();
        ArrayList<ParIntInt> chaves = indiceUsuarioPergunta.readAll();
        for (ParIntInt chave : chaves) {
            if (chave.getIdUsuario() == idUsuario) {
                Pergunta p = super.read(chave.getIdPergunta());
                if (p != null) {
                    lista.add(p);
                }
            }
        }
        return lista;
    }
    public ArrayList<Pergunta> findActiveByUsuario(int idUsuario, int page) throws Exception {
        ArrayList<Pergunta> lista = new ArrayList<>();
        ArrayList<ParIntInt> chaves = indiceUsuarioPergunta.readAll();
        for (ParIntInt chave : chaves) {
            if (chave.getIdUsuario() == idUsuario) {
                Pergunta p = super.read(chave.getIdPergunta());
                if (p != null && p.isAtiva()) {
                    lista.add(p);
                }
            }
        }
        return lista;
    }
}