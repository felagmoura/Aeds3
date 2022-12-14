import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;


//===========================================================================================================//
// CRUD
//===========================================================================================================//

public class CRUD {
    // -------------------------------------------------------------------//
    // ------------------------- ATRIBUTOS -------------------------------//
    // -------------------------------------------------------------------//
    protected final static String CHAVE = "aeds";
    protected final int INT_TAM = 4;
    protected final int CHAR_TAM = 2;
    protected final String PATH;
    protected final static int d = 256;

    protected final long NUM_REGISTROS_END = 0;
    protected int num_registros;

    protected final long USUARIOS_ATIVOS_END = 4;
    protected int num_usuarios_ativos;

    protected final long NUM_EXCLUIDOS_END = 8;
    protected int num_excluidos;

    final static long INICIO_REGISTROS = 12;

    protected RandomAccessFile arquivo;

    protected char lapide;
    protected int len_registro;
    protected byte[] registro;
    protected Conta temp;
    protected long ptr = INICIO_REGISTROS;
    protected long ptro;

    protected static BTree arvore = new BTree<>();

    // -------------------------------------------------------------------//
    // ------------------------- FUNCOES ---------------------------------//
    // -------------------------------------------------------------------//

    // -------------------------------------------------------------------//
    // CONSTRUTOR

    public CRUD(String path) throws FileNotFoundException, IOException {
        this.PATH = path;
        arquivo = new RandomAccessFile(PATH, "rw");

        if (arquivo.length() == 0) {
            num_registros = 0;
            num_usuarios_ativos = 0;
            num_excluidos = 0;
        } else {
            arquivo.seek(NUM_REGISTROS_END);

            num_registros = arquivo.readInt();
            num_usuarios_ativos = arquivo.readInt();
            num_excluidos = arquivo.readInt();
        }

        atualizar_cabecalho();
    }

    // funcao para recriar arvore B+ em mem??ria prim??ria a partir do arquivo de
    // registros
    
