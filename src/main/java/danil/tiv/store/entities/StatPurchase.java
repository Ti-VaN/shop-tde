package danil.tiv.store.entities;


public class StatPurchase {
    private String name;
    private int expenses;

    public StatPurchase(String name, int expenses) {
        this.name = name;
        this.expenses = expenses;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getExpenses() {
        return expenses;
    }

    public void setExpenses(int expenses) {
        this.expenses = expenses;
    }
}
