package org;
import javax.swing.*;
import java.awt.*;

public class LoginView {
    private JFrame frame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private LoginHandler loginHandler;

    public LoginView(LoginHandler handler) {
        this.loginHandler=handler;
        // Konfiguracja okna logowania
        frame = new JFrame("Logowanie");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(4, 2));

        // Pola do wprowadzania danych logowania
        frame.add(new JLabel("Nazwa użytkownika:"));
        usernameField = new JTextField();
        frame.add(usernameField);

        frame.add(new JLabel("Hasło:"));
        passwordField = new JPasswordField();
        frame.add(passwordField);

        frame.add(new JLabel("Rola:"));
        roleComboBox = new JComboBox<>(new String[]{"klient", "pracownik"});
        frame.add(roleComboBox);

        // Przycisk logowania
        JButton loginButton = new JButton("Zaloguj");
        loginButton.addActionListener(e -> handleLogin());
        frame.add(loginButton);
        JButton registerButton = new JButton("Zarejestruj");
        registerButton.addActionListener(e->openRegisterDialog());
        //
        frame.add(registerButton);
        frame.setVisible(true);

    }

    // Obsługa logowania
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String role = (String) roleComboBox.getSelectedItem();

        loginHandler.handleLogin(username, password, role);
    }

    private void openRegisterDialog() {
        JDialog registerDialog = new JDialog(frame, "Rejestracja", true);
        registerDialog.setSize(400, 300);
        registerDialog.setLayout(new GridLayout(6, 2));

        JTextField newNameField = new JTextField();
        JTextField newSurnameField = new JTextField();
        JTextField newUsernameField = new JTextField();
        JPasswordField newPasswordField = new JPasswordField();


        registerDialog.add(new JLabel("Imie:"));
        registerDialog.add(newNameField);
        registerDialog.add(new JLabel("Nazwisko:"));
        registerDialog.add(newSurnameField);
        registerDialog.add(new JLabel("Login:"));
        registerDialog.add(newUsernameField);
        registerDialog.add(new JLabel("Hasło:"));
        registerDialog.add(newPasswordField);

        JButton registerButton = new JButton("Zarejestruj");
        registerButton.addActionListener(e -> {
            String Name = newNameField.getText().trim();
            String Surname = newSurnameField.getText().trim();
            String newUsername = newUsernameField.getText().trim();
            String newPassword = new String(newPasswordField.getPassword()).trim();


            if (newUsername.isEmpty() || newPassword.isEmpty()) {
                JOptionPane.showMessageDialog(registerDialog, "Wszystkie pola muszą być wypełnione!", "Błąd", JOptionPane.ERROR_MESSAGE);
                return;
            }
        if(loginHandler.handleRegister(Name, Surname,newUsername, newPassword))
        {JOptionPane.showMessageDialog(registerDialog, "Rejestracja zakończona sukcesem!");
            registerDialog.dispose();}
        });

        registerDialog.add(registerButton);
        registerDialog.setVisible(true);
    }

    // Metody do manipulacji oknem
    public void closeWindow() {
        frame.dispose();
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(frame, message);
    }

    public void clearPasswordField() {
        passwordField.setText("");
    }
}
