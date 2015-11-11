import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;

/**
 * Created by loki on 15. 11. 9..
 */
public class AStar {

    public static void pathFinding(Environment wumpusEnvironment, ArrayList<Integer> stateSeq, ArrayList<Integer[]> path) {
        int worldSize = wumpusEnvironment.getWorldSize();
        char[][][] world = wumpusEnvironment.getWumpusWorld();

        Node startNode = new Node(0, 0, Node.Direction.LEFT, 0);
        Node endNode = new Node(0, 0, Node.Direction.LEFT, 0);

        findStartLocation(world, worldSize, startNode);
        findGoalLocation(world, worldSize, endNode);

        Node last = start(world, worldSize, startNode, endNode);
        ArrayList<Node> nodesOnPath = getPath(last);
        fillStateSequence(nodesOnPath, stateSeq);
        fillPathSequence(nodesOnPath, path);
    }

    static void findStartLocation(char[][][] world, int worldSize, Node node) {
        for (int i = 0; i < worldSize; i ++) {
            for (int j = 0; j < worldSize; j ++) {
                char agent = world[i][j][3];
                if (agent == 'A' || agent == 'V' || agent == '<' || agent == '>') {
                    node.x = j;
                    node.y = i;
                    node.g = 0;
                    node.dir = Node.Direction.determine(agent);
                }
            }
        }
    }

    static void findGoalLocation(char[][][] world, int worldSize, Node node) {
        for (int i = 0; i < worldSize; i ++) {
            for (int j = 0; j < worldSize; j ++) {
                if (world[i][j][2] == 'G') {
                    node.x = j;
                    node.y = i;
                }
            }
        }
    }

    static Node start(char[][][] world, int worldSize, Node startNode, Node endNode) {
        PriorityQueue<Node> frontiersPriorityQueue = new PriorityQueue<>();
        frontiersPriorityQueue.add(startNode);

        int currOptimal = Integer.MAX_VALUE;
        Node optimalNode = null;

        int[][] optimals = new int[worldSize + 1][worldSize + 1];
        for (int i = 0; i <= worldSize; i ++) {
            for (int j = 0; j <= worldSize; j ++) {
                optimals[i][j] = Integer.MAX_VALUE;
            }
        }

        optimals[startNode.y][startNode.x] = 0;

        while (frontiersPriorityQueue.size() > 0) {
            Node frontier = frontiersPriorityQueue.poll();

            if (endNode.isSameLocation(frontier)) {
                if (optimalNode == null) {
                    optimalNode = frontier;
                }

                if (currOptimal > optimalNode.g) {
                    currOptimal = optimalNode.g;
                    optimalNode = frontier;
                }

                continue;
            }

            // add neighbors of frontier
            Node[] neighbors = new Node[4];
            setNeighbors(frontier, neighbors);
            addNeighborsToFrontierQueue(neighbors, frontiersPriorityQueue,
                    frontier, endNode, world, worldSize, optimals);

            // add forward nodes of frontier
            addForwardNodes(neighbors, frontiersPriorityQueue,
                    frontier, endNode, world, worldSize, optimals);
        }

        return optimalNode;
    }

    static void setNeighbors(Node curr, Node[] nextNodes) {
        if (curr.dir == Node.Direction.LEFT) {
            // forward
            nextNodes[0] = new Node(curr.x - 1, curr.y, Node.Direction.LEFT, curr.g + 1);

            // left
            nextNodes[1] = new Node(curr.x, curr.y - 1, Node.Direction.BOTTOM, curr.g + 2);

            // right
            nextNodes[2] = new Node(curr.x, curr.y + 1, Node.Direction.TOP, curr.g + 2);

            // back
            nextNodes[3] = new Node(curr.x + 1, curr.y, Node.Direction.RIGHT, curr.g + 3);
        } else if (curr.dir == Node.Direction.TOP) {
            // forward
            nextNodes[0] = new Node(curr.x, curr.y + 1, Node.Direction.TOP, curr.g + 1);

            // left
            nextNodes[1] = new Node(curr.x - 1, curr.y, Node.Direction.LEFT, curr.g + 2);

            // right
            nextNodes[2] = new Node(curr.x + 1, curr.y, Node.Direction.RIGHT, curr.g + 2);

            // back
            nextNodes[3] = new Node(curr.x, curr.y - 1, Node.Direction.BOTTOM, curr.g + 3);
        } else if (curr.dir == Node.Direction.RIGHT) {
            // forward
            nextNodes[0] = new Node(curr.x + 1, curr.y, Node.Direction.RIGHT, curr.g + 1);

            // left
            nextNodes[1] = new Node(curr.x, curr.y + 1, Node.Direction.TOP, curr.g + 2);

            // right
            nextNodes[2] = new Node(curr.x, curr.y - 1, Node.Direction.BOTTOM, curr.g + 2);

            // back
            nextNodes[3] = new Node(curr.x - 1, curr.y, Node.Direction.LEFT, curr.g + 3);
        } else if (curr.dir == Node.Direction.BOTTOM) {
            // forward
            nextNodes[0] = new Node(curr.x, curr.y - 1, Node.Direction.BOTTOM, curr.g + 1);

            // left
            nextNodes[1] = new Node(curr.x + 1, curr.y, Node.Direction.RIGHT, curr.g + 2);

            // right
            nextNodes[2] = new Node(curr.x - 1, curr.y, Node.Direction.LEFT, curr.g + 2);

            // back
            nextNodes[3] = new Node(curr.x, curr.y + 1, Node.Direction.TOP, curr.g + 3);
        }
    }

