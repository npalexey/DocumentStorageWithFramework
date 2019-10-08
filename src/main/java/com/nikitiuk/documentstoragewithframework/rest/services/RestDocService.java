package com.nikitiuk.documentstoragewithframework.rest.services;

import com.nikitiuk.documentstoragewithframework.dao.implementations.DocDao;
import com.nikitiuk.documentstoragewithframework.dao.implementations.DocGroupPermissionsDao;
import com.nikitiuk.documentstoragewithframework.dao.implementations.FolderDao;
import com.nikitiuk.documentstoragewithframework.entities.DocBean;
import com.nikitiuk.documentstoragewithframework.entities.FolderBean;
import com.nikitiuk.documentstoragewithframework.entities.GroupBean;
import com.nikitiuk.documentstoragewithframework.entities.helpers.DtoDaoTransformer;
import com.nikitiuk.documentstoragewithframework.entities.helpers.enums.Permissions;
import com.nikitiuk.documentstoragewithframework.exceptions.NoValidDataFromSourceException;
import com.nikitiuk.documentstoragewithframework.rest.entities.DocumentDownloaderResponseBuilder;
import com.nikitiuk.documentstoragewithframework.rest.services.helpers.InspectorService;
import com.nikitiuk.documentstoragewithframework.security.SecurityContext;
import com.nikitiuk.documentstoragewithframework.services.LocalStorageService;
import com.nikitiuk.documentstoragewithframework.services.SearchResultsModifier;
import com.nikitiuk.documentstoragewithframework.services.SolrService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RestDocService {

    private static final Logger logger = LoggerFactory.getLogger(RestDocService.class);
    private static ExecutorService executorService = Executors.newSingleThreadExecutor();
    private DocDao docDao = new DocDao();
    private FolderDao folderDao = new FolderDao();
    private DocGroupPermissionsDao docGroupPermissionsDao = new DocGroupPermissionsDao();
    private LocalStorageService localStorageService = new LocalStorageService();
    private DtoDaoTransformer dtoDaoTransformer = new DtoDaoTransformer();

    public List<DocBean> getDocuments(SecurityContext securityContext) throws Exception {
        return docDao.getDocumentsForUser(dtoDaoTransformer.userPrincipalToUserBean(securityContext.getUserPrincipal()));
    }

    public List<DocBean> getDocumentsInFolder(SecurityContext securityContext, Long folderId) throws Exception {
        FolderBean folder = getFolderByGivenId(folderId);
        return docDao.getDocumentsForUserInFolder(dtoDaoTransformer.userPrincipalToUserBean(securityContext.getUserPrincipal()), folder);
    }

    public List<String> getContentOfDocumentById(SecurityContext securityContext, Long documentId) throws Exception {
        InspectorService.checkIfIdIsNull(documentId);
        DocBean documentToGetContentOf = docDao.getById(documentId);
        InspectorService.checkIfDocumentIsNull(documentToGetContentOf);
        InspectorService.checkUserRightsForDocAndGetAllowedGroups(securityContext.getUserPrincipal(), documentToGetContentOf, Permissions.READ);
        List<String> docContent = new ArrayList<>();
        docContent.add(String.format("Document id: %d, name: %s, path: %s",
                documentToGetContentOf.getId(), documentToGetContentOf.getName(), documentToGetContentOf.getPath()));
        double start = System.currentTimeMillis();
        docContent.addAll(localStorageService.documentContentGetter(documentToGetContentOf.getPath()));
        logger.debug(String.format("It took: %fms to get content of document.", (System.currentTimeMillis() - start) / 1000d));
        return docContent;
    }

    public DocumentDownloaderResponseBuilder downloadDocumentById(SecurityContext securityContext, Long documentId) throws Exception {
        InspectorService.checkIfIdIsNull(documentId);
        DocBean documentToDownload = docDao.getById(documentId);
        InspectorService.checkIfDocumentIsNull(documentToDownload);
        InspectorService.checkUserRightsForDocAndGetAllowedGroups(securityContext.getUserPrincipal(), documentToDownload, Permissions.READ);
        return new DocumentDownloaderResponseBuilder(localStorageService.fileDownloader(documentToDownload.getPath()), documentToDownload.getName());
    }

    public DocBean uploadDocument(SecurityContext securityContext, InputStream fileInputStream,
                                  String designatedName, Long parentFolderId) throws Exception {
        InspectorService.checkIfInputStreamIsNull(fileInputStream);
        InspectorService.checkIfIdIsNull(parentFolderId);
        final String trimmedDesignatedName = replaceSlashesInDesignatedNameAndTrim(designatedName);
        InspectorService.checkIfStringDataIsBlank(trimmedDesignatedName);
        FolderBean folderBean = getFolderByGivenId(parentFolderId);
        InspectorService.checkIfFolderIsNull(folderBean);
        Set<GroupBean> allowedGroups = InspectorService.checkUserRightsForFolderAndGetAllowedGroups(securityContext.getUserPrincipal(), folderBean, Permissions.READ);
        localStorageService.fileUploader(fileInputStream, folderBean.getPath() + trimmedDesignatedName);
        DocBean createdDoc = saveDocToDBAndAssignPermissionsForAllowedGroups(allowedGroups, folderBean.getPath(), trimmedDesignatedName);
        Runnable addTask = () -> {
            try {
                SolrService.indexDocumentWithSolr(folderBean.getPath() + trimmedDesignatedName,
                        new Tika().detect(trimmedDesignatedName));
            } catch (IOException | SolrServerException e) {
                logger.error("Error wile indexing with Solr.", e);
                throw new WebApplicationException("Error while indexing document. Please, try again.");
            }
        };
        executorService.execute(addTask);
        return createdDoc;
    }

    public String searchInEveryDocumentWithStringQuery(String query, SecurityContext securityContext) throws Exception {
        StringBuilder contentBuilder = new StringBuilder();
        if (StringUtils.isBlank(query)) {
            throw new NoValidDataFromSourceException("Query is blank.");
        }
        List<DocBean> permittedDocs = docDao.getDocumentsForUser(dtoDaoTransformer.userPrincipalToUserBean(securityContext.getUserPrincipal()));
        QueryResponse response = SolrService.searchInDocumentsByQuery(query);
        contentBuilder.append(SearchResultsModifier.getSearchResultForPermittedDocs(response, query, permittedDocs));
        return contentBuilder.toString().replace("\n", "<br/>");
    }

    public DocBean updateDocumentById(SecurityContext securityContext, InputStream fileInputStream,
                                      Long documentId, String designatedName) throws Exception {
        InspectorService.checkIfIdIsNull(documentId);
        DocBean documentToUpdate = docDao.getById(documentId);
        InspectorService.checkIfDocumentIsNull(documentToUpdate);
        InspectorService.checkUserRightsForDocAndGetAllowedGroups(securityContext.getUserPrincipal(), documentToUpdate, Permissions.WRITE);
        String modifiedDesignatedName = replaceSlashesInDesignatedNameAndTrim(designatedName);
        if (StringUtils.isBlank(modifiedDesignatedName) || modifiedDesignatedName.equals(documentToUpdate.getName())) {
            if (fileInputStream == null) {
                throw new NoValidDataFromSourceException("No valid data passed to update document");
            }
            return updateWithSameName(fileInputStream, documentToUpdate);
        } else {
            return updateWithNewName(fileInputStream, documentToUpdate, designatedName);
        }
    }

    private DocBean updateWithSameName(InputStream fileInputStream, DocBean documentToUpdate) throws Exception {
        String docNameForContentTypeCheck = localStorageService.fileUpdater(fileInputStream, documentToUpdate.getPath());
        Runnable putTask = () -> {
            try {
                SolrService.indexDocumentWithSolr(documentToUpdate.getPath(), new Tika().detect(docNameForContentTypeCheck));
            } catch (IOException | SolrServerException e) {
                logger.error("Error wile updating file in Solr.", e);
                throw new WebApplicationException("Error while indexing document. Please, try again.");
            }
        };
        executorService.execute(putTask);
        return documentToUpdate;
    }

    private DocBean updateWithNewName(InputStream fileInputStream, DocBean documentToUpdate, String designatedName) throws Exception {
        documentToUpdate.setName(designatedName);
        String oldPath = documentToUpdate.getPath();
        String newPath = documentToUpdate.getPath()
                .substring(0, documentToUpdate.getPath().lastIndexOf("/") + 1) + designatedName;
        documentToUpdate.setPath(newPath);
        if (fileInputStream == null) {
            localStorageService.renameFile(oldPath, newPath);
        } else {
            localStorageService.fileOrRecursiveFolderDeleter(oldPath);
            localStorageService.fileUploader(fileInputStream, newPath);
        }
        Runnable putTask = () -> {
            try {
                SolrService.deleteDocumentOrRecursiveFolderFromSolrIndex(oldPath);
                SolrService.indexDocumentWithSolr(newPath, new Tika().detect(designatedName));
            } catch (IOException | SolrServerException e) {
                logger.error("Error wile updating file in Solr.", e);
                throw new WebApplicationException("Error while indexing document. Please, try again.");
            }
        };
        executorService.execute(putTask);
        return docDao.updateDocument(documentToUpdate);
    }

    public String deleteDocumentById(SecurityContext securityContext, Long documentId) throws Exception {
        InspectorService.checkIfIdIsNull(documentId);
        DocBean documentToDelete = docDao.getById(documentId);
        InspectorService.checkIfDocumentIsNull(documentToDelete);
        InspectorService.checkUserRightsForDocAndGetAllowedGroups(securityContext.getUserPrincipal(), documentToDelete, Permissions.WRITE);
        localStorageService.fileOrRecursiveFolderDeleter(documentToDelete.getPath());
        docDao.deleteDocument(documentToDelete.getId());
        Runnable deleteTask = () -> {
            try {
                SolrService.deleteDocumentOrRecursiveFolderFromSolrIndex(documentToDelete.getPath());
            } catch (IOException | SolrServerException e) {
                logger.error("Error wile deleting from Solr.", e);
                throw new WebApplicationException("Error while deleting document from index. Please, try again.");
            }
        };
        executorService.execute(deleteTask);
        return documentToDelete.getPath();
    }

    private FolderBean getFolderByGivenId(Long folderId) {
        if (folderId == null || folderId == 0) {
            return folderDao.getById(Long.parseLong(System.getProperty("default.folder")));
        } else {
            return folderDao.getById(folderId);
        }
    }

    private DocBean saveDocToDBAndAssignPermissionsForAllowedGroups(Set<GroupBean> allowedGroups, String folderPath, String trimmedDesignatedName) throws Exception {
        DocBean createdDoc = docDao.saveDocument(new DocBean(trimmedDesignatedName, folderPath + trimmedDesignatedName));
        if (CollectionUtils.isNotEmpty(allowedGroups)) {
            for (GroupBean groupBean : allowedGroups) {
                docGroupPermissionsDao.setWriteForDocumentForGroup(createdDoc, groupBean);
            }
            docDao.refresh(createdDoc);
        }
        return createdDoc;
    }

    private String replaceSlashesInDesignatedNameAndTrim(String designatedName) {
        return StringUtils.trim(StringUtils.replaceEach(designatedName, new String[] { "\\", "/" }, new String[] { "", "" }));
    }
}