package br.remoto.teste;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import br.remoto.dao.ConfigurationDAO;
import br.remoto.dao.UserDAO;
import br.remoto.model.Configuration;
import br.remoto.model.ReMoto;
import br.remoto.model.ResultDisplay;
import br.remoto.model.Simulation;
import br.remoto.model.Neuron.Miscellaneous;
import br.remoto.model.vo.ConductanceVO;
import br.remoto.model.vo.DynamicVO;
import br.remoto.model.vo.MiscellaneousVO;
import br.remoto.model.vo.NerveVO;
import br.remoto.model.vo.NeuronVO;
import br.remoto.model.vo.Nucleus;
import br.remoto.model.vo.ResultVO;
import br.remoto.model.vo.User;
import br.remoto.util.PlotCombinedGraph;
import br.remoto.util.PlotScatter;
import br.remoto.util.PlotXYLine;
import br.remoto.util.Point;


public class Sensitivity 
{
	UserDAO userDAO = new UserDAO();
	User user;
	Configuration conf;
	
	MessageFormat mf = new MessageFormat("{0,number,#.#####}", Locale.US);
	
	boolean test;
	
	public Sensitivity()
	{
		ReMoto.path = "C:\\Users\\Vitor\\Desktop\\Workspace\\remoto\\WebContent\\";
		
		user = userDAO.loadUser("vitor", "lodemarta");
		
		conf = new Configuration();
	}
		
	
	public static void main(String[] args) throws FileNotFoundException 
	{
		Sensitivity teste = new Sensitivity();
		
		teste.run();
	}
	
