package com.dyukov.vkregbot;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JTextPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dyukov.vkregbot.config.AppProperties;
import com.dyukov.vkregbot.exceptions.ServiceException;
import com.dyukov.vkregbot.util.ApplicationConstants;
import com.dyukov.vkregbot.view.AppWindow;

public class Main {

   private static Logger logger = LoggerFactory.getLogger(Main.class);

   private static AppWindow window;

   static {
      try {
         window = new AppWindow();
      }
      catch (ServiceException e) {
         e.printStackTrace();
      }
   }

   public static void main(String[] args) {
      Main app = new Main();
      app.runSwingImplementation();
   }

   private void runSwingImplementation() {
      logger.info("Start vkregbot execution.");
      try {
         window.setSize(800, 600);
         Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
         window.setLocation(dim.width/2-window.getSize().width/2, dim.height/2-window.getSize().height/2);
         while(!window.isVisible()) {
            Thread.sleep(1000);
            window.setVisible(true);
         }
      }
      catch (Exception e) {
         logger.error(e.getLocalizedMessage());
         e.printStackTrace();
         System.exit(0);
      }
   }

   public static JTextPane getLogTextPane() throws ServiceException {
      while (window == null) {
         try {
            Thread.sleep(Long.valueOf(AppProperties.getInstance().getProperty(ApplicationConstants.RETRY_DELAY_KEY)));
         }
         catch (InterruptedException e) {
            throw new ServiceException("Failed to get log panel.", e);
         }
      }
      return window.getLogPane();
   }

}
