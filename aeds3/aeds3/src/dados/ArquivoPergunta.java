package dados;

import entidades.Pergunta;
import estruturas.Arquivo;
import estruturas.ArvoreBMais;
import java.io.File;
import java.util.ArrayList;
import util.ParIntInt;

public class ArquivoPergunta extends Arquivo<Pergunta> {

    private ArvoreBMais<ParIntInt> indiceUsuarioPergunta;
    private ArquivoUsuarios arqUsuarios; 

    public ArquivoPergunta(ArquivoUsuarios arqUsuarios) throws Exception {
        super("perguntas", Pergunta.class.getConstructor());
        this.arqUsuarios = arqUsuarios;

 
        indiceUsuarioPergunta = new ArvoreBMais<>(
            ParIntInt.class.getConstructor(),
            5,
            "dados" + File.separator + "perguntas" + File.separator + "perguntas_usuario.btree.db"
        );
    }

    @Override
    public int create(Pergunta p) throws Exception {
        
        if (arqUsuarios.read(p.getIdUsuario()) == null) {
            throw new Exception("Usuário inexistente: não é possível criar a pergunta.");
        }

       
        int id = super.create(p);
        p.setId(id);

        
        indiceUsuarioPergunta.create(new ParIntInt(p.getIdUsuario(), p.getId()));

        return id;
    }

   
    public boolean arquivar(int idPergunta) throws Exception {
        Pergunta p = super.read(idPergunta);
        if (p != null && p.isAtiva()) {
            p.setAtiva(false);
            p.setAlteracao(System.currentTimeMillis());
            return super.update(p);
        }
        return false;
    }

   
    public ArrayList<Pergunta> readByUsuario(int idUsuario) throws Exception {
        ArrayList<Pergunta> lista = new ArrayList<>();

        
        ArrayList<ParIntInt> pares = indiceUsuarioPergunta.read(new ParIntInt(idUsuario, -1));

        for (ParIntInt par : pares) {
            Pergunta p = super.read(par.getIdPergunta());
            if (p != null) {
                lista.add(p);
            }
        }
        return lista;
    }
}