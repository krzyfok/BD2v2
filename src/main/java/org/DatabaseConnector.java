package org;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static java.time.LocalDate.now;

public class DatabaseConnector {
    private Connection connection;

    // Metoda łączenia z bazą danych dla konkretnej roli
    public void connect(String role) throws SQLException {
        String url = "jdbc:mysql://localhost:3306/mydb";
        String username = "root";
        String password = "";

        // Ustalanie poświadczeń na podstawie roli
        switch (role) {
            case "klient":
                username = "root";
                password = "";
                break;
            case "pracownik":
                username = "root";
                password = "";
                break;

            default:
                throw new SQLException("Nieznana rola: " + role);
        }

        // Nawiązanie połączenia z bazą danych
        this.connection = DriverManager.getConnection(url, username, password);
        System.out.println("Połączenie z bazą danych nawiązane dla roli: " + role);
    }

    // Metoda zamykania połączenia
    public void disconnect() throws SQLException {
        if (this.connection != null && !this.connection.isClosed()) {
            this.connection.close();
            System.out.println("Połączenie z bazą danych zostało zamknięte.");
        }
    }

    // Getter dla obiektu Connection
    public Connection getConnection() {
        return this.connection;
    }

    // Weryfikacja danych logowania w bazie danych
    public boolean verifyCredentials(String username, String password, String role) {
        String sql;
        if(role.equals("klient")) {
             sql = "SELECT * FROM klient WHERE login = ? AND haslo = ? ";
        }
        else {
             sql = "SELECT * FROM pracownik WHERE login = ? AND haslo = ? ";
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);


            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next(); // Zwraca true, jeśli znaleziono użytkownika
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
    // Metoda do pobierania produktów z bazy danych
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



    public List<String> getClientOrders(int clientId) throws SQLException {
        List<String> orders = new ArrayList<>();

        String sql = "SELECT idzakup FROM zakup WHERE klient_idklient = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, clientId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int orderId = resultSet.getInt("idzakup");


                String productSql = "SELECT s.nazwa, s.cena FROM zakup_has_sprzet zh " +
                        "JOIN sprzet s ON zh.sprzet_idsprzet = s.idsprzet " +
                        "WHERE zh.zakup_idzakup = ?";
                try (PreparedStatement productStatement = connection.prepareStatement(productSql)) {
                    productStatement.setInt(1, orderId);
                    ResultSet productResultSet = productStatement.executeQuery();

                    StringBuilder products = new StringBuilder();
                    while (productResultSet.next()) {
                        String productName = productResultSet.getString("nazwa");
                        int productPrice = productResultSet.getInt("cena");
                        products.append(productName).append(" - Cena: ").append(productPrice).append(" zł, ");
                    }

                    if (products.length() > 0) {
                        products.setLength(products.length() - 2);
                    }

                    orders.add("Id Zakupu: " + orderId + " - Sprzęt: " + products);
                }
            }
        }

