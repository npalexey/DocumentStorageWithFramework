package com.nikitiuk.documentstoragewithframework.entities.helpers.enums;

public enum Permissions {

    WRITE, READ;

    public static Boolean contains(String string) {
        for(Permissions permissions : Permissions.values()){
            if (string.equals(permissions.name())){
                return true;
            }
        }
        return false;
    }
}
