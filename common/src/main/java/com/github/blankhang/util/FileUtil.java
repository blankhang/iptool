package com.github.blankhang.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 文件工具类
 *
 * @author blank
 **/
public class FileUtil {

    public static InputStream getResourcesFileInputStream(String fileName) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream("" + fileName);
    }

    public static String getPath() {
        return FileUtil.class.getResource("/").getPath();
    }

    public static File createNewFile(String pathName) {
        File file = new File(getPath() + pathName);
        if (file.exists()) {
            file.delete();
        } else {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
        }
        return file;
    }

    public static File readFile(String pathName) {
        return new File(getPath() + pathName);
    }

    /**
     * 将指定文件路径的文件读成流返回
     *
     * @param fileFullPath 文件路径
     * @return java.io.InputStream
     * @throws IOException 异常
     * @author blank
     * @since 1.0.0
     */
    public static InputStream resourceLoader(String fileFullPath) throws IOException {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        return resourceLoader.getResource(fileFullPath).getInputStream();
    }

    /**
     * 将指定的ClassPath[Resources]下文件路径的文件读成流返回
     *
     * @param classPathFile Resources下的相对路径
     * @return java.io.InputStream
     * @throws IOException 异常
     * @author blank
     * @since 1.0.0
     */
    public static InputStream getClassPathResource(String classPathFile) throws IOException {
        ClassPathResource resource = new ClassPathResource(classPathFile);
        return resource.getInputStream();
    }

}
