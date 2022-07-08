package person.ziyu.level.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class Utils {

    private Utils() {}

    public static Set<String> getSetFromFile(String filename, String suffix) {
        checkFile(filename, suffix);
        Set<String> ans = new HashSet<>();
        try {
            InputStreamReader isr = new InputStreamReader(Files.newInputStream(Paths.get(filename)), StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(isr, 5 * 1024 * 1024);
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.replaceAll("\n", "");
                line = line.replaceAll("\t", "");
                line = line.replaceAll("\"", "");
                ans.add(line);
            }
            reader.close();
            isr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ans;
    }

    public static void createFile(String filename, String suffix) {
        checkFormat(filename, suffix);
        File file = new File(filename);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void checkFile(String filename, String suffix) {
        checkFormat(filename, suffix);
        if (!new File(filename).exists()) {
            System.out.println("文件 '" + filename + "' 不存在!");
            System.exit(0);
        }
    }

    private static void checkFormat(String filename, String suffix) {
        if (!filename.endsWith(suffix)) {
            System.out.println("文件格式错误，文件名需以 '" + suffix + "' 结尾");
            System.exit(0);
        }
    }

    public static String removeSpecialChar(String line) {
        String[] specialChars = {"\n", "\t", "\""};
        for (String ch : specialChars) {
            line = line.replaceAll(ch, "");
        }
        return line;
    }
}
