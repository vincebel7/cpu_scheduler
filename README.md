# CPU_Scheduler

Simulates some of the job-scheduling operations of an operating system. Output is a list of CPU operations including IO completion, system display, quantum expiration, and process termination. Includes two sample input lists of processes

p2stdin_a.txt contains a list of process arrivals and display requests

p2stdin_b.txt contains a list of process arrivals, IO arrivals, and display requests

external:

A - indicates process arrival

D - indicates display request

I - indicates IO arrival

internal:

C - indicates IO completion

E - indicates quantum expiration on processor

T - indicates successful process termination
