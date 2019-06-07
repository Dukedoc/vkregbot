package com.dyukov.vkregbot.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.dyukov.vkregbot.config.AppProperties;
import com.dyukov.vkregbot.exceptions.ServiceException;

public class DateTimeUtil {

   public static Date parseDate(String dateStr) throws ServiceException {
      String dateFormatStr = getProperty(ApplicationConstants.DATE_FORMAT_KEY);
      DateFormat dateFormat = new SimpleDateFormat(dateFormatStr);
      try {
         return dateFormat.parse(dateStr);
      }
      catch (ParseException e) {
         throw new ServiceException("Failed to parse date " + dateStr + " by format " + dateFormatStr, e);
      }
   }

   public static String formatDate(Date date) throws ServiceException {
      return new SimpleDateFormat(AppProperties.getInstance().getProperty(ApplicationConstants.DATE_FORMAT_KEY))
            .format(date);
   }

   private static String getProperty(String key) throws ServiceException {
      return AppProperties.getInstance().getProperty(key);
   }

}
