
import java.sql.*;
import java.util.Scanner;

public class InventoryManagement {

    static String url = "jdbc:mysql://localhost:3306/retail_db";
    static String user = "root";
    static String password = "Krish27sql";

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        try (Connection con =
                 DriverManager.getConnection(url, user, password)) {

            System.out.println("MySQL connected successfully!");

            while (true) {
                System.out.println("\n--- INVENTORY MANAGEMENT ---");
                System.out.println("1. Insert Product");
                System.out.println("2. Display Low Stock Products");
                System.out.println("3. Add Stock");
                System.out.println("4. Update Product Price");
                System.out.println("5. Calculate Inventory Value");
                System.out.println("6. Delete Product");
                System.out.println("7. Exit");
                System.out.print("Enter choice: ");

                int choice = sc.nextInt();

                switch (choice) {
                    case 1:
                        insertProduct(con);
                        break;
                    case 2:
                        displayProducts(con);
                        break;
                    case 3:
                        addStock(con);
                        break;
                    case 4:
                        updatePrice(con);
                        break;
                    case 5:
                        calculateInventoryValue(con);
                        break;
                    case 6:
                        deleteProduct(con);
                        break;
                    case 7:
                        System.out.println("Program ended.");
                        return;
                    default:
                        System.out.println("Invalid choice!");
                }
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // 1. Insert 3 products, handling duplicate product IDs
    static void insertProduct(Connection con) {

        String sql = "INSERT INTO Product VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            for (int i = 1; i <= 3; i++) {

                System.out.println("\nEnter details for product " + i);

                System.out.print("Product ID: ");
                int id = sc.nextInt();
                sc.nextLine();

                System.out.print("Product name: ");
                String name = sc.nextLine();

                System.out.print("Category: ");
                String category = sc.nextLine();

                System.out.print("Quantity: ");
                int quantity = sc.nextInt();

                System.out.print("Price: ");
                double price = sc.nextDouble();

                if (quantity < 0 || price < 0) {
                    System.out.println("Invalid quantity or price.");
                    continue;
                }

                ps.setInt(1, id);
                ps.setString(2, name);
                ps.setString(3, category);
                ps.setInt(4, quantity);
                ps.setDouble(5, price);

                try {
                    ps.executeUpdate();
                    System.out.println("Product inserted successfully!");
                } catch (SQLIntegrityConstraintViolationException e) {
                    System.out.println("Duplicate product ID: " + id);
                }
            }

        } catch (SQLException e) {
            System.out.println("Insert error: " + e.getMessage());
        }
    }

    // 2. Display products in a category with quantity below 20,
    // sorted by price in descending order
    static void displayProducts(Connection con) {

        String sql = "SELECT * FROM Product " +
                     "WHERE category = ? AND quantity < 20 " +
                     "ORDER BY price DESC";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            sc.nextLine();
            System.out.print("Enter category: ");
            String category = sc.nextLine();

            ps.setString(1, category);

            try (ResultSet rs = ps.executeQuery()) {

                boolean found = false;

                while (rs.next()) {
                    found = true;

                    System.out.println(
                        "ID: " + rs.getInt("product_id") +
                        ", Name: " + rs.getString("product_name") +
                        ", Category: " + rs.getString("category") +
                        ", Quantity: " + rs.getInt("quantity") +
                        ", Price: " + rs.getDouble("price")
                    );
                }

                if (!found) {
                    System.out.println("No matching products found.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Display error: " + e.getMessage());
        }
    }

    // 3. Add stock to an existing product
    static void addStock(Connection con) {

        String sql = "UPDATE Product " +
                     "SET quantity = quantity + ? WHERE product_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            System.out.print("Enter product ID: ");
            int id = sc.nextInt();

            System.out.print("Enter quantity to add: ");
            int quantity = sc.nextInt();

            if (quantity <= 0) {
                System.out.println("Quantity to add must be positive.");
                return;
            }

            ps.setInt(1, quantity);
            ps.setInt(2, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Stock added successfully!");
            } else {
                System.out.println("Product not found!");
            }

        } catch (SQLException e) {
            System.out.println("Stock error: " + e.getMessage());
        }
    }

    // 4. Update price only if product exists and price is positive
    static void updatePrice(Connection con) {

        System.out.print("Enter product ID: ");
        int id = sc.nextInt();

        System.out.print("Enter new price: ");
        double price = sc.nextDouble();

        if (price <= 0) {
            System.out.println("Price must be greater than zero.");
            return;
        }

        String sql = "UPDATE Product SET price = ? WHERE product_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDouble(1, price);
            ps.setInt(2, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Price updated successfully!");
            } else {
                System.out.println("Product not found!");
            }

        } catch (SQLException e) {
            System.out.println("Price update error: " + e.getMessage());
        }
    }

    // 5. Calculate total inventory value
    static void calculateInventoryValue(Connection con) {

        String sql =
            "SELECT SUM(quantity * price) AS total_value FROM Product";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                double total = rs.getDouble("total_value");
                System.out.println("Total inventory value: " + total);
            }

        } catch (SQLException e) {
            System.out.println("Calculation error: " + e.getMessage());
        }
    }

    // 6. Delete a product only if quantity is zero
    static void deleteProduct(Connection con) {

        String sql =
            "DELETE FROM Product WHERE product_id = ? AND quantity = 0";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            System.out.print("Enter product ID to delete: ");
            int id = sc.nextInt();

            ps.setInt(1, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Product deleted successfully!");
            } else {
                System.out.println(
                    "Product not found or quantity is not zero."
                );
            }

        } catch (SQLException e) {
            System.out.println("Delete error: " + e.getMessage());
        }
    }
}
