package org;

import javax.swing.*;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class EmployeeView implements EmployeeVIewInterface{
    private JFrame frame;

    private JButton newProductButton;
    private JButton editProductButton;
    private JButton removeProductButton;
    private JButton viewStorageButton;

    private JButton addEmployeeButton;
    private JButton modifyEmployeeButton;
    private JButton deleteEmployeeButton;


    private JButton addClientButton;
    private JButton modifyClientButton;
    private JButton deleteClientButton;

    private EmployeePresenter presenter;


    public EmployeeView() {
        frame = new JFrame("Sklep - Panel Pracownika");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Panel for product management buttons
        JPanel productButtonPanel = new JPanel();
        productButtonPanel.setLayout(new GridLayout(2, 2, 5, 5)); // Grid layout for product buttons

        // Przycisk dodawania produktu
        newProductButton = new JButton("Dodaj nowy produkt");
        newProductButton.addActionListener(e->showAddProductDialog());
        productButtonPanel.add(newProductButton);

        // Przycisk edycji produktu
        editProductButton = new JButton("Edytuj produkt");
        editProductButton.addActionListener(e->presenter.chooseeEitedProduct());
        productButtonPanel.add(editProductButton);

        removeProductButton = new JButton("Usuń produkt");
        productButtonPanel.add(removeProductButton);

        viewStorageButton = new JButton("Wyświetl stan magazynu");
        viewStorageButton.addActionListener(e->presenter.viewStorage());
        productButtonPanel.add(viewStorageButton);


        // Panel for employee account management buttons
        JPanel employeeAccountPanel = new JPanel();
        employeeAccountPanel.setLayout(new GridLayout(3, 1, 5, 5)); // Vertical layout for employee account buttons

        addEmployeeButton = new JButton("Dodaj Pracownika");
        addEmployeeButton.addActionListener(e -> showAddEmployeeDialog());
        employeeAccountPanel.add(addEmployeeButton);

        modifyEmployeeButton = new JButton("Modyfikuj Pracownika");
        modifyEmployeeButton.addActionListener(e -> presenter.prepareEmployeeLoginsForModify()); // Changed to prepareLogins method
        employeeAccountPanel.add(modifyEmployeeButton);

        deleteEmployeeButton = new JButton("Usuń Pracownika");
        deleteEmployeeButton.addActionListener(e -> presenter.prepareEmployeeLoginsForDelete()); // Changed to prepareLogins method
        employeeAccountPanel.add(deleteEmployeeButton);

        // Panel for client account management buttons
        JPanel clientAccountPanel = new JPanel();
        clientAccountPanel.setLayout(new GridLayout(3, 1, 5, 5)); // Vertical layout for client account buttons

        addClientButton = new JButton("Dodaj Klienta");
        addClientButton.addActionListener(e -> showAddClientDialog());
        clientAccountPanel.add(addClientButton);

        modifyClientButton = new JButton("Modyfikuj Klienta");
        modifyClientButton.addActionListener(e -> presenter.prepareClientLoginsForModify()); // Changed to prepareLogins method
        clientAccountPanel.add(modifyClientButton);

        deleteClientButton = new JButton("Usuń Klienta");
        deleteClientButton.addActionListener(e -> presenter.prepareClientLoginsForDelete()); // Changed to prepareLogins method
        clientAccountPanel.add(deleteClientButton);


        // Main button panel to hold all management panels
        JPanel mainButtonPanel = new JPanel(new BorderLayout());
        mainButtonPanel.add(productButtonPanel, BorderLayout.CENTER);
        mainButtonPanel.add(employeeAccountPanel, BorderLayout.EAST); // Employee buttons on the right
        mainButtonPanel.add(clientAccountPanel, BorderLayout.WEST); // Client buttons on the left


        frame.add(mainButtonPanel, BorderLayout.CENTER);

        frame.setVisible(true);
    }


    private void showAddEmployeeDialog() {
        JTextField imieField = new JTextField();
        JTextField nazwiskoField = new JTextField();
        JTextField loginField = new JTextField();
        JTextField hasloField = new JTextField();
        JTextField pensjaField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(5, 2));
        panel.add(new JLabel("Imię:"));
        panel.add(imieField);
        panel.add(new JLabel("Nazwisko:"));
        panel.add(nazwiskoField);
        panel.add(new JLabel("Login:"));
        panel.add(loginField);
        panel.add(new JLabel("Hasło:"));
        panel.add(hasloField);
        panel.add(new JLabel("Pensja:"));
        panel.add(pensjaField);

        int result = JOptionPane.showConfirmDialog(
                frame, panel, "Dodaj Pracownika", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String imie = imieField.getText().trim();
            String nazwisko = nazwiskoField.getText().trim();
            String login = loginField.getText().trim();
            String haslo = hasloField.getText().trim();
            String pensjaText = pensjaField.getText().trim();

            try {
                int pensja = Integer.parseInt(pensjaText);
                presenter.addEmployeeAccount(imie, nazwisko, login, haslo, pensja);
            } catch (NumberFormatException e) {
                showMessage("Niepoprawny format pensji.");
            }
        }
    }

    // Modified to accept list of logins and use JComboBox
    public void showModifyEmployeeDialog(List<String> employeeLogins) {
        JComboBox<String> loginComboBox = new JComboBox<>(employeeLogins.toArray(new String[0]));
        JPanel loginPanel = new JPanel(new GridLayout(1,2));
        loginPanel.add(new JLabel("Wybierz pracownika do modyfikacji:"));
        loginPanel.add(loginComboBox);

        int loginResult = JOptionPane.showConfirmDialog(frame, loginPanel, "Modyfikuj Pracownika - Wybierz Login", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (loginResult == JOptionPane.OK_OPTION) {
            String selectedLogin = (String) loginComboBox.getSelectedItem();
            if (selectedLogin != null) {
                presenter.prepareEmployeeForEdit(selectedLogin); // Presenter will handle fetching employee data and then call openEditEmployeeDialog
            } else {
                showMessage("Nie wybrano pracownika.");
            }
        }
    }


    public void openEditEmployeeDialog(String login, String imie, String nazwisko, String haslo, int pensja) {
        JDialog editDialog = new JDialog(frame, "Edytuj Pracownika", true);
        editDialog.setLayout(new GridLayout(6, 2, 10, 10));

        JTextField imieField = new JTextField(imie);
        JTextField nazwiskoField = new JTextField(nazwisko);
        JTextField loginField = new JTextField(login);
        loginField.setEditable(false); // Login should not be editable
        JTextField hasloField = new JTextField(haslo);
        JTextField pensjaField = new JTextField(String.valueOf(pensja));

        editDialog.add(new JLabel("Login (nieedytowalny):"));
        editDialog.add(loginField);
        editDialog.add(new JLabel("Imię:"));
        editDialog.add(imieField);
        editDialog.add(new JLabel("Nazwisko:"));
        editDialog.add(nazwiskoField);
        editDialog.add(new JLabel("Hasło:"));
        editDialog.add(hasloField);
        editDialog.add(new JLabel("Pensja:"));
        editDialog.add(pensjaField);

        JButton saveButton = new JButton("Zapisz zmiany");
        saveButton.addActionListener(e -> {
            try {
                int updatedPensja = Integer.parseInt(pensjaField.getText().trim());
                presenter.modifyEmployeeAccount(login, imieField.getText().trim(), nazwiskoField.getText().trim(), hasloField.getText().trim(), updatedPensja);
                editDialog.dispose();
            } catch (NumberFormatException ex) {
                showMessage("Niepoprawny format pensji.");
            }
        });
        editDialog.add(saveButton);

        JButton cancelButton = new JButton("Anuluj");
        cancelButton.addActionListener(e -> editDialog.dispose());
        editDialog.add(cancelButton);

        editDialog.pack();
        editDialog.setLocationRelativeTo(frame);
        editDialog.setVisible(true);
    }


    // Modified to accept list of logins and use JComboBox
    public void showDeleteEmployeeDialog(List<String> employeeLogins) {
        JComboBox<String> loginComboBox = new JComboBox<>(employeeLogins.toArray(new String[0]));
        JPanel panel = new JPanel(new GridLayout(1, 2));
        panel.add(new JLabel("Wybierz pracownika do usunięcia:"));
        panel.add(loginComboBox);

        int result = JOptionPane.showConfirmDialog(
                frame, panel, "Usuń Pracownika", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String loginToDelete = (String) loginComboBox.getSelectedItem();
            if (loginToDelete != null) {
                presenter.deleteEmployeeAccount(loginToDelete);
            } else {
                showMessage("Nie wybrano pracownika.");
            }
        }
    }


    private void showAddClientDialog() {
        JTextField imieField = new JTextField();
        JTextField nazwiskoField = new JTextField();
        JTextField loginField = new JTextField();
        JTextField hasloField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(4, 2));
        panel.add(new JLabel("Imię:"));
        panel.add(imieField);
        panel.add(new JLabel("Nazwisko:"));
        panel.add(nazwiskoField);
        panel.add(new JLabel("Login:"));
        panel.add(loginField);
        panel.add(new JLabel("Hasło:"));
        panel.add(hasloField);

        int result = JOptionPane.showConfirmDialog(
                frame, panel, "Dodaj Klienta", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String imie = imieField.getText().trim();
            String nazwisko = nazwiskoField.getText().trim();
            String login = loginField.getText().trim();
            String haslo = hasloField.getText().trim();

            presenter.addClientAccount(imie, nazwisko, login, haslo);
        }
    }

    // Modified to accept list of logins and use JComboBox
    public void showModifyClientDialog(List<String> clientLogins) {
        JComboBox<String> loginComboBox = new JComboBox<>(clientLogins.toArray(new String[0]));
        JPanel loginPanel = new JPanel(new GridLayout(1,2));
        loginPanel.add(new JLabel("Wybierz klienta do modyfikacji:"));
        loginPanel.add(loginComboBox);

        int loginResult = JOptionPane.showConfirmDialog(frame, loginPanel, "Modyfikuj Klienta - Wybierz Login", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (loginResult == JOptionPane.OK_OPTION) {
            String selectedLogin = (String) loginComboBox.getSelectedItem();
            if (selectedLogin != null) {
                presenter.prepareClientForEdit(selectedLogin); // Presenter will handle fetching client data and then call openEditClientDialog
            } else {
                showMessage("Nie wybrano klienta.");
            }
        }
    }


    public void openEditClientDialog(String login, String imie, String nazwisko, String haslo) {
        JDialog editDialog = new JDialog(frame, "Edytuj Klienta", true);
        editDialog.setLayout(new GridLayout(5, 2, 10, 10));

        JTextField imieField = new JTextField(imie);
        JTextField nazwiskoField = new JTextField(nazwisko);
        JTextField loginField = new JTextField(login);
        loginField.setEditable(false); // Login should not be editable
        JTextField hasloField = new JTextField(haslo);

        editDialog.add(new JLabel("Login (nieedytowalny):"));
        editDialog.add(loginField);
        editDialog.add(new JLabel("Imię:"));
        editDialog.add(imieField);
        editDialog.add(new JLabel("Nazwisko:"));
        editDialog.add(nazwiskoField);
        editDialog.add(new JLabel("Hasło:"));
        editDialog.add(hasloField);


        JButton saveButton = new JButton("Zapisz zmiany");
        saveButton.addActionListener(e -> {
            presenter.modifyClientAccount(login, imieField.getText().trim(), nazwiskoField.getText().trim(), hasloField.getText().trim());
            editDialog.dispose();
        });
        editDialog.add(saveButton);

        JButton cancelButton = new JButton("Anuluj");
        cancelButton.addActionListener(e -> editDialog.dispose());
        editDialog.add(cancelButton);

        editDialog.pack();
        editDialog.setLocationRelativeTo(frame);
        editDialog.setVisible(true);
    }


    // Modified to accept list of logins and use JComboBox
    public void showDeleteClientDialog(List<String> clientLogins) {
        JComboBox<String> loginComboBox = new JComboBox<>(clientLogins.toArray(new String[0]));
        JPanel panel = new JPanel(new GridLayout(1, 2));
        panel.add(new JLabel("Wybierz klienta do usunięcia:"));
        panel.add(loginComboBox);

        int result = JOptionPane.showConfirmDialog(
                frame, panel, "Usuń Klienta", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String loginToDelete = (String) loginComboBox.getSelectedItem();
            if (loginToDelete != null) {
                presenter.deleteClientAccount(loginToDelete);
            } else {
                showMessage("Nie wybrano klienta.");
            }
        }
    }





    private void showAddProductDialog() {
        // Tworzenie pól tekstowych dla szczegółów produktu
        JTextField productNameField = new JTextField();
        JTextField productPriceField = new JTextField();
        JTextField productQuantityField = new JTextField();

        // Panel z etykietami i polami tekstowymi
        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("Nazwa produktu:"));
        panel.add(productNameField);
        panel.add(new JLabel("Cena:"));
        panel.add(productPriceField);
        panel.add(new JLabel("Ilość:"));
        panel.add(productQuantityField);

        // Wyświetlenie okna dialogowego
        int result = JOptionPane.showConfirmDialog(
                frame,
                panel,
                "Dodaj nowy produkt",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String productName = productNameField.getText().trim();
            String productPriceText = productPriceField.getText().trim();
            String productQuantityText = productQuantityField.getText().trim();

            try {
                double productPrice = Double.parseDouble(productPriceText);
                int productQuantity = Integer.parseInt(productQuantityText);

                // Przekazanie szczegółów do prezentera
                presenter.addProduct(productName, productPrice, productQuantity);

            } catch (NumberFormatException e) {
                showMessage("Niepoprawny format ceny lub ilości. Spróbuj ponownie.");
            }
        }
    }

    public void setPresenter(EmployeePresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void showScrollableMessage(String title, List<String> items) {
        // Tworzenie tekstu z listy
        String content = String.join("\n", items);

        // Tworzenie JTextArea
        JTextArea textArea = new JTextArea(content);
        textArea.setEditable(false); // Ustawienie pola jako tylko do odczytu
        textArea.setLineWrap(true); // Opcjonalne, jeśli chcesz zawijanie wierszy
        textArea.setWrapStyleWord(true); // Estetyczne zawijanie słów

        // Umieszczanie JTextArea w JScrollPane
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300)); // Rozmiar okna dialogowego

        // Wyświetlenie w oknie dialogowym
        JOptionPane.showMessageDialog(
                frame,
                scrollPane,
                title,
                JOptionPane.PLAIN_MESSAGE
        );
    }

    public void openEditProductDialog(int productId, String name, double price, int quantity) {
        // Tworzenie nowego okna dialogowego
        JDialog editDialog = new JDialog((Frame) null, "Edytuj produkt", true);
        editDialog.setSize(400, 300);
        editDialog.setLayout(new GridLayout(4, 2, 10, 10));

        // Pola edycji
        JLabel nameLabel = new JLabel("Nazwa:");
        JTextField nameField = new JTextField(name);

        JLabel priceLabel = new JLabel("Cena:");
        JTextField priceField = new JTextField(String.valueOf(price));
        JLabel quantityLabel = new JLabel("stan:");
        JTextField quantityField = new JTextField(String.valueOf(quantity));
        JButton saveButton = new JButton("Zapisz");
        JButton cancelButton = new JButton("Anuluj");

        // Dodawanie komponentów
        editDialog.add(nameLabel);
        editDialog.add(nameField);
        editDialog.add(priceLabel);
        editDialog.add(priceField);
        editDialog.add(quantityLabel);
        editDialog.add(quantityField);
        editDialog.add(saveButton);
        editDialog.add(cancelButton);

        // Obsługa przycisku Zapisz
        saveButton.addActionListener(e ->
        {
            String updatedName = nameField.getText().trim();
            double updatedPrice = Double.parseDouble(priceField.getText().trim());
            int updateQuantity = Integer.parseInt(quantityField.getText().trim());
            presenter.editProduct(productId, updatedName,updatedPrice,updateQuantity);
            editDialog.dispose();});

        // Obsługa przycisku Anuluj
        cancelButton.addActionListener(e -> editDialog.dispose());

        // Wyświetlenie okna dialogowego
        editDialog.setLocationRelativeTo(null); // Wyśrodkuj
        editDialog.setVisible(true);
    }


    @Override
    public void showMessage(String message) {
        JOptionPane.showMessageDialog(frame, message);
    }

    @Override
    public void closeWindow() {
        frame.dispose();
    }
}






