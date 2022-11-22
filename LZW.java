import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;

public class LZW {
    protected final String PATH_IN, PATH_OUT;
    protected final RandomAccessFile ORIGINAL, COMPRIMIDO;

    protected final HashMap <Integer, byte[]> DICIONARIO;
    
    LZW (String path) throws FileNotFoundException, IOException {
        this.PATH_IN = path;
        this.PATH_OUT = path.replace(".db", "_LZW.db");

        ORIGINAL = new RandomAccessFile (PATH_IN, "r");
        COMPRIMIDO = new RandomAccessFile (PATH_OUT, "rw");

        DICIONARIO = new HashMap<Integer, byte[]>();
    }

    public void comprimir () throws IOException {
        ORIGINAL.seek(0);

        for (int i = 0; i < 256; i++)
            DICIONARIO.put(i, intToByteArray(i));

        comprimir(DICIONARIO.size(), ORIGINAL.length());
    }
    
    /*  
        1) Initialize the dictionary (with the first 256 entries).
        2) [prefix] ← [empty]
        3) B ← next byte in the input.
        4) Is the string [prefix]B in the dictionary?
            # Yes:
                5) [prefix] ← [prefix]B
            # No:
                6) Add the string [prefix]B to the dictionary.
                7) Output the index of [prefix] to the result.
                8) [prefix] ← B
        9) If there are bytes left in the input, jump to step 3.
        10) Else output the index of [prefix] to the result.
    */

    private void comprimir (int index, long arq_len) throws IOException {
        
        int prefixo_len = 0;
        byte[] prefixo = new byte[prefixo_len];
        byte[] verbete = new byte[prefixo_len + 1];
        byte prox_byte;
        
        while (ORIGINAL.getFilePointer() < arq_len) {            
            prox_byte = ORIGINAL.readByte();

            if (prefixo.length > 0 ) verbete = prefixo.clone();
            verbete[verbete.length - 1] = prox_byte;

            if (DICIONARIO.containsValue(verbete)) {
                prefixo_len++;

                prefixo = new byte[prefixo_len];
                prefixo = verbete.clone();

                verbete = new byte[prefixo_len + 1];
            }

            else {
                DICIONARIO.put(index, verbete);
                COMPRIMIDO.writeInt(index++);

                prefixo_len = 1;
                prefixo = new byte[prefixo_len];
                prefixo[0] = Byte.valueOf(prox_byte);

                verbete = new byte[prefixo_len + 1];
            }

        }
    
        DICIONARIO.clear();
    }

    public void descomprimir () throws IOException {
        
        COMPRIMIDO.seek(0);

        for (int i = 0; i < 256; i++)
            DICIONARIO.put(i, intToByteArray(i));

        descomprimir ( new RandomAccessFile("descomprimido.db", "rw"), COMPRIMIDO.length());
    }

    /* 
        1) Initialize the dictionary (with the first 256 entries).
        2) <index> ← first index value in the input.
        3) Write the string at <index> to the result.
        4) <old> ← <index>
        5) <index> ← next index value in the input.
        6) Does <index> exist in the dictionary?
            # Yes:
                7) Write the string at <index> to the result.
                8) B ← first byte of the string at <index>
                9) Add <old>B to the dictionary.
            # No:
                10) B ← first byte of the string at <old>
                11) Add <old>B to the dictionary.
                12) Write the string for <old>B to the output.
        13) <old> ← <index>
        14) If there are indices left in the input, jump to step 5. 
    */


    private void descomprimir (RandomAccessFile descomprimido, long arq_len) throws IOException {
        int index;
        int prefixo_len;

        byte[] prefixo;
        byte[] verbete;
        byte prim_byte;

        index = COMPRIMIDO.readInt();
        
        prefixo = DICIONARIO.get(index);
        prefixo_len = prefixo.length;
        
        descomprimido.write(prefixo);

        while (COMPRIMIDO.getFilePointer() < arq_len) {
            index = COMPRIMIDO.readInt();

            if (DICIONARIO.containsKey(index)) {
                verbete = DICIONARIO.get(index);
                descomprimido.write(verbete);

                prim_byte = Byte.valueOf(verbete[0]);

                verbete = new byte [prefixo_len + 1];
                verbete = prefixo.clone();
                verbete[verbete.length] = Byte.valueOf(prim_byte);

                DICIONARIO.put(DICIONARIO.size(), verbete);
            }

            else {
                prim_byte = Byte.valueOf(prefixo[0]);

                verbete = new byte [prefixo_len + 1];
                verbete = prefixo.clone();
                verbete[verbete.length - 1] = Byte.valueOf(prim_byte);

                descomprimido.write(verbete);
            }

            prefixo = DICIONARIO.get(index);
            prefixo_len = prefixo.length;
        }

    }


    //-------------------------------------------------------------------//
    // TRANFORMA O OBJETO EM UM ARRAY DE BITES

    public byte[] intToByteArray (int i) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(i);
        dos.flush();

        return baos.toByteArray();
    }

    //-------------------------------------------------------------------//
}
