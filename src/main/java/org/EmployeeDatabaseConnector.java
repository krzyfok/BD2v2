package org;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDatabaseConnector implements DatabaseConnectorInterface{
    private Connection connection;
    @Override
    public void connect() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/mydb";
        String username = "root";
        String password = "";

        // Nawiązanie połączenia z bazą danych
        this.connection = DriverManager.getConnection(url, username, password);
        System.out.println("Połączenie z bazą danych nawiązane dla roli pracowink");
    }

    @Override
    public void disconnect() throws SQLException {
        if (this.connection != null && !this.connection.isClosed()) {
            this.connection.close();
            System.out.println("Połączenie z bazą danych zostało zamknięte.");
        }
    }

    public void editProduct(int productId, String name, double cena, int quantity) throws SQLException
    {
        System.out.println(name);
        String updateSql = "UPDATE sprzet SET nazwa = ?, cena = ?, stan_magazynu = ? WHERE idsprzet = ?";
        try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
            updateStatement.setString(1, name);
            updateStatement.setDouble(2, cena);
            updateStatement.setInt(3,quantity);
            updateStatement.setInt(4, productId);


            updateStatement.executeUpdate();



        } catch (SQLException e) {
            // W przypadku błędu logowanie lub inne działania
            e.printStackTrace();
            throw new SQLException("Błąd przy składaniu zamówienia: " + e.getMessage());
        }



    }

    public List<String> getProductsAllInfo() throws SQLException {
        List<String> products = new ArrayList<>();
        String sql = "SELECT idsprzet, nazwa, cena, stan_magazynu FROM sprzet";  // Zapytanie SQL do pobrania produktów z nazwą i ceną
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                int id= resultSet.getInt("idsprzet");
                String productName = resultSet.getString("nazwa");
                double productPrice = resultSet.getDouble("cena");
                int quantity= resultSet.getInt("stan_magazynu");
                products.add(id+"  " +productName + "  " + productPrice + "  PLN  "+ quantity);  // Łączymy nazwę produktu z ceną
            }
        }
        return products;
    }


    public List<String> getProducts() throws SQLException {
        List<String> products = new ArrayList<>();
        String sql = "SELECT idsprzet, nazwa, cena FROM sprzet";  // Zapytanie SQL do pobrania produktów z nazwą i ceną
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                int id= resultSet.getInt("idsprzet");
                String productName = resultSet.getString("nazwa");
                double productPrice = resultSet.getDouble("cena");
                products.add(id+"  " +productName + "  " + productPrice + "  PLN");  // Łączymy nazwę produktu z ceną
            }
        }
        return products;
    }

    public void deleteProduct(int productId) throws SQLException {


        String deleteSql = "DELETE FROM sprzet WHERE idsprzet = ?";

        try (PreparedStatement deleteStatement = connection.prepareStatement(deleteSql)) {
            deleteStatement.setInt(1, productId);
            int affectedRows = deleteStatement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Nie znaleziono produktu o podanym ID.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Nie można usunąc produktu ");
        }
    }

    public List<String> getProductsStorage() throws SQLException {
        List<String> products = new ArrayList<>();
        String sql = "SELECT idsprzet, nazwa,  cena, stan_magazynu FROM sprzet";  // Zapytanie SQL do pobrania produktów z nazwą i ceną
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                int id= resultSet.getInt("idsprzet");
                String productName = resultSet.getString("nazwa");
                double productPrice = resultSet.getDouble("cena");
                int stan = resultSet.getInt("stan_magazynu");
                products.add("id: "+id+" nazwa: " +productName + " - " + productPrice + " PLN  Ilość: "+stan);  // Łączymy nazwę produktu z ceną
            }
        }
        return products;
    }
    public boolean addProductToDatabase(String name, double price, int quantity) throws SQLException {
        String query = "INSERT INTO sprzet (nazwa, cena, stan_magazynu) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, name);
            statement.setDouble(2, price);
            statement.setInt(3, quantity);
            return statement.executeUpdate() > 0;
        }
    }
    public ResultSet findAccount(String role, String login) throws SQLException {
        String tableName = "";
        if ("pracownik".equals(role)) {
            tableName = "pracownik";
        } else if ("klient".equals(role)) {
            tableName = "klient";
        } else {
            throw new IllegalArgumentException("Nieznana rola: " + role);
        }
        String sql = "SELECT * FROM " + tableName + " WHERE login = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, login);
        return preparedStatement.executeQuery();
    }

    // PU10 Dodanie konta do bazy (pracownik)
    public boolean addAccount(String role, String imie, String nazwisko, String login, String haslo, int pensja) {
        if (!"pracownik".equals(role)) {
            throw new IllegalArgumentException("Metoda addAccount dla pracownika wymaga roli 'pracownik'.");
        }
        String sql = "INSERT INTO pracownik (imie, nazwisko, pensja, login, haslo) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, imie);
            preparedStatement.setString(2, nazwisko);
            preparedStatement.setInt(3, pensja);
            preparedStatement.setString(4, login);
            preparedStatement.setString(5, haslo);
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // PU10 Dodanie konta do bazy (klient)
    public boolean addAccount(String role, String imie, String nazwisko, String login, String haslo) {
        if (!"klient".equals(role)) {
            throw new IllegalArgumentException("Metoda addAccount dla klienta wymaga roli 'klient'.");
        }
        String sql = "INSERT INTO klient (imie, nazwisko, login, haslo) VALUES (?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, imie);
            preparedStatement.setString(2, nazwisko);
            preparedStatement.setString(3, login);
            preparedStatement.setString(4, haslo);
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // PU11 Usunięcie konta z bazy
    public boolean deleteAccount(String role, String login) {
        String tableName = "";
        if ("pracownik".equals(role)) {
            tableName = "pracownik";
        } else if ("klient".equals(role)) {
            tableName = "klient";
        } else {
            throw new IllegalArgumentException("Nieznana rola: " + role);
        }
        String sql = "DELETE FROM " + tableName + " WHERE login = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, login);
            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0; // Returns true if at least one row was deleted
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // PU13 Modyfikacja atrybutów konta w bazie (pracownik)
    public boolean modifyAccount(String role, String login, String newImie, String newNazwisko, String newHaslo, int newPensja) {
        if (!"pracownik".equals(role)) {
            throw new IllegalArgumentException("Metoda modifyAccount dla pracownika wymaga roli 'pracownik'.");
        }
        String sql = "UPDATE pracownik SET imie = ?, nazwisko = ?, haslo = ?, pensja = ? WHERE login = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, newImie);
            preparedStatement.setString(2, newNazwisko);
            preparedStatement.setString(3, newHaslo);
            preparedStatement.setInt(4, newPensja);
            preparedStatement.setString(5, login);
            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // PU13 Modyfikacja atrybutów konta w bazie (klient)
    public boolean modifyAccount(String role, String login, String newImie, String newNazwisko, String newHaslo) {
        if (!"klient".equals(role)) {
            throw new IllegalArgumentException("Metoda modifyAccount dla klienta wymaga roli 'klient'.");
        }
        String sql = "UPDATE klient SET imie = ?, nazwisko = ?, haslo = ? WHERE login = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, newImie);
            preparedStatement.setString(2, newNazwisko);
            preparedStatement.setString(3, newHaslo);
            preparedStatement.setString(4, login);
            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Metoda do pobierania loginów pracowników
    public List<String> getEmployeeLogins() throws SQLException {
        List<String> logins = new ArrayList<>();
        String sql = "SELECT login FROM pracownik";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                logins.add(resultSet.getString("login"));
            }
        }
        return logins;
    }

    // Metoda do pobierania loginów klientów
    public List<String> getClientLogins() throws SQLException {
        List<String> logins = new ArrayList<>();
        String sql = "SELECT login FROM klient";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                logins.add(resultSet.getString("login"));
            }
        }
        return logins;
    }
}
