package com.pahuger;

import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        FinancialManager manager = new FinancialManager();
        Scanner scanner = new Scanner(System.in);
        int choice;

        do {
            System.out.println("\nFinancial Management System");
            System.out.println("1. Add Transaction");
            System.out.println("2. View All Transactions");
            System.out.println("3. Show Balance");
            System.out.println("4. Update Transaction");
            System.out.println("5. Delete Transaction");
            System.out.println("6. Exit");
            System.out.print("Enter your choice: ");
            choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    System.out.print("Enter transaction type (Income/Expense): ");
                    scanner.nextLine(); // Consume the newline character
                    String type = scanner.nextLine();
                    System.out.print("Enter transaction amount: ");
                    double amount = scanner.nextDouble();
                    try {
                        manager.addTransaction(type, amount);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    };
                    break;
                case 2:
                    manager.displayTransactions();
                    break;
                case 3:
                    manager.getBalance();
                    break;
                case 4:
                    System.out.print("Enter transaction id: ");
                    int id = scanner.nextInt();
                    // scanner.nextLine();
                    System.out.print("Enter new transaction type (Income/Expense): ");
                    scanner.nextLine();
                    String newType = scanner.nextLine();
                    System.out.print("Enter transaction amount: ");
                    double newAmount = scanner.nextDouble();

                    try {
                        manager.updateTransaction(id, newType, newAmount);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case 5:
                    System.out.print("Enter index of transaction to delete: ");
                    int index = scanner.nextInt();
                    manager.deleteTransaction(index);
                    break;
                case 6:
                    System.out.println("Exiting... Thank you!");
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a valid option.");
                    break;
            }
        } while (choice != 6);

        scanner.close();
    }
}