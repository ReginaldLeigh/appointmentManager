package software2.software2.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import software2.software2.DAO.DBDivisionsDAO;

/**
 * Class used to create Country objects throughout the application.
 * Each country can be assigned to multiple divisions at once.
 */
public class Country {
    private int countryID;
    private String countryName;
    private ObservableList<Division> division = FXCollections.observableArrayList();


    public Country(int countryID, String countryName) {
        this.countryID = countryID;
        this.countryName = countryName;
    }

    public int getCountryID() {
        return countryID;
    }

    public String getCountryName() {
        return countryName;
    }

    /**
     * Retrieves a list of all associated first level divisions
     * @return Returns a list of first level divisions
     */
    public ObservableList<Division> getDivisions() {
        ObservableList<Division> allDivisions = DBDivisionsDAO.getAllDivisions();
        ObservableList<Division> firstLevelDivisions = FXCollections.observableArrayList();

        for (Division division : allDivisions) {
            if (division.getCountryID() == countryID) {
                firstLevelDivisions.add(division);
            }
        }

        return firstLevelDivisions;
    }

    @Override
    public String toString() {
        return (countryName);
    }
}
