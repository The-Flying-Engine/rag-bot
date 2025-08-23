package dev.gov.rag_boot.web;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import static dev.gov.rag_boot.web.DbDtos.*;

@RestController
@RequestMapping("/api/db")
@RequiredArgsConstructor
public class DbController {

  private final DataSource dataSource;
  private final JdbcTemplate jdbc;

  @GetMapping("/info")
  public DbInfo info() throws Exception {
    try (Connection c = dataSource.getConnection()) {
      var meta = c.getMetaData();
      String url = meta.getURL();
      String user = c.getMetaData().getUserName();
      String product = meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion();
      int tableCount = listTableNames(c).size();
      return new DbInfo(url, user, product, tableCount);
    }
  }

  @GetMapping("/tables")
  public Tables tables() throws Exception {
    try (Connection c = dataSource.getConnection()) {
      List<String> names = listTableNames(c);
      List<TableStat> stats = new ArrayList<>();
      for (String t : names) {
        Long cnt = jdbc.queryForObject("SELECT COUNT(*) FROM " + t, Long.class);
        stats.add(new TableStat(t, cnt == null ? 0L : cnt));
      }
      return new Tables(stats);
    }
  }

  // ⚠️ Optional: truncate all user tables (H2 only)
  @PostMapping("/clear")
  public ClearRes clearAll() throws Exception {
    try (Connection c = dataSource.getConnection()) {
      List<String> names = listTableNames(c);
      jdbc.update("SET REFERENTIAL_INTEGRITY FALSE");
      int done = 0;
      for (String t : names) {
        jdbc.update("TRUNCATE TABLE " + t);
        done++;
      }
      jdbc.update("SET REFERENTIAL_INTEGRITY TRUE");
      return new ClearRes(done);
    }
  }

  private List<String> listTableNames(Connection c) throws Exception {
    DatabaseMetaData md = c.getMetaData();
    // PUBLIC schema, TABLE type only
    try (ResultSet rs = md.getTables(null, "PUBLIC", "%", new String[]{"TABLE"})) {
      List<String> names = new ArrayList<>();
      while (rs.next()) {
        // H2 returns uppercase; use unquoted name for COUNT(*)
        names.add(rs.getString("TABLE_NAME"));
      }
      return names;
    }
  }
}
