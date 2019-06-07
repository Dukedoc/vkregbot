package com.dyukov.vkregbot.vk;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dyukov.vkregbot.config.AppProperties;
import com.dyukov.vkregbot.exceptions.ServiceException;
import com.dyukov.vkregbot.google.GoogleFormFiller;
import com.dyukov.vkregbot.util.DateTimeUtil;
import com.dyukov.vkregbot.view.DataRetrievable;

public enum RegistrationMethod {

   MZGB(1) {
      @Override
      public void register(String token, VkUtil vkUtil, DataRetrievable registerData) throws ServiceException {
         String url = vkUtil.getGoogleDocUrl(token, registerData);
         if (url != null)
            new GoogleFormFiller().registerInForm(url, registerData);
         else throw new ServiceException("Failed to retrieve google form url.");
      }

      @Override
      public int getDefaultGroupId() {
         return DEFAULT_MZGB_GROUP_ID;
      }

      @Override
      public String getRegistrationMethodTitle() throws ServiceException {
         return AppProperties.getInstance().getProperty("reg.mzgb.method.title");
      }

      @Override
      public Map<String, String> validate(DataRetrievable dataPanel) throws ServiceException {
         Map<String, String> errors = validateCommonFields(dataPanel);
         if (dataPanel.getVkProfileLink() == null || dataPanel.getVkProfileLink().isEmpty())
            errors.put("vkProfile", AppProperties.getInstance().getProperty("vk.profile.is.empty.err"));
         return errors;
      }
   },

   KRUGOZOR(2) {
      @Override
      public void register(String token, VkUtil vkUtil, DataRetrievable registerData) throws ServiceException {
         vkUtil.makeKrugozorRegistration(token, new Date(), registerData);
      }

      @Override
      public int getDefaultGroupId() {
         return DEFAULT_KRUGOZOR_GROUP_ID;
      }

      @Override
      public String getRegistrationMethodTitle() throws ServiceException {
         return AppProperties.getInstance().getProperty("reg.krugozor.method.title");
      }

      @Override
      public Map<String, String> validate(DataRetrievable dataPanel) throws ServiceException {
         return validateCommonFields(dataPanel);
      }
   };

   public static final int DEFAULT_MZGB_GROUP_ID = -151368365;

   public static final int DEFAULT_KRUGOZOR_GROUP_ID = -158348383;

   private int registrationMode;

   RegistrationMethod(int registrationMode) {
      this.registrationMode = registrationMode;
   }

   public static RegistrationMethod getRegistrationMethod(int groupId, int mode) {
      int correctGroupId = groupId > 0 ? -groupId : groupId;
      if (correctGroupId == DEFAULT_MZGB_GROUP_ID)
         return RegistrationMethod.valueOf("MZGB");
      if (correctGroupId == DEFAULT_KRUGOZOR_GROUP_ID)
         return RegistrationMethod.valueOf("KRUGOZOR");
      for (RegistrationMethod currentMethod : RegistrationMethod.values()) {
         if (mode == currentMethod.getRegistrationMode()) {
            return currentMethod;
         }
      }
      throw new IllegalArgumentException("Unknown registration mode " + mode);
   }

   public static RegistrationMethod getRegistrationMethod(int groupId, String title) throws ServiceException {
      int correctGroupId = groupId > 0 ? -groupId : groupId;
      if (correctGroupId == DEFAULT_MZGB_GROUP_ID)
         return RegistrationMethod.valueOf("MZGB");
      if (correctGroupId == DEFAULT_KRUGOZOR_GROUP_ID)
         return RegistrationMethod.valueOf("KRUGOZOR");
      for (RegistrationMethod registrationMethod : RegistrationMethod.values()) {
         if (registrationMethod.getRegistrationMethodTitle().equals(title)) {
            return registrationMethod;
         }
      }
      throw new ServiceException("Unknown registration method " + title);
   }

   public static List<String> getRegistrationMethodTitles() throws ServiceException {
      List<String> titles = new ArrayList<>();
      for (RegistrationMethod registrationMethod : RegistrationMethod.values()) {
         titles.add(registrationMethod.getRegistrationMethodTitle());
      }
      return titles;
   }

   protected Map<String, String> validateCommonFields(DataRetrievable dataPanel) throws ServiceException {
      Map<String, String> errors = new HashMap<>();
      AppProperties appProperties = AppProperties.getInstance();
      if (dataPanel.getGroupId() == null || dataPanel.getGroupId().isEmpty())
         errors.put("groupId", appProperties.getProperty("group.id.is.empty.err"));
      else try {
         Integer.parseInt(dataPanel.getGroupId());
      } catch (NumberFormatException e) {
         errors.put("groupId", appProperties.getProperty("group.id.is.not.a.num.err"));
      }
      if (dataPanel.getTeamName() == null || dataPanel.getTeamName().isEmpty())
         errors.put("teamName", appProperties.getProperty("team.name.is.empty.err"));
      if (dataPanel.getScanWallTime() == null || dataPanel.getScanWallTime().isEmpty())
         errors.put("runTime", appProperties.getProperty("run.time.is.empty.err"));
      else {
         String runTimeStr = dataPanel.getScanWallTime();
         DateTimeUtil.parseDate(runTimeStr);
      }
      return errors;
   }

   public abstract void register(String token, VkUtil vkUtil, DataRetrievable registerData) throws ServiceException;

   public abstract int getDefaultGroupId();

   public abstract String getRegistrationMethodTitle() throws ServiceException;

   public abstract Map<String, String> validate(DataRetrievable dataPanel) throws ServiceException;

   public int getRegistrationMode() {
      return registrationMode;
   }

}
