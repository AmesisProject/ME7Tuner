package ui.view.ldrpid;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import math.map.Map3d;
import model.kfldimx.Kfldimx;
import model.ldrpid.Kfldrl;
import model.ldrpid.LdrpidCalculator;
import parser.me7log.Me7LogParser;
import preferences.filechooser.FileChooserPreferences;
import ui.map.axis.MapAxis;
import ui.map.map.MapTable;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class LdrpidView {
    private LdrpidCalculator.LdrpidResult ldrpidResult;

    private MapTable nonLinearTable = MapTable.getMapTable(new Double[0], new Double[0], new Double[0][0]);
    private MapTable linearTable = MapTable.getMapTable(new Double[0], new Double[0], new Double[0][0]);
    private MapTable kfldrlTable = MapTable.getMapTable(new Double[0], new Double[0], new Double[0][0]);
    private MapTable kfldimxTable = MapTable.getMapTable(new Double[0], new Double[0], new Double[0][0]);

    private MapAxis kfldimxXAxis;

    private JLabel logFileLabel;

    private final JProgressBar dpb = new JProgressBar();

    public JPanel getPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();

        constraints.fill = GridBagConstraints.BOTH;

        constraints.weightx = 0.5;
        constraints.weighty = 0.5;

        constraints.gridx = 0;
        constraints.gridy = 0;

        panel.add(getNonLinearMapPanel(), constraints);

        constraints.gridx = 1;
        constraints.gridy = 0;

        panel.add(getLinearMapPanel(), constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.insets.top = 46;

        panel.add(getKflDrlMapPanel(), constraints);

        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.insets.top = 16;

        panel.add(getKfldimxMapPanel(), constraints);

        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        constraints.insets.top = 0;

        panel.add(getLogsButton(panel), constraints);



        return panel;
    }

    private JPanel getHeader(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;

        JLabel label = new JLabel(title);
        panel.add(label, c);

        return panel;
    }

    private void initKfldimxAxis() {
        Double[][] kfldImxXAxisValues = new Double[1][];
        kfldImxXAxisValues[0] = Kfldimx.getStockXAxis();
        kfldimxXAxis = MapAxis.getMapAxis(kfldImxXAxisValues);
    }

    private JPanel getKfldimxMapPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;

        panel.add(new JLabel("KFLDIMX X-Axis"), c);

        c.gridx = 0;
        c.gridy = 1;

        initKfldimxAxis();

        panel.add(kfldimxXAxis.getScrollPane(), c);

        c.gridx = 0;
        c.gridy = 2;

        panel.add(getHeader("KFLDIMX"), c);

        c.gridx = 0;
        c.gridy = 3;

        initKflimxMap();

        panel.add(kfldimxTable.getScrollPane(), c);

        return panel;
    }

    private JPanel getKflDrlMapPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;

        panel.add(getHeader("KFLDRL"), c);

        c.gridx = 0;
        c.gridy = 1;

        initKfldrlMap();

        panel.add(kfldrlTable.getScrollPane(), c);

        return panel;
    }

    private JPanel getLogsButton(JPanel parent) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();

        JButton button = new JButton("Load ME7 Logs");
        button.addActionListener(e -> {
            final JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setCurrentDirectory(FileChooserPreferences.getDirectory());

            int returnValue = fc.showOpenDialog(parent);

            if (returnValue == JFileChooser.APPROVE_OPTION) {

                SwingUtilities.invokeLater(() -> {
                    FileChooserPreferences.setDirectory(fc.getSelectedFile().getParentFile());

                    logFileLabel.setText(fc.getSelectedFile().getPath());

                    SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                        @Override
                        public Void doInBackground() {
                            Me7LogParser parser = new Me7LogParser();
                            Map<String, List<Double>> values = parser.parseLogDirectory(Me7LogParser.LogType.LDRPID, fc.getSelectedFile(), (value, max) -> {
                                SwingUtilities.invokeLater(() -> {
                                    dpb.setMaximum(max);
                                    dpb.setValue(value);
                                    dpb.setVisible(value < max - 1);
                                });
                            });
                            ldrpidResult = LdrpidCalculator.caclulateLdrpid(values);
                            return null;
                        }

                        @Override
                        public void done() {
                            nonLinearTable.setMap(ldrpidResult.nonLinearOutput);
                            linearTable.setMap(ldrpidResult.linearOutput);
                            kfldrlTable.setMap(ldrpidResult.kfldrl);

                            Double[][] kfldImxXAxisValues = new Double[1][];
                            kfldImxXAxisValues[0] = ldrpidResult.kfldimx.xAxis;
                            kfldimxXAxis.setTableData(kfldImxXAxisValues);
                            kfldimxTable.setMap(ldrpidResult.kfldimx);
                        }
                    };

                    worker.execute();
                });
            }
        });

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.insets.top = 16;
        constraints.anchor = GridBagConstraints.CENTER;

        panel.add(button, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.insets.top = 0;

        dpb.setIndeterminate(false);
        dpb.setVisible(false);
        panel.add(dpb, constraints);

        constraints.gridx = 0;
        constraints.gridy = 2;

        logFileLabel = new JLabel("No File Selected");
        panel.add(logFileLabel, constraints);

        return panel;
    }

    private JPanel getNonLinearMapPanel() {

        JPanel panel = new JPanel();

        panel.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();

        constraints.gridx = 0;
        constraints.gridy = 0;

        panel.add(getHeader("Non Linear Boost"), constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;

        initNonLinearMap();

        JScrollPane scrollPane = nonLinearTable.getScrollPane();

        panel.add(scrollPane, constraints);

        return panel;
    }

    private JPanel getLinearMapPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();

        constraints.gridx = 0;
        constraints.gridy = 0;

        panel.add(getHeader("Linear Boost"), constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;

        initLinearMap();

        JScrollPane kfmiopMapScrollPane = linearTable.getScrollPane();

        panel.add(kfmiopMapScrollPane, constraints);

        return panel;
    }

    private void initKflimxMap() {
        kfldimxTable = MapTable.getMapTable(Kfldimx.getStockYAxis(), Kfldimx.getStockXAxis(), Kfldimx.getEmptyMap());
        kfldimxTable.setEditable(false);
    }

    private void initKfldrlMap() {
        kfldrlTable = MapTable.getMapTable(Kfldrl.getStockYAxis(), Kfldrl.getStockXAxis(), Kfldrl.getEmptyMap());
        kfldrlTable.setEditable(false);
    }

    private void initNonLinearMap() {
        nonLinearTable = MapTable.getMapTable(Kfldrl.getStockYAxis(), Kfldrl.getStockXAxis(), Kfldrl.getEmptyMap());
        nonLinearTable.getPublishSubject().subscribe(new Observer<Map3d>() {
            @Override
            public void onNext(@NonNull Map3d map3d) {
                Map3d linearMap3d = LdrpidCalculator.calculateLinearTable(map3d.zAxis);
                Map3d kfldrlMap3d = LdrpidCalculator.calculateKfldrl(map3d.zAxis, linearMap3d.zAxis);
                Map3d kfldimxMap3d = LdrpidCalculator.calculateKfldimx(map3d.zAxis, linearMap3d.zAxis);

                linearTable.setMap(linearMap3d);
                kfldrlTable.setMap(kfldrlMap3d);

                Double[][] kfldImxXAxisValues = new Double[1][];
                kfldImxXAxisValues[0] = kfldimxMap3d.xAxis;
                kfldimxXAxis.setTableData(kfldImxXAxisValues);
                kfldimxTable.setMap(kfldimxMap3d);
            }

            @Override
            public void onSubscribe(@NonNull Disposable disposable) {
            }

            @Override
            public void onError(@NonNull Throwable throwable) {
            }

            @Override
            public void onComplete() {
            }
        });
    }

    private void initLinearMap() {
        linearTable = MapTable.getMapTable(Kfldrl.getStockYAxis(), Kfldrl.getStockXAxis(), Kfldrl.getEmptyMap());
        linearTable.setEditable(false);
    }
}