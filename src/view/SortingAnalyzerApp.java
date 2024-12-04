package view;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class SortingAnalyzerApp extends JFrame {
    private JComboBox<String> columnSelector;
    private JComboBox<String> algorithmSelector;
    private JTextArea resultArea;
    private List<String[]> csvData;
    private String[] headers;
    private ChartPanel chartPanel;
    private XYSeries dataSeries;
    private JSplitPane splitPane;

    public SortingAnalyzerApp() {
        setTitle("CSV Sorting Analyzer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLayout(new BorderLayout(10, 10));

        // Create components
        JPanel topPanel = new JPanel(new FlowLayout());
        JButton uploadButton = new JButton("Upload CSV");
        columnSelector = new JComboBox<>();
        algorithmSelector = new JComboBox<>(new String[]{
            "Insertion Sort", "Shell Sort", "Merge Sort", "Quick Sort", "Heap Sort"
        });
        JButton sortButton = new JButton("Sort");

        // Initialize chart
        dataSeries = new XYSeries("Data Points");
        XYSeriesCollection dataset = new XYSeriesCollection(dataSeries);
        JFreeChart chart = ChartFactory.createScatterPlot(
            "Data Visualization",
            "Index",
            "Value",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(600, 400));

        // Setup result area
        setupResultArea();
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chartPanel, new JScrollPane(resultArea));
        splitPane.setResizeWeight(0.5);

        // Add components
        topPanel.add(uploadButton);
        topPanel.add(new JLabel("Select Column:"));
        topPanel.add(columnSelector);
        topPanel.add(new JLabel("Select Algorithm:"));
        topPanel.add(algorithmSelector);
        topPanel.add(sortButton);

        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        uploadButton.addActionListener(e -> uploadCSV());
        sortButton.addActionListener(e -> performSort());
    }

}