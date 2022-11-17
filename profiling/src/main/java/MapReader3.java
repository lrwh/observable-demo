import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MapReader3 {

    public static void main(String[] args) throws IOException {

        Long startTime = System.nanoTime();
        Map<String,Long> map = readMap("/opt/profiling/profiling.txt");
        long time = System.nanoTime() - startTime;

        System.out.printf("Read %d elements in %.3f seconds\n",map.size(),time/(Math.pow(10,9)));
    }

    private static Map<String,Long> readMap(String fileName) throws IOException {
        Map<String,Long> map = new HashMap<>(100000);
        try(BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            for (String line ;(line = br.readLine())!=null;){
                int sep = line.indexOf(":");
                String key = trim(line,0,sep);
                String value = trim(line,sep+1,line.length());
                map.put(key,Long.parseLong(value));
            }
        }
        return map;
    }

    private static String trim(String line,int from,int to){
        while (from<to && line.charAt(from) <= ' '){
            from ++;
        }
        while (to > from && line.charAt(to-1) <= ' '){
            to--;
        }
        return line.substring(from,to);
    }
}
