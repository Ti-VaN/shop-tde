package danil.tiv;

import danil.tiv.store.DAO.ShopDAO;
import danil.tiv.store.service.ShopService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Wrong arguments counts");
            return;
        }

        String operationType = args[0];
        String inputFilePath = args[1];
        String outputFilePath = args[2];

        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/shop", "postgres", "zidegu24")) {
            ShopDAO shopDAO = new ShopDAO(connection);
            ShopService service = new ShopService(shopDAO);

            if ("search".equalsIgnoreCase(operationType)) {
                service.processSearch(inputFilePath, outputFilePath);
            } else if ("stat".equalsIgnoreCase(operationType)) {
                service.processStat(inputFilePath, outputFilePath);
            } else {
                System.out.println("Wrong key");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}