package com.zy.observable.server.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 通过工具类，生成类列表信息，输出到文件
 */
public class BizClassUtils {

    public static void main(String[] args) throws Exception{
        List<String> classNames = getClassesInPackage("com.zy.observable");
        try (PrintWriter writer = new PrintWriter(new File("methods.txt"))) {
            for (String className : classNames) {
                writer.println(className + "[*]");
                System.out.println(className+"[*]");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getClassesInPackage(String packageName) throws IOException, ClassNotFoundException {
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = ClassLoader.getSystemResources(path);
        List<String> classNames = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            if (resource.getProtocol().equals("file")) {
                // 处理文件系统目录
                classNames.addAll(findClassesInDirectory(new File(resource.getFile()), packageName));
            } else if (resource.getProtocol().equals("jar")) {
                // 处理JAR包中的类
                classNames.addAll(findClassesInJar(resource));
            }
        }
        return classNames;
    }

    private static List<String> findClassesInDirectory(File directory, String packageName) {
        List<String> classNames = new ArrayList<>();
        if (!directory.exists()) return classNames;

        File[] files = directory.listFiles();
        if (files == null) return classNames;

        for (File file : files) {
            if (file.isDirectory()) {
                // 递归处理子目录
                classNames.addAll(findClassesInDirectory(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class") && !file.getName().contains("$")) {
                // 过滤出类文件（排除内部类）
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                classNames.add(className);
            }
        }
        return classNames;
    }

    private static List<String> findClassesInJar(URL jarUrl) throws IOException, ClassNotFoundException {
        List<String> classNames = new ArrayList<>();
        String jarPath = jarUrl.getPath().substring(5, jarUrl.getPath().indexOf("!")); // 提取JAR路径
        try (JarFile jar = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName().replace('/', '.');
                if (name.endsWith(".class") && !name.contains("$")) {
                    String className = name.substring(0, name.length() - 6).replaceAll("BOOT-INF\\.classes\\.","");
                    System.out.println("className:"+name);
                    classNames.add(className);
                }
            }
        }
        return classNames;
    }
}
