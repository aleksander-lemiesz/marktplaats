package sot.service.memory;

import sot.service.exception.GetObjectException;
import sot.service.model.Customer;
import sot.service.resources.CustomerRepository;

import java.util.List;

public class CustomerRepositoryMemory implements CustomerRepository {

    private FakeDataStore fakeDataStore;

    public CustomerRepositoryMemory() {
        this.fakeDataStore = FakeDataStore.singleton();
    }

    @Override
    public List<Customer> getCustomers() {
        return fakeDataStore.getCustomers();
    }

    @Override
    public int genProdNum() {
        return fakeDataStore.genCustNum();
    }

    @Override
    public void add(Customer customer) {
        fakeDataStore.add(customer);
    }

    @Override
    public Customer getCustomer(int id) throws GetObjectException {
        return fakeDataStore.getCustomer(id);
    }

    @Override
    public void editCustomer(int id, String username, String password, int wallet) {
        fakeDataStore.editCustomer(id, username, password, wallet);
    }

    @Override
    public void incrWallet(int cId, int wallet) {
        fakeDataStore.incrWallet(cId, wallet);
    }

    @Override
    public void deleteAll() {
        fakeDataStore.deleteAllCusts();
    }
}
