import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class HashExtensivel {
    final char CHAT_TAM = 2;
    final int INT_TAM = 4;
    final long LONG_TAM = 8;
    
    Diretorio diretorio;

    RandomAccessFile arqDir;
    final String PATH_DIR = "0diretorio.db";

    RandomAccessFile arqIndex;
    final String PATH_INDEX = "0index_hash.db";
    long ptr_index;

    final int BUCKET_TAM = 4;   

    int profundidade_global;

    HashExtensivel () throws FileNotFoundException,IOException {
        arqDir = new RandomAccessFile(PATH_DIR, "rw");
        arqIndex = new RandomAccessFile(PATH_INDEX, "rw");

        diretorio = new Diretorio();
    }
    
    class Bucket {
        final int NULO = -1;
        char lapide;
        int id;
        long end;
        int num_registros;
        int profundidade_local;
        long ptr;
        int id_bucket;

        Bucket (int bloco_pos) throws IOException {
            
            profundidade_local = profundidade_global;
            num_registros = 0;
            id = -1;
            end = -1;

            ptr_index = arqIndex.getFilePointer();

            arqIndex.writeInt(bloco_pos);
            arqIndex.writeInt(profundidade_local);
            arqIndex.writeInt(num_registros);

            for (int i = 0; i < BUCKET_TAM; i++) {
                arqIndex.writeChar(' ');
                arqIndex.writeInt(id);
                arqIndex.writeLong(end);
            }
        }

        int inserir (int id, long end) throws IOException {
            long ptr_registros;
            
            if (bucket_cheio()) return 0;

            id_bucket = arqIndex.readInt();
            profundidade_local = arqIndex.readInt();

            ptr = arqIndex.getFilePointer();
            num_registros = arqIndex.readInt();

            for (int i = 0; i < BUCKET_TAM; i++) {
                ptr_registros = arqIndex.getFilePointer();
                
                this.lapide = arqIndex.readChar();
                this.id = arqIndex.readInt();
                this.end = arqIndex.readLong();

                if (this.id == NULO) {
                    arqIndex.seek(ptr_registros);
                    
                    arqIndex.writeChar(lapide);
                    arqIndex.writeInt(id);
                    arqIndex.writeLong(end);

                    break;
                }
                
                if (this.id == id) return -1;
                
                ptr_registros = arqIndex.getFilePointer();
                arqIndex.seek(ptr_registros);
            }
            
            arqIndex.seek(ptr);
            arqIndex.writeInt(++num_registros);

            return 1;
        }

        public int getProfundidade() throws IOException {
            int profundidade;
            
            ptr = arqIndex.getFilePointer() + INT_TAM;
            arqIndex.seek(ptr);

            profundidade = arqIndex.readInt();
            
            return profundidade;
        }

        public int aumentaProfundidade() throws IOException {
            int profundidade;
            
            ptr = arqIndex.getFilePointer() + INT_TAM;
            arqIndex.seek(ptr);

            profundidade = arqIndex.readInt();

            arqIndex.writeInt(++profundidade);
            
            return profundidade;
        }

        public int diminuiProfundidade() throws IOException {
            int profundidade;
            
            ptr = arqIndex.getFilePointer() + INT_TAM;
            arqIndex.seek(ptr);

            profundidade = arqIndex.readInt();

            arqIndex.writeInt(--profundidade);
            
            return profundidade;
        }

        boolean bucket_cheio () {
            return num_registros == BUCKET_TAM;
        }

        boolean bucket_vazio () {
            return num_registros == 0;
        }
    }

    class Diretorio {
        
        ArrayList <Bucket> buckets = new ArrayList<Bucket>();
        Map <Integer, Long> mapDir = new HashMap<Integer, Long>();
        long ptr;

        Diretorio () throws IOException {
            
            if (arqDir.length() == 0) {
                profundidade_global = 1;
                arqDir.writeInt(profundidade_global);

                for (int bucket_pos = 0; bucket_pos < 1 << profundidade_global; bucket_pos++) {
                    ptr = arqDir.getFilePointer();
                    buckets.add(new Bucket(bucket_pos));
                    mapDir.put(bucket_pos, ptr);

                    arqDir.writeInt(bucket_pos);
                    arqDir.writeLong(ptr_index);
                }
            }

            else {
                arqDir.seek(0);
                profundidade_global = arqDir.readInt();
            }
        }

        int hash (int id) {
            return id & ((1 << profundidade_global) - 1);
/*             int bucket_pos = id & ((1 << profundidade_global) - 1);

            ptr = mapDir.get(bucket_pos);

            arqDir.

            int profundidade_local = buckets.get(bucket_pos).getProfundidade();
            
            if (profundidade_local < profundidade_global)
                bucket_pos = bucket_pos & ((1 << profundidade_local) - 1);
            
            return bucket_pos;
 */        }

        void aponta_para_bucket (int bucket_pos) throws IOException {
            ptr = mapDir.get(bucket_pos);
            arqDir.seek(ptr + INT_TAM);

            ptr_index = arqDir.readLong();
            arqIndex.seek(ptr_index);
        }

        int getNovoIndex (int bucket_pos, int profundidade) {
            return bucket_pos ^ (1 << (profundidade - 1));
        }

        void grow () throws IOException {
            int index_novo;

            arqDir.seek(arqDir.length());
            arqIndex.seek(arqIndex.length());
            
            for (int bucket_pos = 0; bucket_pos < 1 << profundidade_global; bucket_pos++) {
                ptr = arqDir.getFilePointer();
                index_novo = getNovoIndex(bucket_pos, profundidade_global + 1);
                
                buckets.add(new Bucket(index_novo));
                mapDir.put(index_novo, ptr);

                arqDir.writeInt(index_novo);
                arqDir.writeLong(mapDir.get(bucket_pos));
            }

            arqDir.seek(0);
            arqDir.writeInt(++profundidade_global);
        }

        void inserir (int id, long end) throws IOException {
            int status;
            int bucket_pos = hash(id);

            
            aponta_para_bucket (bucket_pos);
            status = buckets.get(bucket_pos).inserir(id, end);

            if (status == 1) 
                System.out.println("Chave [" + id + "] inserida no bucket: [" + bucket_pos + "]");
            
            else if (status == 0) {
                split (bucket_pos);
                inserir(id, end);
            }
        }

        void split (int bucket_pos) throws IOException {
            int profundidade_local;
            int index_novo;

            aponta_para_bucket (bucket_pos);
            profundidade_local = buckets.get(bucket_pos).aumentaProfundidade();

            if (profundidade_local > getProfundidadeGlobal()) grow();

            index_novo = getNovoIndex (bucket_pos, profundidade_local);
            mapDir.replace(index_novo, mapDir.get(index_novo), atualizaPtr ());
            //buckets.get(index_novo).aumentaProfundidade();
        }

        int getProfundidadeGlobal () throws IOException {
            arqDir.seek(0);
            return arqDir.readInt();
        }

    }
}
