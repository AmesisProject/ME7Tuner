package ui.view.closedloopfueling;


import contract.MlhfmFileContract;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import preferences.closedloopfueling.ClosedLoopFuelingLogFilterPreferences;
import stddev.StandardDeviation;
import ui.viewmodel.closedloopfueling.ClosedLoopFuelingMe7LogViewModel;
import ui.viewmodel.MlhfmViewModel;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.util.List;
import java.util.Map;

public class ClosedLoopFuelingMe7LogUiManager {

    private JFreeChart chart;
    private JPanel closedLoopLogPanel;
    private JLabel fileLabel;
    private ClosedLoopFuelingMe7LogViewModel closedLoopViewModel;

    private Map<String, List<Double>> mlhfmMap;
    private File me7LogFile;

    ClosedLoopFuelingMe7LogUiManager() {
        closedLoopViewModel = ClosedLoopFuelingMe7LogViewModel.getInstance();
        closedLoopViewModel.getPublishSubject().subscribe(new Observer<Map<String, List<Double>>>() {
            @Override
            public void onNext(Map<String, List<Double>> me7LogMap) {
                if(mlhfmMap != null) {
                    drawChart(me7LogMap, mlhfmMap);
                }
            }

            @Override
            public void onSubscribe(Disposable disposable) {}

            @Override
            public void onError(Throwable throwable) {}

            @Override
            public void onComplete() {}
        });

        MlhfmViewModel mlhfmViewModel = MlhfmViewModel.getInstance();
        mlhfmViewModel.getMlhfmPublishSubject().subscribe(new Observer<Map<String, List<Double>>>() {
            @Override
            public void onNext(Map<String, List<Double>> mlhfmMap) {
                ClosedLoopFuelingMe7LogUiManager.this.mlhfmMap = mlhfmMap;
            }

            @Override
            public void onSubscribe(Disposable disposable) {}

            @Override
            public void onError(Throwable throwable) {}

            @Override
            public void onComplete() {}
        });
    }

    JPanel getMe7LogPanel() {
        initChart();
        closedLoopLogPanel = new JPanel();
        closedLoopLogPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        c.weightx = 1.0;
        c.gridheight = 1;

        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0.95;
        closedLoopLogPanel.add(getChartPanel(), c);

        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 1;
        c.weighty = 0.05;
        closedLoopLogPanel.add(getActionPanel(), c);

        return closedLoopLogPanel;
    }

    private JPanel getChartPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 1;
        c.gridwidth = 1;
        c.weightx = 1.0;
        c.weighty = 1.0;

        panel.add(new ChartPanel(chart), c);

        return panel;
    }

    private JPanel getActionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        c.gridwidth = 2;

        c.weightx = 0.1;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.LINE_START;

        JButton button = getConfigureFilterButton();
        panel.add(button, c);

        c.weightx = 0.9;
        c.gridx = 1;
        c.gridy = 0;
        c.anchor = GridBagConstraints.CENTER;

        button = getFileButton();
        panel.add(button, c);

        c.gridx = 1;
        c.gridy = 1;

        fileLabel = new JLabel("No File Selected");
        panel.add(fileLabel, c);

        return panel;
    }

    private JButton getConfigureFilterButton() {
        JButton button = new JButton("Configure Filter");

        button.addActionListener(e -> {
            ClosedLoopFuelingMe7LogFilterConfigPanel filterConfigPane = new ClosedLoopFuelingMe7LogFilterConfigPanel();

            int result = JOptionPane.showConfirmDialog(closedLoopLogPanel, filterConfigPane,
                    "", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                for (ClosedLoopFuelingMe7LogFilterConfigPanel.FieldTitle fieldTitle : ClosedLoopFuelingMe7LogFilterConfigPanel.FieldTitle.values()) {
                    switch (fieldTitle) {
                        case MIN_THROTTLE_ANGLE:
                            ClosedLoopFuelingLogFilterPreferences.setMinThrottleAnglePreference(Double.valueOf(filterConfigPane.getFieldText(fieldTitle)));
                            break;
                        case MIN_RPM:
                            ClosedLoopFuelingLogFilterPreferences.setMinRpmPreference(Double.valueOf(filterConfigPane.getFieldText(fieldTitle)));
                            break;
                        case MAX_STD_DEV:
                            ClosedLoopFuelingLogFilterPreferences.setMaxStdDevPreference(Double.valueOf(filterConfigPane.getFieldText(fieldTitle)));
                            break;
                        case STD_DEV_SAMPLE_WINDOW:
                            ClosedLoopFuelingLogFilterPreferences.setStdDevSampleWindowPreference(Integer.valueOf(filterConfigPane.getFieldText(fieldTitle)));
                            break;
                    }

                    if(this.me7LogFile != null) {
                        loadMe7File(this.me7LogFile);
                    }
                }
            }
        });

        return button;
    }

    private JButton getFileButton() {
        JButton button = new JButton("Load Logs");

        button.addActionListener(e -> {
            final JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            int returnValue = fc.showOpenDialog(closedLoopLogPanel);

            if (returnValue == JFileChooser.APPROVE_OPTION) {
                this.me7LogFile = fc.getSelectedFile();
                loadMe7File(this.me7LogFile);
            }
        });

        return button;
    }

    private void loadMe7File(File file) {
        closedLoopViewModel.loadDirectory(file);
        fileLabel.setText(file.getName());
    }

    private void initChart() {
        XYSeriesCollection dataset = new XYSeriesCollection();

        chart = ChartFactory.createScatterPlot(
                "Standard Deviation",
                "Voltage", "Std Dev", dataset);

        XYPlot plot = (XYPlot)chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.BLACK);
        plot.setRangeGridlinePaint(Color.BLACK);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(false, true);
        plot.setRenderer(renderer);

        plot.getRenderer().setSeriesShape(0, new Ellipse2D.Double(0,0,1,1));
        plot.getRenderer().setSeriesPaint(0, Color.BLUE);
    }

    private void drawChart(Map<String, List<Double>> me7LogMap, Map<String, List<Double>> mlhfmMap) {

        Map<Double, List<Double>> stdDev = StandardDeviation.getStandDeviationMap(me7LogMap, mlhfmMap, ClosedLoopFuelingLogFilterPreferences.getStdDevSampleWindowPreference());
        List<Double> voltages = mlhfmMap.get(MlhfmFileContract.MAF_VOLTAGE_HEADER);

        XYSeries series = new XYSeries("Std Dev");

        for (Double voltage : voltages) {
            List<Double> values = stdDev.get(voltage);

            for (Double value : values) {
                series.add(voltage, value);
            }
        }

        XYPlot plot = (XYPlot)chart.getPlot();
        ((XYSeriesCollection)plot.getDataset()).removeAllSeries();
        ((XYSeriesCollection)plot.getDataset()).addSeries(series);
    }
}
