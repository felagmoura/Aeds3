import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

//===========================================================================================================//
// CRUD
//===========================================================================================================//

class OrdenacaoExterna {
    //-------------------------------------------------------------------//
    //------------------------- ATRIBUTOS -------------------------------//
    //-------------------------------------------------------------------//
    protected final long INICIO_REGISTROS;
    protected final int NUM_ARQS_TEMP = 4;
    protected int bloco_tam;
    
    protected int limite_escrita;
    protected int limite_leitura;

    protected int index_escrita;
    protected int index_leitura;

    protected Conta[] segmento;
    protected final int NULO;

    protected CRUD arquivo_original;
    protected int registro_total_original;
    protected long ptr;
    protected int passadas;
    
    protected RandomAccessFile[] temps;

    protected long[] ptrs;
    protected int[] registros_total;
    protected boolean[] fim_arq;
    protected boolean[] fim_bloco;

    //-------------------------------------------------------------------//
    //------------------------- FUNCOES ---------------------------------//
    //-------------------------------------------------------------------//
    
    //-------------------------------------------------------------------//
    // CONSTRUTOR

    OrdenacaoExterna (CRUD arq_original) throws FileNotFoundException {
        this.arquivo_original = arq_original;
        this.INICIO_REGISTROS = arq_original.INICIO_REGISTROS;
        NULO = -1;

        temps = new RandomAccessFile [NUM_ARQS_TEMP];
        
        bloco_tam = 3;
        segmento = new Conta [bloco_tam];
        
        registro_total_original = 0;
        registros_total = new int [NUM_ARQS_TEMP];

        limite_escrita = 2;
        index_escrita = 0;
    }

    //-------------------------------------------------------------------//
    // INTERCALACAO BALANCEADA

    public void intercalacao_balanceada () throws FileNotFoundException, IOException {
        distribuir ();
        intercalar ();
    }

    //===================================================================//
    // FASE DA DISTRIBUICAO
    //===================================================================//
    private void distribuir () throws FileNotFoundException, IOException {
        for (int i = 0; i < limite_escrita; i++)
            temps[i] = new RandomAccessFile("arq_temp"+ i +".db", "rw");
        
        ptr = INICIO_REGISTROS;
        arquivo_original.seek(ptr);

        distribuir (0);        
    }

    private void distribuir (int count_blocos) throws FileNotFoundException, IOException {
        while (ptr < arquivo_original.length()) {
            get_segmento_original ();
            ordena_segmento ();
            
            set_index_escrita (count_blocos++);
            transcreve_segmento ();
        }

        passadas = count_blocos;  
    }

        // LE UM NUMERO DETERMINADO DE REGISTROS

    private void get_segmento_original () throws IOException {
        Conta conta = new Conta();
        int i;

        for (i = 0; i < bloco_tam && ptr < arquivo_original.length(); i++, ptr = arquivo_original.getFilePointer()) {
            conta = readRegistro(arquivo_original.arquivo);
            
            if (conta.id != NULO) 
                segmento[i] = conta;
            else
                i--;
        }

        while (i < bloco_tam)
            segmento[i++] = new Conta(); 
    }
        //---------------------------------------------------------------//

        // FAZ A ORDENACAO DO SEGMENTO
    private void ordena_segmento () {
        quicksort(0, segmento.length - 1);
    }

    private void quicksort (int menor_pos, int maior_pos) {
        int i = menor_pos;
        int j = maior_pos;
        
        Conta pivo = segmento[(maior_pos + menor_pos) / 2];

        if (pivo.id == NULO || segmento[menor_pos].id == NULO || segmento[maior_pos].id == NULO) {
            i = bloco_tam;
            j = 0;
        }

        while (i <= j) {
            while (segmento[i].id < pivo.id) i++;
            while (segmento[j].id > pivo.id) j--;

            if (i <= j) 
                troca(i++, j--);
        }

        if (menor_pos < j) quicksort(menor_pos, j);
        if (i < maior_pos) quicksort(i, maior_pos);
    }

