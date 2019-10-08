package com.nikitiuk.documentstoragewithframework.rest.services.helpers;

import com.nikitiuk.documentstoragewithframework.entities.*;
import com.nikitiuk.documentstoragewithframework.entities.helpers.enums.Permissions;
import com.nikitiuk.documentstoragewithframework.exceptions.NoRightsForActionException;
import com.nikitiuk.documentstoragewithframework.exceptions.NoValidDataFromSourceException;
import com.nikitiuk.documentstoragewithframework.security.UserPrincipal;
import javassist.NotFoundException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class InspectorService {

    private static final Logger logger = LoggerFactory.getLogger(InspectorService.class);

    public static void checkIfIdIsNull(Long id) throws NoValidDataFromSourceException {
        if (id == null) {
            throw new NoValidDataFromSourceException("No id was passed.");
        }
    }

    public static void checkIfStringDataIsBlank(String name) throws NoValidDataFromSourceException {
        if (StringUtils.isBlank(name)) {
            throw new NoValidDataFromSourceException("No valid name was passed.");
        }
    }

    public static void checkIfAnyWasDeleted(Integer quantity) throws NotFoundException {
        if (quantity == 0) {
            throw new NotFoundException("No entities were deleted.");
        }
    }

    public static void checkIfInputStreamIsNull(InputStream fileInputStream) throws NoValidDataFromSourceException {
        if (fileInputStream == null) {
            throw new NoValidDataFromSourceException("No file was passed to upload.");
        }
    }

    public static void checkIfDocumentIsNull(DocBean document) throws NoValidDataFromSourceException {
        if (document == null || document.getPath() == null) {
            throw new NoValidDataFromSourceException("No document with such properties exists.");
        }
    }

    public static void checkIfFolderIsNull(FolderBean folder) throws NoValidDataFromSourceException {
        if (folder == null || folder.getPath() == null) {
            throw new NoValidDataFromSourceException("No folder with such properties exists.");
        }
    }

    public static void checkIfGroupIsNull(GroupBean group) throws NoValidDataFromSourceException {
        if (group == null || group.getName() == null) {
            throw new NoValidDataFromSourceException("No group with such properties exists.");
        }
    }

    public static void checkIfUserIsNull(UserBean user) throws NoValidDataFromSourceException {
        if (user == null || user.getName() == null) {
            throw new NoValidDataFromSourceException("No user with such properties exists.");
        }
    }

    public static void checkIfDesiredCollectionIsEmpty(Collection<?> collection) throws NotFoundException {
        if(CollectionUtils.isEmpty(collection)) {
            throw new NotFoundException("No data was found with such properties.");
        }
    }

    public static Set<GroupBean> checkUserRightsForDocAndGetAllowedGroups(UserPrincipal userPrincipal, DocBean document, Permissions permissions) throws NoRightsForActionException {
        checkIfPrincipalsGroupsAreEmpty(userPrincipal);
        Set<GroupBean> userGroups = userPrincipal.getGroups();
        Set<GroupBean> groupSet = new HashSet<>();
        for (GroupBean group : userGroups) {
            if (permissions == Permissions.READ && document.checkIfDocumentHasGroup(group)) {
                groupSet.add(group);
            }
            if (permissions == Permissions.WRITE) {
                for (DocGroupPermissions docGroupPermissions : document.getDocumentsPermissions()) {
                    if (docGroupPermissions.getGroup().equals(group) && docGroupPermissions.getPermissions() == permissions) {
                        groupSet.add(group);
                    }
                }
            }
        }
        if (groupSet.isEmpty()) {
            throw new NoRightsForActionException(String.format("User %s doesn't have rights to %s this document.", userPrincipal.getName(), permissions.toString()));
        }
        return groupSet;
    }

    public static Set<GroupBean> checkUserRightsForFolderAndGetAllowedGroups(UserPrincipal userPrincipal, FolderBean folder, Permissions permissions) throws NoRightsForActionException {
        checkIfPrincipalsGroupsAreEmpty(userPrincipal);
        Set<GroupBean> userGroups = userPrincipal.getGroups();
        Set<GroupBean> groupSet = new HashSet<>();
        for (GroupBean group : userGroups) {
            if (permissions == Permissions.READ && folder.checkIfFolderHasGroup(group)) {
                groupSet.add(group);
            }
            if (permissions == Permissions.WRITE) {
                for (FolderGroupPermissions folderGroupPermissions : folder.getFoldersPermissions()) {
                    if (folderGroupPermissions.getGroup().equals(group) && folderGroupPermissions.getPermissions() == permissions) {
                        groupSet.add(group);
                    }
                }
            }
        }
        if (groupSet.isEmpty()) {
            throw new NoRightsForActionException(String.format("User %s doesn't have rights to %s to/from this folder.", userPrincipal.getName(), permissions.toString()));
        }
        return groupSet;
    }

    private static void checkIfPrincipalsGroupsAreEmpty(UserPrincipal userPrincipal) throws NoRightsForActionException {
        if (CollectionUtils.isEmpty(userPrincipal.getGroups())) {
            throw new NoRightsForActionException(String.format("User %s is not in any group.", userPrincipal.getName()));
        }
    }
}