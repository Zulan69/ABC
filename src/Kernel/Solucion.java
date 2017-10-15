package Kernel;

import java.util.Random;
import MCDP.ModelMCDP;
import sun.rmi.runtime.Log;


/**
 * @authors Leandro Vasquez & Roberto Zulantay. 
 *
 */
public class Solucion{
	
	/**
	 * The variable {@originalModel} is used for manipulate the origin and final data.
	 */
	private ModelMCDP originalModel;
	private double selectionProbability;
	private int trials;

	public int getTrials() {
		return trials;
	}


	public void setTrials(int trials) {
		this.trials = trials;
	}


	/**
	 * The ArrayList<Solution> {@code neighbourSet} is used for save a set of neighbour for one Solution.
	 */
	//private ArrayList<Flor> neighbourSet;
	
	
    public double getSelectionProbability() {
		return selectionProbability;
	}


	/**
     * This constructor {@code Solution()} generates an random initial solution based on a model MCDP.
     */
	public Solucion(ModelMCDP model, int optionInitialSolution)
	{
		originalModel = model.clone();
		//neighbourSet = new ArrayList<Flor>();
		boolean check = false;
		
		if (optionInitialSolution == 1)
		{
			// Generates a random initial solution.
			// Genera una solucionn consistente, ha pasado por las 3 restricciones y se ha calculado su fitness.
			while (true)
			{
				check = generate_RandomY();
				if (check == true)
				{
					//System.out.println("==================================================== ENCONTRADO_Y");
					break;
				}
				//System.out.println("==================================================== AGAIN Y");
			}
			while (true)
			{
				check = generate_ManualZ();
				if (check == true)
				{
					//System.out.println("==================================================== ENCONTRADO Z");
					break;
				}
				//System.out.println("==================================================== AGAIN Z");
			}
			int objectiveFunctionValue = originalModel.objectiveFunction();
			originalModel.setLocalZ(objectiveFunctionValue);
		}
		if (optionInitialSolution == 2)
		{
			// Generates a random initial solution.
			// Genera una solucionn consistente, ha pasado por las 3 restricciones y se ha calculado su fitness.
			while (true)
			{
				check = Flor_ManualWithRandomBitB_Y();
				if (check == true)
				{
					//System.out.println("==================================================== ENCONTRADO_Y");
					break;
				}
				//System.out.println("==================================================== AGAIN Y");
			}
			while (true)
			{
				check = generate_ManualZ();
				if (check == true)
				{
					//System.out.println("==================================================== ENCONTRADO Z");
					break;
				}
				//System.out.println("==================================================== AGAIN Z");
			}
			int objectiveFunctionValue = originalModel.objectiveFunction();
			originalModel.setLocalZ(objectiveFunctionValue);
		}
		
	}
	
	
	public boolean Cambio(int maq){
		
		boolean check;
		// Generates a random initial solution.
		// Genera una solucionn consistente, ha pasado por las 3 restricciones y se ha calculado su fitness.
		while (true)
		{
			check = Flor_ManualWithRandomBitB_Y();
			if (check == true)
			{
				//.println("==================================================== ENCONTRADO_Y");
				break;
			}
			//System.out.println("==================================================== AGAIN Y");
		}
		while (true)
		{
			check = generate_ManualZ();
			if (check == true)
			{
				//System.out.println("==================================================== ENCONTRADO Z");
				break;
			}
			//System.out.println("==================================================== AGAIN Z");
		}
		int objectiveFunctionValue = originalModel.objectiveFunction();
		originalModel.setLocalZ(objectiveFunctionValue);
		
		
		
		return true;
		
	}
	
	
	
	public Solucion(ModelMCDP model)
	{
		originalModel = model.clone();
		//neighbourSet = new ArrayList<Flor>();
	}

	private boolean generate_RandomY()
	{
		int Y[][] = new int[originalModel.getM()][originalModel.getC()];
		
		// Generar matriz M*C randomicamente
		for (int i = 0; i < originalModel.getM(); i++)
		{
			Random random = new Random();
			int cell;
			
			// The array is initialized with zeros.
			for (int k = 0; k < originalModel.getC(); k++)
			{
				Y[i][k] = 0;
			}
			
			// Get random value between 1 to Number Cell.
			cell = (int) (random.nextDouble() * originalModel.getC());
			Y[i][cell] = 1;
		}
		
		// Save the new matrix.
		this.originalModel.setY(Y);
		
		return originalModel.consistencyConstraint_1() & originalModel.consistencyConstraint_3();
	}
	
