/*
REFACTORING NOTES:
-separate inner classes
-create pojo for global variables to reside in
*/

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CyclicBarrier;
import java.lang.InterruptedException;
import java.lang.IllegalArgumentException;
import java.util.concurrent.BrokenBarrierException;

public class HorseRacingUtility{

    static List<RaceHorse> raceTrack = new ArrayList<RaceHorse>();

    static List<RaceHorse> finishers = new LinkedList<RaceHorse>();
    
    static List<Thread> threads = new ArrayList<Thread>();
    
    static CyclicBarrier barrierGate;
    
    static int distance;

    static IOValidator io = new IOValidator();		

    final static int gateToFinishDistance = (int)((Math.random()*70)+100);	

    final static int barnToGateDistance   = (int)((Math.random()*100)+50);	

    static boolean isAllDead = false;
    
    static boolean isRace = false;
    
    static final int MAX_INT = -1 >>> 1;

    static final Comparator<RaceHorse> nameCompare = (h1, h2) -> h1.getName().compareTo(h2.getName());

    static final Comparator<RaceHorse> timeCompare = (h1, h2) -> (int)(h1.getElapsedNs()-h2.getElapsedNs());

    static Map<RaceHorse, ArrayList<LinkedHashMap<Integer, Boolean>>> log = new LinkedHashMap<RaceHorse, ArrayList<LinkedHashMap<Integer, Boolean>>>();

    static {

        int temp = 0;

        while(temp<2){

            temp = io.inInt("Please input number of horses you want to race: ");
        
        }

        for(int i=1; i<temp+1; i++){

            //raceTrack.add( new RaceHorse(io.inStr("Please enter your ["+i+"] horse's name: "), io.inStr("Please enter your ["+i+"] horse's battle cry: ")));
            raceTrack.add( new RaceHorse("Horse["+i+"]", "Horse["+i+"] wins!"));

        }

        System.out.println("TOTAL HORSES SPAWNED: "+raceTrack.size());

        raceTrack = Collections.synchronizedList(raceTrack);

        finishers = Collections.synchronizedList(finishers);

        Collections.sort(raceTrack, nameCompare);

        raceTrack.stream().forEach((horse) -> {

            log.put(horse, new ArrayList<LinkedHashMap<Integer, Boolean>>())	;

        });

    }


    final class RaceHorseStrutter implements Runnable {

        RaceHorse horse;                

        RaceHorseStrutter(RaceHorse horse){

            this.horse = horse;
            
        }

        public boolean isLast(){

            synchronized(raceTrack){

                return raceTrack
                .stream()
                .filter((rhorse) -> rhorse!=horse && rhorse.getPosition()!=horse.getPosition())
                .allMatch((rhorse) -> horse.getPosition()<rhorse.getPosition());
                
            }	

        }

        public void run(){                        
            
            while(isRace == false){                
                
                long time_start = System.nanoTime();			
                
			    horse.setStartingNs(time_start);                    

                while(horse.getPosition() < distance){

                    System.out.print(".");

                    int remaining_distance = distance-horse.getPosition();

                    log.get(horse).add(new LinkedHashMap<Integer, Boolean>(){{  //log entry

                        put(horse.getPosition(), false);

                    }});					

                    if(remaining_distance <= horse.getMaxSpeed()){      //determines which horse.strut method to use

                        horse.preciseStrut(distance-horse.getPosition());

                    }else if(this.isLast()==true && isRace == true && horse.getPosition()!=0){											

                        int boostByRandMaxTwenty = (int)((Math.random()*20)+1);

                        while(boostByRandMaxTwenty >= remaining_distance){

                            boostByRandMaxTwenty = (int)((Math.random()*20)+1);

                        }

                        horse.preciseStrut(boostByRandMaxTwenty);

                    }else if(horse.getPosition()<distance){

                        horse.strut();

                    }

                    if(horse.getPosition()>=distance){ //distance reached

                        System.out.println();                        

                        synchronized(finishers){

                            finishers.add(horse);

                        }

                        long time_end = System.nanoTime();

                        horse.setElapsedNs(time_end - time_start);

                        log.get(horse).add(new LinkedHashMap<Integer, Boolean>(){{

                            put(horse.getPosition(), false);

                        }});                    
                            
                        if(isRace == false){
                            
                            horse.resetRacingState();

                            try {

                                barrierGate.await();

                            } catch (BrokenBarrierException | InterruptedException ex) {

                                System.out.println("Barrier error");
                                
                            }
                            
                        }    

                    }															

                    try {

                        Thread.sleep(100);

                    } catch (InterruptedException e) {

                        Thread.currentThread().interrupt();

                    }			

                }
                
                System.out.println(this.isLast() == true ? horse.getName()+" thread done\n\n" : horse.getName()+" thread done");
                
            }    
            
        }
    }

    public int getMaxSizeOfLogs(){

        int max=0;

        Set<Map.Entry<RaceHorse, ArrayList<LinkedHashMap<Integer, Boolean>>>> logEntrySet = log.entrySet();

        for( Map.Entry<RaceHorse, ArrayList<LinkedHashMap<Integer, Boolean>>> horseLog : logEntrySet){

            if(horseLog.getValue().size()>max){

                max = horseLog.getValue().size();

            }

        }

        return max;

    }

