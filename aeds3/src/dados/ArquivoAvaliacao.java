package dados;

import entidades.Avaliacao;
import entidades.Resposta;
import estruturas.Arquivo;
import estruturas.ArvoreBMais;
import java.io.File;
import java.util.ArrayList;
import util.ParIntInt;

/**
 * CRUD de avaliações + dois índices em Árvore B+:
 *   - indiceRespostaAvaliacao: idResposta -> idAvaliacao (avaliações de uma resposta)
 *   - indiceUsuarioAvaliacao : idUsuario  -> idAvaliacao (avaliações feitas por um usuário)
 *
 * Reaproveita o ParIntInt do mesmo jeito que ArquivoResposta: primeiro campo
 * (getIdUsuario()) é a CHAVE de busca, segundo (getIdPergunta()) é o VALOR (idAvaliacao).
 */
public class ArquivoAvaliacao extends Arquivo<Avaliacao> {

    private ArvoreBMais<ParIntInt> indiceRespostaAvaliacao;
    private ArvoreBMais<ParIntInt> indiceUsuarioAvaliacao;
    private ArquivoUsuarios arqUsuarios;
    private ArquivoResposta arqRespostas;

    public ArquivoAvaliacao(ArquivoUsuarios arqUsuarios, ArquivoResposta arqRespostas) throws Exception {
        super("avaliacoes", Avaliacao.class.getConstructor());
        this.arqUsuarios = arqUsuarios;
        this.arqRespostas = arqRespostas;

        indiceRespostaAvaliacao = new ArvoreBMais<>(
            ParIntInt.class.getConstructor(),
            5,
            "dados" + File.separator + "avaliacoes" + File.separator + "avaliacoes_resposta.btree.db"
        );

        indiceUsuarioAvaliacao = new ArvoreBMais<>(
            ParIntInt.class.getConstructor(),
            5,
            "dados" + File.separator + "avaliacoes" + File.separator + "avaliacoes_usuario.btree.db"
        );
    }

    private void ajustarResposta(int idResposta, int deltaNota, int deltaAvaliacoes) throws Exception {
        Resposta r = arqRespostas.read(idResposta);
        if (r != null) {
            r.setNota((short) (r.getNota() + deltaNota));
            r.setAvaliacoes((short) (r.getAvaliacoes() + deltaAvaliacoes));
            arqRespostas.update(r);
        }
    }

    // ---------------------------------------------------------------- CREATE
    @Override
    public int create(Avaliacao a) throws Exception {
        if (arqUsuarios.read(a.getIdUsuario()) == null) {
            throw new Exception("Usuário inexistente: não é possível registrar a avaliação.");
        }

        Resposta r = arqRespostas.read(a.getIdResposta());
        if (r == null) {
            throw new Exception("Resposta inexistente: não é possível registrar a avaliação.");
        }
        if (!r.isAtiva()) {
            throw new Exception("Resposta arquivada: não aceita novas avaliações.");
        }

        if (jaAvaliou(a.getIdUsuario(), a.getIdResposta())) {
            throw new Exception("Usuário já avaliou esta resposta.");
        }

        int id = super.create(a);
        a.setId(id);

        indiceRespostaAvaliacao.create(new ParIntInt(a.getIdResposta(), id));
        indiceUsuarioAvaliacao.create(new ParIntInt(a.getIdUsuario(), id));

        // +1 na nota se concordou, -1 se discordou; sempre +1 na contagem de avaliações
        ajustarResposta(a.getIdResposta(), a.isAvaliacao() ? 1 : -1, 1);
        return id;
    }

    // ---------------------------------------------------------------- UPDATE
    
    @Override
    public boolean update(Avaliacao nova) throws Exception {
        Avaliacao atual = super.read(nova.getId());
        if (atual == null) {
            return false;
        }

        if (atual.getIdUsuario() != nova.getIdUsuario() || atual.getIdResposta() != nova.getIdResposta()) {
            throw new Exception("Não é permitido alterar o autor ou a resposta avaliada.");
        }

        // se o voto mudou de valor (ex: concordou -> discordou), ajusta a nota pela diferença
        if (atual.isAvaliacao() != nova.isAvaliacao()) {
            int delta = nova.isAvaliacao() ? 2 : -2; // sair de -1 pra +1 (ou o contrário) é uma diferença de 2
            ajustarResposta(nova.getIdResposta(), delta, 0);
        }

        return super.update(nova);
    }

    // ---------------------------------------------------------------- DELETE
    @Override
    public boolean delete(int idAvaliacao) throws Exception {
        Avaliacao a = super.read(idAvaliacao);
        if (a == null) {
            return false;
        }

        if (!super.delete(idAvaliacao)) {
            return false;
        }

        indiceRespostaAvaliacao.delete(new ParIntInt(a.getIdResposta(), idAvaliacao));
        indiceUsuarioAvaliacao.delete(new ParIntInt(a.getIdUsuario(), idAvaliacao));

        // desfaz o efeito que essa avaliação tinha causado na nota/contagem da resposta
        ajustarResposta(a.getIdResposta(), a.isAvaliacao() ? -1 : 1, -1);

        return true;
    }

    // ---------------------------------------------------------------- CONSULTAS

    public ArrayList<Avaliacao> readByResposta(int idResposta) throws Exception {
        ArrayList<ParIntInt> pares = indiceRespostaAvaliacao.read(new ParIntInt(idResposta, -1));
        return carregar(pares);
    }

    public ArrayList<Avaliacao> readByUsuario(int idUsuario) throws Exception {
        ArrayList<ParIntInt> pares = indiceUsuarioAvaliacao.read(new ParIntInt(idUsuario, -1));
        return carregar(pares);
    }

    /** Impede que o mesmo usuário avalie a mesma resposta mais de uma vez. */
    public boolean jaAvaliou(int idUsuario, int idResposta) throws Exception {
        ArrayList<Avaliacao> avaliacoesDoUsuario = readByUsuario(idUsuario);
        for (Avaliacao a : avaliacoesDoUsuario) {
            if (a.getIdResposta() == idResposta) {
                return true;
            }
        }
        return false;
    }

    // ---------------------------------------------------------------- AUXILIARES

    private ArrayList<Avaliacao> carregar(ArrayList<ParIntInt> pares) throws Exception {
        ArrayList<Avaliacao> lista = new ArrayList<>();
        for (ParIntInt par : pares) {
            Avaliacao a = super.read(par.getIdPergunta()); // aqui getIdPergunta() = idAvaliacao
            if (a != null) {
                lista.add(a);
            }
        }
        return lista;
    }
    
}