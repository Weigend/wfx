package de.qaware.sdfx.example.charts;


import de.qaware.sdfx.lookup.Lookup;

import javafx.application.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.stage.*;

public class ChartControllerMain extends Application {

    public static void main(String[] args) {
        Lookup.init(new PlatformModule());
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent p = FXMLLoader.load(getClass().getResource("chart.fxml"));
        primaryStage.setScene(new Scene(p));
        primaryStage.show();
    }
}
