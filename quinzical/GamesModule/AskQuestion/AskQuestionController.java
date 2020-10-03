package quinzical.GamesModule.AskQuestion;

import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import quinzical.GamesModule.GameManager;
import quinzical.GamesModule.GamesMenuController;
import quinzical.GamesModule.SelectQuestion.SelectQuestionController;
import quinzical.Questions.Question;
import quinzical.Utilities.AskQuestionUtilities;

import java.net.URL;
import java.util.ResourceBundle;

public class AskQuestionController implements Initializable {

    public Label questionInfoLabel;
    public Label questionTypeLabel;
    public Button submitAnswerButton;
    public TextField answerField;
    public Slider speedAdjustSlider;
    public Button playClueButton;
    public Button dontKnowButton;

    private Question _question;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        _question = SelectQuestionController.getInstance().getSelectedQuestion();
        _question.getParent().advanceLowestValuedQuestionIndex();

        questionInfoLabel.setText(_question.getParent() + ": $" + _question.getValue());
        questionTypeLabel.setText(
                _question.get_whatIs().substring(0, 1).toUpperCase() + _question.get_whatIs().substring(1)
        );

        speedAdjustSlider.setValue(AskQuestionUtilities.getDefaultReadingSpeed());
        speedAdjustSlider.valueProperty().addListener((e, oldSpeed, newSpeed) -> {
            AskQuestionUtilities.setReadingSpeed(newSpeed.intValue());
        });
        handlePlayClueButton();
    }

    public void handlePlayClueButton() {
        AskQuestionUtilities.speak(_question.get_clue());
    }

    public void handleSubmitAnswerButtonAction() {
        String playerAnswer = answerField.getText().toLowerCase().trim();

        boolean eventFinished = false;
        for (String correctAnswer : _question.get_answer()) {
            if (playerAnswer.equals(correctAnswer.toLowerCase().trim())) {
                eventFinished = true;
                GameManager.getInstance().incrementCurrentScore(_question.getValue());
                correctAnswerGiven();
            }
        }

        if (!eventFinished) {
            GameManager.getInstance().decrementCurrentScore(_question.getValue());
            incorrectAnswerGiven();
        }

        checkEveryQuestionAnswered();

        // Return to the Games menu scene.
        GamesMenuController.getInstance().setMainStageToGamesMenuScene();
    }

    public void handleDontKnowButtonAction() {
        AskQuestionUtilities.answerUnknown(_question.get_answer()[0]);

        checkEveryQuestionAnswered();

        // Return to the main menu scene.
        GamesMenuController.getInstance().setMainStageToGamesMenuScene();
    }

    public void correctAnswerGiven() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

//        alert.setTitle("Correct");
        alert.setHeaderText("Correct!");
        String contentText = "Added $" + _question.getValue() + " to the current score.\n\n"
                + "Your current score is now $" + GameManager.getInstance().getCurrentScore();
        GameManager.getInstance().updateBestScore();
        AskQuestionUtilities.revertReadingSpeedToDefault();
        AskQuestionUtilities.speak("Correct");

        // Formats the pop-up.
        alert.getDialogPane().setContent(new Label(contentText));
        alert.getDialogPane().setMinWidth(alert.getDialogPane().getWidth());
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    public void incorrectAnswerGiven() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setHeaderText("Incorrect!");
        String contentText = "The correct answer was: " + _question.get_answer()[0] + "\n"
                + "$" + _question.getValue() + " has been deducted from your current winning.\n\n"
                + "Your current winning is now $" + GameManager.getInstance().getCurrentScore();

        AskQuestionUtilities.revertReadingSpeedToDefault();
        AskQuestionUtilities.speak("The correct answer was " + _question.get_answer()[0]);

        // Formats the pop-up.
        alert.getDialogPane().setContent(new Label(contentText));
        alert.getDialogPane().setMinWidth(alert.getDialogPane().getWidth());
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    public void checkEveryQuestionAnswered() {
        if (GameManager.getInstance().isEveryQuestionAnswered()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);

            alert.setHeaderText("Every Question Answered");
            String contentText = "Congratulations!" + "\n\n"
                    + "You have completed every question in the question board," + "\n"
                    + "and your total score was $" + GameManager.getInstance().getCurrentScore() + ".\n\n"
                    + "The game will now reset and a new set of question board will be ready.";

            // Resetting the game.
            GameManager.getInstance().newGame();

            // Formats the pop-up.
            alert.getDialogPane().setContent(new Label(contentText));
            alert.getDialogPane().setMinWidth(alert.getDialogPane().getWidth());
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.showAndWait();
        }
    }
}