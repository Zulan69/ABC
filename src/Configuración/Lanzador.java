package Configuración;

import java.util.Calendar;
import java.util.GregorianCalendar;

import Kernel.ArtificialBeeColony;
import Kernel.DataResult;
import Kernel.InterCambio;
import Kernel.Solucion;
import MCDP.Problemas;
import Configuración.*;

public class Lanzador {

	private int j;
	private int k;
	private int i;
	private boolean ContAbejas=false;

	public boolean ejecutarAplicacion() {

		Log log = new Log();
		int mejorSol = 0;
		boolean start = true;

		// Ve si existe el fichero LOG si no existe lo Crea
		if (log.detectarArchivo()) {

			// Abre el archivo config.txt si no existe crea uno por defecto
			ConfigInicialABC config = new ConfigInicialABC();
			config.detectarArchivo();

			/* Solo servira para saber cuantos Loop se realizan */
			ArtificialBeeColony aux = (ArtificialBeeColony) config.leerArchivo("archivo");

			Problemas problemas = new Problemas();
			problemas.procesarArchivos();

			int[][] result = new int[aux.getEjecuciones()][aux.getAbejas()];
			int[][] resultPob = new int[aux.getEjecuciones()][aux.getAbejas()];
			long[][] resultTime = new long[aux.getEjecuciones()][aux.getAbejas()];
			
			DataResult resultados = new DataResult();
			

			for (i = 0; i < problemas.getProblemas().size(); i++) {

				log.escribirArchivo("Inicio problema " + i + " " + problemas.getNombreProblema(i));

				/* Ejecuciones ABC */
				for (j = 0; j < aux.getEjecuciones(); j++) {

					start = true;

					long time_start, time_end;
					time_start = System.currentTimeMillis();
					time_end = System.currentTimeMillis();

					InterCambio sol = new InterCambio();
					
					for (k = 0; k < aux.getAbejas(); k++) {

						ArtificialBeeColony ABC = (ArtificialBeeColony) config.leerArchivo("archivo");
						
						time_start = System.currentTimeMillis();
						
						if(ContAbejas)
							System.out.println("Abeja "+k);
						
						sol = ABC.algorithm(problemas.getProblema(i));
						
						start = false;
						
						if (k == 0) {
							result[j][k] = sol.getSolucion().getModel().getLocalZ();
							resultPob[j][k] = 0;

							mejorSol = sol.getSolucion().getModel().getLocalZ();
							
						} else {
							
							if (sol.getSolucion().getModel().getLocalZ() < mejorSol) {
								mejorSol = sol.getSolucion().getModel().getLocalZ();
								result[j][k]= sol.getSolucion().getModel().getLocalZ();
								resultPob[j][k] = sol.getSolucion().getModel().getLocalZ();
								
							} else {
								
								result[j][k] = mejorSol;
								resultPob[j][k] = sol.getSolucion().getModel().getLocalZ();
								
							}
							
						}
						time_end = System.currentTimeMillis();
						
						
						//resultTime[k][j] = time_end - time_start;
						//sol.getModel().setAndShowMeasures();	
					}
					
					
					resultados.setIteracionOptimo(result,resultPob);
					resultados.setResumen(resultados.PromedioMejorSol(),resultados.PoblacionPromedio());
				}
				Excel ex = new Excel();
				ex.writerResultExcel(result, problemas.getNombreProblema(i), resultPob, resultTime,aux.getEjecuciones(),aux.getAbejas());
				
				resultados.setIteracionOptimo(result,resultPob);
				resultados.setResumen(resultados.PromedioMejorSol(),resultados.PoblacionPromedio());
				
				System.out.println("promedio mejor sol "+resultados.PromedioMejorSol());
				System.out.println("mejor sol "+mejorSol);
				
				
			}

			// Escribe Resumen Datos
//			Excel ex = new Excel();
//			ex.writerResultExcelFinish(resultados, resultPob, null, resultPob, resultTime,j,k);
//			
			
//			System.out.println("a "+result.length +"B"+result[0].length);
//			System.out.println("a "+resultPob.length +"B"+resultPob[0].length);
//			for (int j = 0; j < result.length; j++) {
//				System.out.println("asd");
//				for (int k = 0; k < result[0].length; k++) {
//					System.out.println("a "+resultPob[j][k]); 
//				}
//			}
			
		} else {

			return false;
		}

		return true;
	}
}
