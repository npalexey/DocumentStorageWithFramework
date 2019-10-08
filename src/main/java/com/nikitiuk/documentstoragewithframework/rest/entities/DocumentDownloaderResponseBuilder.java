package com.nikitiuk.documentstoragewithframework.rest.entities;

import java.io.BufferedInputStream;

public class DocumentDownloaderResponseBuilder {

    private BufferedInputStream fileStream;
    private String documentName;

    public DocumentDownloaderResponseBuilder() {

    }

    public DocumentDownloaderResponseBuilder(BufferedInputStream fileStream, String documentName) {
        this.fileStream = fileStream;
        this.documentName = documentName;
    }

    public BufferedInputStream getFileStream() {
        return fileStream;
    }

    public void setFileStream(BufferedInputStream fileStream) {
        this.fileStream = fileStream;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }
}