	public void run() throws FileNotFoundException
	{
		ConfigurationDAO simDAO = new ConfigurationDAO();
    	String cdSimulation = "1234";

		conf = simDAO.getConfiguration(14);
		
		//conf.setMerge( true );
		
		ResultVO resultVO = new ResultVO();
		
		resultVO.setWithEMGnoise( false );
		resultVO.setWithEMGattenuation( false );
		
		resultVO.setOpt( ReMoto.array );
		resultVO.setTask("");
		resultVO.setCdAnalysis("parameters");

		conf.setResult( resultVO );
		
		conf.setCdMuscleModel("hill");
		
		conf.setPrimaryBag1Gain(10000);
		conf.setPrimaryBag2AndChainGain(5000);
		
		conf.setCdJoint("ankle");
		conf.setCdJointModel("isometric");
		conf.setDecimationFrequency(20000);
		conf.setRecruitmentOrderFES("uniform");
		conf.setStep(0.001);
		conf.setTFin(500);
		conf.setMiscellaneous("step", conf.getStep());
		
		int numOfSimulations = 10;
		
		conf.setChangedConfiguration( true );
		conf.setKeepProperties( false );

		String synapticDynamics = ReMoto.facilitating;
		//String synapticDynamics = ReMoto.none;
		
		String stimulus = "HReflex";
		//String stimulus = "StretchReflex";
		
		test = false;
		
		String dataPath;
		String graphicPath;
		
		if(test){
			
			dataPath = ReMoto.path + "simulation\\data\\tests\\" + stimulus + "\\" +  (int) (conf.getStep() * 1000) + "\\" ;
			graphicPath = ReMoto.path + "simulation\\graphics\\tests\\" + stimulus + "\\" +  (int) (conf.getStep() * 1000)  + "\\" ;
		}
		else{
			
			dataPath = ReMoto.path + "simulation\\data\\" + stimulus + "\\" + (int) (conf.getStep() * 1000) + "\\";
			graphicPath = ReMoto.path + "simulation\\graphics\\" + stimulus + "\\" +  (int) (conf.getStep() * 1000)  + "\\";
		}
		
		
		
		String timeStepString = "TimeStep: " + String.valueOf((int) (conf.getStep() * 1000)) + " us";
		String synapticDynamicsString = "SynapticDynamics: " + synapticDynamics;
		
		Simulation sim = new Simulation( conf, cdSimulation, timeStepString, synapticDynamicsString );
    	
    	sim.createNetwork();
    	sim.createJoint();
		sim.resetMuscles(conf.getStep());
		sim.createInputs();
		sim.createStimulation();
		sim.createSynapses();
		
		
		if(stimulus.equals("HReflex")){
			NerveVO ptn = conf.getNerve(ReMoto.PTN);
    		
    		ptn.setAmp(11.3);
    		ptn.setTini(150);
    		ptn.setTfin(151);
    		ptn.setFreq(1);
    		
    		
    		//conf.setJointStimulusInitialTime(1000);
			//conf.setJointStimulusFinalTime(5000);
			//conf.setJointInitialAngle(0);
			//conf.setJointFinalAngle(5);
			conf.setJointVelocity(125);
			conf.setKneeAngle(0);
    		
    		
		}
		else if(stimulus.equals("StretchReflex")){
			
			for(int j = 0; j < conf.getAllNerves().size(); j++){
				
		    	NerveVO nerveVO = (NerveVO) conf.getAllNerves().get(j);
		    	nerveVO.setActive(false);
		    }
			
			//conf.setJointStimulusInitialTime(200);
			//conf.setJointStimulusFinalTime(5000);
			//conf.setJointInitialAngle(0);
			//conf.setJointFinalAngle(5);
			conf.setJointVelocity(125);
			conf.setKneeAngle(0);
		}
		
		String path = null;
		
		if(synapticDynamics.equals(ReMoto.facilitating))
		{	
			//for(int variation = 10; variation <= 70; variation = variation + 30) // Varying variation
			//{	
				int variation = 70;
				
    			for(int tau = 15; tau <= 1500; tau = tau * 10) // Varying tau
    			{	
    				
    				for(int gmax = 500; gmax <= 700; gmax = gmax + 50) // Varying Gmax
    				{	
    					path = synapticDynamics + "\\var=" + variation + "\\tau=" + tau + "\\Gmax=" + gmax;
        				
    					conf.getSynapseType("SOL", "SOL", "AF Ia-MN S").setGmax(gmax);	
    		    		conf.getSynapseType("SOL", "SOL", "AF Ia-MN FR").setGmax(gmax);	
    		    		conf.getSynapseType("SOL", "SOL", "AF Ia-MN FF").setGmax(gmax);
    		    		
    		    		conf.getSynapseType("SOL", "SOL", "AF Ia-MN S").getDynamics().setDynamicType(synapticDynamics);
    		    		conf.getSynapseType("SOL", "SOL", "AF Ia-MN FR").getDynamics().setDynamicType(synapticDynamics);
    		    		conf.getSynapseType("SOL", "SOL", "AF Ia-MN FF").getDynamics().setDynamicType(synapticDynamics);
    		    		
    		    		conf.getSynapseType("SOL", "SOL", "AF Ia-MN S").getDynamics().setTau(tau);
    		    		conf.getSynapseType("SOL", "SOL", "AF Ia-MN FR").getDynamics().setTau(tau);
    		    		conf.getSynapseType("SOL", "SOL", "AF Ia-MN FF").getDynamics().setTau(tau);
    		    		
    		    		conf.getSynapseType("SOL", "SOL", "AF Ia-MN S").getDynamics().setVariation(variation);
    		    		conf.getSynapseType("SOL", "SOL", "AF Ia-MN FR").getDynamics().setVariation(variation);
    		    		conf.getSynapseType("SOL", "SOL", "AF Ia-MN FF").getDynamics().setVariation(variation);
    		    		
        				
    		    		for(int k = 1; k <= numOfSimulations; k++){
    		    			
    		    			System.out.println("-> SCENARIO:  " + timeStepString + "  " + 
    		    								synapticDynamicsString + 
    		    								"  Stimulus: " + stimulus + 
    		    								"  Gmax: " + gmax + 
    		    								"  Syn. Var.: " + variation + 
    		    								"  Syn. Time Cte: " + tau + 
    		    								"  Sim.: " + k);
    		    			
    		    			sim.run();
    		        		
    		    			generateLogOfScenario(k, dataPath, path);
    		    			
    		    			generateAndStoreData("emg", sim, dataPath, graphicPath, path, k);
    		    			generateAndStoreData("spikesMNs", sim, dataPath, graphicPath, path, k);
    		    			generateAndStoreData("spikesIas", sim, dataPath, graphicPath, path, k);
    		    			generateAndStoreData("meanFiringRateIa", sim, dataPath, graphicPath, path, k);
    		    			
    		    			generateAndStoreData("jointAngle", sim, dataPath, graphicPath, path, k);
    		    			generateAndStoreData("muscleForce", sim, dataPath, graphicPath, path, k);
    		    			generateAndStoreData("jointTorque", sim, dataPath, graphicPath, path, k);
    		    			
    		    		}
        			}
    			//}
			}
    	}
    	else if(synapticDynamics.equals(ReMoto.none)){
    			
    		for(int gmax = 500; gmax <= 700; gmax = gmax + 50) // Varying Gmax
    		{	
    			path = synapticDynamics + "\\Gmax=" + gmax;
    			
    			conf.getSynapseType("SOL", "SOL", "AF Ia-MN S").setGmax(gmax);	
	    		conf.getSynapseType("SOL", "SOL", "AF Ia-MN FR").setGmax(gmax);	
	    		conf.getSynapseType("SOL", "SOL", "AF Ia-MN FF").setGmax(gmax);
	    		
	    		conf.getSynapseType("SOL", "SOL", "AF Ia-MN S").getDynamics().setDynamicType(synapticDynamics);
	    		conf.getSynapseType("SOL", "SOL", "AF Ia-MN FR").getDynamics().setDynamicType(synapticDynamics);
	    		conf.getSynapseType("SOL", "SOL", "AF Ia-MN FF").getDynamics().setDynamicType(synapticDynamics);
	    		
				
	    		for(int k = 1; k <= numOfSimulations; k++){
	    			
	    			System.out.println("-> SCENARIO:  " + timeStepString + "  " + 
	    								synapticDynamicsString + 
	    								"  Stimulus: " + stimulus + 
	    								"  Gmax: " + gmax + 
	    								"  Syn. Var.: ###" + 
	    								"  Syn. Time Cte: ###" + 
	    								"  Sim.: " + k);
	    			
	    			sim.run();
	        		
	    			generateLogOfScenario(k, dataPath, path);
	    			
	    			generateAndStoreData("emg", sim, dataPath, graphicPath, path, k);
	    			generateAndStoreData("spikesMNs", sim, dataPath, graphicPath, path, k);
	    			generateAndStoreData("spikesIas", sim, dataPath, graphicPath, path, k);
	    			generateAndStoreData("meanFiringRateIa", sim, dataPath, graphicPath, path, k);
	    			
	    			generateAndStoreData("jointAngle", sim, dataPath, graphicPath, path, k);
	    			generateAndStoreData("muscleForce", sim, dataPath, graphicPath, path, k);
	    			generateAndStoreData("jointTorque", sim, dataPath, graphicPath, path, k);
	    			
	    		}
    		}
		}
	}
	
