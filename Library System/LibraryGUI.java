import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * The Swing window. It only collects input, validates its format, and calls Library.
 * All business rules live in the model classes, not here.
 */
public class LibraryGUI extends JFrame {

    private final Library library;
    private final JLabel statusLabel = new JLabel(" ");

    // Catalog tab
    private final JComboBox<String> itemTypeBox = new JComboBox<>(new String[]{"Book", "Magazine", "DVD"});
    private final JTextField titleField = new JTextField(18);
    private final JTextField creatorField = new JTextField(18);
    private final JTextField yearField = new JTextField(18);
    private final JTextField extraField = new JTextField(18);
    private final JLabel creatorLabel = new JLabel("Author:");
    private final JLabel extraLabel = new JLabel("Pages:");
    private final JLabel itemRulesLabel = new JLabel(" ");
    private final JTextField searchField = new JTextField(20);
    private final DefaultTableModel itemModel =
            readOnlyModel("ID", "Type", "Title", "Creator", "Year", "Details", "Status");
    private final JTable itemTable = new JTable(itemModel);

    // Members tab
    private final JComboBox<String> memberTypeBox = new JComboBox<>(new String[]{"Student", "Teacher", "Guest"});
    private final JTextField memberNameField = new JTextField(18);
    private final JTextField memberEmailField = new JTextField(18);
    private final JLabel memberRulesLabel = new JLabel(" ");
    private final DefaultTableModel memberModel =
            readOnlyModel("ID", "Type", "Name", "Email", "Limit", "Fine rate", "On loan");
    private final JTable memberTable = new JTable(memberModel);

    // Circulation tab
    private final JComboBox<LibraryItem> availableBox = new JComboBox<>();
    private final JComboBox<Member> borrowerBox = new JComboBox<>();
    private final JComboBox<LibraryItem> loanedBox = new JComboBox<>();
    private final JTextField borrowDateField = new JTextField(LocalDate.now().toString(), 12);
    private final JTextField returnDateField = new JTextField(LocalDate.now().toString(), 12);
    private final JTextArea receiptArea = new JTextArea(9, 40);

    // Ledger tab
    private final DefaultTableModel txModel =
            readOnlyModel("ID", "Date", "Action", "Item", "Member", "Days late", "Fine (PHP)");
    private final JTable txTable = new JTable(txModel);
    private final JTextArea reportArea = new JTextArea();

    public LibraryGUI(Library library) {
        super("Aklatan Library System");
        this.library = library;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 660);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Catalog", buildCatalogTab());
        tabs.addTab("Members", buildMembersTab());
        tabs.addTab("Circulation", buildCirculationTab());
        tabs.addTab("Ledger & Report", buildLedgerTab());
        add(tabs, BorderLayout.CENTER);

        statusLabel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        add(statusLabel, BorderLayout.SOUTH);

