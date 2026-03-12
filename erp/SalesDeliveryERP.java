import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.nio.file.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class SalesDeliveryERP {

    public static void main(String[] args) {
        new Login();
    }
}

/* LOGIN SYSTEM */
class Login extends JFrame implements ActionListener {

    JTextField user;
    JPasswordField pass;
    JButton login;

    Login(){

        setTitle("ERP Sales & Delivery System - Login");
        setSize(400, 250);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new GridBagLayout()); // Use GridBagLayout for the frame directly

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title label
        JLabel titleLabel = new JLabel("ERP SALES & DELIVERY SYSTEM", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(new Color(40, 70, 120));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 20, 10);
        add(titleLabel, gbc);

        // Reset insets for form fields
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.gridwidth = 1;

        // Username label
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Username:"), gbc);

        // Username field
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        user = new JTextField(15);
        add(user, gbc);

        // Password label
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Password:"), gbc);

        // Password field
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        pass = new JPasswordField(15);
        add(pass, gbc);

        // Login button - centered
        login = new JButton("LOGIN");
        login.setPreferredSize(new Dimension(100, 30));
        login.setBackground(new Color(40, 70, 120));
        login.setForeground(Color.WHITE);
        login.setFont(new Font("Arial", Font.BOLD, 12));
        login.addActionListener(this);

        gbc.gridx =1;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(15, 10, 20, 10);
        add(login, gbc); // Add button directly, not in a panel

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e){

        if(user.getText().equals("admin") &&
                new String(pass.getPassword()).equals("1234")){

            new Dashboard();
            dispose();

        }else{
            JOptionPane.showMessageDialog(this,"Invalid Login");
        }
    }
}

/* SEARCH ORDER PANEL - WITH INSTANT SEARCH */
class SearchOrderPanel extends JPanel {

    private JTextField searchField;
    private JLabel resultLabel;
    private JTable table;
    private Runnable loadSelectedRowCallback;

    public SearchOrderPanel(JTable table, Runnable loadSelectedRowCallback) {
        this.table = table;
        this.loadSelectedRowCallback = loadSelectedRowCallback;

        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder("SEARCH ORDERS"));
        setPreferredSize(new Dimension(0, 100));

        // Search input panel
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.add(new JLabel("Order ID:"), BorderLayout.WEST);
        searchField = new JTextField();
        inputPanel.add(searchField, BorderLayout.CENTER);

        // Result panel
        resultLabel = new JLabel("Type to search...", JLabel.CENTER);
        resultLabel.setForeground(Color.GRAY);
        resultLabel.setFont(new Font("Arial", Font.ITALIC, 11));

        add(inputPanel, BorderLayout.NORTH);
        add(resultLabel, BorderLayout.SOUTH);

        // Add document listener for instant search
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { performInstantSearch(); }
            public void removeUpdate(DocumentEvent e) { performInstantSearch(); }
            public void changedUpdate(DocumentEvent e) { performInstantSearch(); }
        });
    }

    private void performInstantSearch() {
        String id = searchField.getText().trim();

        if (id.isEmpty()) {
            // Clear selection when search is empty
            table.clearSelection();
            resultLabel.setText("Type to search...");
            resultLabel.setForeground(Color.GRAY);
            return;
        }

        boolean found = false;
        for (int i = 0; i < table.getRowCount(); i++) {
            if (table.getValueAt(i, 0).toString().toLowerCase().contains(id.toLowerCase())) {
                table.setRowSelectionInterval(i, i);
                table.scrollRectToVisible(table.getCellRect(i, 0, true));
                resultLabel.setText("✓ Found at row " + (i + 1));
                resultLabel.setForeground(new Color(0, 150, 0));

                // Load to form using callback
                if (loadSelectedRowCallback != null) {
                    loadSelectedRowCallback.run();
                }
                found = true;
                break;
            }
        }

        if (!found) {
            table.clearSelection();
            resultLabel.setText("✗ Order Not Found");
            resultLabel.setForeground(Color.RED);
        }
    }

    public void clearSearch() {
        searchField.setText("");
        resultLabel.setText("Type to search...");
        resultLabel.setForeground(Color.GRAY);
    }
}

/* ORDERS LIST PANEL */
class OrdersListPanel extends JPanel {
    private DefaultTableModel model;
    private JTable table;
    private JLabel totalSalesLabel;

