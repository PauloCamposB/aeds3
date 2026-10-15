package dados;

import entidades.Pergunta;
import estruturas.Arquivo;
import estruturas.ArvoreBMais;
import java.io.File;
import java.util.ArrayList;
import util.ParIntInt;
import util.ParLongInt;

public class ArquivoPergunta extends Arquivo<Pergunta> {

    private ArvoreBMais<ParIntInt> indiceUsuarioPergunta;
    private ArvoreBMais<ParLongInt> indiceData; // data de criação (decrescente) -> idPergunta
    private ArquivoUsuarios arqUsuarios; 

    public ArquivoPergunta(ArquivoUsuarios arqUsuarios) throws Exception {
        super("perguntas", Pergunta.class.getConstructor());
        this.arqUsuarios = arqUsuarios;

 
        indiceUsuarioPergunta = new ArvoreBMais<>(
            ParIntInt.class.getConstructor(),
            5,
            "dados" + File.separator + "perguntas" + File.separator + "perguntas_usuario.btree.db"
        );

        indiceData = new ArvoreBMais<>(
            ParLongInt.class.getConstructor(),
            5,
            "dados" + File.separator + "perguntas" + File.separator + "perguntas_data.btree.db"
        );

        // Perguntas criadas antes deste índice existir: reconstrói a partir do índice por usuário
        if (indiceData.empty() && !indiceUsuarioPergunta.empty()) {
            reconstruirIndiceData();
        }
    }

    private void reconstruirIndiceData() throws Exception {
        for (ParIntInt par : indiceUsuarioPergunta.read(null)) {
            Pergunta p = super.read(par.getIdPergunta());
            if (p != null) {
                indiceData.create(new ParLongInt(p.getCriacao(), p.getId()));
            }
        }
    }

    @Override
    public int create(Pergunta p) throws Exception {
        
        if (arqUsuarios.read(p.getIdUsuario()) == null) {
            throw new Exception("Usuário inexistente: não é possível criar a pergunta.");
        }

       
        int id = super.create(p);
        p.setId(id);

        
        indiceUsuarioPergunta.create(new ParIntInt(p.getIdUsuario(), p.getId()));
        indiceData.create(new ParLongInt(p.getCriacao(), p.getId()));

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

    // ------------------------------------------------------- PERGUNTAS RECENTES

    public static final int TAMANHO_PAGINA = 5;

    /** IDs das perguntas ATIVAS, da mais recente para a mais antiga. */
    private ArrayList<Pergunta> recentesAtivas() throws Exception {
        ArrayList<Pergunta> ativas = new ArrayList<>();
        // read(null) devolve todos os pares, já na ordem da árvore (data decrescente)
        for (ParLongInt par : indiceData.read(null)) {
            Pergunta p = super.read(par.getIdPergunta());
            if (p != null && p.isAtiva()) {
                ativas.add(p);
            }
        }
        return ativas;
    }

    /** Total de páginas de perguntas ativas (mínimo 1, mesmo sem perguntas). */
    public int totalPaginasRecentes() throws Exception {
        int total = recentesAtivas().size();
        return Math.max(1, (total + TAMANHO_PAGINA - 1) / TAMANHO_PAGINA);
    }

    /** Uma página (5 perguntas) de perguntas ativas; pagina começa em 0. */
    public ArrayList<Pergunta> listarRecentes(int pagina) throws Exception {
        ArrayList<Pergunta> ativas = recentesAtivas();
        ArrayList<Pergunta> resultado = new ArrayList<>();
        int inicio = pagina * TAMANHO_PAGINA;
        for (int i = inicio; i < ativas.size() && resultado.size() < TAMANHO_PAGINA; i++) {
            resultado.add(ativas.get(i));
        }
        return resultado;
    }
}