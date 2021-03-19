package com.sumit.musicdistinct;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.metadata.Metadata;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AllArgsConstructor
@Setter
@Getter
public class MusicMetadata {
    private String fileName;
    private String title;
    private String artists;
    private String composer;
    private String genre;
    private String album;
    private File file;
    private FileName fileNameInst;
    private Set<String> artistSet;

    public MusicMetadata(File file, @Nullable Metadata metadata, String fileName) {
        this.file = file;
        this.fileName = fileName;
        this.fileNameInst = new FileName(fileName);
        if (metadata != null) {
            this.title = metadata.get("title");
            this.artists = metadata.get("xmpDM:artist");
            this.composer = metadata.get("xmpDM:composer");
            this.genre = metadata.get("xmpDM:genre");
            this.album = metadata.get("xmpDM:album");
            this.artistSet = parseAuthors(this.artists);
        }
    }

    private static Set<String> parseAuthors(String artist) {
        Set<String> s = new HashSet<>();
        if (StringUtils.isNotEmpty(artist)) {
            String a = artist.replace(" feat.", ",");
            a = a.replace(" feat", ",");
            a = a.replace(" ft.", ",");
            a = a.replace(" ft", ",");
            a = a.replace(" & ", ",");
            s = new HashSet<>(Arrays.asList(a.split(",")));
        }
        return s;
    }

    @Override
    public boolean equals(Object o) {
        boolean compare = compare(o);
        if (compare)
            System.out.println("true for " + this.toString() + " vs " + o.toString());
        return compare;
    }

    private boolean compare(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MusicMetadata that = (MusicMetadata) o;
        if (this.areMetadataAllNull() && that.areMetadataAllNull()) {
            return this.fileNameInst.equals(that.fileNameInst);
        }
        if (this.areMetadataAllNull()) {
            return compareMetadataWithFileName(this.fileNameInst, that);
        }
        if (that.areMetadataAllNull()) {
            return compareMetadataWithFileName(that.fileNameInst, this);
        }
        return Objects.equals(title, that.title)
                && this.compareArtists(that)
//                && Objects.equals(composer, that.composer)
//                && Objects.equals(genre, that.genre)
//                && Objects.equals(album, that.album)
                ;
    }

    private boolean compareArtists(MusicMetadata metadata) {
        if (StringUtils.isEmpty(this.artists) && StringUtils.isEmpty(metadata.artists))
            return true;

        Collection<String> commonAuthors = CollectionUtils.intersection(this.artistSet, metadata.artistSet);
        return commonAuthors.size() > 0;
    }

    private boolean compareMetadataWithFileName(FileName fileNameInst, MusicMetadata metadata) {
        return Objects.equals(fileNameInst.songTitle, metadata.title)
                && fileNameInst.compareArtists(metadata)
//                && Objects.equals(fileNameInst.author, metadata.artists)
                ;
    }

    @Override
    public int hashCode() {
        return 1;
//        if (this.areMetadataAllNull())
//            return this.fileNameInst.hashCode();
//        else{
//            return Objects.hash(title, artists);
////            return Objects.hash(title, artists, composer, genre);
//        }

    }

    @Override
    public String toString() {
        return "MusicMetadata{" +
                "fileName='" + fileName + '\'' +
                ", title='" + title + '\'' +
                ", artists='" + artists + '\'' +
                ", composer='" + composer + '\'' +
                ", genre='" + genre + '\'' +
                ", album='" + album + '\'' +
                '}';
    }

    public boolean areMetadataAllNull() {
        return StringUtils.isEmpty(this.title) && StringUtils.isEmpty(this.artists);
    }

    public String getFileName() {
        return fileName;
    }

    public String getTitle() {
        return title;
    }

    public String getArtists() {
        return artists;
    }

    public String getComposer() {
        return composer;
    }

    public String getGenre() {
        return genre;
    }

    public String getAlbum() {
        return album;
    }

    public File getFile() {
        return file;
    }

    static class FileName {
        private int num;
        private String author;
        private String songTitle;
        private Set<String> artistSet;

        private static final Pattern pattern = Pattern.compile("^(?<number>\\d+)(?: )*(?:-|\\.)(?: )*(?<author>[^-]+)-( )*(?<titleWithFileExtension>.*)$");

        FileName(String fileName) {
            if (StringUtils.isNotBlank(fileName)) {
                Matcher m = pattern.matcher(fileName);
                if (m.matches()) {
                    String number = m.group("number");
                    num = Integer.parseInt(number);
                    author = m.group("author");
                    String titleWithFileExtension = m.group("titleWithFileExtension");
                    songTitle = titleWithFileExtension.substring(0, titleWithFileExtension.length() - 4);
                    artistSet = MusicMetadata.parseAuthors(author);
                }
            }
        }

        public int getNum() {
            return num;
        }

        public void setNum(int num) {
            this.num = num;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getSongTitle() {
            return songTitle;
        }

        public void setSongTitle(String songTitle) {
            this.songTitle = songTitle;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FileName fileName = (FileName) o;
            return Objects.equals(author, fileName.author)
                    && this.compareArtists(fileName)
//                    && Objects.equals(songTitle, fileName.songTitle)
                    ;
        }

        private boolean compareArtists(MusicMetadata metadata) {
            if (StringUtils.isEmpty(this.author) && StringUtils.isEmpty(metadata.artists))
                return true;

            Collection<String> commonAuthors = CollectionUtils.intersection(this.artistSet, metadata.artistSet);
            return commonAuthors.size() > 0;
        }

        private boolean compareArtists(FileName metadata) {
            if (StringUtils.isEmpty(this.author) && StringUtils.isEmpty(metadata.author))
                return true;

            Collection<String> commonAuthors = CollectionUtils.intersection(this.artistSet, metadata.artistSet);
            return commonAuthors.size() > 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(author, songTitle);
        }
    }
}
