package com.dyukov.vkregbot.util;

import java.util.Date;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dyukov.vkregbot.exceptions.ServiceException;
import com.dyukov.vkregbot.view.DataRetrievable;
import com.dyukov.vkregbot.vk.VkUtil;

public class RegisterTask extends TimerTask {

   private VkUtil vkUtil;

   private String token;

   private Logger logger = LoggerFactory.getLogger(RegisterTask.class);

   private DataRetrievable registerData;

   public RegisterTask(DataRetrievable registerData) throws ServiceException {
      this.registerData = registerData;
      logger.info("Instantiating vk utils");
      this.vkUtil = VkUtil.getInstance();
      logger.info("Vk utils are instantiated.");
      logger.info("Retrieving vk session token...");
      token = vkUtil.authorize(registerData);
      if (token == null) {
         throw new ServiceException("Token is not retrieved.");
      }
      logger.info("Vk session token is retrieved.");
      logger.info("Registration task is scheduled.");
   }

   @Override
   public void run() {
      if (token != null) {
         try {
            registerData.getRegistrationMethod().register(token, vkUtil, registerData);
         }
         catch (ServiceException e) {
            logger.error("Failed to run task", e);
         }
         logger.info("Execution finished on " + new Date());
      }
   }
}
