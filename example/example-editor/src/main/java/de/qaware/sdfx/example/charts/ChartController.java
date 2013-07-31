package de.qaware.sdfx.example.charts;

import javafx.fxml.*;
import javafx.scene.chart.*;
import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

/**
 *
 */
public class ChartController implements Initializable {

    public LineChart<Integer, Integer> chart;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Random random = new Random();
        int values = 10 + random.nextInt(90);
        List<XYChart.Series<Integer, Integer>> dataList = chart.getData();
        XYChart.Series<Integer, Integer> series = new XYChart.Series<>();
        dataList.add(series);
        series.setName("Random Test Series");
        for (Integer i = 0; i < values; i++) {
            series.getData().add(new XYChart.Data<>(i, random.nextInt(1000)));
        }
    }
}
