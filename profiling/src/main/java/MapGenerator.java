import java.io.File;
import java.io.FileWriter;

public class MapGenerator {
    public static String fileName = "/opt/profiling/profiling.txt";
    public static void main(String[] args) {
        String filePath;
        try {
            filePath = fileName.substring(0, fileName.lastIndexOf("/"));
            File file = new File(filePath);
            if (!file.exists()){
                file.mkdirs();
            }
        }catch (Exception e){
            System.out.println("文件夹创建异常");
            e.printStackTrace();
            return;
        }
        System.out.println(filePath);
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("");//清空原文件内容
            for (int i = 0; i < 16200000; i++) {
                writer.write("name"+i+":"+i+"\n");
            }
            writer.flush();
            System.out.println("write success!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
