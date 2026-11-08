package com.restofestro.model;

public class Food {
    private int id;
    private String name;
    private String state;
    private String category;
    private String description;
    private double price;
    private String image;
    private String vegType;
    private boolean available;
    private int cartQuantity;
    private int foodId;

   
   

    public Food() {
    }

    public Food(int id, String name, String state, String category, String description,
                double price, String image, String vegType, boolean available,int cartQuantity,int foodId) {
        this.id = id;
        this.name = name;
        this.state = state;
        this.category = category;
        this.description = description;
        this.price = price;
        this.image = image;
        this.vegType = vegType;
        this.available = available;
        this.cartQuantity=cartQuantity;
        this.foodId=foodId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getVegType() {
        return vegType;
    }

    public void setVegType(String vegType) {
        this.vegType = vegType;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
    public int getCartQuantity() {
        return cartQuantity;
    }

    public void setCartQuantity(int cartQuantity) {
        this.cartQuantity = cartQuantity;
    }
    public int getFoodId() {
        return foodId;
    }

    public void setFoodId(int foodId) {
        this.foodId = foodId;
    }
}