    static void addNeighborsToFrontierQueue(Node[] neighbors, PriorityQueue<Node> pq,
                                            Node frontier, Node endNode,
                                            char[][][] world, int worldSize, int[][] optimals) {
        for (Node node : neighbors) {
            if (outOfWorld(node, world, worldSize)) {
                continue;
            }

            if (world[node.y][node.x][1] == 'W') {
                node.isWumpus = true;
            }

            node.isHaveArrow = frontier.isHaveArrow;
            node.h = heuristic(endNode, node);
            node.f = node.g + node.h;

            if (optimals[node.y][node.x] <= node.f) {
                continue;
            }

            if (validNode(node, world, worldSize)) {
                pq.add(node);
                node.prev = frontier;

                optimals[frontier.y][frontier.x] = frontier.f;
            } else {
                if (frontier.isHaveArrow) {
                    node.g += 2;
                    node.f += 2;
                    node.isHaveArrow = false;
                    pq.add(node);
                    node.prev = frontier;

                    optimals[frontier.y][frontier.x] = frontier.f;
                }
            }
        }
    }

    static void addForwardNodes(Node[] neighbors, PriorityQueue<Node> pq,
                                Node frontier, Node endNode, char[][][] world, int worldSize, int[][] optimals) {
        int offset = 2;
        Node prev = null;
        for (int i = 0; i < neighbors.length; i ++) {
            if (frontier.dir == neighbors[i].dir) {
                prev = neighbors[i];
                break;
            }
        }

        while(true) {
            Node forwardNode = new Node(frontier.x, frontier.y, frontier.dir, frontier.g + offset);
            switch (frontier.dir) {
                case LEFT:
                    forwardNode.x -= offset;
                    break;
                case RIGHT:
                    forwardNode.x += offset;
                    break;
                case TOP:
                    forwardNode.y += offset;
                    break;
                case BOTTOM:
                    forwardNode.y -= offset;
                    break;
            }

            if (outOfWorld(forwardNode, world, worldSize)) {
                break;
            }

            if (!validNode(forwardNode, world, worldSize)) {
                break;
            }

            forwardNode.isHaveArrow = frontier.isHaveArrow;
            forwardNode.h = heuristic(endNode, forwardNode);
            forwardNode.f = forwardNode.g + forwardNode.h;

            if (optimals[forwardNode.y][forwardNode.x] <= forwardNode.f) {
                break;
            }

            forwardNode.prev = prev;
            prev = forwardNode;

            pq.add(forwardNode);
            optimals[forwardNode.y][forwardNode.x] = forwardNode.f;

            offset ++;
        }
    }

    static boolean outOfWorld(Node node, char[][][] world, int worldSize) {
        return node.x < 0 || node.x >= worldSize
                || node.y < 0 || node.y >= worldSize;
    }

    static boolean validNode(Node node, char[][][] world, int worldSize) {
        char[] percepts = world[node.y][node.x];
        return !(percepts[0] == 'P') && !(percepts[1] == 'W');
    }

    static int heuristic(Node endNode, Node node) {
        int diffX = endNode.x - node.x;
        int diffY = endNode.y - node.y;
//        return (int)Math.sqrt(diffX * diffX + diffY * diffY);
        return Math.abs(diffX) + Math.abs(diffY);
    }

    static ArrayList<Node> getPath(Node last) {
        ArrayList<Node> nodesOnPath = new ArrayList<>();
        while (last != null) {
            nodesOnPath.add(last);
            last = last.prev;
        }

        Collections.reverse(nodesOnPath);

        if (nodesOnPath.size() > 10) {
            System.out.println("...");
            for (int i = nodesOnPath.size() - 10; i < nodesOnPath.size(); i ++) {
                System.out.println(nodesOnPath.get(i));
            }
        } else {
            for (Node node : nodesOnPath) {
                System.out.println(node);
            }
        }

        return nodesOnPath;
    }

