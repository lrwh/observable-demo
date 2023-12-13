package com.zy.observable.server.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description
 * @Author liurui
 * @Date 2022/11/10 15:01
 */
@RestController
@RequestMapping("/profiling")
public class ProfilingController {

    private static final Logger logger = LoggerFactory.getLogger(ProfilingController.class);


    @Value("${profiling.file}")
    private String fileName;

    /**
     * @Description
     * @Param dataSize 初始数据容量
     * @return java.lang.String
     **/
    @RequestMapping("/generator")
    public String generatorData(Long dataSize) {
        String filePath;
        try {
            filePath = fileName.substring(0, fileName.lastIndexOf("/"));
            File file = new File(filePath);
            if (!file.exists()){
                file.mkdirs();
            }
        }catch (Exception e){
            logger.error("write error! 文件夹创建异常 ",e);
            return "write error!";
        }
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("");//清空原文件内容
            if (dataSize == null) {
                dataSize = 16200000L;
            }
            for (int i = 0; i < dataSize; i++) {
                writer.write("name" + i + ":" + i + "\n");
            }
            writer.flush();
        } catch (Exception e) {
            logger.error("write error!",e);
            return "write error!";
        }
        return "write success!";
    }

    /**
     * @Description
     * @Param way 执行方法
     * @Param initialCapacity map 初始容量
     * @return java.lang.String
     **/
    @RequestMapping("/mapReader")
    public String mapReader(Integer way,Integer initialCapacity) {
        Long startTime = System.nanoTime();
        Map<String, Long> map = null;
        boolean flag = true;
        try {
            if (way==null){
                way=1;
            }
            logger.info("way:{},initialCapacity:{}",way,initialCapacity);
            switch (way) {
                case 2:
                    map = reader2();
                    break;
                case 3:
                    map = reader3(initialCapacity);
                    break;
                default:
                    map = reader();
                    break;
            }
        } catch (IOException e) {
            logger.error("read error!",e);
            flag = false;
        }
        long time = System.nanoTime() - startTime;
        if (flag) {
            return String.format("Read %d elements in %.3f seconds\n", map.size(), time / (Math.pow(10, 9)));
        } else {
            return String.format("read error in %.3f seconds", time / (Math.pow(10, 9)));
        }
    }

    private Map<String, Long> reader() throws IOException {
        Map<String, Long> map = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            for (String line; (line = br.readLine()) != null; ) {
                String[] kv = line.split(":", 2);
                String key = kv[0].trim();
                String value = kv[1].trim();
                map.put(key, Long.parseLong(value));
            }
        }
        return map;
    }

    private Map<String, Long> reader2() throws IOException {
        Map<String, Long> map = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            for (String line; (line = br.readLine()) != null; ) {
                int sep = line.indexOf(":");
                String key = trim(line, 0, sep);
                String value = trim(line, sep + 1, line.length());
                map.put(key, Long.parseLong(value));
            }
        }
        return map;
    }

    private Map<String, Long> reader3(Integer initialCapacity) throws IOException {
        if (initialCapacity==null){
            initialCapacity = 100000;
        }
        Map<String, Long> map = new HashMap<>(initialCapacity);
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            for (String line; (line = br.readLine()) != null; ) {
                int sep = line.indexOf(":");
                String key = trim(line, 0, sep);
                String value = trim(line, sep + 1, line.length());
                map.put(key, Long.parseLong(value));
            }
        }
        return map;
    }

    private static String trim(String line, int from, int to) {
        while (from < to && line.charAt(from) <= ' ') {
            from++;
        }
        while (to > from && line.charAt(to - 1) <= ' ') {
            to--;
        }
        return line.substring(from, to);
    }


}
