import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * View + controller: the student information form and all of its event handlers.
 *
 * Events demonstrated:
 *   ActionEvent  - Register, Clear, Exit buttons (and Enter key in text fields)
 *   ItemEvent    - course combo box and gender radio buttons
 *   KeyEvent     - input filtering and live character counters on text fields
 *   WindowEvent  - exit confirmation (windowClosing) and initial focus (windowOpened)
 *   MouseEvent   - button hover colours, double-click to clear the log
 */
public class StudentFormFrame extends JFrame {

    // ---------- Constants ----------
    private static final String[] COURSES = {
            "-- Select Course --", "BSc Computer Science", "BSc Information Technology",
            "BBIT", "BSc Software Engineering", "BSc Data Science"
    };
    private static final int NAME_MAX = 40;
    private static final int ADM_MAX = 15;
    private static final String ADM_REGEX = "[A-Za-z0-9/\\-]{5,15}";
    private static final String NAME_REGEX = "[A-Za-z][A-Za-z .'\\-]*";

    private static final Color BTN_NORMAL = new Color(230, 230, 230);
    private static final Color BTN_HOVER = new Color(173, 216, 230);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    // ---------- Components ----------
    private JTextField txtName;
    private JTextField txtAdmNo;
    private JComboBox<String> cmbCourse;
    private JRadioButton rbMale, rbFemale, rbOther;
    private ButtonGroup genderGroup;
    private JButton btnRegister, btnClear, btnExit;
    private JTextArea txtDisplay;
    private JLabel lblStatus, lblNameCount, lblAdmCount;

    // ---------- Data ----------
    private final List<Student> students = new ArrayList<>();
    private final StudentDAO dao = new StudentDAO();   // MySQL access

    public StudentFormFrame() {
        super("Student Information Interface");
        initComponents();
        layoutComponents();
        registerListeners();

        // Let our WindowListener decide whether the window really closes
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(560, 620);
        setLocationRelativeTo(null);
        getRootPane().setDefaultButton(btnRegister);
    }

    // =====================================================================
    // 1. Create components
    // =====================================================================
    private void initComponents() {
        txtName = new JTextField(20);
        txtAdmNo = new JTextField(20);
        cmbCourse = new JComboBox<>(COURSES);

        rbMale = new JRadioButton("Male");
        rbFemale = new JRadioButton("Female");
        rbOther = new JRadioButton("Other");
        genderGroup = new ButtonGroup();
        genderGroup.add(rbMale);
        genderGroup.add(rbFemale);
        genderGroup.add(rbOther);

        btnRegister = createButton("Register", KeyEvent.VK_R, "Validate and register the student");
        btnClear = createButton("Clear", KeyEvent.VK_C, "Reset all input fields");
        btnExit = createButton("Exit", KeyEvent.VK_X, "Close the application");

        txtDisplay = new JTextArea(10, 40);
        txtDisplay.setEditable(false);
        txtDisplay.setLineWrap(true);
        txtDisplay.setWrapStyleWord(true);
        txtDisplay.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        txtDisplay.setToolTipText("Double-click to clear the log");

        lblStatus = new JLabel(" Ready.");
        lblStatus.setBorder(BorderFactory.createEtchedBorder());
        lblNameCount = new JLabel("0/" + NAME_MAX);
        lblAdmCount = new JLabel("0/" + ADM_MAX);
    }

    private JButton createButton(String text, int mnemonic, String tip) {
        JButton b = new JButton(text);
        b.setMnemonic(mnemonic);
        b.setToolTipText(tip);
        b.setOpaque(true);
        b.setBackground(BTN_NORMAL);
        b.setFocusPainted(false);
        return b;
    }

