package quinzical.GamesModule.ScoreBoard;

import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import quinzical.GamesModule.GamesMenuController;
import quinzical.MainMenu.MainMenu;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ScoreBoardController implements Initializable {

    public Button backButton;
    public VBox nameArea;
    public VBox scoreArea;

    private static ScoreBoardController _instance;

    private Map<String,Integer> scoreBoardMap;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        _instance = this;
        scoreBoardMap = ScoreBoardManager.getInstance().getScoreBoardMap();
        loadScoreBoard();
    }

    public static ScoreBoardController getInstance() {
        return _instance;
    }

    public void handleBackButton() {
        GamesMenuController.getInstance().setMainStageToGamesMenuScene();
    }

    public void loadScoreBoard() {
        GridPane playerBoardGrid = new GridPane();
        playerBoardGrid.setGridLinesVisible(false);

        int i = 0;
        for (String name : scoreBoardMap.keySet()) {
            Label playerName = createLabel(name);
            playerBoardGrid.add(playerName,0,i);
            i++;
        }

        GridPane scoreBoardGrid = new GridPane();
        scoreBoardGrid.setGridLinesVisible(false);

        int j = 0;
        for (Integer score : scoreBoardMap.values()) {
            Label playerScore = createLabel(Integer.toString(score));
            scoreBoardGrid.add(playerScore,0,j);
            j++;
        }

        playerBoardGrid.setAlignment(Pos.CENTER_RIGHT);
        nameArea.getChildren().clear();
        nameArea.getChildren().add(playerBoardGrid);
        nameArea.setPadding(new Insets(5,60,0,0));

        scoreBoardGrid.setAlignment(Pos.CENTER_LEFT);
        scoreArea.getChildren().clear();
        scoreArea.getChildren().add(scoreBoardGrid);
        scoreArea.setPadding(new Insets(5,0,0,60));
    }

    private Label createLabel(String labelName) {
        Label label = new Label(labelName);

        label.getStyleClass().clear();
        label.getStyleClass().add("label");
        label.getStylesheets().add(getClass().getClassLoader().getResource("quinzical/GamesModule/ScoreBoard/Label.css").toExternalForm());

        return label;
    }

}