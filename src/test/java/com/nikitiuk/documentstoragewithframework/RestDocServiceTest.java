package com.nikitiuk.documentstoragewithframework;

import com.nikitiuk.documentstoragewithframework.rest.services.RestDocService;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;

class RestDocServiceTest {

    /*@Test
    void downloadFileTest() {
        Logger logger = LoggerFactory.getLogger(RestDocService.class);
        try {
            Client client = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();
            FileDataBodyPart filePart = new FileDataBodyPart("file", new File("/home/npalexey/Downloads/codeconventions-150003.pdf"));
            FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
            FormDataMultiPart multipart = (FormDataMultiPart) formDataMultiPart.field("foo", "bar").bodyPart(filePart);
            WebTarget target = client.target("http://localhost:9999/DocumentStorageWithSearchCapability/rest/doc/somenewpdf.pdf");
            Response response = target.request().post(Entity.entity(multipart, multipart.getMediaType()));
            //Use response object to verify upload success
            formDataMultiPart.close();
            multipart.close();
        } catch (IOException e) {
            logger.error("Exception caught: " + e);
        }
    }*/
}