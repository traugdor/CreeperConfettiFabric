package su226.creeperconfetti;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CreeperExplosionTracker {
    private static final Map<UUID, Long> explodingCreepers = new ConcurrentHashMap<>();
    private static final long EXPIRATION_TIME_MS = 500; // 500ms should be enough
    
    public static void setCreeperExplosion(UUID creeperId) {
        explodingCreepers.put(creeperId, System.currentTimeMillis());
    }
    
    public static void clearCreeperExplosion() {
        // Clean up expired entries
        long currentTime = System.currentTimeMillis();
        explodingCreepers.entrySet().removeIf(entry -> 
            currentTime - entry.getValue() > EXPIRATION_TIME_MS
        );
    }
    
    public static boolean isCreeperExploding() {
        clearCreeperExplosion(); // Auto-cleanup
        return !explodingCreepers.isEmpty();
    }
}
