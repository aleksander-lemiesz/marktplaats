package sot.service.resources;

public class CustomerLogic {
    public CustomerRepository custRepo;

    public CustomerLogic(CustomerRepository custRepo) {
        this.custRepo = custRepo;
    }
}
