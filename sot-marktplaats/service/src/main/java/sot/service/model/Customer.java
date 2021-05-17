package sot.service.model;

public class Customer {
    private int id;
    private String username;
    private String password;
    private int wallet;
    private String role = "CUSTOMER";

    public Customer() {
    }

    public Customer(int id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.wallet = 5;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getWallet() {
        return wallet;
    }

    public void setWallet(int wallet) {
        this.wallet = wallet;
    }

    public void increaseWallet(int toAdd) {
        this.wallet += toAdd;
    }

    public void decreaseWallet(int toSub) {
        this.wallet += toSub;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
