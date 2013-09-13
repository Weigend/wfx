#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.charts;

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.platform.api.PreloaderNotificationService;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

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

    private static Lookup lookup = new Lookup(ChartController.class);
    public LineChart<Integer, Integer> chart;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Random random = new Random();
        int values = 10 + random.nextInt(40);
        List<XYChart.Series<Integer, Integer>> dataList = chart.getData();
        XYChart.Series<Integer, Integer> series = new XYChart.Series<>();
        dataList.add(series);
        series.setName("Random Test Series");
        PreloaderNotificationService notificationService = lookup.lookup(PreloaderNotificationService.class);
        Bundle bundle = FrameworkUtil.getBundle(ChartController.class);
        for (Integer i = 0; i < values; i++) {
            series.getData().add(new XYChart.Data<>(i, random.nextInt(1000)));
            try {
                Thread.sleep(random.nextInt(100));

            } catch (InterruptedException e) {

            }
            double progress=(double)i / (double)values;
            notificationService.sendNotification(bundle, String.format("Initialize Chart: %d%%",(int)(progress*100)),
                    progress);
        }
    }
}
