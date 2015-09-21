package br.remoto.model.Musculotendon.Muscle.ExtrafusalMuscle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

import br.remoto.model.Configuration;
import br.remoto.model.ModulatingSignal;
import br.remoto.model.MotorUnit;
import br.remoto.model.ReMoto;
import br.remoto.util.Conversion;
import br.remoto.util.Sample;
import br.remoto.util.Signal;
import br.remoto.model.Musculotendon.MusculotendonSuperClass;
import br.remoto.model.Musculotendon.Muscle.MuscleSuperClass;


public abstract class ExtrafusalMuscleSuperClass extends MuscleSuperClass
{
	protected MotorUnit motorunits[];
	protected Hashtable mn_mu = new Hashtable(); 
	
	protected ArrayList forceStore = new ArrayList();
	protected ArrayList motorUnitForceStore = new ArrayList();
	
	protected double optimalLength;
	
	//protected double viscosityCoeficient;
	//protected double elasticCoeficientOfParallelElement;
	protected double c_T;
    protected double k_T;
    protected double Lr_T;
    
	protected double slackTendonLength;
	
	
	protected double activationNormSType;
	protected double activationNormFType;
	
	protected ArrayList STypeMuscleActivationStore = new ArrayList();
	protected ArrayList FTypeMuscleActivationStore = new ArrayList();
	
	//protected double timeScale;
	
	protected String muscleModel;

	protected MusculotendonSuperClass associatedMusculotendon;
	
	
	public ExtrafusalMuscleSuperClass(Configuration conf, String cdMuscle, MotorUnit[] mu, String cdMuscleModel) {
		super(conf, cdMuscle);
		this.muscleModel = cdMuscleModel;
		this.motorunits = mu;
	}
	
	public abstract void setInitialLengthNorm();
	
	public void reset(double sample, boolean emgAttenuation)
	{
		for(int n = 0; n < motorunits.length; n++)
		{
			motorunits[n].reset( sample, emgAttenuation );
		}
		
		
	}

	
	public double instantMuscleEMG(double t)
	{
		double output = 0;
		
		for(int n = 0; n < motorunits.length; n++)
		{
			output += motorunits[n].getEMGSignal(t);
		}

		return output;
	}
	
	public abstract double instantMuscleForce(double t);
	
	public abstract double instantMotorUnitForce(String cdNeuron, double t);
	
	public double instantMotorUnitEMG(String cdNeuron, double t)
	{
		// Pick mu in hashtable 
		MotorUnit mu = pickMotorUnit(cdNeuron);

        if( mu == null )
        	return 0;
        
		return mu.getEMGSignal(t);
	}
	
	public abstract void atualize(double t);
	
	protected void atualizeActivation(double t){
		super.atualizeActivation(t);
		//STypeMuscleActivationStore.add( new Signal( ReMoto.activationNormSType, activationNormSType, t) );
		//FTypeMuscleActivationStore.add( new Signal( ReMoto.activationNormFType, activationNormFType, t) );
		
		samplerSTypeMuscleActivationStore.sample(STypeMuscleActivationStore, "STypeMuscleActivation", t, activationNormSType);
		samplerFTypeMuscleActivationStore.sample(FTypeMuscleActivationStore, "FTypeMuscleActivation", t, activationNormFType);
	}
	
	public double calculateLengthNormAux(double force, double t) {
		
		/*
		ModulatingSignal signal = new ModulatingSignal();
		
		signal.setCdSignal(ReMoto.sine);
		signal.setTini(500);
		signal.setTfin(510);
		signal.setAmp(0.2);
		signal.setFreq(100);
		//signal.setWidth(100);
		signal.setDelay(0);
	    */
		
		
		ModulatingSignal signal = new ModulatingSignal();
		
		signal.setCdSignal(ReMoto.ramp);
		signal.setTini(1000);
		signal.setTfin(3000);
		signal.setAmp(0.13);
		//signal.setFreq(1);
		signal.setWidth(1200);
		//signal.setDelay(1200);
		
		
		
	    /*
		//System.out.println("DEBUG: " + "   force: "  + force);
		double aux;
		
		if(t < 1000) aux = 0.95;
		else if ( t < 2200) aux = 0.95 + 0.00011 * (t - 1000);
		else aux = 1.082;
		return aux;
		*/
		
		
		//return 1 + signal.value(t);
		
		return 0.95;

		//return 1.1 - force/777777;
	}
	
	
	
	
	public MotorUnit pickMotorUnit(String cdNeuron)
	{
		// Try to pick fiber in hashtable 
		MotorUnit mu = (MotorUnit)mn_mu.get(cdNeuron);
		
		if( mu == null )
		{
			for(int n = 0; n < motorunits.length; n++)
	        {
	        	String cd = motorunits[n].getCd();

	        	if( cdNeuron.equals( cd ) )
	        	{
	        		mu = motorunits[n];

	        		mn_mu.put(cdNeuron, mu);
	        		
	        		break;
	        	}
	        }
		}
		
		return mu;
	}

	
	public MotorUnit[] getMotorunits() {
		return motorunits;
	}
	
