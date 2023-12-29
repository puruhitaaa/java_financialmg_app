package com.pahuger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FinancialManager {
    private double balance = 0;
    private Connection conn;

    public FinancialManager() {
        try {
            String url = "jdbc:sqlite:finance.db";
            conn = DriverManager.getConnection(url);
            createTransactionsTable();
            System.out.println("Connection to SQLite has been established.");
            setBalance();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void createTransactionsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS transactions (\n"
                + " id integer PRIMARY KEY,\n"
                + " type text NOT NULL,\n"
                + " amount real NOT NULL\n"
                + ");";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void addTransaction(String type, double amount) throws Exception {
        String sql = "INSERT INTO transactions(type, amount) VALUES(?,?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (type.strip().toLowerCase().equals("expense")) {
                if (balance - amount < 0) {
                    throw new Exception("Insufficient balance!");
                }
            }

            pstmt.setString(1, type);
            pstmt.setDouble(2, amount);
            pstmt.executeUpdate();
            setBalance();
            System.out.println("Added transaction: " + type + ", " + "Amount: " + amount);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void displayTransactions() {
        String sql = "SELECT id, type, amount FROM transactions";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();

            System.out.println("Transactions list:");
            while (rs.next()) {
                System.out.println(rs.getInt("id") + ". " + rs.getString("type") + ", " + rs.getDouble("amount"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void updateTransaction(int id, String newType, double newAmount) throws Exception {
        String sql = "UPDATE transactions SET type = ? , "
                   + "amount = ? "
                   + "WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (newType.strip().toLowerCase().equals("expense")) {
                if (balance - newAmount < 0) {
                    throw new Exception("Insufficient balance!");
                }
            }

            pstmt.setString(1, newType);
            pstmt.setDouble(2, newAmount);
            pstmt.setInt(3, id);
            pstmt.executeUpdate();
            System.out.println("Updated a transaction with id: " + id);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void deleteTransaction(int id) {
        String sql = "DELETE FROM transactions WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("Deleted a transaction with id: " + id);
            setBalance();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void setBalance() {
        double income = 0;
        double expense = 0;

        String sql = "SELECT type, amount FROM transactions";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String type = rs.getString("type");
                double amount = rs.getDouble("amount");

                if (type.strip().toLowerCase().equals("income")) {
                    income += amount;
                } else if (type.strip().toLowerCase().equals("expense")) {
                    expense += amount;
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        balance = income - expense;
    }

    public void getBalance() {
        System.out.println("Balance: " + balance);
    }
}