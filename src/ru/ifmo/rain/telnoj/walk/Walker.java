package ru.ifmo.rain.telnoj.walk;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class Walker {
    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.out.println("usage: java <class> <input> <output>");
        } else {
            try {
                Path inputFilePath = Paths.get(args[0]);
                if (!Files.exists(inputFilePath)) {
                    error("Input file `" + args[0] + "` doesn't exist");
                    return;
                }
                try (BufferedReader reader = Files.newBufferedReader(inputFilePath, StandardCharsets.UTF_8)) {
                    Path outputFilePath = Paths.get(args[1]);
                    try (BufferedWriter writer = Files.newBufferedWriter(outputFilePath, StandardCharsets.UTF_8)) {
                        String c = reader.readLine();
                        while (c != null) {
                            try {
                                writeFileTreeHash(writer, c);
                            } catch (IOException e) {
                                error("Unable to write in file `" + args[1] + "`");
                            }
                            try {
                                c = reader.readLine();
                            } catch (IOException e) {
                                error("Unable to read from file `" + args[0] + "`");
                            }
                        }
                    } catch (IOException e) {
                        error("Wrong format of output file `" + args[1] + "`");
                    }
                } catch (InvalidPathException e) {
                    error("Invalid ouput filename: `" + args[1] + "`");
                } catch (IOException e) {
                    error("Wrong format of input file `" + args[0] +"`");
                }
            } catch (InvalidPathException e) {
                error("Invalid input filename: `" + args[0] + "`");
            }
        }
    }

    private static void writeFileTreeHash(BufferedWriter writer, String fileName) throws IOException {
        try {
            Files.walkFileTree(Paths.get(fileName), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    writeHash(writer, 0, file.toString());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException {
                    if (attr.isRegularFile()) {
                        writeHash(writer, getHashFromFile(file), file.toString());
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (InvalidPathException e) {
            writeHash(writer, 0, fileName);
            error("Invalid format of input file `" + fileName + "`");
        }
    }

    private static void writeHash(BufferedWriter writer, int hash, String fileName) throws IOException {
        writer.write(String.format("%08x %s", hash, fileName));
        writer.newLine();
    }

    private static int getHashFromFile(Path filePath) {
        int hash = 0x811c9dc5;
        try (BufferedInputStream input = new BufferedInputStream(new FileInputStream(filePath.toFile()))) {
            int s, primeVal = 0x01000193;
            while ((s = input.read()) != -1) {
                hash = (hash * primeVal) ^ (s & 0xff);
            }
        } catch (IOException e) {
            hash = 0;
        }
        return hash;
    }

    private static void error(String message) {
        System.err.println(message);
    }
}
