import java.util.Collection;

/**
 * Created by dasve_000 on 1/17/2015.
 */
public class DoubleAgent implements Agent {

    private int steps = 0;
    private int direction = 0;

    private int acIndex;

    private int homeCoordinate02;
    private int homeCoordinate13;

    private int travelDist02;
    private int travelDist13;

    private boolean movedEast;
    private boolean wasNorth;
    private boolean bump;

    private boolean power = false;



    public String nextAction(Collection<String> percepts) {
        if(!power) {
            power = !power;
            acIndex = 0;
        }
        bump = false;
        for(String s : percepts) {
            if(s.contentEquals("DIRT")) {
                return "SUCK";
            }
            if(s.contentEquals("BUMP")) {
                bump = true;
            }
        }
        if(steps == 0) {
            System.out.println("At step " + steps);
            if(bump) {
                steps++;
                direction += 3;
                wasNorth = true;
                homeCoordinate02--;
                return "TURN_LEFT";
            }
            else {
                homeCoordinate02++;
                acIndex = 4;
            }
        }
        if(steps == 1) {
            System.out.println("At step " + steps);
            if(bump) {
                steps++;
                direction += 3;
                homeCoordinate13--;
                return "TURN_LEFT";
            }
            homeCoordinate13++;
            acIndex = 4;
        }
        if(steps == 2) {
            System.out.println("At step " + steps + " facing " + direction % 4);
            // s bump
            if(bump && direction % 4 == 2) {
                direction += 3;
                acIndex = 3;
                wasNorth = false;
            }
            // n bump
            else if(bump && direction % 4 == 0) {
                direction += 1;
                acIndex = 2;
                wasNorth = true;
            }
            // e bump
            else if(bump && direction % 4 == 1) {
                steps++;
            }
            else if(direction % 4 == 1) {
                if(movedEast) {
                    if(wasNorth) {
                        direction += 1;
                        acIndex = 2;
                    }
                    else {
                        direction += 3;
                        acIndex = 3;
                    }
                    movedEast = false;
                }
                else {
                    acIndex = 4;
                    movedEast = true;
                }
            }
            else {
                acIndex = 4;
            }
        }
        if(steps == 3) {
            System.out.println("At step " + steps);
            // at e edge - go to w edge
                if(direction % 4 != 3) {
                    direction += 1;
                    acIndex = 2;
                }
            else if(direction % 4 == 3 && bump) {
                    steps++;
            }
            else {
                    acIndex = 4;
                }
        }
        if(steps == 4) {
            System.out.println("At step " + steps + " Was North? " + wasNorth);
            if(wasNorth) {
                // already at nw corner
                steps++;
            }
            else {
                // at sw corner, go to nw corner
                if(direction % 4 == 0) {
                    if(bump) {
                        // at nw corner
                        steps++;
                    }
                    else {
                        acIndex = 4;
                    }
                }
                else {
                    direction += 1;
                    acIndex = 2;
                }
            }

        }
        if(steps == 5) {
            // at nw corner
            System.out.println("At step " + steps + ". Going to travel " + homeCoordinate13 + " cells. Have travelled " + travelDist13 + " cells.");
            if(direction % 4 != 1) {
                // turn east
                direction += 1;
                acIndex = 2;
            }
            else if(direction % 4 == 1 && travelDist13 == homeCoordinate13) {
                steps++;
            }
            else {
                travelDist13 += 1;
                acIndex = 4;
            }
        }
        if(steps == 6) {
            // at n edge, right above home
            System.out.println("At step " + steps);
            if(direction % 4 != 2) {
                // turn south
                direction += 1;
                acIndex = 2;
            }
            else if(direction % 4 == 2 && travelDist02 == homeCoordinate02) {
                steps++;
            }
            else {
                travelDist02 += 1;
                acIndex = 4;
            }
        }
        if(steps == 7) {
            System.out.println("At step " + steps);
            acIndex = 1;
        }

        String[] actions = { "TURN_ON", "TURN_OFF", "TURN_RIGHT", "TURN_LEFT", "GO", "SUCK" };
        return actions[acIndex];

    }
}
