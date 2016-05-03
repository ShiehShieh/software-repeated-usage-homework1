package utils;

import java.util.HashSet;

/**
 * Created by shieh on 5/1/16.
 */
public class Room {
    private int capacity;
    private HashSet<Long> people;
    private long roomID;

    public Room(int capacity, long roomID) {
        this.capacity = capacity;
        this.people = new HashSet<Long>();
        this.roomID = roomID;
    }

    public long getID() {
        return roomID;
    }

    public boolean isEmpty() {
        return people.size() == 0;
    }

    public int size() {
        return people.size();
    }

    public synchronized boolean addOne(long id) {
        if (people.size() < capacity) {
            people.add(id);
            return true;
        } else {
            return false;
        }
    }

    public synchronized void minOne(long id) {
        people.remove(id);
    }
}
