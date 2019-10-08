package com.nikitiuk.documentstoragewithframework.security;

import com.nikitiuk.documentstoragewithframework.dao.implementations.UserDao;
import com.nikitiuk.documentstoragewithframework.entities.GroupBean;
import com.nikitiuk.documentstoragewithframework.entities.UserBean;
import com.nikitiuk.documentstoragewithframework.exceptions.NoValidDataFromSourceException;
import com.nikitiuk.documentstoragewithframework.rest.services.helpers.ResponseService;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.helpers.Order;
import com.nikitiuk.javabeansinitializer.server.request.types.RequestContext;
import org.glassfish.jersey.internal.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.Method;
import java.util.*;

/**
 * This filter verify the access permissions for a user
 * based on username and password provided in request
 */
@Provider
@Priority(Priorities.AUTHORIZATION)
@Order(0)
public class AuthenticationFilter implements ContainerRequestFilter {

    private static final String AUTHORIZATION_PROPERTY = System.getProperty("current.authorization.property");
    private static final String AUTHENTICATION_SCHEME = System.getProperty("current.authentication.scheme");
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
    private UserDao userDao = new UserDao();

    @Context
    private RequestContext requestContext;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        Method method = this.requestContext.getMethod();

        if (method.isAnnotationPresent(DenyAll.class)) {
            requestContext.abortWith(ResponseService.errorResponse(
                    Response.Status.FORBIDDEN, "Access blocked for all users!"));
            return;
        }

        //Get request headers
        final MultivaluedMap<String, String> headers = requestContext.getHeaders();

        //Fetch authorization header
        final List<String> authorization = headers.get(AUTHORIZATION_PROPERTY);

        //If no authorization information present
        if (authorization == null || authorization.isEmpty()) {
            //Access allowed for all
            if(!method.isAnnotationPresent(PermitAll.class)) {
                //block access
                requestContext.abortWith(ResponseService.errorResponse(
                        Response.Status.UNAUTHORIZED, "You cannot access this resource"));
            }
            setDefaultContext(requestContext);
            return;
        }

        //Get encoded username and password
        final String encodedUserPassword = authorization.get(0).replaceFirst(
                AUTHENTICATION_SCHEME + " ", "");

        //Decode username and password
        String usernameAndPassword = new String(Base64.decode(encodedUserPassword.getBytes()));

        //Verify user access
        try {
            if (method.isAnnotationPresent(RolesAllowed.class)) {
                RolesAllowed rolesAnnotation = method.getAnnotation(RolesAllowed.class);
                Set<String> rolesSet = new HashSet<>(Arrays.asList(rolesAnnotation.value()));
                //Is user valid?
                if (!checkUserForValidityAndSetContextIfSo(usernameAndPassword, rolesSet, requestContext)) {
                    requestContext.abortWith(ResponseService.errorResponse(
                            Response.Status.UNAUTHORIZED, "You cannot access this resource"));
                }
            } else {
                setContextIfNoAnnotationsArePresent(usernameAndPassword, requestContext);
            }
        } catch (Exception e) {
        logger.error("Error at AuthenticationFilter setContextIfNoAnnotationsArePresent.", e);
        requestContext.abortWith(ResponseService.errorResponse(Response.Status.UNAUTHORIZED,
                String.format("You cannot access this resource. %s", e.getMessage())));
        }
    }

    private void setDefaultContext(ContainerRequestContext requestContext) {
        UserBean user = userDao.getUserByName("Guest");

        String scheme = requestContext.getUriInfo().getRequestUri().getScheme();
        requestContext.setSecurityContext(new SecurityContext(createUserPrincipal(user), scheme));
    }

    private void setContextIfNoAnnotationsArePresent(final String usernameAndPassword,
                                                     ContainerRequestContext requestContext) throws Exception {

        final String[] decipheredUsernameAndPassword = decoupleBasicAuth(usernameAndPassword);
        UserBean user = userDao.getUserByName(decipheredUsernameAndPassword[0]);
        if(user == null) {
            requestContext.abortWith(ResponseService.errorResponse(
                    Response.Status.UNAUTHORIZED, "You cannot access this resource"));
            return;
        }
        if (!user.getPassword().equals(decipheredUsernameAndPassword[1])) {
            requestContext.abortWith(ResponseService.errorResponse(
                    Response.Status.UNAUTHORIZED, "You cannot access this resource"));
            return;
        }
        String scheme = requestContext.getUriInfo().getRequestUri().getScheme();
        requestContext.setSecurityContext(new SecurityContext(createUserPrincipal(user), scheme));
    }

    private boolean checkUserForValidityAndSetContextIfSo(final String usernameAndPassword,
                                                          final Set<String> rolesSet, ContainerRequestContext requestContext) throws Exception {
        final String [] decoupledUsernameAndPassword = decoupleBasicAuth(usernameAndPassword);

        //Step 1. Fetch password from database and match with password in argument
        //If both match then get the defined role for user from database and continue; else return [false]
        UserBean user = userDao.getUserByName(decoupledUsernameAndPassword[0]);
        if (!user.getPassword().equals(decoupledUsernameAndPassword[1])) {
            return false;
        }

        //Step 2. Verify user role
        for (GroupBean group : user.getGroups()) {
            if (rolesSet.contains(group.getName())) {
                String scheme = requestContext.getUriInfo().getRequestUri().getScheme();
                requestContext.setSecurityContext(new SecurityContext(createUserPrincipal(user), scheme));
                return true;
            }
        }
        return false;
    }

    private String[] decoupleBasicAuth(final String usernameAndPassword) throws Exception {
        final String[] decoupledUsernameAndPassword = usernameAndPassword.split(":", 2);
        if(decoupledUsernameAndPassword.length != 2) {
            throw new NoValidDataFromSourceException("Wrong syntax in username or password.");
        }
        return decoupledUsernameAndPassword;
    }

    private UserPrincipal createUserPrincipal(UserBean user) {
        UserPrincipal userPrincipal = new UserPrincipal();
        userPrincipal.setId(user.getId());
        userPrincipal.setName(user.getName());
        userPrincipal.setGroups(user.getGroups());
        return userPrincipal;
    }
}
