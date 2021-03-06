package quinzical.PracticeModule.PracticeMenu;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import quinzical.MainMenu.MainMenu;
import quinzical.PracticeModule.AskQuestion.AskPracticeQuestionController;
import quinzical.PracticeModule.PracticeGameManager;
import quinzical.Utilities.HelpUtilities;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * The controller for a view for the player to select
 * a category to practice and then pass down the category selected to
 * ask practice question controller
 * <p></p>
 * It takes care of how events caused by button presses in the "PracticeMenu" view are handled.
 *
 * @author Danil Linkov
 */
public class PracticeMenuController implements Initializable {

    // fxml objects used in the scene
    @FXML
    private Button selectCategoryButton;
    @FXML
    private Label helpLabel;
    @FXML
    private ComboBox<String> dropDownMenu;
    @FXML
    private HBox helpArea;
    @FXML
    private ToggleButton locationToggle;

    // Used to store the selected category from the drop down menu
    private String selectedCategory;

    // Used for scene transitioning
    private final MainMenu mainMenuModel = MainMenu.getInstance();
    private PracticeGameManager practiceGameManager;

    // Creating its own static instance for scene transitioning
    private static PracticeMenuController instance;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        instance = this;
        practiceGameManager = new PracticeGameManager();
        practiceGameManager.loadAllCategories("NZ");
        // Setting the drop down list to have all the category files
        dropDownMenu.setItems(FXCollections.observableList(practiceGameManager.getCategories()));

        BooleanBinding isCategoryNull = Bindings.isNull(dropDownMenu.valueProperty());
        selectCategoryButton.disableProperty().bind(isCategoryNull);
    }

    /**
     * This method is used to return the instance of this controller
     * @return Returns current instance of this scene
     */
    public static PracticeMenuController getInstance() {
        return instance;
    }

    /**
     * Return back to the main menu
     */
    public void handleBackToMainMenuButton() {
        MainMenu.getInstance().returnToMainMenuScene();
    }

    /**
     * When the player selects a category and clicks submit
     */
    public void handleSelectCategoryButton() {
        try {
            // Check that they have actually selected a category
            if(selectedCategory!=null) {
                // Load the ask practice question scene
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/quinzical/PracticeModule/AskQuestion/AskPracticeQuestion.fxml"));
                Parent root = loader.load();

                AskPracticeQuestionController askPracticeQuestionController = loader.getController();
                // Set its category name equal to the selected category name which is then used inside that controller
                askPracticeQuestionController.setCategoryName(selectedCategory,locationToggle.isSelected() ? "international" : "NZ");

                mainMenuModel.setMainStageScene(new Scene(root));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the location toggle button which changes the location of the categories list.
     */
    public void handleToggleClick() {
        String location = locationToggle.isSelected() ? "international" :"NZ";

        practiceGameManager.loadAllCategories(location);
        dropDownMenu.setItems(FXCollections.observableList(practiceGameManager.getCategories()));
        dropDownMenu.setPromptText("Select " + location + " category");

        if (location.equals("NZ")) {
            locationToggle.setText("NZ");
        }
        else {
            locationToggle.setText("INT");
        }
    }

    /**
     * Save the selected category when the player selects something from the drop down list
     */
    public void handleDropDownClick() {
        selectedCategory = dropDownMenu.getValue();
    }

    /**
     * Method used to set the main stage into this scene
     */
    public void setMainStageToPracticeMenuScene() {
        mainMenuModel.setMainStageScene(selectCategoryButton.getScene());
    }

    /**
     * Handles the scoreboard button click and take them to the score board scene
     */
    public void handleHelpButton() {
        HelpUtilities.setHelpText(helpLabel,"\n\nSelect a category from the drop down to practice a random question from" +
                "\n\nClick the toggle button next to the drop down to change from NZ/International categories list" +
                "\n\nClick back to menu to go back");
        HelpUtilities.bringToFront(helpArea);
    }

    /**
     * Help button functionality which brings the help area to the front so the user can see it
     */
    public void handleHelpCloseButton() {
        HelpUtilities.bringToBack(helpArea);
    }

}
