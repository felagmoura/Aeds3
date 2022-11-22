import java.io.RandomAccessFile;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

//classe criadora dos nós
class No {
    // char do nó
    Character ch;
    // frequencia da variavel
    Integer freq;
    // inicializando nos filhos
    No esquerda = null;
    No direita = null;

    // construtor classe
    No(Character ch, Integer freq) {
        this.ch = ch;
        this.freq = freq;
    }

    // cosntrutor classe
    public No(Character ch, Integer freq, No esquerda, No direita) {
        this.ch = ch;
        this.freq = freq;
        this.esquerda = esquerda;
        this.direita = direita;
    }
}

// classe main
public class Huffman {

    // funcao faz tudo
    public static void criarArvoreHuffman(String text) {
        // se a string for zero
        if (text == null || text.length() == 0) {
            return;
        }
        // conta a frequencia dos chars num mapa
        // cria um mapa
        Map<Character, Integer> freq = new HashMap<>();
        // itera sobre a string e transforma num array de char
        for (char c : text.toCharArray()) {
            // salva o char e sua frequencia no mapa
            freq.put(c, freq.getOrDefault(c, 0) + 1);
        }
        // cria uma fila de prioridade dos nos da arvore
        // maior prioridade = menor frequencia(seguindo regra de criacao da arvore huffman)
        PriorityQueue<No> pq = new PriorityQueue<>(Comparator.comparingInt(l -> l.freq));
        // itera sobre os mapas
        for (var entry : freq.entrySet()) {
            // cria um nó folha e adiciona na fila
            pq.add(new No(entry.getKey(), entry.getValue()));
        }
        // loop pra criar a arvore huffman ate sobrar 1 no
        while (pq.size() != 1) {
            // itera criando os nós de acordo com a prioridade
            No esquerda = pq.poll();
            No direita = pq.poll();
            // pega a soma da frequencia dos dois nos
            int soma = esquerda.freq + direita.freq;
            // cria um nó pai sem char e o adiciona na fila de prioridade para continuar construindo a arvore
            pq.add(new No(null, soma, esquerda, direita));
        }
        // salva ponteiro para a raiz
        No raiz = pq.peek();
        // salva os codigos huffman num hashmap
        Map<Character, String> codigoHuffman = new HashMap<>();
        codificaData(raiz, "", codigoHuffman);
        // printa os codigos huffman
        System.out.println("Código Huffman dos caracteres é: " + codigoHuffman);
        // printa a string inicial
        System.out.println("A string incial é" + text);
        // cria uma stringbuilder pra poder printar a string inteira final
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            sb.append(codigoHuffman.get(c));
        }
        System.out.println("A string codificada é: " + sb);
        System.out.print("A string decodificada é: ");
        //itera na arvore para decodar
        if (ehFolha(raiz)) {
            while (raiz.freq-- > 0) {
                System.out.print(raiz.ch);
            }
        } else {
            int index = -1;
            while (index < sb.length() - 1) {
                index = decodificaData(raiz, index, sb);
            }
        }
    }

    // codifica os dados
    public static void codificaData(No raiz, String str, Map<Character, String> codigoHuffman) {
        if (raiz == null) {
            return;
        }
        //checa se é raiz
        if (ehFolha(raiz)) {
            codigoHuffman.put(raiz.ch, str.length() > 0 ? str : "1");
        }
        //itera recursivamente
        codificaData(raiz.esquerda, str + '0', codigoHuffman);
        codificaData(raiz.direita, str + '1', codigoHuffman);
    }

    public static int decodificaData(No raiz, int index, StringBuilder sb) {
        // checa se a raiz é nula
        if (raiz == null) {
            return index;
        }
        //checa se é raiz
        if (ehFolha(raiz)) {
            System.out.print(raiz.ch);
            return index;
        }
        index++;
        raiz = (sb.charAt(index) == '0') ? raiz.esquerda : raiz.direita;
        // itera recursivamente
        index = decodificaData(raiz, index, sb);
        return index;
    }

    // checa se só tem 1 no
    public static boolean ehFolha(No raiz) {
        return raiz.esquerda == null && raiz.direita == null;
    }

    // driver code
    public static void main(String args[]) {

    }
}