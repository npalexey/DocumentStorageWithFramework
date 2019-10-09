package com.nikitiuk.documentstoragewithframework.rest.controllers;

import com.nikitiuk.documentstoragewithframework.entities.DocBean;
import com.nikitiuk.documentstoragewithframework.rest.entities.DocumentDownloaderResponseBuilder;
import com.nikitiuk.documentstoragewithframework.rest.services.RestDocService;
import com.nikitiuk.documentstoragewithframework.rest.services.helpers.ResponseService;
import com.nikitiuk.documentstoragewithframework.rest.services.helpers.ThymeleafResponseService;
import com.nikitiuk.documentstoragewithframework.rest.services.helpers.enums.Actions;
import com.nikitiuk.documentstoragewithframework.rest.services.helpers.enums.EntityTypes;
import com.nikitiuk.documentstoragewithframework.security.SecurityContext;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.beans.AutoWire;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.beans.Controller;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.beans.controller.*;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.http.DELETE;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.http.GET;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.http.POST;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.http.PUT;
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
import java.io.InputStream;
import java.util.List;

@Tag(name = "Document Controller")
@PermitAll
@Path("/docs")
@Controller
public class RestDocController {

    private static final Logger logger = LoggerFactory.getLogger(RestDocController.class);

    @AutoWire
    private RestDocService docService;

    @PermitAll
    @GET
    @Produces(MimeType.TEXT_HTML)
    public Response getDocuments(@Context RequestContext context) throws IOException {
        try {
            List<DocBean> docBeanList = docService.getDocuments(
                    (SecurityContext) context.getISecurityContext());
            return ThymeleafResponseService.visualiseEntitiesInStorage(EntityTypes.DOCUMENT, docBeanList);
        } catch (Exception e) {
            logger.error("Error at RestDocController getDocuments.", e);
            return ResponseService.errorResponse(ResponseCode.HTTP_404_NOT_FOUND,
                    String.format("Error while producing list of documents. %s", e.getMessage()));
        }
    }

    @PermitAll
    @GET
    @Path("/in-folder/{folderid}")
    @Produces(MimeType.TEXT_HTML)
    public Response getDocumentInFolder(@Context RequestContext context,
                                        @PathParam("folderid") long folderId) throws IOException {
        try {
            List<DocBean> docBeanList = docService.getDocumentsInFolder(
                    (SecurityContext) context.getISecurityContext(), folderId);
            return ThymeleafResponseService.visualiseEntitiesInStorage(EntityTypes.DOCUMENT, docBeanList);
        } catch (Exception e) {
            logger.error(String.format("Error at RestDocController getDocumentsInFolder, folderId: %d.", folderId), e);
            return ResponseService.errorResponse(ResponseCode.HTTP_404_NOT_FOUND,
                    String.format("Error while producing list of documents in folder. %s", e.getMessage()));
        }
    }

    @PermitAll
    @GET
    @Path("/{documentid}")
    public Response downloadDocument(@Context RequestContext context,
                                     @PathParam("documentid") long documentId) throws IOException {
        try {
            DocumentDownloaderResponseBuilder documentDownloaderResponseBuilder = docService.downloadDocumentById(
                    (SecurityContext) context.getISecurityContext(), documentId);
            return ResponseService.okResponseForFile(documentDownloaderResponseBuilder);
        } catch (Exception e) {
            logger.error(String.format("Error at RestDocController downloadDocumentById, id: %d.", documentId), e);
            return ResponseService.errorResponse(ResponseCode.HTTP_404_NOT_FOUND,
                    String.format("Error occurred while downloading the document by id: %d. %s",
                            documentId, e.getMessage()));
        }
    }

    @PermitAll
    @GET
    @Path("/{documentid}/content")
    @Produces(MimeType.TEXT_HTML)
    public Response getContentOfDocument(@PathParam("documentid") long documentId,
                                         @Context RequestContext context) throws IOException {
        try {
            List<String> documentContent = docService.getContentOfDocumentById(
                    (SecurityContext) context.getISecurityContext(), documentId);
            return ThymeleafResponseService.visualiseDocumentContent(documentContent);
        } catch (Exception e) {
            logger.error(String.format("Error at RestDocController getContentOfDocumentById, id: %d.", documentId), e);
            return ResponseService.errorResponse(ResponseCode.HTTP_404_NOT_FOUND,
                    String.format("Error occurred while getting content of the document by id: %d. %s",
                            documentId, e.getMessage()));
        }
    }

