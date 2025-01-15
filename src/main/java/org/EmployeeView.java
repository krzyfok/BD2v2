package org;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class EmployeeView implements ShopViewInterface {
    private JFrame frame;

    private JButton newProductButton;
    private JButton editProductButton;
    private JButton removeProductButton;
    private JButton viewStorageButton;



    private EmployeePresenter presenter;

    public EmployeeView() {
        frame = new JFrame("Sklep");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Przycisk dodawania produktu do koszyka
        newProductButton = new JButton("Dodaj nowy produkt");
        newProductButton.addActionListener(e->showAddProductDialog());

        // Przyciski do zgłoszeń serwisowych i wyświetlania zamówień
        editProductButton = new JButton("Edytuj produkt");
        editProductButton.addActionListener(e->presenter.chooseeEitedProduct());
        removeProductButton = new JButton("Usuń produkt");
        viewStorageButton = new JButton("Wyświetl stan magazynu");
        viewStorageButton.addActionListener(e->presenter.viewStorage());

        // Układ przycisków
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 2, 0, 0));

        buttonPanel.add(newProductButton);
        buttonPanel.add(editProductButton);
        buttonPanel.add(removeProductButton);
        buttonPanel.add(viewStorageButton);

        frame.add(buttonPanel, BorderLayout.CENTER);

        frame.setVisible(true);
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

    public void openEditProductDialog(int productId, String name, double price) {
        // Tworzenie nowego okna dialogowego
        JDialog editDialog = new JDialog((Frame) null, "Edytuj produkt", true);
        editDialog.setSize(400, 300);
        editDialog.setLayout(new GridLayout(4, 2, 10, 10));

        // Pola edycji
        JLabel nameLabel = new JLabel("Nazwa:");
        JTextField nameField = new JTextField(name);

        JLabel priceLabel = new JLabel("Cena:");
        JTextField priceField = new JTextField(String.valueOf(price));

        JButton saveButton = new JButton("Zapisz");
        JButton cancelButton = new JButton("Anuluj");

        // Dodawanie komponentów
        editDialog.add(nameLabel);
        editDialog.add(nameField);
        editDialog.add(priceLabel);
        editDialog.add(priceField);
        editDialog.add(saveButton);
        editDialog.add(cancelButton);

        // Obsługa przycisku Zapisz
        saveButton.addActionListener(e ->
        {
            String updatedName = nameField.getText().trim();
            double updatedPrice = Double.parseDouble(priceField.getText().trim());
            presenter.editProduct(productId, updatedName,updatedPrice);
            editDialog.dispose();});

        // Obsługa przycisku Anuluj
        cancelButton.addActionListener(e -> editDialog.dispose());

        // Wyświetlenie okna dialogowego
        editDialog.setLocationRelativeTo(null); // Wyśrodkuj
        editDialog.setVisible(true);
    }

    @Override
    public void updateProductList(List<String> products) {
    }

    @Override
    public void showProductDetails(String productDetails) {
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
