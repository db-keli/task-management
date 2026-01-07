package org.example.utils;

import org.example.exceptions.FileNotAvailableException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileUtils {

    public static boolean isFileAvailable(String filePath) throws FileNotAvailableException {
        File file = new File(filePath);

        if(!file.exists() || !file.isFile()) {
            throw new FileNotAvailableException("file does not exist");
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            return true;
        } catch (IOException e) {
            throw new FileNotAvailableException("File cannot be opened: " + filePath);
        }
    }
}
