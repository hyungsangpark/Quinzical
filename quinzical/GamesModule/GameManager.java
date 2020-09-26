package quinzical.GamesModule;

import javafx.scene.layout.GridPane;
import quinzical.Questions.Category;
import quinzical.Questions.Question;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class GameManager {

    private QuestionBoard _questionBoard;
    private int _currentScore;
    private int _bestScore;

    private static GameManager _instance;

    public GameManager() {
        _instance = this;
        _questionBoard = new QuestionBoard();
        _questionBoard.createBoard();
        loadGame();
    }

    public static GameManager getInstance() {
        return _instance;
    }

    public GridPane getQuestionBoard() {
        return _questionBoard.getQuestionBoard();
    }

    public Question getQuestionInCategory(int categoryIndex, int questionIndex) {
        return _questionBoard.getCategory(categoryIndex).getQuestion(questionIndex);
    }

    public int getCurrentScore() {
        return _currentScore;
    }

    public void incrementCurrentScore(int value) {
        _currentScore += value;
    }

    public void decrementCurrentScore(int value) {
        _currentScore -= value;
    }

    public void updateBestScore() {
        if (_currentScore > _bestScore) {
            _bestScore = _currentScore;
        }
    }

    public int getBestScore() {
        return _bestScore;
    }

    public void newGame() {
        _questionBoard = new QuestionBoard();
        _questionBoard.createBoard();
        _currentScore = 0;
        saveGame();
    }

    public void saveGame() {
        String savePath = new File("").getAbsolutePath();
        savePath+="/save";

        File saveDir = new File(savePath);

        if (!Files.exists(Paths.get(savePath))) {
            saveDir.mkdir();
        }

        savePath+="/save.txt";
        File saveFile = new File(savePath);
        saveFile.delete();

        try {
            saveFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            FileWriter saveWriter = new FileWriter(savePath);
            saveWriter.write(_currentScore + "," + _bestScore + "\n");

            for(int i = 0; i < _questionBoard.getSize(); i++) {
                Category currentCategory = _questionBoard.getCategory(i);
                String saveLine = currentCategory.toString();

                for (int j=0;j<currentCategory.getSize();j++) {
                    Question currentQuestion = currentCategory.getQuestion(j);
                    saveLine += ","+currentQuestion.getLineNumber();
                }

                saveWriter.write(saveLine + "," + currentCategory.getLowestValuedQuestionIndex() + "\n");
            }

            saveWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadGame() {
        String savePath = new File("").getAbsolutePath();
        savePath+="/save";
        savePath+="/save.txt";

        if (Files.exists(Paths.get(savePath))) {
            try {
                List<String> allLines = Files.readAllLines(Paths.get(savePath));
                System.out.println(allLines);
                System.out.println(allLines.size());
                List<String> lineSplit = Arrays.asList(allLines.get(0).split("\\s*,\\s*"));
                _currentScore = Integer.parseInt(lineSplit.get(0));
                _bestScore = Integer.parseInt(lineSplit.get(1));

                _questionBoard = new QuestionBoard();

                for (int i = 1; i < allLines.size(); i++) {
                    lineSplit = Arrays.asList(allLines.get(i).split("\\s*,\\s*"));
                    Category newCategory = new Category(lineSplit.get(0)); // parent
                    _questionBoard.addCategory(newCategory);

                    savePath = new File("").getAbsolutePath()+"/categories/"+lineSplit.get(0)+".txt";
                    List<String> questionLines = Files.readAllLines(Paths.get(savePath));
                    newCategory.setLowestValuedQuestionIndex(Integer.parseInt(lineSplit.get(6)));

                    for(int j = 1; j < 6; j++) {
                        String selectedQuestionLine = questionLines.get(Integer.parseInt(lineSplit.get(j)));
                        List<String> selectedQSplit = Arrays.asList(selectedQuestionLine.split("\\s*\\|\\s*"));
                        // change answer to answer array
                        Question newQuestionToAdd = new Question(selectedQSplit.get(0),selectedQSplit.get(2),newCategory);
                        newQuestionToAdd.set_whatIsThis(selectedQSplit.get(1));
                        newQuestionToAdd.setLineNumber(Integer.parseInt(lineSplit.get(j)));
                        newCategory.addQuestion(newQuestionToAdd);
                        // set parent
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            newGame();
        }
    }
}
