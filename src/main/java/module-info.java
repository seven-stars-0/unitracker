module project.unitracker {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.base;
    requires javafx.graphics;
    requires jdk.compiler;
    requires java.desktop;

    opens project.unitracker.controller to javafx.fxml;
    exports project.unitracker;
    opens project.unitracker.controller.modal to javafx.fxml;
    opens project.unitracker.controller.tab to javafx.fxml;
    opens project.unitracker.controller.main to javafx.fxml;
    opens project.unitracker.controller.tab.chart to javafx.fxml;
    opens project.unitracker.controller.other to javafx.fxml;
    opens project.unitracker.utility.psql to javafx.base, javafx.fxml;
    opens project.unitracker.utility.uimodel to javafx.base, javafx.fxml;
    opens project.unitracker.utility.constant to javafx.base, javafx.fxml;
    opens project.unitracker.utility.validator to javafx.base, javafx.fxml;
}