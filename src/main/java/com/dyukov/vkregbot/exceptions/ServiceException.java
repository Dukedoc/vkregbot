package com.dyukov.vkregbot.exceptions;

public class ServiceException extends Exception {

   public ServiceException() {
      super();
   }

   public ServiceException(String message, Throwable cause) {
      super(message, cause);
   }

   public ServiceException(String s) {
      super(s);
   }
}
