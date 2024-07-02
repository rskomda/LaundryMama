package laundrymama.laundrymama;

public class CustomerModel {
    private int customerID;
    private String customerName;
    private String customerPhone;
    private int orderAmount;

    public CustomerModel(int customerID, String customerName, String customerPhone, int orderAmount) {
        this.customerID = customerID;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.orderAmount = orderAmount;
    }

    public int getCustomerID() {
        return customerID;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public int getOrderAmount() {
        return orderAmount;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }
    
}
