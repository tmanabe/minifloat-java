import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

public class Collector {
    static class Element implements Comparable<Element> {
        float priority;
        int id;

        Element(float priority, int id) {
            this.priority = priority;
            this.id = id;
        }

        @Override
        public int compareTo(Element element) {
            return Float.compare(priority, element.priority);
        }
    }

    int capacity;
    PriorityQueue<Element> priorityQueue;

    Collector(int capacity) {
        assert 0 < capacity;
        this.capacity = capacity;
        priorityQueue = new PriorityQueue<>(capacity);
    }

    void collect(float priority, int id) {
        if (priorityQueue.size() < capacity) {
            priorityQueue.add(new Element(priority, id));
        } else {
            if (priorityQueue.peek().priority < priority) {
                Element element = priorityQueue.poll();
                element.priority = priority;
                element.id = id;
                priorityQueue.add(element);
            }
        }
    }

    Set<Integer> top() {
        Set<Integer> results = new HashSet<>();
        while(priorityQueue.peek() != null) {
            results.add(priorityQueue.poll().id);
        }
        return results;
    }
}
