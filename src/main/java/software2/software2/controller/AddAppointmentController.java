package software2.software2.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import software2.software2.DAO.DBAppointmentsDAO;
import software2.software2.DAO.DBContactsDAO;
import software2.software2.DAO.DBCustomersDAO;
import software2.software2.DAO.DBUsersDAO;
import software2.software2.model.Appointment;
import software2.software2.model.Contact;
import software2.software2.model.Customer;
import software2.software2.model.User;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;

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
    private ComboBox<String> startMins;
    @FXML
    private DatePicker endDatepicker;
    @FXML
    private ComboBox<LocalTime> endHours;
    @FXML
    private ComboBox<String> endMins;
    @FXML
    private ComboBox<Customer> customerDropdown;
    @FXML
    private ComboBox<Contact> contactDropdown;
    @FXML
    private ComboBox<User> userDropdown;


    Parent scene;
    Stage stage;

    public void switchScene(ActionEvent event, String resource) throws IOException {
        stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(FXMLLoader.load(getClass().getResource(resource)), 1400, 800);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void onActionCancel(ActionEvent event) throws IOException {
        switchScene(event, "/software2/software2/view/mainmenu.fxml");
    }

    @FXML
    private void onActionSave(ActionEvent event) throws IOException {
        Customer customer = customerDropdown.getSelectionModel().getSelectedItem();
        Contact contact = contactDropdown.getSelectionModel().getSelectedItem();
        User user = userDropdown.getSelectionModel().getSelectedItem();
        LocalDate startDate = startDatepicker.getValue();
        LocalDate endDate = endDatepicker.getValue();
        LocalTime startTime = startHours.getValue();
        LocalTime endTime = endHours.getValue();


        int id = Integer.parseInt(idField.getText());
        String title = titleField.getText();
        String description = descriptionField.getText();
        String location = locationField.getText();
        String type = typeField.getText();
        LocalDateTime start = LocalDateTime.of(startDate, startTime);
        LocalDateTime end = LocalDateTime.of(endDate, endTime);
        int customerId = customer.getId();
        int userId = user.getId();
        int contactId = contact.getId();

        DBAppointmentsDAO.addAppointment(new Appointment(id, title, description, location, type, start, end, customerId, userId, contactId));
        switchScene(event, "/software2/software2/view/mainmenu.fxml");
    }

    public void setIdField() {
        idField.setText(String.valueOf(DBAppointmentsDAO.getNewAppointmentID()));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setIdField();

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
