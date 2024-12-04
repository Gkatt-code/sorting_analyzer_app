package view;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.util.ArrayList;
import java.io.*;
import java.util.List;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
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

    // Right result area
    private void setupResultArea() {
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    }

    // CSV file uploading
    private void uploadCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV files", "csv"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (CSVReader reader = new CSVReader(new FileReader(fileChooser.getSelectedFile()))) {
                csvData = reader.readAll();
                headers = csvData.get(0); 
                csvData.remove(0);

                columnSelector.removeAllItems();
                for (int i = 0; i < headers.length; i++) {
                    columnSelector.addItem(headers[i]);
                }

                // Clear previous visualization
                dataSeries.clear();

            } catch (IOException | CsvException ex) {
                JOptionPane.showMessageDialog(this, "Error reading CSV: " + ex.getMessage());
            }
        }
    }

    // Error Handling
    private boolean isNumeric(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(str.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void performSort() {
        if (csvData == null || csvData.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please upload a CSV file first.");
            return;
        }

        int selectedColumn = columnSelector.getSelectedIndex();
        String selectedAlgorithm = (String) algorithmSelector.getSelectedItem();

        List<Double> values = new ArrayList<>();
        for (String[] row : csvData) {
            if (row.length > selectedColumn && isNumeric(row[selectedColumn])) {
                values.add(Double.parseDouble(row[selectedColumn].trim()));
            }
        }

        if (values.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No numeric values found in the selected column.");
            return;
        }

        // Initial visualization
        updateVisualization(values);

        long startTime = System.nanoTime();
        
        // Perform sorting in a separate thread
        new Thread(() -> {
            switch (selectedAlgorithm) {
                case "Insertion Sort" -> insertionSortWithVisualization(values);
                case "Shell Sort" -> shellSortWithVisualization(values);
                case "Merge Sort" -> mergeSortWithVisualization(values);
                case "Quick Sort" -> quickSortWithVisualization(values);
                case "Heap Sort" -> heapSortWithVisualization(values);
            }

            long endTime = System.nanoTime();
            double duration = (endTime - startTime) / 1_000_000.0;

            SwingUtilities.invokeLater(() -> {
                StringBuilder result = new StringBuilder();
                result.append(String.format("Sorting Results:\n"));
                result.append(String.format("Algorithm: %s\n", selectedAlgorithm));
                result.append(String.format("Time taken: %.2f ms\n", duration));
                result.append(String.format("Total numeric values: %d\n", values.size()));
                result.append("\nAll sorted values:\n");

                for (int i = 0; i < values.size(); i++) {
                    result.append(String.format("%d. %.4f\n", (i + 1), values.get(i)));
                    if ((i + 1) % 100 == 0) result.append("\n");
                }

                resultArea.setText(result.toString());
                resultArea.setCaretPosition(0);
                updateVisualization(values);
            });
        }).start();
    }
}