        updateItemForm();
        updateMemberForm();
        refreshAll();
    }

    // =====================================================================
    // Tab builders
    // =====================================================================

    private JPanel buildCatalogTab() {
        JPanel form = formPanel("Add an item");
        addRow(form, 0, new JLabel("Type:"), itemTypeBox);
        addWide(form, 1, itemRulesLabel);
        addRow(form, 2, new JLabel("Title:"), titleField);
        addRow(form, 3, creatorLabel, creatorField);
        addRow(form, 4, new JLabel("Year:"), yearField);
        addRow(form, 5, extraLabel, extraField);
        JButton addButton = new JButton("Add item");
        addWide(form, 6, addButton);
        JButton removeButton = new JButton("Remove selected item");
        addWide(form, 7, removeButton);

        itemTypeBox.addActionListener(e -> updateItemForm());
        addButton.addActionListener(e -> onAddItem());
        removeButton.addActionListener(e -> onRemoveItem());
        onChange(searchField, this::refreshItems);

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchBar.add(new JLabel("Search (title, creator, ID or type):"));
        searchBar.add(searchField);

        JPanel right = new JPanel(new BorderLayout());
        right.add(searchBar, BorderLayout.NORTH);
        right.add(new JScrollPane(itemTable), BorderLayout.CENTER);
        itemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        return splitTab(form, right);
    }

    private JPanel buildMembersTab() {
        JPanel form = formPanel("Register a member");
        addRow(form, 0, new JLabel("Type:"), memberTypeBox);
        addWide(form, 1, memberRulesLabel);
        addRow(form, 2, new JLabel("Name:"), memberNameField);
        addRow(form, 3, new JLabel("Email:"), memberEmailField);
        JButton registerButton = new JButton("Register member");
        addWide(form, 4, registerButton);

        memberTypeBox.addActionListener(e -> updateMemberForm());
        registerButton.addActionListener(e -> onRegisterMember());

        return splitTab(form, new JScrollPane(memberTable));
    }

    private JPanel buildCirculationTab() {
        JPanel borrow = formPanel("Borrow an item");
        addRow(borrow, 0, new JLabel("Available item:"), availableBox);
        addRow(borrow, 1, new JLabel("Member:"), borrowerBox);
        addRow(borrow, 2, new JLabel("Date (yyyy-mm-dd):"), borrowDateField);
        JButton borrowButton = new JButton("Borrow");
        addWide(borrow, 3, borrowButton);
        borrowButton.addActionListener(e -> onBorrow());

        JPanel giveBack = formPanel("Return an item");
        addRow(giveBack, 0, new JLabel("Item on loan:"), loanedBox);
        addRow(giveBack, 1, new JLabel("Date (yyyy-mm-dd):"), returnDateField);
        JButton returnButton = new JButton("Return and calculate fine");
        addWide(giveBack, 2, returnButton);
        returnButton.addActionListener(e -> onReturn());

        JPanel top = new JPanel(new GridLayout(1, 2, 10, 0));
        top.add(borrow);
        top.add(giveBack);

        receiptArea.setEditable(false);
        receiptArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane receiptScroll = new JScrollPane(receiptArea);
        receiptScroll.setBorder(BorderFactory.createTitledBorder("Receipt"));

        JPanel tab = new JPanel(new BorderLayout(0, 10));
        tab.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        tab.add(top, BorderLayout.NORTH);
        tab.add(receiptScroll, BorderLayout.CENTER);
        return tab;
    }

    private JPanel buildLedgerTab() {
        reportArea.setEditable(false);
        reportArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane reportScroll = new JScrollPane(reportArea);
        reportScroll.setBorder(BorderFactory.createTitledBorder("Library report"));
        JScrollPane txScroll = new JScrollPane(txTable);
        txScroll.setBorder(BorderFactory.createTitledBorder("Transactions"));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, reportScroll, txScroll);
        split.setResizeWeight(0.4);

        JPanel tab = new JPanel(new BorderLayout());
        tab.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        tab.add(split, BorderLayout.CENTER);
        return tab;
    }

    // =====================================================================
    // Actions: each one validates input, calls Library, and reports the result
    // =====================================================================

    private void onAddItem() {
        try {
            String type = (String) itemTypeBox.getSelectedItem();
            int year = parseWholeNumber(yearField.getText(), "Year");
            int extra = parseWholeNumber(extraField.getText(), extraLabel.getText().replace(":", ""));
            LibraryItem item = library.addItem(type, titleField.getText(), creatorField.getText(), year, extra);
            titleField.setText("");
            creatorField.setText("");
            yearField.setText("");
            extraField.setText("");
            refreshAll();
            showInfo("Added " + item.getItemType() + " " + item + ".");
        } catch (LibraryException ex) {
            showError(ex.getMessage());
        }
    }

    private void onRemoveItem() {
        int row = itemTable.getSelectedRow();
        if (row < 0) {
            showError("Select an item in the table first.");
            return;
        }
        String id = (String) itemModel.getValueAt(row, 0);
        int answer = JOptionPane.showConfirmDialog(this, "Remove " + id + " from the catalog?",
                "Confirm removal", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            library.removeItem(id);
            refreshAll();
            showInfo("Removed " + id + ".");
        } catch (LibraryException ex) {
            showError(ex.getMessage());
        }
    }

    private void onRegisterMember() {
        try {
            Member m = library.registerMember((String) memberTypeBox.getSelectedItem(),
                    memberNameField.getText(), memberEmailField.getText());
            memberNameField.setText("");
            memberEmailField.setText("");
            refreshAll();
            showInfo("Registered " + m.getMemberType() + " " + m.getName() + " as " + m.getId() + ".");
        } catch (LibraryException ex) {
            showError(ex.getMessage());
        }
    }

    private void onBorrow() {
        try {
            LibraryItem item = (LibraryItem) availableBox.getSelectedItem();
            Member member = (Member) borrowerBox.getSelectedItem();
            if (item == null) {
                throw new LibraryException("No item is available to borrow.");
            }
            if (member == null) {
                throw new LibraryException("Register a member first.");
            }
            LocalDate date = parseDate(borrowDateField.getText());
            library.borrowItem(item.getId(), member.getId(), date);
            receiptArea.setText("BORROWED\n"
                    + "Item   : " + item.getItemType() + " " + item + "\n"
                    + "Member : " + member.getName() + " (" + member.getMemberType() + ")\n"
                    + "Date   : " + date + "\n"
                    + "Due    : " + item.getDueDate() + "  (" + item.getLoanDays() + "-day loan)\n");
            refreshAll();
            showInfo("Borrowed " + item + ".");
        } catch (LibraryException ex) {
            showError(ex.getMessage());
        }
    }

    private void onReturn() {
        try {
            LibraryItem item = (LibraryItem) loanedBox.getSelectedItem();
            if (item == null) {
                throw new LibraryException("Nothing is on loan.");
            }
            Member member = item.getBorrower();
            LocalDate due = item.getDueDate();
            LocalDate date = parseDate(returnDateField.getText());
            Transaction t = library.returnItem(item.getId(), date);
            StringBuilder sb = new StringBuilder("RETURNED\n");
            sb.append("Item      : ").append(item.getItemType()).append(' ').append(item).append('\n');
            sb.append("Member    : ").append(member.getName()).append(" (").append(member.getMemberType()).append(")\n");
            sb.append("Due       : ").append(due).append("   Returned: ").append(date).append('\n');
            sb.append("Days late : ").append(t.getDaysLate()).append('\n');
            sb.append(String.format("Fine      : %d x PHP %.2f x %.1f (%s rate) = PHP %.2f%n",
                    t.getDaysLate(), item.getFinePerDay(), member.getFineMultiplier(),
                    member.getMemberType(), t.getFine()));
            receiptArea.setText(sb.toString());
            refreshAll();
            showInfo("Returned " + item + (t.getFine() > 0 ? String.format(" - fine PHP %.2f.", t.getFine()) : "."));
        } catch (LibraryException ex) {
            showError(ex.getMessage());
        }
    }

    // =====================================================================
    // Refreshing the display
    // =====================================================================

    private void refreshAll() {
        refreshItems();
        refreshMembers();
        refreshCirculation();
        refreshLedger();
    }

    private void refreshItems() {
        itemModel.setRowCount(0);
        LocalDate today = LocalDate.now();
        for (LibraryItem item : library.searchItems(searchField.getText())) {
            String status = "Available";
            if (!item.isAvailable()) {
                status = "Due " + item.getDueDate() + " - " + item.getBorrower().getName();
                if (item.daysLate(today) > 0) {
                    status = "OVERDUE " + item.daysLate(today) + "d - " + item.getBorrower().getName();
                }
            }
            itemModel.addRow(new Object[]{item.getId(), item.getItemType(), item.getTitle(), item.getCreator(),
                    item.getYear(), item.getExtraInfo(), status});
        }
    }

    private void refreshMembers() {
        memberModel.setRowCount(0);
        for (Member m : library.getMembers()) {
            memberModel.addRow(new Object[]{m.getId(), m.getMemberType(), m.getName(), m.getEmail(),
                    m.getBorrowLimit(), "x" + m.getFineMultiplier(),
                    m.getBorrowedItems().size() + " / " + m.getBorrowLimit()});
        }
    }

    private void refreshCirculation() {
        availableBox.removeAllItems();
        loanedBox.removeAllItems();
        borrowerBox.removeAllItems();
        for (LibraryItem item : library.getItems()) {
            if (item.isAvailable()) {
                availableBox.addItem(item);
            } else {
                loanedBox.addItem(item);
            }
        }
        for (Member m : library.getMembers()) {
            borrowerBox.addItem(m);
        }
    }

    private void refreshLedger() {
        txModel.setRowCount(0);
        for (Transaction t : library.getTransactions()) {
            txModel.addRow(new Object[]{t.getId(), t.getDate(), t.getAction(),
                    t.getItemId() + " " + t.getItemTitle(), t.getMemberName(),
                    t.getDaysLate(), String.format("%.2f", t.getFine())});
        }
        reportArea.setText(library.generateReport(LocalDate.now()));
        reportArea.setCaretPosition(0);
    }

    /** POLYMORPHISM on screen: the same calls return different rules for each type. */
    private void updateItemForm() {
        String type = (String) itemTypeBox.getSelectedItem();
        LibraryItem sample = Library.sampleItem(type);
        creatorLabel.setText("Magazine".equals(type) ? "Publisher:" : "DVD".equals(type) ? "Director:" : "Author:");
        extraLabel.setText("Magazine".equals(type) ? "Issue no.:" : "DVD".equals(type) ? "Minutes:" : "Pages:");
        if (sample != null) {
            itemRulesLabel.setText(String.format("%d-day loan, PHP %.2f per day late%s", sample.getLoanDays(),
                    sample.getFinePerDay(), sample.calculateFine(1000) < 1000 * sample.getFinePerDay()
                            ? String.format(" (max PHP %.2f)", sample.calculateFine(1000)) : ""));
        }
    }

    private void updateMemberForm() {
        Member sample = Library.sampleMember((String) memberTypeBox.getSelectedItem());
        if (sample != null) {
            memberRulesLabel.setText(String.format("Borrows up to %d, fines x%.1f",
                    sample.getBorrowLimit(), sample.getFineMultiplier()));
        }
    }

    // =====================================================================
    // Small helpers
    // =====================================================================

    private int parseWholeNumber(String text, String fieldName) throws LibraryException {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException ex) {
            throw new LibraryException(fieldName + " must be a whole number.");
        }
    }

    private LocalDate parseDate(String text) throws LibraryException {
        try {
            return LocalDate.parse(text.trim());
        } catch (DateTimeParseException ex) {
            throw new LibraryException("Enter the date as yyyy-mm-dd, for example 2026-10-23.");
        }
    }

    private void showError(String message) {
        statusLabel.setText("Error: " + message);
        JOptionPane.showMessageDialog(this, message, "Cannot continue", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String message) {
        statusLabel.setText(message);
    }

    private static DefaultTableModel readOnlyModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private static JPanel formPanel(String title) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        return p;
    }

    private static void addRow(JPanel p, int row, Component label, Component field) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row;
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        p.add(label, c);
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        p.add(field, c);
    }

    private static void addWide(JPanel p, int row, Component comp) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row;
        c.gridx = 0;
        c.gridwidth = 2;
        c.insets = new Insets(4, 6, 4, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        p.add(comp, c);
    }

    private static JPanel splitTab(JPanel form, Component content) {
        JPanel left = new JPanel(new BorderLayout());
        left.add(form, BorderLayout.NORTH);
        JPanel tab = new JPanel(new BorderLayout(10, 0));
        tab.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        tab.add(left, BorderLayout.WEST);
        tab.add(content, BorderLayout.CENTER);
        return tab;
    }

    private static void onChange(JTextField field, Runnable action) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                action.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                action.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                action.run();
            }
        });
    }
}
