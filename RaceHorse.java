public class RaceHorse{
	
    private String 	name;
    private int 	position;
    private int		maxSpeed 	= 10;
    private int		minSpeed 	= 1;
    private String 	battleCry 	= "I win!";
    private long 	elapsedNs	= 0;
    private long 	startingNs	= 0;

    RaceHorse(String name, String battleCry){

        this.setName(name);
        
        this.setBattleCry(battleCry);
        
    }

    public int getMaxSpeed(){

        return this.maxSpeed;
    
    }

    public void shoutBattleCry(){

        System.out.println("\t\t"+this.battleCry+"\t\t");
    
    }

    public void setBattleCry(String battleCry){

        this.battleCry = battleCry;
       
    }

    public void setElapsedNs(long elapsedNs){

        this.elapsedNs = elapsedNs;
    
    }
	
	public void setStartingNs(long startingNs){

        this.startingNs = startingNs;
    
    }

    public void setName(String name){

        this.name = name;
    
    }

    public String getBattleCry(){

        return this.battleCry;
    
    }

	public long getStartingNs(){

        return this.startingNs;
    
    }    
    
    public long getElapsedNs(){

        return this.elapsedNs;
    
    }

    public String getName(){

        return this.name;
    
    }

    public int getPosition(){

        return this.position;
    
    }

    public void resetRacingState(){

        this.position   = 0;
        
        this.elapsedNs  = 0;        
    
    }

    public int preciseStrut(int increment){

        this.position = this.position + increment;

        return this.position;
    
    }

    public int strut(){

        this.position = this.position + ((int)((Math.random()*maxSpeed)+minSpeed));

        return this.position;
    
    }

    public int strut(int increment){

        this.position = this.position + ((int)((Math.random()*increment)+minSpeed));

        return this.position;
    
    }
		
}