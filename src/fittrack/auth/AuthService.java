package fittrack.auth;

import fittrack.model.User;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/** Manages user accounts and authentication with SHA-256 hashing. */
public class AuthService {
    private static AuthService instance;
    private final Map<String, User> users = new LinkedHashMap<>();
    private User currentUser;

    private AuthService() {
        register("admin",  "admin123",  User.Role.ADMIN,   "Alex Admin",     72.0, 175.0, 30, "General Fitness");
        register("alex",   "fit123",    User.Role.ATHLETE, "Alex Runner",    68.0, 172.0, 25, "Endurance");
        register("sam",    "gym456",    User.Role.ATHLETE, "Sam Lifter",     85.0, 180.0, 28, "Muscle Gain");
        register("morgan", "yoga789",   User.Role.ATHLETE, "Morgan Flow",    60.0, 165.0, 32, "Weight Loss");
    }

    public static synchronized AuthService getInstance() {
        if (instance == null) instance = new AuthService();
        return instance;
    }

    public void register(String username, String password, User.Role role,
                         String displayName, double weight, double height, int age, String goal) {
        users.put(username.toLowerCase(),
                new User(username.toLowerCase(), hash(password), role, displayName, weight, height, age, goal));
    }

    public User login(String username, String password) {
        User u = users.get(username.toLowerCase());
        if (u != null && u.getPasswordHash().equals(hash(password))) { currentUser = u; return u; }
        return null;
    }

    public void logout()               { currentUser = null; }
    public User getCurrentUser()       { return currentUser; }
    public boolean isLoggedIn()        { return currentUser != null; }
    public List<User> getAllUsers()    { return new ArrayList<>(users.values()); }
    public List<String> getUsernames() { return new ArrayList<>(users.keySet()); }

    private String hash(String input) {
        try {
            byte[] b = MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte x : b) sb.append(String.format("%02x", x));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) { return input; }
    }
}
