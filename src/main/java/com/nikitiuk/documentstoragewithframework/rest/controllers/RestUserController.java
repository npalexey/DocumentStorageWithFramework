package com.nikitiuk.documentstoragewithframework.rest.controllers;

import com.nikitiuk.documentstoragewithframework.entities.UserBean;
import com.nikitiuk.documentstoragewithframework.rest.services.RestUserService;
import com.nikitiuk.documentstoragewithframework.rest.services.helpers.ResponseService;
import com.nikitiuk.documentstoragewithframework.rest.services.helpers.ThymeleafResponseService;
import com.nikitiuk.documentstoragewithframework.rest.services.helpers.enums.Actions;
import com.nikitiuk.documentstoragewithframework.rest.services.helpers.enums.EntityTypes;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.beans.AutoWire;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.beans.Controller;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.beans.controller.Consumes;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.beans.controller.Path;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.beans.controller.PathParam;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.beans.controller.Produces;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.http.DELETE;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.http.GET;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.http.POST;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.http.PUT;
import com.nikitiuk.javabeansinitializer.server.response.Response;
import com.nikitiuk.javabeansinitializer.server.response.ResponseCode;
import com.nikitiuk.javabeansinitializer.server.utils.MimeType;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import java.io.IOException;
import java.util.List;

@Tag(name = "User Controller")
@PermitAll
@Path("/users")
@Controller
public class RestUserController {

    private static final Logger logger = LoggerFactory.getLogger(RestUserController.class);

    @AutoWire
    private RestUserService userService;

    /*public RestUserController() {
        this.userService = (RestUserService) AppListener
                .getContext()
                .getBeanContainer()
                .get(RestUserService.class);
    }*/

    @PermitAll
    @GET
    @Produces({ MimeType.TEXT_HTML })
    public Response getUsers() throws IOException {
        try {
            List<UserBean> userBeanList = userService.getUsers();
            return ThymeleafResponseService.visualiseEntitiesInStorage(EntityTypes.USER, userBeanList);
        } catch (Exception e) {
            logger.error("Error at RestUserController getUsers.", e);
            return ResponseService.errorResponse(ResponseCode.HTTP_404_NOT_FOUND,
                    String.format("Error while producing list of users. %s", e.getMessage()));
        }
    }

    @PermitAll
    @GET
    @Path("/{userid}")
    @Produces({ MimeType.TEXT_HTML })
    public Response getSingleUser(@PathParam("userid") long userId) throws IOException {
        try {
            UserBean singleUser = userService.getSingleUser(userId);
            return ThymeleafResponseService.visualiseSingleEntity(EntityTypes.USER, singleUser, Actions.FOUND);
        } catch (Exception e) {
            logger.error(String.format("Error at RestUserController getSingleUser, id: %d", userId), e);
            return ResponseService.errorResponse(ResponseCode.HTTP_404_NOT_FOUND,
                    String.format("Error while getting user by id: %d. %s", userId, e.getMessage()));
        }
    }

    @RolesAllowed({ "ADMINS" })
    @POST
    @Consumes(MimeType.APPLICATION_JSON)
    @Produces(MimeType.TEXT_HTML)
    public Response createUser(UserBean userBean) throws IOException {
        try {
            UserBean createdUser = userService.createUser(userBean);
            return ThymeleafResponseService.visualiseSingleEntity(EntityTypes.USER, createdUser, Actions.CREATED);
        } catch (Exception e) {
            logger.error("Error at RestUserController createUser.", e);
            return ResponseService.errorResponse(ResponseCode.HTTP_404_NOT_FOUND,
                    String.format("Error while creating user. %s", e.getMessage()));
        }
    }

    @RolesAllowed({ "ADMINS" })
    @PUT
    @Consumes(MimeType.APPLICATION_JSON)
    @Produces(MimeType.TEXT_HTML)
    public Response updateUser(UserBean userBean) throws IOException {
        try {
            UserBean updatedUser = userService.updateUser(userBean);
            return ThymeleafResponseService.visualiseSingleEntity(EntityTypes.USER, updatedUser, Actions.UPDATED);
        } catch (Exception e) {
            logger.error("Error at RestUserController updateUser.", e);
            return ResponseService.errorResponse(ResponseCode.HTTP_404_NOT_FOUND,
                    String.format("Error while updating user. %s", e.getMessage()));
        }
    }

    @RolesAllowed({ "ADMINS" })
    @DELETE
    @Path("/{userid}")
    @Produces(MimeType.TEXT_HTML)
    public Response deleteUserById(@PathParam("userid") long userId) throws IOException {
        try {
            userService.deleteUser(userId);
            return ResponseService.okResponseSimple(String.format("User with id: %d deleted successfully.", userId));
        } catch (Exception e) {
            logger.error(String.format("Error at RestUserController deleteUser, id: %d", userId), e);
            return ResponseService.errorResponse(ResponseCode.HTTP_404_NOT_FOUND,
                    String.format("Error while deleting user by id: %d. %s", userId, e.getMessage()));
        }
    }
}
