package util;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class ProcessadorTexto {
    private static final HashSet<String> STOP_WORDS = new HashSet<>(Arrays.asList(
        // artigos
        "a", "o", "as", "os", "um", "uma", "uns", "umas",

        // preposições
        "ante", "apos", "ate", "com", "contra", "de", "desde", "em", "entre",
        "para", "perante", "por", "sem", "sob", "sobre", "tras",

        // contrações de preposição com artigo ou pronome
        "ao", "aos", "do", "da", "dos", "das", "no", "na", "nos", "nas",
        "num", "numa", "nuns", "numas", "dum", "duma", "duns", "dumas",
        "pelo", "pela", "pelos", "pelas", "pro", "pra", "pros", "pras",
        "deste", "desta", "destes", "destas", "desse", "dessa", "desses", "dessas",
        "daquele", "daquela", "daqueles", "daquelas", "disto", "disso", "daquilo",
        "neste", "nesta", "nestes", "nestas", "nesse", "nessa", "nesses", "nessas",
        "naquele", "naquela", "naqueles", "naquelas", "nisto", "nisso", "naquilo",

        // numerais escritos por extenso ("um" e "uma" já estão nos artigos)
        "dois", "duas", "tres", "quatro", "cinco", "seis", "sete", "oito",
        "nove", "dez", "cem", "mil",

        // conjunções
        "e", "ou", "mas", "nem", "que", "se", "porem", "contudo", "todavia",
        "entao", "logo", "pois", "porque", "quando", "enquanto", "embora",

        // pronomes
        "eu", "tu", "ele", "ela", "vos", "eles", "elas", "voce", "voces",
        "me", "te", "lhe", "lhes", "meu", "minha", "meus", "minhas",
        "seu", "sua", "seus", "suas", "nosso", "nossa", "nossos", "nossas",
        "este", "esta", "estes", "estas", "isto", "esse", "essa", "esses", "essas",
        "isso", "aquele", "aquela", "aqueles", "aquelas", "aquilo",

        // verbos de ligação e auxiliares mais comuns ("e" de "é" já está acima)
        "sao", "ser", "foi", "era", "ha",

        // domínio de perguntas e respostas: palavras interrogativas e termos
        // genéricos que estariam em quase todas as perguntas
        "como", "qual", "quais", "quem", "onde", "pergunta", "perguntas",
        "duvida", "duvidas"
    ));

    public static ArrayList<String> extrairTermos(String texto) {
        ArrayList<String> termos = new ArrayList<>();
        if (texto == null) {
            return termos;
        }

        // minúsculas e sem acentos: "Introdução" -> "introducao"
        String normalizado = removerAcentos(texto.toLowerCase());

        // vetor de palavras
        String[] palavras = normalizado.split("[^a-z0-9]+");

        // descarta as stop words, os numerais e os pedaços vazios
        for (String palavra : palavras) {
            if (palavra.isEmpty()) continue;
            if (STOP_WORDS.contains(palavra)) continue;
            if (palavra.matches("[0-9]+")) continue;
            termos.add(palavra);
        }
        return termos;
    }

    public static HashMap<String, Float> calcularTF(ArrayList<String> termos) {
        HashMap<String, Float> tf = new HashMap<>();
        if (termos == null || termos.isEmpty()) {
            return tf;
        }

        // conta as ocorrências de cada termo
        HashMap<String, Integer> ocorrencias = new HashMap<>();
        for (String termo : termos) {
            ocorrencias.put(termo, ocorrencias.getOrDefault(termo, 0) + 1);
        }

        // divide pelo total de termos válidos
        float total = termos.size();
        for (String termo : ocorrencias.keySet()) {
            tf.put(termo, ocorrencias.get(termo) / total);
        }
        return tf;
    }

    // Remove os acentos e a cedilha: "inteligência" -> "inteligencia", "ação" -> "acao".
    private static String removerAcentos(String texto) {
        String decomposto = Normalizer.normalize(texto, Normalizer.Form.NFD);
        return decomposto.replaceAll("\\p{M}", "");
    }
}
