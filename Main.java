import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;

//===========================================================================================================//
// MAIN
//===========================================================================================================//

public class Main {
    protected static final String PATH = "hexa_desordenado copy.db";

    public static enum Operacao {
        CriarConta, Transferencia, Ler_Registro, Atualizar, Deletar, Encerrar, Imprimir_Arquivos, Reiniciar_Arquivo, Ordenar, Comprimir, Descomprimir;
    }

    static Scanner scr = new Scanner (System.in, "UTF8");
    public static void main(String[] args) throws FileNotFoundException, IOException, Exception { 
        char opcao;
        int id;
        int id_pagador;
        int id_recebedor;
        float valor_transferencia;

        Conta conta;
        Operacao op;
        
        CRUD arquivo = new CRUD(PATH);
        LZW lzw = new LZW(PATH);

        //arquivo.createBTree();
        OrdenacaoExterna ordenar = new OrdenacaoExterna (arquivo);
                       
        do { // executa operacao a partir do comando digitado
            opcao = menu_opcoes ();
            op = determina_operacao(opcao);
            
            switch (op) {
                case CriarConta:
                    conta = preencher_dados_conta(new Conta(), arquivo);
                    arquivo.inserir(conta);
                    System.out.println("\n//\n  ======================================\n"
                                        + "  //     CONTA CRIADA COM SUCESSO     //\n"
                                        + "  ======================================\n//\n");
                    break;
    
                case Transferencia:
                    System.out.println("// ID DO PAGADOR:\n//");
                    id_pagador = get_id();
                                
                    System.out.println("// -------------------------- //\n"
                                    + "// ID DO RECEBEDOR:\n//");                    
                    id_recebedor = get_id();
            
                    System.out.print("// -------------------------- //\n"
                                    + "// VALOR TRANSFERIDO:\n// ");
                    valor_transferencia = Float.parseFloat(scr.nextLine());

                    arquivo.tranferir(id_pagador, id_recebedor, valor_transferencia);
                    break;
                
                case Ler_Registro:
                    id = get_id();
                    arquivo.ler_registro(id);
                    break;

                case Imprimir_Arquivos:
                    arquivo.imprimir_arquivo();
                    break;
                case Atualizar:
                    id = get_id();
                    conta = atualizar_dados(new Conta(), arquivo);
                    arquivo.atualizar(id, conta);
                    break;
                    
                case Deletar:
                    id = get_id();
                    arquivo.excluir(id);
                    break;

                case Ordenar:
                    ordenar.intercalacao_balanceada();
                    break;
                
                case Reiniciar_Arquivo:
                    arquivo.resetar_arquivo();
                    break;

                case Comprimir:
                    lzw.comprimir();
                    lzw.descomprimir(); 
                    break;

                case Descomprimir:
                    // TODO    
                    break;
                
                case Encerrar:
                    break;
            }
        } while (op != Operacao.Encerrar);
        
        scr.close();
    }

    // -------------------------------------------------------------------//
    // ------------------------- FUNCOES ---------------------------------//
    // -------------------------------------------------------------------//

    // -------------------------------------------------------------------//
    // CARREGA MENU DE OPCOES NO TERMINAL
    public static char menu_opcoes () {
        char opcao;
        
        System.out.print("//=======================================================//\n"
                        + "// MENU PRINCIPAL\n"
                        + "//=======================================================//\n"
                        + "// para:                 digite:\n"
                        + "//\n"
                        + "// Criar Conta: ----------- [C/c]\n"
                        + "// Fazer Transferencia: --- [T/t]\n"
                        + "// Ler Registro: ---------- [L/l]\n"
                        + "// Imprimir Arquivo: ------ [I/i]\n"
                        + "// Ordenar Arquivo: ------- [O/o]\n"
                        + "// Atualizar Registro: ---- [A/a]\n"
                        + "// Deletar Registro: ------ [D/d]\n"
                        + "// Comprimir Arquivo: ----- [Z/z]\n"
                        + "// Descomprimir Arquivo: -- [U/u]\n"
                        + "// Encerrar Sessao: ------- [E/e]\n"
                        + "//=======================================================//\n"
                        + "//\n"
                        + "// ");

        opcao = scr.nextLine().toUpperCase().charAt(0);

        System.out.println("//");
        return opcao;
    }

