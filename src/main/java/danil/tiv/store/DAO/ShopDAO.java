package danil.tiv.store.DAO;

import danil.tiv.store.entities.Customer;
import danil.tiv.store.entities.StatCustomer;
import danil.tiv.store.entities.StatPurchase;
import danil.tiv.store.entities.StatResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class ShopDAO {
    private Connection connection;

    public ShopDAO(Connection connection) {
        this.connection = connection;
    }

    public List<Customer> findByLastName(String lastName) throws SQLException {
        List<Customer> customers = new ArrayList<>();

        String query = "SELECT id, first_name, last_name FROM customers WHERE last_name = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, lastName);

            resultSet(customers, statement);
        }

        return customers;
    }

    public List<Customer> findByProductAndMinTimes(String productName, int minTimes) throws SQLException {
        List<Customer> customers = new ArrayList<>();

        String query = "SELECT c.id, c.first_name, c.last_name " +
                "FROM customers c " +
                "JOIN purchases p ON c.id = p.customer_id " +
                "JOIN products pr ON p.product_id = pr.id " +
                "WHERE pr.name = ? " +
                "GROUP BY c.id, c.first_name, c.last_name " +
                "HAVING COUNT(p.id) >= ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, productName);
            statement.setInt(2, minTimes);

            resultSet(customers, statement);
        }

        return customers;
    }



    public List<Customer> findByExpenseRange(int minExpenses, int maxExpenses) throws SQLException {
        List<Customer> customers = new ArrayList<>();

        String query = "SELECT c.id, c.first_name, c.last_name " +
                "FROM customers c " +
                "JOIN purchases p ON c.id = p.customer_id " +
                "JOIN products pr ON p.product_id = pr.id " +
                "GROUP BY c.id, c.first_name, c.last_name " +
                "HAVING SUM(pr.price) BETWEEN ? AND ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, minExpenses);
            statement.setInt(2, maxExpenses);

            resultSet(customers, statement);
        }

        return customers;
    }

    public List<Customer> findBadCustomers(int limit) throws SQLException {
        List<Customer> customers = new ArrayList<>();

        String query = "SELECT c.id, c.first_name, c.last_name, COUNT(p.id) as purchase_count " +
                "FROM customers c " +
                "LEFT JOIN purchases p ON c.id = p.customer_id " +
                "GROUP BY c.id, c.first_name, c.last_name " +
                "ORDER BY purchase_count ASC " +
                "LIMIT ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, limit);

            resultSet(customers, statement);
        }

        return customers;
    }

    public StatResult calculateStat(LocalDate startDate, LocalDate endDate) throws SQLException {
        // Рассчёт общего числа дней за период
        int totalDays = (int) (ChronoUnit.DAYS.between(startDate, endDate) + 1);

        // Получение общей суммы расходов и средней суммы расходов
        int totalExpenses = 0;
        int avgExpenses = 0;
        List<StatCustomer> customers = new ArrayList<>();

        // Подсчёт общей суммы покупок
        String totalExpensesQuery = "SELECT SUM(pr.price) as total_expenses " +
                "FROM purchases p " +
                "JOIN products pr ON p.product_id = pr.id " +
                "WHERE p.purchase_date BETWEEN ? AND ?";

        try (PreparedStatement statement = connection.prepareStatement(totalExpensesQuery)) {
            statement.setDate(1, java.sql.Date.valueOf(startDate));
            statement.setDate(2, java.sql.Date.valueOf(endDate));
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    totalExpenses = resultSet.getInt("total_expenses");
                }
            }
        }

        // Подсчёт количества уникальных покупателей
        int customerCount = 0;
        if (totalExpenses > 0) {
            String customerCountQuery = "SELECT COUNT(DISTINCT customer_id) as customer_count " +
                    "FROM purchases " +
                    "WHERE purchase_date BETWEEN ? AND ?";
            try (PreparedStatement statement = connection.prepareStatement(customerCountQuery)) {
                statement.setDate(1, java.sql.Date.valueOf(startDate));
                statement.setDate(2, java.sql.Date.valueOf(endDate));
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        customerCount = resultSet.getInt("customer_count");
                        if (customerCount > 0) {
                            avgExpenses = totalExpenses / customerCount;
                        }
                    }
                }
            }
        }

        // Получение данных о покупателях и их расходах
        String customersQuery = "SELECT c.id, c.first_name, c.last_name, " +
                "SUM(pr.price) as total_expenses " +
                "FROM customers c " +
                "JOIN purchases p ON c.id = p.customer_id " +
                "JOIN products pr ON p.product_id = pr.id " +
                "WHERE p.purchase_date BETWEEN ? AND ? " +
                "GROUP BY c.id, c.first_name, c.last_name " +
                "ORDER BY total_expenses DESC";

        try (PreparedStatement statement = connection.prepareStatement(customersQuery)) {
            statement.setDate(1, java.sql.Date.valueOf(startDate));
            statement.setDate(2, java.sql.Date.valueOf(endDate));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String customerName = resultSet.getString("first_name") + " " + resultSet.getString("last_name");
                    int customerTotalExpenses = resultSet.getInt("total_expenses");

                    // Получение покупок каждого клиента
                    List<StatPurchase> purchases = new ArrayList<>();
                    String purchasesQuery = "SELECT pr.name, SUM(pr.price) as total_expenses " +
                            "FROM purchases p " +
                            "JOIN products pr ON p.product_id = pr.id " +
                            "WHERE p.customer_id = ? AND p.purchase_date BETWEEN ? AND ? " +
                            "GROUP BY pr.name " +
                            "ORDER BY total_expenses DESC";

                    try (PreparedStatement purchasesStatement = connection.prepareStatement(purchasesQuery)) {
                        purchasesStatement.setInt(1, resultSet.getInt("id"));
                        purchasesStatement.setDate(2, java.sql.Date.valueOf(startDate));
                        purchasesStatement.setDate(3, java.sql.Date.valueOf(endDate));
                        try (ResultSet purchasesResultSet = purchasesStatement.executeQuery()) {
                            while (purchasesResultSet.next()) {
                                purchases.add(new StatPurchase(
                                        purchasesResultSet.getString("name"),
                                        purchasesResultSet.getInt("total_expenses")));
                            }
                        }
                    }

                    customers.add(new StatCustomer(
                            customerName,
                            purchases,
                            customerTotalExpenses));
                }
            }
        }

        return new StatResult(
                totalDays,
                totalExpenses,
                avgExpenses,
                customers);
    }



    private void resultSet(List<Customer> customers, PreparedStatement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Customer customer = new Customer();
                customer.setId(resultSet.getInt("id"));
                customer.setFirstName(resultSet.getString("first_name"));
                customer.setLastName(resultSet.getString("last_name"));

                customers.add(customer);
            }
        }
    }
}
