package Kernel;



import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

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
	
	/*ABC PARAMETERS*/
	int MAX_LENGTH; 		/*The number of parameters of the problem to be optimized*/
    int Abejas; 			/*The number of total bees/colony size. employed + onlookers*/
    int FOOD_NUMBER; 		/*The number of food sources equals the half of the colony size*/
    
    int LIMIT;  			/*A food source which could not be improved through "limit" trials is abandoned by its employed bee*/
    int MAX_EPOCH; 			/*The number of cycles for foraging {a stopping criteria}*/
    int MIN_SHUFFLE;
    int MAX_SHUFFLE;

	Random rand;
    ArrayList<Solucion> foodSources;
    ArrayList<Flor> solutions;
    Solucion gBest;
    int epoch;
	
	private ModelMCDP base;
	private double MejorFit;
	private int PosicionMejorFit;
	private Object[] probabilidad;

	public ArtificialBeeColony(int Abejas, int NP2, int Tries, int Ejecuciones) {
		this.Abejas = Abejas;
		this.FOOD_NUMBER = NP2 ;
		this.limite = NP2;
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

	public Solucion algorithm(ModelMCDP model) {

		Log log = new Log();
		
		
		foodSources = new ArrayList<Solucion>();
    	solutions = new ArrayList<Flor >();
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
		return gBest;
		
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
		// TODO Auto-generated method stub
		Solucion currentBee = null;
        //int shuffles = 0;

        for(int i =0; i < FOOD_NUMBER; i++) {
            currentBee = foodSources.get(i);
            if(currentBee.getTrials() >= LIMIT) {
//              
            	//shuffles = getRandomNumber(MIN_SHUFFLE, MAX_SHUFFLE);
//                for(int j = 0; j < shuffles; j++) {
//                    randomlyArrange(i);
//                }
//                currentBee.computeConflicts();
//                currentBee.setTrials(0);
            	
            	Solucion newHoney = new Solucion(model,1);
                currentBee = newHoney;

            }
        }
	}

	private void memorizeBestFoodSource() {
		
		int min = foodSources.get(0).getModel().getLocalZ();
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
                t++;
                neighborBeeIndex = getExclusiveRandomNumber(FOOD_NUMBER-1, i);
	            neighborBee = foodSources.get(neighborBeeIndex);
	            sendToWork(currentBee, neighborBee);
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
            //A randomly chosen solution is used in producing a mutant solution of the solution i
            //neighborBee = getRandomNumber(0, Food_Number-1);
        	
        	//System.out.println("abejas "+Abejas);
            neighborBeeIndex = getExclusiveRandomNumber(foodSources.size()-1, i);
            currentBee = foodSources.get(i);
            
            neighborBee = foodSources.get(neighborBeeIndex);
            
            sendToWork(currentBee, neighborBee);
        }
		
	}

	
	public void sendToWork(Solucion currentBee, Solucion neighborBee) {
    	
		int newValue = 0;
        int tempValue = 0;
        int prevConflicts = 0;
        int currConflicts = 0;
        

        
        Solucion Aux = currentBee;
        prevConflicts= currentBee.getModel().objectiveFunction();
        
        //The parameter to be changed is determined randomly
        newValue = (int)(tempValue+(tempValue - neighborBee.getModel().objectiveFunction())*(rand.nextDouble()-0.5)*2);
        // Validacion si es mayor que las maquinas o menor que cero
        if(newValue > currentBee.getModel().getM() || newValue < 0){
        	newValue = (int) rand.nextInt() *currentBee.getModel().getM();
        }
        
        /*
		    v_{ij}=x_{ij}+\phi_{ij}*(x_{kj}-x_{ij}) 
		    solution[param2change]=Foods[i][param2change]+(Foods[i][param2change]-Foods[neighbour][param2change])*(r-0.5)*2;
        */
        
        currentBee.Cambio(newValue);
        
        currConflicts = currentBee.getModel().objectiveFunction();
        

        //greedy selection
        if(prevConflicts < currConflicts) {						//No improvement
            
        	currentBee = Aux;
            
            currentBee.setTrials(currentBee.getTrials()+1);
        } else {												//improved solution
            currentBee.setTrials(0);
        }   
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
	 
	private void add(Solucion solucion) {
		// TODO Auto-generated method stub
		
	}

	private ArrayList<Double> calculaprobabilidades(ArrayList<Solucion> flor, double MejorFit) {

		ArrayList<Double> prob = null;
		for (int i = 0; i < flor.size(); i++) {

			prob.set(i,
					(0.9 * (CalculateFitness(CalculateFitness(flor.get(i).getModel().objectiveFunction()) / MejorFit))
							+ 0.1));

		}

		return prob;
	}

	public int getEjecuciones() {
		return this.Ejecuciones;
	}

//	public static void quickSort3(ArrayList<Solucion> arr, int low, int high) {
//		if (arr == null || arr.size() == 0)
//			return;
//
//		if (low >= high)
//			return;
//
//		// pick the pivot
//		int middle = low + (high - low) / 2;
//		int pivot = arr.get(middle).getFunObj();
//
//		// make left < pivot and right > pivot
//		int i = low, j = high;
//		while (i <= j) {
//			while (arr.get(i).getFunObj() < pivot) {
//				i++;
//			}
//
//			while (arr.get(j).getFunObj() > pivot) {
//				j--;
//			}
//
//			if (i <= j) {
//				Solucion temp = arr.get(i);
//				arr.set(i, arr.get(j));
//				arr.set(j, temp);
//
//				i++;
//				j--;
//			}
//		}
//
//		// recursively sort two sub parts
//		if (low < j)
//			quickSort3(arr, low, j);
//
//		if (high > i)
//			quickSort3(arr, i, high);
//	}
}
