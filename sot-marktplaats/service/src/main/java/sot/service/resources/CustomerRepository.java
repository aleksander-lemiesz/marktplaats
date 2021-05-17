package sot.service.resources;

import sot.service.exception.GetObjectException;
import sot.service.model.Customer;

import java.util.List;

public interface CustomerRepository {

    public List<Customer> getCustomers();
    int genProdNum();
    void add(Customer customer);
    Customer getCustomer(int id) throws GetObjectException;
    void editCustomer(int id, String username, String password, int wallet);
    void incrWallet(int cId, int wallet);
    void deleteAll();
}