    public void createBTree() {
        try {
            temp = new Conta();
            int idConta;
            long pos;

            ptr = INICIO_REGISTROS;
            // percorre o registro pegando as ids e posicoes de ponteiro especificas de cada
            // insercao
            for (int i = 0; i < num_registros
                    && ptr < arquivo.length(); i++, ptr += len_registro + INT_TAM + CHAR_TAM) {

                arquivo.seek(ptr);
                pos = arquivo.getFilePointer();

                lapide = arquivo.readChar();
                len_registro = arquivo.readInt();

                registro = new byte[len_registro];

                arquivo.read(registro);
                temp.byteArrayInput(registro);
                idConta = temp.getID();
                arvore.insert(idConta, pos); // chama a funcao de insercao na arvore com os valores especificos em cada
                                             // chamada
                System.out.println("Inserido na funcaoo createBTree: ID: " + temp.getID() + "Posicao: " + pos);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // -------------------------------------------------------------------//
    // PREENCHE O CABECALHO

    private void atualizar_cabecalho() throws IOException {
        arquivo.seek(NUM_REGISTROS_END);

        arquivo.writeInt(num_registros);
        arquivo.writeInt(num_usuarios_ativos);
        arquivo.writeInt(num_excluidos);
    }

    private void atualizar_cabecalho(long end, int campo) throws IOException {
        arquivo.seek(end);
        arquivo.writeInt(campo);
    }

    // -------------------------------------------------------------------//
    // ESCREVE REGISTRO NO ARQUIVO
    private void writeRegistro(byte[] registro) throws IOException {
        arquivo.writeChar(' ');
        arquivo.writeInt(registro.length);
        arquivo.write(registro);
    }

    // -------------------------------------------------------------------//
    // BUSCA REGISTRO

    private Conta buscar(int chave) throws IOException {

        Conta conta_procurada = new Conta();
        temp = new Conta();

        ptr = INICIO_REGISTROS;

        for (int i = 0; i < num_registros && ptr < arquivo.length(); i++, ptr += len_registro + INT_TAM + CHAR_TAM) {

            arquivo.seek(ptr);

            lapide = arquivo.readChar();
            len_registro = arquivo.readInt();

            registro = new byte[len_registro];

            arquivo.read(registro);
            temp.byteArrayInput(registro);

            if (temp.id == chave && lapide != '*') {
                conta_procurada = temp;
                break;
            }
        }
        conta_procurada.senha = descriptografar(conta_procurada.senha);
        return conta_procurada;
    }

    public void buscarCasamentoPadrao(String padrao, int hash) throws IOException {

        Conta continha = new Conta();

        ptr = INICIO_REGISTROS;
        // loopa pelo registro
        for (int i = 0; i < num_registros && ptr < arquivo.length(); i++, ptr += len_registro + INT_TAM + CHAR_TAM) { 

            arquivo.seek(ptr); //posiciona o ponteiro no inicio do arquivo

            lapide = arquivo.readChar();
            len_registro = arquivo.readInt();

            registro = new byte[len_registro];
            //testa um registro por vez
            arquivo.read(registro);
            continha.byteArrayInput(registro);
            String txt = continha.adicionaString();
            //faz a pesquisa com rabin karp
            search(padrao, txt, hash); 
        }
    }

    //funcao para fazer a busca por ID utilizando os ??ndices na ??rvore B+
    private Conta buscarUsandoArvore(int id) throws IOException{
        long pos;
        Conta tmp = new Conta();
        Conta conta_procurada = new Conta();
        try{
           ptro = pos = (long) arvore.search(id);// pega a posicao do registro
        }catch (Exception e){
            e.printStackTrace();
            return tmp;
        }
        arquivo.seek(pos); // vai ate a posicao do registro

        lapide = arquivo.readChar();
        len_registro = arquivo.readInt();

        registro = new byte[len_registro];
        arquivo.read(registro);
        tmp.byteArrayInput(registro);

        if (lapide != '*') {
            conta_procurada = tmp;
        }

        return conta_procurada;
    }

    public boolean valida_usuario(String chave) throws IOException {
        boolean valido = true;

        ptr = INICIO_REGISTROS;
        temp = new Conta();

        for (int i = 0; i < num_registros && ptr < arquivo.length(); i++, ptr += len_registro + INT_TAM + CHAR_TAM) {
            arquivo.seek(ptr);

            lapide = arquivo.readChar();
            len_registro = arquivo.readInt();

            registro = new byte[len_registro];

            arquivo.read(registro);
            temp.byteArrayInput(registro);

            if (temp.nomeUsuario.equals(chave) && lapide != '*')
                valido = false;
        }

        return valido;
    }
    // -------------------------------------------------------------------//
    // INSERE REGISTRO NO ARQUIVO

    public void inserir(Conta conta) throws IOException {
        int ultima_id;

        arquivo.seek(NUM_REGISTROS_END);
        ultima_id = arquivo.readInt();

        conta.id = ultima_id;
        conta.senha = criptografar(conta.senha);
        registro = conta.byteArrayOutput();

        atualizar_cabecalho(NUM_REGISTROS_END, ++num_registros);
        atualizar_cabecalho(USUARIOS_ATIVOS_END, ++num_usuarios_ativos);

        arquivo.seek(arquivo.length());
        long pos = arquivo.getFilePointer();
        arvore.insert(conta.id, pos);
        writeRegistro(registro);
    }

    // -------------------------------------------------------------------//
    // IMPRIME REGISTRO

    public void ler_registro(int id) throws IOException {
        //temp = buscarUsandoArvore(id);
        temp = buscar(id);

        if (temp.id == id && lapide != '*') {
            temp.imprimir();
        }
        else{
            System.out.println("ID nao encontrada.");
        }
    }

    /*
     * temp = buscar(id);
     * 
     * if (temp.id == id)
     * temp.imprimir();
     * else
     * System.out.println("//\n  ======================================\n"
     *                      + "  //     ESTE REGISTRO NAO EXISTE     //\n"
     *                      + "  ======================================\n//");
     */
    

    // -------------------------------------------------------------------//
    // IMPRIME ARQUIVO

    public void imprimir_arquivo() throws IOException {
        temp = new Conta();

        ptr = INICIO_REGISTROS;

        for (int i = 0; i < num_registros && ptr < arquivo.length(); i++, ptr += len_registro + INT_TAM + CHAR_TAM) {

            arquivo.seek(ptr);

            lapide = arquivo.readChar();
            len_registro = arquivo.readInt();

            registro = new byte[len_registro];

            arquivo.read(registro);
            temp.byteArrayInput(registro);

            if (lapide != '*')
                temp.imprimir();
        }
    }

    // -------------------------------------------------------------------//
    // ATUALIZA DADOS DO REGISTRO

    public void atualizar(int id, Conta conta_atualizada) throws IOException {
        temp = buscar(id);
        //Conta temp = buscarUsandoArvore(id); //busca por id na arvore b+
        byte[] novo_registro;

        if (temp.id != id)
            System.out.println("//\n  ======================================\n"
                                + "  //     ESTE REGISTRO NAO EXISTE     //\n"
                                + "  ======================================\n//");
        else {
            conta_atualizada.saldo = temp.saldo;
            conta_atualizada.senha = criptografar(conta_atualizada.senha);
            registro = temp.byteArrayOutput();
            novo_registro = conta_atualizada.byteArrayOutput();

            arquivo.seek(ptr);

            if (registro.length == novo_registro.length)
                writeRegistro(novo_registro);
            else {
                arquivo.writeChar('*');
                atualizar_cabecalho(NUM_EXCLUIDOS_END, ++num_excluidos);
                arvore.delete(id); // deleta na arvore para reinsercao com valor atualizado

                arquivo.seek(arquivo.length());
                long pos = arquivo.getFilePointer(); // nova posicao do registro
                writeRegistro(novo_registro);
                arvore.insert(id, pos); //insercao com ponteiro atualizado
                atualizar_cabecalho(NUM_REGISTROS_END, ++num_registros);
            }
        }
    }

    // -------------------------------------------------------------------//
    // EXCLUI REGISTRO

    public void excluir(int id) throws IOException {
        temp = buscar(id);//buscarUsandoArvore(id); //busca por id na arvore b+

        if (temp.id == id) {
            arquivo.seek(ptro);
            arquivo.writeChar('*');

            atualizar_cabecalho(USUARIOS_ATIVOS_END, --num_usuarios_ativos);
            atualizar_cabecalho(NUM_EXCLUIDOS_END, ++num_excluidos);

            System.out.println("//\n  ===========================================\n"
                                + "  //     REGISTRO EXCLUIDO COM SUCESSO     //\n"
                                + "  ===========================================\n//");
        }

        else
            System.out.println("//\n  ======================================\n"
                                + "  //     ESTE REGISTRO NAO EXISTE     //\n"
                                + "  ======================================\n//");
    }

    // -------------------------------------------------------------------//
    // TRANFERE UM VALOR DE UM REGISTRO PARA OUTRO
    public void tranferir(int id_pagador, int id_recebedor, float valor) throws IOException {
        Conta pagador = buscar(id_pagador);
        long ptr_pagador = ptr;

        Conta recebedor = buscar(id_recebedor);
        long ptr_recebedor = ptr;

        if (pagador.saldo < valor)
            System.out.println("//\n  ================================\n"
                                + "  //     SALDO INSUFICIENTE     //\n"
                                + "  ================================\n//");
        else if (pagador.id != id_pagador)
            System.out.println("//\n  ============================================\n"
                                + "  //     REGISTRO DO PAGADOR NAO EXISTE     //\n"
                                + "  ============================================\n//");
        else if (recebedor.id != id_recebedor)
            System.out.println("//\n  ==============================================\n"
                                + "  //     REGISTRO DO RECEBEDOR NAO EXISTE     //\n"
                                + "  ==============================================\n//");
        else {
            System.out.println("//\n  ================================================\n"
                                + "  //     TRANFERENCIA REALIZADA COM SUCESSO     //\n"
                                + "  ================================================\n//");

            pagador.saldo -= valor;
            pagador.transferenciasRealizadas++;

            recebedor.saldo += valor;
            recebedor.transferenciasRealizadas++;

            arquivo.seek(ptr_pagador);

            registro = pagador.byteArrayOutput();
            writeRegistro(registro);

            arquivo.seek(ptr_recebedor);

            registro = recebedor.byteArrayOutput();
            writeRegistro(registro);
        }

    }

    // -------------------------------------------------------------------//
    // APAGA REGISTROS DO ARQUIVO E CRIA UM NOVO ARQUIVO
    public void resetar_arquivo() throws IOException {
        this.arquivo.close();

        File arquivo = new File(PATH);
        arquivo.delete();

        this.arquivo = new RandomAccessFile(PATH, "rw");
        this.num_registros = 0;
        this.num_usuarios_ativos = 0;
        this.num_excluidos = 0;

        atualizar_cabecalho();

    }

    // -------------------------------------------------------------------//
    // FUNCOES DE ARQUIVO PARA DEIXAR MANIPULACAO DE UM ARQUIVO
    // DENTRO DO OBJETO CRUD MAIS LEGIVEL 
    public void seek(long pos) throws IOException {
        arquivo.seek(pos);
    }

    public long length() throws IOException {
        return arquivo.length();
    }

    public long getFilePointer() throws IOException {
        return arquivo.getFilePointer();
    }

    
    public static String criptografar(String texto)
    {
        String res = "";
        texto = texto.toUpperCase();
        for (int i = 0, j = 0; i < texto.length(); i++)
        {
            char c = texto.charAt(i);
            if (c < 'A' || c > 'Z')
                continue;
            res += (char) ((c + CHAVE.charAt(j) - 2 * 'A') % 26 + 'A');
            j = ++j % CHAVE.length();
        }
        return res;
    }
 
    public static String descriptografar(String texto)
    {
        String res = "";
        texto = texto.toUpperCase();
        for (int i = 0, j = 0; i < texto.length(); i++)
        {
            char c = texto.charAt(i);
            if (c < 'A' || c > 'Z')
                continue;
            res += (char) ((c - CHAVE.charAt(j) + 26) % 26 + 'A');
            j = ++j % CHAVE.length();
        }
        return res;
    }

    static void search(String padrao, String txt, int q)
    {
        int M = padrao.length();
        int N = txt.length();
        int i, j;
        int p = 0; // valor hash do padrao
        int t = 0; // valor hash do texto
        int h = 1;
        int opCount = 0;
  
        //calculando o valor de h
        for (i = 0; i < M - 1; i++)
            h = (h * d) % q;
  
        /*calculando o valor hash do padrao e da 
         * primeira janela de texto
         */
        for (i = 0; i < M; i++) {
            p = (d * p + padrao.charAt(i)) % q;
            t = (d * t + txt.charAt(i)) % q;
        }
  
        // anda com o padrao de texto um por um
        for (i = 0; i <= N - M; i++) {
  
            // checa o valor da janela com o padrao
            //caso bata, faz a compara??ao completa
            if (p == t) {
                /* checa os caracteres */
                for (j = 0; j < M; j++) {
                    if (txt.charAt(i + j) != padrao.charAt(j))
                        break;
                }
  
                // if p == t and pat[0...M-1] = txt[i, i+1, ...i+M-1]
                if (j == M)
                    System.out.println("Padrao encontrado no ID " + txt.charAt(0) + txt.charAt(1) + "\n");
            }
  
            // calcula o valor hash da proxima janela 
            // aproveitando de maneira eficiente
            if (i < N - M) {
                t = (d * (t - txt.charAt(i) * h) + txt.charAt(i + M)) % q;
  
                //converte t para positivo caso negativo
                if (t < 0)
                    t = (t + q);
            }
        }
    }
}


// ===========================================================================================================//
