package danil.tiv.store.entities;

import java.util.List;

public class StatResult {
    private int totalDays;
    private int totalExpenses;
    private int avgExpenses;
    private List<StatCustomer> customers;

    public StatResult(int totalDays, int totalExpenses, int avgExpenses, List<StatCustomer> customers) {
        this.totalDays = totalDays;
        this.totalExpenses = totalExpenses;
        this.avgExpenses = avgExpenses;
        this.customers = customers;
    }

    public int getTotalDays() {
        return totalDays;
    }

    public void setTotalDays(int totalDays) {
        this.totalDays = totalDays;
    }

    public int getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(int totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public int getAvgExpenses() {
        return avgExpenses;
    }

    public void setAvgExpenses(int avgExpenses) {
        this.avgExpenses = avgExpenses;
    }

    public List<StatCustomer> getCustomers() {
        return customers;
    }

    public void setCustomers(List<StatCustomer> customers) {
        this.customers = customers;
    }
}
