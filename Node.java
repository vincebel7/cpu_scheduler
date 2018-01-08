/*******************************************************************************
/
/      filename:  Node.java
/
/   description:  Object class for Node
/
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

public class Node {
    char event;
    boolean fromReady2 = false;
    boolean wasInReady2 = false;
    boolean wasInIO = false;
    int arrival, job, memory, runtime, remaining, 
               readyArrival, start, completed, IOarrival, IOburst, IOremaining;

    public Node next;
    
    public Node(char event, int arrival){ //Constructor for D, I
        this.event = event;
        this.arrival = arrival;
        this.next = null;
    }

    //Constructor for I
    public Node(char event, int arrival, int IOburst){ 
        this.event = event;
        this.arrival = arrival;
        this.IOburst = IOburst;
        this.next = null;
    }
    
    //Constructor for A
    public Node(char event, int arrival, int job, int memory, int runtime){ 
        this.event = event;
        this.arrival = arrival;
        this.job = job;
        this.memory = memory;
        this.runtime = runtime;
        this.remaining = runtime;
        this.next = null;
    }
    
    //setters
    public void decRemaining(){ this.remaining--; } //decrements remaining time
    
    public void setReadyArrival(int readyArrival){ 
        this.readyArrival = readyArrival; }
    
    public void setIOparams(int IOremaining, int IOarrival){
        this.IOarrival = IOarrival;
        this.IOburst = IOremaining;
        this.IOremaining = IOremaining; }
    
    public void decIOremaining(){ this.IOremaining--; }
    
    public void setFromReady2(boolean fromReady2) { 
        this.fromReady2 = fromReady2; }
    
    public void setWasInReady2(boolean wasInReady2){ 
        this.wasInReady2 = wasInReady2; }
    
    public void setWasInIO(boolean wasInIO){ this.wasInIO = wasInIO; }
    
    public void setStart(int start) { this.start = start; }
    
    public void setCompleted(int completed){ this.completed = completed; }
    
    public void setNext(Node next){ this.next = next; }
    
    //getters
    public boolean getFromReady2() { return fromReady2; }
    
    public boolean getWasInReady2() { return wasInReady2; }
    
    public boolean getWasInIO(){ return wasInIO; }
    
    public char getEvent(){ return event; }

    public int getArrival(){ return arrival; }
    
    public int getIOarrival(){ return IOarrival; }
    
    public int getIOburst(){ return IOburst; }
    
    public int getIOremaining(){ return IOremaining; }

    public int getJob(){ return job; }

    public int getMemory(){ return memory; }

    public int getRuntime(){ return runtime; }
    
    //remaining is the number of timeslices remaining to finish on CPU
    public int getRemaining(){ return remaining; }
    
    //readyArrival is the time a process enters the ready1 queue
    public int getReadyArrival(){ return readyArrival; }
    
    //start is the time a process first goes on the CPU
    public int getStart(){ return start; }
    
    //completed is the time a process finishes on the CPU
    public int getCompleted(){ return completed; }
    
    public Node getNext(){ return next; }    
}