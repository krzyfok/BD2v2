package org;
import java.sql.SQLException;

public class LoginHandler {
    private LoginView loginView;
    private ShopDatabaseConnector databaseConnector;

    public LoginHandler() {
        this.loginView = new LoginView(this);
        this.databaseConnector = new ShopDatabaseConnector();
    }


    public void handleLogin(String username, String password, String role) {
        try {

            databaseConnector.connect();


            if (databaseConnector.verifyCredentials(username, password, role)) {
                loginView.showMessage("Zalogowano pomyślnie jako: " + role);
                loginView.closeWindow(); // Zamknięcie okna logowania

                // Przekierowanie do odpowiedniego widoku
                switch (role) {
                    case "klient":
                        UserSession.setLoggedInUserId(databaseConnector.getUserIdByUsernameAndRole(username,password));
                        ShopView shopView = new ShopView();  // Poprawnie inicjalizujemy ShopView
                        ShopPresenter shopPresenter = new ShopPresenter(shopView, new ShopDatabaseConnector());
                        shopView.setPresenter(shopPresenter);
                        break;
                    case "pracownik":

                        EmployeeView employeeView = new EmployeeView();  // Poprawnie inicjalizujemy ShopView
                        EmployeePresenter employeePresenter = new EmployeePresenter(employeeView, new EmployeeDatabaseConnector());
                        employeeView.setPresenter(employeePresenter);
                        break;

                }
            } else {
                loginView.showMessage("Niepoprawne dane logowania.");
            }
        } catch (SQLException ex) {
            loginView.showMessage("Błąd podczas logowania: " + ex.getMessage());
            ex.printStackTrace();
        } finally {

            loginView.clearPasswordField();
            try {
                databaseConnector.disconnect();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public boolean handleRegister(String Name, String Surname,String newUsername, String newPassword) {

    try{
        databaseConnector.connect();

        if(databaseConnector.userExist(newUsername))
        {
                loginView.showMessage("Login zajęty");
                return false;
        }
        databaseConnector.registerNewUser(Name, Surname, newUsername, newPassword);


    }
    catch (SQLException ex) {
        loginView.showMessage("Błąd podczas logowania: " + ex.getMessage());
        ex.printStackTrace();
    } finally {

        loginView.clearPasswordField();
        try {
            databaseConnector.disconnect();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    return true;
    }
}
