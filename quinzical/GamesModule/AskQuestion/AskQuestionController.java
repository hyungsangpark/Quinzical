package quinzical.GamesModule.AskQuestion;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import quinzical.GamesModule.GameManager;
import quinzical.GamesModule.GameType;
import quinzical.GamesModule.GamesMenu.GamesMenuController;
import quinzical.GamesModule.SelectQuestion.SelectQuestionController;
import quinzical.MainMenu.MainMenu;
import quinzical.Questions.Question;
import quinzical.Utilities.AskQuestionUtilities;
import quinzical.Utilities.HelpUtilities;
import quinzical.Utilities.Notification;
import quinzical.Utilities.TTSUtility;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * The controller for a view to ask the question to the player,
 * and tell the player about whether the player was correct or not.
 * <p></p>
 * It takes care of how events caused by button presses in the "AskQuestion" view are handled.
 *
 * @author Hyung Park
 */
public class AskQuestionController implements Initializable {

    // Components in the view
    public Label questionInfoLabel;
    public Button submitAnswerButton;
    public ComboBox<String> selectQuestionType;
    public TextField answerField;
    public Slider speedAdjustSlider;
    public Button playClueButton;
    public Button dontKnowButton;
    public Label timeLabel;

    private boolean isMacronCaps = false;
    public Button macronAButton;
    public Button macronEButton;
    public Button macronIButton;
    public Button macronOButton;
    public Button macronUButton;
    public Button switchMacronCapsButton;
    private Button[] macronButtons;

    public Button helpCloseButton;
    public Button helpButton;
    public Label helpLabel;
    public HBox helpArea;

    // Frequently used instances of classes.
    private final GameManager _gameManager = GameManager.getInstance();
    private final SelectQuestionController _selectQuestionController = SelectQuestionController.getInstance();
    final int questionTime = 60*1000;

    // Reference to the question currently being asked.
    private Question _question;
    long endTime = System.currentTimeMillis()+1000*5;
    boolean done = false;

    /**
     * The initial method that fxml view calls from this controller as it loads.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Retrieve selected question.
        _question = _selectQuestionController.getSelectedQuestion();
        // Mark the question as answered.
        _question.getParent().advanceLowestValuedQuestionIndex();

        // Allocating respective labels for the question.
        questionInfoLabel.setText(_question.getParent() + ": $" + _question.getValue());
        selectQuestionType.setItems(FXCollections.observableList(AskQuestionUtilities.getQuestionTypes()));

        // Initializing speed adjust slider (setting default view and event handler)
        speedAdjustSlider.setValue(TTSUtility.getDefaultReadingSpeed());
        speedAdjustSlider.valueProperty().addListener((e, oldSpeed, newSpeed) -> TTSUtility.setReadingSpeed(newSpeed.intValue()));

        // Only enable submit button when both question type has been selected and some answer has been entered.
        submitAnswerButton.disableProperty().bind(Bindings.or(
                answerField.textProperty().isEmpty(),
                selectQuestionType.valueProperty().isNull()
        ));

        macronButtons = new Button[]{macronAButton, macronEButton, macronIButton, macronOButton, macronUButton};
        AskQuestionUtilities.configureMacronButtons(macronButtons, answerField, isMacronCaps);

        setTimer();
        showTimer();

        // Read out the question.
        handlePlayClueButton();
    }

    public void macronSwitchCaps() {
        AskQuestionUtilities.macronSwitchCaps(macronButtons, isMacronCaps, answerField);
        isMacronCaps = !isMacronCaps;
    }

    private void setTimer() {
        Timer myTimer = new Timer();
        myTimer.schedule(new TimerTask(){
            @Override
            public void run() {
                    if (!done) {
                        Platform.runLater(() -> handleSubmitAnswerButtonAction());
                    }
            }
        }, questionTime);
        endTime = System.currentTimeMillis() + questionTime;
    }

    private void showTimer() {
        DateFormat timeFormat = new SimpleDateFormat("ss");
        final Timeline timeline = new Timeline(
                new KeyFrame(
                        Duration.millis(200),
                        event -> {
                            final long diff = endTime - System.currentTimeMillis();
                            if ( diff < 0 ) {
                                timeLabel.setText(timeFormat.format(0) + " secs left");
                            } else {
                                timeLabel.setText(timeFormat.format(diff) + " secs left");
                            }
                        }
                )
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    /**
     * Handles the event of Play clue button (the large circular button in the middle) being pressed.
     * <p></p>
     * It speaks the clue of the selected question.
     */
    public void handlePlayClueButton() {
        TTSUtility.speak(_question.getClue());
    }

