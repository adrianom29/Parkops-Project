import java.io.Console;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Reads a private key from a .env file, system environment variables,
 * or safely via interactive console input.
 */
public final class GeminiCredentials {

    private GeminiCredentials() {
    }

    public static String readKey() {
        // 1. Check the .env file first
        String configured = readDotEnv("GEMINI_API_KEY");

        // 2. Fall back to system environment variables if not in .env
        if (configured == null || configured.isBlank()) {
            configured = System.getenv("GEMINI_API_KEY");
        }

        // 3. Return the key if found in either .env or system env
        if (configured != null && !configured.isBlank()) {
            return configured.strip();
        }

        // 4. Safe manual entry fallback if no environment variable exists
        Console terminal = System.console();
        if (terminal == null) {
            throw new IllegalArgumentException(
                    "Run directly in a terminal, without piping or redirecting input/output, so the key can be entered privately.");
        }
        char[] entered = terminal.readPassword("Paste your Gemini API key, then press Enter (nothing is displayed): ");
        if (entered == null) {
            throw new IllegalArgumentException("No key entered; no request made");
        }
        try {
            String key = new String(entered).strip();
            if (key.isBlank()) {
                throw new IllegalArgumentException("No key entered; no request made");
            }
            return key;
        } finally {
            Arrays.fill(entered, '\0');
        }
    }

    /**
     * Minimal .env reader (no external dependency): looks for KEY=VALUE in
     * ./.env (the project root when launched via run.ps1/run.sh). Blank lines
     * and # comments are ignored; surrounding quotes are removed. Returns null
     * if the file or key is absent. Never prints the value.
     */
    private static String readDotEnv(String name) {
        Path file = Path.of(".env");
        if (!Files.isRegularFile(file)) {
            return null;
        }
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                String trimmed = line.strip();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                if (trimmed.startsWith("export ")) {
                    trimmed = trimmed.substring("export ".length()).strip();
                }
                int eq = trimmed.indexOf('=');
                if (eq <= 0 || !trimmed.substring(0, eq).strip().equals(name)) {
                    continue;
                }
                String value = trimmed.substring(eq + 1).strip();
                if (value.length() >= 2
                        && ((value.startsWith("\"") && value.endsWith("\""))
                        || (value.startsWith("'") && value.endsWith("'")))) {
                    value = value.substring(1, value.length() - 1);
                }
                return value;
            }
        } catch (IOException ex) {
            return null;
        }
        return null;
    }
}
