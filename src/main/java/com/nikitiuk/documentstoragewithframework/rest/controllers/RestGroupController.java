package com.nikitiuk.documentstoragewithframework.rest.controllers;

import com.nikitiuk.documentstoragewithframework.entities.GroupBean;
import com.nikitiuk.documentstoragewithframework.rest.services.RestGroupService;
import com.nikitiuk.documentstoragewithframework.rest.services.helpers.ResponseService;
import com.nikitiuk.documentstoragewithframework.rest.services.helpers.ThymeleafResponseService;
import com.nikitiuk.documentstoragewithframework.rest.services.helpers.enums.Actions;
import com.nikitiuk.documentstoragewithframework.rest.services.helpers.enums.EntityTypes;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Tag(name = "Group Controller")
@PermitAll
@Path("/groups")
public class RestGroupController {

    private static final Logger logger = LoggerFactory.getLogger(RestGroupController.class);
    private RestGroupService groupService = new RestGroupService();

    @PermitAll
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getGroups() {
        try {
            List<GroupBean> groupBeanList = groupService.getGroups();
            return ThymeleafResponseService.visualiseEntitiesInStorage(EntityTypes.GROUP, groupBeanList);
        } catch (Exception e) {
            logger.error("Error at RestGroupController getGroups.", e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND,
                    String.format("Error while producing list of groups. %s", e.getMessage()));
        }
    }

    @PermitAll
    @GET
    @Path("/{groupid}")
    @Produces(MediaType.TEXT_HTML)
    public Response getSingleGroup(@PathParam("groupid") long groupId) {
        try {
            GroupBean groupById = groupService.getGroupById(groupId);
            return ThymeleafResponseService.visualiseSingleEntity(EntityTypes.GROUP, groupById, Actions.FOUND);
        } catch (Exception e) {
            logger.error(String.format("Error at RestGroupController getSingleGroup, id: %d.", groupId), e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND,
                    String.format("Error while getting group by id: %d. %s", groupId, e.getMessage()));
        }
    }

    @RolesAllowed({ "ADMINS" })
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_HTML)
    public Response createGroup(GroupBean groupBean) {
        try {
            GroupBean createdGroup = groupService.createGroup(groupBean);
            return ThymeleafResponseService.visualiseSingleEntity(EntityTypes.GROUP, createdGroup, Actions.CREATED);
        } catch (Exception e) {
            logger.error("Error at RestGroupController createGroup.", e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND,
                    String.format("Error while creating group. %s", e.getMessage()));
        }
    }

    @RolesAllowed({ "ADMINS" })
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_HTML)
    public Response updateGroup(GroupBean groupBean) {
        try {
            GroupBean updatedGroup = groupService.updateGroup(groupBean);
            return ThymeleafResponseService.visualiseSingleEntity(EntityTypes.GROUP, updatedGroup, Actions.UPDATED);
        } catch (Exception e) {
            logger.error("Error at RestGroupController updateGroup.", e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND,
                    String.format("Error while updating group. %s", e.getMessage()));
        }
    }

    @RolesAllowed({ "ADMINS" })
    @DELETE
    @Path("/{groupid}")
    @Produces(MediaType.TEXT_HTML)
    public Response deleteGroupById(@PathParam("groupid") long groupId) {
        try {
            groupService.deleteGroupById(groupId);
            return ResponseService.okResponseSimple("Group deleted successfully");
        } catch (Exception e) {
            logger.error(String.format("Error at RestGroupController deleteGroup, id: %d", groupId), e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while deleting group. " + e.getMessage());
        }
    }
}