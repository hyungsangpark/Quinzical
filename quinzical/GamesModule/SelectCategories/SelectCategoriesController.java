package quinzical.GamesModule.SelectCategories;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import quinzical.GamesModule.GameManager;
import quinzical.GamesModule.GameType;
import quinzical.GamesModule.GamesMenu.GamesMenuController;
import quinzical.MainMenu.MainMenu;
import quinzical.Utilities.HelpUtilities;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ResourceBundle;

public class SelectCategoriesController implements Initializable {

    @FXML
    private Label userScoreLabel, helpLabel;
    @FXML
    private Button selectButton, randomButton, backButton, helpCloseButton, helpButton;
    @FXML
    private VBox gridArea;
    @FXML
    private HBox helpArea;

    private final GameManager gameManager = GameManager.getInstance();
    private final GamesMenuController gamesMenuController = GamesMenuController.getInstance();
    private final MainMenu mainMenuModel = MainMenu.getInstance();
    private static SelectCategoriesController instance;

    private ArrayList<String> categories;
    private ObservableList<String> selectedCategories;
    private ArrayList<ToggleButton> toggleButtons;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        instance = this;
        userScoreLabel.setText("Current Score: $" + gameManager.getCurrentScore());

        loadAllCategories();
        selectedCategories = FXCollections.observableArrayList();
        toggleButtons = new ArrayList<>();

        gridArea.getChildren().clear();
        gridArea.getChildren().add(getQuestionBoard());

        IntegerBinding lengthSize = Bindings.size(selectedCategories);
        BooleanBinding listPopulated = lengthSize.greaterThan(4);
        selectButton.disableProperty().bind(listPopulated.not());
    }

    public static SelectCategoriesController getInstance() {
        return instance;
    }

    public GridPane getQuestionBoard() {
        GridPane categoriesBoard = new GridPane();
        categoriesBoard.setGridLinesVisible(false);
//        categoriesBoard.setStyle("-fx-background-color:#FFFFFF");

        int i = 0;
        int j = 0;

        for (String category : categories) {
            categoriesBoard.add(createToggleButton(category),j,i);
            j++;
            if (j == 3) {
                j = 0;
                i++;
            }
        }

        evenlySpreadOut(categoriesBoard,i);
        categoriesBoard.setVgap(20);
//        gridArea.setPadding(new Insets(30,0,30,0));
        categoriesBoard.setStyle("-fx-background-color:#072365");

        return categoriesBoard;
    }

    private void evenlySpreadOut(GridPane categoriesBoard, int rows) {
        // Format each rows to be center aligned and have identical height.
        RowConstraints rowConstraints = new RowConstraints();
        rowConstraints.setValignment(VPos.CENTER);
        rowConstraints.setPercentHeight(100d / rows);
        for (int i = 0; i < rows; i++) {
            categoriesBoard.getRowConstraints().add(rowConstraints);
        }

        // Format each columns to be center aligned and have identical width.
        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setHalignment(HPos.CENTER);
        columnConstraints.setPercentWidth(100d / (3));
        for (int i = 0; i < 3; i++) {
            categoriesBoard.getColumnConstraints().add(columnConstraints);
        }
    }

    private ToggleButton createToggleButton(String category) {
        ToggleButton toggleButton = new ToggleButton(category);
        toggleButton.setStyle("-fx-font-size:18px");
        toggleButton.setPadding(new Insets(15,0,15,0));
        toggleButton.setPrefHeight(100);
        toggleButton.setPrefWidth(200);

        // set shadow for button
        DropShadow shadow = new DropShadow();
        shadow.setBlurType(BlurType.THREE_PASS_BOX);
        shadow.setWidth(18.0);
        shadow.setHeight(18.0);
        shadow.setRadius(8.5);
        shadow.setOffsetX(3.0);
        shadow.setOffsetY(3.0);
        shadow.setSpread(0.03);
        shadow.setColor(new Color(0,0,0,0.25));
        toggleButton.setEffect(shadow);

        toggleButton.getStyleClass().clear();
        toggleButton.getStyleClass().add("toggleButton");
        toggleButton.getStylesheets().add(getClass().getClassLoader().getResource(
                "quinzical/GamesModule/SelectCategories/ToggleButton.css").toExternalForm()
        );

        toggleButton.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
            boolean isSelected = newValue;
            if (isSelected) {
                if (selectedCategories.size() < 5) {
                    selectedCategories.add(category);
                }
                else {
                    toggleButton.setSelected(false);
                }
            }
            else {
                selectedCategories.remove(category);
            }
        });

        toggleButtons.add(toggleButton);

        return toggleButton;
    }

    private void loadAllCategories() {
        // Used to store all the file paths in the folder
        ArrayList<String> filePaths;

        // Getting the categories folder path
        String categoriesPath = new File("").getAbsolutePath();
        categoriesPath += gameManager.getCurrentGameType() == GameType.NZ ? "/categories/NZ" : "/categories/international";

        // Creating a file object based on the path
        File categoriesFolder = new File(categoriesPath);

        // Getting all the file paths in that folder
        filePaths = new ArrayList<>(Arrays.asList(categoriesFolder.list()));
        categories = new ArrayList<>();

        // Adding them to the list
        for (String filePath : filePaths) {
            categories.add(filePath.replace(".txt", ""));
        }
    }

    public void handleReturnToGamesMenuButtonAction() {
        gamesMenuController.setMainStageToGamesMenuScene();
    }

    public void handleSelectButton() {
        gameManager.newGame(new ArrayList<>(selectedCategories));

        try {
            Parent selectQuestion = FXMLLoader.load(getClass().getResource("/quinzical/GamesModule/SelectQuestion/SelectQuestion.fxml"));
            mainMenuModel.setMainStageScene(new Scene(selectQuestion, MainMenu.getAppWidth(), MainMenu.getAppHeight()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleRandomSelect() {
        clearSelectedCategories();
        ArrayList<Integer> randomIndexArray = randomIndexArray(categories.size());

        for (int i = 0; i < 5; i++) {
            toggleButtons.get(randomIndexArray.get(i)).setSelected(true);
        }
    }

    private void clearSelectedCategories() {
        for (ToggleButton tg : toggleButtons) {
            tg.setSelected(false);
        }
    }

    private ArrayList<Integer> randomIndexArray (int length) {
        ArrayList<Integer> shuffledArray = new ArrayList<>();

        for(int i = 0; i< length;i++) {
            shuffledArray.add(i);
        }

        // Shuffling the array list and returning it
        Collections.shuffle(shuffledArray);

        return shuffledArray;
    }

    public void handleHelpButton() {
        HelpUtilities.setHelpText(helpLabel,"text");
        HelpUtilities.bringToFront(helpArea);
    }

    public void handleHelpCloseButton() {
        HelpUtilities.bringToBack(helpArea);
    }

}
