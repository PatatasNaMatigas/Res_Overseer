package org.g5.ui.quiz;

import android.content.Context;
import android.util.Log;

import org.g5.util.LineIO;
import org.g5.util.Time;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;

public class QuizData {

    private static int[] pastResults;
    private static int[] answer = new int[3];

    private static LineIO lineIO;

    public QuizData(Context context) {
        File file = new File(context.getFilesDir(), "quiz_data.txt");
        try {
            if (!file.createNewFile())
                file.createNewFile();
        } catch (IOException e) {}
        lineIO = new LineIO(file);
    }

    public static boolean answeredToday() {
        int[] date = Time.ldToDateArray(LocalDate.now());
        return lineIO.hasLine(date[0] + "_" + date[1] + "_" + date[2]);
    }

    public static void setAnswer(int answer, int qNum) {
        QuizData.answer[qNum] = answer;
    }

    public static int getAnswer(int qNum) {
        return answer[qNum];
    }

    public static int getFinalAnswer(int qNum) {
        int[] date = Time.ldToDateArray(LocalDate.now());
        return Integer.parseInt(lineIO.getLine(date[0] + "_" + date[1] + "_" + date[2] + ":").split(":")[1].split("\\s*\\|")[qNum]);
    }

    public static int[] getAnswers(LocalDate ld) {
        int[] date = Time.ldToDateArray(ld);
        String[] answers = lineIO.getLine(date[0] + "_" + date[1] + "_" + date[2]).split(":")[1].split("\\s*\\|");
        return new int[] {
                answer[0] = Integer.parseInt(answers[0]),
                answer[1] = Integer.parseInt(answers[1]),
                answer[2] = Integer.parseInt(answers[2])
        };
    }

    public static void done() {
        int[] date = Time.ldToDateArray(LocalDate.now());
        String answers = answer[0] + "|" + answer[1] + "|" + answer[2];
        lineIO.writeLine(date[0] + "_" + date[1] + "_" + date[2] + ":" + answers);
    }
}