    private void troca (int i, int j) {
        Conta temp = segmento[i];
        segmento[i] = segmento[j];
        segmento[j] = temp;
    }
        //---------------------------------------------------------------//

        // ESCREVE OS BLOCOS NOS ARQUIVOS TEMPORARIOS 
    private void transcreve_segmento () throws IOException{
        int i;
        
        for (i = 0; i < bloco_tam && segmento[i].id != NULO; i++) {
            writeRegistro (temps[index_escrita], segmento[i]);
        }

        registro_total_original += i;
        registros_total[index_escrita] += i;

    }
        //---------------------------------------------------------------//

        // ATUALIZA O VALOR DO INDEX DO ARQUIVO DE ESCRITA
    private void set_index_escrita (int i) {
        i %= 2;

        if (limite_escrita == 4) 
            i += 2;

        this.index_escrita = i;
    }

    private void set_index () {
        index_leitura = 0;
        index_escrita = 0;

        if (limite_leitura == 4) 
            index_leitura = 2;
        if (limite_escrita == 4)
            index_escrita = 2;
    }
        //---------------------------------------------------------------//

    private void writeRegistro (RandomAccessFile arquivo, Conta conta) throws IOException {
        byte[] registro = conta.byteArrayOutput();

        arquivo.writeChar(' ');
        arquivo.writeInt(registro.length);
        arquivo.write(registro);
    }

    public Conta readRegistro (RandomAccessFile arquivo) throws IOException {
        Conta temp = new Conta();
        
        char lapide = arquivo.readChar();
        int len_registro = arquivo.readInt();

        byte[] registro = new byte[len_registro];
            
        arquivo.read(registro);
        if (lapide != '*')
            temp.byteArrayInput(registro);

        return temp;
    }

    //===================================================================//
    // FASE DA INTERCALACAO
    //===================================================================//
    private void intercalar () throws IOException {        
        ptrs = new long [NUM_ARQS_TEMP];
        fim_arq = new boolean [NUM_ARQS_TEMP];
        fim_bloco = new boolean [NUM_ARQS_TEMP];

        limite_escrita = 4;
        limite_leitura = 2;

        for (int i = 0; i < NUM_ARQS_TEMP; i++) {
            temps[i] = new RandomAccessFile("arq_temp"+ i +".db", "rw");
            
            temps[i].seek(0);            
            ptrs[i] = temps[i].getFilePointer();
        }

        while (passadas > 1) {
            intercalar (0);
            
            if (passadas > 1) 
                troca_arqs (limite_escrita, limite_leitura);

            reseta_variaveis ();
        }

        rescreve_arquivo_original (index_escrita);
        deleta_temps ();
    }

    private void intercalar (int count_blocos) throws IOException {

        Conta[] registro = new Conta[NUM_ARQS_TEMP];
        int[] count_registros;

        while (!fim_arq[index_leitura] || !fim_arq[index_leitura + 1]) {
            count_registros = new int[NUM_ARQS_TEMP];

            set_index_escrita (count_blocos++);
            le_blocos (registro, count_registros);

            for (int i = index_leitura; i < limite_leitura; i++) 
                fim_arq[i] = fim_arq(count_registros[i], i);
        }

        passadas = count_blocos;
    }

        // COMPARA OS VALORES DAS IDS DO REGISTRO EM UM BLOCO
    private void le_blocos (Conta[] registro, int[] count_registros) throws IOException {        
        while (!fim_bloco[index_leitura] || !fim_bloco[index_leitura + 1]) {                

            for (int i = index_leitura; i < limite_leitura; i++) {
                ptrs[i] = temps[i].getFilePointer(); 
                fim_bloco[i] = fim_bloco(count_registros[i], i);

                if (!fim_bloco[i]) 
                    registro[i] = readRegistro(temps[i]);
            }

            if (fim_bloco[index_leitura] && fim_bloco[index_leitura + 1]) {
                for (int i = index_leitura; i < limite_leitura; i++)
                    fim_bloco[i] = false;
                break;
            }

            else if (fim_bloco[index_leitura] || fim_bloco[index_leitura + 1]) 
                escreve_registro (registro, count_registros);
            
            else 
                escreve_menor_registro (registro, count_registros);
        }
    }
        //---------------------------------------------------------------//

