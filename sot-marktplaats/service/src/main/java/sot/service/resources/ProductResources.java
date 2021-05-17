package sot.service.resources;

import sot.service.exception.GetObjectException;
import sot.service.memory.ProductRepositoryMemory;
import sot.service.model.Product;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.List;

@Path("/products")
public class ProductResources {

    private final ShopLogic logic;

    public ProductResources() {
        logic = new ShopLogic(new ProductRepositoryMemory());
    }

    @Context
    private UriInfo uriInfo;

    @GET // GET at httplocalhost:9090/shop/products/hello
    @Path("hello")
    @PermitAll
    @Produces(MediaType.TEXT_PLAIN)
    public Response sayHello() {
        String msg = "Hello, welcome in our second-hand shop!";
        return Response.ok(msg).build();
    }

    @GET // GET at httplocalhost:XXXX/shop/products/count
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getCount() {
        return Response.ok(logic.prodRep.getSize()).build();
    }

    @GET // GET at httplocalhost:XXXX/shop/products/all
    @Path("all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllProducts() {
        GenericEntity<List<Product>> entity = new GenericEntity<>(logic.prodRep.getProducts()){};
        return Response.ok(entity).build();
    }

    @GET // GET at httplocalhost:XXXX/shop/products/1
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStudentPath(@PathParam("id") int prodNum) {
        try {
            Product product = logic.prodRep.getProduct(prodNum);
            if (product == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(product).build();
        } catch (GetObjectException e) {
            e.printStackTrace();
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    // Delete
    @DELETE // delete at http://localhost:9090/shop/products/2
    @Path("{id}")
    @RolesAllowed({"ADMIN", "CUSTOMER"})
    public Response deleteProduct(@PathParam("id") int prodNum) {

        try {

            Product product = logic.prodRep.getProduct(prodNum);
            if (product == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            logic.prodRep.delete(product);

            return Response.noContent().build();
        } catch (GetObjectException e) {
            e.printStackTrace();
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    // Post
    @POST // POST at http://localhost:9090/shop/products/
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({"ADMIN", "CUSTOMER"})
    public Response createProduct(Product product) {
        try {

            if (logic.prodRep.getProduct(product.getId()) != null) {
                String entity = "Product with number " + product.getId() + " already exists.";
                return Response.status(Response.Status.CONFLICT).entity(entity).build();
            }
            logic.prodRep.add(product);
            String url = uriInfo.getAbsolutePath() + "/" + product.getId();
            URI uri = URI.create(url);
            return  Response.created(uri).build();
        } catch (GetObjectException e) {
            e.printStackTrace();
            String entity = "Product with number " + product.getId() + " already exists.";
            return Response.status(Response.Status.CONFLICT).build();
        }
    }

    // PUT
    @PUT // PUT at http://localhost:9090/shop/products/
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({"ADMIN", "CUSTOMER"})
    public Response updateProduct(Product product) {
        try {

            Product studentExisting = logic.prodRep.getProduct(product.getId());
            if (studentExisting == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Not existing product number.").build();
            }
            logic.prodRep.editProduct(product.getId(), product.getPrice(), product.getName(), product.getDescription());
            return Response.noContent().build();
        } catch (GetObjectException e) {
            e.printStackTrace();
            return Response.status(Response.Status.NOT_FOUND).entity("Not existing product number.").build();
        }
    }

    @GET // GET at http://localhost:9090/shop/products?name=T-Shirt
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllProducts(@QueryParam("name") String name) {
        // if query parameter is missing return all students.
        if (!uriInfo.getQueryParameters().containsKey("name")) {
            GenericEntity<List<Product>> entity = new GenericEntity<>(logic.prodRep.getProducts()){};
            return Response.ok(entity).build();
        }
        // if query param is present, filter students based on name
        List<Product> filtered = logic.prodRep.getProducts(name); //getStudents(name);
        if (filtered.isEmpty()) {
            //return Response.status(Response.Status.BAD_REQUEST).entity("Please provide a valid name").build();
            return Response.status(Response.Status.NO_CONTENT).entity("No products with this name").build();
        }
        // return the filtered list of students
        GenericEntity<List<Product>> entity = new GenericEntity<>(filtered){};
        return Response.ok(entity).build();
    }

    // POST with formal parameters
    @POST // POST at http://localhost:9090/shop/products/
    @RolesAllowed({"ADMIN", "CUSTOMER"})
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response createProduct(@FormParam("price") int price, @FormParam("ownerId") int ownerId,
                                  @FormParam("name") String name, @FormParam("desc") String desc) {
        int prodNum = logic.prodRep.genProdNum();
        Product product = new Product(prodNum, ownerId, price, name, desc);
        logic.prodRep.add(product);
        String url = uriInfo.getAbsolutePath() + "/" + product.getId();
        URI uri = URI.create(url);
        return Response.created(uri).build();
    }

    // Delete
    @DELETE // delete at http://localhost:9090/shop/products/
    @RolesAllowed("ADMIN")
    public Response deleteAllProducts() {
        logic.prodRep.deleteAll();
        return Response.noContent().build();
    }

}
