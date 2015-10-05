/**
 * Created by loki on 2015. 10. 5..
 */
public class MyNode {
    public int x, y;

    public boolean isVisited;

    public boolean isSafe;

    public boolean isPit;
    public boolean isBreeze;

    public boolean isWumpus;
    public boolean isStench;

    public boolean isBump;

    public MyNode() {

    }

    public MyNode(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
