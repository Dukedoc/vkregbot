package com.dyukov.vkregbot.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;

import com.dyukov.vkregbot.exceptions.ServiceException;

public class AppProperties implements Serializable {

   private static volatile AppProperties instance;

   private static Properties properties;

   private static final String[] propertyResources = new String[] { "/config.properties", "/google.properties", "/input.properties", "/errors.properties" };

   synchronized public static AppProperties getInstance() throws ServiceException {
      try {
         AppProperties localProperties = instance;

         if (localProperties == null) {
            synchronized (AppProperties.class) {
               if (localProperties == null) {
                  properties = new Properties();
                  for (String propertyResource : propertyResources) {
                     InputStream input = AppProperties.class.getResourceAsStream(propertyResource);
                     properties.load(input);
                  }
                  instance = localProperties = new AppProperties();
               }
            }
         }
         return localProperties;
      } catch (IOException e) {
         throw new ServiceException("Failed to load properties.", e);
      }
   }

   public String getProperty(String key) {
      return properties.getProperty(key);
   }

   String getProperty(String key, String defaultValue) {
      return properties.getProperty(key, defaultValue);
   }

   int getProperty(String key, int defaultValue) throws ServiceException {
      String property = getProperty(key);
      if (property == null) {
         return defaultValue;
      }
      try {
         return Integer.valueOf(property);
      }
      catch (NumberFormatException e) {
         throw new ServiceException(key + "is not a number.", e);
      }
   }

   int getIntProperty(String key) throws ServiceException {
      String property = getProperty(key);
      try {
         return Integer.valueOf(property);
      }
      catch (NumberFormatException e) {
         throw new ServiceException(key + "is not a number.", e);
      }
   }

}