    static void fillStateSequence(ArrayList<Node> nodesOnPath, ArrayList<Integer> stateSequence) {
        stateSequence.add(Action.START_TRIAL);

        for (int i = 0; i < nodesOnPath.size() - 1; i ++) {
            Node curr = nodesOnPath.get(i);
            Node next = nodesOnPath.get(i + 1);

            if (curr.dir == Node.Direction.LEFT) {
                if (curr.x - 1 == next.x) {

                    if (next.isWumpus) {
                        stateSequence.add(Action.SHOOT);
                    }

                    stateSequence.add(Action.GO_FORWARD);
                } else if (curr.y - 1 == next.y) {
                    stateSequence.add(Action.TURN_LEFT);

                    if (next.isWumpus) {
                        stateSequence.add(Action.SHOOT);
                    }

                    stateSequence.add(Action.GO_FORWARD);
                } else if (curr.y + 1 == next.y) {
                    stateSequence.add(Action.TURN_RIGHT);

                    if (next.isWumpus) {
                        stateSequence.add(Action.SHOOT);
                    }

                    stateSequence.add(Action.GO_FORWARD);
                } else if (curr.x + 1 == next.x) {
                    stateSequence.add(Action.TURN_RIGHT);
                    stateSequence.add(Action.TURN_RIGHT);

                    if (next.isWumpus) {
                        stateSequence.add(Action.SHOOT);
                    }

                    stateSequence.add(Action.GO_FORWARD);
                }
            } else if (curr.dir == Node.Direction.TOP) {
                if (curr.y + 1 == next.y) {

                    if (next.isWumpus) {
                        stateSequence.add(Action.SHOOT);
                    }

                    stateSequence.add(Action.GO_FORWARD);
                } else if (curr.x - 1 == next.x) {
                    stateSequence.add(Action.TURN_LEFT);

                    if (next.isWumpus) {
                        stateSequence.add(Action.SHOOT);
                    }

                    stateSequence.add(Action.GO_FORWARD);
                } else if (curr.x + 1 == next.x) {
                    stateSequence.add(Action.TURN_RIGHT);

                    if (next.isWumpus) {
                        stateSequence.add(Action.SHOOT);
                    }

                    stateSequence.add(Action.GO_FORWARD);
                } else if (curr.y - 1 == next.y) {
                    stateSequence.add(Action.TURN_RIGHT);
                    stateSequence.add(Action.TURN_RIGHT);

                    if (next.isWumpus) {
                        stateSequence.add(Action.SHOOT);
                    }

                    stateSequence.add(Action.GO_FORWARD);
                }
            } else if (curr.dir == Node.Direction.RIGHT) {
                if (curr.x + 1 == next.x) {

                    if (next.isWumpus) {
                        stateSequence.add(Action.SHOOT);
                    }

                    stateSequence.add(Action.GO_FORWARD);
                } else if (curr.y - 1 == next.y) {
                    stateSequence.add(Action.TURN_RIGHT);

                    if (next.isWumpus) {
                        stateSequence.add(Action.SHOOT);
                    }

                    stateSequence.add(Action.GO_FORWARD);
                } else if (curr.y + 1 == next.y) {
                    stateSequence.add(Action.TURN_LEFT);

                    if (next.isWumpus) {
                        stateSequence.add(Action.SHOOT);
                    }

                    stateSequence.add(Action.GO_FORWARD);
                } else if (curr.x - 1 == next.x) {
                    stateSequence.add(Action.TURN_RIGHT);
                    stateSequence.add(Action.TURN_RIGHT);

                    if (next.isWumpus) {
                        stateSequence.add(Action.SHOOT);
                    }

                    stateSequence.add(Action.GO_FORWARD);
                }
            } else if (curr.dir == Node.Direction.BOTTOM) {
                if (curr.y - 1 == next.y) {

                    if (next.isWumpus) {
                        stateSequence.add(Action.SHOOT);
                    }

                    stateSequence.add(Action.GO_FORWARD);
                } else if (curr.x - 1 == next.x) {
                    stateSequence.add(Action.TURN_RIGHT);

                    if (next.isWumpus) {
                        stateSequence.add(Action.SHOOT);
                    }

                    stateSequence.add(Action.GO_FORWARD);
                } else if (curr.x + 1 == next.x) {
                    stateSequence.add(Action.TURN_LEFT);

                    if (next.isWumpus) {
                        stateSequence.add(Action.SHOOT);
                    }

                    stateSequence.add(Action.GO_FORWARD);
                } else if (curr.y + 1 == next.y) {
                    stateSequence.add(Action.TURN_RIGHT);
                    stateSequence.add(Action.TURN_RIGHT);

                    if (next.isWumpus) {
                        stateSequence.add(Action.SHOOT);
                    }

                    stateSequence.add(Action.GO_FORWARD);
                }
            }
        }

        stateSequence.add(Action.GRAB);
        stateSequence.add(Action.END_TRIAL);
    }

    static void fillPathSequence(ArrayList<Node> nodesOnPath, ArrayList<Integer[]> path) {
        for (Node node : nodesOnPath) {
            Integer[] location = new Integer[2];
            location[0] = node.y;
            location[1] = node.x;
            path.add(location);
        }
    }

}
