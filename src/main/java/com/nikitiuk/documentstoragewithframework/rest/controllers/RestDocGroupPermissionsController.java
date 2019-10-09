package com.nikitiuk.documentstoragewithframework.rest.controllers;

import com.nikitiuk.documentstoragewithframework.entities.DocGroupPermissions;
import com.nikitiuk.documentstoragewithframework.rest.entities.DocGroupPermissionsRequest;
import com.nikitiuk.documentstoragewithframework.rest.services.RestDocGroupPermissionsService;
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
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.http.PUT;
import com.nikitiuk.javabeansinitializer.server.response.Response;
import com.nikitiuk.javabeansinitializer.server.response.ResponseCode;
import com.nikitiuk.javabeansinitializer.server.utils.MimeType;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import java.io.IOException;
import java.util.List;

@Tag(name = "Document Permissions Controller")
@RolesAllowed({ "ADMINS" })
@Path("/permissions")
@Controller
public class RestDocGroupPermissionsController {

    private static final Logger logger = LoggerFactory.getLogger(RestDocGroupPermissionsController.class);

    @AutoWire
    private RestDocGroupPermissionsService restDocGroupPermissionsService;

    @GET
    @Produces(MimeType.TEXT_HTML)
    public Response getAllGroupPermissionsForDocuments() throws IOException {
        try {
            List<DocGroupPermissions> docGroupPermissionsList =
                    restDocGroupPermissionsService.getAllGroupPermissionsForDocuments();
            return ThymeleafResponseService.visualiseEntitiesInStorage(
                    EntityTypes.DOC_GROUP_PERMISSIONS, docGroupPermissionsList);
        } catch (Exception e) {
            logger.error("Error at RestDocGroupPermissionsController getAllGroupPermissionsForDocuments.", e);
            return ResponseService.errorResponse(ResponseCode.HTTP_404_NOT_FOUND, String.format(
                    "Error while producing all permissions for documents for every group. %s", e.getMessage()));
        }
    }

    @GET
    @Path("/for-group/{groupid}")
    @Produces(MimeType.TEXT_HTML)
    public Response getPermissionsForDocumentsByGroupId(@PathParam("groupid") long groupId) throws IOException {
        try {
            List<DocGroupPermissions> docGroupPermissionsList =
                    restDocGroupPermissionsService.getPermissionsForDocumentsByGroupId(groupId);
            return ThymeleafResponseService.visualiseEntitiesInStorage(
                    EntityTypes.DOC_GROUP_PERMISSIONS, docGroupPermissionsList);
        } catch (Exception e) {
            logger.error(String.format(
                    "Error at RestDocGroupPermissionsController getPermissionsForDocumentsByGroupId, groupId: %d.",
                    groupId), e);
            return ResponseService.errorResponse(ResponseCode.HTTP_404_NOT_FOUND, String.format(
                    "Error while producing permissions for documents for groupId: %d. %s", groupId, e.getMessage()));
        }
    }

    @GET
    @Path("/for-document/{documentid}")
    @Produces(MimeType.TEXT_HTML)
    public Response getPermissionsForDocumentByDocId(@PathParam("documentid") long documentId) throws IOException {
        try {
            List<DocGroupPermissions> docGroupPermissionsList =
                    restDocGroupPermissionsService.getPermissionsForDocumentByDocId(documentId);
            return ThymeleafResponseService.visualiseEntitiesInStorage(
                    EntityTypes.DOC_GROUP_PERMISSIONS, docGroupPermissionsList);
        } catch (Exception e) {
            logger.error(String.format(
                    "Error at RestDocGroupPermissionsController getPermissionsForDocumentByDocId, documentId: %d.",
                    documentId), e);
            return ResponseService.errorResponse(ResponseCode.HTTP_404_NOT_FOUND, String.format(
                    "Error while producing permissions for document, documentId: %d. %s", documentId, e.getMessage()));
        }
    }

    @PUT
    @Path("/set-write")
    @Consumes(MimeType.APPLICATION_JSON)
    @Produces(MimeType.TEXT_HTML)
    public Response setWritePermissionsForGroupForDocument(DocGroupPermissionsRequest requestObj) throws IOException {
        try {
            DocGroupPermissions setDocGroupPermissions =
                    restDocGroupPermissionsService.setWriteForDocumentForGroup(requestObj);
            return ThymeleafResponseService.visualiseSingleEntity(
                    EntityTypes.DOC_GROUP_PERMISSIONS, setDocGroupPermissions, Actions.SET);
        } catch (Exception e) {
            logger.error(String.format(
                    "Error at RestDocGroupPermissionsController setWritePermissionsForGroupForDocument, " +
                            "documentId: %d, groupId: %d.", requestObj.getDocumentId(), requestObj.getGroupId()), e);
            return ResponseService.errorResponse(ResponseCode.HTTP_404_NOT_FOUND,
                    String.format("Error while setting write permissions for documentId: %d, for groupId: %d. %s",
                            requestObj.getDocumentId(), requestObj.getGroupId(), e.getMessage()));
        }
    }