    public void printLog(int distance){

        if(isRace == true) {

            this.predictBoosts();

        }

        for(int i=0; i<this.getMaxSizeOfLogs(); i++){

            for(RaceHorse horse : raceTrack){

                try{

                Set<Map.Entry<Integer, Boolean>> logListEntrySet = log.get(horse).get(i).entrySet();

                for(Map.Entry<Integer, Boolean> logEntry : logListEntrySet){

                    if(horse == finishers.get(0) && isRace == true && i==log.get(horse).size()-1){ //battlecry

                        System.out.print(logEntry.getValue() == true 
                        ? horse.getName()+" "+logEntry.getKey()+"/"+distance+" [BST]: "+horse.getBattleCry()+"\t\t" 
                        :  horse.getName()+" "+logEntry.getKey()+"/"+distance+" [NRM]: "+horse.getBattleCry()+"\t\t");
                        
                        
                        
                    }else{

                        System.out.print(logEntry.getValue() == true 
                        ? horse.getName()+" "+logEntry.getKey()+"/"+distance+" [BST]\t\t" 
                        :  horse.getName()+" "+logEntry.getKey()+"/"+distance+" [NRM]\t\t");
                        
                    }

                } 

                }catch(Exception ex){

                    System.out.print("\t\t\t\t\t");

                }

            }

            System.out.println();

        }

    }

    public void predictBoosts(){

        for(int i=0; i<this.getMaxSizeOfLogs(); i++){

            RaceHorse lastHorse = new RaceHorse("","");            

            int min = MAX_INT;

            for(RaceHorse horse : raceTrack){

                try{

                    Set<Map.Entry<Integer, Boolean>> logListEntrySet = log.get(horse).get(i).entrySet();

                    for(Map.Entry<Integer, Boolean> logEntry : logListEntrySet){

                        if(logEntry.getKey()<min && logEntry.getKey()!=min){

                            min = logEntry.getKey();

                            lastHorse = horse;

                        }else if(logEntry.getKey() == min){

                            min = MAX_INT;                 

                            continue;

                        }

                    }   

                }catch(Exception ex){
                }

            }

            if(min > 0 && min < MAX_INT){

                log.get(lastHorse).get(i).replace(min, true);

            }
        }

    }

    public void resetLog(){

        log = new LinkedHashMap <RaceHorse, ArrayList<LinkedHashMap<Integer, Boolean>>>();

        raceTrack.parallelStream().forEach((horse) -> {

            log.put(horse, new ArrayList<LinkedHashMap<Integer, Boolean>>())	;

        });
    }

    public void start() {	

        for(RaceHorse h : raceTrack){

            threads.add( new Thread( new RaceHorseStrutter(h)));
        
        }                        
        
        barrierGate = new CyclicBarrier(threads.size()+1, () -> {     //party size + 1 because main thread also waits on barrier
            
            this.printLog(barnToGateDistance);		

            System.out.println("All horses have reached the gate.");

            for(int sec=3;sec>0;sec--){

                try{

                    Thread.sleep(1000);

                }catch(InterruptedException | IllegalArgumentException ex){
                }

                System.out.println(sec);

            }

            System.out.println("~GO!");

            finishers = new ArrayList<RaceHorse>();	

            this.resetLog();
            
            distance = gateToFinishDistance;
            
            isRace = true;
            
        });
        
        distance = barnToGateDistance;
        
        isRace   = false;

        threads.parallelStream().forEach((t) -> {

            t.start();
        
        });     
        
        try {
            
            barrierGate.await();

        } catch (BrokenBarrierException | InterruptedException ex) {

            System.out.println("Barrier error");
        }

        
        Thread deathListen = new Thread(() -> {

            while(isAllDead==false){

                isAllDead = threads.parallelStream().allMatch((t) -> !(t.isAlive()));																

                try {

                    Thread.sleep(100);

                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();
                    
                }
            }
                        
        });
        
        try{

            deathListen.start();
            
            synchronized(deathListen){
            
                deathListen.wait();       
                
            }

        }catch(InterruptedException ex){
            
            System.out.println("thread death listener interrutped");
            
        }                        
              
        synchronized(finishers){	

            try{

                Collections.sort(finishers, timeCompare);

            }catch(NullPointerException ex){

                System.out.println("Null pointer while sorting finishers.ArrayList");

            }

            this.printLog(gateToFinishDistance);

            System.out.println("\n\n[Finished]("+finishers.size()+")\t\tElapsed\t\tStarting");		

            for(int i=0; i<finishers.size();i++){

                try{

                    System.out.println("["+(i+1)+"]\t"+finishers.get(i).getName()+"\t"+finishers.get(i).getElapsedNs()+"ns\t"+finishers.get(i).getStartingNs()+"ns");

                }catch(NullPointerException ex){

                    System.out.println("#"+i+"is a null pointer");
                
                }
                
            }	

            try{

                System.out.println(finishers.get(0).getName()+" won the race!");			

            }catch(NullPointerException ex){

                System.out.println("Winning horse fled! Null");
            }
        }	

    }
}