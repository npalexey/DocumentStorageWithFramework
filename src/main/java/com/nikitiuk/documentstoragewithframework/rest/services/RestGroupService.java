package com.nikitiuk.documentstoragewithframework.rest.services;

import com.nikitiuk.documentstoragewithframework.dao.implementations.GroupDao;
import com.nikitiuk.documentstoragewithframework.entities.GroupBean;
import com.nikitiuk.documentstoragewithframework.exceptions.NoValidDataFromSourceException;
import com.nikitiuk.documentstoragewithframework.rest.services.helpers.InspectorService;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.beans.AutoWire;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.beans.Bean;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Bean
public class RestGroupService {

    @AutoWire
    private GroupDao groupDao;

    public List<GroupBean> getGroups() throws Exception {
        return groupDao.getGroups();
    }

    public GroupBean getGroupById(Long id) throws Exception {
        InspectorService.checkIfIdIsNull(id);
        return groupDao.getById(id);
    }

    public GroupBean createGroup(GroupBean groupBean) throws Exception {
        checkOnCreateOrUpdateForNulls(groupBean);
        return groupDao.saveGroup(groupBean);
    }

    public GroupBean updateGroup(GroupBean groupBean) throws Exception {
        checkOnCreateOrUpdateForNulls(groupBean);
        return groupDao.updateGroup(groupBean);
    }

    public void deleteGroupById(Long groupId) throws Exception {
        InspectorService.checkIfIdIsNull(groupId);
        groupDao.deleteById(groupId);
    }

    private void checkOnCreateOrUpdateForNulls(GroupBean groupBean) throws NoValidDataFromSourceException {
        if (groupBean == null || StringUtils.isBlank(groupBean.getName())) {
            throw new NoValidDataFromSourceException("No valid data was passed.");
        }
    }
}