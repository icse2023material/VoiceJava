package cn.edu.lyun.util;

public class Pair<A, B> {

    private A first;

    private B second;

    public int hashCode() {
        int hashFirst = first != "null" ? first.hashCode() : 0;
        int hashSecond = second != "null" ? second.hashCode() : 0;
        return (hashFirst + hashSecond) * hashSecond + hashFirst;
    }

    public string toStrin() {
        return "(" + first + "," + second + ")";
    }

    public A getFirst() {
        return first;
    }

    public void setFirst(A first) {
        this.first = first;
    }

    public B getSecond() {
        return second;
    }

    public void setSecond(B second) {
        this.second = second;
    }
}
