import java.util.Random;

public class Reproduction {
    public static void main(String[] args) {
        int maxBlockRange = 1000;
        Random randomNum = new Random();

        // Simulating player at x=1000, z=1000
        int playerX = 1000;
        int playerZ = 1000;

        System.out.println("Player Location: X=" + playerX + ", Z=" + playerZ);
        System.out.println("Max Range: " + maxBlockRange);

        for (int i = 0; i < 10; i++) {
            int randLocX = playerX + randomNum.nextInt(maxBlockRange);
            int randLocZ = playerZ + randomNum.nextInt(maxBlockRange);

            if (randomNum.nextInt(maxBlockRange) % 2 == 0) {
                randLocX = -randLocX;
                randLocZ = -randLocZ;
            }

            double distanceX = Math.abs(randLocX - playerX);
            double distanceZ = Math.abs(randLocZ - playerZ);
            double distance = Math.sqrt(Math.pow(randLocX - playerX, 2) + Math.pow(randLocZ - playerZ, 2));

            System.out.println("Run " + i + ": Target X=" + randLocX + ", Target Z=" + randLocZ + " | Distance: " + distance);

            if (distance > maxBlockRange) {
                System.out.println("FAILURE: Distance " + distance + " is greater than max range " + maxBlockRange);
            }
        }
    }
}
