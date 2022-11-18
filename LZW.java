public class LZW {
    protected final String PATH_IN, PATH_OUT;
    
    LZW (String path) {
        this.PATH_IN = path;
        this.PATH_OUT = path.concat("_LZW");

    }
}
