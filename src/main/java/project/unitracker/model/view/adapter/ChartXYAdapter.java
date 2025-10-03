package project.unitracker.model.view.adapter;

import javafx.scene.chart.XYChart;
import project.unitracker.model.database.DatabaseHandler;
import project.unitracker.utility.uimodel.DataPoint;

import java.util.List;

// Converte i dati ottenuti dal DataRetriever a XYSeries<String, Double> per passarli al ChartTabModel
public class ChartXYAdapter {

    public static XYChart.Series<String, Number> getXYSeries(String code, String period, boolean appliedOnly) {
        List<DataPoint> tableData = DatabaseHandler.getInstance().getLineChartData(code, period, appliedOnly);

        XYChart.Series<String, Number> chartData = new XYChart.Series<>();

        // Qui avviene la conversione da DataPoint a XYChart.Data
        for (DataPoint dp : tableData)
            chartData.getData().add( new XYChart.Data<>(dp.date(), dp.value()) );

        return chartData;
    }

}