        // ESCREVE OS VALORES ORDENADOS DO ULTIMO TEMP NO ARQUIVO ORIGINAL
    private void rescreve_arquivo_original (int index) throws IOException {
            Conta registro;

            arquivo_original.resetar_arquivo();
            ptrs[index] = 0;

            for (int i = 0; i < registro_total_original; i++) {
                registro = readRegistro(temps[index]);
                arquivo_original.inserir(registro);
            }
        }
        //---------------------------------------------------------------//

        // ESCREVE O REGISTRO NO ARQUIVO TEMP
    private void escreve_registro (Conta[] registro, int[] count_registros) throws IOException {
        for (int i = index_leitura; i < limite_leitura; i++) {
            if (fim_bloco[i]) {
                writeRegistro (temps[index_escrita], registro[prox_index(i)]);
                temps[i].seek(ptrs[i]);
                
                count_registros[prox_index(i)]++;
                registros_total[index_escrita]++;
            }
        }
    }

    private void escreve_menor_registro (Conta[] registro, int[] count_registros) throws IOException {
        for (int i = index_leitura; i < limite_leitura; i++) {
            if (registro[i].id < registro[prox_index(i)].id) {
                writeRegistro (temps[index_escrita], registro[i]);
                temps[prox_index(i)].seek(ptrs[prox_index(i)]);
                
                count_registros[i]++;
                registros_total[index_escrita]++;
            }
        }
    }
        //---------------------------------------------------------------//

        // INDICA O INDEX DO OUTRO ARQUIVO DE LEITURA
    private int prox_index (int i) {
        i += 1;
        i %= 2;

        if (limite_leitura == 4)
            i += 2;

        return i; 
    }
        //---------------------------------------------------------------//

        // RESETA OS VALORES DAS VARIAVEIS DOS ARQUIVOS TEMP PARA 0 E FALSO
    private void reseta_variaveis () throws IOException {
        for (int i = 0; i < NUM_ARQS_TEMP; i++) {
            temps[i].seek(0);
            ptrs[i] = temps[i].getFilePointer();
            fim_arq[i] = false;
            fim_bloco[i] = false;
        }
    }
        //---------------------------------------------------------------//

        // INVERTE OS PAPEIS DOS ARQUIVOS TEMPS PARA A PROXIMA INTERCALACAO
    private void troca_arqs (int limite_escrita, int limite_leitura) {
        this.limite_escrita = limite_leitura;
        this.limite_leitura = limite_escrita;

        bloco_tam *= 2;

        set_index ();

        for (int i = index_escrita; i < this.limite_escrita; i++) 
            registros_total[i] = 0;
    }
        //---------------------------------------------------------------//

        // INDICA O FIM DO ARQUIVO
    private boolean fim_arq (int num_registros, int index) throws IOException {
        return ptrs[index] >= temps[index].length() || num_registros >= registros_total[index];
    }
        //---------------------------------------------------------------//

        // INDICA O FIM DO BLOCO
    private boolean fim_bloco (int num_registros, int index) throws IOException {
        return (num_registros > 0 && num_registros % bloco_tam == 0) || fim_arq(num_registros, index);
    }
        //---------------------------------------------------------------//

        // DELETA ARQUIVOS TEMPORARIOS
    private void deleta_temps () throws IOException {
        File[] temps = new File [NUM_ARQS_TEMP];
        
        for (int i = 0; i < NUM_ARQS_TEMP; i++) {
            this.temps[i].close();
            
            temps[i] = new File("arq_temp"+ i +".db");
            temps[i].delete();
        }
            
    }

} 

//===========================================================================================================//