	private boolean generate_ManualZ()
	{
		int Z[][] = new int[originalModel.getP()][originalModel.getC()];
		int[] indexK = new int[originalModel.getM()];
		
		// Generar matriz temporal indexK
		// requisito que la matriz Y sea consistente a las restricciones.
		for (int i = 0; i < originalModel.getM(); i++)
		{
			// 
			for (int k = 0; k < originalModel.getC(); k++)
			{
				if (originalModel.getY()[i][k] == 1)
				{
					indexK[i] = k;
				}
			}
		}
			
		// Create P*C matrix <--> Z[][] de forma manual
		// Inicializar P*C
		for (int j = 0; j < originalModel.getP(); j++)
		{
			for (int k = 0; k < originalModel.getC(); k++)
			{
				Z[j][k] = 0;
			}
		}
		
		// sumar los unos en la matriz A, pero separandolos por celdas, revisar vectorM
		for (int j = 0; j < originalModel.getP(); j++)
		{
			for (int i = 0; i < originalModel.getM(); i++)
			{
				if (originalModel.getA()[i][j] == 1)
				{
					// obtener indice de la matriz Y (M*C)
					int index = indexK[i];
					Z[j][index] = Z[j][index] + 1;
				}
			}
		}
		
		//en la matriz Z, asignar un 1 al valor mas alto de la fila.
		for (int j = 0; j < originalModel.getP(); j++)
		{
			int valorAlto = Integer.MIN_VALUE;
			int posicionK = 0;
			for (int k = 0; k < originalModel.getC(); k++)
			{
				if (Z[j][k] >= valorAlto)
				{
					posicionK = k;
					valorAlto = Z[j][k];
				}
			}
			//llenar de ceros la fila K
			for (int k = 0; k < originalModel.getC(); k++)
			{
				Z[j][k] = 0;
			}
			// asignar la posicionK el valor 1, ya que es el valor mas alto.
			Z[j][posicionK] = 1;	
		}	
		
		originalModel.setZ(Z);
		
		int objectiveFunctionValue = originalModel.objectiveFunction();
		originalModel.setLocalZ(objectiveFunctionValue);
		
		return originalModel.checkConstraint();
	}

	 public void mostrarSolucion()
	{
		originalModel.showInitialData();
	
		return;
	}


	 public ModelMCDP getModel(){
		 
		 
		 return originalModel;
		 
	 }
	 
//	 public int getFunObj(){
//			
//			return originalModel.objectiveFunction(originalModel.getA(),originalModel.getY(),originalModel.getZ());	
//			
//			
//	}
	 
	 private boolean Flor_ManualWithRandomBitB_Y()
		{
		 	
		 	// Determine a new solution matrix Y
			int[] oldCell, newCell;
			int Y[][] = new int[originalModel.getM()][originalModel.getC()];
		
			// Add matrix Y into temp matrix Y.
			for (int i = 0; i < originalModel.getM(); i++)
			{
				for (int k = 0; k < originalModel.getC(); k++)
				{
				    Y[i][k] = originalModel.getY()[i][k];
				    
				}
			}
		
			// Find a (x, y) in the matrix with value 1.
			oldCell = searchCellPosition(Y, 1);
			
			// Find a (x, y) in the matrix.
			newCell = searchCellPositionInRow(Y, 1, oldCell[0]);
			
			// Change the values to generate a new neighbor.
			// Example
			// [x,y] <=> [0,0] = 1
			// [x,y+1] <=> [0,1] = 0
			// after->
			// [x,y] <=> [0,0] = 0
			// [x,y+1] <=> [0,1] = 1
			
			Y[oldCell[0]][oldCell[1]] = 0;
			Y[newCell[0]][newCell[1]] = 1;
			

			//System.out.println("\n");
			// Save the new matrix.
			this.originalModel.setY(Y);
			
			boolean res1 =originalModel.consistencyConstraint_1() ;
			
			boolean res2 = originalModel.consistencyConstraint_3();
			
			boolean res =  res1 & res2;
			
			//System.out.println("res1 "+ res1 + " res2 "+ res2 + "  ambas => "+ res);
			return res;
		}
	