    @PUT
    @Path("/set-read")
    @Consumes(MimeType.APPLICATION_JSON)
    @Produces(MimeType.TEXT_HTML)
    public Response setReadPermissionsForGroupForDocument(DocGroupPermissionsRequest requestObj) throws IOException {
        try {
            DocGroupPermissions setDocGroupPermissions =
                    restDocGroupPermissionsService.setReadForDocumentForGroup(requestObj);
            return ThymeleafResponseService.visualiseSingleEntity(
                    EntityTypes.DOC_GROUP_PERMISSIONS, setDocGroupPermissions, Actions.SET);
        } catch (Exception e) {
            logger.error(String.format(
                    "Error at RestDocGroupPermissionsController setReadPermissionsForGroupForDocument, " +
                            "documentId: %d, groupId: %d.", requestObj.getDocumentId(), requestObj.getGroupId()), e);
            return ResponseService.errorResponse(ResponseCode.HTTP_404_NOT_FOUND,
                    String.format("Error while setting read permissions for documentId: %d, for groupId: %d. %s",
                            requestObj.getDocumentId(), requestObj.getGroupId(), e.getMessage()));
        }
    }

    @DELETE
    @Path("/for-group/{groupid}")
    @Produces(MimeType.TEXT_HTML)
    public Response deleteAllPermissionsForGroup(@PathParam("groupid") long groupid) throws IOException {
        try {
            Integer quantityOfDeletedPermission = restDocGroupPermissionsService.deleteAllPermissionsForGroup(groupid);
            return ResponseService.okResponseSimple(
                    String.format("All %d permissions for documents for group(id:%d) deleted successfully",
                            quantityOfDeletedPermission, groupid));
        } catch (Exception e) {
            logger.error(String.format(
                    "Error at RestDocGroupPermissionsController deleteAllPermissionsForGroup, groupId: %d.",
                    groupid), e);
            return ResponseService.errorResponse(ResponseCode.HTTP_404_NOT_FOUND, String.format(
                    "Error while deleting all permissions for documents for groupId: %d. %s", groupid, e.getMessage()));
        }
    }

    @DELETE
    @Path("/for-document/{documentid}")
    @Produces(MimeType.TEXT_HTML)
    public Response deleteAllPermissionsForDocument(@PathParam("documentid") long documentid) throws IOException {
        try {
            Integer quantityOfDeletedPermission =
                    restDocGroupPermissionsService.deleteAllPermissionsForDocumentExceptAdmin(documentid);
            return ResponseService.okResponseSimple(
                    String.format("All %d permissions for document(id:%d) deleted successfully",
                            quantityOfDeletedPermission, documentid));
        } catch (Exception e) {
            logger.error(String.format(
                    "Error at RestDocGroupPermissionsController deleteAllPermissionsForDocument, documentId: %d.",
                    documentid), e);
            return ResponseService.errorResponse(ResponseCode.HTTP_404_NOT_FOUND, String.format(
                    "Error while deleting all permissions for documentId: %d. %s", documentid, e.getMessage()));
        }
    }

    @DELETE
    @Path("/for-document-for-group/")
    @Consumes(MimeType.APPLICATION_JSON)
    @Produces(MimeType.TEXT_HTML)
    public Response deletePermissionsForDocumentForGroup(DocGroupPermissionsRequest requestObj) throws IOException {
        try {
            Integer quantityOfDeletedPermission =
                    restDocGroupPermissionsService.deletePermissionsForDocumentForGroup(requestObj);
            return ResponseService.okResponseSimple(
                    String.format("%d permission for document(id:%d) for group(id:%d) deleted successfully",
                            quantityOfDeletedPermission, requestObj.getDocumentId(), requestObj.getGroupId()));
        } catch (Exception e) {
            logger.error(String.format(
                    "Error at RestDocGroupPermissionsController deletePermissionsForDocumentForGroup, " +
                            "documentId: %d, groupId: %d", requestObj.getDocumentId(), requestObj.getGroupId()), e);
            return ResponseService.errorResponse(ResponseCode.HTTP_404_NOT_FOUND, String.format(
                    "Error while deleting all permissions for documentId: %d,  for groupId: %d. %s",
                    requestObj.getDocumentId(), requestObj.getGroupId(), e.getMessage()));
        }
    }
}