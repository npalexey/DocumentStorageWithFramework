package com.nikitiuk.documentstoragewithframework.rest.entities;

public class DocGroupPermissionsRequest {

    private Long groupId;
    private Long documentId;

    public DocGroupPermissionsRequest() {

    }

    public DocGroupPermissionsRequest(long groupId, long documentId) {
        this.groupId = groupId;
        this.documentId = documentId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }
}
