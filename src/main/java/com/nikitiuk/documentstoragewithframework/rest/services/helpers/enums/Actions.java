package com.nikitiuk.documentstoragewithframework.rest.services.helpers.enums;

public enum Actions {

    CREATED, UPDATED, FOUND, UPLOADED, SET;

    public static Boolean contains(String string) {
        for(Actions actions : Actions.values()){
            if (string.equals(actions.name())){
                return true;
            }
        }
        return false;
    }
}
