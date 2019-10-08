package com.nikitiuk.documentstoragewithframework.rest.services;

import com.nikitiuk.documentstoragewithframework.dao.implementations.UserDao;
import com.nikitiuk.documentstoragewithframework.entities.UserBean;
import com.nikitiuk.documentstoragewithframework.exceptions.NoValidDataFromSourceException;
import com.nikitiuk.documentstoragewithframework.rest.services.helpers.InspectorService;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.beans.AutoWire;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.beans.Bean;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Bean
public class RestUserService {

    @AutoWire
    private UserDao userDao;

    public List<UserBean> getUsers() throws Exception {
        return userDao.getUsers();
    }

    public UserBean getSingleUser(Long userId) throws Exception {
        InspectorService.checkIfIdIsNull(userId);
        return userDao.getById(userId);
    }

    public UserBean createUser(UserBean userBean) throws Exception {
        checkOnCreateOrUpdateForNulls(userBean);
        return userDao.saveUser(userBean);
    }

    public UserBean updateUser(UserBean userBean) throws Exception {
        checkOnCreateOrUpdateForNulls(userBean);
        return userDao.updateUser(userBean);
    }

    public void deleteUser(Long userId) throws Exception {
        InspectorService.checkIfIdIsNull(userId);
        userDao.deleteById(userId);
    }

    private void checkOnCreateOrUpdateForNulls(UserBean userBean) throws NoValidDataFromSourceException {
        if (userBean == null || StringUtils.isBlank(userBean.getName())) {
            throw new NoValidDataFromSourceException("No valid data was passed.");
        }
    }
}
