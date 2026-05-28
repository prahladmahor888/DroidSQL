# Project-specific ProGuard rules for security hardening

# Strip source file name and line numbers to prevent reverse engineering of source hierarchy
-keepattributes !SourceFile,!LineNumberTable

# Package-level obfuscation: collapse and move classes into a single package
-repackageclasses 'com.smartqueue.droidsql.secure'

# Allow package access modifiers to be made public/private during optimization
-allowaccessmodification

# Strip Android logging output to prevent leak of queries or databases in production Logcat
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}