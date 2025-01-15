package org;

import javax.swing.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EmployeePresenter {
    private EmployeeVIewInterface view;
    private DatabaseConnector databaseConnector;
    private String paymentMethod;
    private boolean installments;
    private List<String> cart;
    public EmployeePresenter(EmployeeVIewInterface view, DatabaseConnector databaseConnector) {
        this.view = view;
        this.cart = new ArrayList<>();
        this.databaseConnector = databaseConnector;

    }


    public void addToCart(String selectedProduct) {
        if (selectedProduct != null) {
            cart.add(selectedProduct);
            view.showMessage(selectedProduct + " dodano do koszyka.");
        } else {
            view.showMessage("Proszę wybrać produkt.");
        }
    }

    // Wyświetlanie zawartości koszyka
    public void viewCart() {
        if (cart.isEmpty()) {
            view.showMessage("Koszyk jest pusty.");
        } else {
            StringBuilder cartContent = new StringBuilder("Produkty w koszyku:\n");
            for (String product : cart) {
                cartContent.append(product).append("\n");
            }
            view.showMessage(cartContent.toString());
        }
    }
    // Ładowanie produktów z bazy danych





    public void editProduct(int productId, String name, double price, int quantity)
    {

            try {
                databaseConnector.editProduct(productId, name, price, quantity);
            }
            catch (SQLException e) {
                view.showMessage("Błąd podczas edycji produktów: " + e.getMessage());
            }
    }



    public void chooseeEitedProduct() {
        try {
            int clientId = UserSession.getLoggedInUserId();

            List<String> clientProducts = databaseConnector.getProductsAllInfo();
            if (clientProducts.isEmpty()) {
                view.showMessage("Brak produktów w magazynie.");
                return;
            }

            String selectedProduct = (String) JOptionPane.showInputDialog(
                    null,
                    "Wybierz sprzęt do edycji:",
                    "Wybór sprzętu",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    clientProducts.toArray(),
                    clientProducts.get(0)
            );

            if (selectedProduct == null || selectedProduct.trim().isEmpty()) {
                view.showMessage("Nie wybrano sprzętu.");
                return;
            }

            String[] productDetails = selectedProduct.split("  ");
            int productId = Integer.parseInt(productDetails[0]);
            String name = productDetails[1];
            double price = Double.parseDouble(productDetails[2]);
            int quantity = Integer.parseInt(productDetails[4]);

            view.openEditProductDialog(productId,name,price,quantity);
            view.showMessage("Produkt zmieniono.");
        } catch (SQLException e) {
            view.showMessage("Błąd podczas edycji: " + e.getMessage());
        }
    }

    // Wyświetlanie zamówień klienta
    public void viewStorage() {
        try {
            List<String> products = databaseConnector.getProductsStorage();
            if (products.isEmpty()) {
                view.showMessage("Brak Produktów.");
            } else {
                view.showScrollableMessage("Produkty", products);
            }
        } catch (SQLException e) {
            view.showMessage("Błąd podczas pobierania produktów: " + e.getMessage());
        }
    }
    public void addProduct(String name, double price, int quantity) {
        try {
            boolean success = databaseConnector.addProductToDatabase(name, price, quantity);
            if (success) {
                view.showMessage("Produkt został pomyślnie dodany.");
            } else {
                view.showMessage("Nie udało się dodać produktu. Spróbuj ponownie.");
            }
        } catch (SQLException e) {
            view.showMessage("Błąd podczas dodawania produktu: " + e.getMessage());
        }
    }


    // Wyświetlanie zgłoszeń serwisowych klienta
    public void viewServiceRequests() {
        try {
            int clientId = UserSession.getLoggedInUserId();
            List<String> requests = databaseConnector.getClientServiceRequests(clientId);
            if (requests.isEmpty()) {
                view.showMessage("Brak zgłoszeń serwisowych.");
            } else {
                view.showMessage("Zgłoszenia serwisowe:\n" + String.join("\n", requests));
            }
        } catch (SQLException e) {
            view.showMessage("Błąd podczas pobierania zgłoszeń: " + e.getMessage());
        }
    }
    public void handlePayment(String paymentMethod, boolean installments) {
        this.paymentMethod = paymentMethod;
        this.installments = installments;
    }
    public boolean orderCart() {
        if (cart.isEmpty()) {
            view.showMessage("Koszyk jest pusty. Nie można złożyć zamówienia.");
            return false;
        }

        try {
            // Pobieramy ID klienta
            int clientId = UserSession.getLoggedInUserId();  // Zakładam, że masz metodę, która zwróci ID klienta
            // Składamy zamówienie
            this.databaseConnector.placeOrder(clientId, cart,  installments);  // Zmienione, by przekazać metodę płatności i raty
            cart.clear();  // Opróżniamy koszyk po złożeniu zamówienia
            view.showMessage("Zamówienie zostało złożone pomyślnie.");
        } catch (SQLException e) {
            view.showMessage("Wystąpił błąd przy składaniu zamówienia: " + e.getMessage());
        }
        return true;
    }
    public void clearCart() {
        cart.clear();  // Zakładając, że masz listę `cart` przechowującą produkty w koszyku
        view.showMessage("Koszyk został wyczyszczony.");  // Powiadomienie użytkownika
    }

    public double calculateTotalPrice()  {
        double totalPrice = 0;
        for (String item : cart) {
            String[] parts = item.split(" "); // Dzielimy po ciągu " - "

            int productId =    Integer.parseInt(parts[0]);
            try {
                totalPrice += databaseConnector.getCena(productId);
            }
            catch (SQLException e)
            {
                System.err.println("Wystąpił błąd przy obliczaniu ceny: " + e.getMessage());
            }
        }
        return totalPrice;
    }
}