    /**
     * Handles the event of "Submit Answer" button being pressed.
     * <p></p>
     * It retrieves the player's answer from the answer field, cleans it up,
     * then checks whether the player has got the correct answer or not. After that,
     * it returns the main stage to the SelectQuestion view.
     * <p></p>
     * If the player has entered the correct answer, it runs the procedure for correct
     * answer given which is described in the following method.
     * {@link quinzical.GamesModule.AskQuestion.AskQuestionController#correctAnswerGiven()}
     * <p></p>
     * If the player has entered the incorrect answer, it runs the procedure for
     * incorrect answer given which is described in the following method.
     * {@link quinzical.GamesModule.AskQuestion.AskQuestionController#incorrectAnswerGiven()}
     */
    public void handleSubmitAnswerButtonAction() {
        done = true;
        // Retrieve and clean up player's answer
        String cleanedPlayerAnswer = AskQuestionUtilities.answerCleanUp(answerField.getText());
            // Checking whether any of the correct answer matches the player's answer
            boolean isUserAnswerCorrect = false;
            for (String correctAnswer : _question.getAnswer()) {
                if (cleanedPlayerAnswer.equals(AskQuestionUtilities.answerCleanUp(correctAnswer))
                && selectQuestionType.getValue().toLowerCase().equals(_question.getQuestionType())) {
                    isUserAnswerCorrect = true;
                    correctAnswerGiven();
                }
            }

            // If none of the correct answer matches the player's answer
            if (!isUserAnswerCorrect) {
                incorrectAnswerGiven();
            }

            TTSUtility.endTTSSpeaking();
            if (!_gameManager.isInternationalGameUnlocked() && isTwoCategoriesComplete()) {
                _gameManager.unlockInternationalGame();
                notifyInternationalGameUnlock();

                // Because international section has now been enabled, go back to Games menu.
                GamesMenuController.getInstance().setMainStageToGamesMenuScene();
            } else {
                checkIsEveryQuestionAnswered();
            }
    }

    /**
     * Handles the event of "Don't Know" button being pressed.
     * <p></p>
     * Displays a pop up showing the player the answer to the clue,
     * then checks whether every question has been answered,
     * then returns the main stage to SelectQuestion view.
     */
    public void handleDontKnowButtonAction() {
        done = true;
        AskQuestionUtilities.answerUnknown(_question.getAnswer()[0], _question.getQuestionType());

        TTSUtility.endTTSSpeaking();
        if (!_gameManager.isInternationalGameUnlocked() && isTwoCategoriesComplete()) {
            _gameManager.unlockInternationalGame();
            notifyInternationalGameUnlock();

            // Because international section has now been enabled, go back to Games menu.
            GamesMenuController.getInstance().setMainStageToGamesMenuScene();
        } else {
            checkIsEveryQuestionAnswered();
        }
    }

