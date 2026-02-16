module com.example.budgetpro {

    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.google.gson;


    opens com.example.budgetpro to javafx.fxml;
    opens com.example.budgetpro.pages to javafx.fxml;

    exports com.example.budgetpro;
    exports com.example.budgetpro.services;
}
