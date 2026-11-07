package dados;

import estruturas.ElementoLista;
import estruturas.ListaInvertida;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import util.ProcessadorTexto;

/**
 * Índice invertido das palavras-chave das perguntas.
 *
 * Cada termo aponta para a lista de perguntas que o contêm, junto com o TF
 * (frequência do termo / total de termos da palavra-chave) daquela pergunta.
 * O "número de entidades" da ListaInvertida guarda o N (total de perguntas
 * indexadas), usado pelo IDF na busca.
 */
public class IndiceInvertido {

    private static final int ELEMENTOS_POR_BLOCO = 50;

    private final ListaInvertida lista;

    public IndiceInvertido() throws Exception {
        File d = new File("dados" + File.separator + "perguntas");
        if (!d.exists())
            d.mkdirs();

        lista = new ListaInvertida(
            ELEMENTOS_POR_BLOCO,
            "dados" + File.separator + "perguntas" + File.separator + "palavras.dicionario.listainv.db",
            "dados" + File.separator + "perguntas" + File.separator + "palavras.blocos.listainv.db"
        );
    }

    /** Indexa uma pergunta: um (id; TF) em cada termo + incrementa N. */
    public void indexar(int id, String palavrasChave) throws Exception {
        HashMap<String, Float> tf = tfDe(palavrasChave);
        for (String termo : tf.keySet())
            lista.create(termo, new ElementoLista(id, tf.get(termo)));
        lista.incrementaEntidades();
    }

    /** Remove a pergunta de todos os termos das palavras-chave informadas + decrementa N. */
    public void desindexar(int id, String palavrasChave) throws Exception {
        for (String termo : tfDe(palavrasChave).keySet())
            lista.delete(termo, id);
        if (lista.numeroEntidades() > 0)
            lista.decrementaEntidades();
    }

    /**
     * Troca as palavras-chave de uma pergunta já indexada (N não muda):
     * termos que saíram são removidos, os novos são inseridos e os que
     * continuam têm o TF atualizado.
     */
    public void atualizar(int id, String antigas, String novas) throws Exception {
        HashMap<String, Float> tfAntigo = tfDe(antigas);
        HashMap<String, Float> tfNovo = tfDe(novas);

        for (String termo : tfAntigo.keySet())
            if (!tfNovo.containsKey(termo))
                lista.delete(termo, id);

        for (String termo : tfNovo.keySet()) {
            ElementoLista e = new ElementoLista(id, tfNovo.get(termo));
            if (lista.read(termo, id) != null)
                lista.update(termo, e);
            else
                lista.create(termo, e);
        }
    }

    /** Lista de (id; TF) do termo, ordenada por id. Vazia se o termo não existe. */
    public ElementoLista[] lista(String termo) throws Exception {
        return lista.read(termo);
    }

    /** Total de perguntas indexadas (N do IDF). */
    public int totalPerguntas() throws Exception {
        return lista.numeroEntidades();
    }

    /** Termos distintos de um texto, com o TF de cada um. */
    private HashMap<String, Float> tfDe(String palavrasChave) {
        if (palavrasChave == null)
            return new HashMap<>();
        ArrayList<String> termos = ProcessadorTexto.extrairTermos(palavrasChave);
        return ProcessadorTexto.calcularTF(termos);
    }
}
