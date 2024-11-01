import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class PDFExtractorApp {
    private JFrame frame;
    private JTextField filePathField;
    private JTextArea extractedTextArea;
    private JProgressBar progressBar;
    private String extractedText; // To hold the extracted text for downloading

    public PDFExtractorApp() {
        frame = new JFrame("PDF Data Extractor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        // Create a panel for the file selection and extraction buttons
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout());

        JLabel label = new JLabel("Select PDF File: ");
        filePathField = new JTextField(30);
        JButton browseButton = new JButton("Browse");
        JButton extractButton = new JButton("Extract Text");
        JButton downloadButton = new JButton("Download");

        inputPanel.add(label);
        inputPanel.add(filePathField);
        inputPanel.add(browseButton);
        inputPanel.add(extractButton);
        inputPanel.add(downloadButton); // Add download button

        // Create a panel for the extracted text and progress bar
        JPanel outputPanel = new JPanel();
        outputPanel.setLayout(new BorderLayout());

        extractedTextArea = new JTextArea();
        extractedTextArea.setLineWrap(true);
        extractedTextArea.setWrapStyleWord(true);
        extractedTextArea.setEditable(false);
        extractedTextArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        progressBar = new JProgressBar();
        progressBar.setVisible(false);

        outputPanel.add(progressBar, BorderLayout.NORTH);
        outputPanel.add(new JScrollPane(extractedTextArea), BorderLayout.CENTER);

        // Add panels to the main frame
        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(outputPanel, BorderLayout.CENTER);

        // Button actions
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showOpenDialog(frame);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    filePathField.setText(selectedFile.getAbsolutePath());
                }
            }
        });

        extractButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String filePath = filePathField.getText();
                if (filePath.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Please select a PDF file.",
                            "Warning", JOptionPane.WARNING_MESSAGE);
                } else {
                    extractTextFromPDF(filePath);
                }
            }
        });

        downloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (extractedText != null && !extractedText.isEmpty()) {
                    saveTextToFile(extractedText);
                } else {
                    JOptionPane.showMessageDialog(frame, "No text available to download.",
                            "Warning", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        frame.setVisible(true);
    }

    private void extractTextFromPDF(String filePath) {
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                progressBar.setVisible(true);
                progressBar.setIndeterminate(true);

                PDDocument document = Loader.loadPDF(new File(filePath));
                PDFTextStripper pdfStripper = new PDFTextStripper();
                String text = pdfStripper.getText(document);
                document.close();

                return text;
            }

            @Override
            protected void done() {
                try {
                    extractedText = get(); // Store the extracted text
                    extractedTextArea.setText(extractedText);
                    progressBar.setVisible(false);
                } catch (InterruptedException | ExecutionException e) {
                    JOptionPane.showMessageDialog(frame, "Error extracting text: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    private void saveTextToFile(String text) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Specify a file to save");
        int userSelection = fileChooser.showSaveDialog(frame);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) {
                writer.write(text);
                JOptionPane.showMessageDialog(frame, "Text saved successfully to " + fileToSave.getAbsolutePath());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, "Error saving text: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PDFExtractorApp());
    }
}
