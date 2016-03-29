package utils;

/**
 * Created by huangli on 3/24/16.
 */
public class Pair<First,Second> {
    private First first;
    private Second second;
    public Pair() {}
    public Pair(First l, Second r){
        this.first = l;
        this.second = r;
    }
    public First getL(){ return first; }
    public Second getR(){ return second; }
    public void setL(First l){ this.first = l; }
    public void setR(Second r){ this.second = r; }
}
