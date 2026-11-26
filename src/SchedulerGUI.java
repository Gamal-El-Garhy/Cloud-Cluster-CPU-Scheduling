import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SchedulerGUI extends JFrame {

    private final DefaultTableModel inputModel;
    private final JTable inputTable;
    private final JTextField quantumField = new JTextField("3", 4);
    private final JTextField csField = new JTextField("1", 4);
    private final JCheckBox compactCheck = new JCheckBox("Compact Matrix", true);

    private final GanttPanel ganttPanel = new GanttPanel();
    private final DefaultTableModel metricsModel;
    private final JLabel summaryLabel = new JLabel("Run a scheduler to view results.");
    private final JTextArea traceArea = new JTextArea();

    private SimulationResult lastResult = null;

    public SchedulerGUI() {
        super("Cloud-Cluster CPU Scheduler Simulator (SJF & ACS)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 740);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // 1. Top Control Panel (Presets + Q/CS + Run buttons)
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        topPanel.setBorder(BorderFactory.createTitledBorder("Controls & Presets"));

        JButton tc1Btn = new JButton("Load TC 1");
        JButton tc2Btn = new JButton("Load TC 2");
        JButton tc3Btn = new JButton("Load TC 3");
        JButton customBtn = new JButton("Custom Input");
        JButton runAcsBtn = new JButton("▶ Run ACS");
        JButton runSjfBtn = new JButton("▶ Run Preemptive SJF");

        Font boldBtn = runAcsBtn.getFont().deriveFont(Font.BOLD);
        runAcsBtn.setFont(boldBtn);
        runSjfBtn.setFont(boldBtn);

        topPanel.add(tc1Btn);
        topPanel.add(tc2Btn);
        topPanel.add(tc3Btn);
        topPanel.add(customBtn);
        topPanel.add(new JSeparator(SwingConstants.VERTICAL));
        topPanel.add(new JLabel("Quantum (Q):"));
        topPanel.add(quantumField);
        topPanel.add(new JLabel("Context Switch (CS):"));
        topPanel.add(csField);
        topPanel.add(compactCheck);
        topPanel.add(runAcsBtn);
        topPanel.add(runSjfBtn);

        add(topPanel, BorderLayout.NORTH);

        // 2. Left Panel: Process Input Table
        inputModel = new DefaultTableModel(
                new String[]{"ID", "Arrival", "Burst", "Priority"}, 0);
        inputTable = new JTable(inputModel);
        inputTable.setRowHeight(24);

        JPanel inputButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        JButton addRowBtn = new JButton("+ Add");
        JButton removeRowBtn = new JButton("- Remove");
        JButton clearBtn = new JButton("Clear");
        inputButtons.add(addRowBtn);
        inputButtons.add(removeRowBtn);
        inputButtons.add(clearBtn);

        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Processes Input (Double-click cell to edit)"));
        leftPanel.setPreferredSize(new Dimension(300, 0));
        leftPanel.add(new JScrollPane(inputTable), BorderLayout.CENTER);
        leftPanel.add(inputButtons, BorderLayout.SOUTH);

        add(leftPanel, BorderLayout.WEST);

        // 3. Center Panel: Gantt Chart (top) + Tabs for Metrics & Step Trace (bottom)
        JPanel ganttWrapper = new JPanel(new BorderLayout());
        ganttWrapper.setBorder(BorderFactory.createTitledBorder("Gantt Chart Timeline"));
        JScrollPane ganttScroll = new JScrollPane(ganttPanel);
        ganttScroll.setPreferredSize(new Dimension(0, 155));
        ganttWrapper.add(ganttScroll, BorderLayout.CENTER);
        ganttWrapper.add(createLegendPanel(), BorderLayout.SOUTH);

        metricsModel = new DefaultTableModel(
                new String[]{"Process", "Arrival", "Burst", "Finish", "Turnaround", "Waiting", "Response"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable metricsTable = new JTable(metricsModel);
        metricsTable.setRowHeight(24);

        summaryLabel.setBorder(new EmptyBorder(8, 8, 8, 8));
        summaryLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        JPanel metricsTab = new JPanel(new BorderLayout(5, 5));
        metricsTab.add(new JScrollPane(metricsTable), BorderLayout.CENTER);
        metricsTab.add(summaryLabel, BorderLayout.SOUTH);

        traceArea.setEditable(false);
        traceArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane traceTab = new JScrollPane(traceArea);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Metrics & Averages", metricsTab);
        tabs.addTab("Heuristic Demotion Matrix & Aging Trace", traceTab);

        JSplitPane centerSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, ganttWrapper, tabs);
        centerSplit.setResizeWeight(0.28);
        add(centerSplit, BorderLayout.CENTER);

        // 4. Button Actions
        tc1Btn.addActionListener(e -> loadPreset(1));
        tc2Btn.addActionListener(e -> loadPreset(2));
        tc3Btn.addActionListener(e -> loadPreset(3));
        customBtn.addActionListener(e -> {
            if (inputTable.isEditing()) {
                inputTable.getCellEditor().stopCellEditing();
            }
            inputModel.setRowCount(0);
            inputModel.addRow(new Object[]{1, 0, 1, 1});
            quantumField.setText("2");
            csField.setText("0");
        });

        addRowBtn.addActionListener(e -> {
            int nextId = inputModel.getRowCount() + 1;
            inputModel.addRow(new Object[]{nextId, 0, 1, 1});
        });
        removeRowBtn.addActionListener(e -> {
            int row = inputTable.getSelectedRow();
            if (row >= 0) inputModel.removeRow(row);
        });
        clearBtn.addActionListener(e -> inputModel.setRowCount(0));

        runAcsBtn.addActionListener(e -> runSimulation(new ACSScheduler()));
        runSjfBtn.addActionListener(e -> runSimulation(new PreemptiveSJFScheduler()));

        compactCheck.addActionListener(e -> {
            if (lastResult != null) {
                updateTraceText(lastResult);
            }
        });

        // Load Assignment Test Case 2 by default
        loadPreset(2);
    }

    private JPanel createLegendPanel() {
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        legend.add(legendChip("Running Process", new Color(78, 154, 214)));
        legend.add(legendChip("Context Switch (CS)", new Color(240, 173, 78)));
        legend.add(legendChip("Idle", new Color(210, 214, 220)));
        return legend;
    }

    private JPanel legendChip(String text, Color color) {
        JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        JLabel box = new JLabel("   ");
        box.setOpaque(true);
        box.setBackground(color);
        box.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        chip.add(box);
        chip.add(new JLabel(text));
        return chip;
    }

    private void loadPreset(int tc) {
        if (inputTable.isEditing()) {
            inputTable.getCellEditor().stopCellEditing();
        }
        inputModel.setRowCount(0);
        switch (tc) {
            case 1 -> {
                quantumField.setText("3");
                csField.setText("0");
                inputModel.addRow(new Object[]{1, 0, 4, 2});
                inputModel.addRow(new Object[]{2, 1, 7, 3});
                inputModel.addRow(new Object[]{3, 3, 2, 1});
            }
            case 2 -> {
                quantumField.setText("3");
                csField.setText("1");
                inputModel.addRow(new Object[]{1, 0, 5, 4});
                inputModel.addRow(new Object[]{2, 1, 6, 2});
                inputModel.addRow(new Object[]{3, 4, 4, 3});
            }
            case 3 -> {
                quantumField.setText("3");
                csField.setText("1");
                inputModel.addRow(new Object[]{1, 0, 20, 4});
                inputModel.addRow(new Object[]{2, 1, 15, 5});
                inputModel.addRow(new Object[]{3, 4, 2, 1});
                inputModel.addRow(new Object[]{4, 8, 1, 2});
                inputModel.addRow(new Object[]{5, 12, 2, 1});
                inputModel.addRow(new Object[]{6, 25, 1, 3});
            }
        }
    }

    private List<Process> readProcessesFromTable() {
        if (inputTable.isEditing()) {
            inputTable.getCellEditor().stopCellEditing();
        }
        List<Process> list = new ArrayList<>();
        for (int i = 0; i < inputModel.getRowCount(); i++) {
            int id = Integer.parseInt(inputModel.getValueAt(i, 0).toString().trim());
            int arrival = Integer.parseInt(inputModel.getValueAt(i, 1).toString().trim());
            int burst = Integer.parseInt(inputModel.getValueAt(i, 2).toString().trim());
            int priority = Integer.parseInt(inputModel.getValueAt(i, 3).toString().trim());

            if (arrival < 0 || burst <= 0 || priority < 1) {
                throw new IllegalArgumentException(
                        "Invalid values in row " + (i + 1) + " (Arrival >= 0, Burst > 0, Priority >= 1).");
            }
            list.add(new Process(id, arrival, burst, priority));
        }
        return list;
    }

    private void runSimulation(Scheduler scheduler) {
        try {
            int q = Integer.parseInt(quantumField.getText().trim());
            int cs = Integer.parseInt(csField.getText().trim());
            List<Process> processes = readProcessesFromTable();

            SimulationResult result = scheduler.run(processes, q, cs);
            this.lastResult = result;

            ganttPanel.setResult(result);
            updateMetricsTable(result);
            updateTraceText(result);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Please enter valid integers for Q, CS, and all table cells.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateMetricsTable(SimulationResult result) {
        Metrics m = MetricsCalculator.calculate(result);
        metricsModel.setRowCount(0);
        for (Metrics.Row r : m.rows()) {
            metricsModel.addRow(new Object[]{
                    r.id(), r.arrival(), r.burst(), r.finish(),
                    r.turnaround(), r.waiting(), r.response()
            });
        }
        summaryLabel.setText(String.format(Locale.US,
                "%s (Q=%d, CS=%d)  |  Avg TAT: %.2f  |  Avg WT: %.2f  |  Avg RT: %.2f  |  CPU Util: %.2f%% (Busy %d, CS %d, Idle %d, Total %d)",
                result.getSchedulerName(), result.getQuantum(), result.getContextSwitchTime(),
                m.averageTurnaround(), m.averageWaiting(), m.averageResponse(),
                m.cpuUtilization(), m.busyTime(), m.contextSwitchTime(), m.idleTime(), m.totalTime()));
    }

    private void updateTraceText(SimulationResult result) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8)) {
            new MatrixPrinter(ps, compactCheck.isSelected()).print(result);
        }
        traceArea.setText(baos.toString(StandardCharsets.UTF_8));
        traceArea.setCaretPosition(0);
    }

    // Custom Panel to draw the colored Gantt Chart
    private static class GanttPanel extends JPanel {
        private record Block(String label, TimeStepLog.Activity activity, int start, int end) {}

        private static final Color[] PROCESS_COLORS = {
                new Color(78, 154, 214),
                new Color(92, 184, 92),
                new Color(155, 107, 196),
                new Color(224, 108, 117),
                new Color(56, 178, 172),
                new Color(214, 137, 16),
                new Color(108, 122, 137)
        };

        private final List<Block> blocks = new ArrayList<>();
        private int totalTime = 0;

        GanttPanel() {
            setBackground(Color.WHITE);
        }

        void setResult(SimulationResult result) {
            blocks.clear();
            totalTime = 0;

            String prevKey = null;
            for (TimeStepLog log : result.getLogs()) {
                if (log.getActivity() == TimeStepLog.Activity.END) continue;

                String label = switch (log.getActivity()) {
                    case RUNNING -> log.getRunningId();
                    case CONTEXT_SWITCH -> "CS";
                    case IDLE -> "Idle";
                    default -> "";
                };
                String key = log.getActivity() + ":" +
                        (log.getRunningId() != null ? log.getRunningId() : String.valueOf(log.getSwitchTo()));

                if (!blocks.isEmpty() && key.equals(prevKey)
                        && blocks.get(blocks.size() - 1).end() == log.getTime()) {
                    Block last = blocks.remove(blocks.size() - 1);
                    blocks.add(new Block(last.label(), last.activity(), last.start(), log.getTime() + 1));
                } else {
                    blocks.add(new Block(label, log.getActivity(), log.getTime(), log.getTime() + 1));
                }
                prevKey = key;
                totalTime = log.getTime() + 1;
            }

            int unitWidth = 34;
            int width = Math.max(700, 60 + totalTime * unitWidth);
            setPreferredSize(new Dimension(width, 95));
            revalidate();
            repaint();
        }

        private Color colorFor(Block b) {
            if (b.activity() == TimeStepLog.Activity.CONTEXT_SWITCH) {
                return new Color(240, 173, 78);
            }
            if (b.activity() == TimeStepLog.Activity.IDLE) {
                return new Color(210, 214, 220);
            }
            try {
                int num = Integer.parseInt(b.label().replaceAll("\\D+", ""));
                return PROCESS_COLORS[(Math.max(num - 1, 0)) % PROCESS_COLORS.length];
            } catch (NumberFormatException e) {
                return PROCESS_COLORS[Math.abs(b.label().hashCode()) % PROCESS_COLORS.length];
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (blocks.isEmpty()) {
                g2.setColor(Color.GRAY);
                g2.drawString("Click '▶ Run ACS' or '▶ Run Preemptive SJF' to render the Gantt Chart.", 20, 48);
                return;
            }

            int left = 25;
            int top = 16;
            int barHeight = 44;
            int unitWidth = 34;

            for (Block b : blocks) {
                int x = left + b.start() * unitWidth;
                int w = (b.end() - b.start()) * unitWidth;

                g2.setColor(colorFor(b));
                g2.fillRect(x, top, w, barHeight);
                g2.setColor(Color.DARK_GRAY);
                g2.drawRect(x, top, w, barHeight);

                // Centered block label
                g2.setFont(new Font("SansSerif", Font.BOLD, 12));
                g2.setColor(b.activity() == TimeStepLog.Activity.RUNNING ? Color.WHITE : Color.BLACK);
                FontMetrics fm = g2.getFontMetrics();
                int textX = x + (w - fm.stringWidth(b.label())) / 2;
                int textY = top + (barHeight + fm.getAscent()) / 2 - 2;
                g2.drawString(b.label(), textX, textY);

                // Start time tick
                g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
                g2.setColor(Color.BLACK);
                g2.drawString(String.valueOf(b.start()), x - 3, top + barHeight + 16);
            }

            // Final end time tick
            Block last = blocks.get(blocks.size() - 1);
            int endX = left + last.end() * unitWidth;
            g2.drawString(String.valueOf(last.end()), endX - 5, top + barHeight + 16);
        }
    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new SchedulerGUI().setVisible(true);
        });
    }

    public static void main(String[] args) {
        launch();
    }
}