    @PermitAll
    @POST
    @Path("/{parentid}")
    @Consumes(MimeType.MULTIPART_FORM_DATA)
    @Produces(MimeType.TEXT_HTML)
    public Response uploadDocument(@FormDataParam("file") InputStream fileInputStream,
                                   @Context RequestContext context,
                                   @FormDataParam("designatedName") String designatedName,
                                   @PathParam("parentid") long parentFolderId) throws IOException {
        try {
            DocBean uploadedDocument = docService.uploadDocument((SecurityContext)
                    context.getISecurityContext(), fileInputStream, designatedName, parentFolderId);
            return ThymeleafResponseService.visualiseSingleEntity(
                    EntityTypes.DOCUMENT, uploadedDocument, Actions.UPLOADED);
        } catch (Exception e) {
            logger.error(String.format("Error at RestDocController uploadDocument, parentId: %d.", parentFolderId), e);
            return ResponseService.errorResponse(ResponseCode.HTTP_404_NOT_FOUND,
                    String.format("Error while uploading document: %s. %s", designatedName, e.getMessage()));
        }
    }

    @PermitAll
    @POST
    @Path("/search")
    @Produces(MimeType.TEXT_HTML)
    public Response searchInEveryDocumentWithStringQuery(@DefaultValue("") @QueryParam("query") String query,
                                                         @Context RequestContext context) throws IOException {
        try {
            String searchResult = docService.searchInEveryDocumentWithStringQuery(query,
                    (SecurityContext) context.getISecurityContext());
            return ThymeleafResponseService.visualiseSearchResult(searchResult);
        } catch (Exception e) {
            logger.error("Error at RestDocController searchInEveryDocumentWithStringQuery.", e);
            return ResponseService.errorResponse(ResponseCode.HTTP_404_NOT_FOUND,
                    String.format("Error while searching for: %s. %s", query, e.getMessage()));
        }
    }

    @PermitAll
    @PUT
    @Path("/{documentid}")
    @Consumes(MimeType.MULTIPART_FORM_DATA)
    @Produces(MimeType.TEXT_HTML)
    public Response updateDocument(@Context RequestContext context,
                                   @PathParam("documentid") long documentId,
                                   @FormDataParam("file") InputStream fileInputStream,
                                   @FormDataParam("designatedName") String designatedName) throws IOException {
        try {
            DocBean updatedDocument = docService.updateDocumentById(
                    (SecurityContext) context.getISecurityContext(), fileInputStream, documentId, designatedName);
            return ThymeleafResponseService.visualiseSingleEntity(
                    EntityTypes.DOCUMENT, updatedDocument, Actions.UPDATED);
        } catch (Exception e) {
            logger.error(String.format("Error at RestDocController updateDocumentById, id: %d.", documentId), e);
            return ResponseService.errorResponse(ResponseCode.HTTP_404_NOT_FOUND,
                    String.format("Error occurred while updating the document by id: %d. %s",
                            documentId, e.getMessage()));
        }
    }

    @PermitAll
    @DELETE
    @Path("/{documentid}")
    @Produces(MimeType.TEXT_HTML)
    public Response deleteDocument(@Context RequestContext context,
                                   @PathParam("documentid") long documentId) throws IOException {
        try {
            String deletedDocumentPath = docService.deleteDocumentById(
                    (SecurityContext) context.getISecurityContext(), documentId);
            return ResponseService.okResponseSimple(String.format(
                    "Document: '%s' deleted successfully.", deletedDocumentPath));
        } catch (Exception e) {
            logger.error(String.format("Error at RestDocController deleteDocumentById, id: %d", documentId), e);
            return ResponseService.errorResponse(ResponseCode.HTTP_404_NOT_FOUND,
                    String.format("Error occurred while deleting the document by id: %d. %s",
                            documentId, e.getMessage()));
        }
    }
}