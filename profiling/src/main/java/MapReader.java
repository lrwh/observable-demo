import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MapReader {

    public static void main(String[] args) throws IOException {

        Long startTime = System.nanoTime();
        Map<String,Long> map = readMap("/opt/profiling/profiling.txt");
        long time = System.nanoTime() - startTime;

        System.out.printf("Read %d elements in %.3f seconds\n",map.size(),time/(Math.pow(10,9)));
    }

    private static Map<String,Long> readMap(String fileName) throws IOException {
        Map<String,Long> map = new HashMap<>();
        try(BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            for (String line ;(line = br.readLine())!=null;){
                String[] kv = line.split(":",2);
                String key = kv[0].trim();
                String value = kv[1].trim();
                map.put(key,Long.parseLong(value));
            }
        }
        return map;
    }
}
