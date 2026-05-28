package com.smartqueue.droidsql.utils;

import net.zetetic.database.sqlcipher.SQLiteDatabase;
import net.zetetic.database.sqlcipher.SQLiteStatement;
import android.util.Xml;

import com.smartqueue.droidsql.model.QueryResult;

import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Utility class providing secure database and table import functionality.
 * Designed to prevent directory traversal and SQL Injection attacks.
 */
public class SQLImportHelper {

    /**
     * Checks if the given stream starts with the SQLite database file magic header.
     */
    public static boolean isValidSQLiteHeader(InputStream inputStream) {
        try {
            byte[] header = new byte[16];
            int read = inputStream.read(header);
            if (read == 16) {
                String headerStr = new String(header);
                return headerStr.startsWith("SQLite format 3");
            }
        } catch (IOException e) {
            // Ignore
        }
        return false;
    }

    /**
     * Parses a CSV file input stream into a grid list of rows and cell values.
     */
    public static List<List<String>> parseCSV(InputStream inputStream) throws IOException {
        List<List<String>> result = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            List<String> row = parseCSVLine(line);
            if (!row.isEmpty()) {
                result.add(row);
            }
        }
        return result;
    }

    private static List<String> parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        result.add(sb.toString().trim());
        return result;
    }

    /**
     * Parses a modern Excel .xlsx worksheet file without external heavyweight library dependencies.
     * Extracts values from xl/sharedStrings.xml and aligns cells using xl/worksheets/sheet1.xml.
     */
    public static List<List<String>> parseXLSX(InputStream inputStream) throws Exception {
        List<String> sharedStrings = new ArrayList<>();
        List<List<String>> rows = new ArrayList<>();

        ZipInputStream zip = new ZipInputStream(inputStream);
        ZipEntry entry;
        byte[] sharedStringsBytes = null;
        byte[] sheetBytes = null;

        while ((entry = zip.getNextEntry()) != null) {
            String name = entry.getName();
            if (name.equals("xl/sharedStrings.xml")) {
                sharedStringsBytes = readAllBytes(zip);
            } else if (name.equals("xl/worksheets/sheet1.xml")) {
                sheetBytes = readAllBytes(zip);
            }
            zip.closeEntry();
        }

        if (sharedStringsBytes != null) {
            parseSharedStrings(sharedStringsBytes, sharedStrings);
        }
        if (sheetBytes != null) {
            parseSheet(sheetBytes, sharedStrings, rows);
        }
        return rows;
    }

    private static byte[] readAllBytes(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[4096];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

    private static void parseSharedStrings(byte[] bytes, List<String> sharedStrings) throws Exception {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(new ByteArrayInputStream(bytes), "UTF-8");
        int eventType = parser.getEventType();
        StringBuilder textBuilder = null;
        while (eventType != XmlPullParser.END_DOCUMENT) {
            String name = parser.getName();
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if ("t".equals(name)) {
                        textBuilder = new StringBuilder();
                    }
                    break;
                case XmlPullParser.TEXT:
                    if (textBuilder != null) {
                        textBuilder.append(parser.getText());
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if ("t".equals(name) && textBuilder != null) {
                        sharedStrings.add(textBuilder.toString());
                        textBuilder = null;
                    }
                    break;
            }
            eventType = parser.next();
        }
    }

    private static void parseSheet(byte[] bytes, List<String> sharedStrings, List<List<String>> rows) throws Exception {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(new ByteArrayInputStream(bytes), "UTF-8");
        int eventType = parser.getEventType();
        List<String> currentRow = null;
        String currentCellType = null;
        StringBuilder valueBuilder = null;

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String name = parser.getName();
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if ("row".equals(name)) {
                        currentRow = new ArrayList<>();
                    } else if ("c".equals(name)) {
                        currentCellType = parser.getAttributeValue(null, "t");
                    } else if ("v".equals(name)) {
                        valueBuilder = new StringBuilder();
                    }
                    break;
                case XmlPullParser.TEXT:
                    if (valueBuilder != null) {
                        valueBuilder.append(parser.getText());
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if ("v".equals(name) && valueBuilder != null) {
                        String val = valueBuilder.toString();
                        if ("s".equals(currentCellType)) {
                            try {
                                int idx = Integer.parseInt(val);
                                if (idx >= 0 && idx < sharedStrings.size()) {
                                    val = sharedStrings.get(idx);
                                }
                            } catch (NumberFormatException e) {
                                // Ignore
                            }
                        }
                        if (currentRow != null) {
                            currentRow.add(val);
                        }
                        valueBuilder = null;
                    } else if ("row".equals(name) && currentRow != null) {
                        rows.add(currentRow);
                        currentRow = null;
                    }
                    break;
            }
            eventType = parser.next();
        }
    }

    /**
     * Securely imports tabular grid data into a SQLite database table.
     * Sanitizes table/column structures and uses parameter binding to prevent SQL injection.
     */
    public static QueryResult importTabularData(SQLiteDatabase database, String tableName, List<List<String>> data, boolean hasHeader) {
        if (data == null || data.isEmpty()) {
            return new QueryResult(false, "ERROR: No data found to import.");
        }

        // Sanitize Table Name to prevent SQL Injection
        String cleanTableName = tableName.replaceAll("[^a-zA-Z0-9_]", "");
        if (cleanTableName.isEmpty()) {
            return new QueryResult(false, "ERROR: Invalid table name.");
        }

        List<String> headers = new ArrayList<>();
        int startRow = 0;

        List<String> firstRow = data.get(0);
        int colCount = firstRow.size();

        if (hasHeader) {
            for (String col : firstRow) {
                String cleanCol = col.replaceAll("[^a-zA-Z0-9_]", "").trim();
                if (cleanCol.isEmpty()) {
                    cleanCol = "col_" + (headers.size() + 1);
                }
                headers.add(cleanCol);
            }
            startRow = 1;
        } else {
            for (int i = 1; i <= colCount; i++) {
                headers.add("col_" + i);
            }
        }

        // Build CREATE TABLE query
        StringBuilder createSql = new StringBuilder("CREATE TABLE ");
        createSql.append(cleanTableName).append(" (");
        for (int i = 0; i < headers.size(); i++) {
            createSql.append(headers.get(i)).append(" TEXT");
            if (i < headers.size() - 1) {
                createSql.append(", ");
            }
        }
        createSql.append(");");

        // Build INSERT query using placeholders
        StringBuilder insertSql = new StringBuilder("INSERT INTO ");
        insertSql.append(cleanTableName).append(" (");
        for (int i = 0; i < headers.size(); i++) {
            insertSql.append(headers.get(i));
            if (i < headers.size() - 1) {
                insertSql.append(", ");
            }
        }
        insertSql.append(") VALUES (");
        for (int i = 0; i < headers.size(); i++) {
            insertSql.append("?");
            if (i < headers.size() - 1) {
                insertSql.append(", ");
            }
        }
        insertSql.append(");");

        database.beginTransaction();
        SQLiteStatement stmt = null;
        try {
            // Create Table
            database.execSQL(createSql.toString());

            // Compile Insert Statement
            stmt = database.compileStatement(insertSql.toString());

            int insertedCount = 0;
            for (int r = startRow; r < data.size(); r++) {
                List<String> row = data.get(r);
                stmt.clearBindings();
                for (int c = 0; c < headers.size(); c++) {
                    String val = "";
                    if (c < row.size()) {
                        val = row.get(c);
                        if (val == null) val = "";
                    }
                    stmt.bindString(c + 1, val);
                }
                stmt.executeInsert();
                insertedCount++;
            }

            database.setTransactionSuccessful();
            return new QueryResult(true, "Query OK, " + insertedCount + " rows imported successfully into '" + cleanTableName + "'");
        } catch (Exception e) {
            return new QueryResult(false, "ERROR: Import failed: " + e.getMessage());
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            database.endTransaction();
        }
    }
}
