package org.g5.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class LineWriter {

    private final ArrayList<String> lines = new ArrayList<>();
    private final File file;

    public LineWriter(File file) {
        this.file = file;
        initData();
    }

    private void initData() {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeLine(String line, int lineIndex) {

        if (lineIndex < 0 || lineIndex > lines.size()) {
            throw new IllegalArgumentException("Invalid lineIndex: " + lineIndex);
        }

        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, false))) {
            for (int i = 0; i < lines.size(); i++) {
                if (i != lineIndex) {
                    bufferedWriter.write(lines.get(i) + '\n');
                } else {
                    bufferedWriter.write(line + '\n');
                }
            }

            if (lineIndex == lines.size()) {
                bufferedWriter.write(line);
                bufferedWriter.newLine();
            }

            if (lineIndex < lines.size()) {
                lines.set(lineIndex, line);
            } else {
                lines.add(line);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getLine(int lineIndex) {
        if (lineIndex < 0 || lineIndex >= lines.size())
            return "";

        return lines.get(lineIndex);
    }
}