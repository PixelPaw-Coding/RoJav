import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.nio.file.*;

public class RoJavMain extends JFrame {

    private static final Path MODS_DIR = Paths.get("mods");
    private static final Path SETTINGS_FILE = Paths.get("settings.properties");

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JTextArea modsTextArea;
    private JTextArea fastFlagsEditor;

    public RoJavMain() {
        setTitle("RoJav Mod Loader");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        applyFluentStyle();

        JSplitPane splitPane = new JSplitPane();
        splitPane.setDividerLocation(220);
        splitPane.setEnabled(false);

        // ===== SIDEBAR =====
        DefaultListModel<String> model = new DefaultListModel<>();
        model.addElement("Mods");
        model.addElement("FFlags Editor");

        JList<String> sidebar = new JList<>(model);
        sidebar.setFont(fluentFont(14));
        sidebar.setFixedCellHeight(48);
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        sidebar.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sidebar.setSelectedIndex(0);

        splitPane.setLeftComponent(sidebar);

        // ===== CONTENT =====
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        contentPanel.add(createModsPanel(), "Mods");
        contentPanel.add(createFastFlagsPanel(), "FFlags Editor");

        splitPane.setRightComponent(contentPanel);

        // ===== BOTTOM BAR =====
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 12));
        bottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220,220,220)));

        JButton saveBtn = fluentButton("Save");
        JButton launchBtn = fluentButton("Launch Roblox");

        saveBtn.addActionListener(e -> saveFastFlags());
        launchBtn.addActionListener(e -> launchRoblox());

        bottom.add(saveBtn);
        bottom.add(launchBtn);

        add(splitPane, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        sidebar.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cardLayout.show(contentPanel, sidebar.getSelectedValue());
            }
        });

        loadModsList();
        loadFastFlags();
    }

    // ===== MODS PANEL =====
    private JPanel createModsPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));

        JButton openMods = fluentButton("Open Mods Folder");
        openMods.addActionListener(e -> openModsFolder());

        modsTextArea = new JTextArea();
        modsTextArea.setFont(fluentFont(13));
        modsTextArea.setEditable(false);
        modsTextArea.setMargin(new Insets(10,10,10,10));

        panel.add(openMods, BorderLayout.NORTH);
        panel.add(new JScrollPane(modsTextArea), BorderLayout.CENTER);
        return panel;
    }

    // ===== FASTFLAGS PANEL =====
    private JPanel createFastFlagsPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));

        fastFlagsEditor = new JTextArea();
        fastFlagsEditor.setFont(fluentFont(13));
        fastFlagsEditor.setMargin(new Insets(10,10,10,10));

        JScrollPane scroll = new JScrollPane(fastFlagsEditor);
        scroll.setRowHeaderView(new LineNumberView(fastFlagsEditor));

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // ===== FILE OPS =====
    private void openModsFolder() {
        try {
            Files.createDirectories(MODS_DIR);
            Desktop.getDesktop().open(MODS_DIR.toFile());
            loadModsList();
        } catch (Exception ignored) {}
    }

    private void loadModsList() {
        try {
            Files.createDirectories(MODS_DIR);
            StringBuilder sb = new StringBuilder();
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(MODS_DIR)) {
                for (Path p : ds) sb.append(p.getFileName()).append("\n");
            }
            modsTextArea.setText(sb.length() == 0 ? "(mods folder empty)" : sb.toString());
        } catch (Exception e) {
            modsTextArea.setText("Failed to read mods folder");
        }
    }

    private void loadFastFlags() {
        try {
            if (Files.exists(SETTINGS_FILE)) {
                fastFlagsEditor.setText(Files.readString(SETTINGS_FILE));
            }
        } catch (Exception ignored) {}
    }

    private void saveFastFlags() {
        try {
            Files.writeString(SETTINGS_FILE, fastFlagsEditor.getText());
        } catch (Exception ignored) {}
    }

    private void launchRoblox() {
        try {
            Path exe = Paths.get("C:\\Program Files (x86)\\Roblox\\RobloxPlayerLauncher.exe");
            if (Files.exists(exe)) new ProcessBuilder(exe.toString()).start();
        } catch (Exception ignored) {}
    }

    // ===== FLUENT STYLE =====
    private void applyFluentStyle() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
    }

    private Font fluentFont(int size) {
        return new Font("Segoe UI", Font.PLAIN, size);
    }

    private JButton fluentButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(fluentFont(13));
        btn.setPreferredSize(new Dimension(140, 36));
        return btn;
    }

    // ===== LINE NUMBERS =====
    static class LineNumberView extends JComponent {
        private final JTextArea area;
        LineNumberView(JTextArea a) {
            area = a;
            area.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e){ repaint(); }
                public void removeUpdate(DocumentEvent e){ repaint(); }
                public void changedUpdate(DocumentEvent e){ repaint(); }
            });
        }
        protected void paintComponent(Graphics g) {
            FontMetrics fm = g.getFontMetrics(area.getFont());
            int h = fm.getHeight();
            int start = area.getVisibleRect().y / h + 1;
            int end = start + area.getVisibleRect().height / h;
            int y = -area.getVisibleRect().y + h;
            for (int i = start; i <= end; i++) {
                g.drawString(String.valueOf(i), 8, y);
                y += h;
            }
        }
        public Dimension getPreferredSize() {
            return new Dimension(40, Integer.MAX_VALUE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new RoJavMain().setVisible(true);
        });
    }
}
