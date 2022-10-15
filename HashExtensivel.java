import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class HashExtensivel {
    Diretorio diretorio;

    RandomAccessFile arqIndex;
    final String PATH = "index.db";
    long ptr;

    HashExtensivel (int profundidade, int bucket_tamanho) throws FileNotFoundException {
        arqIndex = new RandomAccessFile(PATH, "rw");
        diretorio = new Diretorio(profundidade, bucket_tamanho);
    }
    
    static class Bucket {
        int profundidade;
        int tamanho;
        Map <Integer, Long> registros = new HashMap<Integer, Long>();

        Bucket (int profundidade, int tamanho) {
            this.profundidade = profundidade;
            this.tamanho = tamanho;
        }

        int inserir (int id, long end) throws IOException {            
            if (bucket_cheio()) return 0;
            
            for (Map.Entry <Integer, Long> it : registros.entrySet())
                if (it.getKey() == id) return -1;

            registros.put(id, end);
            return 1;
        }

        boolean remover (int id) {            

            if (registros.get(id) == null)
                return false;
            else
                registros.remove(id);
            return true;
        }

        boolean atualizar (int id, long end) {
            if (registros.get(id) == null)
                return false;
            else
                registros.replace(id, end);
            return true;
        }

        void imprimir () {
            for (Map.Entry <Integer, Long> it : registros.entrySet())
                System.out.print("[" + it.getKey() + "; " + it.getValue() + "]");
            System.out.println("\n");
        }

        boolean bucket_cheio () {
            if (registros.size() == tamanho) 
                return true;
            else
                return false;
        }

        boolean bucket_vazio () {
            if (registros.size() == 0) 
                return true;
            else
                return false;
        }
    
        int getProfundidade () {
            return profundidade;
        }

        int aumentaProfundidade () {
            return ++profundidade;
        }

        int diminuiProfundidade () {
            return --profundidade;
        }
    
        Map <Integer, Long> copy () {
            Map <Integer, Long> temp = registros;
            return temp;
        }

        void clear () {
            registros.clear();
        }
    }

    static class Diretorio {
        int profundidade_global;
        int bucket_tamanho;
        ArrayList <Bucket> buckets = new ArrayList<Bucket>();

        Diretorio (int profundidade, int bucket_tamanho) {
            this.profundidade_global = profundidade;
            this.bucket_tamanho = bucket_tamanho;

            for (int i = 0; i < 1 << profundidade; i++) {
                buckets.add(new Bucket(profundidade, bucket_tamanho));
            }
        }

        int hash (int id) {
            return id & ((1 << profundidade_global) - 1);
        }

        int pairIndex (int bucket_pos, int profundidade) {
            return bucket_pos ^ (1 << (profundidade - 1));
        }

        String bucket_id (int pos) {
            int profundidade = buckets.get(pos).getProfundidade();
            String s = new String();

            while (pos > 0 && profundidade > 0) {
                s = (pos % 2 == 0 ? "0" : "1") + s;
                pos /= 2;
                profundidade--;
            }

            while (profundidade > 0) {
                s = "0" + s;
                profundidade--;
            }

            return s;
        }

        void grow (int profundidade_local) {
            for (int i = 0; i < 1 << profundidade_global; i++)
                buckets.add(new Bucket(profundidade_local, bucket_tamanho));
            profundidade_global++;
        }

        void shrink () {
            int i;

            for( i = 0 ; i < buckets.size() ; i++ ) {
                if(buckets.get(i).getProfundidade() == profundidade_global) {
                    return;
                }
            }

            profundidade_global--;

            for(i = 0 ; i < 1 << profundidade_global ; i++ )
                buckets.remove(buckets.size() - 1);
        }

        void inserir (int id, long end, boolean reinserido) throws IOException {
            int status;
            int bucket_pos = hash (id);
            
            status = buckets.get(bucket_pos).inserir(id, end);

            if (status == 1) {
                if (!reinserido) 
                    System.out.println("Chave " + id + " inserida em: " + bucket_id(bucket_pos));
                else
                    System.out.println("movido");
            }

            else if (status == 0) {
                split (bucket_pos);
                inserir (id, end, reinserido);
            }

            else 
                System.out.println("id já existe");

        }
    
        void remover (int id, int modo) {
            int bucket_no = hash(id);

            if(buckets.get(bucket_no).remover(id))
                System.out.println("Removido");
            
            if(modo > 0) {
                if(buckets.get(bucket_no).bucket_vazio() && buckets.get(bucket_no).getProfundidade() > 1)
                    merge(bucket_no);
            }
            if(modo > 1) {
                shrink();
            }
        }

        void atualizar (int id, long end) {
            int bucket_pos = hash(id);
            buckets.get(bucket_pos).atualizar(id, end);
        }

        void imprimir () {
            int i, j;
            int profundidade_local;

            System.out.println("Profundidade Global: " + profundidade_global);

            for (i = 0; i < buckets.size(); i++) {
                profundidade_local = buckets.get(i).getProfundidade();
                
                for (j = profundidade_local; j < profundidade_global; j++) {
                    System.out.print(" " + i + " => ");
                    buckets.get(i).imprimir();
                }
            }
        }

        void split (int bucket_pos) throws IOException {
            int profundidade_local;
            int index_novo;
            int index_diff;
            int dir_size;
            int i;

            Map <Integer, Long> temp = new HashMap<Integer, Long>();
            

            profundidade_local = buckets.get(bucket_pos).aumentaProfundidade();
            if (profundidade_local > profundidade_global) grow(profundidade_local);

            index_novo = pairIndex(bucket_pos, profundidade_local);

            temp = buckets.get(bucket_pos).copy();
            buckets.get(bucket_pos).clear();

            for (Map.Entry <Integer, Long> it : temp.entrySet()) {
                if (hash(it.getKey()) == bucket_pos)
                    buckets.get(bucket_pos).inserir(it.getKey(), it.getValue());
                else 
                    buckets.get(index_novo).inserir(it.getKey(), it.getValue());
            }
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            index_diff = 1 << profundidade_local;
            dir_size = 1 << profundidade_global;

            for ( i = index_novo - index_diff; i >= 0; i -= index_diff)
                buckets.add(i, buckets.get(index_novo));
            for ( i = index_novo + index_diff; i < dir_size; i += index_diff)
                buckets.add(i, buckets.get(index_novo));
            for (Map.Entry <Integer, Long> it : temp.entrySet())
                inserir( it.getKey(), it.getValue(), true);

        }
    
        void merge (int bucket_pos) {
            int profundidade_local;
            int pair_index;
            int index_diff;
            int dir_size;
            int i;

            profundidade_local = buckets.get(bucket_pos).getProfundidade();
            pair_index = pairIndex(bucket_pos, profundidade_local);
            index_diff = 1 << profundidade_local;
            dir_size = 1 << profundidade_global;

            if( buckets.get(pair_index).getProfundidade() == profundidade_local )
            {
                buckets.get(pair_index).diminuiProfundidade();
                buckets.remove(bucket_pos);
                buckets.add(bucket_pos, buckets.get(pair_index));
                for( i = bucket_pos - index_diff; i >= 0; i -= index_diff)
                    buckets.add(i, buckets.get(pair_index));
                for( i = bucket_pos + index_diff; i < dir_size; i += index_diff )
                    buckets.add(i, buckets.get(pair_index));
            }
        }
    

    }
}
