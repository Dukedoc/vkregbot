package com.dyukov.vkregbot.vk;

import java.io.IOException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dyukov.vkregbot.exceptions.AuthorizationException;
import com.dyukov.vkregbot.exceptions.ServiceException;
import com.dyukov.vkregbot.util.file.FileUtil;
import com.dyukov.vkregbot.view.DataRetrievable;

public class VkUtil {

    private static volatile VkUtil instance;

    private VkApplication vkApplication;

    private Logger logger = LoggerFactory.getLogger(VkUtil.class);

    private VkUtil() {
    }

    public static VkUtil getInstance() {
        synchronized (VkUtil.class) {
            if (instance == null) {
                instance = new VkUtil();
                instance.vkApplication = new VkApplication();
            }
            return instance;
        }
    }

    public String authorize(DataRetrievable data) throws ServiceException {
        String sessionCode = null;
        try {
            sessionCode = FileUtil.getInstance().readSessionCode();
        } catch (IOException e) {
            logger.info("Failed to retrieve session code.");
        }
        if (sessionCode != null) {
            logger.info("Found cached session token. Checking it...");
            try {
                vkApplication.getTopPost(sessionCode, Integer.valueOf(data.getGroupId()));
                logger.info("Cached session code is valid.");
            } catch (ServiceException e) {
                if (e instanceof AuthorizationException) {
                    logger.info("Cached session token is expired.");
                    sessionCode = null;
                } else {
                    throw e;
                }
            }
        }
        if (sessionCode == null) {
            logger.info("Retrieveing new session code...");
            sessionCode = vkApplication.authorize();
            logger.info("Session code is retrieved");
            try {
                logger.info("Persisting session code...");
                FileUtil.getInstance().persistSessionCode(sessionCode);
                logger.info("Session code is persisted.");
            } catch (IOException e) {
                logger.info("Failed to persisit session code");
            }
        }
        return sessionCode;
    }

    String getGoogleDocUrl(String code, DataRetrievable registerData) throws ServiceException {
        return vkApplication.getGoogleUrl(code, registerData);
    }

    void makeKrugozorRegistration(String code, Date date, DataRetrievable registerData) throws ServiceException {
        vkApplication.krugozor_reg(code, date, registerData);
    }

}
