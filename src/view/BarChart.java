/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

import com.formdev.flatlaf.FlatIntelliJLaf;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

public class BarChart extends JFrame {

    private JComboBox<String> columnSelector;
    private JComboBox<String> algorithmSelector;
    private JTextArea resultArea;
    private List<String[]> csvData;
    private String[] headers;
    private ChartPanel chartPanel;
    private DefaultCategoryDataset dataset;
    private JSplitPane splitPane;
    private Color[] barColors;

    public BarChart() {
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
        dataset = new DefaultCategoryDataset();
        JFreeChart chart = createChart();
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

    private JFreeChart createChart() {
        JFreeChart chart = ChartFactory.createBarChart(
                "Data Visualization",
                "Index",
                "Value",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );

        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = new BarRenderer();
        plot.setRenderer(renderer);
        plot.setBackgroundPaint(Color.WHITE);

        return chart;
    }

    private void setupResultArea() {
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    }

    private void uploadCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV files", "csv"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (CSVReader reader = new CSVReader(new FileReader(fileChooser.getSelectedFile()))) {
                csvData = reader.readAll();
                headers = csvData.get(0);
                csvData.remove(0);

                columnSelector.removeAllItems();
                for (String header : headers) {
                    columnSelector.addItem(header);
                }

                dataset.clear();

            } catch (IOException | CsvException ex) {
                JOptionPane.showMessageDialog(this, "Error reading CSV: " + ex.getMessage());
            }
        }
    }

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

    private void updateVisualization(List<Double> values, int[] status) {
        SwingUtilities.invokeLater(() -> {
            dataset.clear();
            for (int i = 0; i < values.size(); i++) {
                dataset.addValue(values.get(i), "Values", String.valueOf(i));
            }

            CategoryPlot plot = (CategoryPlot) chartPanel.getChart().getPlot();
            BarRenderer renderer = (BarRenderer) plot.getRenderer();

            for (int i = 0; i < values.size(); i++) {
                Color barColor = switch (status[i]) {
                    case 0 ->
                        Color.LIGHT_GRAY; // Unsorted
                    case 1 ->
                        Color.YELLOW;     // Currently being compared
                    case 2 ->
                        Color.GREEN;      // Sorted
                    default ->
                        Color.BLUE;      // Default
                };
                renderer.setSeriesPaint(0, barColor);
            }
        });
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

        int[] status = new int[values.size()];
        Arrays.fill(status, 0);
        updateVisualization(values, status);

        long startTime = System.nanoTime();

        new Thread(() -> {
            switch (selectedAlgorithm) {
                case "Insertion Sort" ->
                    insertionSortWithVisualization(values, status);
                case "Shell Sort" ->
                    shellSortWithVisualization(values, status);
                case "Merge Sort" ->
                    mergeSortWithVisualization(values, status);
                case "Quick Sort" ->
                    quickSortWithVisualization(values, status);
                case "Heap Sort" ->
                    heapSortWithVisualization(values, status);
            }

            long endTime = System.nanoTime();
            double duration = (endTime - startTime) / 1_000_000.0;

            Arrays.fill(status, 2); // Mark all as sorted
            updateVisualization(values, status);

            SwingUtilities.invokeLater(() -> {
                StringBuilder result = new StringBuilder();
                result.append(String.format("Sorting Results:\n"));
                result.append(String.format("Algorithm: %s\n", selectedAlgorithm));
                result.append(String.format("Time taken: %.2f ms\n", duration));
                result.append(String.format("Total numeric values: %d\n", values.size()));
                resultArea.setText(result.toString());
                resultArea.setCaretPosition(0);
            });
        }).start();
    }

    // Sorting algorithms with visualization
    private void insertionSortWithVisualization(List<Double> values, int[] status) {
        for (int i = 1; i < values.size(); i++) {
            double key = values.get(i);
            int j = i - 1;

            status[i] = 1; // Current element being inserted
            updateVisualization(values, status);

            while (j >= 0 && values.get(j) > key) {
                values.set(j + 1, values.get(j));
                status[j + 1] = 1;
                status[j] = 2;
                updateVisualization(values, status);
                j--;
            }
            values.set(j + 1, key);
            status[j + 1] = 2;
            visualizationDelay();
        }
    }

    private void shellSortWithVisualization(List<Double> values, int[] status) {
        int n = values.size();
        for (int gap = n / 2; gap > 0; gap /= 2) {
            for (int i = gap; i < n; i++) {
                double temp = values.get(i);
                status[i] = 1;
                updateVisualization(values, status);

                int j = i;
                while (j >= gap && values.get(j - gap) > temp) {
                    values.set(j, values.get(j - gap));
                    status[j] = 2;
                    updateVisualization(values, status);
                    j -= gap;
                }
                values.set(j, temp);
                status[j] = 2;
                visualizationDelay();
            }
        }
    }

    private void mergeSortWithVisualization(List<Double> values, int[] status) {
        mergeSortHelper(values, 0, values.size() - 1, status);
    }

    private void mergeSortHelper(List<Double> values, int left, int right, int[] status) {
        if (left < right) {
            int mid = (left + right) / 2;
            mergeSortHelper(values, left, mid, status);
            mergeSortHelper(values, mid + 1, right, status);
            merge(values, left, mid, right, status);
        }
    }

    private void merge(List<Double> values, int left, int mid, int right, int[] status) {
        int n1 = mid - left + 1;
        int n2 = right - mid;

        List<Double> leftArray = new ArrayList<>();
        List<Double> rightArray = new ArrayList<>();

        for (int i = 0; i < n1; i++) {
            leftArray.add(values.get(left + i));
        }
        for (int j = 0; j < n2; j++) {
            rightArray.add(values.get(mid + 1 + j));
        }

        int i = 0, j = 0, k = left;

        while (i < n1 && j < n2) {
            status[k] = 1;
            updateVisualization(values, status);

            if (leftArray.get(i) <= rightArray.get(j)) {
                values.set(k, leftArray.get(i));
                i++;
            } else {
                values.set(k, rightArray.get(j));
                j++;
            }
            status[k] = 2;
            k++;
            visualizationDelay();
        }

        while (i < n1) {
            status[k] = 1;
            updateVisualization(values, status);
            values.set(k, leftArray.get(i));
            status[k] = 2;
            i++;
            k++;
            visualizationDelay();
        }

        while (j < n2) {
            status[k] = 1;
            updateVisualization(values, status);
            values.set(k, rightArray.get(j));
            status[k] = 2;
            j++;
            k++;
            visualizationDelay();
        }
    }

    private void quickSortWithVisualization(List<Double> values, int[] status) {
        quickSortHelper(values, 0, values.size() - 1, status);
    }

    private void quickSortHelper(List<Double> values, int low, int high, int[] status) {
        if (low < high) {
            int pi = partition(values, low, high, status);
            quickSortHelper(values, low, pi - 1, status);
            quickSortHelper(values, pi + 1, high, status);
        }
    }

    private int partition(List<Double> values, int low, int high, int[] status) {
        double pivot = values.get(high);
        int i = low - 1;

        status[high] = 1;
        updateVisualization(values, status);

        for (int j = low; j < high; j++) {
            if (values.get(j) < pivot) {
                i++;
                double temp = values.get(i);
                values.set(i, values.get(j));
                values.set(j, temp);
                status[i] = 2;
                status[j] = 1;
                updateVisualization(values, status);
            }
        }

        double temp = values.get(i + 1);
        values.set(i + 1, values.get(high));
        values.set(high, temp);

        status[i + 1] = 2;
        status[high] = 0;
        updateVisualization(values, status);

        return i + 1;
    }

    private void heapSortWithVisualization(List<Double> values, int[] status) {
        int n = values.size();

        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(values, n, i, status);
        }

        for (int i = n - 1; i > 0; i--) {
            double temp = values.get(0);
            values.set(0, values.get(i));
            values.set(i, temp);

            status[i] = 2;
            updateVisualization(values, status);

            heapify(values, i, 0, status);
        }
    }

    private void heapify(List<Double> values, int n, int i, int[] status) {
        int largest = i;
        int left = 2 * i + 1;
        int right = 2 * i + 2;

        status[i] = 1;
        updateVisualization(values, status);

        if (left < n && values.get(left) > values.get(largest)) {
            largest = left;
        }

        if (right < n && values.get(right) > values.get(largest)) {
            largest = right;
        }

        if (largest != i) {
            double swap = values.get(i);
            values.set(i, values.get(largest));
            values.set(largest, swap);

            status[i] = 2;
            status[largest] = 1;
            updateVisualization(values, status);

            heapify(values, n, largest, status);
        }
        status[i] = 0;
    }

    private void visualizationDelay() {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                FlatIntelliJLaf.setup();
                new SortingAnalyzerApp().setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
