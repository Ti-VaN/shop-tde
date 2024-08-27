package danil.tiv.store.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import danil.tiv.store.DAO.ShopDAO;
import danil.tiv.store.entities.Customer;
import danil.tiv.store.entities.StatCustomer;
import danil.tiv.store.entities.StatPurchase;
import danil.tiv.store.entities.StatResult;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ShopService {
    private ShopDAO shopDAO;
    private ObjectMapper objectMapper;

    public ShopService(ShopDAO shopDAO) {
        this.shopDAO = shopDAO;
        this.objectMapper = new ObjectMapper();
    }

    // Метод для обработки поиска по критериям
    public void processSearch(String inputFilePath, String outputFilePath) {
        try {
            // Чтение входного JSON файла
            File inputFile = new File(inputFilePath);
            ObjectNode inputJson = (ObjectNode) objectMapper.readTree(inputFile);

            ArrayNode criterias = (ArrayNode) inputJson.get("criterias");
            ObjectNode outputJson = objectMapper.createObjectNode();
            outputJson.put("type", "search");
            ArrayNode results = outputJson.putArray("results");

            // Обработка каждого критерия поиска
            for (int i = 0; i < criterias.size(); i++) {
                ObjectNode criteria = (ObjectNode) criterias.get(i);
                ObjectNode resultObject = results.addObject();
                resultObject.set("criteria", criteria);
                ArrayNode customerResults = resultObject.putArray("results");

                processCriteria(criteria, customerResults);
            }

            // Запись результата в выходной JSON файл
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            objectMapper.writeValue(new File(outputFilePath), outputJson);

        } catch (IOException | SQLException e) {
            handleError(outputFilePath, e.getMessage());
        }
    }

    private void processCriteria(ObjectNode criteria, ArrayNode customerResults) throws SQLException {
        if (criteria.has("lastName")) {
            String lastName = criteria.get("lastName").asText();
            List<Customer> customers = shopDAO.findByLastName(lastName);
            addCustomersToResults(customers, customerResults);
        } else if (criteria.has("productName") && criteria.has("minTimes")) {
            String productName = criteria.get("productName").asText();
            int minTimes = criteria.get("minTimes").asInt();
            List<Customer> customers = shopDAO.findByProductAndMinTimes(productName, minTimes);
            addCustomersToResults(customers, customerResults);
        } else if (criteria.has("minExpenses") && criteria.has("maxExpenses")) {
            int minExpenses = criteria.get("minExpenses").intValue();
            int maxExpenses = criteria.get("maxExpenses").intValue();
            List<Customer> customers = shopDAO.findByExpenseRange(minExpenses, maxExpenses);
            addCustomersToResults(customers, customerResults);
        } else if (criteria.has("badCustomers")) {
            int limit = criteria.get("badCustomers").asInt();
            List<Customer> customers = shopDAO.findBadCustomers(limit);
            addCustomersToResults(customers, customerResults);
        }
    }

    private void addCustomersToResults(List<Customer> customers, ArrayNode customerResults) {
        for (Customer customer : customers) {
            customerResults.addObject()
                    .put("firstName", customer.getFirstName())
                    .put("lastName", customer.getLastName());
        }
    }

    // Метод для обработки статистики за период
    public void processStat(String inputFilePath, String outputFilePath) {
        try {
            // Чтение входного JSON файла
            File inputFile = new File(inputFilePath);
            ObjectNode inputJson = (ObjectNode) objectMapper.readTree(inputFile);

            // Получаем даты из входного файла
            String startDateStr = inputJson.get("startDate").asText();
            String endDateStr = inputJson.get("endDate").asText();
            LocalDate startDate = LocalDate.parse(startDateStr, DateTimeFormatter.ISO_DATE);
            LocalDate endDate = LocalDate.parse(endDateStr, DateTimeFormatter.ISO_DATE);

            // Получение данных статистики
            StatResult statResult = shopDAO.calculateStat(startDate, endDate);

            // Формирование выходного JSON файла
            ObjectNode outputJson = objectMapper.createObjectNode();
            outputJson.put("type", "stat");
            outputJson.put("totalDays", statResult.getTotalDays());
            outputJson.put("totalExpenses", statResult.getTotalExpenses());
            outputJson.put("avgExpenses", statResult.getAvgExpenses());

            ArrayNode customersNode = outputJson.putArray("customers");
            for (StatCustomer customer : statResult.getCustomers()) {
                ObjectNode customerNode = customersNode.addObject();
                customerNode.put("name", customer.getName());
                ArrayNode purchasesNode = customerNode.putArray("purchases");

                for (StatPurchase purchase : customer.getPurchases()) {
                    purchasesNode.addObject()
                            .put("name", purchase.getName())
                            .put("expenses", purchase.getExpenses());
                }

                customerNode.put("totalExpenses", customer.getTotalExpenses());
            }

            // Запись результата в выходной JSON файл
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            objectMapper.writeValue(new File(outputFilePath), outputJson);
        } catch (IOException | SQLException e) {
            // Обработка ошибок и запись в выходной файл
            handleError(outputFilePath, e.getMessage());
        }
    }

    private void handleError(String outputFilePath, String errorMessage) {
        try {
            ObjectNode errorJson = objectMapper.createObjectNode();
            errorJson.put("type", "error");
            errorJson.put("message", errorMessage);
            objectMapper.writeValue(new File(outputFilePath), errorJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
