import java.io.*;
import java.util.*;

/**
 * EdaSession4 - ProFootball 2025 Matchmaking Simulation
 * Authors: [Initials], Team: [Code]
 */
public class EdaSession4 {

    public static void main(String[] args) {
        List<Request> requests = readRequests("player_requests.csv");
        printRequests(requests);

        // Queues
        PriorityQueue<Request> premiumLong = new PriorityQueue<>(Comparator.comparingInt(Request::getSkillLevel).reversed());
        PriorityQueue<Request> premiumShort = new PriorityQueue<>(Comparator.comparingInt(Request::getSkillLevel).reversed());
        Queue<Request> nonPremiumLong = new LinkedList<>();
        Queue<Request> nonPremiumShort = new LinkedList<>();

        // Distribute requests into queues
        for (Request r : requests) {
            if (r.isPremium()) {
                if (r.getMatchType() == 'L') premiumLong.add(r);
                else premiumShort.add(r);
            } else {
                if (r.getMatchType() == 'L') nonPremiumLong.add(r);
                else nonPremiumShort.add(r);
            }
        }

        // Process matches
        processMatches(premiumLong, "Premium", "Long", 2);
        processMatches(premiumShort, "Premium", "Short", 2);
        processMatches(nonPremiumLong, "Non-premium", "Long", 1);
        processMatches(nonPremiumShort, "Non-premium", "Short", 1);
    }

    /** Reads requests from CSV file */
    private static List<Request> readRequests(String filename) {
        List<Request> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                String reqId = parts[0];
                String playerId = parts[1];
                boolean premium = Boolean.parseBoolean(parts[2]);
                int skill = Integer.parseInt(parts[3]);
                char type = parts[4].charAt(0);
                list.add(new Request(reqId, playerId, premium, skill, type));
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
        return list;
    }

    /** Prints all requests */
    private static void printRequests(List<Request> requests) {
        System.out.println("Requests:");
        for (Request r : requests) {
            System.out.println(r);
        }
        System.out.println();
    }

    /** Processes matches from a queue */
    private static void processMatches(Queue<Request> queue, String type, String matchType, int matchesPerCycle) {
        while (queue.size() >= 2) {
            for (int i = 0; i < matchesPerCycle && queue.size() >= 2; i++) {
                Request r1 = queue.poll();
                Request r2 = queue.poll();
                System.out.printf("%s %s Match: %s (Skill %d) vs %s (Skill %d)%n",
                        type, matchType, r1.getPlayerId(), r1.getSkillLevel(), r2.getPlayerId(), r2.getSkillLevel());
            }
        }
    }
}

/**
 * Represents a match request.
 */
class Request {
    private final String requestId;
    private final String playerId;
    private final boolean premium;
    private final int skillLevel;
    private final char matchType;

    public Request(String requestId, String playerId, boolean premium, int skillLevel, char matchType) {
        this.requestId = requestId;
        this.playerId = playerId;
        this.premium = premium;
        this.skillLevel = skillLevel;
        this.matchType = matchType;
    }

    public String getRequestId() { return requestId; }
    public String getPlayerId() { return playerId; }
    public boolean isPremium() { return premium; }
    public int getSkillLevel() { return skillLevel; }
    public char getMatchType() { return matchType; }

    @Override
    public String toString() {
        return String.format("%s; %s; Premium: %b; Skill: %d; Type: %c",
                requestId, playerId, premium, skillLevel, matchType);
    }
}