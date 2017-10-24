package Kernel;



import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

import javax.swing.text.DefaultEditorKit.BeepAction;

import org.omg.CORBA.Current;

import com.sun.xml.internal.ws.policy.spi.PolicyAssertionValidator.Fitness;

import Configuración.Log;
import Kernel.Flor;
import Kernel.Solucion;
import MCDP.ModelMCDP;
import MCDP.Solution;

public class ArtificialBeeColony {

	/* PARAMETROS METAHEURiSTICA */
	
	int Ejecuciones;
	int Tries;
	int MejorSol;
	int limite;
	boolean explorar;
	
	/*Parametros ABEJAS*/
	int MAX_LENGTH; 		/*The number of parameters of the problem to be optimized*/
    int Abejas; 			/*The number of total bees/colony size. employed + onlookers*/
    int FOOD_NUMBER; 		/*The number of food sources equals the half of the colony size*/
    
    int LIMIT;  			/*A food source which could not be improved through "limit" trials is abandoned by its employed bee*/
    int MAX_EPOCH; 			/*The number of cycles for foraging {a stopping criteria}*/
    

	Random rand;
    ArrayList<Solucion> foodSources=null;;
    ArrayList<Flor> solutions;
    Solucion gBest;
    int epoch;
	
	private ModelMCDP base;
	private double MejorFit;
	private int PosicionMejorFit;
	private Object[] probabilidad;
	private boolean ONtrial=false;
	private boolean OnLocalz=false;
	private boolean ONMejoro=false;
	private int countMejoro;
	private int val;
	private boolean PrevActualConflict=false;

	public ArtificialBeeColony(int Abejas, int NP2,int limite, int Tries, int Ejecuciones) {
		this.Abejas = Abejas;
		this.FOOD_NUMBER = NP2 ;
		this.limite = limite;
		this.Tries = Tries;
		this.Ejecuciones = Ejecuciones;
		
		
	}

	public void setSol(int solucion) {
		this.MejorSol = solucion;
		return;
	}

	public int getSol() {
		return MejorSol;
	}

	public int getTries() {
		return Tries;
	}

	public int getAbejas() {
		return Abejas;
	}
	
	public void setAbejas(int bee) {
		this.Abejas = bee;
	}
	

	double CalculateFitness(double fun) {
		double result = 0;

		if (fun >= 0) {
			result = 1 / (fun + 1);
		} else {
			result = 1 + Math.abs(fun);
		}
		return result;
	}

	public InterCambio algorithm(ModelMCDP model) {

		
		foodSources = new ArrayList<Solucion>();
    	rand = new Random();
        boolean done = false;
        epoch = 0;
        

        initialize(model);
        memorizeBestFoodSource();

        while(!done) {
            if(epoch < limite) {
                sendEmployedBees();
                
                getFitness();
                calculateProbabilities();
                sendOnlookerBees();
                memorizeBestFoodSource();
                sendScoutBees(model);
                epoch++;
                
                
            } else {
                done = true;
            }
            
        }
        
        
        InterCambio Sol = new InterCambio(gBest); 
        
        return Sol;
		
	}
	
	/* Initializes all of the solutions' placement of queens in ramdom positions.
	 *
	 */ 
   public void initialize(ModelMCDP model) {
       
	   for(int i = 0; i < FOOD_NUMBER; i++) {
           Solucion newHoney = new Solucion(model,1);
      
           foodSources.add(newHoney);
       }
   }


	private void sendScoutBees(ModelMCDP model) {
		Solucion currentBee = null;
        
        for(int i =0; i < FOOD_NUMBER; i++) {
            
        	currentBee = foodSources.get(i);
            
        	if(ONtrial)
        		System.out.println("Trial " +" i "+ i +"| Trial:"+currentBee.getTrials());
            
            if(currentBee.getTrials() >= Tries){
            	
            	Solucion newHoney = new Solucion(model,1);
                currentBee = newHoney;
                foodSources.set(i, currentBee);
                if(ONtrial)
                	System.out.println("Nueva Solucion");

            }
        }
	}

	private void memorizeBestFoodSource() {
		
		gBest = foodSources.get(0);
		int min = gBest.getModel().getLocalZ();
		int Fitness =0;
		
		
		for (int i = 0; i < foodSources.size(); i++) {
			Fitness = foodSources.get(i).getModel().objectiveFunction();
			
			if(Fitness < min){
				min=Fitness;
			    gBest = foodSources.get(i);
			}
			
		}
		 
		
	}

	private void sendOnlookerBees() {
		// TODO Auto-generated method stub
		int i = 0;
        int t = 0;
        int neighborBeeIndex = 0;
        Solucion currentBee = null;
        Solucion neighborBee = null;

        while(t < FOOD_NUMBER) {
            currentBee = foodSources.get(i);
            if(rand.nextDouble() < currentBee.getSelectionProbability()) {
                
                neighborBeeIndex = getExclusiveRandomNumber(FOOD_NUMBER-1, i);
	            neighborBee = foodSources.get(neighborBeeIndex);
	            sendToWork(currentBee, neighborBee,t);
	            t++;
            }
            i++;
            if(i == FOOD_NUMBER) {
                i = 0;
            }
            //currentBee.getModel().setAndShowMeasures();
        }
        
	}