    // =====================================================================
    // 2. Layout
    // =====================================================================
    private void layoutComponents() {
        setLayout(new BorderLayout(8, 8));

        JLabel title = new JLabel("Student Registration", SwingConstants.CENTER);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        add(title, BorderLayout.NORTH);

        // Form panel
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Student Details"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 8, 6, 8);
        g.anchor = GridBagConstraints.WEST;

        addRow(form, g, 0, "Student Name:", txtName, lblNameCount);
        addRow(form, g, 1, "Admission No:", txtAdmNo, lblAdmCount);
        addRow(form, g, 2, "Course:", cmbCourse, null);

        JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        genderPanel.add(rbMale);
        genderPanel.add(rbFemale);
        genderPanel.add(rbOther);
        addRow(form, g, 3, "Gender:", genderPanel, null);

        // Buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        buttons.add(btnRegister);
        buttons.add(btnClear);
        buttons.add(btnExit);

        JPanel top = new JPanel(new BorderLayout());
        top.add(form, BorderLayout.CENTER);
        top.add(buttons, BorderLayout.SOUTH);
        top.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        // Display area
        JScrollPane scroll = new JScrollPane(txtDisplay);
        scroll.setBorder(BorderFactory.createTitledBorder("Registration Feedback"));

        JPanel center = new JPanel(new BorderLayout(5, 5));
        center.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        center.add(top, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);
        add(lblStatus, BorderLayout.SOUTH);
    }

    private void addRow(JPanel p, GridBagConstraints g, int row, String label,
                        JComponent field, JLabel counter) {
        g.gridy = row;
        g.gridx = 0;
        g.weightx = 0;
        g.fill = GridBagConstraints.NONE;
        p.add(new JLabel(label), g);

        g.gridx = 1;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        p.add(field, g);

        g.gridx = 2;
        g.weightx = 0;
        g.fill = GridBagConstraints.NONE;
        if (counter != null) {
            p.add(counter, g);
        }
    }

    // =====================================================================
    // 3. Register ALL listeners
    // =====================================================================
    private void registerListeners() {
        registerActionEvents();
        registerItemEvents();
        registerKeyEvents();
        registerWindowEvents();
        registerMouseEvents();
    }

    // ---------------------- ActionEvent ----------------------
    private void registerActionEvents() {
        btnRegister.addActionListener(e -> registerStudent());
        btnClear.addActionListener(e -> clearForm());

        // Exit button re-uses the WindowEvent handler so confirmation code exists once
        btnExit.addActionListener(e ->
                dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));

