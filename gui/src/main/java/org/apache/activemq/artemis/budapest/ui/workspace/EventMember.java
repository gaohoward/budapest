package org.apache.activemq.artemis.budapest.ui.workspace;

import org.apache.activemq.artemis.budapest.ui.instance.UserEvent;

public interface EventMember
{
   void receive(UserEvent event);
}
