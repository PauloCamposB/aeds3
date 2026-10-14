package dados;

import entidades.Pergunta;
import entidades.Resposta;
import estruturas.Arquivo;
import estruturas.ArvoreBMais;
import java.io.File;
import java.util.ArrayList;
import util.ParIntInt;

/**
 * CRUD de respostas + dois índices em Árvore B+:
 *   - indicePerguntaResposta: idPergunta -> idResposta (respostas de uma pergunta)
 *   - indiceUsuarioResposta : idUsuario  -> idResposta (respostas de um usuário)
 *
 * Os dois índices reaproveitam o ParIntInt: o primeiro campo (getIdUsuario())
 * é a CHAVE de busca e o segundo (getIdPergunta()) é o VALOR (idResposta).
 * O valor -1 funciona como coringa, retornando todos os pares da chave.
 */
public class ArquivoResposta extends Arquivo<Resposta> {

    // limite do tamanho do registro: o Arquivo guarda o tamanho em um short
    private static final int TAM_MAX_REGISTRO = Short.MAX_VALUE;

    private ArvoreBMais<ParIntInt> indicePerguntaResposta;
    private ArvoreBMais<ParIntInt> indiceUsuarioResposta;
    private ArquivoUsuarios arqUsuarios;
    private ArquivoPergunta arqPerguntas;

    public ArquivoResposta(ArquivoUsuarios arqUsuarios, ArquivoPergunta arqPerguntas) throws Exception {
        super("respostas", Resposta.class.getConstructor());
        this.arqUsuarios = arqUsuarios;
        this.arqPerguntas = arqPerguntas;

        indicePerguntaResposta = new ArvoreBMais<>(
            ParIntInt.class.getConstructor(),
            5,
            "dados" + File.separator + "respostas" + File.separator + "respostas_pergunta.btree.db"
        );

        indiceUsuarioResposta = new ArvoreBMais<>(
            ParIntInt.class.getConstructor(),
            5,
            "dados" + File.separator + "respostas" + File.separator + "respostas_usuario.btree.db"
        );
    }

    // ---------------------------------------------------------------- CREATE
    @Override
    public int create(Resposta r) throws Exception {
        if (arqUsuarios.read(r.getIdUsuario()) == null) {
            throw new Exception("Usuário inexistente: não é possível criar a resposta.");
        }

        Pergunta p = arqPerguntas.read(r.getIdPergunta());
        if (p == null) {
            throw new Exception("Pergunta inexistente: não é possível criar a resposta.");
        }
        if (!p.isAtiva()) {
            throw new Exception("Pergunta arquivada: não aceita novas respostas.");
        }

        validar(r);

        int id = super.create(r);
        r.setId(id);

        // idPergunta -> idResposta  e  idUsuario -> idResposta
        indicePerguntaResposta.create(new ParIntInt(r.getIdPergunta(), id));
        indiceUsuarioResposta.create(new ParIntInt(r.getIdUsuario(), id));

        return id;
    }

    // ---------------------------------------------------------------- UPDATE
    @Override
    public boolean update(Resposta nova) throws Exception {
        Resposta atual = super.read(nova.getId());
        if (atual == null) {
            return false;
        }

        // Os índices são montados com esses dois campos: alterá-los exigiria reindexar
        if (atual.getIdPergunta() != nova.getIdPergunta() || atual.getIdUsuario() != nova.getIdUsuario()) {
            throw new Exception("Não é permitido alterar a pergunta ou o autor de uma resposta.");
        }

        validar(nova);
        return super.update(nova);
    }

    // ---------------------------------------------------------------- DELETE
    @Override
    public boolean delete(int idResposta) throws Exception {
        Resposta r = super.read(idResposta);
        if (r == null) {
            return false;
        }

        if (!super.delete(idResposta)) {
            return false;
        }

        // mantém os dois índices consistentes com o arquivo principal
        indicePerguntaResposta.delete(new ParIntInt(r.getIdPergunta(), idResposta));
        indiceUsuarioResposta.delete(new ParIntInt(r.getIdUsuario(), idResposta));
        return true;
    }

    // ---------------------------------------------------------------- ARQUIVAR
    /** Arquivamento lógico: a resposta continua nos índices, mas com ativa = false. */
    public boolean arquivar(int idResposta) throws Exception {
        Resposta r = super.read(idResposta);
        if (r != null && r.isAtiva()) {
            r.setAtiva(false);
            return super.update(r);
        }
        return false;
    }

    // ---------------------------------------------------------------- CONSULTAS

    /** Todas as respostas (ativas e arquivadas) de uma pergunta. */
    public ArrayList<Resposta> readByPergunta(int idPergunta) throws Exception {
        return readByPergunta(idPergunta, false);
    }

    /** Respostas de uma pergunta; com apenasAtivas = true, as arquivadas são omitidas. */
    public ArrayList<Resposta> readByPergunta(int idPergunta, boolean apenasAtivas) throws Exception {
        ArrayList<ParIntInt> pares = indicePerguntaResposta.read(new ParIntInt(idPergunta, -1));
        return carregar(pares, apenasAtivas);
    }

    /** Todas as respostas (ativas e arquivadas) escritas por um usuário. */
    public ArrayList<Resposta> readByUsuario(int idUsuario) throws Exception {
        ArrayList<ParIntInt> pares = indiceUsuarioResposta.read(new ParIntInt(idUsuario, -1));
        return carregar(pares, false);
    }

    // ---------------------------------------------------------------- AUXILIARES

    // Converte os pares (chave, idResposta) lidos da B+ em objetos Resposta
    private ArrayList<Resposta> carregar(ArrayList<ParIntInt> pares, boolean apenasAtivas) throws Exception {
        ArrayList<Resposta> lista = new ArrayList<>();
        for (ParIntInt par : pares) {
            Resposta r = super.read(par.getIdPergunta()); // aqui getIdPergunta() = idResposta
            if (r != null && (!apenasAtivas || r.isAtiva())) {
                lista.add(r);
            }
        }
        return lista;
    }

    private void validar(Resposta r) throws Exception {
        if (r.getResposta() == null || r.getResposta().trim().isEmpty()) {
            throw new Exception("A resposta não pode ficar vazia.");
        }
        if (r.toByteArray().length > TAM_MAX_REGISTRO) {
            throw new Exception("Resposta muito longa para ser armazenada.");
        }
    }
}
