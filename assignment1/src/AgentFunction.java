/*
 * Class that defines the agent function.
 * 
 * Written by James P. Biagioni (jbiagi1@uic.edu)
 * for CS511 Artificial Intelligence II
 * at The University of Illinois at Chicago
 * 
 * Last modified 2/19/07 
 * 
 * DISCLAIMER:
 * Elements of this application were borrowed from
 * the client-server implementation of the Wumpus
 * World Simulator written by Kruti Mehta at
 * The University of Texas at Arlington.
 * 
 */

import java.util.PriorityQueue;
import java.util.Random;

class AgentFunction {

    // string to store the agent's name
    // do not remove this variable
    private String agentName = "Agent Smith";

    // all of these variables are created and used
    // for illustration purposes; you may delete them
    // when implementing your own intelligent agent
    private int[] actionTable;
    private boolean bump;
    private boolean glitter;
    private boolean breeze;
    private boolean stench;
    private boolean scream;
    private Random rand;

    MyGraph graph;


    public AgentFunction() {
        // for illustration purposes; you may delete all code
        // inside this constructor when implementing your
        // own intelligent agent

        // this integer array will store the agent actions
        actionTable = new int[8];

        actionTable[0] = Action.GO_FORWARD;
        actionTable[1] = Action.GO_FORWARD;
        actionTable[2] = Action.GO_FORWARD;
        actionTable[3] = Action.GO_FORWARD;
        actionTable[4] = Action.TURN_RIGHT;
        actionTable[5] = Action.TURN_LEFT;
        actionTable[6] = Action.GRAB;
        actionTable[7] = Action.SHOOT;

        // new random number generator, for
        // randomly picking actions to execute
        rand = new Random();
        rand.setSeed(0);

        graph = new MyGraph(rand);
    }

    public int process(TransferPercept tp) {
        // To build your own intelligent agent, replace
        // all code below this comment block. You have
        // access to all percepts through the object
        // 'tp' as illustrated here:

        if (tp.getGlitter()) {
            return Action.GRAB;
        }

        // read in the current percepts
        bump = tp.getBump();
        breeze = tp.getBreeze();
        stench = tp.getStench();
        scream = tp.getScream();

        graph.getCurrNode().isBump = bump;
        graph.getCurrNode().isBreeze = breeze;
        graph.getCurrNode().isStench = stench;

        if (bump) {
            graph.wasBumped();
        }

        graph.updateGraph();

        if (graph.getCurrentActions().size() == 0) {
            graph.updateCurrentFrontier();
        }

        int action = (Integer)graph.getCurrentActions().poll();
        if (action == Action.TURN_LEFT) {
            graph.turnLeft();
        } else if (action == Action.TURN_RIGHT) {
            graph.turnRight();
        } else if (action == Action.GO_FORWARD) {
            graph.goForward();
        } else {
            action = Action.GRAB;
        }

        return action;
    }

    // public method to return the agent's name
    // do not remove this method
    public String getAgentName() {
        return agentName;
    }

}