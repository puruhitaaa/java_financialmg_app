package com.pahuger;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

/**
 * JavaFX App
 */
public class App extends Application {
    private static final String DB_URL = "jdbc:sqlite:financial_management.db";
    private ObservableList<FinancialRecord> financialRecords;
    private TableView<FinancialRecord> table;
    private static Scene scene;

    @Override
    public void start(Stage primaryStage) throws IOException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
            Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS financial_records (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "description TEXT NOT NULL," +
                    "amount REAL NOT NULL)";
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        primaryStage.setTitle("Small-scale Business Financial Management App");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(8);
        grid.setHgap(10);

        Label descriptionLabel = new Label("Description:");
        GridPane.setConstraints(descriptionLabel, 0, 0);

        TextField descriptionInput = new TextField();
        GridPane.setConstraints(descriptionInput, 1, 0);

        Label amountLabel = new Label("Amount:");
        GridPane.setConstraints(amountLabel, 0, 1);

        TextField amountInput = new TextField();
        GridPane.setConstraints(amountInput, 1, 1);

        Button addButton = new Button("Add");
        GridPane.setConstraints(addButton, 1, 2);
        addButton.setOnAction(e -> addFinancialRecord(descriptionInput.getText(), Double.parseDouble(amountInput.getText())));
        
        Button updateButton = new Button("Update");
        GridPane.setConstraints(updateButton, 2, 2);
        updateButton.setOnAction(e -> {
            FinancialRecord recordToUpdate = table.getSelectionModel().getSelectedItem();
            updateFinancialRecord(recordToUpdate.getId(), descriptionInput.getText(), Double.parseDouble(amountInput.getText()));
        });

        Button deleteButton = new Button("Delete");
        GridPane.setConstraints(deleteButton, 3, 2);
        deleteButton.setOnAction(e -> {
            FinancialRecord recordToDelete = table.getSelectionModel().getSelectedItem();
            deleteFinancialRecord(recordToDelete.getId());
        });

        table = new TableView<>();
        TableColumn<FinancialRecord, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<FinancialRecord, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<FinancialRecord, Double> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));

        table.getColumns().addAll(idColumn, descriptionColumn, amountColumn);
        GridPane.setConstraints(table, 0, 3, 2, 1);

        financialRecords = FXCollections.observableArrayList();
        table.setItems(financialRecords);

        grid.getChildren().addAll(descriptionLabel, descriptionInput, amountLabel, amountInput, addButton, updateButton, deleteButton, table);

        Scene scene = new Scene(grid, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();

        viewFinancialRecords();
    }

    private void addFinancialRecord(String description, double amount) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO financial_records(description, amount) VALUES(?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, description);
            pstmt.setDouble(2, amount);
            int affectedRows = pstmt.executeUpdate();

            Statement statement = conn.createStatement();

            if (affectedRows > 0) {
                try (ResultSet rs = statement.executeQuery("SELECT last_insert_rowid()")) {
                    if (rs.next()) {
                        financialRecords.add(new FinancialRecord(rs.getInt(1), description, amount));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void viewFinancialRecords() {
        financialRecords.clear();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM financial_records");

            while (rs.next()) {
                financialRecords.add(new FinancialRecord(
                        rs.getInt("id"),
                        rs.getString("description"),
                        rs.getDouble("amount")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateFinancialRecord(int id, String description, double amount) {
        String sql = "UPDATE financial_records SET description = ?, amount = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, description);
            pstmt.setDouble(2, amount);
            pstmt.setInt(3, id);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                refreshTable();
                System.out.println("Record updated successfully.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteFinancialRecord(int id) {
        String sql = "DELETE FROM financial_records WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                refreshTable();
                System.out.println("Record deleted successfully.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void refreshTable() {
        financialRecords.clear();
        loadFinancialRecords();
    }

    private void loadFinancialRecords() {
        String sql = "SELECT * FROM financial_records";
        try (Connection conn = DriverManager.getConnection(DB_URL);
            Statement stmt  = conn.createStatement();
            ResultSet rs    = stmt.executeQuery(sql)) {

            while (rs.next()) {
                financialRecords.add(new FinancialRecord(
                        rs.getInt("id"),
                        rs.getString("description"),
                        rs.getDouble("amount")));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

    public static class FinancialRecord {
        private final Integer id;
        private final String description;
        private final Double amount;

        public FinancialRecord(Integer id, String description, Double amount) {
            this.id = id;
            this.description = description;
            this.amount = amount;
        }

        public Integer getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

        public Double getAmount() {
            return amount;
        }
    }
}