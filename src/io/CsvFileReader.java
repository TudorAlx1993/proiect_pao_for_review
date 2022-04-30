package io;

import configs.Codes;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class CsvFileReader {
    private static final CsvFileReader csvFileReader = new CsvFileReader();

    private CsvFileReader() {
    }

    public static CsvFileReader getInstance() {
        return CsvFileReader.csvFileReader;
    }

    public List<String> readLines(String fileName) {
        List<String> csvFileLines = new ArrayList<>();
        try {
            final FileReader fileReader = new FileReader(fileName);
            final BufferedReader bufferedReader = new BufferedReader(fileReader);

            String csvFileLine;
            while ((csvFileLine = bufferedReader.readLine()) != null)
                csvFileLines.add(csvFileLine);

            bufferedReader.close();
        } catch (FileNotFoundException exception) {
            exception.printStackTrace();
            System.err.println("File " + fileName + " was not found!");
            System.exit(Codes.EXIT_ON_ERROR);
        } catch (IOException exception) {
            exception.printStackTrace();
            System.exit(Codes.EXIT_ON_ERROR);
        }

        //remove the header of the csv file
        // also remove the end line character
        final String endLineCharacter = "\n";
        return csvFileLines
                .stream()
                .skip(1)
                .map(fileLine -> fileLine.replace(endLineCharacter, ""))
                .collect(Collectors.toList());
    }
}
