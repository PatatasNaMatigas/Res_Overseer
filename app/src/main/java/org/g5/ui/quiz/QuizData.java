package org.g5.ui.quiz;

import android.content.Context;
import android.util.Log;

import org.g5.util.LineWriter;
import org.g5.util.Time;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

public class QuizData {

    private static int[]answer = new int[3];

    private static LineWriter lineWriter;

    public QuizData(Context context) {
        File file = new File(context.getFilesDir(), "quiz_data.txt");
        try {
            if (!file.createNewFile())
                file.createNewFile();
        } catch (IOException e) {}
        lineWriter = new LineWriter(file);
    }

    public static boolean answeredToday() {
        int[] date = Time.ldToDateArray(LocalDate.now());
        return lineWriter.hasLine(date[0] + "_" + date[1] + "_" + date[2]);
    }

    public static void setAnswer(int answer, int qNum) {
        QuizData.answer[qNum] = answer;
    }

    public static int getAnswer(int qNum) {
        return answer[qNum];
    }

    public static int getFinalAnswer(int qNum) {
        return Integer.parseInt(lineWriter.getLine(1).split("\\s*\\|")[qNum]);
    }

    public static void done() {
        int[] date = Time.ldToDateArray(LocalDate.now());
        String answers = answer[0] + "|" + answer[1] + "|" + answer[2];
        lineWriter.writeLine(date[0] + "_" + date[1] + "_" + date[2], 0);
        lineWriter.writeLine(answers, 1);
    }
}
