package com.sumit.musicdistinct;

import org.apache.commons.io.FileUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        System.out.println("Hello World!");
        long startTime = System.currentTimeMillis();
        if (args.length < 1) {
            System.out.println("empty arguments");
        } else {
            String dirLocation = args[0];
            HashSet<MusicMetadata> musicSet = new HashSet<>();
            try {
                List<File> files = Files.list(Paths.get(dirLocation))
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".mp3"))
                        .map(Path::toFile).collect(Collectors.toList());
                for (File file : files) {
                    Metadata metadata = getMetadata(file);
                    String fileName = file.getName();
                    boolean f = false;
                    if (fileName.equals("72. The Weeknd - Blinding Lights.mp3")) {
                        System.out.println("72. The Weeknd - Blinding Lights found for :: ");
                        f = true;
                    }
                    if (metadata == null) {
                        if (f)
                            System.out.println("its going on null ");
                        musicSet.add(new MusicMetadata(file, null, fileName));
                    } else {
                        if (f)
                            System.out.println("its going on non null ");
                        musicSet.add(new MusicMetadata(file, metadata, fileName));
                    }
                }

                System.out.println("total music " + musicSet.size());
                File uniqueDirectory = new File(dirLocation + "/list");
                if (uniqueDirectory.mkdir()) {
                    System.out.println("copying unique songs into /list folder");
                    for (MusicMetadata metadata : musicSet) {
                        File destination = new File(uniqueDirectory.getPath() + "/" + metadata.getFileName());
                        FileUtils.copyFile(metadata.getFile(), destination);
                    }

                } else System.out.println("directory creation failed");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        long l = System.currentTimeMillis() - startTime;
        System.out.println("total time " + l / 1000 + " seconds viz. " + String.format(java.util.Locale.US, "%.2f", (((float) l) / 60000)) + " minutes.");
    }

    private static Metadata getMetadata(File f) {
        if (f == null)
            return null;
        try {
            InputStream input = new FileInputStream(f);
            ContentHandler handler = new DefaultHandler();
            Metadata metadata = new Metadata();
            Parser parser = new Mp3Parser();

            ParseContext parseCtx = new ParseContext();
            parser.parse(input, handler, metadata, parseCtx);
            input.close();
            return metadata;
        } catch (IOException | TikaException | SAXException e) {
            System.out.println("exception thrown for " + f.getName());
            e.printStackTrace();
            return null;
        }
    }
}
