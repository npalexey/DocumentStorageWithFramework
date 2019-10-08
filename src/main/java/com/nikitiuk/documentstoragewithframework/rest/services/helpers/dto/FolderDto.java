package com.nikitiuk.documentstoragewithframework.rest.services.helpers.dto;

public class FolderDto {

    private Long id;

    private String path;

    public FolderDto() {

    }

    public FolderDto(String path) {
        this.path = path;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
