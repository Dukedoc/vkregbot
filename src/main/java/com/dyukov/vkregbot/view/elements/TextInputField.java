package com.dyukov.vkregbot.view.elements;

import java.awt.Color;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.dyukov.vkregbot.exceptions.ServiceException;
import com.dyukov.vkregbot.util.DateTimeUtil;

public class TextInputField extends JTextField {

   public static final int INT_TYPE = 0;
   private static final int STR_TYPE = 1;
   public static final int DATE_TYPE = 2;

   private ChangeColorLabel relatedLabel;
   private int type;

   public TextInputField(ChangeColorLabel relatedLabel) {
      this(relatedLabel, STR_TYPE);
   }

   public TextInputField(String text, ChangeColorLabel relatedLabel, int type) {
      setText(text);
      this.type = type;
      this.relatedLabel = relatedLabel;

      TextInputField that = this;
      this.getDocument().addDocumentListener(new DocumentListener() {
         @Override
         public void insertUpdate(DocumentEvent e) {
            handleValueChange(that);
         }

         @Override
         public void removeUpdate(DocumentEvent e) {
            handleValueChange(that);
         }

         @Override
         public void changedUpdate(DocumentEvent e) {
            handleValueChange(that);
         }
      });
   }

   public TextInputField(ChangeColorLabel relatedLabel, int type) {
      this("", relatedLabel, type);

   }

   private void handleValueChange(TextInputField that) {
      switch (that.type) {
         case INT_TYPE:
            handleIntegerValue();
            break;
         case STR_TYPE:
            handleStrValue();
            break;
         case DATE_TYPE:
            handleDateValue();
      }
   }

   private void handleDateValue() {
      if (getText().isEmpty()) {
         setLabelColorToError();
      } else {
         String runTimeStr = getText();
         try {
            DateTimeUtil.parseDate(runTimeStr);
         } catch (ServiceException e) {
            setLabelColorToError();
         }
         setLabelColorToOK();
      }
   }

   private void handleStrValue() {
      if (getText().isEmpty()) {
         setLabelColorToError();
      } else {
         setLabelColorToOK();
      }
   }

   private void setLabelColorToError() {
      if (relatedLabel.getPrevColor() == null) {
         relatedLabel.setPrevColor(relatedLabel.getForeground());
      }
      relatedLabel.setForeground(Color.RED);
   }

   private void handleIntegerValue() {
      if (getText().equals("-")) {
         setLabelColorToOK();
      } else try {
         Integer.parseInt(getText());
         setLabelColorToOK();
      } catch (NumberFormatException ex) {
         setLabelColorToError();
      }
   }

   private void setLabelColorToOK() {
      if (relatedLabel.getPrevColor() != null) {
         relatedLabel.setForeground(relatedLabel.getPrevColor());
         relatedLabel.setPrevColor(null);
      }
   }
}
