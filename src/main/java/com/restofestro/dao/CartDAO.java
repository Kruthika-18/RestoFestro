package com.restofestro.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.restofestro.model.Food;
import com.restofestro.util.DBConnection;

public class CartDAO {

    // Add an item, or increase quantity if it's already in the cart
    public void addToCart(int userId, int foodId, int quantity) {
        String checkSql = "SELECT id, quantity FROM cart WHERE user_id = ? AND food_id = ?";
        String updateSql = "UPDATE cart SET quantity = ? WHERE id = ?";
        String insertSql = "INSERT INTO cart (user_id, food_id, quantity) VALUES (?, ?, ?)";

        try (Connection con = DBConnection.getConnection()) {

            try (PreparedStatement checkPs = con.prepareStatement(checkSql)) {
                checkPs.setInt(1, userId);
                checkPs.setInt(2, foodId);
                ResultSet rs = checkPs.executeQuery();

                if (rs.next()) {
                    int cartId = rs.getInt("id");
                    int newQuantity = rs.getInt("quantity") + quantity;

                    try (PreparedStatement updatePs = con.prepareStatement(updateSql)) {
                        updatePs.setInt(1, newQuantity);
                        updatePs.setInt(2, cartId);
                        updatePs.executeUpdate();
                    }
                } else {
                    try (PreparedStatement insertPs = con.prepareStatement(insertSql)) {
                        insertPs.setInt(1, userId);
                        insertPs.setInt(2, foodId);
                        insertPs.setInt(3, quantity);
                        insertPs.executeUpdate();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Get all cart items for a user, each row combined with its food details
    public List<Food> getCartItems(int userId) {
        List<Food> items = new ArrayList<>();
        String sql = "SELECT f.*, c.quantity AS cart_quantity, c.id AS cart_id "
                   + "FROM cart c JOIN food f ON c.food_id = f.id "
                   + "WHERE c.user_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Food food = new Food();
                food.setId(rs.getInt("cart_id")); // reuse id field to store the CART row id
                food.setFoodId(rs.getInt("id"));
                food.setName(rs.getString("name"));
                food.setPrice(rs.getDouble("price"));
                food.setImage(rs.getString("image"));
                food.setVegType(rs.getString("veg_type"));
                food.setState(rs.getString("state"));
                food.setCategory(rs.getString("category"));
                food.setAvailable(true);
                food.setCartQuantity(rs.getInt("cart_quantity"));
                items.add(food);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    // Update the quantity of one cart row
    public void updateQuantity(int cartId, int quantity) {
        String sql = "UPDATE cart SET quantity = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, quantity);
            ps.setInt(2, cartId);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Remove one item from the cart
    public void removeItem(int cartId) {
        String sql = "DELETE FROM cart WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, cartId);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Clear the entire cart for a user (used after placing an order)
    public void clearCart(int userId) {
        String sql = "DELETE FROM cart WHERE user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}