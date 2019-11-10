package org.apache.activemq.artemis.budapest.ui;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.awt.Component;

public class CommonUI
{
   public static void errorDialog(Component parent, String msg, Throwable e)
   {
      StringBuilder builder = new StringBuilder("");
      if (e != null)
      {
         builder.append("Exception: " + e);
         builder.append("\n error message: " + e.getMessage());
         builder.append("\n stack trace: ");
         StackTraceElement[] stack = e.getStackTrace();
         for (StackTraceElement elem : stack)
         {
            builder.append("\n" + elem.toString());
         }
      }
      JOptionPane.showMessageDialog(parent, builder.toString(), msg, JOptionPane.ERROR_MESSAGE);
   }

   public static String inputDialog(Component parent, String prompt)
   {
      String input = JOptionPane.showInputDialog(parent, prompt);
      if (input != null) return input.trim();
      return input;
   }

   public static java.io.File inputDirDialog(Component parent, String title)
   {
      // TODO Auto-generated method stub
      final JFileChooser fc = new JFileChooser();
      fc.setDialogTitle(title);
      fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      //In response to a button click:
      int returnVal = fc.showOpenDialog(parent);
      if (returnVal == JFileChooser.APPROVE_OPTION)
      {
         return fc.getSelectedFile();
      }
      return null;
   }
}
