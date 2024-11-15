package electricbilling;

import java.sql.*;
import java.time.LocalDate;
import java.util.Random;
import java.util.Scanner;

public class electricbilling {

    private static final String URL = "jdbc:mysql://localhost:3306/ElectricityBillingDB";
    private static final String USER = "root";
    private static final String PASSWORD = "Adhi@2006"; // Replace with your password

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
                System.out.println("Connected to the database.");

                int choice;
                do {
                    System.out.println("\nChoose an option:");
                    System.out.println("1. Add a new customer");
                    System.out.println("2. Generate bill");
                    System.out.println("3. Display all bills");
                    System.out.println("4. Display monthly bills");
                    System.out.println("5. Make a payment");
                    choice = scanner.nextInt();

                    switch (choice) {
                        case 1:
                            scanner.nextLine();
                            System.out.print("Enter your Customer ID: ");
                            int customerId = scanner.nextInt();
                            scanner.nextLine(); // Consume newline
                            System.out.print("Enter customer name: ");
                            String name = scanner.nextLine();
                            System.out.print("Enter customer address: ");
                            String address = scanner.nextLine();
                            System.out.print("Enter customer email: ");
                            String email = scanner.nextLine();
                            addCustomer(conn, customerId, name, address, email);
                            generateRandomPreviousMonthData(conn, customerId); // Generate historical data
                            break;

                        case 2:
                            System.out.print("Enter your Customer ID: ");
                            customerId = scanner.nextInt();
                            greetCustomer(conn, customerId);
                            generateRandomBill(conn, customerId);  // Generate bill with random units
                            break;

                        case 3:
                            System.out.print("Enter your Customer ID: ");
                            customerId = scanner.nextInt();
                            greetCustomer(conn, customerId);
                            displayAllBills(conn, customerId);
                            break;

                        case 4:
                            System.out.print("Enter your Customer ID: ");
                            customerId = scanner.nextInt();
                            greetCustomer(conn, customerId);
                            System.out.print("Enter year (YYYY): ");
                            int year = scanner.nextInt();
                            System.out.print("Enter month (MM): ");
                            int month = scanner.nextInt();
                            displayMonthlyBills(conn, customerId, year, month);
                            break;

                        case 5:
                            System.out.print("Enter your Customer ID: ");
                            customerId = scanner.nextInt();
                            greetCustomer(conn, customerId);
                            System.out.print("Enter Bill ID to pay: ");
                            int billId = scanner.nextInt();
                            System.out.print("Enter payment amount: ");
                            double amount = scanner.nextDouble();
                            scanner.nextLine(); // Consume newline
                            System.out.print("Enter payment method (e.g., Credit Card, UPI): ");
                            String paymentMethod = scanner.nextLine();
                            makePayment(conn, billId, amount, paymentMethod);
                            break;

                        default:
                            System.out.println("Invalid option. Please try again.");
                    }

                } while (choice >= 1 && choice <= 5);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    public static void greetCustomer(Connection conn, int customerId) {
        String query = "SELECT name FROM Customers WHERE customer_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    System.out.println("Welcome, " + name + "!");
                } else {
                    System.out.println("Customer ID not found. Please add the customer details.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching customer name: " + e.getMessage());
        }
    }

    public static void addCustomer(Connection conn, int customerId, String name, String address, String email) {
        String query = "INSERT INTO Customers (customer_id, name, address, email) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, customerId);
            stmt.setString(2, name);
            stmt.setString(3, address);
            stmt.setString(4, email);
            stmt.executeUpdate();
            System.out.println("Customer added successfully.");
        } catch (SQLException e) {
            System.out.println("Error adding customer: " + e.getMessage());
        }
    }

    public static void generateRandomPreviousMonthData(Connection conn, int customerId) {
        Random rand = new Random();
        double ratePerUnit = 5.0;
        LocalDate currentDate = LocalDate.now();

        try {
            for (int i = 1; i <= 12; i++) {
                double unitsConsumed = 800 + rand.nextInt(9200); // Random units between 800 and 9999
                double amount = unitsConsumed * ratePerUnit;
                LocalDate billDate = currentDate.minusMonths(i);

                String query = "INSERT INTO BillingRecords (customer_id, units_consumed, amount, bill_date, status) VALUES (?, ?, ?, ?, 'Paid')";

                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setInt(1, customerId);
                    stmt.setDouble(2, unitsConsumed);
                    stmt.setDouble(3, amount);
                    stmt.setDate(4, Date.valueOf(billDate));
                    stmt.executeUpdate();
                    System.out.printf("Generated random data for %s: %,.0f units, totaling ₹%,.2f.\n", billDate, unitsConsumed, amount);
                }
            }
            System.out.println("Random previous month data generated successfully for customer ID: " + customerId);

        } catch (SQLException e) {
            System.out.println("Error generating random previous month data: " + e.getMessage());
        }
    }

    public static void generateRandomBill(Connection conn, int customerId) {
        Random rand = new Random();
        double unitsConsumed = 1000 + rand.nextInt(9000); // Current month consumption
        double ratePerUnit = 5.0;
        double amount = unitsConsumed * ratePerUnit;
        LocalDate billDate = LocalDate.now();

        // Insert the current month's bill
        String query = "INSERT INTO BillingRecords (customer_id, units_consumed, amount, bill_date, status) VALUES (?, ?, ?, ?, 'Unpaid')";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, customerId);
            stmt.setDouble(2, unitsConsumed);
            stmt.setDouble(3, amount);
            stmt.setDate(4, Date.valueOf(billDate));
            stmt.executeUpdate();

            System.out.printf("Bill generated successfully with %,.0f units consumed, totaling ₹%,.2f.\n", unitsConsumed, amount);

            // Retrieve previous month's consumption
            double previousMonthConsumption = getPreviousMonthConsumption(conn, customerId);

            // Provide feedback based on consumption comparison
            if (previousMonthConsumption != -1) {
                System.out.printf("Previous month consumption was %,.0f units.\n", previousMonthConsumption);
                if (unitsConsumed < previousMonthConsumption) {
                    System.out.println("Great job! Your consumption has decreased. Keep it up!");
                } else if (unitsConsumed > previousMonthConsumption) {
                    System.out.println("Your consumption has increased. Try to lower it next month. You're doing great, just be mindful!");
                } else {
                    System.out.println("Your consumption is the same as last month. You can do better—let's aim for a reduction!");
                }
            } else {
                System.out.println("No previous month data available for comparison. You're starting fresh—make it count!");
            }
        } catch (SQLException e) {
            System.out.println("Error generating bill: " + e.getMessage());
        }
    }


    public static void displayAllBills(Connection conn, int customerId) {
        String query = "SELECT * FROM BillingRecords WHERE customer_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                System.out.printf("%-10s %-10s %-10s %-10s %-10s\n", "Bill ID", "Units", "Amount", "Date", "Status");
                while (rs.next()) {
                    int billId = rs.getInt("bill_id");
                    double unitsConsumed = rs.getDouble("units_consumed");
                    double amount = rs.getDouble("amount");
                    Date billDate = rs.getDate("bill_date");
                    String status = rs.getString("status");

                    System.out.printf("%-10d %-10.2f %-10.2f %-10s %-10s\n", billId, unitsConsumed, amount, billDate, status);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error displaying bills: " + e.getMessage());
        }
    }

    public static double getPreviousMonthConsumption(Connection conn, int customerId) {
        String query = "SELECT units_consumed FROM BillingRecords " +
                       "WHERE customer_id = ? AND bill_date = (SELECT MAX(bill_date) " +
                       "FROM BillingRecords WHERE customer_id = ? AND MONTH(bill_date) = MONTH(NOW()) - 1 AND YEAR(bill_date) = YEAR(NOW()))";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, customerId);
            stmt.setInt(2, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("units_consumed");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving previous month consumption: " + e.getMessage());
        }
        return -1; // Return -1 if no data is found
    }


    public static void displayMonthlyBills(Connection conn, int customerId, int year, int month) {
        String query = "SELECT * FROM BillingRecords WHERE customer_id = ? " +
                       "AND YEAR(bill_date) = ? AND MONTH(bill_date) = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, customerId);
            stmt.setInt(2, year);
            stmt.setInt(3, month);
            try (ResultSet rs = stmt.executeQuery()) {
                System.out.printf("%-10s %-10s %-10s %-10s %-10s\n", "Bill ID", "Units", "Amount", "Date", "Status");
                while (rs.next()) {
                    int billId = rs.getInt("bill_id");
                    double unitsConsumed = rs.getDouble("units_consumed");
                    double amount = rs.getDouble("amount");
                    Date billDate = rs.getDate("bill_date");
                    String status = rs.getString("status");

                    System.out.printf("%-10d %-10.2f %-10.2f %-10s %-10s\n", billId, unitsConsumed, amount, billDate, status);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error displaying monthly bills: " + e.getMessage());
        }
    }

    public static void makePayment(Connection conn, int billId, double amount, String paymentMethod) {
        String query = "UPDATE BillingRecords SET status = 'Paid' WHERE bill_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, billId);
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.printf("Payment of ₹%,.2f received for Bill ID %d using %s.\n", amount, billId, paymentMethod);
            } else {
                System.out.println("Bill ID not found or payment already made.");
            }
        } catch (SQLException e) {
            System.out.println("Error making payment: " + e.getMessage());
        }
    }
}

