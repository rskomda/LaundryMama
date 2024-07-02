package laundrymama.laundrymama;

public class HistoryModel {
    private int orderId;
    private String date;
    private String employeeName;
    private String customerName;
    private int total;

    public HistoryModel(int orderId, String date, String employeeName, String customerName, int total) {
        this.orderId = orderId;
        this.date = date;
        this.employeeName = employeeName;
        this.customerName = customerName;
        this.total = total;
    }

    public int getOrderId() {
        return orderId;
    }

    public String getDate() {
        return date;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public int getTotal() {
        return total;
    }
}
