module com.example.budgetpro {

    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.google.gson;
    requires java.prefs;


    opens com.example.budgetpro to javafx.fxml;
    //opens com.example.budgetpro.pages to javafx.fxml;
    opens com.example.budgetpro.pages to javafx.fxml, javafx.base;

    exports com.example.budgetpro;
    exports com.example.budgetpro.services;
}
