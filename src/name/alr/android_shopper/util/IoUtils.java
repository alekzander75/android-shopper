package name.alr.android_shopper.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import android.os.Environment;

/**
 * @author alopezruiz@gmail.com (Alejandro Lopez Ruiz)
 */
public class IoUtils {

    public static boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    public static List<String> toStrings(File file) {
        List<String> lines = new LinkedList<String>();
        try {
            FileInputStream stream = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            } finally {
                reader.close();
            }
        } catch (Exception exception) {
            throw new RuntimeException("Failed to read file.", exception);
        }
        return lines;
    }

}
