package danil.tiv.store.entities;

import java.util.List;

public class StatCustomer {
    private String name;
    private List<StatPurchase> purchases;
    private int totalExpenses;

    public StatCustomer(String name, List<StatPurchase> purchases, int totalExpenses) {
        this.name = name;
        this.purchases = purchases;
        this.totalExpenses = totalExpenses;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<StatPurchase> getPurchases() {
        return purchases;
    }

    public void setPurchases(List<StatPurchase> purchases) {
        this.purchases = purchases;
    }

    public int getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(int totalExpenses) {
        this.totalExpenses = totalExpenses;
    }
}
