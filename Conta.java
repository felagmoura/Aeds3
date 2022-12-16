import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

//===========================================================================================================//
// CONTA
//===========================================================================================================//

public class Conta {
    //-------------------------------------------------------------------//
    //------------------------- ATRIBUTOS -------------------------------//
    //-------------------------------------------------------------------//
    protected static final String CHAVE = "aeds";
    protected int id;
    protected String nomePessoa;
    protected String[] emails;
    protected String nomeUsuario;
    protected String senha;
    protected String cpf;
    protected String cidade;
    protected int transferenciasRealizadas;
    protected float saldo;

    //-------------------------------------------------------------------//
    //------------------------- FUNCOES ---------------------------------//
    //-------------------------------------------------------------------//

    //-------------------------------------------------------------------//
    // CONSTRUTOR

    public Conta () {
        this(null,  null, null, null, null, null, 0F);
    }

    public Conta (String nomePessoa, String[] emails, String nomeUsuario, String senha, String cpf, String cidade, float saldo) {
        this.id = -1;
        this.nomePessoa = nomePessoa;
        this.emails = emails;
        this.nomeUsuario = nomeUsuario;
        this.senha = senha;
        this.cpf = cpf;
        this.cidade = cidade;
        this.transferenciasRealizadas = 0;
        this.saldo = saldo;
    }

    public Conta (String nomePessoa, String[] emails, String nomeUsuario, String senha, String cpf, String cidade) {
        this.id = -1;
        this.nomePessoa = nomePessoa;
        this.emails = emails;
        this.nomeUsuario = nomeUsuario;
        this.senha = senha;
        this.cpf = cpf;
        this.cidade = cidade;
        this.transferenciasRealizadas = 0;
        this.saldo = 0;
    }

    public int getID(){
        return id;
    }

    //-------------------------------------------------------------------//
    // TRANFORMA O OBJETO EM UM ARRAY DE BITES

    public byte[] byteArrayOutput () throws IOException {
        int num_emails = emails.length;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(id);
        dos.writeUTF(nomePessoa);
        dos.writeInt(num_emails);
        for (int i = 0; i < num_emails; i++)
            dos.writeUTF(emails[i]);
        dos.writeUTF(nomeUsuario);
        dos.writeUTF(senha);
        dos.writeUTF(cpf);
        dos.writeUTF(cidade);
        dos.writeInt(transferenciasRealizadas);
        dos.writeFloat(saldo);

        return baos.toByteArray();
    }

    //-------------------------------------------------------------------//
    // TRANFORMA UM ARRAY DE BITES EM UM OBJETO

    public void byteArrayInput (byte ba[]) throws IOException { 
        int num_emails;

        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);
        
        this.id = dis.readInt();
        this.nomePessoa = dis.readUTF();
        num_emails = dis.readInt();
        this.emails = new String[num_emails];
        for (int i = 0; i < num_emails; i++)
            this.emails[i] = dis.readUTF();
        this.nomeUsuario = dis.readUTF();
        this.senha = dis.readUTF();
        this.cpf = dis.readUTF();
        this.cidade = dis.readUTF();
        this.transferenciasRealizadas = dis.readInt();
        this.saldo = dis.readFloat();
    }

    //-------------------------------------------------------------------//
    // IMPRIME O OBJETO
    public void imprimir () {
        String emails = emails_to_String();
        DecimalFormat df = new DecimalFormat("#,##0.00");

        System.out.println("//\n// ID: " + this.id
                        + " // Nome: " + this.nomePessoa
                        + " // Emails: " + emails
                        + "// Usuario: " + this.nomeUsuario
                        + " // Senha: " + descriptografar(this.senha)
                        + " // CPF: " + this.cpf
                        + " // Cidade: " + this.cidade
                        + " // Transferencias: " + this.transferenciasRealizadas
                        + " // Saldo: " + df.format(this.saldo) + " //\n//");
    }

    public String adicionaString() {
        String emails = emails_to_String();
        String resp = "";
        DecimalFormat df = new DecimalFormat("#,##0.00");

        return + this.id
        + " " + this.nomePessoa
        + " " + emails
        + " " + this.nomeUsuario
        + " " + descriptografar(this.senha)
        + " " + this.cpf
        + " " + this.cidade
        + " " + this.transferenciasRealizadas
        + " " + df.format(this.saldo) + " //\n//";
    }

    //-------------------------------------------------------------------//
    // TRANFORMA O ARRAY DE EMAILS EM UMA STRING
    public String emails_to_String () {
        String emails = new String();

        for (int i = 0; i < this.emails.length; i++) 
            emails += "/ " + i + " " + this.emails[i] + " ";
        return emails;
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
}

//===========================================================================================================//