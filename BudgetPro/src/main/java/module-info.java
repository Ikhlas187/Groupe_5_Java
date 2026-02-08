module com.example.budgetpro {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.budgetpro to javafx.fxml;
    exports com.example.budgetpro;
}