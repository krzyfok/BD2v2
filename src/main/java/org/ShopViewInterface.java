package org;
import java.util.List;

public interface ShopViewInterface {
    void updateProductList(List<String> products);
    void showMessage(String message);
    void closeWindow();
    void showProductDetails(String productDetails);
    void showScrollableMessage(String title, List<String> items);
    public void openEditProductDialog(int productId, String name, double price);
}
