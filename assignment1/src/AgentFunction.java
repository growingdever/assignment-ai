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

import com.sun.tools.javac.util.Assert;

import java.util.*;

class AgentFunction {

    static int MAX_LABYRINTH_SIZE = 20;
    static int MAX_SAFE_LABYRINTH_SIZE = 41;
    static int DIR_LEFT = 0;
    static int DIR_TOP = 1;
    static int DIR_RIGHT = 2;
    static int DIR_BOTTOM = 3;

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

    MyNode[][] labyrinth;
    int currDir = DIR_LEFT;
    int currX, currY;
    int prevX, prevY;

    ArrayList<MyNode> frontiers;
    MyNode currFrontier;

    ArrayDeque<Integer> currActions;


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

        init();
    }

    void init() {
        int x, y;

        y = -(MAX_LABYRINTH_SIZE + 1);

        labyrinth = new MyNode[MAX_SAFE_LABYRINTH_SIZE][];
        for (int i = 0; i < labyrinth.length; i++) {
            x = -MAX_LABYRINTH_SIZE;
            y ++;

            labyrinth[i] = new MyNode[labyrinth.length];
            for (int j = 0; j < labyrinth.length; j ++) {
                labyrinth[i][j] = new MyNode(x, y);

                x ++;
            }
        }

        prevX = currX = MAX_LABYRINTH_SIZE;
        prevY = currY = MAX_LABYRINTH_SIZE;

        frontiers = new ArrayList<>();
        currActions = new ArrayDeque<>();
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

        MyNode currNode = labyrinth[currY][currX];
        currNode.isVisited = true;
        currNode.isBump = bump;
        currNode.isBreeze = breeze;
        currNode.isStench = stench;

        if (bump) {
            wasBumped();
        }

        if (!breeze && !stench) {
            currNode.isSafe = true;
        }

        updateLabyrinth();

        // add frontier by here
        if (currNode.isSafe) {
            MyNode left = labyrinth[currY][currX - 1];
            MyNode top = labyrinth[currY + 1][currX];
            MyNode right = labyrinth[currY][currX + 1];
            MyNode bottom = labyrinth[currY - 1][currX];

            if (left != currFrontier && !left.isVisited && frontiers.indexOf(left) == -1) {
                frontiers.add(left);
            }
            if (top != currFrontier && !top.isVisited && frontiers.indexOf(top) == -1) {
                frontiers.add(top);
            }
            if (right != currFrontier && !right.isVisited && frontiers.indexOf(right) == -1) {
                frontiers.add(right);
            }
            if (bottom != currFrontier && !bottom.isVisited && frontiers.indexOf(bottom) == -1) {
                frontiers.add(bottom);
            }
        }

        if (currActions.size() == 0) {
            updateActionsForFrontier();
        }

        int action = currActions.poll();
        if (action == Action.TURN_LEFT) {
            turnLeft();
        } else if (action == Action.TURN_RIGHT) {
            turnRight();
        } else if (action == Action.GO_FORWARD) {
            goForward();
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

    int worldToLocalPos(int pos) {
        return pos - MAX_LABYRINTH_SIZE;
    }

    int localToWorldPos(int pos) {
        return pos + MAX_LABYRINTH_SIZE;
    }

    void updateLabyrinth() {
        int offsetX[] = {-1, 0, 1, 0};
        int offsetY[] = {0, 1, 0, -1};
        for (int i = 1; i <= MAX_SAFE_LABYRINTH_SIZE - 2; i++) {
            for (int j = 1; j <= MAX_SAFE_LABYRINTH_SIZE - 2; j++) {
                boolean contradiction = false;
                MyNode node = labyrinth[i][j];

                for (int k1 = 0; k1 < offsetX.length; k1++) {
                    int offsetX1 = j + offsetX[k1];
                    int offsetY1 = i + offsetY[k1];

                    MyNode node1 = labyrinth[offsetY1][offsetX1];
                    for (int k2 = 0; k2 < offsetX.length; k2++) {
                        int offsetX2 = j + offsetX[k2];
                        int offsetY2 = i + offsetY[k2];
                        if (offsetX1 == offsetX2 && offsetY1 == offsetY2) {
                            continue;
                        }

                        MyNode node2 = labyrinth[offsetY2][offsetX2];

                        if (perceptionCross(node1, node2)) {
                            contradiction = true;
                        }
                    }
                }

                if (contradiction) {
                    node.isSafe = true;
                    if (!node.isVisited && frontiers.indexOf(node) == -1 && currFrontier != node) {
                        frontiers.add(node);
                    }
                }
            }
        }
    }

    boolean perceptionCross(MyNode lhs, MyNode rhs) {
        return (lhs.isBreeze && rhs.isStench) || (lhs.isStench && rhs.isBreeze);
    }

    void updateActionsForFrontier() {
        Assert.check(currActions.size() == 0);

        MyNode currNode = labyrinth[currY][currX];

        if (frontiers.size() > 0) {
            int i = rand.nextInt(frontiers.size());
            currFrontier = frontiers.get(i);
            frontiers.remove(i);
        } else {
            currFrontier = getDangerFrontier();
        }

        boolean visited[][] = new boolean[MAX_SAFE_LABYRINTH_SIZE][];
        for (int i = 0; i < MAX_SAFE_LABYRINTH_SIZE; i++) {
            visited[i] = new boolean[MAX_SAFE_LABYRINTH_SIZE];
        }

        visited[currY][currX] = true;

        HashMap<MyNode, MyNode> pred = new HashMap<>();
        pred.put(currNode, null);

        Queue<MyNode> queue = new ArrayDeque<>();
        queue.offer(currNode);

        int offsetX[] = {-1, 0, 1, 0};
        int offsetY[] = {0, 1, 0, -1};

        boolean pathExist = false;

        while (!queue.isEmpty()) {
            MyNode node = queue.poll();
            if (node == currFrontier) {
                pathExist = true;
                break;
            }

            for (int i = 0; i < offsetX.length; i++) {
                int nextX = localToWorldPos(node.x + offsetX[i]);
                int nextY = localToWorldPos(node.y + offsetY[i]);
                MyNode nextNode = labyrinth[nextY][nextX];
                if (!visited[nextY][nextX] && nextNode.isVisited) {
                    visited[nextY][nextX] = true;
                    queue.offer(nextNode);
                    pred.put(nextNode, node);
                } else {
                    if (nextNode == currFrontier) {
                        visited[nextY][nextX] = true;
                        queue.offer(nextNode);
                        pred.put(nextNode, node);
                    }
                }
            }
        }

        Assert.check(pathExist);

        MyNode predNode = pred.get(currFrontier);

        ArrayList<MyNode> onPathNodes = new ArrayList<>();
        onPathNodes.add(currFrontier);

        while(true) {
            onPathNodes.add(predNode);
            predNode = pred.get(predNode);
            if (predNode == null) {
                break;
            }
        }

        Assert.check(!onPathNodes.contains(null));

        Collections.reverse(onPathNodes);

        int prevDir = currDir;
        for (int i = 0; i < onPathNodes.size() - 1; i++) {
            MyNode a = onPathNodes.get(i);
            MyNode b = onPathNodes.get(i + 1);
            prevDir = addActions(a, b, prevDir);
        }
    }

    int addActions(MyNode curr, MyNode next, int prevDir) {
        if (curr.x + 1 == next.x && curr.y == next.y) {
            // left to right

            if (prevDir == DIR_LEFT) {
                currActions.offer(Action.TURN_LEFT);
                currActions.offer(Action.TURN_LEFT);
                currActions.offer(Action.GO_FORWARD);
            } else if (prevDir == DIR_TOP) {
                currActions.offer(Action.TURN_RIGHT);
                currActions.offer(Action.GO_FORWARD);
            } else if (prevDir == DIR_RIGHT) {
                currActions.offer(Action.GO_FORWARD);
            } else if (prevDir == DIR_BOTTOM) {
                currActions.offer(Action.TURN_LEFT);
                currActions.offer(Action.GO_FORWARD);
            }

            return DIR_RIGHT;
        } else if (curr.x - 1 == next.x && curr.y == next.y) {
            // right to left

            if (prevDir == DIR_LEFT) {
                currActions.offer(Action.GO_FORWARD);
            } else if (prevDir == DIR_TOP) {
                currActions.offer(Action.TURN_LEFT);
                currActions.offer(Action.GO_FORWARD);
            } else if (prevDir == DIR_RIGHT) {
                currActions.offer(Action.TURN_LEFT);
                currActions.offer(Action.TURN_LEFT);
                currActions.offer(Action.GO_FORWARD);
            } else if (prevDir == DIR_BOTTOM) {
                currActions.offer(Action.TURN_RIGHT);
                currActions.offer(Action.GO_FORWARD);
            }

            return DIR_LEFT;
        } else if (curr.y + 1 == next.y && curr.x == next.x) {
            // bottom to top

            if (prevDir == DIR_LEFT) {
                currActions.offer(Action.TURN_RIGHT);
                currActions.offer(Action.GO_FORWARD);
            } else if (prevDir == DIR_TOP) {
                currActions.offer(Action.GO_FORWARD);
            } else if (prevDir == DIR_RIGHT) {
                currActions.offer(Action.TURN_LEFT);
                currActions.offer(Action.GO_FORWARD);
            } else if (prevDir == DIR_BOTTOM) {
                currActions.offer(Action.TURN_LEFT);
                currActions.offer(Action.TURN_LEFT);
                currActions.offer(Action.GO_FORWARD);
            }

            return DIR_TOP;
        } else if (curr.y - 1 == next.y && curr.x == next.x) {
            // top to bottom

            if (prevDir == DIR_LEFT) {
                currActions.offer(Action.TURN_LEFT);
                currActions.offer(Action.GO_FORWARD);
            } else if (prevDir == DIR_TOP) {
                currActions.offer(Action.TURN_LEFT);
                currActions.offer(Action.TURN_LEFT);
                currActions.offer(Action.GO_FORWARD);
            } else if (prevDir == DIR_RIGHT) {
                currActions.offer(Action.TURN_RIGHT);
                currActions.offer(Action.GO_FORWARD);
            } else if (prevDir == DIR_BOTTOM) {
                currActions.offer(Action.GO_FORWARD);
            }

            return DIR_BOTTOM;
        }

        // error!
        Assert.check(false);
        return DIR_LEFT;
    }

    MyNode getDangerFrontier() {
        MyNode currNode = labyrinth[currY][currX];

        ArrayList<MyNode> maybeFrontiers = new ArrayList<>();

        int offsetX[] = {-1, 0, 1, 0};
        int offsetY[] = {0, 1, 0, -1};

        for (int i = 0; i < MAX_SAFE_LABYRINTH_SIZE; i++) {
            for (int j = 0; j < MAX_SAFE_LABYRINTH_SIZE; j++) {
                MyNode node = labyrinth[i][j];
                if (node.isVisited) {
                    continue;
                }

                boolean promise = false;
                for (int k = 0; k < offsetX.length; k++) {
                    int nextY = i + offsetY[k];
                    int nextX = j + offsetX[k];
                    if (nextY < 0 || nextY >= MAX_SAFE_LABYRINTH_SIZE) {
                        continue;
                    }
                    if (nextX < 0 || nextX >= MAX_SAFE_LABYRINTH_SIZE) {
                        continue;
                    }

                    if (labyrinth[nextY][nextX].isVisited) {
                        promise = true;
                    }
                }

                if (!promise) {
                    continue;
                }

                maybeFrontiers.add(node);
            }
        }

        return maybeFrontiers.get(rand.nextInt(maybeFrontiers.size()));
    }

    public void turnRight() {
        if (currDir == DIR_LEFT) {
            currDir = DIR_TOP;
        } else if (currDir == DIR_TOP) {
            currDir = DIR_RIGHT;
        } else if (currDir == DIR_RIGHT) {
            currDir = DIR_BOTTOM;
        } else if (currDir == DIR_BOTTOM) {
            currDir = DIR_LEFT;
        }
    }

    public void turnLeft() {
        if (currDir == DIR_LEFT) {
            currDir = DIR_BOTTOM;
        } else if (currDir == DIR_TOP) {
            currDir = DIR_LEFT;
        } else if (currDir == DIR_RIGHT) {
            currDir = DIR_TOP;
        } else if (currDir == DIR_BOTTOM) {
            currDir = DIR_RIGHT;
        }
    }

    public void goForward() {
        prevX = currX;
        prevY = currY;

        if (currDir == DIR_LEFT) {
            currX -= 1;
        } else if (currDir == DIR_TOP) {
            currY += 1;
        } else if (currDir == DIR_RIGHT) {
            currX += 1;
        } else if (currDir == DIR_BOTTOM) {
            currY -= 1;
        }
    }

    void wasBumped() {
        currX = prevX;
        currY = prevY;
    }

}