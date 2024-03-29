package software2.software2.controller;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import software2.software2.DAO.DBAppointmentsDAO;
import software2.software2.DAO.DBContactsDAO;
import software2.software2.DAO.DBCustomersDAO;
import software2.software2.DAO.DBUsersDAO;
import software2.software2.helper.LocalToEST;
import software2.software2.helper.LocalToUTC;
import software2.software2.model.Appointment;
import software2.software2.model.Contact;
import software2.software2.model.Customer;
import software2.software2.model.User;

import java.io.IOException;
import java.net.URL;
import java.time.*;
import java.util.ResourceBundle;

/**
 * Presents form which allows user to add new appointments to system.
 */
public class AddAppointmentController implements Initializable {

    @FXML
    private TextField idField;
    @FXML
    private TextField titleField;
    @FXML
    private TextField descriptionField;
    @FXML
    private TextField locationField;
    @FXML
    private TextField typeField;
    @FXML
    private DatePicker startDatepicker;
    @FXML
    private ComboBox<LocalTime> startHours;

    @FXML
    private DatePicker endDatepicker;
    @FXML
    private ComboBox<LocalTime> endHours;

    @FXML
    private ComboBox<Customer> customerDropdown;
    @FXML
    private ComboBox<Contact> contactDropdown;
    @FXML
    private ComboBox<User> userDropdown;

    Parent scene;
    Stage stage;

    /** Moves user to a different page within the application.
     @param event An ActionEvent.
     @param resource The file path for the next FXML resource to be loaded.
     */
    public void switchScene(ActionEvent event, String resource) throws IOException {
        stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(FXMLLoader.load(getClass().getResource(resource)), 1400, 600);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Returns user to the Main Menu
     * @param event an ActionEvent
     * @throws IOException
     */
    @FXML
    private void onActionCancel(ActionEvent event) throws IOException {
        switchScene(event, "/software2/software2/view/mainmenu.fxml");
    }

    /**
     * Creates new Appointment and pushes information into database, LAMBDA USED HERE.
     * Lambda Expression - utc: Converts user local time to UTC time zone before inserting data into database.
     * @param event an ActionEvent
     * @throws IOException
     */
    @FXML
    private void onActionSave(ActionEvent event) throws IOException {
        Customer customer = customerDropdown.getSelectionModel().getSelectedItem();
        Contact contact = contactDropdown.getSelectionModel().getSelectedItem();
        User user = userDropdown.getSelectionModel().getSelectedItem();
        // checks for blank fields
        boolean blankCheck = blankCheck();

        // blankCheck returns True is all fields contain data
        if (blankCheck) {
            int apptId = Integer.parseInt(idField.getText());
            String title = titleField.getText();
            String description = descriptionField.getText();
            String location = locationField.getText();
            String type = typeField.getText();
            int customerId = customer.getId();
            int userId = user.getId();
            int contactId = contact.getId();

            LocalDate startDate = startDatepicker.getValue();
            LocalTime startTime = startHours.getValue();
            LocalDateTime start = LocalDateTime.of(startDate, startTime);
            LocalToUTC utc = local -> {
                ZonedDateTime zonedLocal = local.atZone(ZoneId.systemDefault());
                LocalDateTime timeUTC = zonedLocal.withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();
                return timeUTC;
            };

            LocalDateTime utcStart = utc.convertToUTC(start);
            LocalDate endDate = endDatepicker.getValue();
            LocalTime endTime = endHours.getValue();
            LocalDateTime end = LocalDateTime.of(endDate, endTime);
            LocalDateTime utcEnd = utc.convertToUTC(end);

            if (timeCheck(start, end) && checkOfficeHrs(start, end) && !checkOverlap(customerId, apptId, start, end)) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Successfully added appointment!");
                alert.setHeaderText("Success");
                alert.setTitle("Appointments");
                alert.showAndWait();
                DBAppointmentsDAO.addAppointment(new Appointment(apptId, title, description, location, type, utcStart, utcEnd, customerId, userId, contactId));
                switchScene(event, "/software2/software2/view/mainmenu.fxml");
            }
        }
    }

    /**
     * Sets the Appointment ID field
     */
    public void setIdField() {
        idField.setText(String.valueOf(DBAppointmentsDAO.getNewAppointmentID()));
    }

