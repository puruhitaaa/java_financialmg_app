module com.pahuger {
    requires transitive javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.pahuger to javafx.fxml;
    exports com.pahuger;
}
