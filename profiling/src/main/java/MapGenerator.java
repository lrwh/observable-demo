import java.io.FileWriter;

public class MapGenerator {
    public static String fileName = "/opt/profiling/profiling.txt";
    public static void main(String[] args) {

        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("");//清空原文件内容
            for (int i = 0; i < 16500000; i++) {
                writer.write("name"+i+":"+i+"\n");
            }
            writer.flush();
            System.out.println("write success!");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