    /**
     * Displays a pop up notifying the player that the player answer was correct,
     * and that the current score has incremented by the value of the question.
     * <p></p>
     * After incrementing the current score by the value of this question, compare
     * and update the best score for possible occasions of current score being
     * higher than the best score.
     */
    public void correctAnswerGiven() {
        // Increment current score and check-and-update best score.
        _gameManager.incrementCurrentScore(_question.getValue());

        // Revert currently reading speed to default, then say "Correct".
        TTSUtility.revertReadingSpeedToDefault();
        TTSUtility.speak("Correct");

        String contentText = "Added $" + _question.getValue() + " to the current score.\n\n"
                + "Your current score is now $" + _gameManager.getCurrentScore();

        Notification.smallInformationPopup("Correct", "Correct!", contentText);
    }

    /**
     * Displays a pop up notifying the player that the player answer was incorrect,
     * then display and read out the correct answer, then decrement the current
     * score by the value of the question.
     */
    public void incorrectAnswerGiven() {
        // Decrement current score.
        _gameManager.decrementCurrentScore(_question.getValue());

        // Revert currently reading speed to default, then say "Correct".
        TTSUtility.revertReadingSpeedToDefault();
        TTSUtility.speak("The correct answer was: "
                + _question.getQuestionType() + " " + _question.getAnswer()[0]);

        StringBuilder contentText = new StringBuilder();
        contentText.append("The correct answer was: ")
                .append(_question.getQuestionType().substring(0,1).toUpperCase())
                .append(_question.getQuestionType().substring(1)).append(" ")
                .append(_question.getAnswer()[0].replaceAll("`", ""))
                .append("\n$" + _question.getValue()).append(" has been deducted from your current winning.\n\n")
                .append("Your current winning is now $").append(_gameManager.getCurrentScore());

        Notification.largeInformationPopup("Incorrect", "Incorrect!", contentText.toString());
    }

    public boolean isTwoCategoriesComplete() {
        int numCategoriesComplete = 0;
        for (int categoryIndex = 0; categoryIndex < 5; categoryIndex++) {
            if (_gameManager.isCategoryComplete(categoryIndex)) {
                numCategoriesComplete++;
            }
        }
        return numCategoriesComplete == 2;
    }

    /**
     * Checks whether every question in the question has been answered by
     * checking whether the lowest valued question index in every category
     * is 5.
     * <p></p>
     * If every question has been answered, display a pop up notifying the
     * player that the player has completed every question in the question
     * board, the player's total score, and that the game will now reset.
     */
    public void checkIsEveryQuestionAnswered() {
        if (_gameManager.isEveryQuestionAnswered()) {
            _gameManager.setCurrentGameFinished();
            setSceneToEndGameScene();
            if (_gameManager.isGameFinished(GameType.NZ) && _gameManager.isGameFinished(GameType.INTERNATIONAL)) {
                _gameManager.resetGame();
            }
        }
        else {
            // End any currently-running speaking methods and return to the question board.
            TTSUtility.endTTSSpeaking();
            SelectQuestionController.getInstance().setMainStageToSelectQuestionScene();
        }
    }

    private void notifyInternationalGameUnlock() {
        StringBuilder contentText = new StringBuilder();
        contentText.append("Congratulations on completing two categories!\n\n")
                .append("You have unlocked international game module.\n")
                .append("You can now switch between two modules by pressing a button on the ")
                .append("bottom right section of the games menu screen.\n\n")
                .append("Let's now take a tour outside NZ...");

        Notification.largeInformationPopup("International Unlocked", "International Game Mode has been unlocked!", contentText.toString());
    }

    private void setSceneToEndGameScene() {
        try {
            Parent scoreBoard = FXMLLoader.load(getClass().getResource("/quinzical/GamesModule/EndGame/EndGameScene.fxml"));
            MainMenu.getInstance().setMainStageScene(new Scene(scoreBoard, MainMenu.getAppWidth(), MainMenu.getAppHeight()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleHelpButton() {
        HelpUtilities.setHelpText(helpLabel,"text");
        HelpUtilities.bringToFront(helpArea);
    }

    public void handleHelpCloseButton() {
        HelpUtilities.bringToBack(helpArea);
    }

}
