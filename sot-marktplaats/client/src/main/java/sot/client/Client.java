package sot.client;

import org.glassfish.jersey.SslConfigurator;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import sot.exceptions.ConnectionException;
import sot.exceptions.EmptyCollectionException;
import sot.exceptions.GetObjectException;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

import static java.lang.System.exit;

public class Client {

    private static File getFile(String fileName) throws URISyntaxException {
        ClassLoader classLoader = Client.class.getClassLoader();
        URL url = classLoader.getResource(fileName);
        if (url == null) {
            throw new RuntimeException("Cannot open file " + fileName);
        }
        return new File(url.toURI());
    }

    public static void main(String[] args) throws URISyntaxException {

        WebTarget serviceTarget = null;

        //Log in
        String username = "";
        String password = "";
        String action = "";
        try {
            do {
                try {
                    var loginAndPassword = logInOrRegister();
                    username = loginAndPassword.get(0);
                    password = loginAndPassword.get(1);
                    action = loginAndPassword.get(2);
                } catch (EmptyCollectionException e) {
                    //e.printStackTrace();
                }

                // Connection configuration
                final File keyStore = getFile("keystore_client");
                final File trustStore = getFile("truststore_client");

                System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");

                SslConfigurator sslConfig = SslConfigurator.newInstance()
                        .keyStoreFile(keyStore.getAbsolutePath())
                        .keyStorePassword("asdfgh")
                        .trustStoreFile(trustStore.getAbsolutePath())
                        .trustStorePassword("asdfgh")
                        .keyPassword("asdfgh");

                final SSLContext sslContext = sslConfig.createSSLContext();

                ClientConfig config = new ClientConfig();

                config.register(HttpAuthenticationFeature.basic(username, password));

                javax.ws.rs.client.Client client = ClientBuilder.newBuilder().withConfig(config)
                        .sslContext(sslContext).build();

                URI baseURI = UriBuilder.fromUri("https://localhost:9090/shop").build();
                serviceTarget = client.target(baseURI);

            } while (!hello(serviceTarget));
        } catch (ConnectionException e) {
            System.out.println("There is problem connecting with the database");
            //e.printStackTrace();
        }

        //Allow to register if needed
        if (action.equals("register")) {
            registerView(serviceTarget);
        }

        // Show the basic information on products and operations
        try {
            while (mainView(serviceTarget, getUserId(serviceTarget ,username, password))) {
            }
        } catch (EmptyCollectionException e) {
            System.out.println("Wrong customer ID");
            //e.printStackTrace();
        }

    }

    private static ArrayList<String> logInOrRegister() throws EmptyCollectionException {
        String check;
        do {
            System.out.print("Do you have an account? [Y/n] ");
            check = readInput();
        } while (!check.equals("Y") && !check.equals("n"));

        if (check.equals("n")) {
            do {
                System.out.print("Do you wish to quit? [Y/n] ");
                check = readInput();
            } while (!check.equals("Y") && !check.equals("n"));

            if (check.equals("Y")) {
                exit(0);
            } else {
                return register();
            }
        } else {
            return logIn();
        }
        throw new EmptyCollectionException();
    }

    private static ArrayList<String> logIn() {
        ArrayList<String> toReturn = new ArrayList<>();
        String check;

        do {
            System.out.print("Username: ");
            check = readInput();
        } while (isNumeric(check));
        String username = check;

        System.out.print("Password: ");
        String password = readInput();

        toReturn.add(username);
        toReturn.add(password);
        toReturn.add("login");

        return toReturn;
    }

    private static ArrayList<String> register() {
        ArrayList<String> toReturn = new ArrayList<>();

        String username = "login";
        String password = "password";

        toReturn.add(username);
        toReturn.add(password);
        toReturn.add("register");


        return toReturn;
    }

