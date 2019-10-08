package com.nikitiuk.documentstoragewithframework.rest.services.helpers;

import com.nikitiuk.documentstoragewithframework.rest.services.helpers.enums.Actions;
import com.nikitiuk.documentstoragewithframework.rest.services.helpers.enums.EntityTypes;
import com.nikitiuk.javabeansinitializer.server.response.Response;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.util.List;

public class ThymeleafResponseService {

    public static Response visualiseEntitiesInStorage(EntityTypes entityType, List<?> entityList) throws IOException {
        final Context ctx = new Context();
        ctx.setVariable("entityType", entityType);
        ctx.setVariable("inStorage", entityList);
        return ResponseService.okResponseWithContext("storagehome", ctx);
    }

    public static Response visualiseSingleEntity(EntityTypes entityType, Object entity, Actions action) throws IOException {
        final Context ctx = new Context();
        ctx.setVariable("entityType", entityType);
        ctx.setVariable("entity", entity);
        ctx.setVariable("action", action);
        if(action == Actions.CREATED || action == Actions.UPLOADED) {
            return ResponseService.createdResponse("singleentity", ctx);
        }
        return ResponseService.okResponseWithContext("singleentity", ctx);
    }

    public static Response visualiseDocumentContent(List<String> documentContent) throws IOException {
        final Context ctx = new Context();
        ctx.setVariable("docContent", documentContent);
        ctx.setVariable("filePath", documentContent.get(0));
        return ResponseService.okResponseWithContext("content", ctx);
    }

    public static Response visualiseSearchResult(String searchResult) throws IOException {
        final Context ctx = new Context();
        ctx.setVariable("searchResult", searchResult);
        return ResponseService.okResponseWithContext("search", ctx);
    }
}