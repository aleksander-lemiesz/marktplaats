package sot.service.memory;

import sot.service.exception.GetObjectException;
import sot.service.model.Customer;
import sot.service.model.Product;

import java.util.ArrayList;
import java.util.List;

public class FakeDataStore {

    public static FakeDataStore instance = new FakeDataStore();
    private final List<Product> products = new ArrayList<>();
    private final List<Customer> customers = new ArrayList<>();

    private FakeDataStore() {
        System.out.println("New FakeDataStore()");

        var login = new Customer(0, "login", "password");
        login.setRole("GUEST");
        customers.add(login);
        customers.add(new Customer(1, "Asia", "qwe"));
        customers.add(new Customer(2, "q", "1"));
        customers.add(new Customer(3, "Thomas", "1"));
        customers.add(new Customer(4, "Adam", "1"));
        customers.add(new Customer(5, "Eve", "1"));
        customers.add(new Customer(6, "Alex", "123"));
        var kate = new Customer(7, "Kate", "1");
        kate.setRole("ADMIN");
        customers.add(kate);

        products.add(new Product(1, customers.get(1).getId(), 15, "Leather Jacket", "Men's wear, M"));
        products.add(new Product(2, customers.get(1).getId(), 30, "Sneakers", "39, Women's wear"));
        products.add(new Product(3, customers.get(2).getId(), 10, "Hat", "Leather hat"));
        products.add(new Product(4, customers.get(4).getId(), 8, "Hoodie", " Red, size M"));
        products.add(new Product(5, customers.get(3).getId(), 20, "Jeans", "size XL"));
        products.add(new Product(6, customers.get(5).getId(), 13, "Book", "Dune"));
        products.add(new Product(7, customers.get(6).getId(), 11, "Headset", "black, wireless"));
        products.add(new Product(8, customers.get(4).getId(), 18, "Bag", "Leather, brown bag"));
        products.add(new Product(9, customers.get(1).getId(), 5, "T-Shirt", "Black, M"));
    }

    public static FakeDataStore singleton() {
        return instance;
    }

    public int getSize() {
        return products.size();
    }

    public List<Product> getProducts() {
        return new ArrayList<>(products);
    }

    public void delete(Product product) {
        products.remove(product);
    }

    public Product getProduct(int prodNum) throws GetObjectException {
        for (Product p: products) {
            if (p.getId() == prodNum) {
                return p;
            }
        }
        throw new GetObjectException();
    }

    public void add(Product product) {
        products.add(product);
    }

    public void editStudent(int id, int price, String name, String description) {
        for (Product p: products) {
            if (p.getId() == id) {
                p.setPrice(price);
                p.setName(name);
                p.setDescription(description);
            }
        }
    }

    public List<Product> getProducts(String name) {
            List<Product> filtered = new ArrayList<>();
            for (Product product : products) {
                String[] splitName = product.getName().split(" ");
                String properName = splitName[0];
                for (String part : splitName) {
                    if (part.equals(name)) {
                        filtered.add(product);
                        break;
                    }
                }
            }
            return filtered;
    }

    public int genProdNum() {
        int maxId = 0;
        for (Product p: products) {
            if (p.getId() > maxId) {
                maxId = p.getId();
            }
        }
        return maxId + 1;
    }

    public List<Customer> getCustomers() {
        return new ArrayList<>(customers);
    }

    public int genCustNum() {

        int maxId = 0;
        for (Customer c: customers) {
            if (c.getId() > maxId) {
                maxId = c.getId();
            }
        }
        return maxId + 1;
    }

    public void add(Customer customer) {
        customers.add(customer);
    }

    public void editCustomer(int id, String username, String password, int wallet) {
        for (Customer c: customers) {
            if (c.getId() == id) {
                c.setUsername(username);
                c.setPassword(password);
                c.setWallet(wallet);
            }
        }
    }

    public void incrWallet(int cId, int wallet) {
        for (Customer c : customers) {
            if (c.getId() == cId) {
                c.increaseWallet(wallet);
            }
        }
    }

    public Customer getCustomer(int id) throws GetObjectException {
        for (Customer c : customers) {
            if (c.getId() == id) {
                return c;
            }
        }
        throw new GetObjectException();
    }

    public void deleteAllProds() {
        products.clear();
    }

    public void deleteAllCusts() {
        customers.clear();
    }
}
