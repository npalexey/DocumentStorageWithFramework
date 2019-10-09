package com.nikitiuk.documentstoragewithframework.rest.controllers;

import com.nikitiuk.documentstoragewithframework.entities.GroupBean;
import com.nikitiuk.documentstoragewithframework.rest.services.RestGroupService;
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

@Tag(name = "Group Controller")
@PermitAll
@Path("/groups")
@Controller
public class RestGroupController {

    private static final Logger logger = LoggerFactory.getLogger(RestGroupController.class);

    @AutoWire
    private RestGroupService groupService;

    @PermitAll
    @GET
    @Produces(MimeType.TEXT_HTML)
    public Response getGroups() throws IOException {
        try {
            List<GroupBean> groupBeanList = groupService.getGroups();
            return ThymeleafResponseService.visualiseEntitiesInStorage(EntityTypes.GROUP, groupBeanList);
        } catch (Exception e) {
            logger.error("Error at RestGroupController getGroups.", e);
            return ResponseService.errorResponse(ResponseCode.HTTP_404_NOT_FOUND,
                    String.format("Error while producing list of groups. %s", e.getMessage()));
        }
    }

    @PermitAll
    @GET
    @Path("/{groupid}")
    @Produces(MimeType.TEXT_HTML)
    public Response getSingleGroup(@PathParam("groupid") long groupId) throws IOException {
        try {
            GroupBean groupById = groupService.getGroupById(groupId);
            return ThymeleafResponseService.visualiseSingleEntity(EntityTypes.GROUP, groupById, Actions.FOUND);
        } catch (Exception e) {
            logger.error(String.format("Error at RestGroupController getSingleGroup, id: %d.", groupId), e);
            return ResponseService.errorResponse(ResponseCode.HTTP_404_NOT_FOUND,
                    String.format("Error while getting group by id: %d. %s", groupId, e.getMessage()));
        }
    }

    @RolesAllowed({ "ADMINS" })
    @POST
    @Consumes(MimeType.APPLICATION_JSON)
    @Produces(MimeType.TEXT_HTML)
    public Response createGroup(GroupBean groupBean) throws IOException {
        try {
            GroupBean createdGroup = groupService.createGroup(groupBean);
            return ThymeleafResponseService.visualiseSingleEntity(EntityTypes.GROUP, createdGroup, Actions.CREATED);
        } catch (Exception e) {
            logger.error("Error at RestGroupController createGroup.", e);
            return ResponseService.errorResponse(ResponseCode.HTTP_404_NOT_FOUND,
                    String.format("Error while creating group. %s", e.getMessage()));
        }
    }

    @RolesAllowed({ "ADMINS" })
    @PUT
    @Consumes(MimeType.APPLICATION_JSON)
    @Produces(MimeType.TEXT_HTML)
    public Response updateGroup(GroupBean groupBean) throws IOException {
        try {
            GroupBean updatedGroup = groupService.updateGroup(groupBean);
            return ThymeleafResponseService.visualiseSingleEntity(EntityTypes.GROUP, updatedGroup, Actions.UPDATED);
        } catch (Exception e) {
            logger.error("Error at RestGroupController updateGroup.", e);
            return ResponseService.errorResponse(ResponseCode.HTTP_404_NOT_FOUND,
                    String.format("Error while updating group. %s", e.getMessage()));
        }
    }

    @RolesAllowed({ "ADMINS" })
    @DELETE
    @Path("/{groupid}")
    @Produces(MimeType.TEXT_HTML)
    public Response deleteGroupById(@PathParam("groupid") long groupId) throws IOException {
        try {
            groupService.deleteGroupById(groupId);
            return ResponseService.okResponseSimple("Group deleted successfully");
        } catch (Exception e) {
            logger.error(String.format("Error at RestGroupController deleteGroup, id: %d", groupId), e);
            return ResponseService.errorResponse(ResponseCode.HTTP_404_NOT_FOUND, "Error while deleting group. " + e.getMessage());
        }
    }
}