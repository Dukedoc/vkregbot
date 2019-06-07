package com.dyukov.vkregbot.view.elements;

import java.awt.Color;

import javax.swing.JLabel;

public class ChangeColorLabel extends JLabel {

   private Color prevColor;

   public ChangeColorLabel(String name) {
      super(name);
   }

   public Color getPrevColor() {
      return prevColor;
   }

   public void setPrevColor(Color color) {
      this.prevColor = color;
   }
}
