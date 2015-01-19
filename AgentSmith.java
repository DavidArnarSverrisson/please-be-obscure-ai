package vacuumcleaner.src;
import java.util.Collection;
import java.util.Random;

public class AgentSmith implements Agent
{	
	private Random random = new Random();
	private boolean isOn = false;
	private boolean firstInitDone = false;
	private boolean secondInitDone = false;
	private boolean bump = false;
	private boolean dirt = false;
	int wayFacing = 0; // 0: north, 1: east, 2: south, 3: west
	private boolean movingNorth = false;
	private boolean moveEast = false;
	private boolean movedEast = false;
	private boolean moveHome = false;
	private boolean movedNorthHome = false;
	
	private int tilesMovedNorth = 0;
	private int tilesMovedEast = 0;
	
    public String nextAction(Collection<String> percepts) {
    	System.out.println(percepts);
    	
    	// Turn agent on if it's off
    	if(!isOn)
    	{
    		isOn = true;
    		//System.out.println("--Turning on");
    		return "TURN_ON";
    	}
    	
    	// Percieve the surroundings
    	bump = false; dirt = false;
    	for (String s : percepts) {
            if(s.contentEquals("BUMP")) {
                bump = true;
            }
            if(s.contentEquals("DIRT")) {
                dirt = true;
            }
        }
    	
    	// We've survayed the room. Now we start moving home
    	if(moveHome)
    	{
    		return MoveHome();
    	}
    	
    	// Special instance if we just moved east and bumped. We decrement the tiles moved so the bump isn't counted as a successful move
    	if(movedEast && bump)
    		tilesMovedEast--;
    	
    	// If dirt is found, we ignore everything else and just suck
    	if(dirt)
    		return "SUCK";
    	
    	// Initialization is done by moving straight ahead after spawning until a wall is bumped into, then moving left until another wall is bumped into.
    	// The agent assumes the direction he faces when spawning is North. So essentially during initialization he moved towards the Nort-West corner of the room
    	if(!firstInitDone || !secondInitDone)
    		return Initialize();
    	
    	// After initialization the agent moves South until he hits a wall, then takes a step East, then moves North until he hits a wall.
    	// Essentially he zig-zags up and down, doing one column at a time, until he hits a bump when he tries taking a step East.
    	
    	// Agent has finished moving South/North so he takes a step East
    	if(moveEast)
    	{
    		tilesMovedEast++;
    		moveEast = false;
    		// Next step we need to know that we just moved east. So that if we bump into a wall when moving east we know we have finished survaying the room
    		movedEast = true;
    		// Move east
    		return "GO";
    	}
    	// Move north
    	else if(movingNorth)
    		return MoveNorth();
    	// Move south
    	else
    		return MoveSouth();
	}
    
    private String Initialize()
    {
    	// Move straight ahead after spawning (Agent assumes this is North) until we hit a wall
    	if(!firstInitDone)
    	{
    		if(bump)
    		{
    			tilesMovedNorth--;
    			firstInitDone = true;
    			TurnedLeft();
    			return "TURN_LEFT";
    		}
    		else
    		{
    			tilesMovedNorth++;
    			return "GO";
    		}
    	}
    	// Move straight West after hitting the North wall after spawning
    	else
    	{
    		if(bump)
    		{
    			tilesMovedEast++;
    			System.out.println("Init done!");
    			secondInitDone = true;
    			TurnedLeft();
    			return "TURN_LEFT";
    		}
    		else
    		{
    			tilesMovedEast--;
    			return "GO";
    		}
    	}
    }
    
