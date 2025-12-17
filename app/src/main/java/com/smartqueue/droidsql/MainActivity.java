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
import com.smartqueue.droidsql.databinding.ActivityMainBinding;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Setup ViewBinding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(DatabaseViewModel.class);

        // Setup UI listeners
        setupListeners();

        // Observe LiveData for reactive UI updates
        observeViewModel();
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
                
                String sql = binding.etSqlInput.getText().toString().trim();
                
                // STANDARD: Only execute if command ends with semicolon (;)
                // Also allow strict EXIT/QUIT without semicolon for convenience
                if (!sql.isEmpty() && (sql.endsWith(";") || 
                    sql.equalsIgnoreCase("EXIT") || 
                    sql.equalsIgnoreCase("QUIT"))) {
                    
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
                if (count == 1 && s.length() > start && s.charAt(start) == '\n') {
                    // Newline detected
                    String currentText = binding.etSqlInput.getText().toString();
                    
                    if (currentText.endsWith("\n")) {
                        // Check content BEFORE the newline
                        String rawSql = currentText.substring(0, currentText.length() - 1);
                        String trimmedSql = rawSql.trim();
                        
                        // Execute ONLY if valid terminator found
                        if (!trimmedSql.isEmpty() && (trimmedSql.endsWith(";") || 
                             trimmedSql.equalsIgnoreCase("EXIT") || 
                             trimmedSql.equalsIgnoreCase("QUIT"))) {
                            
                            viewModel.executeSQL(trimmedSql);
                            binding.etSqlInput.setText("");
                        }
                        // Otherwise, let the newline stay (multiline input)
                    }
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
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
        String[] options = {"Font Size", "Color Theme"};
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Terminal Settings")
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    showFontSizeDialog();
                } else if (which == 1) {
                    showColorThemeDialog();
                }
            })
            .setNegativeButton("Close", null)
            .show();
    }

    private void showFontSizeDialog() {
        String[] fonts = {"Small (10sp)", "Medium (13sp)", "Large (16sp)", "Extra Large (20sp)"};
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
        String[] themes = {"Matrix Green", "Cyberpunk Amber", "Classic White", "Hacker Cyan"};
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
        applyFontSize(prefs.getInt("font_size_index", 1));
        applyTheme(prefs.getInt("theme_index", 0));
    }

    private void applyFontSize(int index) {
        float size;
        switch (index) {
            case 0: size = 10f; break;
            case 1: size = 13f; break; // Default
            case 2: size = 16f; break;
            case 3: size = 20f; break;
            default: size = 13f;
        }
        
        binding.tvTerminalOutput.setTextSize(size);
        binding.etSqlInput.setTextSize(size);
        binding.tvPrompt.setTextSize(size);
    }

    private void applyTheme(int index) {
        int color;
        switch (index) {
            case 0: color = 0xFF00FF00; break; // Matrix Green
            case 1: color = 0xFFFFB000; break; // Cyberpunk Amber
            case 2: color = 0xFFEEEEEE; break; // Classic White
            case 3: color = 0xFF00FFFF; break; // Hacker Cyan
            default: color = 0xFF00FF00;
        }
        
        // Apply color to terminal elements
        binding.tvTerminalOutput.setTextColor(color);
        binding.etSqlInput.setTextColor(color);
        // Prompt can stay distinct or match theme - matching theme looks cleaner
        binding.tvPrompt.setTextColor(color); 
        
        // Also update status bar icons for consistency
        // Also update status bar icons for consistency
        binding.topToolbar.setTitleTextColor(color); // Apply to Toolbar Title
        binding.btnPrevious.setTextColor(color);
        binding.btnNext.setTextColor(color);
        binding.btnListTables.setTextColor(color);
        binding.btnExport.setTextColor(color);
        binding.btnSettings.setTextColor(color);
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
        
        // Auto-open logic
        java.io.File dbFile = getDatabasePath("ecommerce.db");
        if (!dbFile.exists()) {
            // First run (or data cleared): Generate sample data
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

    /**
     * Updates the command prompt indicator (MySQL-style)
     */
    private void updatePrompt() {
        // Always show 'mysql>' prompt like real MySQL
        binding.tvPrompt.setText("mysql>");
        binding.tvPrompt.setTextColor(getResources().getColor(R.color.terminal_success, null));
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
                
                // Safe Color Retrieval
                int color = androidx.core.content.ContextCompat.getColor(this, R.color.terminal_text);
                item.setTextColor(color);
                
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
                    divider.setBackgroundColor(0xFF333333); // Dark Gray
                    listContainer.addView(divider);
                }
            }
            
            // Setup Action Buttons with error handling
            android.view.View btnSample = dialogView.findViewById(R.id.btnActionSample);
            if (btnSample != null) {
                btnSample.setOnClickListener(v -> {
                    viewModel.generateSampleDatabase();
                    dialog.dismiss();
                });
            }
            
            android.view.View btnTips = dialogView.findViewById(R.id.btnActionTips);
            if (btnTips != null) {
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
        // ViewModel automatically closes database in onCleared()
    }
}