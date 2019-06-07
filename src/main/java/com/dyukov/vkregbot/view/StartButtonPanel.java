package com.dyukov.vkregbot.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Map;
import java.util.Timer;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dyukov.vkregbot.AppLauncher;
import com.dyukov.vkregbot.exceptions.ServiceException;
import com.dyukov.vkregbot.util.file.FileUtil;

class StartButtonPanel extends JPanel {

   private JButton button;
   private Logger logger = LoggerFactory.getLogger(StartButtonPanel.class);
   private AppLauncher appLauncher;
   private Timer timer;

   StartButtonPanel(DataInputPanel dataPanel) {
      button = new JButton("Start");
      JButton exitButton = new JButton("Reset token");
      exitButton.addActionListener((ActionEvent e) -> {
         try {
            FileUtil.getInstance().persistSessionCode("");
            logger.info("Cached session token deleted.");
         }
         catch (IOException ex) {
            logger.info("Failed to reset session code.");
         }
      });
      button.addActionListener((ActionEvent e) -> {
         button.setEnabled(false);
         exitButton.setEnabled(false);
         dataPanel.setDisabled();
         try {
            Map<String, String> errors = dataPanel.getRegistrationMethod().validate(dataPanel);
            if (!errors.isEmpty()) {
               button.setEnabled(true);
               exitButton.setEnabled(true);
               dataPanel.modeChanged();
               dataPanel.showErrors(errors);
            } else {
               appLauncher = new AppLauncher(dataPanel);
               Thread thread = new Thread(appLauncher);
               thread.start();
            }
         }
         catch (ServiceException ex) {
            logger.error(ex.getLocalizedMessage());
         }
      });
      setLayout(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();
      add(button, gbc);
      add(exitButton, gbc);
   }

   Timer getTimer() {
      return appLauncher == null ? null : appLauncher.getTimer();
   }

}