    public OrdersListPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("ORDERS LIST"));

        // Create table model with added Date column
        model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[]{
                "Order ID", "Customer", "Product", "Quantity",
                "Unit Price", "Total Price", "Order Date", "Delivery Status"
        });

        // Create table
        table = new JTable(model);
        table.setRowHeight(25);
        table.getTableHeader().setReorderingAllowed(false);
        JScrollPane scrollPane = new JScrollPane(table);

        // Create summary panel
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        totalSalesLabel = new JLabel("Total Sales: ₱0.00");
        totalSalesLabel.setFont(new Font("Arial", Font.BOLD, 14));
        summaryPanel.add(totalSalesLabel);

        add(scrollPane, BorderLayout.CENTER);
        add(summaryPanel, BorderLayout.SOUTH);
    }

    public JTable getTable() {
        return table;
    }

    public DefaultTableModel getModel() {
        return model;
    }

    public void setTotalSales(double totalSales) {
        totalSalesLabel.setText(String.format("Total Sales: ₱%.2f", totalSales));
    }

    public void loadData() {
        model.setRowCount(0);
        double totalSales = 0;

        try {
            File file = new File("sales.txt");
            if (!file.exists()) {
                file.createNewFile();
            }

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null && !line.trim().isEmpty()) {
                String data[] = line.split(",");
                if (data.length >= 8) { // Updated to check for 8 fields (including date)
                    model.addRow(data);
                    totalSales += Double.parseDouble(data[5]);
                }
            }

            br.close();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
        }

        setTotalSales(totalSales);
    }

    public boolean isOrderIdExists(String id) {
        try {
            BufferedReader br = new BufferedReader(new FileReader("sales.txt"));
            String line;
            while ((line = br.readLine()) != null) {
                String data[] = line.split(",");
                if (data[0].equals(id)) {
                    br.close();
                    return true;
                }
            }
            br.close();
        } catch (Exception e) {
        }
        return false;
    }
}

/* ADD ORDER PANEL */
class AddOrderPanel extends JPanel {
    private JTextField orderID;
    private JTextField customer;
    private JTextField product;
    private JTextField quantity;
    private JTextField price;
    private JTextField orderDate; // New date field
    private JComboBox<String> status;
    private Runnable onOrderAdded;

    // Public getters for the text fields
    public JTextField getOrderIDField() { return orderID; }
    public JTextField getCustomerField() { return customer; }
    public JTextField getProductField() { return product; }
    public JTextField getQuantityField() { return quantity; }
    public JTextField getPriceField() { return price; }
    public JTextField getOrderDateField() { return orderDate; }
    public JComboBox<String> getStatusField() { return status; }

    public AddOrderPanel(Runnable onOrderAdded) {
        this.onOrderAdded = onOrderAdded;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(8, 2, 10, 10));

        orderID = new JTextField();
        customer = new JTextField();
        product = new JTextField();
        quantity = new JTextField();
        price = new JTextField();

        // Date field with current date as default
        orderDate = new JTextField();
        orderDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        orderDate.setToolTipText("Format: YYYY-MM-DD");

        status = new JComboBox<>(new String[]{
                "Pending", "Processing", "Shipped", "Delivered"
        });

        JButton addButton = new JButton("ADD ORDER");
        addButton.setBackground(new Color(40, 180, 70));
        addButton.setForeground(Color.WHITE);
        addButton.setFont(new Font("Arial", Font.BOLD, 14));

        formPanel.add(new JLabel("Order ID:"));
        formPanel.add(orderID);
        formPanel.add(new JLabel("Customer Name:"));
        formPanel.add(customer);
        formPanel.add(new JLabel("Product:"));
        formPanel.add(product);
        formPanel.add(new JLabel("Quantity:"));
        formPanel.add(quantity);
        formPanel.add(new JLabel("Unit Price:"));
        formPanel.add(price);
        formPanel.add(new JLabel("Order Date (YYYY-MM-DD):"));
        formPanel.add(orderDate);
        formPanel.add(new JLabel("Delivery Status:"));
        formPanel.add(status);
        formPanel.add(new JLabel(""));
        formPanel.add(addButton);

        add(formPanel, BorderLayout.CENTER);

