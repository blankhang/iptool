package com.github.blankhang.util;

import java.io.File;
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
     * 将指定的ClassPath[Resources]下文件路径的文件读成流返回
     *
     * @param classPathFile Resources下的相对路径
     * @return java.io.InputStream
     * @author blank
     * @see <a href=https://stackoverflow.com/questions/20389255/reading-a-resource-file-from-within-jar>reading-a-resource-file-from-within-jar</a>
     * @since 1.0.0
     */
    public static InputStream getClassPathResource(String classPathFile) {
        // this is the path within the jar file
        InputStream inputStream = FileUtil.class.getResourceAsStream(classPathFile);
        if (inputStream == null) {
            // this is how we load file within editor (eg eclipse)
            inputStream = FileUtil.class.getClassLoader().getResourceAsStream(classPathFile);
        }
        return inputStream;
    }

}
