package dados;

import entidades.Pergunta;
import estruturas.ElementoLista;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import util.ProcessadorTexto;
import util.ResultadoBusca;

public class BuscaPerguntas {

    private final IndiceInvertido indice;
    private final ArquivoPergunta arqPerguntas;

    public BuscaPerguntas(IndiceInvertido indice, ArquivoPergunta arqPerguntas) {
        this.indice = indice;
        this.arqPerguntas = arqPerguntas;
    }

   
    public ArrayList<ResultadoBusca> buscar(String consulta) throws Exception {
        ArrayList<ResultadoBusca> filtrados = new ArrayList<>();
        for (ResultadoBusca r : ranquear(consulta)) {
            Pergunta p = (arqPerguntas == null) ? null : arqPerguntas.read(r.getId());
            if (p != null && p.isAtiva()) {
                r.setPergunta(p);
                filtrados.add(r);
            }
        }
        return filtrados;
    }


    public ArrayList<ResultadoBusca> ranquear(String consulta) throws Exception {
        ArrayList<ResultadoBusca> resultado = new ArrayList<>();

        
        LinkedHashSet<String> termos = new LinkedHashSet<>(ProcessadorTexto.extrairTermos(consulta));
        int n = indice.totalPerguntas();
        if (termos.isEmpty() || n <= 0) {
            return resultado;
        }

       
        HashMap<Integer, Double> scores = new HashMap<>();
        for (String termo : termos) {
            ElementoLista[] lista = indice.lista(termo);
            if (lista == null || lista.length == 0) {
                continue; 
            }
            double idf = Math.log10((double) n / lista.length);
            for (ElementoLista e : lista) {
                double tfidf = e.getFrequencia() * idf;
                scores.merge(e.getId(), tfidf, Double::sum);
            }
        }

        for (HashMap.Entry<Integer, Double> par : scores.entrySet()) {
            resultado.add(new ResultadoBusca(par.getKey(), par.getValue().floatValue()));
        }
        resultado.sort(null); 
        return resultado;
    }
}