    // Start moving home
    private String MoveHome()
    {
    	/* When we start moving home the agent is already oriented in the correct direction North/South.
    	 * So we basically have 3 cases when moving Home:
    	 * 1. Move North/South as many tiles as tilesMovedNorth tells us
    	 * 2. We've finished moving North/South. So now we turn to face the correct orientation West/East
    	 * 3. Move East/West as many tiles as tilesMovedEast tells us
    	 * We've now reached Home!! 
    	 */
    	
    	// Here we've finished moving North/South so we turn to face the correct orientation
    	if(movedNorthHome)
    	{
    		// We turn left if: We're facing North and need to move West. Or we're facing South and need to move East.
    		if(tilesMovedEast > 0 && wayFacing == 0 || tilesMovedEast < 0 && wayFacing == 2)
    		{
    			movedNorthHome = false;
    			TurnedLeft();
    			return "TURN_LEFT";
    		}
    		// We turn right if: We're facing North and need to move East. Or we're facing South and need to move West.
    		else
    		{
    			movedNorthHome = false;
    			TurnedRight();
    			return "TURN_RIGHT";
    		}
    	}
    	// Move North/South
    	else if(tilesMovedNorth != 0)
    	{
    		// We just moved south so we decrement tilesMovedNorth until it reaches 0
    		if(tilesMovedNorth > 0)
    			tilesMovedNorth--;
    		// We just moved north so we increment tilesMovedNorth until it reaches 0
    		else
    			tilesMovedNorth++;
    		
    		// If tilesMovedNorth just changed from 1/-1 to 0 we've finished moving North/South. Next we want to turn West/East then start moving in that direction
    		if(tilesMovedNorth == 0)
    			movedNorthHome = true;
    		
    		return "GO";
    	}
    	// Move East/West
    	else if(tilesMovedEast != 0)
    	{
    		// We just moved west so we decrement tilesMovedEast until it reaches 0
    		if(tilesMovedEast > 0)
    			tilesMovedEast--;
    		// We just moved east so we increment tilesMovedEast until it reaches 0
    		else
    			tilesMovedEast++;
    		
    		return "GO";
    	}
    	moveHome = false;
    	return "TURN_OFF";
    }
    
    // Agent is moving north
    private String MoveNorth()
    {
    	// We hit the wall when moving north
    	if(bump && !movedEast)
		{
    		tilesMovedNorth--;
    		// Turn right to face eastwards
			TurnedRight();
			// Queue moving east(GO) for next step
			moveEast = true;
			return "TURN_RIGHT";
		}
    	// We hit a bump when moving east. Meaning we've finished survaying the room and want to move home
    	else if(bump && movedEast)
    	{
    		// Special case need when we finish in the same row as our home. We then tell the agent to simply turn right away and start going East/West home
    		if(tilesMovedNorth == 0)
    			movedNorthHome = true;
    		
    		moveHome = true;
    		// North and East are both walls. So our home must be South of us. So we turn South.
    		TurnedRight();
    		return "TURN_RIGHT";
    	}
    	// We managed to move one step eastwards. So now we turn right and now start moving south
    	else if(movedEast)
    	{
    		TurnedRight();
    		movedEast = false;
    		// We now face South
    		movingNorth = false;
    		return "TURN_RIGHT";
    	}
    	// Move north
		else
		{
			tilesMovedNorth++;
			return "GO";
		}
    }
    
 // Agent is moving south
    private String MoveSouth()
    {
    	// We hit the wall when moving south
    	if(bump && !movedEast)
		{
    		tilesMovedNorth++;
    		// Turn left to face eastwards
			TurnedLeft();
			// Queue moving east(GO) for next step
			moveEast = true;
			return "TURN_LEFT";
		}
    	// We hit a bump when moving east. Meaning we've finished survaying the room and want to move Home
    	else if(bump && movedEast)
    	{
    		// Special case need when we finish in the same row as our home. We then tell the agent to simply turn right away and start going East/West home
    		if(tilesMovedNorth == 0)
    			movedNorthHome = true;
    		
    		moveHome = true;
    		// South and East are both walls. So our home must be North of us. So we turn North.
    		TurnedLeft();
    		return "TURN_LEFT";
    	}
    	// We managed to move one step eastwards. So now we turn right and now start moving north
    	else if(movedEast)
    	{
    		TurnedLeft();
    		movedEast = false;
    		// We now face North
    		movingNorth = true;
    		return "TURN_LEFT";
    	}
    	// Move south
		else
		{
			tilesMovedNorth--;
			return "GO";
		}
    }
    
    private void TurnedLeft()
    {
    	// Update the agents orientation after moving left. North -> West :: West -> South :: South -> East :: East -> North
    	wayFacing--;
    	if(wayFacing < 0)
    		wayFacing = 3;
    }
    
    private void TurnedRight()
    {
    	// Update the agents orientation after moving right. North -> East :: East -> South :: South -> West :: West -> North
    	wayFacing++;
    	if(wayFacing > 3)
    		wayFacing = 0;
    }
}
