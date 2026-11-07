package com.pdm.loan.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Stores uploaded documents on disk in uploads/<ApplicationID>/<DocumentID>_<file name>.
 * (The schema keeps only IsSupplied / SuppliedDate, not the file itself.)
 */
public final class FileStore {

    public static final long MAX_BYTES = 10L * 1024 * 1024;
    private static final Path ROOT = Path.of("uploads");

    private FileStore() {
    }

    public static void validate(File f) {
        String name = f.getName().toLowerCase(Locale.ROOT);
        if (!(name.endsWith(".pdf") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png"))) {
            throw new IllegalArgumentException(f.getName() + ": only PDF, JPG or PNG files are accepted.");
        }
        if (f.length() > MAX_BYTES) {
            throw new IllegalArgumentException(f.getName() + " is larger than 10 MB.");
        }
    }

    public static Path save(String applicationId, String documentId, File source) throws IOException {
        validate(source);
        Path dir = ROOT.resolve(applicationId);
        Files.createDirectories(dir);
        // keep one file per document: remove an older upload first
        for (Path old : filesOf(applicationId, documentId)) {
            Files.deleteIfExists(old);
        }
        Path target = dir.resolve(documentId + "_" + source.getName());
        Files.copy(source.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
        return target;
    }

    public static List<Path> filesOf(String applicationId, String documentId) throws IOException {
        List<Path> result = new ArrayList<>();
        Path dir = ROOT.resolve(applicationId);
        if (documentId == null || !Files.isDirectory(dir)) {
            return result;
        }
        try (Stream<Path> s = Files.list(dir)) {
            s.filter(p -> p.getFileName().toString().startsWith(documentId + "_")).forEach(result::add);
        }
        return result;
    }

    public static String fileName(String applicationId, String documentId) {
        try {
            List<Path> files = filesOf(applicationId, documentId);
            if (files.isEmpty()) {
                return null;
            }
            String n = files.get(0).getFileName().toString();
            return n.substring(n.indexOf('_') + 1);
        } catch (IOException e) {
            return null;
        }
    }
}
