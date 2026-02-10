module com.example.budgetpro {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.budgetpro to javafx.fxml;
    exports com.example.budgetpro;
    opens com.example.budgetpro.pages to javafx.fxml;
}