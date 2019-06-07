package com.dyukov.vkregbot.google;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dyukov.vkregbot.config.AppProperties;
import com.dyukov.vkregbot.exceptions.ServiceException;
import com.dyukov.vkregbot.util.ApplicationConstants;
import com.dyukov.vkregbot.view.DataRetrievable;

public class GoogleFormFiller {

   private static final String HIDDEN = "hidden";

   private static final String INPUT_TAG = "input";

   private static final String TEXTAREA = "textarea";

   private static final String LISTITEM_TYPE = "listitem";

   private static final String LISTBOX_TYPE = "listbox";

   private static final String RADIOGROUP_TYPE = "radiogroup";

   private static final String ROLE_ATTR = "role";

   private AppProperties appProperties;
   private Logger logger = LoggerFactory.getLogger(GoogleFormFiller.class);

   public GoogleFormFiller() throws ServiceException {
      appProperties = AppProperties.getInstance();
   }

   public void registerInForm(String formUrl, DataRetrievable registerData) throws ServiceException {
      if (formUrl == null) {
         throw new ServiceException("Google form url is not defined.");
      }
      try {
         logger.info("Starting register in the form " + formUrl);
         Document document = Jsoup.connect(formUrl).get();
         Elements inputs = document.select(getProperty(ApplicationConstants.INPUTS_SELECTOR_KEY));
         logger.info("Inputs retrieved. Size: " + inputs.size());
         logger.info("Starting handling inputs...");
         handleInputs(document, formUrl, inputs, registerData);
      }
      catch (IOException e) {
         throw new ServiceException("Failed to read inputs.", e);
      }
   }

   private String getProperty(String key) {
      return appProperties.getProperty(key);
   }

   private void handleInputs(Document document, String formUrl, Elements inputs, DataRetrievable registerData) throws ServiceException {
      List<KeyValue> keyValues = getKeyValuesFromInputs(document, inputs, registerData);
      logger.info("Retrieved keyValues from inputs. Size: " + keyValues.size());
      String responseUrl = getFormResponseUrl(formUrl);
      logger.info("Response form url generated: " + responseUrl);
      sendResponse(keyValues, responseUrl);
   }

   private void sendResponse(List<KeyValue> keyValues, String responseUrl) throws ServiceException {
      try {
         Connection con = Jsoup.connect(responseUrl);
         for (KeyValue keyValue : keyValues) {
            con = con.data(keyValue.getKey(), keyValue.getValue());
         }
         logger.info("Connection created: " + con);

         con.post();
      }
      catch (IOException e) {
         throw new ServiceException("Failed to send google form response", e);
      }
   }

   private String getFormResponseUrl(String formUrl) {
      int viewformIndex = formUrl.indexOf("viewform");
      return formUrl.substring(0, viewformIndex) + "formResponse";
   }

   private List<KeyValue> getKeyValuesFromInputs(Document document, Elements inputs, DataRetrievable registerData) throws ServiceException {
      List<KeyValue> keyValues = new ArrayList<>();
      for (Element input : inputs) {
         KeyValue keyValue = handleInput(document, input, registerData);
         if (keyValue != null) {
            keyValues.add(keyValue);
         }
      }
      return keyValues;
   }

   @Nullable
   private KeyValue handleInput(Document document, Element input, DataRetrievable registerData) throws ServiceException {
      logger.info("Handling input...");
      String type = getInputType(document, input);
      logger.info("Input type = " + type);
      if (type != null) {
         InputType inputType = InputType.fromInputType(type);
         KeyValue value = inputType.getValue(input, registerData);
         logger.info("Value for input: " + value);
         return value;
      }
      logger.info("Input is null and not handled.");
      return null;
   }

   private String getInputType(Document document, Element input) throws ServiceException {
      if (INPUT_TAG.equals(input.tagName()) && !HIDDEN.equals(input.attr(getProperty(ApplicationConstants.TYPE_ATTRIBUTE_KEY)))) {
         return INPUT_TAG;
      }
      if (TEXTAREA.equals(input.tagName())) {
         return TEXTAREA;
      }
      if (isListitemChild(document, input)) {
         return null;
      }
      String role = getInputRole(input);
      if (LISTITEM_TYPE.equals(role)) {
         return LISTITEM_TYPE;
      }
      if (LISTBOX_TYPE.equals(role)) {
         return LISTBOX_TYPE;
      }
      if (RADIOGROUP_TYPE.equals(role)) {
         return RADIOGROUP_TYPE;
      }

      throw new ServiceException("Unknow element type.");
   }

   private String getInputRole(Element input) {
      Element parent = input.previousElementSibling();
      if (!parent.hasAttr(ROLE_ATTR)) {
         parent = input.parent();
      }
      return parent.attr(ROLE_ATTR);
   }

   private boolean isListitemChild(Document document, Element input) {
      String inputId = input.attr(getProperty(ApplicationConstants.NAME_ATTRIBUTE_KEY));
      Elements checkboxKeeper = document.select("[name=" + inputId + "_sentinel]");
      return checkboxKeeper != null && !checkboxKeeper.isEmpty();
   }
}
