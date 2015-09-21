package br.remoto.teste;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import br.remoto.dao.ConfigurationDAO;
import br.remoto.dao.UserDAO;
import br.remoto.model.Configuration;
import br.remoto.model.Nerve;
import br.remoto.model.ReMoto;
import br.remoto.model.ResultDisplay;
import br.remoto.model.Simulation;
import br.remoto.model.vo.ResultVO;
import br.remoto.model.vo.User;
import br.remoto.util.PlotCombinedGraph;
import br.remoto.util.PlotXYLine;
import br.remoto.util.Point;

// Linha de comando para execucao, a aprtir do diretorio raiz onde estao os .class:
// java -Xms900m -Xmx1000m -cp .;F:\rogerio\workspace\lib\jfreechart-1.0.6.jar;F:\rogerio\workspace\lib\jcommon-1.0.10.jar;F:\rogerio\workspace\lib\hsqldb.jar;C:\java\tomcat\lib\servlet-api.jar br.remoto.teste.CurvaRecrutamento

public class CopyOfCurvaRecrutamento 
{
	UserDAO userDAO = new UserDAO();
	User user;
	Configuration conf;
	
	
	public CopyOfCurvaRecrutamento()
	{
		//ReMoto.path = "C:\\java\\tomcat\\webapps\\remoto\\";
		
		ReMoto.path = "C:\\Users\\Vitor\\Desktop\\Workspace\\remoto\\WebContent\\";
		
		user = userDAO.loadUser("vitor", "lodemarta");
		
		conf = new Configuration();
	}
		
	
	public static void main(String[] args) throws FileNotFoundException 
	{
		CopyOfCurvaRecrutamento teste = new CopyOfCurvaRecrutamento();
		
		teste.levantaCurva();
	}
	
	public void levantaCurva() throws FileNotFoundException
	{
		ConfigurationDAO simDAO = new ConfigurationDAO();
    	String cdSimulation = "1234";

		conf = simDAO.getConfiguration(14);

		double tFin = 300;

		ResultVO resultVO = new ResultVO();
		
		resultVO.setWithEMGnoise( false );
		resultVO.setWithEMGattenuation( true );
		
		resultVO.setOpt( ReMoto.array );
		resultVO.setTask("");

		int linha = 1;
		int linhaMN = 1;

		conf.setTFin( tFin );
		conf.setStep(0.05);
		//conf.setMerge( true );
		
		
		
		int numSubplots = 1;
		
		conf.setNumOfSubplots(numSubplots);
		
		List[] nmMuscles 			= new List[numSubplots];
		List[] nmSubplots 			= new List[numSubplots];
		List[] nmCdNeurons 			= new List[numSubplots];
		List[] nmCdSpecification 	= new List[numSubplots];
		List[] yLabels 				= new List[numSubplots];
		List[] legendLabels 		= new List[numSubplots];
		
		for(int k = 0; k < numSubplots; k++){
			nmSubplots[k] 			= new ArrayList();
			nmCdNeurons[k] 			= new ArrayList();
			nmCdSpecification[k] 	= new ArrayList();
			yLabels[k] 				= new ArrayList();
			legendLabels[k] 		= new ArrayList();
			nmMuscles[k] 			= new ArrayList();
		}
		
		nmSubplots[0].add(			"EMG");
		nmCdNeurons[0].add(			"");
		nmCdSpecification[0].add(	"");
		yLabels[0].add(				"EMG SOL");
		legendLabels[0].add(		"SOL");
		nmMuscles[0].add(			"SOL");
		
		conf.setNmSubplots(			nmSubplots);
		conf.setNmCdNeurons(		nmCdNeurons);
		conf.setNmCdSpecification(	nmCdSpecification);
		conf.setyLabels(			yLabels);
		conf.setLegendLabels(		legendLabels);
		conf.setNmMuscles(			nmMuscles);
		
		
    	Simulation sim = new Simulation( conf, cdSimulation );
    	
    	sim.createNetwork();
    	sim.createJoint();
		sim.resetMuscles(conf.getStep());
		sim.createInputs();
		sim.createStimulation();
		sim.createSynapses();
		
		resultVO.setCdAnalysis("parameters");

    	for(int i = 0; i <= 1; i ++)
		{
    		List nerves = conf.getNerves();
    		
    		sim.run();
    		
    		conf.setChangedConfiguration( false );
    		conf.setKeepProperties( true );
    		
			ArrayList outputEMG = new ArrayList();
			
			conf.setResult( resultVO );
			
			ResultDisplay results = new ResultDisplay(conf);
			
			results.generateResults(sim, outputEMG);

			double y = 0;
			double t = 0;
			
			XYSeries emg = new XYSeries("EMG");

			
			
			File output = new File(ReMoto.path + "emg" + i + ".txt");
		    
		    PrintWriter printWriter = new PrintWriter(output);
		    
		    
			for(int j = 0; j < outputEMG.size(); j++)
			{
				Point point = (Point)outputEMG.get(j);
				
				t = point.getX();
				y = point.getY();
				
				printWriter.println(t + "\t" + y);
				
				emg.add( t, Double.valueOf( y ) );
			}
			
			printWriter.close();
			
			XYSeriesCollection datasetS = new XYSeriesCollection();
		    datasetS.addSeries( emg );

		    PlotXYLine.generate(datasetS,
	    						 ReMoto.path + "emg" + i + ".jpg",
								 "EMG", 
								 "applied current [mA]", 
								 "EMG [mV]");
			
		    
		    
		    
		    
		    
			results = null;
			//sim = null;

			System.gc();
		}
    	
    	
	
	}
	
	

}
