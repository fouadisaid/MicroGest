import org.mindrot.jbcrypt.BCrypt;

public class GenerateHash {
    public static void main(String[] args) {
        String adminHash = BCrypt.hashpw("admin123", BCrypt.gensalt());
        String agentHash = BCrypt.hashpw("agent123", BCrypt.gensalt());
        String superHash = BCrypt.hashpw("super123", BCrypt.gensalt());

        System.out.println("============================================");
        System.out.println("🔑 GÉNÉRATION DES HASH POUR MICROGEST");
        System.out.println("============================================");
        System.out.println();
        System.out.println("-- admin / admin123");
        System.out.println("UPDATE users SET password = '" + adminHash + "' WHERE username = 'admin';");
        System.out.println();
        System.out.println("-- agent1 / agent123");
        System.out.println("UPDATE users SET password = '" + agentHash + "' WHERE username = 'agent1';");
        System.out.println();
        System.out.println("-- superviseur1 / super123");
        System.out.println("UPDATE users SET password = '" + superHash + "' WHERE username = 'superviseur1';");
        System.out.println();
        System.out.println("============================================");
        System.out.println("📋 Copiez-collez ces commandes dans PostgreSQL");
        System.out.println("============================================");
    }
}