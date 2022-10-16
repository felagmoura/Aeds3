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
    protected final int INT_TAM = 4;
    protected final int CHAR_TAM = 2;
    protected final String PATH = "hexa_desordenado copy.db";

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

    protected static BTree arvore = new BTree<>();

    // -------------------------------------------------------------------//
    // ------------------------- FUNCOES ---------------------------------//
    // -------------------------------------------------------------------//

    // -------------------------------------------------------------------//
    // CONSTRUTOR

    public CRUD() throws FileNotFoundException, IOException {
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

    // funcao para recriar arvore B+ em memória primária a partir do arquivo de
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

        return conta_procurada;
    }

    //funcao para fazer a busca por ID utilizando os índices na árvore B+
    private Conta buscarUsandoArvore(int id) throws IOException{
        long pos;
        Conta tmp = new Conta();
        Conta conta_procurada = new Conta();
        try{
            pos = (long) arvore.search(id); // pega a posicao do registro
        }catch (Exception e){
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
        temp = buscarUsandoArvore(id);

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
     * + "  //     ESTE REGISTRO NAO EXISTE     //\n"
     * + "  ======================================\n//");
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
        Conta temp = buscarUsandoArvore(id); //busca por id na arvore b+
        byte[] novo_registro;

        if (temp.id != id)
            System.out.println("//\n  ======================================\n"
                    + "  //     ESTE REGISTRO NAO EXISTE     //\n"
                    + "  ======================================\n//");
        else {
            conta_atualizada.id = temp.id;
            conta_atualizada.transferenciasRealizadas = temp.transferenciasRealizadas;
            conta_atualizada.saldo = temp.saldo;

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
        temp = buscarUsandoArvore(id); //busca por id na arvore b+

        if (temp.id == id) {
            arquivo.seek(ptr);
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

    public void seek(long pos) throws IOException {
        arquivo.seek(pos);
    }

    public long length() throws IOException {
        return arquivo.length();
    }

    public long getFilePointer() throws IOException {
        return arquivo.getFilePointer();
    }
}

// ===========================================================================================================//
