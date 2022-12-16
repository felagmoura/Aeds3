class CifraVigenere{
    public static String criptografar(String texto, String CHAVE)
    {
        String res = "";
        texto = texto.toUpperCase(); // coloca em caixa alta
        for (int i = 0, j = 0; i < texto.length(); i++)
        {
            char c = texto.charAt(i);
            if (c < 'A' || c > 'Z')
                continue; // ignora numeros
            res += (char) ((c + CHAVE.charAt(j) - 2 * 'A') % 26 + 'A'); // faz o calculo de vigenere
            j = ++j % CHAVE.length();
        }
        return res;
    }
 
    public static String descriptografar(String texto, String CHAVE)
    {
        String res = "";
        texto = texto.toUpperCase();
        for (int i = 0, j = 0; i < texto.length(); i++)
        {
            char c = texto.charAt(i);
            if (c < 'A' || c > 'Z')
                continue;
            res += (char) ((c - CHAVE.charAt(j) + 26) % 26 + 'A'); // faz o calculo contrario de vigenere
            j = ++j % CHAVE.length();
        }
        return res;
    }
}