import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

public class NotepadApp extends JFrame implements ActionListener {

    private JTextArea textArea;
    private JMenuItem newFileItem, openFileItem, saveFileItem, saveAsFileItem, exitItem, darkModeItem;
    private boolean isDarkMode = false;
    private boolean isModified = false;
    private File currentFile = null;

    // Demo password for app access
    private static final String APP_PASSWORD = "1234";

    public NotepadApp() {
        if (!showPasswordDialog()) {
            JOptionPane.showMessageDialog(null, "Wrong password! Application will close.");
            System.exit(0);
        }

        initializeUI();
    }

    private boolean showPasswordDialog() {
        JPasswordField passwordField = new JPasswordField();

        int option = JOptionPane.showConfirmDialog(
                null,
                passwordField,
                "Enter Password to Open Notepad",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        return option == JOptionPane.OK_OPTION &&
               String.valueOf(passwordField.getPassword()).equals(APP_PASSWORD);
    }

    private void initializeUI() {
        setTitle("Java Notepad");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        textArea = new JTextArea();
        textArea.setFont(new Font("Arial", Font.PLAIN, 16));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                isModified = true;
            }
        });

        add(new JScrollPane(textArea), BorderLayout.CENTER);

        createMenuBar();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });

        setVisible(true);
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenu viewMenu = new JMenu("View");

        newFileItem = new JMenuItem("New");
        openFileItem = new JMenuItem("Open");
        saveFileItem = new JMenuItem("Save");
        saveAsFileItem = new JMenuItem("Save As");
        exitItem = new JMenuItem("Exit");
        darkModeItem = new JMenuItem("Dark Mode");

        // Keyboard shortcuts
        newFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        openFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        saveFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));

        newFileItem.addActionListener(this);
        openFileItem.addActionListener(this);
        saveFileItem.addActionListener(this);
        saveAsFileItem.addActionListener(this);
        exitItem.addActionListener(this);
        darkModeItem.addActionListener(this);

        fileMenu.add(newFileItem);
        fileMenu.add(openFileItem);
        fileMenu.add(saveFileItem);
        fileMenu.add(saveAsFileItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        viewMenu.add(darkModeItem);

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);

        setJMenuBar(menuBar);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == newFileItem) {
            createNewFile();
        } else if (source == openFileItem) {
            openFile();
        } else if (source == saveFileItem) {
            saveFile();
        } else if (source == saveAsFileItem) {
            saveFileAs();
        } else if (source == exitItem) {
            exitApplication();
        } else if (source == darkModeItem) {
            toggleDarkMode();
        }
    }

    private void createNewFile() {
        if (!confirmSaveIfNeeded()) {
            return;
        }

        textArea.setText("");
        currentFile = null;
        isModified = false;
        setTitle("Java Notepad");
    }

    private void openFile() {
        if (!confirmSaveIfNeeded()) {
            return;
        }

        JFileChooser fileChooser = new JFileChooser();

        int choice = fileChooser.showOpenDialog(this);
        if (choice == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
                textArea.read(reader, null);
                currentFile = selectedFile;
                isModified = false;
                setTitle("Java Notepad - " + currentFile.getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error opening file:\n" + ex.getMessage(),
                        "Open Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveFile() {
        if (currentFile == null) {
            saveFileAs();
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile))) {
            textArea.write(writer);
            isModified = false;
            JOptionPane.showMessageDialog(this, "File saved successfully.");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error saving file:\n" + ex.getMessage(),
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveFileAs() {
        JFileChooser fileChooser = new JFileChooser();

        int choice = fileChooser.showSaveDialog(this);
        if (choice == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            saveFile();
            setTitle("Java Notepad - " + currentFile.getName());
        }
    }

    private void exitApplication() {
        if (confirmSaveIfNeeded()) {
            System.exit(0);
        }
    }

    private boolean confirmSaveIfNeeded() {
        if (!isModified) {
            return true;
        }

        int option = JOptionPane.showConfirmDialog(
                this,
                "You have unsaved changes. Do you want to save them?",
                "Unsaved Changes",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION) {
            return false;
        }

        if (option == JOptionPane.YES_OPTION) {
            saveFile();
            return !isModified; // only continue if save succeeded
        }

        return true;
    }

    private void toggleDarkMode() {
        if (!isDarkMode) {
            textArea.setBackground(Color.BLACK);
            textArea.setForeground(Color.WHITE);
            textArea.setCaretColor(Color.WHITE);
            darkModeItem.setText("Light Mode");
            isDarkMode = true;
        } else {
            textArea.setBackground(Color.WHITE);
            textArea.setForeground(Color.BLACK);
            textArea.setCaretColor(Color.BLACK);
            darkModeItem.setText("Dark Mode");
            isDarkMode = false;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(NotepadApp::new);
    }
}