package io;

import configs.Codes;
import configs.CsvFileConfig;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

// am facut singleton pentru ca asa am inteles din cerinta
public final class CsvFileWriter {
    private static final CsvFileWriter csvFileWriter = new CsvFileWriter();

    private CsvFileWriter() {
    }

    public static CsvFileWriter getInstance() {
        return CsvFileWriter.csvFileWriter;
    }

    public void saveData(String fileName, List<List<String>> fileLines) {
        try {
            FileWriter fileWriter = new FileWriter(fileName);
            this.write(fileWriter, fileLines);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException exception) {
            exception.printStackTrace();
            System.exit(Codes.EXIT_ON_ERROR);
        }
    }

    private void write(FileWriter fileWriter, List<List<String>> fileLines) {
        Consumer<List<String>> formatAndWriteLine = (line) -> {
            try {
                fileWriter.write(String.join(CsvFileConfig.getFileSeparator(), line) + "\n");
            } catch (IOException exception) {
                exception.printStackTrace();
                System.exit(Codes.EXIT_ON_ERROR);
            }
        };
        fileLines.forEach(formatAndWriteLine);
    }
}
