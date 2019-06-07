package com.dyukov.vkregbot.util.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtil {

    private static final String SID_FILE_NAME = "sessionId.sid";

    private static volatile FileUtil instance;

    private FileUtil() {
    }

    public static FileUtil getInstance() {
        synchronized (FileUtil.class) {
            if (instance == null) {
                instance = new FileUtil();
            }
        }
        return instance;
    }

    public void persistSessionCode(String code) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(SID_FILE_NAME));
        writer.write(code);
        writer.close();
    }

    public String readSessionCode() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(SID_FILE_NAME));
        String code = reader.readLine();
        reader.close();
        return code;
    }

}