	public void setFb(MotorUnit[] mu) {
		this.motorunits = mu;
	}


	public double getOptimalLength() {
		return optimalLength;
	}


	public void setOptimalLength(double optimalLength) {
		this.optimalLength = optimalLength;
	}

	/*
	public double getViscosityCoeficient() {
		return viscosityCoeficient;
	}


	public void setViscosityCoeficient(double viscosityCoeficient) {
		this.viscosityCoeficient = viscosityCoeficient;
	}


	public double getElasticCoeficientOfParallelElement() {
		return elasticCoeficientOfParallelElement;
	}


	public void setElasticCoeficientOfParallelElement(
			double elasticCoeficientOfParallelElement) {
		this.elasticCoeficientOfParallelElement = elasticCoeficientOfParallelElement;
	}
	*/

	public ArrayList getForceStore() {
		return forceStore;
	}


	public void setForceStore(ArrayList forceStore) {
		this.forceStore = forceStore;
	}


	public ArrayList getMotorUnitForceStore(String cdNeuron) {
		
		MotorUnit mu = pickMotorUnit(cdNeuron);
		ArrayList aux = new ArrayList();
		Signal signal;
		
		for(int i = 0; i < motorUnitForceStore.size(); i++){
			signal = (Signal) motorUnitForceStore.get(i);
			
			if(signal.getType().equals(String.valueOf(mu.getIndex()))){
				aux.add(signal);
			}
		}
		
		return aux;
	}


	public void setMotorUnitForceStore(ArrayList motorUnitForceStore) {
		this.motorUnitForceStore = motorUnitForceStore;
	}


	public double getActivationNormSType() {
		return activationNormSType;
	}


	public void setActivationNormSType(double activationNormSType) {
		this.activationNormSType = activationNormSType;
	}


	public double getActivationNormFType() {
		return activationNormFType;
	}


	public void setActivationNormFType(double activationNormFType) {
		this.activationNormFType = activationNormFType;
	}


	public ArrayList getSTypeMuscleActivationStore() {
		return STypeMuscleActivationStore;
	}


	public void setSTypeMuscleActivationStore(ArrayList sTypeMuscleActivationStore) {
		STypeMuscleActivationStore = sTypeMuscleActivationStore;
	}


	public ArrayList getFTypeMuscleActivationStore() {
		return FTypeMuscleActivationStore;
	}


	public void setFTypeMuscleActivationStore(ArrayList fTypeMuscleActivationStore) {
		FTypeMuscleActivationStore = fTypeMuscleActivationStore;
	}
	

	public void setAssociatedMusculotendon(
			MusculotendonSuperClass associatedMusculotendon) {
		this.associatedMusculotendon = associatedMusculotendon;
	}

	/*
	public double getTimeScale() {
		return timeScale;
	}


	public void setTimeScale(double timeScale) {
		this.timeScale = timeScale;
	}
	*/
	
	public abstract double getMaximumMuscleForce();

	public MusculotendonSuperClass getAssociatedMusculotendon() {
		return associatedMusculotendon;
	}
	
	
	//public abstract void setParameters(String cdNucleus, ArrayList muscles);

	public String getMuscleModel() {
		return muscleModel;
	}

	public void setMuscleModel(String muscleModel) {
		this.muscleModel = muscleModel;
	}

	public double getSlackTendonLength() {
		return slackTendonLength;
	}

	public void setSlackTendonLength(double slackTendonLength) {
		this.slackTendonLength = slackTendonLength;
	}

	public double getC_T() {
		return c_T;
	}

	public void setC_T(double c_T) {
		this.c_T = c_T;
	}

	public double getK_T() {
		return k_T;
	}

	public void setK_T(double k_T) {
		this.k_T = k_T;
	}

	public double getLr_T() {
		return Lr_T;
	}

	public void setLr_T(double lr_T) {
		Lr_T = lr_T;
	}
	
	
	
}