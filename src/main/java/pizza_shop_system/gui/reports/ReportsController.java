package pizza_shop_system.gui.reports;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import pizza_shop_system.gui.base.BaseController;
import pizza_shop_system.gui.utils.StyleUtil;
import pizza_shop_system.reports.ReportGenerator;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class ReportsController extends BaseController {

    @FXML
    private ComboBox<String> timeSelectionChoiceBox;

    @FXML
    private DatePicker datePicker;

    @FXML
    private Label weeklyStartDateLabel;

    @FXML
    private Button generateButton;

    @FXML
    private TextArea reportTextArea;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final ReportGenerator reportGenerator = new ReportGenerator();
    private final StyleUtil styleUtil = new StyleUtil();

    private void stylize() {
        styleUtil.fadeButtonOnHover(generateButton);
    }

    @FXML
    private void initialize() {
        stylize();

        // Populate ComboBox with report types
        timeSelectionChoiceBox.getItems().addAll("Daily Report", "Weekly Report");
        timeSelectionChoiceBox.getSelectionModel().select("Daily Report");

        // Set visible row count to show both options at once
        timeSelectionChoiceBox.setVisibleRowCount(2);

        // Set default date to today and make sure the DatePicker is visible
        datePicker.setValue(LocalDate.now());
        datePicker.setVisible(true);
        datePicker.setEditable(true); // Allow user to type in the DatePicker

        // Add a listener to detect text changes in the DatePicker's text field
        datePicker.getEditor().textProperty().addListener((_, _, newValue) -> {

            // List of all possible (valid) input formats for the date
            List<String> possibleFormats = List.of("M/d/yyyy", "MM/dd/yyyy", "M/dd/yyyy", "MM/d/yyyy");
            LocalDate parsedDate = null;

            // Attempt to parse each possibleFormat until one succeeds
            for (String format : possibleFormats) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                    parsedDate = LocalDate.parse(newValue, formatter);
                    break;
                } catch (DateTimeParseException ignored) {
                }
            }

            // If there is a parsedDate then it is converted into the DatePickers format and DatePickers value is updated, otherwise its invalid input and is ignored
            if (parsedDate != null) {
                String formattedDate = parsedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                datePicker.setValue(parsedDate);
            } else {
                System.out.println("Invalid date format, ignoring input.");
            }
        });

        // Listener for report type selection
        timeSelectionChoiceBox.getSelectionModel().selectedItemProperty().addListener((_, _, newVal) -> {
            handleReportTypeSelection(newVal);
        });

        generateButton.setDisable(false);
        reportTextArea.setVisible(false);

        generateButton.setOnAction(e -> {
            try {
                generateReport();
            } catch (IOException ex) {
                ex.printStackTrace();
                showError("Failed to generate report: " + ex.getMessage());
            }
        });
    }

    private boolean isValidDate(String date) {
        try {
            LocalDate.parse(date, dateFormatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private void handleReportTypeSelection(String reportType) {
        boolean isWeekly = "Weekly Report".equals(reportType);
        weeklyStartDateLabel.setVisible(isWeekly);
    }

    private void generateReport() throws IOException {
        String reportType = timeSelectionChoiceBox.getValue();
        LocalDate selectedDate = datePicker.getValue();

        // Ensure that the date is selected or typed in
        if (selectedDate == null) {
            showError("Please select or type a date.");
            return;
        }

        String reportContent = null;

        try {
            if ("Daily Report".equals(reportType)) {
                reportContent = reportGenerator.generateDailyReport(selectedDate);
            } else if ("Weekly Report".equals(reportType)) {
                reportContent = reportGenerator.generateWeeklyReport(selectedDate);
            }

            reportTextArea.setText(reportContent != null ? reportContent : "No data available.");
            reportTextArea.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
            showError("An error occurred while generating the report: " + e.getMessage());
        }
    }

    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
