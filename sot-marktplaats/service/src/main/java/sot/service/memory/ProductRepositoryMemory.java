package sot.service.memory;

import sot.service.exception.GetObjectException;
import sot.service.model.Product;
import sot.service.resources.ProductRepository;

import java.util.List;

public class ProductRepositoryMemory implements ProductRepository {

    private FakeDataStore fakeDataStore;

    public ProductRepositoryMemory() {
        this.fakeDataStore = FakeDataStore.singleton();
    }

    @Override
    public int getSize() {
        return fakeDataStore.getSize();
    }

    @Override
    public List<Product> getProducts() {
        return fakeDataStore.getProducts();
    }

    @Override
    public List<Product> getProducts(String name) {
        return fakeDataStore.getProducts(name);
    }

    @Override
    public void delete(Product product) {
        fakeDataStore.delete(product);
    }

    @Override
    public Product getProduct(int prodNum) throws GetObjectException {
        return fakeDataStore.getProduct(prodNum);
    }

    @Override
    public void add(Product product) {
        fakeDataStore.add(product);
    }

    @Override
    public void editProduct(int id, int price, String name, String description) {
        fakeDataStore.editStudent(id, price, name, description);
    }

    @Override
    public int genProdNum() {
        return fakeDataStore.genProdNum();
    }

    @Override
    public void deleteAll() {
        fakeDataStore.deleteAllProds();
    }

}
