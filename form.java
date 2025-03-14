import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.awt.*;

public class form extends JFrame {
    private JPanel panel1;
    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField3;
    private JTextField textField4;
    private JTable table1;
    private JButton deleteRowButton;
    private JButton addContactButton;
    public static DefaultTableModel model;
    private ArrayList<String[]> clients;

    public form() {
        setSize(700, 400);
        setContentPane(panel1);
        setVisible(true);
        model = new DefaultTableModel();
        table1.setModel(model);
        clients = Connect.executeQuery("Select * from sakila.contacts");
        updateTable();

        deleteRowButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int selectedRow = table1.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(null, "Please select a row to delete.");
                    return;
                }
                int Id = Integer.parseInt(model.getValueAt(selectedRow, 0).toString()); // Get Actor ID
                int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this actor?",
                        "Confirm Deletion", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    Connect.deleteRow(Id);
                    refreshTable();
                }
            }
        });

        addContactButton.addActionListener(e -> {
            String name = textField1.getText().trim();
            String phone = textField2.getText().trim();
            String email = textField3.getText().trim();
            String address = textField4.getText().trim();

            if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || address.isEmpty()) {
                JOptionPane.showMessageDialog(null, "All fields are required!");
                return;
            }

            if (!phone.matches("\\d{7,}")) {
                JOptionPane.showMessageDialog(null, "Invalid phone number! Must contain only digits and be at least 7 digits long.");
                return;
            }

            if (!email.contains("@")) {
                JOptionPane.showMessageDialog(null, "Invalid email! Must contain '@'.");
                return;
            }

            // Call database function
            Connect.addContact(name, phone, email, address);

            // Refresh UI
            JOptionPane.showMessageDialog(null, "Contact Added!");
            textField1.setText("");
            textField2.setText("");
            textField3.setText("");
            textField4.setText("");

            // Reload the contact list
            showContacts(null);
        });

        table1.setDefaultEditor(Object.class, new DefaultCellEditor(new JTextField()) {
            private String oldValue; // Store the old value before editing
            @Override
            public Component getTableCellEditorComponent(JTable table1, Object value, boolean isSelected, int row, int column) {
                oldValue = (value != null) ? value.toString() : ""; // Capture the old value
                return super.getTableCellEditorComponent(table1, value, isSelected, row, column);
            }


            @Override
            public boolean stopCellEditing() {
                String newValue = getCellEditorValue().toString();
                int row = table1.getEditingRow();
                int column = table1.getEditingColumn();
                String columnName = table1.getColumnName(column);
                String Id = (String) table1.getValueAt(row, 0);
                if (!newValue.equals(oldValue)) {
                    String[] columns = {columnName};
                    String[] newValues = {newValue};
                    Connect.updateDatabase(Id, columns, newValues);
                }
                return super.stopCellEditing();
            }
        });}

        private void refreshTable() {
            clients = Connect.executeQuery("SELECT * FROM sakila.contacts");
            updateTable();
        }

    private void showContacts(ActionEvent e) {
        ArrayList<String[]> contacts = Connect.executeQuery("SELECT * FROM contacts");

        form.model.setRowCount(0);

        for (String[] contact : contacts) {
            form.model.addRow(contact);
        }
    }

    private void updateTable() {
            model.setRowCount(0);
            for (String[] actor : clients) {
                model.addRow(actor);
            }
        }
    }
