package org.apache.activemq.artemis.budapest.ui.workspace;

public class WorkspaceEvent
{
   public static final int NEW_PROJECT = 1;
   public static final int NEW_LOG_ADDED = 2;
   public static final int LOG_REMOVED = 3;
   public static final int PROJECT_REMOVED = 4;
   public static final int PROJECT_CLOSED = 5;
   public static final int NAME_CHANGED = 6;

   private Object eventData;
   private int eventType;

   public WorkspaceEvent(int type, Object data)
   {
      this.eventType = type;
      this.eventData = data;
   }

   public int getEventType()
   {
      return eventType;
   }

   public Object getEventData()
   {
      return eventData;
   }
}
