package sot.service.resources;

import sot.service.memory.CustomerRepositoryMemory;
import sot.service.model.Customer;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.lang.reflect.Method;
import java.util.*;

public class AuthenticationFilter implements ContainerRequestFilter {
    /**
     - resourceInfo contains information about the requested operation (GET,
     PUT, POST …).
     - resourceInfo will be assigned/set automatically by the Jersey
     framework, you do not need to assign/set it.
     */
    @Context
    private ResourceInfo resourceInfo;

    // requestContext contains information about the HTTP request message
    @Override
    public void filter(ContainerRequestContext requestContext) {
        // here you will perform AUTHENTICATION and AUTHORIZATION
        /* if you want to abort this HTTP request, you do this:

      Response response = Response.status(Response.Status.UNAUTHORIZED).build();
      requestContext.abortWith(response);

        */

        // AUTHENTICATION:
        // 1. extract username and password from requestContext
        // 2. validate username and password (e.g., database)
        // 3. if invalid user, abort requestContext with UNAUTHORIZED response

        // AUTHORIZATION:
        // 1. extract allowed roles for requested operation from resourceInfo
        // 2. check if the user has one of these roles
        // 3. if not, abort requestContext with FORBIDDEN response

        final String AUTHORIZATION_PROPERTY = "Authorization";
        final String AUTHENTICATION_SCHEME = "Basic";

//Get request headers
        final MultivaluedMap<String, String> headers = requestContext.getHeaders();

        //Fetch authorization header
        final List<String> authorization = headers.get(AUTHORIZATION_PROPERTY);

        //If no authorization information present: abort with UNAUTHORIZED and stop
        if (authorization == null || authorization.isEmpty()) {
            Response response = Response.status(Response.Status.UNAUTHORIZED).
                    entity("Missing username and/or password.").build();
            requestContext.abortWith(response);
            return;
        }

        //Get encoded username and password
        final String encodedCredentials = authorization.get(0).replaceFirst(AUTHENTICATION_SCHEME + " ", "");

        //Decode username and password into one string
        String credentials = new String(Base64.getDecoder().decode(encodedCredentials.getBytes()));

        //Split username and password tokens in credentials
        final StringTokenizer tokenizer = new StringTokenizer(credentials, ":");
        final String username = tokenizer.nextToken();
        final String password = tokenizer.nextToken();

        //Check if username and password are valid (e.g., database)
        //If not valid: abort with UNAUTHORISED and stop
        if (!isValidUser(username, password)) {
            Response response = Response.status(Response.Status.UNAUTHORIZED).
                    entity("Invalid username and/or password.").build();
            requestContext.abortWith(response);
            return;
        }

        // AUTHORIZATION

        /* Get information about the service method which is being called. This information includes the annotated/permitted roles. */
        Method method = resourceInfo.getResourceMethod();
// if access is allowed for all -> do not check anything further : access is approved for all
        if (method.isAnnotationPresent(PermitAll.class)) {
            return;
        }

// if access is denied for all: deny access
        if (method.isAnnotationPresent(DenyAll.class)) {
            Response response = Response.status(Response.Status.FORBIDDEN).build();
            requestContext.abortWith(response);
            return;
        }

/* here you do
    1. the AUTHENTICATION first (as explained in previous sections), and
    2. if AUTHENTICATION succeeds, you do the authorization like this:
*/
        if (method.isAnnotationPresent(RolesAllowed.class)) {
            // get allowed roles for this method
            RolesAllowed rolesAnnotation = method.getAnnotation(RolesAllowed.class);
            Set<String> rolesSet = new HashSet<String>(Arrays.asList(rolesAnnotation.value()));

    /* isUserAllowed : implement this method to check if this user has any of
                       the roles in the rolesSet
       if not isUserAllowed abort the requestContext with FORBIDDEN response*/
            if (!isUserAllowed(username, password, rolesSet)) {
                Response response = Response.status(Response.Status.FORBIDDEN).build();
                requestContext.abortWith(response);
                return;
            }


        }
    }

    private boolean isUserAllowed(String username, String password, Set<String> rolesSet) {
        // Get user status
        var custsRepo = new CustomerRepositoryMemory();
        var customers = custsRepo.getCustomers();
        Customer customer = null;
        if (customers != null) {
            for (Customer c : customers) {
                if (username.equals(c.getUsername()) && password.equals(c.getPassword())) {
                    customer = c;
                }
            }
        }

        // check user permissions
        String userPermissions = customer.getRole();
        return rolesSet.contains(userPermissions);
    }

    private boolean isValidUser(String username, String password) {
        var custsRepo = new CustomerRepositoryMemory();
        var customers = custsRepo.getCustomers();
        if (customers != null) {
            for (Customer c : customers) {
                if (username.equals(c.getUsername()) && password.equals(c.getPassword())) {
                    return true;
                }
            }
        }
        return false;
    }

}