    /**
     * Checks all customer appointments for any potential overlap, LAMBDA USED HERE
     * Lambda Expression - est: Converts user's local time to EST time zone in order to compare all appointments
     * @param cust_id Customer ID
     * @param appt_id
     * @param Astart Start Time
     * @param Aend End Time
     * @return Returns boolean
     */
    private boolean checkOverlap(int cust_id, int appt_id, LocalDateTime Astart, LocalDateTime Aend) {
        boolean overlap = false;
        // Grabs list of all appointments
        ObservableList<Appointment> appointments = DBAppointmentsDAO.getCustomerAppointments(cust_id);

        for (Appointment appointment: appointments) {
            LocalDateTime Bstart = appointment.getStart();
            LocalDateTime Bend = appointment.getEnd();

            // Converts local time to EST
            LocalToEST est = local -> {
                ZonedDateTime zonedLocal = local.atZone(ZoneId.systemDefault());
                LocalDateTime timeEst = zonedLocal.withZoneSameInstant(ZoneId.of("America/New_York")).toLocalDateTime();
                return timeEst ;
            };

            LocalDateTime AstartEST = est.convertToEST(Astart);
            LocalDateTime AendEST = est.convertToEST(Aend);
            LocalDateTime BstartEST = est.convertToEST(Bstart);
            LocalDateTime BendEST = est.convertToEST(Bend);


            if ((AstartEST.isAfter(BstartEST) || AstartEST.isEqual(BstartEST)) && (AstartEST.isBefore(BendEST))) {
                overlap = true;
            } else if (AendEST.isAfter(BstartEST) && (AendEST.isBefore(BendEST) || AendEST.isEqual(BendEST))) {
                overlap = true;
            } else if ((AstartEST.isBefore(BstartEST) || AstartEST.isEqual(BstartEST)) && (AendEST.isAfter(BendEST) || AendEST.isEqual(BendEST))) {
                overlap = true;
            }
        }

        if (overlap) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Customer has conflicting appointments. Please select another time");
            alert.setTitle("Appointments");
            alert.showAndWait();
        }

        return overlap;
    }

    /**
     * Compares user entered time to office hours.
     * @param start Start Time
     * @param end End Time
     * @return Returns boolean
     */
    public boolean checkOfficeHrs(LocalDateTime start, LocalDateTime end) {
        boolean isOpen = true;
        LocalTime openTime = LocalTime.of(8, 0);
        LocalTime closeTime = LocalTime.of(22, 0);
        LocalDate openDate = start.toLocalDate();
        LocalDate closeDate = end.toLocalDate();

        LocalDateTime open = LocalDateTime.of(openDate, openTime);
        LocalDateTime close = LocalDateTime.of(openDate, closeTime);

        if (start.isBefore(open) || end.isAfter(close)) {
            isOpen = false;
        } else if (start.getDayOfWeek() == DayOfWeek.SATURDAY || start.getDayOfWeek() == DayOfWeek.SUNDAY) {
            isOpen = false;
        } else if (end.getDayOfWeek() == DayOfWeek.SATURDAY || end.getDayOfWeek() == DayOfWeek.SUNDAY) {
            isOpen = false;
        }

        if (!isOpen) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Selected appointment occurs outside of office hours (Monday - Friday 08:00 to 22:00 EST). Please try again");
            alert.setTitle("Appointments");
            alert.showAndWait();
        }

        return isOpen;
    }

    /**
     * Validates entered Start Time is not before entered End Time
     * @param start Start Time
     * @param end End Time
     * @return Returns boolean
     */
    public boolean timeCheck(LocalDateTime start, LocalDateTime end) {
        boolean inOrder = true;
        if (start.isAfter(end)) {
            inOrder = false;
            Alert alert = new Alert(Alert.AlertType.ERROR, "Start time is scheduled after End time. Please select new options and try again.");
            alert.setTitle("Appointments");
            alert.showAndWait();
        }

        return inOrder;
    }

    /**
     * Validates information has been entered in all form fields
     * @return Returns boolean
     */
    private boolean blankCheck() {
        String fieldName = "";

        if (titleField.getText() == "") {
            fieldName = "Title";
        } else if (descriptionField.getText() == "") {
            fieldName = "Description";
        } else if (locationField.getText() == "") {
            fieldName = "Location";
        } else if (typeField.getText() == "") {
            fieldName = "Type";
        } else if (startDatepicker.getValue() == null) {
            fieldName = "Start Date";
        } else if (startHours.getValue() == null) {
            fieldName = "Start Time";
        } else if (endDatepicker.getValue() == null) {
            fieldName = "End Date";
        } else if (endHours.getValue() == null) {
            fieldName = "End Time";
        } else if (customerDropdown.getSelectionModel().getSelectedItem() == null) {
            fieldName = "Customer";
        } else if (contactDropdown.getSelectionModel().getSelectedItem() == null) {
            fieldName = "Contact";
        } else if (userDropdown.getSelectionModel().getSelectedItem() == null) {
            fieldName = "User";
        }

        if (fieldName != "") {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please enter a valid value for the " + fieldName + " field");
            alert.setTitle("Add Appointment");
            alert.showAndWait();
            return false;
        }
        return true;
    }


    /**
     * On startup, prepares all combo boxes on user form
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setIdField();

        // Creates time slots
        LocalTime start = LocalTime.of(0,0);
        LocalTime end = LocalTime.of(23,50);

        while(start.isBefore(end.plusSeconds(1))) {
            startHours.getItems().add(start);
            endHours.getItems().add(start);

            if (start.getMinute() == 50 && start.getHour() == 23) {
                break;
            }

            start = start.plusMinutes(10);
        }

        startHours.setVisibleRowCount(5);
        endHours.setVisibleRowCount(5);

        customerDropdown.setItems(DBCustomersDAO.getAllCustomers());
        contactDropdown.setItems(DBContactsDAO.getAllContacts());
        userDropdown.setItems(DBUsersDAO.getAllUsers());
    }
}