	public void generateAndStoreData(String output, Simulation sim, String dataPath, String graphicPath, String path, int simCount) throws FileNotFoundException{
		
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
		
		if(output.equals("emg")){
			nmSubplots[0].add(			"EMG");
			nmCdNeurons[0].add(			"");
			nmCdSpecification[0].add(	"");
			yLabels[0].add(				"EMG SOL");
			legendLabels[0].add(		"SOL");
			nmMuscles[0].add(			"SOL");
		}
		else if(output.equals("spikesMNs")){
			nmSubplots[0].add(			"spikeTimes");
			nmCdNeurons[0].add(			"All MNs");
			nmCdSpecification[0].add(	"atTerminal");
			yLabels[0].add(				"MNs SOL");
			legendLabels[0].add(		"SOL");
			nmMuscles[0].add(			"SOL");
		}
		else if(output.equals("spikesIas")){
			nmSubplots[0].add(			"spikeTimes");
			nmCdNeurons[0].add(			"Ia");
			nmCdSpecification[0].add(	"atTerminal");
			yLabels[0].add(				"Ia SOL");
			legendLabels[0].add(		"SOL");
			nmMuscles[0].add(			"SOL");
		}
		else if(output.equals("meanFiringRateIa")){
			nmSubplots[0].add(			"meanFiringRate");
			nmCdNeurons[0].add(			"Ia");
			nmCdSpecification[0].add(	"atTerminal");
			yLabels[0].add(				"Ia SOL");
			legendLabels[0].add(		"SOL");
			nmMuscles[0].add(			"SOL");
		}
		else if(output.equals("jointAngle")){
			nmSubplots[0].add(			"jointAngle");
			nmCdNeurons[0].add(			"");
			nmCdSpecification[0].add(	"");
			yLabels[0].add(				"Angle");
			legendLabels[0].add(		"Ankle");
			nmMuscles[0].add(			"SOL");
		}
		else if(output.equals("muscleForce")){
			nmSubplots[0].add(			"muscleForce");
			nmCdNeurons[0].add(			"All MUs");
			nmCdSpecification[0].add(	"");
			yLabels[0].add(				"Force SOL");
			legendLabels[0].add(		"SOL");
			nmMuscles[0].add(			"SOL");
		}
		else if(output.equals("jointTorque")){
			nmSubplots[0].add(			"jointTorque");
			nmCdNeurons[0].add(			"");
			nmCdSpecification[0].add(	"");
			yLabels[0].add(				"Torque");
			legendLabels[0].add(		"Ankle");
			nmMuscles[0].add(			"SOL");
		}
		else return;
		
		conf.setNmSubplots(			nmSubplots);
		conf.setNmCdNeurons(		nmCdNeurons);
		conf.setNmCdSpecification(	nmCdSpecification);
		conf.setyLabels(			yLabels);
		conf.setLegendLabels(		legendLabels);
		conf.setNmMuscles(			nmMuscles);
		
		
		ResultDisplay results = new ResultDisplay(conf);
		
		ArrayList outputList = new ArrayList();
		
		results.generateResults(sim, outputList);

		double y = 0;
		double t = 0;
		
		XYSeries xySeries = new XYSeries(output);
		
		File outputFile = null;
	    
		if(test){
			
			outputFile = new File(dataPath + output + simCount + ".txt");
		}
		else{
			
			outputFile = new File(dataPath + path + "\\" + output + simCount + ".txt");
		}
		
	    PrintWriter printWriter = new PrintWriter(outputFile);
	    
	    
		for(int j = 0; j < outputList.size(); j++)
		{
			Point point = (Point)outputList.get(j);
			
			t = point.getX();
			y = point.getY();
			
			Object[] objT = { new Double(t) };
			Object[] objF = { new Double(y) };
			
			printWriter.println(mf.format(objT) + "\t" + mf.format(objF));
			
			xySeries.add( t, Double.valueOf(y) );
		}
		
		printWriter.close();
		
		XYSeriesCollection datasetS = new XYSeriesCollection();
	    datasetS.addSeries( xySeries );

	    if(test){
	    	if(output.equals("spikesMNs") || output.equals("spikesIas")){
		    	PlotScatter.generate(datasetS,
						 graphicPath + output + simCount + ".jpg",
						 output, 
						 "Time [ms]", 
						 output);
		    }
		    else{
		    	PlotXYLine.generate(datasetS,
		    			 graphicPath + output + simCount + ".jpg",
						 output, 
						 "Time [ms]", 
						 output);
		    }
	    }
	    else{
	    	if(output.equals("spikesMNs") || output.equals("spikesIas")){
		    	PlotScatter.generate(datasetS,
						 graphicPath + path + "\\" + output + simCount + ".jpg",
						 output, 
						 "Time [ms]", 
						 output);
		    }
		    else{
		    	PlotXYLine.generate(datasetS,
		    			 graphicPath + path + "\\" + output + simCount + ".jpg",
						 output, 
						 "Time [ms]", 
						 output);
		    }
	    }
	    
	    
	    results = null;
		//sim = null;

		System.gc();
		
	}
	
