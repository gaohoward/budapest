package org.apache.activemq.artemis.budapest.ui.instance;

//event generated from user actions on
//a component, like mouse clicks
public class UserEvent
{
   public static final int PROJ_SELECTED = 1;
   public static final int PROJ_REMOVED = 2;
   public static final int ASK_OPEN_PROJ = 3;
   public static final int REC_SELECTED = 4;
   public static final int TO_PARENT = 5;
   public static final int GOTO_LINE = 6;
   public static final int UPDATE_STATUS = 7;
   public static final int GOTO_PARENT_LINE = 8;

   private Object eventSource;
   private int eventType;
   private Object eventData;

   public UserEvent(Object source, final int type, Object data)
   {
      this.eventSource = source;
      this.eventType = type;
      this.eventData = data;
   }

   public Object getEventSource()
   {
      return this.eventSource;
   }

   public int getEventType()
   {
      return this.eventType;
   }

   public Object getEventData()
   {
      return this.eventData;
   }
}
