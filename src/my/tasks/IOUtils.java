package my.tasks;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.List;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;;

public class IOUtils extends Task {

    public static List<String> readFile(String file) throws BuildException {
        Path path = Paths.get(file);
        List<String> lines;
        try {
            lines = Files.readAllLines(path);
        } catch (IOException e) {
            // Failed to read file using utf8 charset, try iso_8859_1 charset
            try {
                lines = Files.readAllLines(path, StandardCharsets.ISO_8859_1);
            } catch (IOException e2) {
                throw new BuildException("Could not read file " + file + ": " + e2.getMessage());
            }
        }
        return lines;
    }

    public static void writeFile(String file, String content) throws BuildException {
        Path path = Paths.get(file);
        Charset utf8 = Charset.forName("UTF8");
        try {
            BufferedWriter writer = Files.newBufferedWriter(path, utf8, WRITE, TRUNCATE_EXISTING);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            throw new BuildException("Could not write file: " + e.getMessage());
        }
    }

}