    // -------------------------------------------------------------------//
    // DADO O COMANDO DO USUARIO RETORNA O VALOR ENUM QUE REPRESENTA A OPERACAO
    public static Operacao determina_operacao (char opcao) throws Exception, IOException {
        Operacao op = null;

        switch (opcao) {
            case 'C':
                System.out.println("// -------------------------- //\n"
                                + "// CRIAR CONTA\n"
                                + "// -------------------------- //\n"
                                + "//");
                op = Operacao.CriarConta;
                break;
            
            case 'T':
                System.out.println("// -------------------------- //\n"
                                + "// FAZER TRANSFERENCIA\n"
                                + "// -------------------------- //");
                op = Operacao.Transferencia;
                break;
            
            case 'L':
                System.out.println("// -------------------------- //\n"
                                + "// LER REGISTRO\n"
                                + "// -------------------------- //\n"
                                + "//");
                op = Operacao.Ler_Registro;
                break;
            
            case 'I':
                System.out.println("// ------------------------------------ //\n"
                                + "// IMPRIMIR TODO CONTEUDO DO ARQUIVO\n"
                                + "// ------------------------------------ //\n"
                                + "//");
                op = Operacao.Imprimir_Arquivos;
                break;

            case 'O':
                System.out.println("// ------------------------------------ //\n"
                                + "// ORDENAR ARQUIVO\n"
                                + "// ------------------------------------ //\n"
                                + "//");
                op = Operacao.Ordenar;
                break;
            
            case 'A':
                System.out.println("// -------------------------- //\n"
                                + "// ATUALIZAR REGISTRO\n"
                                + "// -------------------------- //\n"
                                + "//");
                op = Operacao.Atualizar;
                break;
            
            case 'D':
                System.out.println("// -------------------------- //\n"
                                + "// DELETAR REGISTRO\n"
                                + "// -------------------------- //\n"
                                + "//");
                op = Operacao.Deletar;
                break;

            case 'E':
                System.out.println("//=========================================================================================//\n"
                                + "// PROGRAMA ENCERRADO\n"
                                + "//=========================================================================================//\n");
                op = Operacao.Encerrar;
                break;

            case 'R':
                System.out.println("//=========================================================================================//\n"
                                + "// ARQUIVO RESETADO\n"
                                + "//=========================================================================================//\n");
                op = Operacao.Reiniciar_Arquivo;
                break;
            
            case 'Z':
                System.out.println("//=========================================================================================//\n"
                                + "// ARQUIVO COMPRIMIDO\n"
                                + "//=========================================================================================//\n");
                op = Operacao.Comprimir;
                break;

            case 'U':
                // TODO
                op = Operacao.Descomprimir;
                break;

            
            default:
                throw new Exception ("\n  =========================================================\n"
                                    + "  //     DIGITE SOMENTE OS CHAR QUE APARECEM NO MENU     //\n"
                                    + "  =========================================================\n");
        }

        return op;
    }

    // -------------------------------------------------------------------//
    // COLETAm OS DADOS DO USUARIO
    public static Conta preencher_dados_conta (Conta conta, CRUD arquivo) throws IOException, Exception {
        System.out.print("// Preencha com seus dados: \n"
                        + "// Nome: ");
        conta.nomePessoa = get_nome (scr.nextLine());

        System.out.println("// Emails:");
        conta.emails = get_emails ();

        System.out.print("// Usuario: ");
        conta.nomeUsuario = get_user (scr.nextLine(), arquivo);

        System.out.print("// Senha: ");
        conta.senha = get_senha (scr.nextLine());

        System.out.print("// CPF: ");
        conta.cpf = get_cpf(scr.nextLine());

        System.out.print("// Cidade: ");
        conta.cidade = get_cidade(scr.nextLine());

        System.out.print("// Saldo: ");
        conta.saldo = get_saldo(scr.nextLine());

        return conta;
    }

    public static Conta atualizar_dados (Conta conta, CRUD arquivo) throws IOException, Exception {
        System.out.print("// Preencha com seus dados: \n"
                        + "// Nome: ");
        conta.nomePessoa = get_nome (scr.nextLine());

        System.out.println("// Emails:");
        conta.emails = get_emails ();

        System.out.print("// Usuario: ");
        conta.nomeUsuario = get_user (scr.nextLine(), arquivo);

        System.out.print("// Senha: ");
        conta.senha = get_senha (scr.nextLine());

        System.out.print("// CPF: ");
        conta.cpf = get_cpf(scr.nextLine());

        System.out.print("// Cidade: ");
        conta.cidade = get_cidade(scr.nextLine());

        return conta;
    }
    
