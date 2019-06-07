package com.dyukov.vkregbot.vk;

import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dyukov.vkregbot.config.AppProperties;
import com.dyukov.vkregbot.exceptions.ServiceException;
import com.dyukov.vkregbot.util.ApplicationConstants;
import com.dyukov.vkregbot.util.DateTimeUtil;
import com.dyukov.vkregbot.util.http.GetHttpRequest;
import com.dyukov.vkregbot.util.json.JsonUtil;
import com.dyukov.vkregbot.view.DataRetrievable;
import com.vk.api.sdk.objects.base.Link;
import com.vk.api.sdk.objects.wall.WallPostFull;
import com.vk.api.sdk.objects.wall.WallpostAttachment;

import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class VkApplication extends Application {

   private static int counter = 0;

   private static String tokenUrl;

   private static String accessCode;

   private static AppProperties properties;

   private Logger logger = LoggerFactory.getLogger(VkApplication.class);

   @Override
   public void start(Stage primaryStage) throws ServiceException {
      final WebView view = new WebView();
      final WebEngine engine = view.getEngine();
      String vkAuthUrl = String.format(getProperty(ApplicationConstants.VK_AUTH_URL_KEY), getProperty(ApplicationConstants.VK_APP_ID_KEY),
            getProperty(ApplicationConstants.VK_REDIRECT_URL_KEY), getProperty(ApplicationConstants.VK_AUTH_RESPONSE_TYPE_KEY));
      engine.load(vkAuthUrl);

      primaryStage.setScene(new Scene(view));
      primaryStage.show();

      engine.locationProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
         try {
            String redirectUrl = getProperty(ApplicationConstants.VK_REDIRECT_URL_KEY);
            if (newValue.startsWith(redirectUrl)) {
               logger.info("Token value is found.");
               tokenUrl = newValue;
               primaryStage.close();
            }
         }
         catch (ServiceException e) {
            logger.error("Unable to read properties", e);
            e.printStackTrace();
         }
      });
   }

   private String getTokenUrl() {
      if (tokenUrl == null) {
         logger.info("Initialization token value.");
         launch(VkApplication.class);
      }
      return tokenUrl;
   }

   private String getAccessCode() throws ServiceException {
      if (accessCode == null) {
         String urlStr = String.format(getProperty(ApplicationConstants.VK_AUTH_TOKEN_URL),
               getProperty(ApplicationConstants.VK_APP_ID_KEY),
               getProperty(ApplicationConstants.VK_CLIENT_SECRET_KEY),
               getProperty(ApplicationConstants.VK_REDIRECT_URL_KEY),
               extractAccessCode(tokenUrl));
         accessCode = new JsonUtil().getAccessTokenFromResponse(urlStr);
      }
      return accessCode;
   }
   /*
   private synchronized List<WallPostFull> getTopPosts(String code, DataRetrievable registerData) throws ServiceException {
      try {
         UserActor actor = getUserActor(code);
         GetResponse response = vk.wall()
                                  .get(actor)
                                  .ownerId(Integer.valueOf(registerData.getGroupId()))
                                  .count(Integer.valueOf(getProperty(ApplicationConstants.VK_RECORDS_COUNT_KEY)))
                                  .offset(0)
                                  .execute();
         if (response != null) {
            return response.getItems();
         }
      }
      catch (Exception e) {
         throw new ServiceException("Failed to retrieve posts list.", e);
      }
      return new ArrayList<>();
   }*/

   WallPostFull getTopPost(String token, int groupId) throws ServiceException {
      int negativeGroupId = groupId > 0 ? -groupId : groupId;
      String url = "https://api.vk.com/method/wall.get?owner_id=" + negativeGroupId + "&query=vk&count=1&offset=0&access_token=" + token + "&v=5.95";
      return new JsonUtil().getTopPost(url);
   }

   /*private WallPostFull getTopPost(String code, DataRetrievable registerData) throws ServiceException {
      try {
         UserActor actor = getUserActor(code);
         GetResponse response = vk.wall().get(actor)
                                  .ownerId(Integer.valueOf(registerData.getGroupId()))
                                  .count(1)
                                  .offset(0)
                                  .execute();
         if (response != null) {
            return response.getItems().get(0);
         }
         return null;
      }
      catch (Exception e) {
         throw new ServiceException("Failed to retrieve posts list.", e);
      }
   }*/

   private void leaveComment(String code, WallPostFull post, String comment) throws ServiceException {
      try {
         if (post != null) {
            String createCommentUrl = "https://api.vk.com/method/wall.createComment?owner_id=" + post.getOwnerId()
                  + "&post_id=" + post.getId()
                  + "&message=" + URLEncoder.encode(comment, "UTF-8")
                  + "&guid=" + new Date().getTime()
                  + "&access_token=" + code
                  + "&v=5.95";
            String element = new GetHttpRequest().execute(createCommentUrl);
            logger.info(element);
         }
      }
      catch (Exception e) {
         throw new ServiceException("Failed to create comment.", e);
      }
   }

   synchronized String getGoogleUrl(String code, DataRetrievable registerData) throws ServiceException {
      int retryCount = Integer.valueOf(getProperty(ApplicationConstants.MAX_RETRY_COUNT_KEY));
      while (counter <= retryCount) {
         WallPostFull topPost = getTopPost(code, Integer.valueOf(registerData.getGroupId()));
         if (topPost != null && new Date((long) topPost.getDate() * 1000).after(DateTimeUtil.parseDate(registerData.getScanWallTime()))) {
            String googleUrl = getGoogleFormUrl(topPost);
            if (googleUrl != null) {
               return googleUrl;
            }
         }
         counter++;
         try {
            logger.info("Post haven't appeared yet. Waiting...");
            Thread.sleep(Integer.valueOf(getProperty(ApplicationConstants.RETRY_DELAY_KEY)));
         }
         catch (InterruptedException e) {
            e.printStackTrace();
         }
      }
      logger.info("Post is not found.");
      return null;
   }

   void krugozor_reg(String code, Date postDate, DataRetrievable registerData) throws ServiceException {
      int retryCount = Integer.valueOf(getProperty(ApplicationConstants.MAX_RETRY_COUNT_KEY));
      while (counter <= retryCount) {
         WallPostFull topPost = getTopPost(code, Integer.valueOf(registerData.getGroupId()));
         if (topPost != null && new Date((long) topPost.getDate() * 1000).after(postDate)) {
            leaveComment(code, topPost,registerData.getTeamName() + " - " + registerData.getTeamMembersCount());
            break;
         }
         counter++;
         try {
            logger.info("Post haven't appeared yet. Waiting...");
            Thread.sleep(Integer.valueOf(getProperty(ApplicationConstants.RETRY_DELAY_KEY)));
         }
         catch (InterruptedException e) {
            e.printStackTrace();
         }
      }
   }

   private synchronized String getGoogleFormUrl(WallPostFull post) {
      List<WallpostAttachment> attachments = post.getAttachments();
      if (attachments != null) {
         for (WallpostAttachment attachment : attachments) {
            if (attachment != null) {
               Link link = attachment.getLink();
               if (link != null) {
                  String url = link.getUrl();
                  if (url.contains("docs.google.com")) {
                     return url;
                  }
               }
            }
         }
      }
      return null;
   }

   /*
   synchronized private UserActor getUserActor(String code) throws ServiceException {
      //synchronized (VkApplication.class) {
      try {
         if (userActor == null) {
            String redirectUrl = getProperty(ApplicationConstants.VK_REDIRECT_URL_KEY);
            String clientSecret = getProperty(ApplicationConstants.VK_CLIENT_SECRET_KEY);
            Integer appId = Integer.valueOf(getProperty(ApplicationConstants.VK_APP_ID_KEY));
            UserAuthResponse authResponse = getVK().oauth().userAuthorizationCodeFlow(appId, clientSecret, redirectUrl, code).execute();
            userActor = new UserActor(authResponse.getUserId(), authResponse.getAccessToken());
         }
         return userActor;
      }
      catch (Exception e) {
         throw new ServiceException("Failed to aithenticate vk user.", e);
      }
      //}
   }*/

   private String getProperty(String key) throws ServiceException {
      return getProperties().getProperty(key);
   }

   private AppProperties getProperties() throws ServiceException {
      if (properties == null) {
         properties = AppProperties.getInstance();
      }
      return properties;
   }

   /*
   private synchronized VkApiClient getVK() {
      synchronized (VkApplication.class) {
         if (vk == null) {
            TransportClient transportClient = HttpTransportClient.getInstance();
            vk = new VkApiClient(transportClient);
         }
      }
      return vk;
   }*/

   /**
    * Performs authorization and returns access_token
    *
    * @return access_token
    */
   String authorize() throws ServiceException {
      getTokenUrl();
      return getAccessCode();
   }

   private String extractAccessCode(String responseCode) {
      if (responseCode != null) {
         int index = responseCode.indexOf("access_token");
         if (index > -1) {
            int usIndex = responseCode.indexOf("&user_id");
            int expIndex = responseCode.indexOf("&expires_in");
            int lastIndex = Math.min(usIndex, expIndex);
            if (lastIndex == -1) {
               lastIndex = Math.max(usIndex, expIndex);
            }
            if (lastIndex == -1) {
               return responseCode.substring(index + "access_token".length());
            } else {
               return responseCode.substring(index + "access_token".length(), lastIndex);
            }
         } else {
            index = responseCode.indexOf(ApplicationConstants.RESPONSE_ACCESS_CODE_PREFIX);
            return responseCode.substring(index + ApplicationConstants.RESPONSE_ACCESS_CODE_PREFIX.length());
         }
      }
      return null;
   }
}
