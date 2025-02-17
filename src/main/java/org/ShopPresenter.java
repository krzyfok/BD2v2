package org;

import javax.swing.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ShopPresenter {
    private ShopViewInterface view;
    private ShopDatabaseConnector databaseConnector;
    private String paymentMethod;
    private boolean installments;
    private List<String> cart;
    public ShopPresenter(ShopViewInterface view, ShopDatabaseConnector databaseConnector) {
        this.view = view;
        this.cart = new ArrayList<>();
        this.databaseConnector = databaseConnector;
        loadProducts();
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
    public void loadProducts() {

        try {databaseConnector.connect();
            try {
                List<String> products = databaseConnector.getProducts();
                view.updateProductList(products);
            } catch (SQLException e) {
                view.showMessage("Błąd podczas ładowania produktów: " + e.getMessage());
            }
        } catch (SQLException e) {
            view.showMessage("Błąd bazy danych: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                databaseConnector.disconnect();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Wyświetlanie szczegółów produktu
    public void showProductDetails(String selectedProduct) {
        if (selectedProduct != null) {
            view.showProductDetails( selectedProduct);
        }
    }




    // Tworzenie zgłoszenia serwisowego
    public void createServiceRequest() {
        try {databaseConnector.connect();
            try {
                int clientId = UserSession.getLoggedInUserId();

                List<String> clientProducts = databaseConnector.getClientProducts(clientId);
                if (clientProducts.isEmpty()) {
                    view.showMessage("Nie masz żadnego sprzętu zakupionego.");
                    return;
                }

                String selectedProduct = (String) JOptionPane.showInputDialog(
                        null,
                        "Wybierz sprzęt do zgłoszenia serwisowego:",
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

                String[] productDetails = selectedProduct.split(", ");
                int serialNumber = Integer.parseInt(productDetails[1].split(": ")[1]);
                int purchaseId = Integer.parseInt(productDetails[2].split(": ")[1]);
                int equipmentId = Integer.parseInt(productDetails[3].split(": ")[1]);
                int workerId = Integer.parseInt(productDetails[4].split(": ")[1]);

                // Tworzenie zgłoszenia serwisowego
                databaseConnector.addServiceRequest(workerId, clientId, serialNumber);
                view.showMessage("Zgłoszenie zostało utworzone.");
            } catch (SQLException e) {
                view.showMessage("Błąd podczas dodawania zgłoszenia: " + e.getMessage());
            }
        }
        catch (SQLException e) {
            view.showMessage("Błąd bazy danych: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                databaseConnector.disconnect();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Wyświetlanie zamówień klienta
    public void viewOrders() {
        try {
            databaseConnector.connect();
            try {
                int clientId = UserSession.getLoggedInUserId();

                // Pobranie listy zamówień klienta
                List<String> orders = databaseConnector.getClientOrders(clientId);

                // Pobranie sumy wartości zamówień
                double totalOrdersValue = databaseConnector.getTotalOrdersValue(clientId);

                if (orders.isEmpty()) {
                    view.showMessage("Brak zamówień.");
                } else {
                    // Dodanie sumy zamówień na dole listy
                    orders.add("\nŁączna wartość zamówień: " + String.format("%.2f PLN", totalOrdersValue));
                    view.showScrollableMessage("Zamówienia", orders);
                }

            } catch (SQLException e) {
                view.showMessage("Błąd podczas pobierania zamówień: " + e.getMessage());
            }
        } catch (SQLException e) {
            view.showMessage("Błąd bazy danych: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                databaseConnector.disconnect();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }


    // Wyświetlanie zgłoszeń serwisowych klienta
    public void viewServiceRequests() {
        try {
            databaseConnector.connect();
            try {
                int clientId = UserSession.getLoggedInUserId();
                List<String> requests = databaseConnector.getClientServiceRequests(clientId);
                if (requests.isEmpty()) {
                    view.showMessage("Brak zgłoszeń serwisowych.");
                } else {
                    view.showScrollableMessage("Zgłoszenia serwisowe", requests);
                }
            } catch (SQLException e) {
                view.showMessage("Błąd podczas pobierania zgłoszeń: " + e.getMessage());
            }
        }
        catch (SQLException e) {
            view.showMessage("Błąd bazy danych: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                databaseConnector.disconnect();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void handlePayment(String paymentMethod, boolean installments) {
        this.paymentMethod = paymentMethod;
        this.installments = installments;
    }
    public boolean orderCart() {

        try {

            databaseConnector.connect();
            if (cart.isEmpty()) {
                view.showMessage("Koszyk jest pusty. Nie można złożyć zamówienia.");
                return false;
            }

            try {
                // Pobieramy ID klienta
                int clientId = UserSession.getLoggedInUserId();  // Zakładam, że masz metodę, która zwróci ID klienta
                // Składamy zamówienie
                this.databaseConnector.placeOrder(clientId, cart, installments);  // Zmienione, by przekazać metodę płatności i raty
                cart.clear();  // Opróżniamy koszyk po złożeniu zamówienia
                view.showMessage("Zamówienie zostało złożone pomyślnie.");
            } catch (SQLException e) {
                view.showMessage("Wystąpił błąd przy składaniu zamówienia: " + e.getMessage());
            }
        }
        catch (SQLException e) {
            view.showMessage("Błąd bazy danych: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                databaseConnector.disconnect();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    return true;
    }
    public void clearCart() {
        cart.clear();  // Zakładając, że masz listę `cart` przechowującą produkty w koszyku
        view.showMessage("Koszyk został wyczyszczony.");  // Powiadomienie użytkownika
    }

    public double calculateTotalPrice()  {
        double totalPrice = 0;
        try { databaseConnector.connect();
            for (String item : cart) {
                String[] parts = item.split(" "); // Dzielimy po ciągu " - "

                int productId = Integer.parseInt(parts[0]);
                try {
                    totalPrice += databaseConnector.getCena(productId);
                } catch (SQLException e) {
                    System.err.println("Wystąpił błąd przy obliczaniu ceny: " + e.getMessage());
                }
            }
        }
        catch (SQLException e) {
            view.showMessage("Błąd bazy danych: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                databaseConnector.disconnect();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return totalPrice;
    }
}
