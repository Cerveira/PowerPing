module com.nuapps.powerping {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.poi.poi;


    opens com.nuapps.powerping to javafx.fxml;
    exports com.nuapps.powerping;
    exports com.nuapps.powerping.model;
}
