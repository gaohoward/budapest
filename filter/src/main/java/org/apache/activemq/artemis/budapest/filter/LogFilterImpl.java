package org.apache.activemq.artemis.budapest.filter;

import java.io.StringReader;

public class LogFilterImpl extends LogFilter
{
  private String filter;
  private LogContext context = new LogContext();
  public LogFilterImpl(String filter)
  {
    super(new StringReader(filter));
    this.filter = filter;
  }
  
  public String getFilter()
  {
    return filter;
  }

  public LogFilter.CMD getMode() throws ParseException
  {
    ReInit(new StringReader(filter));
    return super.getMode();
  }

  public int doMatch(String line, long ln) throws Exception
  {
    ReInit(new StringReader(filter));
    context.setCurrentLn(ln);
    return super.evaluate(line, context);
  }
  
  public int doMatch(String line) throws Exception
  {
    ReInit(new StringReader(filter));
    return super.evaluate(line, context);
  }

  public static void main(String[] args) throws Exception
  {
    LogFilterImpl filterImpl = new LogFilterImpl("HoW");
    System.out.println("***test matches: " + (filterImpl.doMatch("howard-gao") == LogFilter.MATCH));
  }
}
