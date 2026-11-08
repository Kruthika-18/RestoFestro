package com.restofestro.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.restofestro.model.Food;
import com.restofestro.model.Order;
import com.restofestro.util.DBConnection;

public class OrderDAO {

    // Places an order: inserts into orders, then order_items for each cart item, then clears the cart
    public int placeOrder(int userId, String address, String paymentMethod, List<Food> cartItems) {
        String insertOrderSql = "INSERT INTO orders (user_id, address, total, payment_method, status) "
                + "VALUES (?, ?, ?, ?, 'Confirmed')";
        String insertItemSql = "INSERT INTO order_items (order_id, food_id, quantity, price) VALUES (?, ?, ?, ?)";
        String clearCartSql = "DELETE FROM cart WHERE user_id = ?";

        double total = 0;
        for (Food item : cartItems) {
            total += item.getPrice() * item.getCartQuantity();
        }

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false); // start a transaction

            int orderId = -1;

            try (PreparedStatement ps = con.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, userId);
                ps.setString(2, address);
                ps.setDouble(3, total);
                ps.setString(4, paymentMethod);
                ps.executeUpdate();

                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) {
                    orderId = keys.getInt(1);
                }
            }

            // Note: cartItems here hold FOOD id in a different place than the cart view.
            // We need the real food_id, so OrderServlet will pass food id separately (see below).
            try (PreparedStatement ps = con.prepareStatement(insertItemSql)) {
                for (Food item : cartItems) {
                    ps.setInt(1, orderId);
                    ps.setInt(2, item.getFoodId());
                    ps.setInt(3, item.getCartQuantity());
                    ps.setDouble(4, item.getPrice());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            try (PreparedStatement ps = con.prepareStatement(clearCartSql)) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            con.commit();
            return orderId;

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    // Get all orders for a user, most recent first
    public List<Order> getOrdersByUser(int userId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY order_date DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Order order = new Order();
                order.setId(rs.getInt("id"));
                order.setUserId(rs.getInt("user_id"));
                order.setAddress(rs.getString("address"));
                order.setTotal(rs.getDouble("total"));
                order.setPaymentMethod(rs.getString("payment_method"));
                order.setStatus(rs.getString("status"));
                order.setOrderDate(rs.getTimestamp("order_date"));
                orders.add(order);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return orders;
    }
    // Admin: get every order from every user, most recent first
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders ORDER BY order_date DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Order order = new Order();
                order.setId(rs.getInt("id"));
                order.setUserId(rs.getInt("user_id"));
                order.setAddress(rs.getString("address"));
                order.setTotal(rs.getDouble("total"));
                order.setPaymentMethod(rs.getString("payment_method"));
                order.setStatus(rs.getString("status"));
                order.setOrderDate(rs.getTimestamp("order_date"));
                orders.add(order);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return orders;
    }

    // Admin: update an order's status
    public void updateStatus(int orderId, String status) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, orderId);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Get items for one specific order, joined with food names
    public List<Food> getOrderItems(int orderId) {
        List<Food> items = new ArrayList<>();
        String sql = "SELECT f.name, oi.quantity, oi.price "
                   + "FROM order_items oi JOIN food f ON oi.food_id = f.id "
                   + "WHERE oi.order_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Food food = new Food();
                food.setName(rs.getString("name"));
                food.setCartQuantity(rs.getInt("quantity"));
                food.setPrice(rs.getDouble("price"));
                items.add(food);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }
}