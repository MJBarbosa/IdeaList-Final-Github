package com.example.idealist;

public class ReadWriteProductDetailsSO {

    private String productId;
    private String productName;
    private String supplierName;
    private String category;
    private String productDescription;
    private String quantity;
    private String price;
    private String userUid;

    public ReadWriteProductDetailsSO() {
        // Default constructor is required by Firebase
    }

    public ReadWriteProductDetailsSO(String userUid, String productId, String productName, String supplierName, String category, String productDescription, String quantity, String price) {
        this.userUid = userUid;
        this.productId = productId;
        this.productName = productName;
        this.supplierName = supplierName;
        this.category = category;
        this.productDescription = productDescription;
        this.quantity = quantity;
        this.price = price;
    }

    // Getter and setter for userUid
    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    // Getters and setters for the fields (required for Firebase)
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
