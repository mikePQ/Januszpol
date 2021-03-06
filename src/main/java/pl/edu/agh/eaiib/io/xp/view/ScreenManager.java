package pl.edu.agh.eaiib.io.xp.view;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import pl.edu.agh.eaiib.io.xp.controllers.AbstractController;
import pl.edu.agh.eaiib.io.xp.utils.CsvExporter;
import pl.edu.agh.eaiib.io.xp.utils.ResourceUtils;

import java.util.*;

public class ScreenManager {
    public static final String ADD_COMPANY_VIEW_ID = "addCompanyView";
    public static final String ADD_COMPANY_VIEW_FXML = "/fxml/addCompanyView.fxml";
    public static final String EDIT_COMPANY_VIEW_ID = "editCompanyView";
    public static final String EDIT_COMPANY_VIEW_FXML = "/fxml/editCompanyView.fxml";
    public static final String ADD_WORK_RECORD_VIEW_ID = "addWorkRecordView";
    public static final String ADD_WORK_RECORD_VIEW_FXML = "/fxml/addWorkRecordView.fxml";
    public static final String EDIT_WORK_RECORD_VIEW_ID = "editWorkRecordView";
    public static final String EDIT_WORK_RECORD_VIEW_FXML = "/fxml/editWorkRecordView.fxml";
    public static final String ALL_COMPANIES_VIEW_ID = "viewAllCompanies";
    public static final String ALL_COMPANIES_VIEW_FXML = "/fxml/viewAllCompanies.fxml";
    public static final String ALL_WORK_RECORDS_VIEW_ID = "viewAllWorkRecords";
    public static final String ALL_WORK_RECORDS_VIEW_FXML = "/fxml/viewAllWorkRecords.fxml";
    public static final String EXPORT_RECORDS_VIEW_ID = "exportRecordsView";

    private static final ScreenManager INSTANCE = new ScreenManager();

    private static Map<String, String> viewsMap = new HashMap<>();

    private final ScreensController screensController;

    public static AbstractController currentController;

    private static Stage primaryStage;

    public static ScreenManager getInstance() {
        return INSTANCE;
    }

    public static ScreenManager getInstance(Stage primaryStage) {
        ScreenManager.primaryStage = primaryStage;
        return INSTANCE;
    }

    private ScreenManager() {
        screensController = new ScreensController();
    }

    public void initialize() {
        viewsMap.put(ADD_COMPANY_VIEW_ID, ADD_COMPANY_VIEW_FXML);
        viewsMap.put(EDIT_COMPANY_VIEW_ID, EDIT_COMPANY_VIEW_FXML);
        viewsMap.put(ADD_WORK_RECORD_VIEW_ID, ADD_WORK_RECORD_VIEW_FXML);
        viewsMap.put(EDIT_WORK_RECORD_VIEW_ID, EDIT_WORK_RECORD_VIEW_FXML);
        viewsMap.put(ALL_COMPANIES_VIEW_ID, ALL_COMPANIES_VIEW_FXML);
        viewsMap.put(ALL_WORK_RECORDS_VIEW_ID, ALL_WORK_RECORDS_VIEW_FXML);
        for (Map.Entry<String, String> entry : viewsMap.entrySet()) {
            loadScreen(entry.getKey(), entry.getValue());
        }
    }

    private boolean loadScreen(String name, String resource) {
        ResourceBundle bundle = ResourceUtils.loadLabelsForDefaultLocale();
        return screensController.loadScreen(name, resource, bundle);
    }

    public boolean setScreen(String name) {
        return screensController.setScreen(name);
    }

    public String getActiveViewId() {
        return screensController.getActiveView();
    }

    public Parent getContainer() {
        return screensController;
    }

    public Node getMenuBar() {
        List<MenuItem> menuItemsList = new ArrayList<>();
        menuItemsList.add(createMenuItem(ADD_COMPANY_VIEW_ID));
        menuItemsList.add(createMenuItem(ADD_WORK_RECORD_VIEW_ID));
        menuItemsList.add(createMenuItem(ALL_COMPANIES_VIEW_ID));
        menuItemsList.add(createMenuItem(ALL_WORK_RECORDS_VIEW_ID));
        menuItemsList.add(createMenuItem(EXPORT_RECORDS_VIEW_ID));

        Menu menu = new Menu("Menu");
        menu
            .getItems()
            .addAll(menuItemsList);
        MenuBar menuBar = new MenuBar();
        menuBar
            .getMenus()
            .add(menu);
        return menuBar;
    }

    private MenuItem createMenuItem(String screenId) {
        ResourceBundle resources = ResourceUtils.loadLabelsForDefaultLocale();
        String menuText = resources.getString("menu." + screenId);
        MenuItem menuItem = new MenuItem(menuText);
        if( screenId.equals(EXPORT_RECORDS_VIEW_ID) ) {
            menuItem.setOnAction(event -> {
                CsvExporter exporter = new CsvExporter(primaryStage,currentController);
                exporter.exportRecords();
            });
        }
        else {
            menuItem.setOnAction(event -> setScreen(screenId));
        }
        return menuItem;
    }

    public void showErrorDialog(String message) {
        showErrorDialog("Błąd", message);
    }

    public void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
        //        .ifPresent(rs -> {
        //            if (rs == ButtonType.OK) {
        //                System.out.println("Pressed OK.");
        //            }
        //        });
    }

    public void showConfirmationDialog(String message, ConfirmationListener listener) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Potwierdzenie");
        alert.setHeaderText("Potwierdzenie");
        alert.setContentText(message);

        alert
            .showAndWait()
            .ifPresent(result -> {
                if (result == ButtonType.OK) {
                    listener.onConfirmation();
                }
            });
    }

    public interface ConfirmationListener {
        void onConfirmation();
    }

    private static final class ScreensController
        extends StackPane {
        private Map<String, Node> screens = new LinkedHashMap<>();

        private void addScreen(String name, Node screen) {
            screens.put(name, screen);
        }

        private boolean loadScreen(String name, String resource, ResourceBundle resourceBundle) {
            try {
                FXMLLoader myLoader = new FXMLLoader(getClass().getResource(resource), resourceBundle);
                Parent loadScreen = myLoader.load();
                currentController = myLoader.getController();
                addScreen(name, loadScreen);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        private boolean setScreen(final String name) {
            if (getChildren().isEmpty()) {
                getChildren().add(screens.get(name));
            } else {
                reloadScreen(name);
                getChildren().remove(0);
                getChildren().add(0, screens.get(name));
            }
            return true;
        }

        private void reloadScreen(String screenId) {
            String viewFxml = ScreenManager.viewsMap.get(screenId);
            ResourceBundle bundle = ResourceUtils.loadLabelsForDefaultLocale();
            loadScreen(screenId, viewFxml, bundle);
        }

        private String getActiveView() {
            Node view = getChildren().get(0);
            for (Map.Entry<String, Node> entry : screens.entrySet()) {
                if (view.equals(entry.getValue())) {
                    return entry.getKey();
                }
            }
            return ALL_WORK_RECORDS_VIEW_ID;
        }
    }
}