	public void generateLogOfScenario(int simCount, String dataPath, String path) throws FileNotFoundException{
		
		
		File output;
	    
		if(test){
			output = new File(dataPath + "\\scenario" + simCount + ".txt");
		}
		else{
			output = new File(dataPath + path + "\\scenario" + simCount + ".txt");
		}
		
	    PrintWriter printWriter = new PrintWriter(output);
	    
	    printWriter.println("");
	    printWriter.println("*******************************************");
	    printWriter.println("*********   SIMULATION SCENARIO   *********");
	    printWriter.println("*******************************************");
	    printWriter.println("");
	    
	    printWriter.println("");
	    printWriter.println("");
	    printWriter.println("------CONFIGURATION-------");
	    printWriter.println("");
	    
	    printWriter.println("conf.getCdJoint():             " + conf.getCdJoint());
	    printWriter.println("conf.getCdJointModel():        " + conf.getCdJointModel());
	    printWriter.println("conf.getDecimationFrequency(): " + conf.getDecimationFrequency());
	    printWriter.println("conf.getRecruitmentOrderFES(): " + conf.getRecruitmentOrderFES());
	    printWriter.println("conf.getStep():                " + conf.getStep());
	    printWriter.println("conf.getTFin():                " + conf.getTFin());
	    printWriter.println("conf.isChangedConfiguration(): " + conf.isChangedConfiguration());
	    printWriter.println("conf.isKeepProperties():       " + conf.isKeepProperties());
		
	    printWriter.println("");
	    printWriter.println("");
	    printWriter.println("------NEURONS-------");
	    printWriter.println("");
	    printWriter.println("Only neurons present in the simulation are shown.");
	    printWriter.println("");
	    
    	for(int i = 0; i < conf.getNeuronTypes().size(); i++){
    		NeuronVO neuronVO = (NeuronVO) conf.getNeuronTypes().get(i);
	    	
	    	if(neuronVO.isActive()){
	    		printWriter.println("conf.getNeuronTypes().get(" + i + "): " + "\t" + 
	    				"neuronVO.getCdNucleus(): " 	+ neuronVO.getCdNucleus()  		+ "\t" +
	    				"neuronVO.getCategoryType(): " 	+ neuronVO.getCategoryAndType() 	+ "\t" +
	    				"neuronVO.getQuantity(): " 		+ neuronVO.getQuantity());
	    	}
	    }
    	
    	
    	printWriter.println("");
	    printWriter.println("");
	    printWriter.println("------SYNAPSES-------");
	    printWriter.println("");
	    printWriter.println("Only conductances present in the simulation are shown.");
	    printWriter.println("");
	    
	    
	    List conductances = conf.getAllActiveSynapticConductances();
	    
	    for(int i = 0; i < conductances.size(); i++){
	    	ConductanceVO g = (ConductanceVO) conductances.get(i);
	    	
	    	printWriter.println("g.getCdConductanceType(): " + g.getCdConductanceType() + "\t" +
	    						"g.getGmax(): " + g.getGmax() + "\t" + 
	    						"g.getDynamicType(): " + g.getDynamicType() + "\t" +
	    						"g.getDynamics().getTau(): " + g.getDynamics().getTau() + "\t" +
	    						"g.getDynamics().getVariation(): " + g.getDynamics().getVariation());
	    }
	    
	    
    	printWriter.println("");
	    printWriter.println("");
	    printWriter.println("------NERVES-------");
	    printWriter.println("");
	    printWriter.println("Only nerves present in the simulation are shown.");
	    printWriter.println("");
	    
	    for(int i = 0; i < conf.getAllNerves().size(); i++){
	    	NerveVO nerveVO = (NerveVO) conf.getAllNerves().get(i);
	    	
	    	if(nerveVO.isActive()){
	    		printWriter.println("conf.getAllNerves().get(" + i + "): " + "\t" + 
	    							"nerveVO.getCdNerve(): " + nerveVO.getCdNerve() + "\t" + 
	    							nerveVO.isActive());
	    		printWriter.println("");
		    	printWriter.println("\t"  + ".getAmp():      " 		+ nerveVO.getAmp());
	    		printWriter.println("\t"  + ".getCdJoint():  " 	 	+ nerveVO.getCdJoint());
	    		printWriter.println("\t"  + ".getCdSignal(): " 	 	+ nerveVO.getCdSignal());
	    		printWriter.println("\t"  + ".getDelay():    " 	 	+ nerveVO.getDelay());
	    		printWriter.println("\t"  + ".getFreq():     " 		+ nerveVO.getFreq());
	    		printWriter.println("\t"  + ".getTini():     " 		+ nerveVO.getTini());
	    		printWriter.println("\t"  + ".getTfin():     " 		+ nerveVO.getTfin());
	    		
	    	}
	    }
	    
	    printWriter.println("");
	    printWriter.println("");
	    printWriter.println("------JOINT-------");
	    printWriter.println("");
	    
	    printWriter.println("conf.getCdJoint():                  " + conf.getCdJoint());
	    printWriter.println("conf.getCdJointModel():             " + conf.getCdJointModel());
	    
	    //printWriter.println("conf.getJointStimulusInitialTime(): " + conf.getJointStimulusInitialTime());
	    //printWriter.println("conf.getJointStimulusFinalTime(): 	 " + conf.getJointStimulusFinalTime());
	    
	    //printWriter.println("conf.getJointInitialAngle():        " + conf.getJointInitialAngle());
	    //printWriter.println("conf.getJointFinalAngle():          " + conf.getJointFinalAngle());
	    printWriter.println("conf.getJointVelocity():            " + conf.getJointVelocity());
	    
	    
	    printWriter.println("");
	    printWriter.println("");
	    printWriter.println("------MUSCLE-------");
	    printWriter.println("");
	    
	    printWriter.println("conf.getCdMuscleModel(): " + conf.getCdMuscleModel());
	    
	    printWriter.println("");
	    printWriter.println("");
	    printWriter.println("------EMG-------");
	    printWriter.println("");
	    
	    printWriter.println("conf.getCdEMGModel(): " + conf.getCdEMGModel());
	    
	    printWriter.println("");
	    printWriter.println("");
	    printWriter.println("------MUSCLE SPINDLE-------");
	    printWriter.println("");
	    
	    printWriter.println("conf.getCdSpindleModel():                " 	+ conf.getCdSpindleModel());
	    printWriter.println("conf.getGammaStatic():                   " 	+ conf.getGammaStatic());
	    printWriter.println("conf.getGammaDynamic():                  "  	+ conf.getGammaDynamic());
	    printWriter.println("conf.getBag1Gain():                      " 	+ conf.getPrimaryBag1Gain());
	    printWriter.println("conf.getBag2AndChainGain():              " 	+ conf.getPrimaryBag2AndChainGain());
	    printWriter.println("conf.getInitialRecruitmentThresholdIa(): " 	+ conf.getInitialRecruitmentThresholdIa());
	    printWriter.println("conf.getFinalRecruitmentThresholdIa():   " 	+ conf.getFinalRecruitmentThresholdIa());
	    printWriter.println("conf.getInitialRecruitmentThresholdII(): " 	+ conf.getInitialRecruitmentThresholdII());
	    printWriter.println("conf.getFinalRecruitmentThresholdII():   " 	+ conf.getFinalRecruitmentThresholdII());
	    
	    
    	
	    
	    printWriter.println("");
	    printWriter.println("");
	    printWriter.println("------GOLGI TENDON ORGAN-------");
	    printWriter.println("");
	    
	    printWriter.println("conf.getCdGtoModel():                    "  + conf.getCdGtoModel());
	    printWriter.println("conf.getInitialRecruitmentThresholdIb(): "  + conf.getInitialRecruitmentThresholdIb());
	    printWriter.println("conf.getFinalRecruitmentThresholdIb():   "  + conf.getFinalRecruitmentThresholdIb());
	    
    	
	    
	    printWriter.println("");
	    printWriter.println("");
	    printWriter.println("------MISCELLANEOUS-------");
	    printWriter.println("");
	    
	    for(int i = 0; i < conf.getMiscellaneous().size(); i++){
	    	MiscellaneousVO misc = (MiscellaneousVO) conf.getMiscellaneous().get(i);
	    	printWriter.println("conf.getMiscellaneous().get(" + i + "): " + "\t" + 
	    							misc.getValue() 	+ "\t"  + 
	    							misc.getProperty());
	    	
	    }
	    
	    printWriter.close();
	    
	}

}
