package com.dyukov.vkregbot.util.json;

import com.dyukov.vkregbot.exceptions.AuthorizationException;
import com.dyukov.vkregbot.exceptions.ServiceException;
import com.dyukov.vkregbot.util.http.GetHttpRequest;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vk.api.sdk.objects.wall.WallPostFull;

public class JsonUtil {

   public String getAccessTokenFromResponse(String urlStr) throws ServiceException {
      String responseStr = new GetHttpRequest().execute(urlStr);
      JsonObject jsonObject = getJsonResponse(responseStr);
      return jsonObject.get("access_token").getAsString();
   }

   private JsonObject getJsonResponse(String responseStr) {
      JsonElement response = new JsonParser().parse(responseStr);
      return response.getAsJsonObject();
   }

   public WallPostFull getTopPost(String urlStr) throws ServiceException {
      String responseStr = new GetHttpRequest().execute(urlStr);
      JsonElement response = new JsonParser().parse(responseStr);
      JsonObject jsonObject = response.getAsJsonObject();
      if (jsonObject.get("error") != null) {
         if (jsonObject.get("error").getAsJsonObject().get("error_code").getAsInt() == 5) {
            throw new AuthorizationException();
         } else {
            throw new ServiceException("Failed to get top post");
         }
      }
      JsonElement items = jsonObject.get("response").getAsJsonObject().get("items");
      JsonArray itemsArray = items.getAsJsonArray();
      Gson gson = new Gson();
      return gson.fromJson(itemsArray.get(0), WallPostFull.class);
   }

}
