/*******************************************************************************
/
/      filename:  Main.java
/
/   description:  Simulates some of the job and CPU scheduling of a
/                 time-shared operating system
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

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Main {
    static final int LEVEL1_QUANTUM = 100;
    static final int LEVEL2_QUANTUM = 300;
    static final int MAX_MEM = 512;

    static int time = 0;
    static int freeMem = MAX_MEM;
    static Node running_process = null; //process currently on CPU
    static int running_remaining;
    static Node currentIO_event = null;
    
    //Queues
    static Queue newJob = new Queue(); //Process control block
    static Queue scheduling = new Queue(); //Job Scheduling queue
    static Queue ready1 = new Queue(); //Level 1 RR queue
    static Queue ready2 = new Queue(); //Level 2 RR queue
    static Queue IOwait = new Queue();
    static Queue quantumExpire = new Queue(); //process to be expired next T
    static Queue completed = new Queue(); //completed processes queue
        
    public static void main(String[] args) throws FileNotFoundException {    
        String line;
        char event;
        int arrival, job, memory, runtime, IOburst;
            
        //Loads input file
        File input = new File("p2stdin_b.txt");
        Scanner inputScanner = new Scanner(input);
        
        //Scanner inputScanner = new Scanner(System.in);
        
        //Stores input file in process control block
        while(inputScanner.hasNextLine()){
            line = inputScanner.nextLine();
            
            //Tokenize
            StringTokenizer st = new StringTokenizer(line);
            event = st.nextToken().charAt(0);
            arrival = Integer.parseInt(st.nextToken());
            if(st.hasMoreTokens()){ //If event is A or I
                if(event == 'A'){
                    job = Integer.parseInt(st.nextToken());
                    memory = Integer.parseInt(st.nextToken());
                    runtime = Integer.parseInt(st.nextToken());
                    newJob.insert(event, arrival, job, memory, runtime);
                }
                else{
                    IOburst = Integer.parseInt(st.nextToken());
                    newJob.insert(event, arrival, IOburst);
                }
            }
            else newJob.insert(event, arrival); 
        }
        
        schedulingLoad();
        allProcessesCompleted();
    }
    
    //Handles the job scheduling queue and time incrementing
    public static void schedulingLoad(){
        boolean schedulingEmpty = false;
        boolean IO = false;
        Node current = newJob.remove();  
        Node finished_process = null;
        while(true){
            //Handle quantum expire and IO complete before external events
            quantumExpire(); //Handles quantum expirations before ext. events
            if(IOwait.getSize() > 0) IOwait();
            
            //Makes termination print at time after last cycle quantum
            if(finished_process != null){ 
                complete(finished_process);
                if(schedulingEmpty) break;
            }

            if(current.getArrival() == time){
                System.out.println("Event: " + current.getEvent() + 
                                   "   Time: " + time);
                if (current.getMemory() > MAX_MEM)
                    System.out.println("This job exceeds the system's " +
                                       "main memory capacity.");
                else{
                    switch(current.getEvent()){
                        case 'A': 
                            scheduling.insert(current);
                            break;
                        case 'D':
                            display();
                            break;
                        case 'I':
                            IO = true;
                            currentIO_event = current;
                            break;
                    }
                }
                
                if(newJob.getSize() != 0) current = newJob.remove();
                else schedulingEmpty = true;
            }
            
            ready1();
            
            finished_process = cpu(IO);
            IO = false;
            
            time++;
        } 
    }
    
    public static void ready1(){
        int memory;
        
        if(scheduling.getSize() != 0){
            if(scheduling.getEvent() == 'A')
                memory = scheduling.getMemory();
            else memory = 0;
            
            if(freeMem >= memory){
                Node current = scheduling.remove();
                ready1.insert(current);
                current.setReadyArrival(time);
                freeMem -= memory;
            }    
        }
    }
    
    public static Node cpu(boolean IO){
        if(IO){
            running_process.setIOparams(currentIO_event.getIOburst(), time);
            running_process.setWasInIO(true);
            IOwait.insert(running_process);
            running_process = null;
            //return null;    
        }
        
        if(running_process == null){ //Load new process onto CPU
            if(!ready1.isEmpty()){ //Loads from level 1 ready queue
                running_process = ready1.remove();
                
                if((!running_process.getWasInReady2()) && 
                        (!running_process.getWasInIO()))
                    running_process.setStart(time);
                
                running_process.setFromReady2(false);
                running_remaining = LEVEL1_QUANTUM;
            }
            
            else if(!ready2.isEmpty()){ //Loads from level 2 ready queue
                running_process = ready2.remove();
                running_process.setFromReady2(true);
                running_remaining = LEVEL2_QUANTUM;
            }
            
            //Nothing in either queue: Waiting for mem to free, or no proc left 
            else return null; 
        }
        else if((running_process.getFromReady2()) && (!ready1.isEmpty())){
            //quantum expire the process from ready2
            running_process.setWasInReady2(true);
            ready2.insert(running_process);
            running_process = null;
            
            //Loads from ready1
            running_process = ready1.remove();
            //running_process.setStart(time);
            running_process.setFromReady2(false);
            running_remaining = LEVEL1_QUANTUM;
            
        }
        
        running_remaining--;
        running_process.decRemaining();

        if(running_process.getRemaining() == 0){ //move to completed
            Node temp = running_process;
            running_process = null;
            return temp;
        } 

        else if(running_remaining == 0){ //quantum expire, move to ready2 
            quantumExpire.insert(running_process);
            ready2.insert(running_process);
            running_process = null;
        }
        return null;
    }
    
    public static void IOwait(){
        int IOwaitSize = IOwait.getSize();
        for(int i = 0; i < IOwaitSize; i++){
            Node current = IOwait.remove();
            if(current.getIOremaining() > 0)
                current.decIOremaining();
            
            if(current.getIOremaining() == 0){
                    ready1.insert(current);
                    System.out.println("Event: C   Time: " + time);
            }
            else IOwait.insert(current);
        }
    }
    
    public static void quantumExpire(){
        if(quantumExpire.getSize() != 0){
            Node current = quantumExpire.remove();
            System.out.println("Event: E   Time: " + time);
        }
    }
    
    public static void complete(Node n){
        freeMem += n.getMemory(); //free memory
        n.setCompleted(time);
        completed.insert(n);
        System.out.println("Event: T   Time: " + time);
    }
    
    public static void display(){
        System.out.println("\n*********************************************" +
                           "***************");
        System.out.println("\nThe status of the simulator at time " +
                           time + ".");
        int displayCount;
        
        //JOB SCHEDULING QUEUE
            System.out.println("\nThe contents of the JOB SCHEDULING QUEUE");
            System.out.println("----------------------------------------\n");
            displayCount = scheduling.getSize();
            if(displayCount == 0)
                System.out.println("The Job Scheduling Queue is empty.");
            else{
                System.out.println("Job #  Arr. Time  Mem. Req.  Run Time");
                System.out.println("-----  ---------  ---------  --------\n");
                for(int i = 0; i < displayCount; i++){
                    Node current = scheduling.remove();
                    System.out.format("%5d", current.getJob());
                    System.out.format("%11d", current.getArrival());
                    System.out.format("%11d", current.getMemory());
                    System.out.format("%10d", current.getRuntime());
                    System.out.println();
                    scheduling.insert(current);
                }
            }
            
        //FIRST LEVEL READY QUEUE
            System.out.println("\n\nThe contents of the " +
                               "FIRST LEVEL READY QUEUE");
            System.out.println("-------------------------------------------\n");
            displayCount = ready1.getSize();
            if(displayCount == 0)
                System.out.println("The First Level Ready Queue is empty.");
            else{
                System.out.println("Job #  Arr. Time  Mem. Req.  Run Time");
                System.out.println("-----  ---------  ---------  --------\n");
                for(int i = 0; i < displayCount; i++){
                    Node current = ready1.remove();
                    System.out.format("%5d", current.getJob());
                    System.out.format("%11d", current.getArrival());
                    System.out.format("%11d", current.getMemory());
                    System.out.format("%10d", current.getRuntime());
                    System.out.println();
                    ready1.insert(current);
                }
            }
        
        //SECOND LEVEL READY QUEUE
            System.out.println("\n\nThe contents of the " +
                               "SECOND LEVEL READY QUEUE");
            System.out.println("------------------------" +
                               "--------------------\n");
            displayCount = ready2.getSize();
            if(displayCount == 0)
                System.out.println("The Second Level Ready Queue is empty.");
            else{
                System.out.println("Job #  Arr. Time  Mem. Req.  Run Time");
                System.out.println("-----  ---------  ---------  --------\n");
                for(int i = 0; i < displayCount; i++){
                    Node current = ready2.remove();
                    System.out.format("%5d", current.getJob());
                    System.out.format("%11d", current.getArrival());
                    System.out.format("%11d", current.getMemory());
                    System.out.format("%10d", current.getRuntime());
                    System.out.println();
                    ready2.insert(current);
                }
            }

        //IO WAIT QUEUE
            System.out.println("\n\nThe contents of the I/O WAIT QUEUE");
            System.out.println("----------------------------------\n");
            displayCount = IOwait.getSize();
            if(displayCount == 0)
                System.out.println("The I/O Wait Queue is empty.");
            else{
                System.out.println("Job #  Arr. Time  Mem. Req.  Run Time  " + 
                                   "IO Start Time  IO Burst  Comp. Time");
                System.out.println("-----  ---------  ---------  --------  " +
                                   "-------------  --------  ----------\n");
                for(int i = 0; i < displayCount; i++){
                    Node current = IOwait.remove();
                    System.out.format("%5d", current.getJob());
                    System.out.format("%11d", current.getArrival());
                    System.out.format("%11d", current.getMemory());
                    System.out.format("%10d", current.getRuntime());
                    System.out.format("%10d", current.getIOarrival());
                    System.out.format("%10d", current.getIOburst());
                    System.out.format("%12d", (current.getIOarrival() + 
                                      current.getIOburst()));
                    System.out.println();
                    IOwait.insert(current);
                }
            }

        //CPU
        System.out.println("\n\nThe CPU  Start Time  CPU burst time left");
        System.out.println("-------  ----------  -------------------\n");
            if(running_process != null){
                System.out.format("%7d", running_process.getJob());
                System.out.format("%12d", running_process.getStart());
                System.out.format("%21d", running_process.getRemaining());
            }
            else System.out.println("The CPU is idle.");
         
        //FINISHED QUEUE
        System.out.println("\n\nThe contents of the FINISHED LIST");
        System.out.println("---------------------------------\n");
        System.out.println("Job #  Arr. Time  Mem. Req.  Run Time  " +
                           "Start Time  Com. Time");
        System.out.println("-----  ---------  ---------  --------  " +
                           "----------  ---------\n");
        displayCount = completed.getSize();
        if(displayCount == 0)
            System.out.println("The I/O Wait Queue is empty.");   
        else{
            for(int i = 0; i < displayCount; i++){
                Node current = completed.remove();
                System.out.format("%5d", current.getJob());
                System.out.format("%11d", current.getArrival());
                System.out.format("%11d", current.getMemory());
                System.out.format("%10d", current.getRuntime()); 
                System.out.format("%12d", current.getStart());
                System.out.format("%11d", current.getCompleted());
                System.out.println();
                completed.insert(current);
            }
        }
        System.out.println("\n\nThere are " + freeMem + " blocks of main " +
                           "memory available in the system.\n");
    }
    
    public static void allProcessesCompleted(){
        int turnaroundSum = 0;
        int waitSum = 0;
        
        float turnaroundAvg = 0;
        float waitAvg = 0;
        float processCount = completed.getSize(); //should be 93
        
        System.out.println("\nThe contents of the FINAL FINISHED LIST");
        System.out.println("---------------------------------------\n");
        System.out.println("Job #  Arr. Time  Mem. Req.  Run Time  " +
                           "Start Time  Com. Time");
        System.out.println("-----  ---------  ---------  --------  " +
                           "----------  ---------\n");     
        
        for(int i = 0; i < processCount; i++){
            Node current = completed.remove();
            System.out.format("%5d", current.getJob());
            System.out.format("%11d", current.getArrival());
            System.out.format("%11d", current.getMemory());
            System.out.format("%10d", current.getRuntime()); 
            System.out.format("%12d", current.getStart());
            System.out.format("%11d", current.getCompleted());
            System.out.println();
            turnaroundSum += (current.getCompleted() - current.getArrival());
            waitSum += (current.getReadyArrival() - current.getArrival());
        }
        
        DecimalFormat df = new DecimalFormat("#.000");
        turnaroundAvg = turnaroundSum / processCount;
        waitAvg = (waitSum) / processCount; //incorrect unless (waitsum - 179)

        System.out.println("\n\nThe Average Turnaround Time for the " +
                      "simulation was " + df.format(turnaroundAvg) + " units.");
        System.out.println("\nThe Average Job Scheduling Wait Time for the " + 
                           "simulation was " + df.format(waitAvg) + " units.");
        System.out.println("\nThere are " + freeMem + " blocks of main " +
                           "memory available in the system.");
    }  
}