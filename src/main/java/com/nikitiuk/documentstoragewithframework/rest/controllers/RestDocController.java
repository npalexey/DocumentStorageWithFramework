package com.nikitiuk.documentstoragewithframework.rest.controllers;

import com.nikitiuk.documentstoragewithframework.entities.DocBean;
import com.nikitiuk.documentstoragewithframework.rest.entities.DocumentDownloaderResponseBuilder;
import com.nikitiuk.documentstoragewithframework.rest.services.RestDocService;
import com.nikitiuk.documentstoragewithframework.rest.services.helpers.ResponseService;
import com.nikitiuk.documentstoragewithframework.rest.services.helpers.ThymeleafResponseService;
import com.nikitiuk.documentstoragewithframework.rest.services.helpers.enums.Actions;
import com.nikitiuk.documentstoragewithframework.rest.services.helpers.enums.EntityTypes;
import com.nikitiuk.documentstoragewithframework.security.SecurityContext;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.List;

@Tag(name = "Document Controller")
@PermitAll
@Path("/docs")
public class RestDocController {

    private static final Logger logger = LoggerFactory.getLogger(RestDocController.class);
    private RestDocService docService = new RestDocService();

    @PermitAll
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getDocuments(@Context ContainerRequestContext context) {
        try {
            List<DocBean> docBeanList = docService.getDocuments(
                    (SecurityContext) context.getSecurityContext());
            return ThymeleafResponseService.visualiseEntitiesInStorage(EntityTypes.DOCUMENT, docBeanList);
        } catch (Exception e) {
            logger.error("Error at RestDocController getDocuments.", e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND,
                    String.format("Error while producing list of documents. %s", e.getMessage()));
        }
    }

    @PermitAll
    @GET
    @Path("/in-folder/{folderid}")
    @Produces(MediaType.TEXT_HTML)
    public Response getDocumentInFolder(@Context ContainerRequestContext context,
                                        @PathParam("folderid") long folderId) {
        try {
            List<DocBean> docBeanList = docService.getDocumentsInFolder(
                    (SecurityContext) context.getSecurityContext(), folderId);
            return ThymeleafResponseService.visualiseEntitiesInStorage(EntityTypes.DOCUMENT, docBeanList);
        } catch (Exception e) {
            logger.error(String.format("Error at RestDocController getDocumentsInFolder, folderId: %d.", folderId), e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND,
                    String.format("Error while producing list of documents in folder. %s", e.getMessage()));
        }
    }

    @PermitAll
    @GET
    @Path("/{documentid}")
    public Response downloadDocument(@Context ContainerRequestContext context,
                                     @PathParam("documentid") long documentId) {
        try {
            DocumentDownloaderResponseBuilder documentDownloaderResponseBuilder = docService.downloadDocumentById(
                    (SecurityContext) context.getSecurityContext(), documentId);
            return ResponseService.okResponseForFile(documentDownloaderResponseBuilder);
        } catch (Exception e) {
            logger.error(String.format("Error at RestDocController downloadDocumentById, id: %d.", documentId), e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND,
                    String.format("Error occurred while downloading the document by id: %d. %s",
                            documentId, e.getMessage()));
        }
    }

    @PermitAll
    @GET
    @Path("/{documentid}/content")
    @Produces(MediaType.TEXT_HTML)
    public Response getContentOfDocument(@PathParam("documentid") long documentId,
                                         @Context ContainerRequestContext context) {
        try {
            List<String> documentContent = docService.getContentOfDocumentById(
                    (SecurityContext) context.getSecurityContext(), documentId);
            return ThymeleafResponseService.visualiseDocumentContent(documentContent);
        } catch (Exception e) {
            logger.error(String.format("Error at RestDocController getContentOfDocumentById, id: %d.", documentId), e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND,
                    String.format("Error occurred while getting content of the document by id: %d. %s",
                            documentId, e.getMessage()));
        }
    }

    @PermitAll
    @POST
    @Path("/{parentid}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_HTML)
    public Response uploadDocument(@FormDataParam("file") InputStream fileInputStream,
                                   @Context ContainerRequestContext context,
                                   @FormDataParam("designatedName") String designatedName,
                                   @PathParam("parentid") long parentFolderId) {
        try {
            DocBean uploadedDocument = docService.uploadDocument((SecurityContext)
                    context.getSecurityContext(), fileInputStream, designatedName, parentFolderId);
            return ThymeleafResponseService.visualiseSingleEntity(
                    EntityTypes.DOCUMENT, uploadedDocument, Actions.UPLOADED);
        } catch (Exception e) {
            logger.error(String.format("Error at RestDocController uploadDocument, parentId: %d.", parentFolderId), e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND,
                    String.format("Error while uploading document: %s. %s", designatedName, e.getMessage()));
        }
    }

    @PermitAll
    @POST
    @Path("/search")
    @Produces(MediaType.TEXT_HTML)
    public Response searchInEveryDocumentWithStringQuery(@DefaultValue("") @QueryParam("query") String query,
                                                         @Context ContainerRequestContext context) {
        try {
            String searchResult = docService.searchInEveryDocumentWithStringQuery(query,
                    (SecurityContext) context.getSecurityContext());
            return ThymeleafResponseService.visualiseSearchResult(searchResult);
        } catch (Exception e) {
            logger.error("Error at RestDocController searchInEveryDocumentWithStringQuery.", e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND,
                    String.format("Error while searching for: %s. %s", query, e.getMessage()));
        }
    }

    @PermitAll
    @PUT
    @Path("/{documentid}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_HTML)
    public Response updateDocument(@Context ContainerRequestContext context,
                                   @PathParam("documentid") long documentId,
                                   @FormDataParam("file") InputStream fileInputStream,
                                   @FormDataParam("designatedName") String designatedName) {
        try {
            DocBean updatedDocument = docService.updateDocumentById(
                    (SecurityContext) context.getSecurityContext(), fileInputStream, documentId, designatedName);
            return ThymeleafResponseService.visualiseSingleEntity(
                    EntityTypes.DOCUMENT, updatedDocument, Actions.UPDATED);
        } catch (Exception e) {
            logger.error(String.format("Error at RestDocController updateDocumentById, id: %d.", documentId), e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND,
                    String.format("Error occurred while updating the document by id: %d. %s",
                            documentId, e.getMessage()));
        }
    }

    @PermitAll
    @DELETE
    @Path("/{documentid}")
    @Produces(MediaType.TEXT_HTML)
    public Response deleteDocument(@Context ContainerRequestContext context,
                                   @PathParam("documentid") long documentId) {
        try {
            String deletedDocumentPath = docService.deleteDocumentById(
                    (SecurityContext) context.getSecurityContext(), documentId);
            return ResponseService.okResponseSimple(String.format(
                    "Document: '%s' deleted successfully.", deletedDocumentPath));
        } catch (Exception e) {
            logger.error(String.format("Error at RestDocController deleteDocumentById, id: %d", documentId), e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND,
                    String.format("Error occurred while deleting the document by id: %d. %s",
                            documentId, e.getMessage()));
        }
    }
}