        // Pressing Enter in the name field moves to the admission number field
        txtName.addActionListener(e -> txtAdmNo.requestFocusInWindow());
    }

    // ---------------------- ItemEvent ------------------------
    private void registerItemEvents() {
        // Fires twice per change (DESELECTED then SELECTED): only react to SELECTED
        cmbCourse.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                if (cmbCourse.getSelectedIndex() == 0) {
                    setStatus("Course selection cleared.");
                } else {
                    setStatus("Course selected: " + e.getItem());
                }
            }
        });

        ItemListener genderListener = e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                JRadioButton rb = (JRadioButton) e.getItem();
                setStatus("Gender selected: " + rb.getText());
            }
        };
        rbMale.addItemListener(genderListener);
        rbFemale.addItemListener(genderListener);
        rbOther.addItemListener(genderListener);
    }

    // ---------------------- KeyEvent -------------------------
    private void registerKeyEvents() {
        // Name: letters, space, apostrophe, hyphen, full stop only; max length
        txtName.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (isControl(c)) return;
                boolean allowed = Character.isLetter(c) || c == ' ' || c == '\'' || c == '-' || c == '.';
                if (!allowed || txtName.getText().length() >= NAME_MAX) {
                    e.consume();                       // discard the key
                    Toolkit.getDefaultToolkit().beep();
                    setStatus("Name: character '" + c + "' not allowed (letters only, max " + NAME_MAX + ").");
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                lblNameCount.setText(txtName.getText().length() + "/" + NAME_MAX);
            }
        });

        // Admission number: letters, digits, '/' and '-' only; max length
        txtAdmNo.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (isControl(c)) return;
                boolean allowed = Character.isLetterOrDigit(c) || c == '/' || c == '-';
                if (!allowed || txtAdmNo.getText().length() >= ADM_MAX) {
                    e.consume();
                    Toolkit.getDefaultToolkit().beep();
                    setStatus("Admission No: character '" + c + "' not allowed (A-Z, 0-9, / or -, max " + ADM_MAX + ").");
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                lblAdmCount.setText(txtAdmNo.getText().length() + "/" + ADM_MAX);
            }
        });
    }

    private boolean isControl(char c) {
        // Backspace, delete, enter, tab, escape etc. must always pass through
        return Character.isISOControl(c);
    }

    // ---------------------- WindowEvent ----------------------
    private void registerWindowEvents() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                txtName.requestFocusInWindow();
                setStatus(dao.isConnected() ? "Connected to MySQL." : "No database - running in memory only.");
            }

            @Override
            public void windowClosing(WindowEvent e) {
                int choice = JOptionPane.showConfirmDialog(StudentFormFrame.this,
                        "Are you sure you want to exit?\nRegistered students this session: " + students.size(),
                        "Confirm Exit", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (choice == JOptionPane.YES_OPTION) {
                    dao.close();
                    dispose();
                    System.exit(0);
                }
                // NO -> do nothing, window stays open
            }
        });
    }

    // ---------------------- MouseEvent -----------------------
    private void registerMouseEvents() {
        addHover(btnRegister, "Click to register the student");
        addHover(btnClear, "Click to clear the form");
        addHover(btnExit, "Click to exit the application");

        // Double-click on the display area clears the feedback log
        txtDisplay.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    txtDisplay.setText("");
                    setStatus("Feedback log cleared (double-click).");
                }
            }
        });
    }

    private void addHover(JButton button, String hint) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(BTN_HOVER);
                setStatus(hint);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(BTN_NORMAL);
                setStatus(" Ready.");
            }
        });
    }

    // =====================================================================
    // Application logic
    // =====================================================================
    private void registerStudent() {
        String name = txtName.getText().trim();
        String adm = txtAdmNo.getText().trim().toUpperCase();
        String gender = getSelectedGender();

        // ----- Validation -----
        if (name.isEmpty()) {
            showError("Student name is required.", txtName);
            return;
        }
        if (!name.matches(NAME_REGEX)) {
            showError("Name must start with a letter and contain letters only.", txtName);
            return;
        }
        if (adm.isEmpty()) {
            showError("Admission number is required.", txtAdmNo);
            return;
        }
        if (!adm.matches(ADM_REGEX)) {
            showError("Admission number must be 5-15 characters (letters, digits, / or -).", txtAdmNo);
            return;
        }
        if (cmbCourse.getSelectedIndex() == 0) {
            showError("Please select a course.", cmbCourse);
            return;
        }
        if (gender == null) {
            showError("Please select a gender.", rbMale);
            return;
        }
        if (isDuplicate(adm)) {
            showError("Admission number " + adm + " is already registered.", txtAdmNo);
            return;
        }

        // ----- Success -----
        Student s = new Student(name, adm, (String) cmbCourse.getSelectedItem(), gender);
        students.add(s);
        if (dao.isConnected()) {
            if (dao.save(s)) {
                appendDisplay("Saved to MySQL database.");
            } else {
                appendDisplay("ERROR   - Database save failed: " + dao.getLastError());
            }
        }
        appendDisplay("SUCCESS - Registration #" + students.size() + "\n    " + s);
        setStatus("Student registered successfully. Total: " + students.size());
        clearForm();
    }

    private boolean isDuplicate(String adm) {
        for (Student s : students) {
            if (s.getAdmissionNo().equalsIgnoreCase(adm)) return true;
        }
        return dao.exists(adm);
    }

    private String getSelectedGender() {
        if (rbMale.isSelected()) return "Male";
        if (rbFemale.isSelected()) return "Female";
        if (rbOther.isSelected()) return "Other";
        return null;
    }

    /** Resets inputs only; the registration log is kept. */
    private void clearForm() {
        txtName.setText("");
        txtAdmNo.setText("");
        cmbCourse.setSelectedIndex(0);
        genderGroup.clearSelection();
        lblNameCount.setText("0/" + NAME_MAX);
        lblAdmCount.setText("0/" + ADM_MAX);
        txtName.requestFocusInWindow();
    }

    private void showError(String message, JComponent focusTarget) {
        appendDisplay("ERROR   - " + message);
        setStatus("Validation error.");
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
        focusTarget.requestFocusInWindow();
    }

    private void appendDisplay(String text) {
        txtDisplay.append("[" + LocalTime.now().format(TIME_FMT) + "] " + text + "\n");
        txtDisplay.setCaretPosition(txtDisplay.getDocument().getLength());
    }

    private void setStatus(String text) {
        lblStatus.setText(" " + text);
    }
}
