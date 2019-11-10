package org.apache.activemq.artemis.budapest.filter;


public class LogContext
{
  private long currentLn;
  
  public void setCurrentLn(long num)
  {
    currentLn = num;
  }

  public long getCurrentLn()
  {
    return currentLn;
  }
}
