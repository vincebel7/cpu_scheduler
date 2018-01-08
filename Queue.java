/*******************************************************************************
/
/      filename:  Queue.java
/
/   description:  Implementing a queue with a linked list. I found it easier to
/                 rewrite this than to modify the Java implementation
/        author:  Belanger, Vince
/      login id:  FA_17_CPS356_24
/
/         class:  CPS 356
/    instructor:  Perugini
/    assignment:  Midterm Project
/
/      assigned:  October 12, 2017
/           due:  October 26, 2017
/
/******************************************************************************/
package pkg356_scheduler;

public class Queue{
    Node front, back;
    public int size;

    public Queue(){
        front = null;
        back = null;
        size = 0;
    }

    public void insert(char event, int arrival){ //insert node for D
        Node newNode = new Node(event, arrival);
        if(back == null){
            back = newNode;
            front = newNode;
        }
        else{
            back.setNext(newNode);
            back = back.getNext();
        }
        size++;
    }
    
    public void insert(char event, int arrival, int IOburst){//insert node for I
        Node newNode = new Node(event, arrival, IOburst);
        if(back == null){
            back = newNode;
            front = newNode;
        }
        else{
            back.setNext(newNode);
            back = back.getNext();
        }
        size++;
    }
    
    public void insert(char event, int arrival, int job, int memory,
                       int runtime){ //insert node for A
        Node newNode = new Node(event, arrival, job, memory, runtime);
        if(size == 0){
            back = newNode;
            front = newNode;
        }
        else{
            back.setNext(newNode);
            back = back.getNext();
        }
        size++;
    }
    
    public void insert(Node n){ //insert node for D
        if(back == null){
            back = n;
            front = n;
        }
        else{
            back.setNext(n);
            back = back.getNext();
        }
        size++;
    }
    
    public Node remove(){
        Node n = front;
        if(size == 1) n.setNext(null);
        front = n.getNext();
        if(front == null) back = null;
        size--;
        return n;
    }
    
    public char getEvent(){ return front.getEvent(); }
    
    public int getMemory(){ return front.getMemory(); }
    
    public int getSize(){ return size; }
    
    public boolean isEmpty(){ return (front == null); }
}