	private void calculateProbabilities() {
		Solucion thisFood = null;
        double maxfit = foodSources.get(0).getModel().objectiveFunction();
        
        for(int i = 1; i < FOOD_NUMBER; i++) {
            thisFood = foodSources.get(i);
            if(thisFood.getModel().getLocalZ() > maxfit) {
                maxfit = thisFood.getModel().getLocalZ();
            }
        }
         
        for(int j = 0; j < FOOD_NUMBER; j++) {
            thisFood = foodSources.get(j);
            thisFood.setSelectionProbability((0.9*(thisFood.getModel().objectiveFunction()/maxfit))+0.1);
            //System.out.println("proibabilidad "+j+" : "+thisFood.getSelectionProbability());
            
        }
		
	}

	private void getFitness() {
		// TODO Auto-generated method stub
		
		 MejorFit = CalculateFitness(foodSources.get(0).getModel().getLocalZ());
		for (int i=0;i<foodSources.size();i++){
			if (CalculateFitness(foodSources.get(i).getModel().getLocalZ()) > MejorFit)
				MejorFit = CalculateFitness(foodSources.get(i).getModel().getLocalZ());
				PosicionMejorFit = i;
	    }
		
	}

	private void sendEmployedBees() {
		
		int neighborBeeIndex = 0;
        Solucion currentBee = null;
        Solucion neighborBee = null;
        
        for(int i = 0; i < FOOD_NUMBER; i++) {
            
            neighborBeeIndex = getExclusiveRandomNumber(foodSources.size()-1, i);
            currentBee = foodSources.get(i);
            
            neighborBee = foodSources.get(neighborBeeIndex);
            
            if(OnLocalz)
            	System.out.println("[Artif]  antes Localz" +currentBee.getModel().getLocalZ());
            
            sendToWork(currentBee, neighborBee,i);
            
            if(OnLocalz)
            	System.out.println("[Artif] Despues" );
        }
		
	}

	
	public void sendToWork(Solucion currentBee, Solucion neighborBee,int i) {
    	 	
		int newValue = 0;
        int prevConflicts = 0;
        int currConflicts = 0;
        
        Solucion Aux = currentBee;
        prevConflicts= currentBee.getModel().objectiveFunction();
        
        //The parameter to be changed is determined randomly
        newValue = (int) (currentBee.getModel().objectiveFunction()+(currentBee.getModel().objectiveFunction() - neighborBee.getModel().objectiveFunction())*(rand.nextDouble()-0.5)*2);
        // Validacion si es mayor que las maquinas o menor que cero
        if(newValue > currentBee.getModel().getM() || newValue < 0){
        	if(ONMejoro){
	        	newValue = (int) rand.nextInt() *currentBee.getModel().getM();
	        	System.out.println("[ABC] newValue "+newValue);
	        	System.out.println("[ABC] newValue/1000000 "+newValue/100000000);
	        	System.out.println("[ABC] V4 "+V4(newValue/100000000));
	        	System.out.println("[ABC] V4*16 -->"+16*V4(newValue/100000000));
	        	val=(int)(currentBee.getModel().getM()*V4(16/100000000));
	        	System.out.println("[ABC] entero --> "+val);
	        	//currentBee.getModel().showAllData();
	        	System.out.println("\n");
        	}
        }
        if(OnLocalz)
        	System.out.println("[ABC] Localz 0 "+currentBee.getModel().getLocalZ());
        
        /*
	    v_{ij}=x_{ij}+\phi_{ij}*(x_{kj}-x_{ij}) 
	    solution[param2change]=Foods[i][param2change]+(Foods[i][param2change]-Foods[neighbour][param2change])*(r-0.5)*2;
         */
        currentBee.Cambio(val);
        
        
        
        currConflicts = currentBee.getModel().objectiveFunction();
        if(OnLocalz)
        	System.out.println("[ABC]  Localz 1 "+currentBee.getModel().getLocalZ());
        
        
        if(PrevActualConflict)
        	System.out.println("PrevConflict & ACtualConflict "+prevConflicts +" & "+ currConflicts);
        
        if(prevConflicts <= currConflicts) {						//No improvement
        	
        	currentBee = Aux;
            currentBee.setTrials(currentBee.getTrials()+1);
            countMejoro++;
            
        } else {												//improved solution
            
        	currentBee.setTrials(0);
            if(ONMejoro){
            	System.out.println("[ABC] Se demoro en mejorar "+ countMejoro);
            	System.out.println("[ABC] Mejoro "+ currentBee.getModel().getLocalZ());
        	}
            
            //currentBee.getModel().showAllData();
        	
        	foodSources.set(i, currentBee);
        }   
    }
	
	private static double V4(double x)
	   {
	       double s_bin;  
	       s_bin= (2/Math.PI)* Math.atan((Math.PI/60)*x);
	       
	       if(s_bin<0)
	       {
	           s_bin = s_bin*-1;
	       }
	       return s_bin;
	       
	   }
	
    private void setTries(int i) {
		// TODO Auto-generated method stub
		this.Tries = i;
	}

	public int getIndex(int value,int[] aux) {
        int k = 0;
        for(; k < MAX_LENGTH; k++) {
            if(aux[k] == value) {
                break;
            }
        }
        return k;
    }
	
	/* Gets a random number in the range of the parameters
	 *
	 * @param: the minimum random number
	 * @param: the maximum random number
	 * @return: random number
	 */ 
   public int getRandomNumber(int low, int high) {
       return (int)Math.round((high - low) * rand.nextDouble() + low);
   }
	
	 public int getExclusiveRandomNumber(int high, int except) {
	        boolean done = false;
	        int getRand = 0;

	        while(!done) {
	        	getRand = rand.nextInt(high);
	            
	            if(getRand != except){
	                done = true;
	            }
	        }

	        return getRand;     
	    }

	public int getEjecuciones() {
		return this.Ejecuciones;
	}

}
