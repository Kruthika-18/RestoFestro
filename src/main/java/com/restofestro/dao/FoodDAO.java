package com.restofestro.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.restofestro.model.Food;
import com.restofestro.util.DBConnection;

public class FoodDAO {

    // Get all available food items for a state
    public List<Food> getFoodByState(String state) {
        List<Food> foodList = new ArrayList<>();
        String sql = "SELECT * FROM food WHERE state = ? AND available = TRUE";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, state);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                foodList.add(mapRow(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return foodList;
    }

    // Get food items for a state AND category
    public List<Food> getFoodByStateAndCategory(String state, String category) {
        List<Food> foodList = new ArrayList<>();
        String sql = "SELECT * FROM food WHERE state = ? AND category = ? AND available = TRUE";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, state);
            ps.setString(2, category);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                foodList.add(mapRow(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return foodList;
    }

    // Search food by name, across all states
    public List<Food> searchFood(String keyword) {
        List<Food> foodList = new ArrayList<>();
        String sql = "SELECT * FROM food WHERE name LIKE ? AND available = TRUE";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                foodList.add(mapRow(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return foodList;
    }

    // Get one food item by id (used for the food details page)
    public Food getFoodById(int id) {
        String sql = "SELECT * FROM food WHERE id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapRow(rs);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    // Admin: get every food item, regardless of availability
    public List<Food> getAllFood() {
        List<Food> foodList = new ArrayList<>();
        String sql = "SELECT * FROM food ORDER BY state, category, name";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                foodList.add(mapRow(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return foodList;
    }

    // Admin: add a new food item
    public void addFood(Food food) {
        String sql = "INSERT INTO food (name, state, category, description, price, image, veg_type, available) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, food.getName());
            ps.setString(2, food.getState());
            ps.setString(3, food.getCategory());
            ps.setString(4, food.getDescription());
            ps.setDouble(5, food.getPrice());
            ps.setString(6, food.getImage());
            ps.setString(7, food.getVegType());
            ps.setBoolean(8, food.isAvailable());

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Admin: delete a food item by id
    public void deleteFood(int id) {
        String sql = "DELETE FROM food WHERE id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Admin: update an existing food item
    public void updateFood(Food food) {
        String sql = "UPDATE food SET name=?, state=?, category=?, description=?, price=?, image=?, veg_type=?, available=? "
                   + "WHERE id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, food.getName());
            ps.setString(2, food.getState());
            ps.setString(3, food.getCategory());
            ps.setString(4, food.getDescription());
            ps.setDouble(5, food.getPrice());
            ps.setString(6, food.getImage());
            ps.setString(7, food.getVegType());
            ps.setBoolean(8, food.isAvailable());
            ps.setInt(9, food.getId());

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    // Helper: converts one ResultSet row into a Food object
    private Food mapRow(ResultSet rs) throws Exception {
        Food food = new Food();
        food.setId(rs.getInt("id"));
        food.setName(rs.getString("name"));
        food.setState(rs.getString("state"));
        food.setCategory(rs.getString("category"));
        food.setDescription(rs.getString("description"));
        food.setPrice(rs.getDouble("price"));
        food.setImage(rs.getString("image"));
        food.setVegType(rs.getString("veg_type"));
        food.setAvailable(rs.getBoolean("available"));
        return food;
    }
}