    // -------------------------------------------------------------------//
    // FUNCOES QUE RECEBEM OU TRATAM INPUTS DO USUARIO
    public static int get_id () throws Exception {
        String input;

        System.out.print("// Digite uma ID: \n"
                        + "// ID: ");
        input = scr.nextLine();

        for (char c : input.toCharArray()) 
                if (!(c >= '0' && c <= '9'))
                    throw new Exception("\n  ====================================\n"
                                        + "  //     DIGITE SOMENTE NUMEROS     //\n"
                                        + "  ====================================\n");

        return Integer.parseInt(input);
    }

    private static String get_nome (String input) throws Exception {
        if (!valida_nome (input))
            throw new Exception("\n  ===========================\n"
                                + "  //     NOME INVALIDO     //\n"
                                + "  ===========================\n");
        else 
            return input;
    }

    private static boolean valida_nome (String input) {
        boolean valido = true;
        
        for (char c : input.toCharArray()) 
            if (!(c == ' ' || c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z'))
                valido = false; 
    

        return valido && valida_str (input.length());
    }

    private static String[] get_emails () throws Exception {
        int num_emails;
        String[] inputs;

        System.out.print("// Quantos emails serao cadastrados?\n"
                        + "// ");
        num_emails = get_numEmail (scr.nextLine());
        inputs = new String[num_emails];

        for (int i = 0; i < num_emails; i++) {
            System.out.print("// Email: ");
            inputs[i] = scr.nextLine();

            if (!valida_email (inputs[i]))
                throw new Exception("\n  ============================\n"
                                    + "  //     EMAIL INVALIDO     //\n"
                                    + "  ============================\n");
        }

        return inputs;
    }

    private static int get_numEmail (String input) throws Exception {
        boolean num = true;
        for (char c : input.toCharArray()) 
                if (!(c >= '0' && c <= '9'))
                    num = false;
        if (!num) 
            throw new Exception("\n  ====================================\n"
                                + "  //     DIGITE SOMENTE NUMEROS     //\n"
                                + "  ====================================\n");
        else 
            return Integer.parseInt(input);        
    }

    private static boolean valida_email (String input) {
        boolean valido = false;
        
        if (input.contains("@"))
            valido = true;
        
        return valido && valida_str (input.length());
    }

    private static String get_user (String input, CRUD arquivo) throws Exception, IOException {
        if (!valida_str (input.length()))
            throw new Exception ("\n  ==============================\n"
                                + "  //     USUARIO INVALIDO     //\n"
                                + "  ==============================\n");
        
        if (!arquivo.valida_usuario(input))
            throw new Exception ("\n ====================================\n"
                                + "  //     ESTE USUARIO JA EXISTE     //\n"
                                + "  ====================================\n");
        return input;
    }

    private static String get_senha (String input) throws Exception {
        if (!valida_str (input.length()))
            throw new Exception ("\n  ============================\n" 
                                + "  //     SENHA INVALIDA     //\n"
                                + "  ============================\n");
        return input;
    }

    private static String get_cpf (String input) throws Exception {
        if (!valida_str (input.length()) && input.length() == 11)
            throw new Exception ("\n  ==========================\n"
                                + "  //     CPF INVALIDO     //\n"
                                + "  ==========================\n");
        return input;
    }

    private static String get_cidade (String input) throws Exception {
        if (!valida_str (input.length()))
            throw new Exception ("\n  =============================\n"
                                + "  //     CIDADE INVALIDA     //\n"
                                + "  =============================\n");
        return input;
    }

    private static float get_saldo (String input) throws Exception {
        boolean num = true;
        for (char c : input.toCharArray()) 
                if (!(c >= '0' && c <= '9'))
                    num = false;
        if (!num) 
            throw new Exception("\n  ====================================\n"
                                + "  //     DIGITE SOMENTE NUMEROS     //\n"
                                + "  ====================================\n");
        else 
            return Float.parseFloat(input);
    }

    private static boolean valida_str (int length) {
        return length > 0;
    }
    // -------------------------------------------------------------------//
}
