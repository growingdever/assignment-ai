/**
 * Created by loki on 2015. 11. 9..
 */
public class Node implements Comparable<Node> {

    public enum Direction {
        LEFT,
        TOP,
        RIGHT,
        BOTTOM;

        public static Direction determine(char c) {
            switch(c) {
                case 'A':
                    return TOP;
                case 'V':
                    return BOTTOM;
                case '<':
                    return LEFT;
                case '>':
                    return RIGHT;
            }

            return null;
        }
    }

    Node prev;
    int x, y;
    Direction dir;
    int f, g, h;
    boolean isWumpus;
    boolean isHaveArrow;

    public Node(int x, int y, Direction dir, int g) {
        this.x = x;
        this.y = y;
        this.dir = dir;
        this.g = g;
        this.isHaveArrow = true;
    }

    public boolean isSameLocation(Node node) {
        return this.x == node.x && this.y == node.y;
    }

    @Override
    public String toString() {
        return String.format("(%d, %d, %d)", x, y, g);
    }

    @Override
    public int compareTo(Node o) {
        return this.f - o.f;
    }

}