    public static ArrayList<Customer> getCustomers(WebTarget serviceTarget) throws EmptyCollectionException {
        Invocation.Builder requestBuilder = serviceTarget.path("/customers/all").request().accept(MediaType.APPLICATION_JSON);
        Response response = requestBuilder.get();

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            GenericType<ArrayList<Customer>> genericType = new GenericType<>() {};
            ArrayList<Customer> entity = response.readEntity(genericType);
            return entity;
        } else {
            System.err.println("ERROR: Cannot get all! " + response);
            String entity = response.readEntity(String.class);
            System.err.println(entity);
            throw new EmptyCollectionException();
        }
    }

    public static boolean mainView(WebTarget serviceTarget, int userId) {
        String strBuild = "\nThere are " + countProd(serviceTarget) + " products:";
        System.out.println(strBuild);

        ArrayList<Product> products = new ArrayList<>();
        Customer user = new Customer();
        try {
            products = getProducts(serviceTarget);
        } catch (EmptyCollectionException e) {
            System.out.println("Cannot perform GET operation");
            //e.printStackTrace();
        }

        try {
            user = getCustomer(serviceTarget, userId);
        } catch (GetObjectException e) {
            System.out.println("Cannot perform GET operation");
            //e.printStackTrace();
        }
        System.out.println("Your id is: " + userId +
                "\nYou have: " + user.getWallet() + " Euro");
        displayProducts(products);

        commandsMenu();

        String userInput = readInput();

        return crudDecision(serviceTarget, userId, userInput);
    }

    public static void registerView(WebTarget serviceTarget) {
        System.out.println("To create a new account give the credentials.");
        var creds = logIn();
        String username = creds.get(0);
        String password = creds.get(1);
        addCustomerForm(serviceTarget, username, password);
        exit(0);
    }

    private static Customer getCustomer(WebTarget serviceTarget, int userId) throws GetObjectException {
        // Path parameter
        Invocation.Builder requestBuilder = serviceTarget.path("/customers/" + userId).request().accept(MediaType.APPLICATION_JSON);
        Response response = requestBuilder.get();

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            Customer entity = response.readEntity(Customer.class);
            //System.out.println("The service response is: " + entity);
            return entity;
        } else {
            System.err.println("ERROR: Cannot get path param! " + response);
            String entity = response.readEntity(String.class);
            System.err.println(entity);
            throw new GetObjectException();
        }
    }

    public static String readInput() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            return reader.readLine();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return " ";
        }
    }

    public static boolean crudDecision(WebTarget serviceTarget, int userId, String userInput) {
        int prodNum = 0;
        ArrayList<String> params;
        ArrayList<Product> prods;
        switch (userInput) {
            // Add product
            case "+":
                params = addDialog();
                addProductForm(serviceTarget, userId, Integer.parseInt(params.get(0)), params.get(1), params.get(2));
                return true;

            // Remove product
            case "-":
                String check;
                try {
                    do {
                        deleteMenu();
                        check = readInput();
                    } while (!isExistingId(serviceTarget, check));
                    prodNum = Integer.parseInt(check);
                } catch (EmptyCollectionException e) {
                    System.out.println("Could not get products list");
                    //e.printStackTrace();
                }
                int prodPrice = 0;
                int prodOwnerId = 0;
                try {
                    prodPrice = Objects.requireNonNull(getProduct(serviceTarget, userId)).getPrice();
                    prodOwnerId = Objects.requireNonNull(getProduct(serviceTarget, userId)).getOwnerId();
                } catch (GetObjectException e) {
                    System.out.println("Can not get the product from the server");
                    //e.printStackTrace();
                }
                System.out.println("Buying product for " + prodPrice + " Euro from user nr " + prodOwnerId + ".");
                // Take money from one customer
                incrWallet(serviceTarget, userId, -1 * prodPrice);
                // Give money to other customer
                incrWallet(serviceTarget, prodOwnerId, prodPrice);
                deleteProduct(serviceTarget, prodNum);
                return true;

            // Edit product
            case ".":
                params = editDialog(serviceTarget, userId);
                editProduct(serviceTarget, Integer.parseInt(params.get(0)), userId, Integer.parseInt(params.get(1)),
                        params.get(2), params.get(3));
                return true;

            // Filter products
            case ",":
                String filter = filterDialog();
                ArrayList<Product> products = new ArrayList<>();
                try {
                    products = getFilteredProducts(serviceTarget, filter);
                } catch (EmptyCollectionException e) {
                    System.out.println("No products like that");
                    ////e.printStackTrace();
                }

                if (products != null) {
                    displayProducts(products);
                    System.out.println();
                }
                return true;

            // Quit
            case "q":
                System.out.println("Bye!");
                return false;

            // Delete all products
            case "Erase":
                deleteAllProducts(serviceTarget);
                return true;

            // Do nothing
            default:
                System.out.println("Not a valid character. Please try again");
                clearScreen();
                return true;
        }
    }

    public static void commandsMenu() {

        System.out.println("\nAdd: '+' |" +
                " Buy: '-' |" +
                " Edit: '.' |" +
                " Quit: 'q' |" +
                " Filter: ','");
        System.out.print("Waiting for your input: ");

    }

    public static boolean hello(WebTarget serviceTarget) throws ConnectionException {
        Invocation.Builder requestBuilder = serviceTarget.path("/products/hello").request().accept(MediaType.TEXT_PLAIN);
        Response response = requestBuilder.get();

        if (response.getStatus() == 200) {
            String entity = response.readEntity(String.class);
            System.out.println(entity);
            return true;
        } else if(response.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()) {
            System.out.println("Wrong username or password!");
            return false;
        } else {
            System.err.println("ERROR: Cannot get hello! " + response);
            String entity = response.readEntity(String.class);
            System.err.println(entity);
            throw new ConnectionException();
        }
    }

    public static int countProd(WebTarget serviceTarget) {
        Invocation.Builder requestBuilder = serviceTarget.path("/products/count").request().accept(MediaType.TEXT_PLAIN);
        Response response = requestBuilder.get();

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            Integer entity = response.readEntity(Integer.class);
            return entity;
        } else {
            System.err.println("ERROR: Cannot get count! " + response);
            String entity = response.readEntity(String.class);
            System.err.println(entity);
            return -1;
        }
    }

    public static ArrayList<Product> getProducts(WebTarget serviceTarget) throws EmptyCollectionException {
        Invocation.Builder requestBuilder = serviceTarget.path("/products/all").request().accept(MediaType.APPLICATION_JSON);
        Response response = requestBuilder.get();

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            GenericType<ArrayList<Product>> genericType = new GenericType<>() {};
            ArrayList<Product> entity = response.readEntity(genericType);
            return entity;
        } else {
            //System.err.println("ERROR: Cannot get all! " + response);
            String entity = response.readEntity(String.class);
            System.err.println(entity);
            throw new EmptyCollectionException();
        }
    }

    public static Product getProduct(WebTarget serviceTarget, int index) throws GetObjectException {
        // Path parameter
        Invocation.Builder requestBuilder = serviceTarget.path("/products/" + index).request().accept(MediaType.APPLICATION_JSON);
        Response response = requestBuilder.get();

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            Product entity = response.readEntity(Product.class);
            //System.out.println("The service response is: " + entity);
            return entity;
        } else {
            System.err.println("ERROR: Cannot get path param! " + response);
            String entity = response.readEntity(String.class);
            System.err.println(entity);
            throw new GetObjectException();
        }
    }

    public static void displayProducts(ArrayList<Product> products) {
        System.out.format("%-1s", " | ");
        System.out.format("%-10s","Product ID");
        System.out.format("%-1s", " | ");
        System.out.format("%-45s", "Name");
        System.out.format("%-1s", " | ");
        System.out.format("%-15s", "Price");
        System.out.format("%-1s", " | ");
        System.out.format("%-10s", "Owner ID");
        System.out.format("%-1s", " | ");
        System.out.format("%-20s", "Description");

        for (Product product : products) {
            System.out.println();
            System.out.format("%-1s", " | ");
            System.out.format("%-10s",product.getId() + ". ");
            System.out.format("%-1s", " | ");
            System.out.format("%-45s",product.getName() + " ");
            System.out.format("%-1s", " | ");
            System.out.format("%-15s",product.getPrice() + " ");
            System.out.format("%-1s", " | ");
            System.out.format("%-10s",product.getOwnerId() + " ");
            System.out.format("%-1s", " | ");
            System.out.format("%-20s",product.getDescription());
        }
    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void deleteMenu() {
        System.out.print("Give the number of product to buy: ");

    }

    public static void deleteProduct(WebTarget serviceTarget, int prodNum) {
        WebTarget resourceTarget = serviceTarget.path("/products/" + prodNum);
        Invocation.Builder requestBuilder = resourceTarget.request().accept(MediaType.TEXT_PLAIN);
        Response response = requestBuilder.delete();

        if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
            System.out.println("Bought product " + prodNum +" successfully.");
        } else {
            System.err.println("ERROR: Cannot delete product " + prodNum + "! " + response);
            String entity = response.readEntity(String.class);
            System.err.println(entity);
        }
    }

    public static ArrayList<String> addDialog() {
        String check;

        do {
            System.out.print("Give the price of a product: ");
            check = readInput();
        } while (!isNumeric(check));
        String price = check;

        do {
            System.out.print("Give the name of a product: ");
            check = readInput();
        } while (isNumeric(check));
        String name = check;

        System.out.print("Give the description of a product: ");
        String desc = readInput();

        ArrayList<String> toReturn = new ArrayList<>();
        toReturn.add(price);
        toReturn.add(name);
        toReturn.add(desc);
        return toReturn;
    }

    public static void addProduct(WebTarget serviceTarget, int ownerId, int price, String name, String description) {
        int newId = -1;
        try {
            newId = genProdNum(serviceTarget);
        } catch (EmptyCollectionException e) {
            System.out.println("Could not get products from database");
            ////e.printStackTrace();
        }
        Product product = new Product(newId, ownerId, price, name, description);
        Entity<Product> productEntity = Entity.entity(product, MediaType.APPLICATION_JSON);

        Response response = serviceTarget.path("/products/").request().accept(MediaType.TEXT_PLAIN).post(productEntity);

        if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
            System.out.println("Created product with id " + newId + "!");
        } else {
            System.err.println("ERROR: Cannot create product " + newId + "!" + response);
            String entity = response.readEntity(String.class);
            System.err.println(entity);
        }
    }

    public static void addProductForm(WebTarget serviceTarget, int ownerId, int price, String name, String description) {
        Form form = new Form();
        form.param("price", String.valueOf(price));
        form.param("ownerId", String.valueOf(ownerId));
        form.param("name", name);
        form.param("desc", description);
        Entity<Form> formEntity = Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED);

        Response response = serviceTarget.path("/products/").request().accept(MediaType.TEXT_PLAIN).post(formEntity);

        if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
            String studentUrl = response.getHeaderString("Location");
            //System.out.println("Post product is created and can be accessed at: " + studentUrl);
            System.out.println("Added product successfully");
        } else {
            System.err.println("ERROR: Post formal param " + response);
            String entity = response.readEntity(String.class);
            System.err.println(entity);
        }
    }

    public static void addCustomerForm(WebTarget serviceTarget, String username, String password) {
        Form form = new Form();
        form.param("username", username);
        form.param("password", password);
        Entity<Form> formEntity = Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED);

        Response response = serviceTarget.path("/customers/").request().accept(MediaType.TEXT_PLAIN).post(formEntity);

        if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
            String studentUrl = response.getHeaderString("Location");
            //System.out.println("Post product is created and can be accessed at: " + studentUrl);
            System.out.println("Added customer successfully. Restart the client to login");
        } else {
            System.err.println("ERROR: Post formal param " + response);
            String entity = response.readEntity(String.class);
            System.err.println(entity);
        }
    }

    private static int genProdNum(WebTarget serviceTarget) throws EmptyCollectionException {
        int maxId = 0;
        var products = getProducts(serviceTarget);
        for (Product p: products) {
            if (p.getId() > maxId) {
                maxId = p.getId();
            }
        }
        return (maxId + 1);
    }

    private static ArrayList<String> editDialog(WebTarget serviceTarget, int userId) {
        String check;
        Product prod;
        String id = null;
        try {

            do {
                do {
                    System.out.print("Choose the product to be edited: ");
                    check = readInput();
                } while (!isExistingId(serviceTarget, check));
                prod = getProduct(serviceTarget, Integer.parseInt(check));
            } while (!isOwner(prod, userId));
            id = check;
        } catch (GetObjectException e) {
            System.out.println("Could not get product");
            //e.printStackTrace();
        } catch (EmptyCollectionException e) {
            System.out.println("Could not get products");
            //e.printStackTrace();
        }

        do {
            System.out.print("Give new price of a product: ");
            check = readInput();
        } while (!isNumeric(check));
        String price = check;

        do {
            System.out.print("Give new name of a product: ");
            check = readInput();
        } while (isNumeric(check));
        String name = check;

        System.out.print("Give new description of a product: ");
        String desc = readInput();

        ArrayList<String> toReturn = new ArrayList<>();
        toReturn.add(id);
        toReturn.add(price);
        toReturn.add(name);
        toReturn.add(desc);
        return toReturn;
    }

    private static void editProduct(WebTarget serviceTarget, int id, int ownerId, int price, String name, String desc) {
        Product product = new Product(id, ownerId, price, name, desc);
        Entity<Product> productEntity = Entity.entity(product, MediaType.APPLICATION_JSON);

        Response response = serviceTarget.path("/products/").request().accept(MediaType.TEXT_PLAIN).put(productEntity);

        if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
            System.out.println("Updated product " + id + "!");
        } else {
            System.err.println("ERROR: Cannot edit product " + id + "! " + response);
            String entity = response.readEntity(String.class);
            System.err.println(entity);
        }
    }

    private static String filterDialog() {
        String check;
        do {
            System.out.print("Give the string to sort by (no spaces): ");
            check = readInput();
        } while (check.contains(" ") || check.isEmpty());
        String filter = check;
        return filter;
    }

    private static ArrayList<Product> getFilteredProducts(WebTarget serviceTarget, String name) throws EmptyCollectionException {
        Invocation.Builder requestBuilder = serviceTarget.queryParam("name", name).path("/products/").request().accept(MediaType.APPLICATION_JSON);
        Response response = requestBuilder.get();

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            GenericType<ArrayList<Product>> genericType = new GenericType<>(){};
            ArrayList<Product> entity = response.readEntity(genericType);
            System.out.println("Presenting all " + name + "s: ");
            return entity;
        } else if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
            System.out.println("There are no products with this name!");
        } else {
            System.err.println("ERROR: Cannot get query param! " + response);
            String entity = response.readEntity(String.class);
            System.err.println(entity);
        }
        throw new EmptyCollectionException();
    }

    private static boolean isNumeric(String input) {
        try {
            Integer.parseInt(input);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    private static boolean isExistingId(WebTarget serviceTarget, String id) throws EmptyCollectionException {
        if (isNumeric(id)) {
            int prodNum = Integer.parseInt(id);
            var prods = getProducts(serviceTarget);
            if (prods != null) {
                for (Product p : prods) {
                    if (prodNum == p.getId()) {
                        return true;
                    }
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
        return false;
    }

    private static int getUserId(WebTarget serviceTarget, String username, String password) throws EmptyCollectionException {
        var custs = getCustomers(serviceTarget);
        if (custs != null) {
            for (Customer c: custs) {
                //System.out.println(c.getId());
                if (username.equals(c.getUsername()) && password.equals(c.getPassword())) {
                    return c.getId();
                }
            }
        }
        return -1;
    }

    private static boolean isOwner(Product p, int cId) {
        return p.getOwnerId() == cId;
    }

    private static void incrWallet(WebTarget serviceTarget, int cId, int wallet) {

        Form form = new Form();
        form.param("cId", String.valueOf(cId));
        form.param("wallet", String.valueOf(wallet));
        Entity<Form> formEntity = Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED);

        Response response = serviceTarget.path("/customers/").request().accept(MediaType.TEXT_PLAIN).put(formEntity);

        if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
            //System.out.println("Customer wallet updated");
        } else {
            System.err.println("ERROR: Post formal param " + response);
            String entity = response.readEntity(String.class);
            System.err.println(entity);
        }
    }

    private static void deleteAllProducts(WebTarget serviceTarget) {
        WebTarget resourceTarget = serviceTarget.path("/products/");
        Invocation.Builder requestBuilder = resourceTarget.request().accept(MediaType.TEXT_PLAIN);
        Response response = requestBuilder.delete();

        if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
            System.out.println("Deleted all products.");
        } else {
            System.err.println("ERROR: Cannot delete all products " + response);
            String entity = response.readEntity(String.class);
            System.err.println(entity);
        }
    }

    private static void deleteAllCustomers(WebTarget serviceTarget) {
        WebTarget resourceTarget = serviceTarget.path("/customers/");
        Invocation.Builder requestBuilder = resourceTarget.request().accept(MediaType.TEXT_PLAIN);
        Response response = requestBuilder.delete();

        if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
            System.out.println("Deleted all customers.");
        } else {
            System.err.println("ERROR: Cannot delete all customers " + response);
            String entity = response.readEntity(String.class);
            System.err.println(entity);
        }
    }

}
