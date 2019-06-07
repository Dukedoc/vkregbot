package com.dyukov.vkregbot;

import java.util.Date;
import java.util.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dyukov.vkregbot.exceptions.ServiceException;
import com.dyukov.vkregbot.util.DateTimeUtil;
import com.dyukov.vkregbot.util.RegisterTask;
import com.dyukov.vkregbot.view.DataRetrievable;

public class AppLauncher implements Runnable {

   private DataRetrievable data;
   private Logger logger = LoggerFactory.getLogger(AppLauncher.class);
   private Timer timer;


   public AppLauncher(DataRetrievable data) {
      this.data = data;
   }

   @Override
   public void run() {
      try {
         launchApplication(data);
      }
      catch (ServiceException e) {
         logger.error("Failed to run registration", e);
      }
   }

   private void launchApplication(DataRetrievable registerData) throws ServiceException {
      Date runDate = DateTimeUtil.parseDate(registerData.getScanWallTime());
      logger.info("Start wall scanning on: " + runDate);
      timer = new Timer();
      logger.info("Scheduling timer...");
      timer.schedule(new RegisterTask(registerData), runDate);
   }

   public Timer getTimer() {
      return timer;
   }
}
