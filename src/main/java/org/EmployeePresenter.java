package org;

import javax.swing.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EmployeePresenter {
    private EmployeeVIewInterface view;
    private EmployeeDatabaseConnector databaseConnector;
    private String paymentMethod;
    private boolean installments;
    private List<String> cart;
    public EmployeePresenter(EmployeeVIewInterface view, EmployeeDatabaseConnector databaseConnector) {
        this.view = view;
        this.cart = new ArrayList<>();
        this.databaseConnector = databaseConnector;

    }


    public void editProduct(int productId, String name, double price, int quantity)
    {

        try {databaseConnector.connect();
            try {

                databaseConnector.editProduct(productId, name, price, quantity);
            } catch (SQLException e) {
                view.showMessage("Błąd podczas edycji produktów: " + e.getMessage());
            }
        }catch (SQLException e) {
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



    public void chooseeEitedProduct() {

        try {databaseConnector.connect();
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

                view.openEditProductDialog(productId, name, price, quantity);
                view.showMessage("Produkt zmieniono.");
            } catch (SQLException e) {
                view.showMessage("Błąd podczas edycji: " + e.getMessage());
            }
        }catch (SQLException e) {
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
    public void deleteProduct() {
        try { databaseConnector.connect();
            try {
                // Pobranie listy produktów
                List<String> clientProducts = databaseConnector.getProducts();
                if (clientProducts.isEmpty()) {
                    view.showMessage("Brak produktów w magazynie.");
                    return;
                }

                // Wybór produktu do usunięcia
                String selectedProduct = (String) JOptionPane.showInputDialog(
                        null,
                        "Wybierz sprzęt do usunięcia:",
                        "Usuń sprzęt",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        clientProducts.toArray(),
                        clientProducts.get(0)
                );

                if (selectedProduct == null || selectedProduct.trim().isEmpty()) {
                    view.showMessage("Nie wybrano sprzętu.");
                    return;
                }

                // Pobranie ID produktu
                String[] productDetails = selectedProduct.split("  ");
                int productId = Integer.parseInt(productDetails[0]);

                // Potwierdzenie usunięcia
                int confirm = JOptionPane.showConfirmDialog(
                        null,
                        "Czy na pewno chcesz usunąć produkt: " + productDetails[1] + "?",
                        "Potwierdzenie",
                        JOptionPane.YES_NO_OPTION
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    databaseConnector.deleteProduct(productId);
                    view.showMessage("Produkt został usunięty.");
                }

            } catch (SQLException e) {
                view.showMessage("Błąd podczas usuwania: " + e.getMessage());
            }
        }catch (SQLException e) {
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
    public void viewStorage() {

        try {databaseConnector.connect();
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
        }catch (SQLException e) {
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
    public void addProduct(String name, double price, int quantity) {
        try {databaseConnector.connect();
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
        }catch (SQLException e) {
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




    // PU10 Dodanie konta pracownika
    public void addEmployeeAccount(String imie, String nazwisko, String login, String haslo, int pensja) {
        try {
            databaseConnector.connect();
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
            databaseConnector.connect();
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
            databaseConnector.connect();
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
            databaseConnector.connect();
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
            databaseConnector.connect();
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
            databaseConnector.connect();
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
            databaseConnector.connect(); // Or maybe a separate "admin" role if needed, for now employee can manage clients
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
            databaseConnector.connect(); // Or "admin" role
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
            databaseConnector.connect();
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
            databaseConnector.connect();
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
            databaseConnector.connect(); // Or "admin" role
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
            databaseConnector.connect(); // Or "admin" role
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
