package sot.service.resources;

import sot.service.exception.GetObjectException;
import sot.service.model.Product;

import java.util.List;

public interface ProductRepository {

    int getSize();
    List<Product> getProducts();
    List<Product> getProducts(String name);
    void delete(Product product);
    Product getProduct(int stNr) throws GetObjectException;
    void add(Product product);
    void editProduct(int id, int price, String name, String description);
    int genProdNum();
    void deleteAll();
}
