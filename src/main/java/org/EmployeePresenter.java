package org;

import javax.swing.*;
import java.sql.ResultSet;
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




    // PU10 Dodanie konta pracownika
    public void addEmployeeAccount(String imie, String nazwisko, String login, String haslo, int pensja) {
        try {
            databaseConnector.connect("pracownik");
            ResultSet existingAccount = databaseConnector.findAccount("pracownik", login);
            if (existingAccount.next()) {
                view.showMessage("Pracownik z loginem '" + login + "' już istnieje.");
            } else {
                boolean added = databaseConnector.addAccount("pracownik", imie, nazwisko, login, haslo, pensja);
                if (added) {
                    view.showMessage("Konto pracownika '" + login + "' zostało dodane.");
                } else {
                    view.showMessage("Nie udało się dodać konta pracownika.");
                }
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

    // PU12 & Preparation for PU13 - Pobranie danych pracownika do edycji
    public void prepareEmployeeForEdit(String loginToModify) {
        try {
            databaseConnector.connect("pracownik");
            ResultSet employeeData = databaseConnector.findAccount("pracownik", loginToModify);
            if (employeeData.next()) {
                String imie = employeeData.getString("imie");
                String nazwisko = employeeData.getString("nazwisko");
                String haslo = employeeData.getString("haslo");
                int pensja = employeeData.getInt("pensja");
                view.openEditEmployeeDialog(loginToModify, imie, nazwisko, haslo, pensja);
            } else {
                view.showMessage("Nie znaleziono pracownika z loginem '" + loginToModify + "'.");
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

    // Preparation for PU13 - Populate logins for modify employee dialog
    public void prepareEmployeeLoginsForModify() {
        try {
            databaseConnector.connect("pracownik");
            List<String> employeeLogins = databaseConnector.getEmployeeLogins();
            view.showModifyEmployeeDialog(employeeLogins);
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

    // Preparation for PU11 - Populate logins for delete employee dialog
    public void prepareEmployeeLoginsForDelete() {
        try {
            databaseConnector.connect("pracownik");
            List<String> employeeLogins = databaseConnector.getEmployeeLogins();
            view.showDeleteEmployeeDialog(employeeLogins);
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


    // PU13 Modyfikacja konta pracownika
    public void modifyEmployeeAccount(String login, String newImie, String newNazwisko, String newHaslo, int newPensja) {
        try {
            databaseConnector.connect("pracownik");
            boolean modified = databaseConnector.modifyAccount("pracownik", login, newImie, newNazwisko, newHaslo, newPensja);
            if (modified) {
                view.showMessage("Dane pracownika '" + login + "' zostały zaktualizowane.");
            } else {
                view.showMessage("Nie udało się zaktualizować danych pracownika '" + login + "'. Pracownik może nie istnieć.");
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

    // PU11 Usunięcie konta pracownika
    public void deleteEmployeeAccount(String loginToDelete) {
        try {
            databaseConnector.connect("pracownik");
            ResultSet existingAccount = databaseConnector.findAccount("pracownik", loginToDelete);
            if (!existingAccount.next()) {
                view.showMessage("Pracownik z loginem '" + loginToDelete + "' nie istnieje.");
            } else {
                boolean deleted = databaseConnector.deleteAccount("pracownik", loginToDelete);
                if (deleted) {
                    view.showMessage("Konto pracownika '" + loginToDelete + "' zostało usunięte.");
                } else {
                    view.showMessage("Nie udało się usunąć konta pracownika '" + loginToDelete + "'.");
                }
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


    // Client Account Management

    // PU10 Dodanie konta klienta
    public void addClientAccount(String imie, String nazwisko, String login, String haslo) {
        try {
            databaseConnector.connect("pracownik"); // Or maybe a separate "admin" role if needed, for now employee can manage clients
            ResultSet existingAccount = databaseConnector.findAccount("klient", login);
            if (existingAccount.next()) {
                view.showMessage("Klient z loginem '" + login + "' już istnieje.");
            } else {
                boolean added = databaseConnector.addAccount("klient", imie, nazwisko, login, haslo);
                if (added) {
                    view.showMessage("Konto klienta '" + login + "' zostało dodane.");
                } else {
                    view.showMessage("Nie udało się dodać konta klienta.");
                }
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

    // PU12 & Preparation for PU13 - Pobranie danych klienta do edycji
    public void prepareClientForEdit(String loginToModify) {
        try {
            databaseConnector.connect("pracownik"); // Or "admin" role
            ResultSet clientData = databaseConnector.findAccount("klient", loginToModify);
            if (clientData.next()) {
                String imie = clientData.getString("imie");
                String nazwisko = clientData.getString("nazwisko");
                String haslo = clientData.getString("haslo");
                view.openEditClientDialog(loginToModify, imie, nazwisko, haslo);
            } else {
                view.showMessage("Nie znaleziono klienta z loginem '" + loginToModify + "'.");
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

    // Preparation for PU13 - Populate logins for modify client dialog
    public void prepareClientLoginsForModify() {
        try {
            databaseConnector.connect("pracownik");
            List<String> clientLogins = databaseConnector.getClientLogins();
            view.showModifyClientDialog(clientLogins);
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

    // Preparation for PU11 - Populate logins for delete client dialog
    public void prepareClientLoginsForDelete() {
        try {
            databaseConnector.connect("pracownik");
            List<String> clientLogins = databaseConnector.getClientLogins();
            view.showDeleteClientDialog(clientLogins);
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


    // PU13 Modyfikacja konta klienta
    public void modifyClientAccount(String login, String newImie, String newNazwisko, String newHaslo) {
        try {
            databaseConnector.connect("pracownik"); // Or "admin" role
            boolean modified = databaseConnector.modifyAccount("klient", login, newImie, newNazwisko, newHaslo);
            if (modified) {
                view.showMessage("Dane klienta '" + login + "' zostały zaktualizowane.");
            } else {
                view.showMessage("Nie udało się zaktualizować danych klienta '" + login + "'. Klient może nie istnieć.");
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

    // PU11 Usunięcie konta klienta
    public void deleteClientAccount(String loginToDelete) {
        try {
            databaseConnector.connect("pracownik"); // Or "admin" role
            ResultSet existingAccount = databaseConnector.findAccount("klient", loginToDelete);
            if (!existingAccount.next()) {
                view.showMessage("Klient z loginem '" + loginToDelete + "' nie istnieje.");
            } else {
                boolean deleted = databaseConnector.deleteAccount("klient", loginToDelete);
                if (deleted) {
                    view.showMessage("Konto klienta '" + loginToDelete + "' zostało usunięte.");
                } else {
                    view.showMessage("Nie udało się usunąć konta klienta '" + loginToDelete + "'.");
                }
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
}
