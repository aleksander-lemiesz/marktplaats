package sot.service.resources;

public class ShopLogic {

    public ProductRepository prodRep;

    public ShopLogic(ProductRepository prodRep) {
        this.prodRep = prodRep;
    }
}
