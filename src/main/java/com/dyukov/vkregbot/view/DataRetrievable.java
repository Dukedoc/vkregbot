package com.dyukov.vkregbot.view;

import java.util.Map;

import com.dyukov.vkregbot.exceptions.ServiceException;
import com.dyukov.vkregbot.vk.RegistrationMethod;

public interface DataRetrievable {

   RegistrationMethod getRegistrationMethod() throws ServiceException;

   String getGroupId();

   String getScanWallTime();

   String getTeamName();

   String getVkProfileLink();

   int getTeamMembersCount();

   int getDayNumber();

   void showErrors(Map<String, String> errors) throws ServiceException;

   void modeChanged() throws ServiceException;
}
