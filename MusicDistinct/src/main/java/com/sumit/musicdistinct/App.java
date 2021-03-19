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

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
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
            try {
                List<MusicMetadata> metadataList = Files.list(Paths.get(dirLocation))
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".mp3"))
                        .map(Path::toFile).map(App::getMetadata).distinct().collect(Collectors.toList());
                System.out.println("total music " + metadataList.size());
                File uniqueDirectory = new File(dirLocation + "/list");
                if (uniqueDirectory.mkdir()) {
                    System.out.println("copying unique songs into /list folder");
                    for (MusicMetadata metadata : metadataList) {
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

    private static MusicMetadata getMetadata(@Nonnull File f) {
        String fileName = f.getName();
        try {
            InputStream input = new FileInputStream(f);
            ContentHandler handler = new DefaultHandler();
            Metadata metadata = new Metadata();
            Parser parser = new Mp3Parser();

            ParseContext parseCtx = new ParseContext();
            parser.parse(input, handler, metadata, parseCtx);
            input.close();
            return new MusicMetadata(f, metadata, fileName);
        } catch (IOException | TikaException | SAXException e) {
            System.out.println("exception thrown for " + f.getName());
            e.printStackTrace();
            return new MusicMetadata(f, null, fileName);
        }
    }
}
