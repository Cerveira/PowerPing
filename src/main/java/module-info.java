module com.nuapps.powerping {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;


    opens com.nuapps.powerping to javafx.fxml;
    exports com.nuapps.powerping;
}