        addButton.addActionListener(e -> {
            if (onOrderAdded != null) {
                onOrderAdded.run();
            }
        });
    }

    public String getOrderID() { return orderID.getText(); }
    public String getCustomer() { return customer.getText(); }
    public String getProduct() { return product.getText(); }
    public String getQuantity() { return quantity.getText(); }
    public String getPrice() { return price.getText(); }
    public String getOrderDate() { return orderDate.getText(); }
    public String getStatus() { return status.getSelectedItem().toString(); }

    public void setOrderID(String text) { orderID.setText(text); }
    public void setCustomer(String text) { customer.setText(text); }
    public void setProduct(String text) { product.setText(text); }
    public void setQuantity(String text) { quantity.setText(text); }
    public void setPrice(String text) { price.setText(text); }
    public void setOrderDate(String text) { orderDate.setText(text); }
    public void setStatus(String text) { status.setSelectedItem(text); }
}

/* SETTINGS PANEL */
class SettingsPanel extends JPanel {
    private Runnable onBackup;
    private Runnable onRestore;
    private Runnable onDeleteAll;
    private Runnable onRefresh;

    public SettingsPanel(Runnable onBackup, Runnable onRestore, Runnable onDeleteAll, Runnable onRefresh) {
        this.onBackup = onBackup;
        this.onRestore = onRestore;
        this.onDeleteAll = onDeleteAll;
        this.onRefresh = onRefresh;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new GridLayout(4, 1, 10, 10));

        JButton backupBtn = new JButton("BACKUP DATA");
        backupBtn.setBackground(new Color(200, 120, 40));
        backupBtn.setForeground(Color.WHITE);
        backupBtn.setFont(new Font("Arial", Font.BOLD, 12));

        JButton restoreBtn = new JButton("RESTORE DATA");
        restoreBtn.setBackground(new Color(100, 100, 200));
        restoreBtn.setForeground(Color.WHITE);
        restoreBtn.setFont(new Font("Arial", Font.BOLD, 12));

        JButton deleteAllBtn = new JButton("DELETE ALL DATA");
        deleteAllBtn.setBackground(new Color(200, 50, 50));
        deleteAllBtn.setForeground(Color.WHITE);
        deleteAllBtn.setFont(new Font("Arial", Font.BOLD, 12));

        JButton refreshBtn = new JButton("REFRESH TABLE");
        refreshBtn.setBackground(new Color(80, 80, 80));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFont(new Font("Arial", Font.BOLD, 12));

        buttonsPanel.add(backupBtn);
        buttonsPanel.add(restoreBtn);
        buttonsPanel.add(deleteAllBtn);
        buttonsPanel.add(refreshBtn);

        // Info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setBorder(BorderFactory.createTitledBorder("Information"));
        JLabel infoLabel = new JLabel("<html>Data is stored in:<br>sales.txt in the application folder</html>");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        infoPanel.add(infoLabel);

        add(buttonsPanel, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.SOUTH);

        backupBtn.addActionListener(e -> {
            if (onBackup != null) onBackup.run();
        });

        restoreBtn.addActionListener(e -> {
            if (onRestore != null) onRestore.run();
        });

        deleteAllBtn.addActionListener(e -> {
            if (onDeleteAll != null) onDeleteAll.run();
        });

        refreshBtn.addActionListener(e -> {
            if (onRefresh != null) onRefresh.run();
        });
    }
}

/* DASHBOARD - FIXED INITIALIZATION ORDER */
class Dashboard extends JFrame {

    private AddOrderPanel addOrderPanel;
    private SearchOrderPanel searchPanel;
    private OrdersListPanel ordersListPanel;
    private SettingsPanel settingsPanel;
    private JTabbedPane tabbedPane;

