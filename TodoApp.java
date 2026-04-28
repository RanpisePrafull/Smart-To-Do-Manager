import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class TodoApp {

    private JFrame frame;
    private JTable table;
    private DefaultTableModel model;
    private JTextField taskField, deadlineField;
    private JComboBox<String> priorityBox;
    private JProgressBar progressBar;

    private static final String FILE = "tasks.txt";
    
    // Modern Color Palette
    private final Color DEEP_OCEAN = new Color(15, 32, 39);
    private final Color MIDNIGHT = new Color(32, 58, 67);
    private final Color LIGHT_SLATE = new Color(44, 83, 100);
    private final Color ACCENT_CYAN = new Color(0, 255, 230);
    private final Color COMPLETED_GREEN = new Color(46, 204, 113);
    private final Color PENDING_GOLD = new Color(241, 196, 15);

    public TodoApp() {
        setGlobalFont();
        initUI();
        loadTasks();
        updateProgress();
    }

    private void setGlobalFont() {
        Font font = new Font("Segoe UI", Font.BOLD, 13);
        UIManager.put("Label.font", font);
        UIManager.put("Button.font", font);
        UIManager.put("Table.font", new Font("Segoe UI Semibold", Font.PLAIN, 14));
    }

    // Advanced Gradient Background
    class FancyGradientPanel extends JPanel {
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gp = new GradientPaint(0, 0, DEEP_OCEAN, getWidth(), getHeight(), LIGHT_SLATE);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private void initUI() {
        frame = new JFrame("Master Task Dashboard");
        frame.setSize(900, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        FancyGradientPanel mainPanel = new FancyGradientPanel();
        mainPanel.setLayout(new BorderLayout(20, 20));
        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        frame.setContentPane(mainPanel);

        // --- THE TABLE ---
        model = new DefaultTableModel(new String[]{"Task", "Priority", "Deadline", "Status"}, 0);
        table = new JTable(model);
        styleTable();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // --- GLASS PANELS (Top & Bottom) ---
        JPanel top = createGlassPanel();
        taskField = createInteractiveField("Task Title...", 15);
        deadlineField = createInteractiveField("YYYY-MM-DD", 10);
        priorityBox = new JComboBox<>(new String[]{"High 🔥", "Medium ⚡", "Low 🍃"});
        priorityBox.setBackground(MIDNIGHT);
        priorityBox.setForeground(Color.WHITE);

        JButton addBtn = createNeonButton("Add Task", ACCENT_CYAN);
        
        top.add(createLabel("New Task:"));
        top.add(taskField);
        top.add(createLabel("Due:"));
        top.add(deadlineField);
        top.add(priorityBox);
        top.add(addBtn);

        mainPanel.add(top, BorderLayout.NORTH);

        JPanel bottom = createGlassPanel();
        JButton doneBtn = createNeonButton("Toggle Status", COMPLETED_GREEN);
        JButton deleteBtn = createNeonButton("Delete", new Color(255, 71, 87));

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(300, 25));
        progressBar.setForeground(ACCENT_CYAN);
        progressBar.setBackground(DEEP_OCEAN);

        bottom.add(doneBtn);
        bottom.add(deleteBtn);
        bottom.add(Box.createHorizontalStrut(50));
        bottom.add(progressBar);

        mainPanel.add(bottom, BorderLayout.SOUTH);

        // Logic Listeners
        addBtn.addActionListener(e -> addTask());
        doneBtn.addActionListener(e -> toggleStatus());
        deleteBtn.addActionListener(e -> deleteTask());

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void styleTable() {
        table.setRowHeight(50);
        table.setIntercellSpacing(new Dimension(10, 10));
        table.setSelectionBackground(new Color(255, 255, 255, 30));
        table.setOpaque(false);
        ((DefaultTableCellRenderer)table.getDefaultRenderer(Object.class)).setOpaque(false);

        JTableHeader header = table.getTableHeader();
        header.setBackground(MIDNIGHT);
        header.setForeground(ACCENT_CYAN);
        header.setFont(new Font("Segoe UI", Font.BOLD, 15));

        // CUSTOM RENDERER FOR PRIORITY & STATUS PILLS
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
                JPanel cell = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 10));
                cell.setOpaque(false);
                
                String val = String.valueOf(v);
                JLabel label = new JLabel(val);
                label.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
                
                // Column 1: Priority Styles
                if (c == 1) {
                    if (val.contains("High")) label.setForeground(new Color(255, 107, 107));
                    else if (val.contains("Medium")) label.setForeground(new Color(255, 217, 61));
                    else label.setForeground(new Color(123, 237, 159));
                } 
                // Column 3: Status Pill Style
                else if (c == 3) {
                    label.setOpaque(true);
                    label.setBackground(val.equals("Completed") ? COMPLETED_GREEN : PENDING_GOLD);
                    label.setForeground(Color.BLACK);
                    label.setBorder(new EmptyBorder(5, 15, 5, 15));
                } else {
                    label.setForeground(Color.WHITE);
                }

                if (isS) cell.setBorder(new MatteBorder(0, 5, 0, 0, ACCENT_CYAN));
                
                cell.add(label);
                return cell;
            }
        });
    }

    private JPanel createGlassPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15)) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(new Color(255, 255, 255, 20)); // Subtle glass effect
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        p.setOpaque(false);
        return p;
    }

    private JTextField createInteractiveField(String hint, int size) {
        JTextField f = new JTextField(hint, size);
        f.setBackground(new Color(0, 0, 0, 100));
        f.setForeground(Color.LIGHT_GRAY);
        f.setCaretColor(ACCENT_CYAN);
        f.setBorder(new CompoundBorder(new LineBorder(LIGHT_SLATE, 1), new EmptyBorder(8, 12, 8, 12)));
        
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (f.getText().equals(hint)) f.setText("");
                f.setForeground(Color.WHITE);
                f.setBorder(new CompoundBorder(new LineBorder(ACCENT_CYAN, 2), new EmptyBorder(8, 12, 8, 12)));
            }
            public void focusLost(FocusEvent e) {
                if (f.getText().isEmpty()) f.setText(hint);
                f.setBorder(new CompoundBorder(new LineBorder(LIGHT_SLATE, 1), new EmptyBorder(8, 12, 8, 12)));
            }
        });
        return f;
    }

    private JButton createNeonButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setForeground(color);
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(color, 2, true));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setContentAreaFilled(true);
                btn.setBackground(color);
                btn.setForeground(Color.BLACK);
            }
            public void mouseExited(MouseEvent e) {
                btn.setContentAreaFilled(false);
                btn.setForeground(color);
            }
        });
        return btn;
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Color.WHITE);
        return l;
    }

    private void addTask() {
        if (!taskField.getText().isEmpty() && !taskField.getText().contains("...")) {
            model.addRow(new Object[]{taskField.getText(), priorityBox.getSelectedItem(), deadlineField.getText(), "Pending"});
            updateProgress();
            saveTasks();
        }
    }

    private void toggleStatus() {
        int row = table.getSelectedRow();
        if (row != -1) {
            String current = (String) model.getValueAt(row, 3);
            model.setValueAt(current.equals("Pending") ? "Completed" : "Pending", row, 3);
            updateProgress();
            saveTasks();
        }
    }

    private void deleteTask() {
        int row = table.getSelectedRow();
        if (row != -1) {
            model.removeRow(row);
            updateProgress();
            saveTasks();
        }
    }

    private void updateProgress() {
        int total = model.getRowCount();
        int done = 0;
        for (int i = 0; i < total; i++) if ("Completed".equals(model.getValueAt(i, 3))) done++;
        int percent = total == 0 ? 0 : (done * 100 / total);
        progressBar.setValue(percent);
        progressBar.setString("Mastery: " + percent + "%");
    }

    private void saveTasks() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {
            for (int i = 0; i < model.getRowCount(); i++) {
                pw.println(model.getValueAt(i,0)+","+model.getValueAt(i,1)+","+model.getValueAt(i,2)+","+model.getValueAt(i,3));
            }
        } catch (Exception e) {}
    }

    private void loadTasks() {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
            String line;
            while ((line = br.readLine()) != null) model.addRow(line.split(","));
        } catch (Exception e) {}
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TodoApp::new);
    }
}