	 private boolean Flor_ManualWithCambio(int maq)
		{
		 	
		 	// Determine a new solution matrix Y
			int[] oldCell, newCell;
			int Y[][] = new int[originalModel.getM()][originalModel.getC()];
		
			// Add matrix Y into temp matrix Y.
			for (int i = 0; i < originalModel.getM(); i++)
			{
				for (int k = 0; k < originalModel.getC(); k++)
				{
				    Y[i][k] = originalModel.getY()[i][k];
				    
				}
			}
		
			// Find a (x, y) in the matrix with value 1.
			oldCell = searchCellPosition(Y, 1,maq);
			
			// Find a (x, y) in the matrix.
			newCell = searchCellPositionInRow(Y, 1, oldCell[0]);
			
			// Change the values to generate a new neighbor.
			// Example
			// [x,y] <=> [0,0] = 1
			// [x,y+1] <=> [0,1] = 0
			// after->
			// [x,y] <=> [0,0] = 0
			// [x,y+1] <=> [0,1] = 1
			
			Y[oldCell[0]][oldCell[1]] = 0;
			Y[newCell[0]][newCell[1]] = 1;
			

			//System.out.println("\n");
			// Save the new matrix.
			this.originalModel.setY(Y);
			
			boolean res1 =originalModel.consistencyConstraint_1() ;
			
			boolean res2 = originalModel.consistencyConstraint_3();
			
			boolean res =  res1 & res2;
			
			//System.out.println("res1 "+ res1 + " res2 "+ res2 + "  ambas => "+ res);
			return res;
		}

	 
	 private int[] searchCellPosition(int Y[][], int value)
		{
			Random random = new Random();
			int result[] = new int[2];
			
			int x = 0;
			int y = 0;
			while (true)
			{
				// Random (i,k) position.
				// (i,k) <=> (x,y).
				x = (int) (random.nextDouble() * originalModel.getM());
				y = (int) (random.nextDouble() * originalModel.getC());
				
				int cell = Y[x][y]; 
				if (cell == value)
				{
					result[0] = x;
					result[1] = y;
					break;
				}	
			}
		
			return result;
		}
	 
	 private int[] searchCellPosition(int Y[][], int value, int maq)
		{
			Random random = new Random();
			int result[] = new int[2];
			
			int x = 0;
			int y = 0;
			while (true)
			{
				// Random (i,k) position.
				// (i,k) <=> (x,y).
				x = maq;
				y = (int) (random.nextDouble() * originalModel.getC());
				
				int cell = Y[x][y]; 
				if (cell == value)
				{
					result[0] = x;
					result[1] = y;
					break;
				}	
			}
		
			return result;
		}
	 
	 private int[] searchCellPositionInRow(int Y[][], int value, int row)
		{
			Random random = new Random();
			int result[] = new int[2];
			int y;
			
			
			while (true)
			{	
				double r= (   (double)Math.random()*32767 / ((double)(32767)+(double)(1)) );
				//y= (int) (row + (row - aux_y)*(r-0.5)*2);
				
				y = (int) (random.nextDouble() * originalModel.getC());
				y= (int) Math.floor(V4(y)*(originalModel.getC()));
				
				//System.out.println(y);
				
				int cell = Y[row][y]; 
				if (cell != value)
				{
					result[0] = row;
					result[1] = y;
					break;
				}
			}
		
			return result;
		}
	 
	 private static double V4(double x)
	   {
	       double s_bin;  
	       s_bin= (2/Math.PI)* Math.atan((Math.PI/2)*x);
	       
	       if(s_bin<0)
	       {
	           s_bin = s_bin*-1;
	       }
	       return s_bin;
	       
	   }


	public void setSelectionProbability(double mSelectionProbability) {
        this.selectionProbability = mSelectionProbability;
    }
}
