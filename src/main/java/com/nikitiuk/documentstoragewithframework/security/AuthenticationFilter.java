package com.nikitiuk.documentstoragewithframework.security;

import com.nikitiuk.documentstoragewithframework.dao.implementations.UserDao;
import com.nikitiuk.documentstoragewithframework.entities.GroupBean;
import com.nikitiuk.documentstoragewithframework.entities.UserBean;
import com.nikitiuk.documentstoragewithframework.exceptions.NoValidDataFromSourceException;
import com.nikitiuk.documentstoragewithframework.rest.services.helpers.ResponseService;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.beans.AutoWire;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.helpers.Order;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.security.Context;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.security.Filter;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.security.Provider;
import com.nikitiuk.javabeansinitializer.server.request.types.RequestContext;
import com.nikitiuk.javabeansinitializer.server.response.ResponseCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

/**
 * This filter verify the access permissions for a user
 * based on username and password provided in request
 */
@Provider
@Order(0)
public class AuthenticationFilter {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
    private static String AUTHENTICATION_SCHEME;

    @AutoWire
    private UserDao userDao;

    @Context
    private RequestContext requestContext;

    public String getAuthenticationScheme() {
        if (AUTHENTICATION_SCHEME == null) {
            AUTHENTICATION_SCHEME = System.getProperty("current.authentication.scheme");
        }
        return AUTHENTICATION_SCHEME;
    }

    @Filter
    public void filter() throws IOException {
        Method method = requestContext.getMethod();

        if (method.isAnnotationPresent(DenyAll.class)) {
            requestContext.abortWith(ResponseService.errorResponse(
                    ResponseCode.HTTP_403_FORBIDDEN, "Access blocked for all users!"));
            return;
        }

        final String authorization = requestContext.getSecurityInfo();

        //If no authorization information present
        if (StringUtils.isBlank(authorization)) {
            //Access allowed for all
            if (!method.isAnnotationPresent(PermitAll.class)) {
                //block access
                requestContext.abortWith(ResponseService.errorResponse(
                        ResponseCode.HTTP_401_UNAUTHORIZED, "You cannot access this resource"));
            }
            setDefaultContext(requestContext);
            return;
        }

        //Get encoded username and password
        final String encodedUserPassword = authorization.replaceFirst(
                getAuthenticationScheme() + " ", "");

        //Decode username and password
        String usernameAndPassword = new String(Base64.getDecoder().decode(encodedUserPassword/*.getBytes()*/));

        //Verify user access
        try {
            if (method.isAnnotationPresent(RolesAllowed.class)) {
                RolesAllowed rolesAnnotation = method.getAnnotation(RolesAllowed.class);
                Set<String> rolesSet = new HashSet<>(Arrays.asList(rolesAnnotation.value()));
                //Is user valid?
                if (!checkUserForValidityAndSetContextIfSo(usernameAndPassword, rolesSet, requestContext)) {
                    requestContext.abortWith(ResponseService.errorResponse(
                            ResponseCode.HTTP_401_UNAUTHORIZED, "You cannot access this resource"));
                }
            } else {
                setContextIfNoAnnotationsArePresent(usernameAndPassword, requestContext);
            }
        } catch (Exception e) {
            logger.error("Error at AuthenticationFilter setContextIfNoAnnotationsArePresent.", e);
            requestContext.abortWith(ResponseService.errorResponse(ResponseCode.HTTP_401_UNAUTHORIZED,
                    String.format("You cannot access this resource. %s", e.getMessage())));
        }
    }

    private void setDefaultContext(RequestContext requestContext) {
        UserBean user = userDao.getUserByName("Guest");

        requestContext.setISecurityContext(new SecurityContext(createUserPrincipal(user)));
    }

    private void setContextIfNoAnnotationsArePresent(final String usernameAndPassword,
                                                     RequestContext requestContext) throws Exception {

        final String[] decipheredUsernameAndPassword = decoupleBasicAuth(usernameAndPassword);
        UserBean user = userDao.getUserByName(decipheredUsernameAndPassword[0]);
        if (user == null) {
            requestContext.abortWith(ResponseService.errorResponse(
                    ResponseCode.HTTP_401_UNAUTHORIZED, "You cannot access this resource"));
            return;
        }
        if (!user.getPassword().equals(decipheredUsernameAndPassword[1])) {
            requestContext.abortWith(ResponseService.errorResponse(
                    ResponseCode.HTTP_401_UNAUTHORIZED, "You cannot access this resource"));
            return;
        }

        requestContext.setISecurityContext(new SecurityContext(createUserPrincipal(user)));
    }

    private boolean checkUserForValidityAndSetContextIfSo(final String usernameAndPassword,
                                                          final Set<String> rolesSet, RequestContext requestContext) throws Exception {
        final String[] decoupledUsernameAndPassword = decoupleBasicAuth(usernameAndPassword);

        //Step 1. Fetch password from database and match with password in argument
        //If both match then get the defined role for user from database and continue; else return [false]
        UserBean user = userDao.getUserByName(decoupledUsernameAndPassword[0]);
        if (!user.getPassword().equals(decoupledUsernameAndPassword[1])) {
            return false;
        }

        //Step 2. Verify user role
        for (GroupBean group : user.getGroups()) {
            if (rolesSet.contains(group.getName())) {
                requestContext.setISecurityContext(new SecurityContext(createUserPrincipal(user)));
                return true;
            }
        }
        return false;
    }

    private String[] decoupleBasicAuth(final String usernameAndPassword) throws Exception {
        final String[] decoupledUsernameAndPassword = usernameAndPassword.split(":", 2);
        if (decoupledUsernameAndPassword.length != 2) {
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
