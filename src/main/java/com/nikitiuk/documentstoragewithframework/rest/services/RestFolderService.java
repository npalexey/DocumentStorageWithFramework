package com.nikitiuk.documentstoragewithframework.rest.services;

import com.nikitiuk.documentstoragewithframework.dao.implementations.FolderDao;
import com.nikitiuk.documentstoragewithframework.entities.FolderBean;
import com.nikitiuk.documentstoragewithframework.entities.helpers.DtoDaoTransformer;
import com.nikitiuk.documentstoragewithframework.entities.helpers.enums.Permissions;
import com.nikitiuk.documentstoragewithframework.rest.services.helpers.InspectorService;
import com.nikitiuk.documentstoragewithframework.security.SecurityContext;
import com.nikitiuk.documentstoragewithframework.services.LocalStorageService;
import com.nikitiuk.documentstoragewithframework.services.SolrService;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.beans.AutoWire;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.beans.Bean;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Bean
public class RestFolderService {

    private static final Logger logger = LoggerFactory.getLogger(RestFolderService.class);
    private static ExecutorService executorService = Executors.newSingleThreadExecutor();
    @AutoWire
    private FolderDao folderDao;
    @AutoWire
    private DtoDaoTransformer dtoDaoTransformer;
    @AutoWire
    private LocalStorageService localStorageService;

    public List<FolderBean> getFolders(SecurityContext securityContext) throws Exception {
        return folderDao.getFoldersForUser(dtoDaoTransformer.userPrincipalToUserBean(securityContext.getUserPrincipal()));
    }

    public String deleteFolderById(SecurityContext securityContext, Long folderId) throws Exception {
        InspectorService.checkIfIdIsNull(folderId);
        FolderBean folderToDelete = folderDao.getById(folderId);
        InspectorService.checkIfFolderIsNull(folderToDelete);
        InspectorService.checkUserRightsForFolderAndGetAllowedGroups(securityContext.getUserPrincipal(), folderToDelete, Permissions.WRITE);
        localStorageService.fileOrRecursiveFolderDeleter(folderToDelete.getPath());
        /*for(DocBean docBean : docDao.getDocumentsForUserInFolder(
                dtoDaoTransformer.userPrincipalToUserBean(securityContext.getUserPrincipal()), folderToDelete)) {
            docDao.deleteDocument(docBean.getId());
        }*/
        folderDao.deleteFolder(folderToDelete.getId());
        Runnable deleteTask = () -> {
            try {
                SolrService.deleteDocumentOrRecursiveFolderFromSolrIndex(folderToDelete.getPath());
            } catch (IOException | SolrServerException e) {
                logger.error("Error wile deleting from Solr.", e);
                throw new WebApplicationException("Error while deleting document from index. Please, try again.");
            }
        };
        executorService.execute(deleteTask);
        return folderToDelete.getPath();
    }
}