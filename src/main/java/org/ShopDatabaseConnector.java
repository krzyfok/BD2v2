package org;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static java.sql.DriverManager.getConnection;

public class ShopDatabaseConnector implements DatabaseConnectorInterface {
    private Connection connection;

    @Override
    public void connect() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/mydb";
        String username = "root";
        String password = "";

        // Nawiązanie połączenia z bazą danych
        this.connection = DriverManager.getConnection(url, username, password);
        System.out.println("Połączenie z bazą danych nawiązane dla roli klient");
    }
    public Connection getConnection() {
        return this.connection;
    }
    @Override
    public void disconnect() throws SQLException {

    }

    public boolean verifyCredentials(String username, String password, String role) {
        String sql;
        if (role.equals("klient")) {
            sql = "SELECT * FROM klient WHERE login = ? AND haslo = ? ";
        } else {
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

    public int getUserIdByUsernameAndRole(String username, String password) throws SQLException {

        String sql = "SELECT idklient FROM klient WHERE login = ? and haslo=? ";


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

    public double getTotalOrdersValue(int clientId) throws SQLException {
        double totalValue = 0.0;
        String query = "{ ? = CALL oblicz_wartosc_zamowien(?) }"; // Wywołanie procedury SQL

        try (Connection conn = getConnection();
             CallableStatement stmt = conn.prepareCall(query)) {

            stmt.registerOutParameter(1, Types.DECIMAL);
            stmt.setInt(2, clientId);
            stmt.execute();

            totalValue = stmt.getDouble(1);
        }
        return totalValue;
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
}
