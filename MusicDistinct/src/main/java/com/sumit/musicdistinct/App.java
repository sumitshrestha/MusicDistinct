package com.sumit.musicdistinct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
//import org.apache.tika.parser.mp3.Mp3Parser;
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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        if (args.length < 1) {
            System.out.println("empty arguments");
        } else if ("copyLeftOnes".equals(args[0])) {
            String dirLocation = args[1];
            try {
                System.out.println("reading unique song list");
                final Set<MusicMetadata> uniqueSongList = Collections.unmodifiableSet(Files.walk(Paths.get(dirLocation + "/uniqueSongs/")).filter(Files::isRegularFile).map(Path::toFile).map(App::getMetadata).collect(Collectors.toSet()));
                System.out.println("total unique song list " + uniqueSongList.size());
                String newFiles = dirLocation + "\\newSongs";
                System.out.println("reading new songs");
                List<MusicMetadata> newSongList = Files.walk(Paths.get(newFiles))
                        .filter(Files::isRegularFile)
                        .filter(path -> (StringUtils.lowerCase(path.toString()).endsWith(".mp3") || StringUtils.lowerCase(path.toString()).endsWith(".flac")))
                        .map(Path::toFile).map(App::getMetadata).distinct().collect(Collectors.toList());
                System.out.println("total new unique songs " + newSongList.size());
                System.out.println("comparing unique list with new songs. ");
                newSongList.removeIf(uniqueSongList::contains);
                final String directoryName = newFiles + "\\list-" + System.currentTimeMillis();
                File uniqueDirectory = new File(directoryName);
                if (uniqueDirectory.mkdir()) {
                    System.out.format("copying unique songs into %s folder ", directoryName);
                    for (MusicMetadata metadata : newSongList) {
                        File destination = new File(uniqueDirectory.getPath() + "/" + metadata.getFileName());
                        FileUtils.copyFile(metadata.getFile(), destination);
                    }
                } else System.out.println("directory creation failed");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("error processing new songs");
            }
        } else {
            String dirLocation = args[0];
            try {
                List<MusicMetadata> metadataList = Files.walk(Paths.get(dirLocation))
                        .filter(Files::isRegularFile)
                        .filter(path -> (StringUtils.lowerCase(path.toString()).endsWith(".mp3") || StringUtils.lowerCase(path.toString()).endsWith(".flac")))
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

    private static MusicMetadata getMetadata(File f) {
        String fileName = f.getName();
        try {
//            TikaConfig config = new TikaConfig("/path/to/tika-config.xml");
//            Detector detector = config.getDetector();
//            Parser autoDetectParser = new AutoDetectParser(config);
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
