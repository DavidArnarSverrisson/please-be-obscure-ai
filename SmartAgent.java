import java.util.Collection;
import java.util.Random;

/**
 * Created by dasve_000 on 1/17/2015.
 */
public class SmartAgent implements Agent {

    private int direction = 0;

    private int axisNSSize = 0;
    private int axisWESize = 0;

    private int axisNSHome = 0;
    private int axisWEHome = 0;

    private boolean initAxisNS = false;
    private boolean initAxisWE = false;
    private boolean mapAxisNS = false;
    private boolean mapAxisWE = false;

    private boolean[][] roomMap;

    private int[] homeCell;
    private int[] currCell;

    private int unVisited;

    private boolean dimKnown = false;
    private boolean power = false;
    private boolean bump = false;
    private boolean dirt = false;


    public String nextAction(Collection<String> percepts) {
        bump = false;
        dirt = false;
        for (String s : percepts) {
            if(s.contentEquals("BUMP")) {
                bump = true;
            }
            if(s.contentEquals("DIRT")) {
                dirt = true;
            }
        }
        System.out.println("");
        String[] actions = {"TURN_ON", "TURN_OFF", "TURN_RIGHT", "TURN_LEFT", "GO", "SUCK"};

        switch(determineActionPhase()) {
            case 0:
                return powerOn();
            case 1:
                return mapRoom();
            case 2:
                // TODO - algorithm:
                // go to unvisitied cell with lowest move cost
                // when no unvisited cell left, go to next phase.
                return "TURN_OFF";
            case 12:
                return suck();
        }


        return actions[0];
    }

    private int determineActionPhase() {
        // First things first: turn on the robot
        if(!power) {
            return 0;
        }
        // if there is dirt, suck it up sucker
        if (dirt) {
            return 12;
        }
        // If we dont know the dimensions, that is our top priority
        if (!dimKnown) {
            return 1;
        }
        // Visit all cells of the room
        if (unVisited > 0) {
            return 2;
        }

        // Return to home cell
        return 3;
    }

    private String mapRoom() {
        // First we want to bump against the north wall
        if(!initAxisNS) {
            return bumpNWall();
        }
        if(!initAxisWE) {
            return bumpWWall();
        }
        if(!mapAxisNS) {
            return bumpSWall();
        }
        if(!mapAxisWE) {
            return bumpEWall();
        }
        // if at this point - then we should have mapped the world (room)!
        dimKnown = true;
        drawMap();
        printMap();
        currCell = new int[2];
        currCell[0] = 0;
        currCell[1] = axisWESize - 1;
        return "TURN_OFF";
    }

    private void drawMap() {
        roomMap = new boolean[axisNSSize][axisWESize];
        int visited = 0;

        // We have visited the south edge
        for (int j = 0; j < axisWESize; j++){
            roomMap[axisNSSize - 1][j] = true;
            visited++;
        }
        // We have visited the west edge
        for(int k = 0; k < axisNSSize; k++) {
            roomMap[k][0] = true;
            visited++;
        }
        // We have visited part of the north edge
        for (int l = 0; l < axisWEHome; l++) {
            roomMap[l][0] = true;
            visited++;
        }
        unVisited = axisNSSize * axisWESize;
        unVisited = unVisited - visited;
    }

    private void printMap() {
        System.out.println("MAP CURRENTLY LOOKS LIKE:");
        for(int i = 0;  i < axisNSSize; i++) {
            for(int j = 0; j < axisWESize; j++) {
                if(roomMap[i][j]) {
                    System.out.print(" X");
                }
                else {
                    System.out.print("  ");
                }
            }
            System.out.print("\n");
        }
        System.out.println("HomeCell NS axis is: " + axisNSHome);
        System.out.println("HomeCell WE axis is: " + axisWEHome);
        System.out.println("UNVISITED CELLS: " + unVisited);
    }

    private String powerOn() {
        power = true;
        return "TURN_ON";
    }

    private String suck() {
        return "SUCK";
    }

    private String bumpNWall() {
        // Bump the wall to the north. If already at edge, turn left
        if (direction % 4 != 0) {
            return turnNorth();
        }
        else if (bump) {
            initAxisNS = true;
            return turnWest();
        }
        else
            axisNSHome++;
            return "GO";
    }

    private String bumpWWall() {
        if (currDirection() != 3) {
            return turnWest();
        }
        else if(bump) {
            initAxisWE = true;
            return turnSouth();
        }
        else {
            axisWEHome++;
            return "GO";
        }
    }

    private String bumpSWall() {
        if(currDirection() != 2) {
            return turnSouth();
        }
        else if (bump) {
            mapAxisNS = true;
            return turnEast();
        }
        else {
            axisNSSize++;
            return "GO";
        }
    }

    private String bumpEWall() {
        if(currDirection() != 1) {
            return turnEast();
        }
        else if (bump) {
            mapAxisWE = true;
            return turnNorth();
        }
        else {
            axisWESize++;
            return "GO";
        }
    }

    private String turnNorth() {
        System.out.println("TURNING NORHT!");
        if (currDirection() == 0) {
            return "ALREADY TURNED NORTH";
        }
        else if (currDirection() == 1) {
            return turnLeft();
        }
        else if (currDirection() == 2) {
            Random r = new Random();
            if (r.nextBoolean()) {
                return turnRight();
            }
            return turnLeft();
        }
        else if(currDirection() == 3) {
            return turnRight();
        }
        return "I AM A HORRIBLE PROGRAMMER!";
    }

    private String turnWest() {
        System.out.println("TURNING WEST!");
        if (currDirection() == 0) {
            return turnLeft();
        }
        else if (currDirection() == 1) {
            return "ALREADY TURNED WEST!";
        }
        else if (currDirection() == 2) {
            return turnRight();
        }
        else if(currDirection() == 3) {
            Random r = new Random();
            if (r.nextBoolean()) {
                return turnRight();
            }
            return turnLeft();
        }
        return "I AM A HORRIBLE PROGRAMMER!";
    }

    private String turnSouth() {
        System.out.println("TURNING SOUTH!");
        if (currDirection() == 0) {
            Random r = new Random();
            if (r.nextBoolean()) {
                return turnRight();
            }
            return turnLeft();
        }
        else if (currDirection() == 1) {
            return turnRight();
        }
        else if (currDirection() == 2) {
            return "ALREADY TURNED SOUTH!";
        }
        else if(currDirection() == 3) {
            return turnLeft();
        }
        return "I AM A HORRIBLE PROGRAMMER!";
    }

    private String turnEast() {
        System.out.println("TURNING EAST!");
        if (currDirection() == 0) {
            return turnRight();
        }
        else if (currDirection() == 1) {
            return "ALREADY TURNED SOUTH!";
        }
        else if (currDirection() == 2) {
            return turnLeft();
        }
        else if(currDirection() == 3) {
            Random r = new Random();
            if (r.nextBoolean()) {
                return turnRight();
            }
            return turnLeft();
        }
        return "I AM A HORRIBLE PROGRAMMER!";

    }


    private String turnLeft() {
        direction += 3;
        return "TURN_LEFT";
    }

    private String turnRight() {
        direction += 1;
        return "TURN_RIGHT";
    }

    private int currDirection() {
        return direction % 4;
    }
}
