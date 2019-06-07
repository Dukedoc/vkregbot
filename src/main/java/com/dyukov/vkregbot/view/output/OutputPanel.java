package com.dyukov.vkregbot.view.output;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;

public class OutputPanel extends JPanel {

   JTextPane logArea;

   public OutputPanel() {
      setBorder(new EmptyBorder(5, 3, 5, 5));
      setLayout(new BorderLayout());
      logArea = new JTextPane();

      logArea.setBorder(BorderFactory.createLoweredBevelBorder());
      JScrollPane scrollPane = new JScrollPane(logArea,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
      logArea.setEditable(false);
      add(scrollPane, BorderLayout.CENTER);
   }

   public JTextPane getLogPane() {
      return logArea;
   }

}
