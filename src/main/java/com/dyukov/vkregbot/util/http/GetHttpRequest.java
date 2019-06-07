package com.dyukov.vkregbot.util.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.dyukov.vkregbot.exceptions.ServiceException;

public class GetHttpRequest {

   public String execute(String urlStr) throws ServiceException {
      HttpURLConnection connection;
         try {
            URL url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
            String line;
            while ((line = rd.readLine()) != null) {
               response.append(line);
               response.append('\r');
            }
            rd.close();
            return response.toString();
         }
         catch (IOException e) {
            throw new ServiceException("Failed to parse URL " + urlStr, e);
         }
   }

}
