package org;

import java.util.List;

public interface EmployeeVIewInterface {

    void showMessage(String message);
    void closeWindow();
    void showScrollableMessage(String title, List<String> items);
     void openEditProductDialog(int productId, String name, double price, int quantity);
}
