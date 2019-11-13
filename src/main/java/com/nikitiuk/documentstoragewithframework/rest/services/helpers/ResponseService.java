package com.nikitiuk.documentstoragewithframework.rest.services.helpers;

import com.nikitiuk.documentstoragewithframework.rest.entities.DocumentDownloaderResponseBuilder;
import com.nikitiuk.documentstoragewithframework.utils.ThymeleafUtil;
import com.nikitiuk.javabeansinitializer.server.response.Response;
import com.nikitiuk.javabeansinitializer.server.response.ResponseBuilder;
import com.nikitiuk.javabeansinitializer.server.response.ResponseCode;
import com.nikitiuk.javabeansinitializer.server.utils.MimeType;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.thymeleaf.context.Context;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class ResponseService {

    private static Tika tika = new Tika();

    public static Response errorResponse(ResponseCode code, String message) throws IOException {
        final Context ctx = new Context();
        ctx.setVariable("status", code.getStringValue());
        ctx.setVariable("message", message);
        byte[] body = new ThymeleafUtil().getEngine().process("info", ctx).getBytes();
        ResponseBuilder responseBuilder = new ResponseBuilder(code, MimeType.TEXT_HTML, body.length);
        responseBuilder.addBody(new BufferedInputStream(new ByteArrayInputStream(body)));
        return responseBuilder.buildResponse();
    }

    public static Response okResponseWithContext(String template, Context ctx) throws IOException {
        byte[] body = new ThymeleafUtil().getEngine().process(template, ctx).getBytes();
        ResponseBuilder responseBuilder = new ResponseBuilder(ResponseCode.HTTP_200_OK, MimeType.TEXT_HTML, body.length);
        responseBuilder.addBody(new BufferedInputStream(new ByteArrayInputStream(body)));
        return responseBuilder.buildResponse();
    }

    public static Response okResponseSimple(String info) throws IOException {
        byte[] body = info.getBytes();
        ResponseBuilder responseBuilder = new ResponseBuilder(ResponseCode.HTTP_200_OK, MimeType.TEXT_HTML, body.length);
        responseBuilder.addBody(new BufferedInputStream(new ByteArrayInputStream(body)));
        return responseBuilder.buildResponse();
    }

    public static Response noContentResponseSimple() throws IOException {
        ResponseBuilder responseBuilder = new ResponseBuilder(ResponseCode.HTTP_204_NO_CONTENT);
        return responseBuilder.buildResponse();
    }

    public static Response createdResponse(String template, Context ctx) throws IOException {
        byte[] body = new ThymeleafUtil().getEngine().process(template, ctx).getBytes();
        ResponseBuilder responseBuilder = new ResponseBuilder(ResponseCode.HTTP_201_CREATED, MimeType.TEXT_HTML, body.length);
        responseBuilder.addBody(new BufferedInputStream(new ByteArrayInputStream(body)));
        return responseBuilder.buildResponse();
    }

    public static Response okResponseForFile(DocumentDownloaderResponseBuilder documentDownloaderResponseBuilder) throws Exception {
        String fileName = documentDownloaderResponseBuilder.getDocumentName();
        String mimeType = tika.detect(fileName);
        ResponseBuilder responseBuilder = new ResponseBuilder(ResponseCode.HTTP_200_OK, mimeType,
                documentDownloaderResponseBuilder.getFileStream().available(), fileName);
        responseBuilder.addBody(documentDownloaderResponseBuilder.getFileStream());
        return responseBuilder.buildResponse();
    }
}