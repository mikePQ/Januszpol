package pl.edu.agh.eaiib.io.xp.controllers.workRecord;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import pl.edu.agh.eaiib.io.xp.controllers.AbstractController;
import pl.edu.agh.eaiib.io.xp.data.Database;
import pl.edu.agh.eaiib.io.xp.model.WorkRecord;
import pl.edu.agh.eaiib.io.xp.utils.TableButtonCallback;
import pl.edu.agh.eaiib.io.xp.view.ScreenManager;
import pl.edu.agh.eaiib.io.xp.view.filters.AndFilter;
import pl.edu.agh.eaiib.io.xp.view.filters.FiltersFactory;
import pl.edu.agh.eaiib.io.xp.view.filters.WorkRecordsViewFilter;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AllWorkRecordsController
    extends AbstractController {

    @FXML
    private TableView<WorkRecord> workRecordsTableView;

    @FXML
    private TableColumn<WorkRecord, String> companyNameColumn;

    @FXML
    private TableColumn<WorkRecord, Integer> hoursColumn;

    @FXML
    private TableColumn<WorkRecord, LocalDate> dateColumn;

    @FXML
    private TableColumn<WorkRecord, String> editColumn;

    @FXML
    private TableColumn<WorkRecord, String> deleteColumn;

    @FXML
    private TextField companyNameField;

    @FXML
    private TextField nrOfHoursField;

    @FXML
    private DatePicker beginDateControl;

    @FXML
    private DatePicker endDateControl;

    @FXML
    private Button clearButton;

    @FXML
    private Button applyButton;

    private List<WorkRecordsViewFilter> filters = FiltersFactory.getDefaultFilters();
    private WorkRecordsViewFilter activeFilter = FiltersFactory.getEmptyFilter();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        companyNameColumn.setResizable(false);
        hoursColumn.setResizable(false);
        dateColumn.setResizable(false);
        deleteColumn.setResizable(false);

        companyNameColumn.setCellValueFactory(new PropertyValueFactory<>("companyName"));
        hoursColumn.setCellValueFactory(new PropertyValueFactory<>("hours"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        editColumn.setCellValueFactory(new PropertyValueFactory<>("edit"));
        deleteColumn.setCellValueFactory(new PropertyValueFactory<>("delete"));

        TableButtonCallback<WorkRecord> editButtonCallback = new TableButtonCallback<>();
        editButtonCallback.setButtonText("Edytuj");
        editButtonCallback.setListener(item -> {
            ScreenManager.getInstance().setScreen(ScreenManager.EDIT_WORK_RECORD_VIEW_ID);
            ((EditWorkRecordController) ScreenManager.currentController).setEditingWorkRecord((WorkRecord) item);
        });
        editColumn.setCellFactory(editButtonCallback);

        TableButtonCallback<WorkRecord> deleteButtonCallback = new TableButtonCallback<>();
        deleteButtonCallback.setButtonText("Usuń");
        deleteButtonCallback.setListener(item -> ScreenManager
            .getInstance()
            .showConfirmationDialog("Czy na pewno usunąć rekord?", () -> {
                Database
                    .getWorkRecords()
                    .remove(item);
                workRecordsTableView.setItems(FXCollections.observableList(Database.getWorkRecords()));
            }));
        deleteColumn.setCellFactory(deleteButtonCallback);

        workRecordsTableView.setItems(FXCollections.observableList(Database.getWorkRecords()));

        filters = createFilters();

        clearButton.setOnAction(e -> {
            clearFiltersControls();
            refreshFilters();
        });

        applyButton.setOnAction(e -> {
            refreshFilters();
        });
    }

    private List<WorkRecordsViewFilter> createFilters() {
        List<WorkRecordsViewFilter> filters = new ArrayList<>();
        filters.add(FiltersFactory.createCompanyNameFilter(companyNameField));
        filters.add(FiltersFactory.createBeginDateFilter(beginDateControl));
        filters.add(FiltersFactory.createEndDateFilter(endDateControl));
        filters.add(FiltersFactory.createNumberOfHoursFilter(nrOfHoursField));

        return filters;
    }

    private void clearFiltersControls() {
        String empty = "";
        companyNameField.setText(empty);
        nrOfHoursField.setText(empty);
        beginDateControl.getEditor().setText(empty);
        endDateControl.getEditor().setText(empty);
    }

    private void refreshFilters() {
        activeFilter = getActiveFilter();
        refreshListView();
    }

    private WorkRecordsViewFilter getActiveFilter() {
        List<WorkRecordsViewFilter> activeFilters = filters.stream().filter(WorkRecordsViewFilter::isActive).collect(Collectors.toList());
        return new AndFilter(activeFilters);
    }

    private void refreshListView() {
        List<WorkRecord> workRecords = Database.getWorkRecords().stream().filter(record -> activeFilter.accepts(record)).collect(Collectors.toList());
        workRecordsTableView.setItems(FXCollections.observableList(workRecords));
    }
}
