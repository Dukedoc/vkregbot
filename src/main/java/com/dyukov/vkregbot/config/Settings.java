package com.dyukov.vkregbot.config;

import java.util.Map;

import com.dyukov.vkregbot.util.ApplicationConstants;
import com.dyukov.vkregbot.exceptions.ServiceException;
import com.dyukov.vkregbot.view.DataRetrievable;
import com.dyukov.vkregbot.vk.RegistrationMethod;

public class Settings implements DataRetrievable {

   private static volatile Settings instance;

   private AppProperties properties;

   private RegistrationMethod registrationMethod;

   private String groupId;

   private String scanWallTime;

   private String mzgbTeamName;

   private int membersCount;

   private String profileLink;

   private int dayNumber;

   private String krugozorRegMessage;

   private Settings() {
   }

   public static synchronized Settings getInstance() throws ServiceException {
      Settings localSettings = instance;
      if (localSettings == null) {
         synchronized (Settings.class) {
            localSettings = instance;
            if (localSettings == null) {
               instance = localSettings = new Settings();
               localSettings.initialize();
            }
         }
      }
      return localSettings;
   }

   private void initialize() throws ServiceException {
      properties = AppProperties.getInstance();
      registrationMethod = retrieveRegistrationMethod();
      groupId = retrieveGroupId();
      scanWallTime = retrieveScanWallTime();
      mzgbTeamName = retrieveMzgbTeamName();
      krugozorRegMessage = retrieveKrugozorRegMessage();
      membersCount = retrieveMembersCount();
      profileLink = retrieveProfileLink();
      dayNumber = retrieveDayNumber();
   }

   private RegistrationMethod retrieveRegistrationMethod() throws ServiceException {
      int mode = retrieveIntValue(ApplicationConstants.REG_MODE_KEY);
      return RegistrationMethod.getRegistrationMethod(Integer.valueOf(this.getGroupId()), mode);
   }

   private String retrieveGroupId() throws ServiceException {
      int defaultGroupId = retrieveRegistrationMethod().getDefaultGroupId();
      return getProperty(ApplicationConstants.GROUP_ID_KEY, String.valueOf(defaultGroupId));
   }

   private String retrieveScanWallTime() {
      return getProperty(ApplicationConstants.RUN_TIME_KEY);
   }

   private String retrieveMzgbTeamName() {
      return getProperty(ApplicationConstants.REG_TEAM_NAME_KEY);
   }

   private String retrieveKrugozorRegMessage() throws ServiceException {
      String messageTemplate = getProperty(ApplicationConstants.KRUGOZOR_REG_MESSAGE_KEY);
      krugozorRegMessage = messageTemplate
            .replace("%teamName%", retrieveMzgbTeamName())
            .replace("%membersCount%", String.valueOf(retrieveMembersCount()));
      return krugozorRegMessage;
   }

   private int retrieveMembersCount() throws ServiceException {
      return retrieveIntValue(ApplicationConstants.REG_TEAM_PERSONS_COUNT_KEY);
   }

   private int retrieveIntValue(String key, int defaultValue) throws ServiceException {
      return getProperty(key, defaultValue);
   }

   private int retrieveIntValue(String key) throws ServiceException {
      return properties.getIntProperty(key);
   }

   private String retrieveProfileLink() {
      return getProperty(ApplicationConstants.REG_PROFILE_LINK_KEY);
   }

   private int retrieveDayNumber() throws ServiceException {
      return retrieveIntValue(ApplicationConstants.REG_DAY_NUMBER_KEY);
   }

   private String getProperty(String key) {
      return properties.getProperty(key);
   }

   private int getProperty(String key, int defaultValue) throws ServiceException {
      return properties.getProperty(key, defaultValue);
   }

   private String getProperty(String key, String defaultValue) {
      return properties.getProperty(key, defaultValue);
   }

   public RegistrationMethod getRegistrationMethod() {
      return registrationMethod;
   }

   public String getGroupId() {
      return groupId;
   }

   public String getScanWallTime() {
      return scanWallTime;
   }

   public String getTeamName() {
      return mzgbTeamName;
   }

   public String getKrugozorRegMessage() {
      return krugozorRegMessage;
   }

   public int getTeamMembersCount() {
      return membersCount;
   }

   public String getVkProfileLink() {
      return profileLink;
   }

   public int getDayNumber() {
      return dayNumber;
   }

   @Override
   public String toString() {
      return "Settings{" + "registrationMethod=" + registrationMethod.name() + ", groupId=" + groupId + ", scanWallTime=" + scanWallTime + ", mzgbTeamName='" + mzgbTeamName + '\''
            + ", krugozorRegMessage='" + krugozorRegMessage + '\'' + ", membersCount=" + membersCount + ", profileLink='" + profileLink + '\'' + ", dayNumber=" + dayNumber + '}';
   }

   @Override
   public void showErrors(Map<String, String> errors) {

   }

   @Override
   public void modeChanged() {
      // Do nothing
   }
}
