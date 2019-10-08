package com.nikitiuk.documentstoragewithframework.rest.services.helpers.enums;

public enum EntityTypes {

    DOCUMENT, FOLDER, USER, GROUP, DOC_GROUP_PERMISSIONS, FOLDER_GROUP_PERMISSIONS;

    public static Boolean contains(String string) {
        for(EntityTypes entityTypes : EntityTypes.values()){
            if (string.equals(entityTypes.name())){
                return true;
            }
        }
        return false;
    }
}