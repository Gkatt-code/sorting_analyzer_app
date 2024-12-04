package view;

import com.formdev.flatlaf.FlatIntelliJLaf;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.util.ArrayList;
import java.io.*;
import java.util.*;
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

    private void updateVisualization(List<Double> values) {
        dataSeries.clear();
        for (int i = 0; i < values.size(); i++) {
            dataSeries.add(i, values.get(i));
        }
    }

    // Sorting algorithms with visualization
    private void insertionSortWithVisualization(List<Double> values) {
        for (int i = 1; i < values.size(); i++) {
            double key = values.get(i);
            int j = i - 1;
            while (j >= 0 && values.get(j) > key) {
                values.set(j + 1, values.get(j));
                j--;
            }
            values.set(j + 1, key);
            if (i % 10 == 0) visualizationUpdate(values);
        }
    }

    private void shellSortWithVisualization(List<Double> values) {
        int n = values.size();
        for (int gap = n/2; gap > 0; gap /= 2) {
            for (int i = gap; i < n; i++) {
                double temp = values.get(i);
                int j = i;
                while (j >= gap && values.get(j - gap) > temp) {
                    values.set(j, values.get(j - gap));
                    j -= gap;
                }
                values.set(j, temp);
                if (i % 10 == 0) visualizationUpdate(values);
            }
        }
    }

    private void mergeSortWithVisualization(List<Double> values) {
        if (values.size() <= 1) return;
        
        int mid = values.size() / 2;
        List<Double> left = new ArrayList<>(values.subList(0, mid));
        List<Double> right = new ArrayList<>(values.subList(mid, values.size()));

        mergeSortWithVisualization(left);
        mergeSortWithVisualization(right);
        merge(values, left, right);
        visualizationUpdate(values);
    }

    private void merge(List<Double> values, List<Double> left, List<Double> right) {
        int i = 0, j = 0, k = 0;
        while (i < left.size() && j < right.size()) {
            if (left.get(i) <= right.get(j)) {
                values.set(k++, left.get(i++));
            } else {
                values.set(k++, right.get(j++));
            }
        }
        while (i < left.size()) values.set(k++, left.get(i++));
        while (j < right.size()) values.set(k++, right.get(j++));
    }

    private void quickSortWithVisualization(List<Double> values) {
        quickSort(values, 0, values.size() - 1);
    }

    private void quickSort(List<Double> values, int low, int high) {
        if (low < high) {
            int pi = partition(values, low, high);
            quickSort(values, low, pi - 1);
            quickSort(values, pi + 1, high);
            visualizationUpdate(values);
        }
    }

    private int partition(List<Double> values, int low, int high) {
        double pivot = values.get(high);
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (values.get(j) < pivot) {
                i++;
                double temp = values.get(i);
                values.set(i, values.get(j));
                values.set(j, temp);
            }
        }
        double temp = values.get(i + 1);
        values.set(i + 1, values.get(high));
        values.set(high, temp);
        return i + 1;
    }

    private void heapSortWithVisualization(List<Double> values) {
        int n = values.size();
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(values, n, i);
            visualizationUpdate(values);
        }
        for (int i = n - 1; i > 0; i--) {
            double temp = values.get(0);
            values.set(0, values.get(i));
            values.set(i, temp);
            heapify(values, i, 0);
            visualizationUpdate(values);
        }
    }

    private void heapify(List<Double> values, int n, int i) {
        int largest = i;
        int left = 2 * i + 1;
        int right = 2 * i + 2;

        if (left < n && values.get(left) > values.get(largest))
            largest = left;
        if (right < n && values.get(right) > values.get(largest))
            largest = right;

        if (largest != i) {
            double swap = values.get(i);
            values.set(i, values.get(largest));
            values.set(largest, swap);
            heapify(values, n, largest);
        }
    }

    // Visualization Update
    private void visualizationUpdate(List<Double> values) {
        try {
            Thread.sleep(50);
            SwingUtilities.invokeLater(() -> updateVisualization(values));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        FlatIntelliJLaf.setup();
        SwingUtilities.invokeLater(() -> {
            new SortingAnalyzerApp().setVisible(true);
        });
    }
}