package com.smartqueue.droidsql;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.smartqueue.droidsql.model.QueryResult;
import com.smartqueue.droidsql.utils.SQLTemplateHelper;
import com.smartqueue.droidsql.viewmodel.DatabaseViewModel;
import android.widget.ListPopupWindow;
import android.widget.ArrayAdapter;
import java.util.ArrayList;
import com.smartqueue.droidsql.databinding.ActivityMainBinding;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.net.Uri;
import com.smartqueue.droidsql.utils.SQLImportHelper;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.List;

/**
 * Main Activity implementing PocketSQL terminal interface.
 * Uses MVVM architecture with ViewBinding for type-safe UI access.
 * 
 * Performance Notes:
 * - Dynamic table rendering: O(N*M) where N=rows, M=columns (unavoidable)
 * - UI updates: Reactive via LiveData observers
 * - Command history: O(1) navigation using ArrayList indexing
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private DatabaseViewModel viewModel;
    
    // Autocomplete
    private ListPopupWindow suggestionPopup;
    private TerminalAdapter suggestionAdapter;
    
    private int currentThemeColor = 0xFF00FF00; // Default: Matrix Green
    private int currentThemeBgColor = 0xFF000A02;
    private int currentThemeToolbarColor = 0xFF001505;
    private int currentThemeBottomColor = 0xFF001003;
    private List<String> dbSchemaSuggestions = new ArrayList<>();
    private boolean isAutoPairing = false;
    
    // File Picker variables for Importing Data
    private ActivityResultLauncher<String> filePickerLauncher;
    private ActivityResultLauncher<String[]> permissionLauncher;
    private String currentImportType = "";
    
    // Auto-rotation suggestion fields
    private android.view.OrientationEventListener orientationEventListener;
    private int targetManualOrientation = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Setup ViewBinding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(DatabaseViewModel.class);

        // Initialize File Picker
        filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    handleImportUri(uri);
                }
            }
        );

        // Initialize Permission Request Launcher
        permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                // Dynamic permission outcome handling
            }
        );

        // Setup UI listeners
        setupListeners();
        setupTvFocusSelector();
        setupKeyboardAnimationListener();
        setupAutocomplete();
        setupTerminalCopy();

        // Observe LiveData for reactive UI updates
        observeViewModel();
        
        // Setup orientation change suggestion button
        setupRotationListener();

        // Check and request storage permissions if on Android 12L or lower
        checkAndRequestPermissions();

        // Enable screenshot and screen recording protection
        com.smartqueue.droidsql.utils.SecurityUtils.preventScreenshots(this);

        // Root device safety warning
        if (com.smartqueue.droidsql.utils.SecurityUtils.isDeviceRooted()) {
            showRootWarningDialog();
        }
    }

    /**
     * Sets up all button click listeners.
     * Complexity: O(1) for each action
     */
    private void setupListeners() {
        // Auto-focus on command input
        binding.etSqlInput.requestFocus();

        // Execute SQL on Enter key press (like real terminal)
        binding.etSqlInput.setOnEditorActionListener((v, actionId, event) -> {
            String sql = binding.etSqlInput.getText().toString().trim();
            if (!sql.isEmpty()) {
                viewModel.executeSQL(sql);
                binding.etSqlInput.setText("");
                return true;
            }
            return false;
        });

        // Monitor key events for physical keyboard support
        binding.etSqlInput.setOnKeyListener((v, keyCode, event) -> {
            // Check for Enter key press on hardware keyboard
            if (event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER && 
                event.getAction() == android.view.KeyEvent.ACTION_DOWN) {
                
                // Allow Shift+Enter for new lines
                if (event.isShiftPressed()) {
                    return false; // Let system handle new line
                }
                
                // 1. Get text and clean non-breaking spaces
                String rawSql = binding.etSqlInput.getText().toString();
                String sql = rawSql.replace("\u00A0", " ").trim();
                
                // IMPROVED: Check if semicolon exists ANYWHERE (allows spaces after ; and cursor at any position)
                // Also allow strict EXIT/QUIT without semicolon for convenience
                boolean hasSemicolon = sql.contains(";");
                boolean isExitCommand = sql.equalsIgnoreCase("EXIT") || sql.equalsIgnoreCase("QUIT");
                
                if (!sql.isEmpty() && (hasSemicolon || isExitCommand)) {
                    
                    viewModel.executeSQL(sql);
                    binding.etSqlInput.setText("");
                    return true; // Consume event
                }
                
                // If no semicolon, allow newline (return false)
                return false; 
            }
            return false;
        });

        // Add TextWatcher to handle mobile/soft keyboard "Enter" key
        binding.etSqlInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isAutoPairing) return;
                
                boolean autoClose = getSharedPreferences("DroidSQL", MODE_PRIVATE).getBoolean("auto_close_pairs", true);
                if (autoClose && count == 1 && before == 0 && s.length() > start) {
                    char c = s.charAt(start);
                    char closing = 0;
                    if (c == '(') closing = ')';
                    else if (c == '[') closing = ']';
                    else if (c == '{') closing = '}';
                    else if (c == '\'') closing = '\'';
                    else if (c == '"') closing = '"';
                    
                    if (closing != 0) {
                        isAutoPairing = true;
                        binding.etSqlInput.getText().insert(start + 1, String.valueOf(closing));
                        binding.etSqlInput.setSelection(start + 1);
                        isAutoPairing = false;
                    }
                }

                if (count == 1 && s.length() > start && s.charAt(start) == '\n') {
                    // Newline detected at current cursor position
                    
                    // Get text and immediately remove the newline that was just inserted
                    String textWithNewline = binding.etSqlInput.getText().toString();
                    
                    // 1. Replace newline with SPACE to prevent concatenation of keywords (e.g., "table\nWHERE" -> "tableWHERE")
                    // 2. Replace non-breaking spaces (common on Android keyboards) with regular spaces
                    String cleanText = textWithNewline.replace("\n", " ").replace("\u00A0", " ");
                    
                    // 3. Trim and normalize spaces before semicolon
                    String trimmedSql = cleanText.trim();
                    
                    // Check execution conditions
                    boolean hasSemicolon = trimmedSql.contains(";");
                    boolean isExitCommand = trimmedSql.equalsIgnoreCase("EXIT") || trimmedSql.equalsIgnoreCase("QUIT");
                    boolean isTrigger = trimmedSql.toUpperCase().startsWith("CREATE TRIGGER");
                    boolean isTriggerComplete = isTrigger && trimmedSql.toUpperCase().endsWith("END;");
                    
                    // Logic: 
                    // 1. Normal command: Execute if it has semicolon
                    // 2. Trigger command: Execute ONLY if it ends with "END;"
                    boolean shouldExecute = (!isTrigger && hasSemicolon) || (isTrigger && isTriggerComplete) || isExitCommand;
                    
                    if (!trimmedSql.isEmpty() && shouldExecute) {
                        // Execute and clear
                        viewModel.executeSQL(trimmedSql);
                        binding.etSqlInput.setText("");
                    } else {
                        // No semicolon - allow newline for multiline input
                        // Do nothing, let the newline stay
                    }
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                updatePrompt();
                if (suggestionPopup == null) return;
                
                String text = s.toString();
                int cursorPos = binding.etSqlInput.getSelectionStart();
                
                // Find current word being typed
                int wordStart = cursorPos;
                while (wordStart > 0 && (Character.isLetterOrDigit(text.charAt(wordStart - 1)) || text.charAt(wordStart - 1) == '_')) {
                    wordStart--;
                }
                
                if (cursorPos <= wordStart) {
                    suggestionPopup.dismiss();
                    return;
                }
                
                String currentWord = text.substring(wordStart, cursorPos);
                
                // Filter suggestions
                if (currentWord.length() >= 1) { // Changed to >= 1 to allow 'I' -> IN, 'A' -> AS
                    List<String> matches = new ArrayList<>();
                    String upperWord = currentWord.toUpperCase();
                    
                    // 1. Analyze context of the query
                    String textBeforeWord = text.substring(0, wordStart).trim();
                    String textBeforeWordUpper = textBeforeWord.toUpperCase();
                    
                    boolean suggestOnlyTables = false;
                    boolean suggestOnlyColumns = false;
                    
                    boolean isCreateIndex = textBeforeWordUpper.contains("CREATE INDEX") || textBeforeWordUpper.contains("CREATE UNIQUE INDEX");
                    
                    // Check if inside parentheses of CREATE INDEX ON table(...)
                    boolean isIndexColumnContext = false;
                    String indexTable = null;
                    if (isCreateIndex && textBeforeWordUpper.contains("(")) {
                        int openParenIndex = textBeforeWordUpper.lastIndexOf("(");
                        String beforeParen = textBeforeWordUpper.substring(0, openParenIndex).trim();
                        int lastSpace = beforeParen.lastIndexOf(" ");
                        if (lastSpace != -1) {
                            indexTable = beforeParen.substring(lastSpace + 1).trim();
                            isIndexColumnContext = true;
                        } else {
                            indexTable = beforeParen;
                            isIndexColumnContext = true;
                        }
                        if (indexTable != null) {
                            indexTable = indexTable.replace("\"", "").replace("'", "").replace("`", "");
                        }
                    }
                    
                    // Check if previous word indicates table context
                    if (textBeforeWordUpper.endsWith("FROM") || 
                        textBeforeWordUpper.endsWith("JOIN") || 
                        textBeforeWordUpper.endsWith("INTO") || 
                        textBeforeWordUpper.endsWith("UPDATE") || 
                        textBeforeWordUpper.endsWith("TABLE") || 
                        textBeforeWordUpper.endsWith("DESCRIBE") || 
                        textBeforeWordUpper.endsWith("DESC") ||
                        (textBeforeWordUpper.endsWith("ON") && isCreateIndex)) {
                        suggestOnlyTables = true;
                    } 
                    // Check if previous word indicates column context
                    else if (textBeforeWordUpper.endsWith("SET") || 
                             textBeforeWordUpper.endsWith("WHERE") || 
                             (textBeforeWordUpper.endsWith("ON") && !isCreateIndex) || 
                             textBeforeWordUpper.endsWith("SELECT") ||
                             textBeforeWordUpper.endsWith("BY") ||
                             textBeforeWordUpper.endsWith("AND") ||
                             textBeforeWordUpper.endsWith("OR") ||
                             isIndexColumnContext) {
                        suggestOnlyColumns = true;
                    }
                    
                    if (suggestOnlyTables) {
                        // Only suggest matching table names
                        List<String> tables = viewModel.getTableNamesLiveData().getValue();
                        if (tables != null) {
                            for (String table : tables) {
                                if (table.toUpperCase().startsWith(upperWord)) {
                                    matches.add(table);
                                }
                            }
                        }
                    } else if (suggestOnlyColumns) {
                        // Find the table name mentioned in the query
                        String tableName = (indexTable != null) ? indexTable : findTableNameInQuery(text);
                        if (tableName != null) {
                            // Suggest columns of that specific table
                            List<String> columns = viewModel.getColumnsForTable(tableName);
                            for (String col : columns) {
                                if (col.toUpperCase().startsWith(upperWord)) {
                                    matches.add(col);
                                }
                            }
                        } else {
                            // Fallback: suggest all columns in the database if table cannot be determined
                            if (dbSchemaSuggestions != null) {
                                List<String> tables = viewModel.getTableNamesLiveData().getValue();
                                for (String term : dbSchemaSuggestions) {
                                    if (tables == null || !tables.contains(term)) { // If it's not a table, it's a column
                                        if (term.toUpperCase().startsWith(upperWord)) {
                                            matches.add(term);
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Default behavior: suggest matching keywords, tables, and columns
                        for (String keyword : SQLTemplateHelper.getKeywords()) {
                            if (keyword.startsWith(upperWord)) {
                                matches.add(keyword);
                            }
                        }
                        if (dbSchemaSuggestions != null) {
                            for (String term : dbSchemaSuggestions) {
                                if (term.toUpperCase().startsWith(upperWord) && !matches.contains(term)) {
                                    matches.add(term);
                                }
                            }
                        }
                    }
                    
                    if (!matches.isEmpty()) {
                        suggestionAdapter.clear();
                        suggestionAdapter.addAll(matches);
                        suggestionAdapter.notifyDataSetChanged();
                        
                        // Approx width logic or fixed
                        suggestionPopup.setWidth(500);
                        suggestionPopup.show();
                    } else {
                        suggestionPopup.dismiss();
                    }
                } else {
                    suggestionPopup.dismiss();
                }
            }
        });

        // Navigate to previous command - O(1)
        binding.btnPrevious.setOnClickListener(v -> {
            String command = viewModel.navigateHistory(-1);
            binding.etSqlInput.setText(command);
            binding.etSqlInput.setSelection(command.length()); // Cursor at end
        });

        // Navigate to next command - O(1)
        binding.btnNext.setOnClickListener(v -> {
            String command = viewModel.navigateHistory(1);
            binding.etSqlInput.setText(command);
            binding.etSqlInput.setSelection(command.length());
        });

        // Show SQL Templates
        binding.btnTemplates.setOnClickListener(v -> showTemplatesDialog());

        // List Tables
        binding.btnListTables.setOnClickListener(v -> {
            viewModel.listTables();
        });

        // Export Database
        binding.btnExport.setOnClickListener(v -> {
            viewModel.exportCurrentDatabase();
            Toast.makeText(this, "Check Downloads folder", Toast.LENGTH_SHORT).show();
        });

        // Import File
        binding.btnImport.setOnClickListener(v -> {
            showImportOptionsDialog();
        });

        // Settings Dialog
        binding.btnSettings.setOnClickListener(v -> showSettingsDialog());

        // Clear Terminal
        binding.btnClear.setOnClickListener(v -> {
            viewModel.clearTerminal();
        });
        
        // Load saved settings
        loadSettings();
        
        // Setup Symbol Toolbar Logic
        setupSymbolToolbar();
    }

    private String findTableNameInQuery(String sql) {
        String upper = sql.toUpperCase();
        
        List<String> keywords = new java.util.ArrayList<>(java.util.Arrays.asList(
            "FROM", "UPDATE", "INSERT INTO", "JOIN", "DESCRIBE", "DESC", "INTO"
        ));
        if (upper.contains("CREATE INDEX") || upper.contains("CREATE UNIQUE INDEX")) {
            keywords.add("ON");
        }
        
        List<String> tables = viewModel.getTableNamesLiveData().getValue();
        if (tables == null || tables.isEmpty()) {
            return null;
        }
        
        for (String keyword : keywords) {
            int index = upper.indexOf(keyword);
            if (index != -1) {
                // Get the text after the keyword
                String after = sql.substring(index + keyword.length()).trim();
                // Get the first token
                String[] tokens = after.split("[\\s(;,]");
                if (tokens.length > 0 && !tokens[0].isEmpty()) {
                    String candidate = tokens[0].trim();
                    // Clean quotes if any
                    candidate = candidate.replace("\"", "").replace("'", "").replace("`", "");
                    
                    // Verify if this candidate is actually a table in our database
                    for (String table : tables) {
                        if (table.equalsIgnoreCase(candidate)) {
                            return table;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Wires up the symbol buttons to insert text at cursor position.
     */
    private void setupSymbolToolbar() {
        android.widget.LinearLayout container = findViewById(R.id.symbolContainer);
        for (int i = 0; i < container.getChildCount(); i++) {
            android.view.View child = container.getChildAt(i);
            if (child instanceof android.widget.Button) {
                child.setOnClickListener(v -> {
                    String symbol = (String) v.getTag();
                    if (symbol != null) {
                        insertSymbol(symbol);
                    }
                });
            }
        }
    }

    /**
     * Inserts text at the current cursor position in the input field.
     */
    private void insertSymbol(String symbol) {
        int start = Math.max(binding.etSqlInput.getSelectionStart(), 0);
        int end = Math.max(binding.etSqlInput.getSelectionEnd(), 0);
        binding.etSqlInput.getText().replace(Math.min(start, end), Math.max(start, end), symbol, 0, symbol.length());
    }

    /**
     * Shows settings menu.
     * Complexity: O(1)
     */
    private void showSettingsDialog() {
        android.content.SharedPreferences prefs = getSharedPreferences("DroidSQL", MODE_PRIVATE);
        boolean lineWrap = prefs.getBoolean("line_wrap", true);
        boolean vibrate = prefs.getBoolean("vibrate_on_error", true);
        boolean autoClose = prefs.getBoolean("auto_close_pairs", true);
        boolean apiRunning = viewModel.isApiServerRunning();

        String[] options = {
            "📖 SQL Learning Guide",
            "Font Size",
            "Color Theme",
            "Line Wrapping: " + (lineWrap ? "ON" : "OFF"),
            "Vibrate on Error: " + (vibrate ? "ON" : "OFF"),
            "Auto-Close Pairs: " + (autoClose ? "ON" : "OFF"),
            "🌐 SQL REST API: " + (apiRunning ? "RUNNING" : "STOPPED")
        };
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Terminal Settings")
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    showLearningGuideDialog();
                } else if (which == 1) {
                    showFontSizeDialog();
                } else if (which == 2) {
                    showColorThemeDialog();
                } else if (which == 3) {
                    prefs.edit().putBoolean("line_wrap", !lineWrap).apply();
                    applyLineWrap(!lineWrap);
                    Toast.makeText(this, "Line wrapping " + (!lineWrap ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
                } else if (which == 4) {
                    prefs.edit().putBoolean("vibrate_on_error", !vibrate).apply();
                    Toast.makeText(this, "Vibration on error " + (!vibrate ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
                } else if (which == 5) {
                    prefs.edit().putBoolean("auto_close_pairs", !autoClose).apply();
                    Toast.makeText(this, "Auto-close pairs " + (!autoClose ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
                } else if (which == 6) {
                    if (apiRunning) {
                        viewModel.stopApiServer();
                        Toast.makeText(this, "REST API Server stopped", Toast.LENGTH_SHORT).show();
                    } else {
                        viewModel.startApiServer();
                        showApiServerDetailsDialog();
                    }
                }
            })
            .setNegativeButton("Close", null)
            .show();
    }

    private void showApiServerDetailsDialog() {
        String ip = viewModel.getLocalIpAddress();
        int port = viewModel.getApiServerPort();
        String token = viewModel.getApiServerToken();
        String baseUrl = "http://" + ip + ":" + port;

        android.widget.ScrollView scrollView = new android.widget.ScrollView(this);
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);
        layout.setBackgroundColor(currentThemeBgColor);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("🌐 REST API Server Details");
        tvTitle.setTextSize(20f);
        tvTitle.setTextColor(currentThemeColor);
        tvTitle.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        tvTitle.setPadding(0, 0, 0, 30);
        layout.addView(tvTitle);

        TextView tvDetails = new TextView(this);
        tvDetails.setTextSize(14f);
        tvDetails.setTextColor(currentThemeColor);
        tvDetails.setTypeface(Typeface.MONOSPACE);
        
        StringBuilder details = new StringBuilder();
        details.append("Status:    RUNNING\n");
        details.append("IP Addr:   ").append(ip).append("\n");
        details.append("Port:      ").append(port).append("\n");
        details.append("Base URL:  ").append(baseUrl).append("\n");
        details.append("Token:     ").append(token).append("\n\n");
        details.append("--- Authentication ---\n");
        details.append("Send 'X-API-Key' header or 'token' query param.\n\n");
        details.append("--- Example GET Command ---\n");
        details.append("curl -H \"X-API-Key: ").append(token).append("\" \"").append(baseUrl).append("/query?sql=SELECT+*+FROM+sqlite_master;\"\n\n");
        details.append("--- Example POST Command ---\n");
        details.append("curl -X POST -H \"Content-Type: application/json\" -H \"X-API-Key: ").append(token).append("\" -d \"{\\\"sql\\\": \\\"SELECT * FROM sqlite_master;\\\"}\" ").append(baseUrl).append("/query\n\n");
        details.append("(Long press anywhere in terminal to copy output. API endpoints: /status, /query)");
        
        tvDetails.setText(details.toString());
        layout.addView(tvDetails);

        android.widget.Button btnCopy = new android.widget.Button(this);
        btnCopy.setText("Copy Token");
        btnCopy.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF333333));
        btnCopy.setTextColor(Color.WHITE);
        btnCopy.setOnClickListener(v -> {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("DroidSQL API Token", token);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Token copied!", Toast.LENGTH_SHORT).show();
        });
        android.widget.LinearLayout.LayoutParams btnParams = new android.widget.LinearLayout.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        btnParams.topMargin = 20;
        btnCopy.setLayoutParams(btnParams);
        layout.addView(btnCopy);

        scrollView.addView(layout);

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(scrollView)
            .setPositiveButton("Close", null)
            .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();
    }

    private void showLearningGuideDialog() {
        try {
            // Create root container
            android.widget.LinearLayout root = new android.widget.LinearLayout(this);
            root.setOrientation(android.widget.LinearLayout.VERTICAL);
            root.setBackgroundColor(currentThemeBgColor);
            root.setPadding(40, 40, 40, 40);
            
            // Create scroll view
            android.widget.ScrollView scrollView = new android.widget.ScrollView(this);
            scrollView.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));
            root.addView(scrollView);
            
            // Container inside scroll view
            android.widget.LinearLayout container = new android.widget.LinearLayout(this);
            container.setOrientation(android.widget.LinearLayout.VERTICAL);
            scrollView.addView(container);
            
            android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setView(root)
                .create();
                
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
            }
            
            // Populate list initially
            showLearningItemsList(container, dialog);
            
            // Add close button at the bottom of root
            android.widget.Button closeBtn = new android.widget.Button(this);
            closeBtn.setText("Close");
            closeBtn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF333333));
            closeBtn.setTextColor(Color.WHITE);
            closeBtn.setOnClickListener(v -> dialog.dismiss());
            
            android.widget.LinearLayout.LayoutParams btnParams = new android.widget.LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
            btnParams.topMargin = 20;
            closeBtn.setLayoutParams(btnParams);
            root.addView(closeBtn);
            
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showLearningItemsList(android.widget.LinearLayout container, android.app.AlertDialog dialog) {
        container.removeAllViews();
        
        // Title
        TextView title = new TextView(this);
        title.setText("SQL Learning Guide");
        title.setTextColor(currentThemeColor);
        title.setTextSize(22f);
        title.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 10, 0, 30);
        container.addView(title);
        
        List<com.smartqueue.droidsql.utils.SQLTutorialHelper.TutorialItem> items = com.smartqueue.droidsql.utils.SQLTutorialHelper.getItems();
        for (int i = 0; i < items.size(); i++) {
            final com.smartqueue.droidsql.utils.SQLTutorialHelper.TutorialItem item = items.get(i);
            
            TextView itemTv = new TextView(this);
            itemTv.setText(item.keyword + " (" + item.category + ")");
            itemTv.setTextColor(currentThemeColor);
            itemTv.setTextSize(16f);
            itemTv.setPadding(20, 24, 20, 24);
            itemTv.setTypeface(Typeface.MONOSPACE);
            
            // Selectable background
            android.util.TypedValue outValue = new android.util.TypedValue();
            getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            itemTv.setBackgroundResource(outValue.resourceId);
            itemTv.setFocusable(true);
            itemTv.setClickable(true);
            
            // D-pad support matching other buttons
            itemTv.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    itemTv.setBackgroundColor(currentThemeColor);
                    itemTv.setTextColor(currentThemeBgColor);
                } else {
                    itemTv.setBackgroundColor(Color.TRANSPARENT);
                    itemTv.setTextColor(currentThemeColor);
                }
            });
            
            itemTv.setOnClickListener(v -> {
                showLearningItemDetail(container, item, dialog);
            });
            
            container.addView(itemTv);
            
            // Divider
            if (i < items.size() - 1) {
                android.view.View divider = new android.view.View(this);
                android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT, 2);
                divider.setLayoutParams(params);
                divider.setBackgroundColor((currentThemeColor & 0x00FFFFFF) | 0x33000000);
                container.addView(divider);
            }
        }
    }

    private void showLearningItemDetail(android.widget.LinearLayout container, com.smartqueue.droidsql.utils.SQLTutorialHelper.TutorialItem item, android.app.AlertDialog dialog) {
        container.removeAllViews();
        
        // 1. Back button
        TextView backTv = new TextView(this);
        backTv.setText("← Back to List");
        backTv.setTextColor(currentThemeColor);
        backTv.setTextSize(14f);
        backTv.setTypeface(Typeface.MONOSPACE);
        backTv.setPadding(0, 10, 0, 20);
        backTv.setClickable(true);
        backTv.setFocusable(true);
        backTv.setOnClickListener(v -> {
            showLearningItemsList(container, dialog);
        });
        backTv.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                backTv.setBackgroundColor(currentThemeColor);
                backTv.setTextColor(currentThemeBgColor);
            } else {
                backTv.setBackgroundColor(Color.TRANSPARENT);
                backTv.setTextColor(currentThemeColor);
            }
        });
        container.addView(backTv);
        
        // 2. Keyword Title
        TextView titleTv = new TextView(this);
        titleTv.setText(item.keyword);
        titleTv.setTextColor(currentThemeColor);
        titleTv.setTextSize(24f);
        titleTv.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        titleTv.setPadding(0, 0, 0, 5);
        container.addView(titleTv);
        
        // 3. Category
        TextView catTv = new TextView(this);
        catTv.setText(item.category);
        catTv.setTextColor(currentThemeColor);
        catTv.setAlpha(0.7f);
        catTv.setTextSize(12f);
        catTv.setTypeface(Typeface.MONOSPACE);
        catTv.setPadding(0, 0, 0, 20);
        container.addView(catTv);
        
        // 4. Description Label
        TextView descLabel = new TextView(this);
        descLabel.setText("Description:");
        descLabel.setTextColor(currentThemeColor);
        descLabel.setTextSize(14f);
        descLabel.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        descLabel.setPadding(0, 10, 0, 5);
        container.addView(descLabel);
        
        // Description Text
        TextView descTv = new TextView(this);
        descTv.setText(item.description);
        descTv.setTextColor(Color.WHITE); // Standard light color on dark backgrounds
        if (currentThemeColor == 0xFF333333) {
            descTv.setTextColor(Color.BLACK); // Black on Day light background
        }
        descTv.setTextSize(14f);
        descTv.setLineSpacing(4f, 1f);
        descTv.setPadding(0, 0, 0, 20);
        container.addView(descTv);
        
        // 5. Syntax Label
        TextView syntaxLabel = new TextView(this);
        syntaxLabel.setText("Syntax:");
        syntaxLabel.setTextColor(currentThemeColor);
        syntaxLabel.setTextSize(14f);
        syntaxLabel.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        syntaxLabel.setPadding(0, 10, 0, 5);
        container.addView(syntaxLabel);
        
        // Syntax Block
        TextView syntaxTv = new TextView(this);
        syntaxTv.setText(item.syntax);
        syntaxTv.setTextColor(currentThemeColor);
        syntaxTv.setTextSize(13f);
        syntaxTv.setTypeface(Typeface.MONOSPACE);
        syntaxTv.setPadding(20, 20, 20, 20);
        // Semi-transparent color block for code background
        syntaxTv.setBackgroundColor((currentThemeColor & 0x00FFFFFF) | 0x11000000); // 10% opacity code block
        container.addView(syntaxTv);
        
        // Space
        android.view.View space1 = new android.view.View(this);
        space1.setLayoutParams(new android.widget.LinearLayout.LayoutParams(1, 20));
        container.addView(space1);
        
        // 6. Example Label
        TextView exampleLabel = new TextView(this);
        exampleLabel.setText("Example:");
        exampleLabel.setTextColor(currentThemeColor);
        exampleLabel.setTextSize(14f);
        exampleLabel.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        exampleLabel.setPadding(0, 10, 0, 5);
        container.addView(exampleLabel);
        
        // Example Block
        TextView exampleTv = new TextView(this);
        exampleTv.setText(item.example);
        exampleTv.setTextColor(currentThemeColor);
        exampleTv.setTextSize(13f);
        exampleTv.setTypeface(Typeface.MONOSPACE);
        exampleTv.setPadding(20, 20, 20, 20);
        exampleTv.setBackgroundColor((currentThemeColor & 0x00FFFFFF) | 0x15000000); // 13% opacity code block
        container.addView(exampleTv);
        
        // Space
        android.view.View space2 = new android.view.View(this);
        space2.setLayoutParams(new android.widget.LinearLayout.LayoutParams(1, 30));
        container.addView(space2);
        
        // 7. Try it Button
        android.widget.Button tryBtn = new android.widget.Button(this);
        tryBtn.setText("⚡ Try It");
        tryBtn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(currentThemeColor));
        tryBtn.setTextColor(currentThemeBgColor);
        tryBtn.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        tryBtn.setOnClickListener(v -> {
            binding.etSqlInput.setText(item.example);
            binding.etSqlInput.setSelection(item.example.length());
            Toast.makeText(this, "Example query loaded into console", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        container.addView(tryBtn);
    }

    private void applyLineWrap(boolean enabled) {
        android.view.ViewGroup.LayoutParams params = binding.tvTerminalOutput.getLayoutParams();
        if (enabled) {
            params.width = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
        } else {
            params.width = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        binding.tvTerminalOutput.setLayoutParams(params);
    }

    private void triggerErrorFeedback() {
        boolean vibrate = getSharedPreferences("DroidSQL", MODE_PRIVATE).getBoolean("vibrate_on_error", true);
        if (vibrate) {
            android.os.Vibrator vibrator = (android.os.Vibrator) getSystemService(android.content.Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator.vibrate(android.os.VibrationEffect.createOneShot(100, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(100);
                }
            }
        }
    }

    private void showFontSizeDialog() {
        String[] fonts = {"very Small (6sp)", "Small (10sp)", "Medium (13sp)", "Large (16sp)", "Extra Large (20sp)", "Extra Extra Large (24sp)"};
        int currentSize = getSharedPreferences("DroidSQL", MODE_PRIVATE).getInt("font_size_index", 1);

        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Font Size")
            .setSingleChoiceItems(fonts, currentSize, (dialog, which) -> {
                getSharedPreferences("DroidSQL", MODE_PRIVATE)
                    .edit()
                    .putInt("font_size_index", which)
                    .apply();
                applyFontSize(which);
                dialog.dismiss();
            })
            .setPositiveButton("Back", (dialog, which) -> showSettingsDialog())
            .show();
    }

    private void showColorThemeDialog() {
        String[] themes = {
            "Matrix Green", 
            "Cyberpunk Amber", 
            "Classic White", 
            "Hacker Cyan",
            "Dracula Purple",
            "Nord Ice",
            "Monokai Orange",
            "Solarized Teal",
            "VS Code Dark",
            "Default Dark",
            "Day (Light)"
        };
        int currentTheme = getSharedPreferences("DroidSQL", MODE_PRIVATE).getInt("theme_index", 0);

        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Color Theme")
            .setSingleChoiceItems(themes, currentTheme, (dialog, which) -> {
                getSharedPreferences("DroidSQL", MODE_PRIVATE)
                    .edit()
                    .putInt("theme_index", which)
                    .apply();
                applyTheme(which);
                dialog.dismiss();
            })
            .setPositiveButton("Back", (dialog, which) -> showSettingsDialog())
            .show();
    }

    private void loadSettings() {
        android.content.SharedPreferences prefs = getSharedPreferences("DroidSQL", MODE_PRIVATE);
        applyFontSize(prefs.getInt("font_size_index", 2)); // Default: Medium (13sp)
        applyTheme(prefs.getInt("theme_index", 0));
        applyLineWrap(prefs.getBoolean("line_wrap", true));
    }

    private void applyFontSize(int index) {
        float size;
        switch (index) {
            case 0: size = 6f; break;   // Very Small
            case 1: size = 10f; break;  // Small
            case 2: size = 13f; break;  // Medium (Default)
            case 3: size = 16f; break;  // Large
            case 4: size = 20f; break;  // Extra Large
            case 5: size = 24f; break;  // Extra Extra Large
            default: size = 13f;
        }
        
        binding.tvTerminalOutput.setTextSize(size);
        binding.etSqlInput.setTextSize(size);
        binding.tvPrompt.setTextSize(size);
    }

    private void applyTheme(int index) {
        int color;
        int bgColor;
        int toolbarColor;
        int bottomToolbarColor;
        
        switch (index) {
            case 0: // Matrix Green
                color = 0xFF00FF00;
                bgColor = 0xFF000A02;
                toolbarColor = 0xFF001505;
                bottomToolbarColor = 0xFF001003;
                break;
            case 1: // Cyberpunk Amber
                color = 0xFFFFB000;
                bgColor = 0xFF150B1A;
                toolbarColor = 0xFF22122A;
                bottomToolbarColor = 0xFF1B0E21;
                break;
            case 2: // Classic White
                color = 0xFFEEEEEE;
                bgColor = 0xFF0D0D0D;
                toolbarColor = 0xFF1A1A1A;
                bottomToolbarColor = 0xFF141414;
                break;
            case 3: // Hacker Cyan
                color = 0xFF00FFFF;
                bgColor = 0xFF020D14;
                toolbarColor = 0xFF0B1B26;
                bottomToolbarColor = 0xFF07141D;
                break;
            case 4: // Dracula Purple
                color = 0xFFBD93F9;
                bgColor = 0xFF282A36;
                toolbarColor = 0xFF1E1F29;
                bottomToolbarColor = 0xFF21222C;
                break;
            case 5: // Nord Ice
                color = 0xFF88C0D0;
                bgColor = 0xFF2E3440;
                toolbarColor = 0xFF3B4252;
                bottomToolbarColor = 0xFF353C4A;
                break;
            case 6: // Monokai Orange
                color = 0xFFFD971F;
                bgColor = 0xFF272822;
                toolbarColor = 0xFF1E1F1C;
                bottomToolbarColor = 0xFF22231E;
                break;
            case 7: // Solarized Teal
                color = 0xFF2AA198;
                bgColor = 0xFF002B36;
                toolbarColor = 0xFF073642;
                bottomToolbarColor = 0xFF052E37;
                break;
            case 8: // VS Code Dark
                color = 0xFF4FC1FF;
                bgColor = 0xFF1E1E1E;
                toolbarColor = 0xFF007ACC;
                bottomToolbarColor = 0xFF252526;
                break;
            case 9: // Default Dark
                color = 0xFFFFFFFF;
                bgColor = 0xFF121212;
                toolbarColor = 0xFF1F1F1F;
                bottomToolbarColor = 0xFF181818;
                break;
            case 10: // Day (Light)
                color = 0xFF333333;
                bgColor = 0xFFF5F5F5;
                toolbarColor = 0xFFE0E0E0;
                bottomToolbarColor = 0xFFECECEC;
                break;
            default:
                color = 0xFF00FF00;
                bgColor = 0xFF000A02;
                toolbarColor = 0xFF001505;
                bottomToolbarColor = 0xFF001003;
        }
        
        currentThemeColor = color;
        currentThemeBgColor = bgColor;
        currentThemeToolbarColor = toolbarColor;
        currentThemeBottomColor = bottomToolbarColor;
        
        // Apply background colors to layouts
        binding.main.setBackgroundColor(bgColor);
        binding.topToolbar.setBackgroundColor(toolbarColor);
        binding.bottomToolbarArea.setBackgroundColor(bottomToolbarColor);
        
        // Apply color to terminal elements
        binding.tvTerminalOutput.setTextColor(color);
        binding.etSqlInput.setTextColor(color);
        // Prompt matches the theme color
        binding.tvPrompt.setTextColor(color); 
        
        // Also update toolbar title text color for consistency
        binding.topToolbar.setTitleTextColor(color);
        
        // Set suggestion popup background
        if (suggestionPopup != null) {
            suggestionPopup.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(bgColor));
        }
        
        // Style rotation suggestion button dynamically
        if (binding.fabRotate != null) {
            binding.fabRotate.setBackgroundTintList(android.content.res.ColorStateList.valueOf(bgColor));
            binding.fabRotate.setImageTintList(android.content.res.ColorStateList.valueOf(color));
        }
        
        // Dynamic status bar and navigation bar coloring
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(toolbarColor);
            getWindow().setNavigationBarColor(bottomToolbarColor);
        }
        
        // Refresh all buttons colors dynamically
        refreshButtonColors();
    }

    private void setupTvFocusSelector() {
        // Control buttons
        TextView[] controlButtons = {
            binding.btnPrevious,
            binding.btnNext,
            binding.btnListTables,
            binding.btnTemplates,
            binding.btnExport,
            binding.btnClear,
            binding.btnSettings
        };
        
        for (TextView btn : controlButtons) {
            btn.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    btn.setBackgroundColor(currentThemeColor);
                    btn.setTextColor(currentThemeBgColor);
                } else {
                    btn.setBackgroundColor(Color.TRANSPARENT);
                    btn.setTextColor(currentThemeColor);
                }
            });
        }
        
        // Symbol buttons
        android.widget.LinearLayout symbolContainer = findViewById(R.id.symbolContainer);
        if (symbolContainer != null) {
            for (int i = 0; i < symbolContainer.getChildCount(); i++) {
                android.view.View child = symbolContainer.getChildAt(i);
                if (child instanceof TextView) {
                    TextView tv = (TextView) child;
                    tv.setOnFocusChangeListener((v, hasFocus) -> {
                        if (hasFocus) {
                            tv.setBackgroundColor(currentThemeColor);
                            tv.setTextColor(currentThemeBgColor);
                        } else {
                            tv.setBackgroundColor(Color.TRANSPARENT);
                            tv.setTextColor(currentThemeColor);
                        }
                    });
                }
            }
        }


    }

    private void refreshButtonColors() {
        // Control buttons
        TextView[] controlButtons = {
            binding.btnPrevious,
            binding.btnNext,
            binding.btnListTables,
            binding.btnTemplates,
            binding.btnExport,
            binding.btnClear,
            binding.btnSettings
        };
        
        for (TextView btn : controlButtons) {
            if (btn.hasFocus()) {
                btn.setBackgroundColor(currentThemeColor);
                btn.setTextColor(currentThemeBgColor);
            } else {
                btn.setBackgroundColor(Color.TRANSPARENT);
                btn.setTextColor(currentThemeColor);
            }
        }
        
        // Symbol buttons
        android.widget.LinearLayout symbolContainer = findViewById(R.id.symbolContainer);
        if (symbolContainer != null) {
            for (int i = 0; i < symbolContainer.getChildCount(); i++) {
                android.view.View child = symbolContainer.getChildAt(i);
                if (child instanceof TextView) {
                    TextView tv = (TextView) child;
                    if (tv.hasFocus()) {
                        tv.setBackgroundColor(currentThemeColor);
                        tv.setTextColor(currentThemeBgColor);
                    } else {
                        tv.setBackgroundColor(Color.TRANSPARENT);
                        tv.setTextColor(currentThemeColor);
                    }
                }
            }
        }


    }

    private void setupKeyboardAnimationListener() {
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            boolean isKeyboardVisible = insets.isVisible(androidx.core.view.WindowInsetsCompat.Type.ime());
            
            // Animate layout changes smoothly
            androidx.transition.TransitionManager.beginDelayedTransition(binding.bottomToolbarArea);
            if (isKeyboardVisible) {
                binding.controlButtonsArea.setVisibility(android.view.View.GONE);
            } else {
                binding.controlButtonsArea.setVisibility(android.view.View.VISIBLE);
            }
            
            return insets;
        });
    }

    private void setupRotationListener() {
        binding.fabRotate.setOnClickListener(v -> {
            if (targetManualOrientation == android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else if (targetManualOrientation == android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
            binding.fabRotate.setVisibility(android.view.View.GONE);
        });

        orientationEventListener = new android.view.OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (orientation == ORIENTATION_UNKNOWN) {
                    return;
                }

                // Check if system auto-rotate is OFF
                boolean isAutoRotateOff = false;
                try {
                    isAutoRotateOff = android.provider.Settings.System.getInt(
                        getContentResolver(),
                        android.provider.Settings.System.ACCELEROMETER_ROTATION,
                        0
                    ) == 0;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (!isAutoRotateOff) {
                    binding.fabRotate.setVisibility(android.view.View.GONE);
                    // Reset lock if it was set so it follows the system setting dynamically
                    if (getRequestedOrientation() != android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
                        setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    }
                    return;
                }

                int currentConfigOrientation = getResources().getConfiguration().orientation;

                // Normalize physical orientation to 0 (portrait) or 90/270 (landscape)
                // We add tolerance around the angles
                boolean physicalIsLandscape = (orientation >= 60 && orientation <= 120) || (orientation >= 240 && orientation <= 300);
                boolean physicalIsPortrait = (orientation >= 0 && orientation <= 30) || (orientation >= 330 && orientation <= 360);

                if (physicalIsLandscape && currentConfigOrientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
                    // Physical is landscape, but screen is portrait -> suggest landscape rotation
                    targetManualOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    binding.fabRotate.setVisibility(android.view.View.VISIBLE);
                } else if (physicalIsPortrait && currentConfigOrientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
                    // Physical is portrait, but screen is landscape -> suggest portrait rotation
                    targetManualOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    binding.fabRotate.setVisibility(android.view.View.VISIBLE);
                } else {
                    // Screen orientation matches physical, or in intermediate angle -> hide suggestion
                    binding.fabRotate.setVisibility(android.view.View.GONE);
                }
            }
        };

        if (orientationEventListener.canDetectOrientation()) {
            orientationEventListener.enable();
        }
    }

    /**
     * Sets up realtime autocomplete suggestions.
     */
    private void setupAutocomplete() {
        suggestionPopup = new ListPopupWindow(this);
        suggestionPopup.setAnchorView(binding.etSqlInput);
        
        // Use custom adapter for Terminal Theme matching
        suggestionAdapter = new TerminalAdapter(this, new ArrayList<>());
        suggestionPopup.setAdapter(suggestionAdapter);
        suggestionPopup.setModal(false);
        // Add vertical offset to not block current line
        suggestionPopup.setVerticalOffset(10);
        suggestionPopup.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0xFF111111)); // Dark Gray Background

        suggestionPopup.setOnItemClickListener((parent, view, position, id) -> {
            String suggestion = suggestionAdapter.getItem(position);
            if (suggestion != null) {
                int cursorPos = binding.etSqlInput.getSelectionStart();
                String text = binding.etSqlInput.getText().toString();
                
                // Find word start
                int wordStart = cursorPos;
                while (wordStart > 0 && (Character.isLetterOrDigit(text.charAt(wordStart - 1)) || text.charAt(wordStart - 1) == '_')) {
                    wordStart--;
                }
                
                // Replace partial word with suggestion + space
                String before = text.substring(0, wordStart);
                String after = text.substring(cursorPos);
                
                String replacement = suggestion + " ";
                binding.etSqlInput.setText(before + replacement + after);
                binding.etSqlInput.setSelection(before.length() + replacement.length());
                
                suggestionPopup.dismiss();
            }
        });
    }

    /**
     * Observes ViewModel LiveData for reactive UI updates.
     * Updates happen automatically when data changes.
     */
    private void observeViewModel() {
        // Terminal output observer
        viewModel.getTerminalOutput().observe(this, output -> {
            binding.tvTerminalOutput.setText(formatTerminalOutput(output));
            // Auto-scroll to bottom (Termux-style)
            binding.svTerminalOutput.post(() -> 
                binding.svTerminalOutput.fullScroll(android.view.View.FOCUS_DOWN));
        });

        // Execution result observer
        viewModel.getExecutionResult().observe(this, result -> {
            if (result != null) {
                if (!result.isSuccess() && !result.shouldExitApp()) {
                    triggerErrorFeedback();
                }
                // Check for exit signal
                if (result.shouldExitApp()) {
                    finishAffinity(); // Close app completely
                    return;
                }
                
                // Auto-scroll to bottom if needed (handled by terminal output observer too)
            }
        });

        // Database status observer - update prompt
        viewModel.getIsDatabaseOpen().observe(this, isOpen -> {
            updatePrompt();
        });

        // Observe schema suggestions from ViewModel
        viewModel.getSchemaSuggestions().observe(this, suggestions -> {
            if (suggestions != null) {
                dbSchemaSuggestions = suggestions;
            }
        });

        // Database name observer - update prompt
        // Database name observer - update prompt and Toolbar Title
        viewModel.getCurrentDatabaseName().observe(this, dbName -> {
            updatePrompt();
            if (dbName != null && !dbName.isEmpty()) {
                String displayName = dbName.replace(".db", "");
                binding.topToolbar.setTitle(displayName); // Set as Toolbar Title
                
                // Save as last used database
                getSharedPreferences("DroidSQL", MODE_PRIVATE)
                    .edit()
                    .putString("last_db", dbName)
                    .apply();
            } else {
                binding.topToolbar.setTitle("No Database");
            }
        });
        
        // Auto-open logic - only run if database is not already open (avoids duplicate open on screen rotation)
        Boolean isOpen = viewModel.getIsDatabaseOpen().getValue();
        if (isOpen == null || !isOpen) {
            java.io.File ecommerceFile = getDatabasePath("ecommerce.db");
            java.io.File worldFile = getDatabasePath("world.db");
            
            if (!ecommerceFile.exists() || !worldFile.exists()) {
                // First run (or data cleared or new DB added): Generate ALL sample data
                viewModel.generateSampleDatabase();
                // Save as default for next time
                getSharedPreferences("DroidSQL", MODE_PRIVATE)
                    .edit()
                    .putString("last_db", "ecommerce.db")
                    .apply();
            } else {
                // Normal run: Open last used database
                String lastDb = getSharedPreferences("DroidSQL", MODE_PRIVATE).getString("last_db", "ecommerce.db");
                viewModel.createOrOpenDatabase(lastDb);
            }
        }
    }

    /**
     * Updates the command prompt indicator (MySQL-style)
     */
    private void updatePrompt() {
        String text = binding.etSqlInput.getText().toString();
        int newlineCount = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                newlineCount++;
            }
        }
        
        StringBuilder promptBuilder = new StringBuilder("mysql>");
        for (int i = 0; i < newlineCount; i++) {
            promptBuilder.append("\n   ->");
        }
        
        binding.tvPrompt.setText(promptBuilder.toString());
        binding.tvPrompt.setTextColor(binding.etSqlInput.getCurrentTextColor());
    }

    /**
     * Formats terminal output with color-coded messages.
     * SUCCESS = Green, ERROR = Red, INFO = Cyan
     */
    private CharSequence formatTerminalOutput(String text) {
        SpannableString spannable = new SpannableString(text);
        
        // Color code different message types
        int start = 0;
        while (start < text.length()) {
            int lineEnd = text.indexOf('\n', start);
            if (lineEnd == -1) lineEnd = text.length();
            
            String line = text.substring(start, lineEnd);
            
            if (line.contains("[ERROR]")) {
                spannable.setSpan(new ForegroundColorSpan(Color.RED), 
                    start, lineEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (line.contains("[SUCCESS]")) {
                spannable.setSpan(new ForegroundColorSpan(Color.GREEN), 
                    start, lineEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (line.contains("[INFO]")) {
                spannable.setSpan(new ForegroundColorSpan(Color.CYAN), 
                    start, lineEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            
            start = lineEnd + 1;
        }
        
        return spannable;
    }


    /**
     * Shows SQL templates dialog for learning and quick execution.
     * Complexity: O(T) where T = number of templates
     */
    /**
     * Shows SQL templates dialog using custom UI.
     * Complexity: O(T) to build views
     */
    private void showTemplatesDialog() {
        try {
            // Inflate custom layout
            android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_templates, null);
            dialogView.setBackgroundColor(currentThemeBgColor);
            
            TextView tvTitle = dialogView.findViewById(R.id.tvTemplatesTitle);
            if (tvTitle != null) {
                tvTitle.setTextColor(currentThemeColor);
            }
            
            android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .create();
                
            // Transparent background for rounded corners effect
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            }

            // Populate Template List
            android.widget.LinearLayout listContainer = dialogView.findViewById(R.id.llTemplateList);
            String[] names = SQLTemplateHelper.getTemplateNames();
            
            for (int i = 0; i < names.length; i++) {
                final int index = i;
                TextView item = new TextView(this);
                item.setText(names[i]);
                item.setTextSize(16f);
                
                // Set text color to current theme color
                item.setTextColor(currentThemeColor);
                
                item.setPadding(32, 24, 32, 24); // px values
                item.setTypeface(Typeface.MONOSPACE);
                
                // Selectable background
                android.util.TypedValue outValue = new android.util.TypedValue();
                getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                item.setBackgroundResource(outValue.resourceId);
                
                item.setOnClickListener(v -> {
                    String template = SQLTemplateHelper.getTemplate(index);
                    binding.etSqlInput.setText(template);
                    Toast.makeText(this, "Template loaded", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });
                
                listContainer.addView(item);
                
                // Thin divider
                if (i < names.length - 1) {
                    android.view.View divider = new android.view.View(this);
                    android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT, 2); // 2px height for visibility
                    divider.setLayoutParams(params);
                    divider.setBackgroundColor((currentThemeColor & 0x00FFFFFF) | 0x33000000); // 20% opacity theme color
                    listContainer.addView(divider);
                }
            }
            
            // Setup Action Buttons with error handling
            TextView btnSample = dialogView.findViewById(R.id.btnActionSample);
            if (btnSample != null) {
                btnSample.setTextColor(currentThemeColor);
                btnSample.setOnClickListener(v -> {
                    viewModel.generateSampleDatabase();
                    dialog.dismiss();
                });
            }
            
            TextView btnTips = dialogView.findViewById(R.id.btnActionTips);
            if (btnTips != null) {
                btnTips.setTextColor(currentThemeColor);
                btnTips.setOnClickListener(v -> {
                    showPerformanceTips();
                });
            }
            
            android.view.View btnClose = dialogView.findViewById(R.id.btnCloseTemplates);
            if (btnClose != null) {
                btnClose.setOnClickListener(v -> dialog.dismiss());
            }
            
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error opening templates: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Shows performance optimization tips for O(log N) queries.
     */
    private void showPerformanceTips() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Performance Optimization");
        builder.setMessage(SQLTemplateHelper.getPerformanceTips());
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (orientationEventListener != null) {
            orientationEventListener.disable();
        }
    }
    /**
     * Sets up long-press to copy terminal output.
     */
    private void setupTerminalCopy() {
        binding.tvTerminalOutput.setOnLongClickListener(v -> {
            String content = binding.tvTerminalOutput.getText().toString();
            if (!content.isEmpty()) {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("PocketSQL Output", content);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Terminal output copied to clipboard", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
    }

    /**
     * Custom Adapter to style suggestions like valid Terminal/MySQL Workbench.
     */
    private class TerminalAdapter extends android.widget.BaseAdapter {
        private final android.content.Context context;
        private final List<String> items;

        public TerminalAdapter(android.content.Context context, List<String> items) {
            this.context = context;
            this.items = items;
        }

        public void clear() {
            items.clear();
            notifyDataSetChanged();
        }

        public void addAll(List<String> newItems) {
            items.addAll(newItems);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public String getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
            TextView textView;
            if (convertView == null) {
                textView = new TextView(context);
                textView.setPadding(30, 20, 30, 20);
                textView.setTextSize(16f);
                textView.setTypeface(Typeface.MONOSPACE);
            } else {
                textView = (TextView) convertView;
            }

            // Apply current theme colors dynamically
            textView.setTextColor(currentThemeColor);
            textView.setBackgroundColor(currentThemeBgColor);

            String item = getItem(position);
            // Add icon/symbol for visual flair
            textView.setText("➜ " + item);
            
            return textView;
        }
    }

    private void showImportOptionsDialog() {
        String[] options = {
            "📁 SQLite Database File (.db)",
            "📜 SQL Script File (.sql)",
            "📊 CSV File (.csv)",
            "📈 Excel Worksheet (.xlsx)"
        };
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Select File Type to Import")
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    currentImportType = "db";
                    filePickerLauncher.launch("*/*");
                } else if (which == 1) {
                    currentImportType = "sql";
                    filePickerLauncher.launch("*/*");
                } else if (which == 2) {
                    currentImportType = "csv";
                    filePickerLauncher.launch("*/*");
                } else if (which == 3) {
                    currentImportType = "xlsx";
                    filePickerLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void handleImportUri(Uri uri) {
        try {
            String fileName = "imported_data";
            android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        fileName = cursor.getString(nameIndex);
                    }
                }
                cursor.close();
            }
            
            String baseName = fileName;
            int lastDot = fileName.lastIndexOf('.');
            if (lastDot != -1) {
                baseName = fileName.substring(0, lastDot);
            }
            
            final String finalBaseName = baseName;
            
            if ("db".equals(currentImportType)) {
                handleDatabaseImport(uri, finalBaseName);
            } else if ("sql".equals(currentImportType)) {
                handleSqlScriptImport(uri);
            } else if ("csv".equals(currentImportType) || "xlsx".equals(currentImportType)) {
                showTabularImportDialog(uri, finalBaseName);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Import preparation failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void handleDatabaseImport(Uri uri, String defaultDbName) {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setText(defaultDbName);
        input.setSingleLine(true);
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Import Database")
            .setMessage("Enter name for the imported database:")
            .setView(input)
            .setPositiveButton("Import", (dialog, which) -> {
                String dbName = input.getText().toString().trim();
                if (dbName.isEmpty() || dbName.contains("/") || dbName.contains("\\") || dbName.contains("..")) {
                    Toast.makeText(this, "ERROR: Invalid database name", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (!dbName.toLowerCase().endsWith(".db")) {
                    dbName = dbName + ".db";
                }
                
                try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
                    if (inputStream == null) {
                        Toast.makeText(this, "ERROR: Could not open file stream", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    if (!SQLImportHelper.isValidSQLiteHeader(inputStream)) {
                        Toast.makeText(this, "ERROR: Invalid SQLite database file header. File has been blocked for security.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    
                    try (InputStream freshInputStream = getContentResolver().openInputStream(uri)) {
                        java.io.File destination = getDatabasePath(dbName);
                        if (destination.getParentFile() != null) {
                            destination.getParentFile().mkdirs();
                        }
                        
                        try (java.io.FileOutputStream outputStream = new java.io.FileOutputStream(destination)) {
                            byte[] buffer = new byte[4096];
                            int read;
                            while ((read = freshInputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, read);
                            }
                        }
                        
                        viewModel.executeSQL("USE " + dbName.replace(".db", "") + ";");
                        Toast.makeText(this, "Database '" + dbName + "' imported and opened successfully!", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Database import failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void handleSqlScriptImport(Uri uri) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Run SQL Script")
            .setMessage("Are you sure you want to execute all statements from this SQL file in the active database?")
            .setPositiveButton("Run", (dialog, which) -> {
                try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
                    if (inputStream == null) return;
                    
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder script = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        script.append(line).append("\n");
                    }
                    
                    String sql = script.toString();
                    viewModel.executeSQL(sql);
                    Toast.makeText(this, "SQL Script executed successfully", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "SQL Script execution failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showTabularImportDialog(Uri uri, String defaultTableName) {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);
        
        android.widget.TextView labelTable = new android.widget.TextView(this);
        labelTable.setText("Table Name:");
        labelTable.setPadding(0, 10, 0, 10);
        layout.addView(labelTable);
        
        android.widget.EditText inputTable = new android.widget.EditText(this);
        String cleanDefault = defaultTableName.replaceAll("[^a-zA-Z0-9_]", "");
        inputTable.setText(cleanDefault.isEmpty() ? "imported_table" : cleanDefault);
        inputTable.setSingleLine(true);
        layout.addView(inputTable);
        
        android.widget.CheckBox cbHeader = new android.widget.CheckBox(this);
        cbHeader.setText("First row contains column headers");
        cbHeader.setChecked(true);
        cbHeader.setPadding(0, 20, 0, 10);
        layout.addView(cbHeader);
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Import Data to Table")
            .setView(layout)
            .setPositiveButton("Import", (dialog, which) -> {
                String tableName = inputTable.getText().toString().trim();
                boolean hasHeader = cbHeader.isChecked();
                
                if (tableName.isEmpty()) {
                    Toast.makeText(this, "Table name cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                new Thread(() -> {
                    try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
                        if (inputStream == null) return;
                        
                        List<List<String>> data;
                        if ("csv".equals(currentImportType)) {
                            data = SQLImportHelper.parseCSV(inputStream);
                        } else {
                            data = SQLImportHelper.parseXLSX(inputStream);
                        }
                        
                        runOnUiThread(() -> {
                            viewModel.importTabularData(tableName, data, hasHeader);
                        });
                        
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, "Import failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
                    }
                }).start();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void checkAndRequestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();
        
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.S_V2) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(android.Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
        
        if (android.os.Build.VERSION.SDK_INT >= 33) { // Build.VERSION_CODES.TIRAMISU is 33
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, "android.permission.POST_NOTIFICATIONS")
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add("android.permission.POST_NOTIFICATIONS");
            }
        }
        
        if (!permissionsNeeded.isEmpty()) {
            permissionLauncher.launch(permissionsNeeded.toArray(new String[0]));
        }
    }

    private void showRootWarningDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("⚠️ Security Warning")
            .setMessage("This device appears to be ROOTED. Root access compromises Android's sandbox security. " +
                        "Your database files and master encryption keys could be vulnerable to extraction by other applications " +
                        "or malicious software. Please exercise caution when working with sensitive databases.")
            .setPositiveButton("I Understand", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }
}