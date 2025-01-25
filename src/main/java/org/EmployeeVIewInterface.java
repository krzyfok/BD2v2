package org;

import java.util.List;

public interface EmployeeVIewInterface {

    void showMessage(String message);
    void closeWindow();
    void showScrollableMessage(String title, List<String> items);
     void openEditProductDialog(int productId, String name, double price, int quantity);

    void openEditEmployeeDialog(String loginToModify, String imie, String nazwisko, String haslo, int pensja);

    void openEditClientDialog(String loginToModify, String imie, String nazwisko, String haslo);

    void showModifyEmployeeDialog(List<String> employeeLogins);

    void showDeleteEmployeeDialog(List<String> employeeLogins);

    void showModifyClientDialog(List<String> clientLogins);

    void showDeleteClientDialog(List<String> clientLogins);
}