        return orders;
    }



    public List<String> getClientServiceRequests(int clientId) throws SQLException {
        List<String> requests = new ArrayList<>();


        String sql = "SELECT z.idserwis, z.sprzet_nr_seryjny, z.pracownik_idpracownik " +
                "FROM zgloszenie_serwisowe z " +
                "WHERE z.klient_idklient = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, clientId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int serviceRequestId = resultSet.getInt("idserwis");
                int equipmentSerialNumber = resultSet.getInt("sprzet_nr_seryjny");
                int employeeId = resultSet.getInt("pracownik_idpracownik");


                String requestDetails = "Zgłoszenie serwisowe ID: " + serviceRequestId +
                        ", Sprzęt nr seryjny: " + equipmentSerialNumber +
                        ", Pracownik ID: " + employeeId;

                requests.add(requestDetails);
            }
        }

        return requests;
    }
    public List<String> getClientProducts(int clientId) throws SQLException {
        List<String> products = new ArrayList<>();
        String sql = "SELECT s.nazwa, zhs.nr_seryjny, z.idzakup, zhs.sprzet_idsprzet, z.pracownik_idpracownik FROM zakup z " +
                "JOIN zakup_has_sprzet zhs ON z.idzakup = zhs.zakup_idzakup " +
                "JOIN sprzet s ON zhs.sprzet_idsprzet = s.idsprzet " +
                "WHERE z.klient_idklient = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, clientId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {

                String productInfo = "Nazwa: " + resultSet.getString("nazwa") +
                        ", Numer seryjny: " + resultSet.getInt("nr_seryjny") +
                        ", Zakup ID: " + resultSet.getInt("idzakup") +
                        ", Sprzęt ID: " + resultSet.getInt("sprzet_idsprzet") +
                        ", Pracownik ID: " + resultSet.getInt("pracownik_idpracownik");
                products.add(productInfo);
            }
        }
        return products;
    }

    public int getSerialNumberByProductName(String productName) throws SQLException {
        String sql = "SELECT s.nrseryjny FROM zakup_has_sprzet s WHERE s.nazwa = ? and s.zakup_idzakup = ? and s.sprzet_idsprzet=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, productName);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("idsprzet");
            } else {
                throw new SQLException("Nie znaleziono sprzętu o nazwie: " + productName);
            }
        }
    }
    public void addServiceRequest(int workerId,int clientId, int serialNumber) throws SQLException {
        String sql = "INSERT INTO zgloszenie_serwisowe (pracownik_idpracownik,klient_idklient, sprzet_nr_seryjny) " +
                "VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, workerId);
            preparedStatement.setInt(2, clientId);
            preparedStatement.setInt(3, serialNumber);
            preparedStatement.executeUpdate();
        }
    }
    public int getUserIdByUsernameAndRole(String username, String password) throws SQLException {

           String sql = "SELECT idklient FROM klient WHERE login = ? and haslo=? ";


        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2,password);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);  // Pierwsza kolumna zawiera ID użytkownika
            } else {
                throw new SQLException("Nie znaleziono użytkownika o podanym username.");
            }
        }


    }
    public int getEmployeeIdByUsernameAndRole(String username, String password) throws SQLException {

        String sql = "SELECT idpracownik FROM pracownik WHERE login = ? and haslo=? ";


        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);  // Pierwsza kolumna zawiera ID użytkownika
            } else {
                throw new SQLException("Nie znaleziono użytkownika o podanym username.");
            }
        }

    }

    public void placeOrder(int clientId, List<String> cart, boolean installments) throws SQLException {
        System.out.println(installments);

        connection.setAutoCommit(false);

        try {

            String orderSql = "INSERT INTO zakup (pracownik_idpracownik, klient_idklient) VALUES (?, ?)";
            try (PreparedStatement orderStatement = connection.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                orderStatement.setInt(1, 1);
                orderStatement.setInt(2, clientId);
                orderStatement.executeUpdate();


                ResultSet generatedKeys = orderStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int orderId = generatedKeys.getInt(1);
                    int kwota=0;
                    int counter = 0;
                    for (String product : cart) {

                        String[] parts = product.split(" ");
                        if (parts.length < 1) {
                            throw new SQLException("Nieprawidłowy format produktu: " + product);
                        }

                        int productId =    Integer.parseInt(parts[0]);
                        kwota+=getCena(productId);
                        String paymentSql;
                        //generowanie serial number??
                        LocalDateTime now = LocalDateTime.now();
                        String dateTimeString = now.format(DateTimeFormatter.ofPattern("MMddHHmmss"));
                        int baseSerial = Integer.parseInt(dateTimeString);
                        int serialNumber = baseSerial % 1_000_000_000 + productId;
                         serialNumber +=  + counter;
                        counter++;


                        String cartSql = "INSERT INTO zakup_has_sprzet (nr_seryjny, zakup_idzakup, sprzet_idsprzet) VALUES (?, ?, ?)";
                                try (PreparedStatement cartStatement = connection.prepareStatement(cartSql)) {
                                    cartStatement.setInt(1,serialNumber);//jak generować? data+godzina_idproduktu?
                                    cartStatement.setInt(2, orderId);
                                    cartStatement.setInt(3, productId);
                                    cartStatement.executeUpdate();
                                }


                                    paymentSql = "INSERT INTO platnosc (zakup_zakupid, data_platnosci,kwota, zakupy_na_raty) VALUES (?, ?, ?,?)";
                                    try (PreparedStatement paymentStatement = connection.prepareStatement(paymentSql)) {
                                        paymentStatement.setInt(1,orderId);
                                        paymentStatement.setTimestamp(2, new java.sql.Timestamp(new java.util.Date().getTime()));
                                        paymentStatement.setDouble(3, kwota);
                                        paymentStatement.setBoolean(4, installments);
                                        paymentStatement.executeUpdate();
                                    }



                    }
                } else {
                    throw new SQLException("Nie udało się utworzyć zamówienia - brak ID zamówienia.");
                }


                connection.commit();
                System.out.println("Zamówienie zostało złożone.");
            } catch (SQLException e) {

                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Błąd przy składaniu zamówienia: " + e.getMessage());
        } finally {

            connection.setAutoCommit(true);
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

    public int getCena(int id) throws SQLException
    {

        String sql = "SELECT cena FROM sprzet WHERE idsprzet = ?";


        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);  // Pierwsza kolumna zawiera ID użytkownika
            } else {
                throw new SQLException("Nie znaleziono użytkownika o podanym username.");
            }
        }
    }



    // PU12 Wyszukanie konta w bazie
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

    void registerNewUser(String Name, String Surname,String newUsername, String newPassword) throws SQLException
    {

        String insertQuery = "INSERT INTO klient(imie, nazwisko, login, haslo) VALUES (?,?, ?, ?)";
        try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
            insertStatement.setString(1, Name);
            insertStatement.setString(2, Surname);
            insertStatement.setString(3, newUsername);
            insertStatement.setString(4, newPassword);
            insertStatement.executeUpdate();
        }catch (SQLException e) {
            e.printStackTrace();

        }
    }

     boolean userExist(String login) throws  SQLException
    {
        String query = "SELECT COUNT(*) FROM klient WHERE login = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, login);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }
        return false;

    }





}


