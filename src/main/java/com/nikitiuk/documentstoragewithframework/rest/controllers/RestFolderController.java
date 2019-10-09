package com.nikitiuk.documentstoragewithframework.rest.controllers;

import com.nikitiuk.documentstoragewithframework.entities.FolderBean;
import com.nikitiuk.documentstoragewithframework.rest.services.RestFolderService;
import com.nikitiuk.documentstoragewithframework.rest.services.helpers.ResponseService;
import com.nikitiuk.documentstoragewithframework.rest.services.helpers.ThymeleafResponseService;
import com.nikitiuk.documentstoragewithframework.rest.services.helpers.enums.EntityTypes;
import com.nikitiuk.documentstoragewithframework.security.SecurityContext;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.beans.AutoWire;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.beans.Controller;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.beans.controller.Path;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.beans.controller.PathParam;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.beans.controller.Produces;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.http.DELETE;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.http.GET;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.security.Context;
import com.nikitiuk.javabeansinitializer.server.request.types.RequestContext;
import com.nikitiuk.javabeansinitializer.server.response.Response;
import com.nikitiuk.javabeansinitializer.server.response.ResponseCode;
import com.nikitiuk.javabeansinitializer.server.utils.MimeType;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;

import java.io.IOException;
import java.util.List;

@Tag(name = "Folder Controller")
@PermitAll
@Path("/folders")
@Controller
public class RestFolderController {

    private static final Logger logger = LoggerFactory.getLogger(RestFolderController.class);

    @AutoWire
    private RestFolderService folderService;

    @PermitAll
    @GET
    @Produces(MimeType.TEXT_HTML)
    public Response getFolders(@Context RequestContext context) throws IOException {
        try {
            List<FolderBean> folderBeanList = folderService.getFolders(
                    (SecurityContext) context.getISecurityContext());
            return ThymeleafResponseService.visualiseEntitiesInStorage(EntityTypes.FOLDER, folderBeanList);
        } catch (Exception e) {
            logger.error("Error at RestFolderController getFolders.", e);
            return ResponseService.errorResponse(ResponseCode.HTTP_404_NOT_FOUND,
                    String.format("Error while producing list of folders. %s", e.getMessage()));
        }
    }

    @PermitAll
    @DELETE
    @Path("/{folderid}")
    @Produces(MimeType.TEXT_HTML)
    public Response deleteFolder(@Context RequestContext context,
                                 @PathParam("folderid") long folderId) throws IOException {
        try {
            String deletedFolderPath = folderService.deleteFolderById(
                    (SecurityContext) context.getISecurityContext(), folderId);
            return ResponseService.okResponseSimple(String.format(
                    "Folder: '%s' deleted successfully.", deletedFolderPath));
        } catch (Exception e) {
            logger.error(String.format("Error at RestFolderController deleteFolderById, id: %d", folderId), e);
            return ResponseService.errorResponse(ResponseCode.HTTP_404_NOT_FOUND,
                    String.format("Error occurred while deleting the folder by id: %d. %s",
                            folderId, e.getMessage()));
        }
    }
}