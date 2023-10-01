package com.example.idealist;

public class ReadWriteProductDetailsSO {

    private String productId;
    private String productName;
    private String supplierName;
    private String category;
    private String productDescription;
    private String quantity;
    private String price;

    public ReadWriteProductDetailsSO(String textProductId, String textProductName, String textAddSuppName, String selectedAddCategory, String textAddProductDesc, String textAddQuantity, String textAddPrice) {
        this.productId = textProductId;
        this.productName = textProductName;
        this.supplierName = textAddSuppName;
        this.category = selectedAddCategory;
        this.productDescription = textAddProductDesc;
        this.quantity = textAddQuantity;
        this.price = textAddPrice;
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
