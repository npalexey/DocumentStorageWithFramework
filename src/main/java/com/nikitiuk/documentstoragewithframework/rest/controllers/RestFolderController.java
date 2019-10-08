package com.nikitiuk.documentstoragewithframework.rest.controllers;

import com.nikitiuk.documentstoragewithframework.entities.FolderBean;
import com.nikitiuk.documentstoragewithframework.rest.services.RestFolderService;
import com.nikitiuk.documentstoragewithframework.rest.services.helpers.ResponseService;
import com.nikitiuk.documentstoragewithframework.rest.services.helpers.ThymeleafResponseService;
import com.nikitiuk.documentstoragewithframework.rest.services.helpers.enums.EntityTypes;
import com.nikitiuk.documentstoragewithframework.security.SecurityContext;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Tag(name = "Folder Controller")
@PermitAll
@Path("/folders")
public class RestFolderController {

    private static final Logger logger = LoggerFactory.getLogger(RestFolderController.class);
    private RestFolderService folderService = new RestFolderService();

    @PermitAll
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getFolders(@Context ContainerRequestContext context) {
        try {
            List<FolderBean> folderBeanList = folderService.getFolders(
                    (SecurityContext) context.getSecurityContext());
            return ThymeleafResponseService.visualiseEntitiesInStorage(EntityTypes.FOLDER, folderBeanList);
        } catch (Exception e) {
            logger.error("Error at RestFolderController getFolders.", e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND,
                    String.format("Error while producing list of folders. %s", e.getMessage()));
        }
    }

    @PermitAll
    @DELETE
    @Path("/{folderid}")
    @Produces(MediaType.TEXT_HTML)
    public Response deleteFolder(@Context ContainerRequestContext context,
                                 @PathParam("folderid") long folderId) {
        try {
            String deletedFolderPath = folderService.deleteFolderById(
                    (SecurityContext) context.getSecurityContext(), folderId);
            return ResponseService.okResponseSimple(String.format(
                    "Folder: '%s' deleted successfully.", deletedFolderPath));
        } catch (Exception e) {
            logger.error(String.format("Error at RestFolderController deleteFolderById, id: %d", folderId), e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND,
                    String.format("Error occurred while deleting the folder by id: %d. %s",
                            folderId, e.getMessage()));
        }
    }
}