    Dashboard() {

        setTitle("ERP Sales and Delivery Module");
        setSize(1200, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        /* TITLE */
        JLabel title = new JLabel("SALES AND DELIVERY ERP SYSTEM", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setOpaque(true);
        title.setBackground(new Color(40, 70, 120));
        title.setForeground(Color.WHITE);
        title.setPreferredSize(new Dimension(0, 50));
        add(title, BorderLayout.NORTH);

        /* MAIN CONTENT PANEL */
        JPanel mainContent = new JPanel(new BorderLayout(10, 10));
        mainContent.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        /* LEFT PANEL - Will be populated after creating components */
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setPreferredSize(new Dimension(380, 0));

        /* CENTER - ORDERS LIST PANEL (Create this first since it's needed by search panel) */
        ordersListPanel = new OrdersListPanel();
        ordersListPanel.setPreferredSize(new Dimension(550, 0));

        /* RIGHT PANEL - EMPTY */
        JPanel rightPanel = new JPanel();
        rightPanel.setPreferredSize(new Dimension(20, 0));

        /* Create other panels now that ordersListPanel exists */
        // Create search panel with the table reference
        searchPanel = new SearchOrderPanel(ordersListPanel.getTable(), this::loadSelectedRowToForm);

        // Create add order panel
        addOrderPanel = new AddOrderPanel(this::addRecord);

        // Create settings panel
        settingsPanel = new SettingsPanel(
                this::backupData,
                this::restoreFromPC,
                this::deleteAllData,
                () -> ordersListPanel.loadData()
        );

        // Create tabbed pane and add tabs
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 12));
        tabbedPane.addTab("ADD ORDER", addOrderPanel);
        tabbedPane.addTab("SETTINGS", settingsPanel);

        // Add search panel at the top, then tabs below
        leftPanel.add(searchPanel, BorderLayout.NORTH);
        leftPanel.add(tabbedPane, BorderLayout.CENTER);

        /* BOTTOM PANEL - UPDATE/DELETE */
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBorder(BorderFactory.createTitledBorder("ACTIONS"));

        JButton updateBtn = new JButton("UPDATE SELECTED");
        updateBtn.setBackground(new Color(255, 165, 0));
        updateBtn.setForeground(Color.WHITE);
        updateBtn.setFont(new Font("Arial", Font.BOLD, 12));

        JButton deleteBtn = new JButton("DELETE SELECTED");
        deleteBtn.setBackground(new Color(200, 0, 0));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setFont(new Font("Arial", Font.BOLD, 12));

        bottomPanel.add(updateBtn);
        bottomPanel.add(deleteBtn);

        /* Assemble main content */
        mainContent.add(leftPanel, BorderLayout.WEST);
        mainContent.add(ordersListPanel, BorderLayout.CENTER);
        mainContent.add(rightPanel, BorderLayout.EAST);

        add(mainContent, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        /* BUTTON EVENTS */
        updateBtn.addActionListener(e -> updateRecord());
        deleteBtn.addActionListener(e -> deleteRecord());

        // Add table selection listener
        ordersListPanel.getTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && ordersListPanel.getTable().getSelectedRow() != -1) {
                loadSelectedRowToForm();
            }
        });

        // Load initial data
        ordersListPanel.loadData();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    /* LOAD SELECTED ROW TO FORM */
    void loadSelectedRowToForm() {
        JTable table = ordersListPanel.getTable();
        int row = table.getSelectedRow();
        if (row != -1) {
            addOrderPanel.setOrderID(table.getValueAt(row, 0).toString());
            addOrderPanel.setCustomer(table.getValueAt(row, 1).toString());
            addOrderPanel.setProduct(table.getValueAt(row, 2).toString());
            addOrderPanel.setQuantity(table.getValueAt(row, 3).toString());
            addOrderPanel.setPrice(table.getValueAt(row, 4).toString());
            addOrderPanel.setOrderDate(table.getValueAt(row, 6).toString()); // Date is now at index 6
            addOrderPanel.setStatus(table.getValueAt(row, 7).toString()); // Status is now at index 7

            // Switch to Add Order tab when loading a record
            tabbedPane.setSelectedIndex(0);
        }
    }

    /* ADD RECORD */
    void addRecord() {
        if (addOrderPanel.getOrderID().isEmpty() || addOrderPanel.getCustomer().isEmpty() ||
                addOrderPanel.getProduct().isEmpty() || addOrderPanel.getQuantity().isEmpty() ||
                addOrderPanel.getPrice().isEmpty() || addOrderPanel.getOrderDate().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields");
            return;
        }

        try {
            int q = Integer.parseInt(addOrderPanel.getQuantity());
            double p = Double.parseDouble(addOrderPanel.getPrice());
            double total = q * p;

            // Validate date format (basic check)
            String date = addOrderPanel.getOrderDate();
            if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                JOptionPane.showMessageDialog(this, "Please enter date in YYYY-MM-DD format");
                return;
            }

            // Check if order ID already exists
            if (ordersListPanel.isOrderIdExists(addOrderPanel.getOrderID())) {
                JOptionPane.showMessageDialog(this, "Order ID already exists!");
                return;
            }

            FileWriter fw = new FileWriter("sales.txt", true);

            fw.write(addOrderPanel.getOrderID() + "," +
                    addOrderPanel.getCustomer() + "," +
                    addOrderPanel.getProduct() + "," +
                    q + "," +
                    p + "," +
                    total + "," +
                    addOrderPanel.getOrderDate() + "," + // Add date before status
                    addOrderPanel.getStatus() + "\n");

            fw.close();

            JOptionPane.showMessageDialog(this, "Record Added Successfully!");
            ordersListPanel.loadData();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Input - Please enter valid numbers");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding record: " + e.getMessage());
        }
    }

    /* UPDATE RECORD */
    void updateRecord() {
        JTable table = ordersListPanel.getTable();
        if (table.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, "Please select a record to update");
            return;
        }

        String id = addOrderPanel.getOrderID();

        try {
            File file = new File("sales.txt");
            List<String> lines = new ArrayList<>();

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            boolean found = false;

            while ((line = br.readLine()) != null) {
                String data[] = line.split(",");

                if (data[0].equals(id)) {
                    found = true;
                    int q = Integer.parseInt(addOrderPanel.getQuantity());
                    double p = Double.parseDouble(addOrderPanel.getPrice());
                    double total = q * p;

                    line = addOrderPanel.getOrderID() + "," +
                            addOrderPanel.getCustomer() + "," +
                            addOrderPanel.getProduct() + "," +
                            q + "," +
                            p + "," +
                            total + "," +
                            addOrderPanel.getOrderDate() + "," +
                            addOrderPanel.getStatus();
                }
                lines.add(line);
            }
            br.close();

            if (!found) {
                JOptionPane.showMessageDialog(this, "Record not found!");
                return;
            }

            FileWriter fw = new FileWriter(file, false);
            for (String l : lines) {
                if (!l.trim().isEmpty()) {
                    fw.write(l + "\n");
                }
            }
            fw.close();

            JOptionPane.showMessageDialog(this, "Record Updated Successfully!");
            ordersListPanel.loadData();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error Updating: " + e.getMessage());
        }
    }

    /* DELETE RECORD */
    void deleteRecord() {
        JTable table = ordersListPanel.getTable();
        if (table.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, "Please select a record to delete");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this record?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        String id = addOrderPanel.getOrderID();

        try {
            File file = new File("sales.txt");
            List<String> lines = new ArrayList<>();

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                String data[] = line.split(",");
                if (!data[0].equals(id)) {
                    lines.add(line);
                }
            }
            br.close();

            FileWriter fw = new FileWriter(file, false);
            for (String l : lines) {
                if (!l.trim().isEmpty()) {
                    fw.write(l + "\n");
                }
            }
            fw.close();

            JOptionPane.showMessageDialog(this, "Record Deleted Successfully!");
            ordersListPanel.loadData();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error Deleting: " + e.getMessage());
        }
    }

    /* BACKUP TO PC */
    void backupData() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Backup Location");
        fileChooser.setSelectedFile(new File("sales_backup.txt"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File sourceFile = new File("sales.txt");
            File destFile = fileChooser.getSelectedFile();

            try {
                if (!sourceFile.exists()) {
                    sourceFile.createNewFile();
                }

                Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                JOptionPane.showMessageDialog(this,
                        "Backup Successful!\nSaved to: " + destFile.getAbsolutePath());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error backing up: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /* RESTORE FROM PC */
    void restoreFromPC() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Backup File to Restore");

        int userSelection = fileChooser.showOpenDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File sourceFile = fileChooser.getSelectedFile();
            File destFile = new File("sales.txt");

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Restoring will overwrite current data. Continue?",
                    "Confirm Restore", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    Files.copy(sourceFile.toPath(), destFile.toPath(),
                            StandardCopyOption.REPLACE_EXISTING);
                    ordersListPanel.loadData();
                    JOptionPane.showMessageDialog(this,
                            "Restore Successful!\nData loaded from: " + sourceFile.getAbsolutePath());
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this,
                            "Error restoring: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    /* DELETE ALL DATA */
    void deleteAllData() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "⚠ WARNING: This will delete ALL records permanently!\nAre you absolutely sure?",
                "Confirm Delete All", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {

            // Second confirmation
            int confirm2 = JOptionPane.showConfirmDialog(this,
                    "FINAL WARNING: This action cannot be undone!\nDelete all data?",
                    "Final Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);

            if (confirm2 == JOptionPane.YES_OPTION) {
                try {
                    File file = new File("sales.txt");
                    FileWriter fw = new FileWriter(file, false);
                    fw.write(""); // Clear file
                    fw.close();

                    ordersListPanel.loadData();

                    JOptionPane.showMessageDialog(this,
                            "All data has been deleted successfully!");

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this,
                            "Error deleting data: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}