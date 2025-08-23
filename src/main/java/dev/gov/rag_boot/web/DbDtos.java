package dev.gov.rag_boot.web;

import java.util.List;

public class DbDtos {
  public record DbInfo(String url, String user, String product, int tableCount) {}
  public record TableStat(String name, long rows) {}
  public record Tables(List<TableStat> items) {}
  public record ClearRes(int truncatedTables) {}
}
