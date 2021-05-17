package sot.service.resources;

import sot.service.exception.GetObjectException;
import sot.service.memory.CustomerRepositoryMemory;
import sot.service.model.Customer;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.List;

@Path("/customers")
public class CustomersResources {

    private final CustomerLogic logic;

    public CustomersResources() {
        logic = new CustomerLogic(new CustomerRepositoryMemory());
    }

    @Context
    private UriInfo uriInfo;

    @GET // GET at httplocalhost:XXXX/shop/customers/all
    @Path("all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllCustomers() {
        GenericEntity<List<Customer>> entity = new GenericEntity<>(logic.custRepo.getCustomers()){};
        return Response.ok(entity).build();
    }

    @GET // GET at httplocalhost:XXXX/shop/customers/1
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCustPath(@PathParam("id") int custNum) {
        try {

            Customer customer = logic.custRepo.getCustomer(custNum);
            if (customer == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(customer).build();
        } catch (GetObjectException e) {
            e.printStackTrace();
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    // POST with formal parameters
    @POST // POST at http://localhost:9090/shop/customers/
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response createCustomer(@FormParam("username") String username, @FormParam("password") String password) {
        int custNum = logic.custRepo.genProdNum();
        var customer = new Customer(custNum, username, password);
        logic.custRepo.add(customer);
        String url = uriInfo.getAbsolutePath() + "/" + customer.getId();
        URI uri = URI.create(url);
        return Response.created(uri).build();
    }

    // PUT
    @PUT // PUT at http://localhost:9090/shop/customers/
    @RolesAllowed({"ADMIN", "CUSTOMER"})
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateCustomer(Customer customer) {
        try {

            Customer customerExisting = logic.custRepo.getCustomer(customer.getId());
            if (customerExisting == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Not existing customer number.").build();
            }
            logic.custRepo.editCustomer(customer.getId(), customer.getUsername(), customer.getPassword(), customer.getWallet());
            return Response.noContent().build();
        } catch (GetObjectException e) {
            e.printStackTrace();
            return Response.status(Response.Status.NOT_FOUND).entity("Not existing customer number.").build();
        }
    }

    // PUT
    @PUT // PUT at http://localhost:9090/shop/customers/
    @RolesAllowed({"ADMIN", "CUSTOMER"})
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response updateCustomerWallet(@FormParam("cId") int cId, @FormParam("wallet") int wallet) {
        try {

            Customer customerExisting = logic.custRepo.getCustomer(cId);
            if (customerExisting == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Not existing customer number.").build();
            }
            logic.custRepo.incrWallet(cId, wallet);
            return Response.noContent().build();
        } catch (GetObjectException e) {
            e.printStackTrace();
            return Response.status(Response.Status.NOT_FOUND).entity("Not existing customer number.").build();
        }
    }

    // Delete
    @DELETE // delete at http://localhost:9090/shop/customers/
    @RolesAllowed("ADMIN")
    public Response deleteAllCustomers() {
        logic.custRepo.deleteAll();
        return Response.noContent().build();
    }

}
