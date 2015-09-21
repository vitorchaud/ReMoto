package br.remoto.model.Musculotendon.Muscle.ExtrafusalMuscle.Models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

import br.remoto.model.Configuration;
import br.remoto.model.MotorUnit;
import br.remoto.model.ReMoto;
import br.remoto.util.Conversion;
import br.remoto.util.Sample;
import br.remoto.util.Signal;
import br.remoto.model.Conductance.AlphaFunction;
import br.remoto.model.Musculotendon.MusculotendonSuperClass;
import br.remoto.model.Musculotendon.Muscle.ExtrafusalMuscle.ExtrafusalMuscleSuperClass;
import br.remoto.model.Musculotendon.Tendon.NonInnerveted.NonInnervatedTendon;
import br.remoto.model.vo.MuscleVO;


public class Hill extends ExtrafusalMuscleSuperClass
{

	
	protected AlphaFunction[] activationFunction;
	private double maximumMuscleForce;
	private double mass;
	protected double massNorm;
	private double elasticCoeficientOfParallelElementNorm;
	private double viscosityCoeficientNorm;
	private double forceParallelElement;
	private double forceViscousElement;
	private double forceActiveSType;
	private double forceActiveFType;
	private double stepNorm;
	
	public Hill(Configuration conf, String cdMuscle, MotorUnit[] mu, String cdMuscleModel) {
		super(conf, cdMuscle, mu, cdMuscleModel);
		
		this.activationFunction = new AlphaFunction[motorunits.length];
		
		for(int i = 0; i < motorunits.length; i++){
			activationFunction[i] = new AlphaFunction(null, null, null);
		}
		
		MuscleVO vo = conf.getMuscle(cdMuscle, "hill");
		
		samplerLengthStore 					= new Sample(conf.getDecimationFrequency(), 1000 / conf.getStep());
		samplerLengthNormStore 				= new Sample(conf.getDecimationFrequency(), 1000 / conf.getStep());
		samplerVelocityStore 				= new Sample(conf.getDecimationFrequency(), 1000 / conf.getStep());
		samplerAccelerationStore 			= new Sample(conf.getDecimationFrequency(), 1000 / conf.getStep());
		samplerForceParallelElementStore 	= new Sample(conf.getDecimationFrequency(), 1000 / conf.getStep());
		samplerForceViscousElementStore 	= new Sample(conf.getDecimationFrequency(), 1000 / conf.getStep());
		samplerForceActiveSTypeStore 		= new Sample(conf.getDecimationFrequency(), 1000 / conf.getStep());
		samplerForceActiveFTypeStore 		= new Sample(conf.getDecimationFrequency(), 1000 / conf.getStep());
		samplerForceStore 					= new Sample(conf.getDecimationFrequency(), 1000 / conf.getStep());
		
		
		optimalLength = vo.getOptimalLength();
		//viscosityCoeficient = vo.getViscosityCoeficient();
		//elasticCoeficientOfParallelElement = vo.getElasticCoeficientOfParallelElement();
		
		c_T = vo.getC_T();
		k_T = vo.getK_T();
		Lr_T = vo.getLr_T();
		
		pennationAngle = vo.getPennationAngle() * Math.PI / 180; 	// [rad]
		slackTendonLength = vo.getSlackTendonLength();
		maximumMuscleForce = vo.getMaximumMuscleForce();
		mass = vo.getMuscleMass();
		
		//elasticCoeficientOfParallelElementNorm = elasticCoeficientOfParallelElement * optimalLength / maximumMuscleForce;
		//viscosityCoeficientNorm = viscosityCoeficient * optimalLength / (maximumMuscleForce * ReMoto.timeScale);
		//massNorm = (mass * optimalLength) / (maximumMuscleForce * Math.pow(ReMoto.timeScale, 2));
		
		//stepNorm = Conversion.convertMillisecondsToSeconds(conf.getStep()) / ReMoto.timeScale;
		
		//System.out.println("cdMuscle: " + cdMuscle);
		//System.out.println("optimalLength: " + optimalLength);
		//System.out.println("viscosityCoeficient: " + viscosityCoeficient);
		//System.out.println("elasticCoeficientOfParallelElement: " + elasticCoeficientOfParallelElement);
		//System.out.println("elasticCoeficientOfSeriesElement: " + elasticCoeficientOfSeriesElement);
		//System.out.println("pennationAngle: " + pennationAngle + " rad");
		//System.out.println("slackTendonLength: " + slackTendonLength);
		//System.out.println("maximumMuscleForce: " + maximumMuscleForce);
		//System.out.println("mass: " + mass);
		
		//System.out.println("elasticCoeficientOfParallelElementNorm: " + elasticCoeficientOfParallelElementNorm);
		//System.out.println("viscosityCoeficientNorm: " + viscosityCoeficientNorm);
		//System.out.println("massNorm: " + massNorm);
		
		//System.out.println("Creating Hill muscle");
		//System.out.println("maximumMuscleForce: " + maximumMuscleForce);
	}
	/*
	
	public void setInitialLengthNorm(){
		lengthNorm = 1.0;
		lengthNormAtEquilibrium = 1.0;
	}
		
	private double motorUnitForceSaturationFunction(double force, String muType, double b){
		
		double aux = 0;
		
		if(muType.equals(ReMoto.S))
			aux = 2 / (1 + Math.exp(-b * force)) -1;
		else if(muType.equals(ReMoto.FR))
			aux = 2 / (1 + Math.exp(-b * force)) -1;
		else if(muType.equals(ReMoto.FF))
			aux = 2 / (1 + Math.exp(-b * force)) -1;
		
		return aux;
	}	
	
	public double instantMotorUnitActivation(String cdNeuron, double t)
	{
		// Pick mu in hashtable 
		MotorUnit mu = pickMotorUnit(cdNeuron);
		
        if( mu == null )
        	return 0;
        
        if(!activationFunction[mu.getIndex() - 1].isStarted()){
        	activationFunction[mu.getIndex() - 1].setGmax(mu.getGmax());
        	activationFunction[mu.getIndex() - 1].setTpeak(mu.getTpeak());
        	activationFunction[mu.getIndex() - 1].reset( mu.getMiscellaneous() );
        }
        
        if( mu.getNumberOfSpikesAtEndPlate() == 0 )
			return 0;
        
        int indexSpike;
    	int iteration;
    	double tSpike;
    	
    	indexSpike = mu.getIndexSpike();
    	iteration = mu.getIteration();
    	tSpike = mu.gettSpike();
    	
    	
    	if(indexSpike < mu.getNumberOfSpikesAtEndPlate() ){
    		
    		tSpike = mu.getEndPlateSpike(indexSpike);
    		mu.settSpike(tSpike);
    		
    		if( t > tSpike ){
    			
            	mu.setIndexSpike(indexSpike + 1);
            	indexSpike = mu.getIndexSpike();
            	
            	activationFunction[mu.getIndex() - 1].start(iteration);
    			
    		}
    		
    	}
    	
        mu.setIteration(iteration + 1);
        iteration = mu.getIteration();
       
        double force = activationFunction[mu.getIndex() - 1].getValue(iteration);
        
        double output = 0;
        
        if(mu.getType().equals(ReMoto.S))
        	output = motorUnitForceSaturationFunction(force, mu.getType(), mu.getB()) * mu.getTwTet() * mu.getGpeak();
        else if(mu.getType().equals(ReMoto.FR))
        	output = motorUnitForceSaturationFunction(force, mu.getType(), mu.getB()) * mu.getTwTet() * mu.getGpeak();
        else if(mu.getType().equals(ReMoto.FF))
        	output = motorUnitForceSaturationFunction(force, mu.getType(), mu.getB()) * mu.getTwTet() * mu.getGpeak();
        else
        	output = 0;
               
        return output;
	}
	
	protected void calculateActivation(double t){
		
		double outputI = 0;
		double outputII = 0;
		double maximumForce = 0;
		
		for(int n = 0; n < motorunits.length; n++)
		{
			maximumForce += motorunits[n].getGpeak() * motorunits[n].getTwTet();
			
			if(motorunits[n].getType().equals(ReMoto.S))
				outputI += instantMotorUnitActivation(motorunits[n].getCd(), t);
			else outputII += instantMotorUnitActivation(motorunits[n].getCd(), t);
			
		}
		
		activationNormSType = outputI / maximumForce;
		activationNormFType = outputII / maximumForce;
		
		//activationNormSType = 0.1;
		//activationNormFType = 0;
		
		activationNorm = activationNormSType + activationNormFType;
		
		super.atualizeActivation(t);
		
	}
	
	
	public double calculateLengthNorm(){
		
		double k1_T = - shorteningVelocity;
		double k2_T = - (shorteningVelocity + (stepNorm/2) * k1_T);
		double k3_T = - (shorteningVelocity + (stepNorm/2) * k2_T);
		double k4_T = - (shorteningVelocity + stepNorm * k3_T);
		
		lengthNorm =  lengthNorm + (stepNorm * (k1_T + 2*k2_T + 2*k3_T + k4_T)/6);
		return lengthNorm;
		
	}
	
	public void atualize(double t)
	{
		double timeNorm = Conversion.convertMillisecondsToSeconds(t) / ReMoto.timeScale; 
		
		calculateActivation(t);
		
		calculateLengthNorm();
		calculateVelocity(timeNorm);
		calculateForce();
		
		length = lengthNorm * optimalLength;
		
		stretchVelocity = - shorteningVelocity * optimalLength / ReMoto.timeScale;
		stretchAcceleration = - shorteningAcceleration * optimalLength / Math.pow(ReMoto.timeScale, 2);
		
		//lengthStore.add( new Signal( ReMoto.muscleLengthNorm, length, t) );
		
		//lengthNormStore.add( new Signal( ReMoto.muscleLengthNorm, lengthNorm, t) );
		//velocityStore.add(new Signal( ReMoto.muscleVelocity, stretchVelocity, t) );
		//accelerationStore.add( new Signal( ReMoto.muscleAcceleration, stretchAcceleration, t) );
		
		//forceParallelElementStore.add( new Signal( ReMoto.forceParallelElement, forceParallelElement, t) );
		//forceViscousElementStore.add( new Signal( ReMoto.forceViscousElement, forceViscousElement, t) );
		
		//forceActiveSTypeStore.add( new Signal( ReMoto.forceActiveSType, forceActiveSType, t) );
		//forceActiveFTypeStore.add( new Signal( ReMoto.forceActiveFType, forceActiveFType, t) );
		
		samplerLengthStore.sample(lengthStore, t, length);
		samplerLengthNormStore.sample(lengthNormStore, t, lengthNorm);
		samplerVelocityStore.sample(velocityStore, t, stretchVelocity);
		samplerAccelerationStore.sample(accelerationStore, t, stretchAcceleration);
		samplerForceParallelElementStore.sample(forceParallelElementStore, t, forceParallelElement);
		samplerForceViscousElementStore.sample(forceViscousElementStore, t, forceViscousElement);
		samplerForceActiveSTypeStore.sample(forceActiveSTypeStore, t, forceActiveSType);
		samplerForceActiveFTypeStore.sample(forceActiveFTypeStore, t, forceActiveFType);
		
		force = instantMuscleForce(t);
		
		//forceStore.add( new Signal( ReMoto.force, force, t) );
		samplerForceStore.sample(forceStore, t, force);
		
	}
	

	private double calculateForceParallelElement(double lengthNorm){
		
		forceParallelElement = elasticCoeficientOfParallelElementNorm * (lengthNorm - lengthNormAtEquilibrium);
		return forceParallelElement;
	}
	
	private double calculateForceViscousElement(double shorteningVelocity){
		forceViscousElement = - viscosityCoeficientNorm * shorteningVelocity;
		return forceViscousElement;
	}
	
	
	private double calculateForceActiveSType(double t, double lengthNorm, double shorteningVelocity){
		forceActiveSType = activationNormSType * calculateForceLengthRelationship(lengthNorm, ReMoto.S) * calculateForceVelocityRelationship(lengthNorm, shorteningVelocity, ReMoto.S);
		return forceActiveSType;
	}
	
	private double calculateForceActiveFType(double t, double lengthNorm, double shorteningVelocity){
		
		double aux1 = activationNormFType;
		double aux2 = calculateForceLengthRelationship(lengthNorm, ReMoto.F);
		double aux3 = calculateForceVelocityRelationship(lengthNorm, shorteningVelocity, ReMoto.F);
		
		forceActiveFType = aux1 * aux2 * aux3;
		
		return forceActiveFType;
	}
	
	
	private double calculateForceLengthRelationship(double lengthNorm, String fiberType){
		
		
		double output = 0;
	
		if(fiberType.equals(ReMoto.S)){
			output = Math.exp(-Math.pow(Math.abs((Math.pow(lengthNorm, ReMoto.B_SType) - 1) / ReMoto.w_SType), ReMoto.p_SType));
		}
		else if(fiberType.equals(ReMoto.F)){
			output = Math.exp(-Math.pow(Math.abs((Math.pow(lengthNorm, ReMoto.B_FType) - 1) / ReMoto.w_FType), ReMoto.p_FType));
		}
		
		return output;
	}
	
	
	
	private double calculateForceVelocityRelationship(double lengthNorm, double shorteningVelocity, String fiberType){
		
		double num = 0;
		double den = 0;
		
		if(shorteningVelocity < 0){
			if(fiberType.equals(ReMoto.S)){
				num = ReMoto.bv_SType * ReMoto.timeScale + 
						(ReMoto.av0_SType + ReMoto.av1_SType * lengthNorm + ReMoto.av2_SType * Math.pow(lengthNorm, 2)) *
						shorteningVelocity;
				den = ReMoto.bv_SType * ReMoto.timeScale - shorteningVelocity;
			}
			else if(fiberType.equals(ReMoto.F)){
				num = ReMoto.bv_FType * ReMoto.timeScale + 
				(ReMoto.av0_FType + ReMoto.av1_FType * lengthNorm + ReMoto.av2_FType * Math.pow(lengthNorm, 2)) *
				shorteningVelocity;
				den = ReMoto.bv_FType * ReMoto.timeScale - shorteningVelocity;
			}
		}
		else{
			if(fiberType.equals(ReMoto.S)){
				num = ReMoto.maximumVelocitySType * ReMoto.timeScale + shorteningVelocity;
				den = ReMoto.maximumVelocitySType * ReMoto.timeScale - (shorteningVelocity * (ReMoto.cv0_SType + ReMoto.cv1_SType * lengthNorm));
			}
			else if(fiberType.equals(ReMoto.F)){
				num = ReMoto.maximumVelocityFType * ReMoto.timeScale + shorteningVelocity;
				den = ReMoto.maximumVelocityFType * ReMoto.timeScale - (shorteningVelocity * (ReMoto.cv0_FType + ReMoto.cv1_FType * lengthNorm));
			}
		}
		
		return num/den;
		
	}
	
	
	public double calculateAcceleration(double t, double lengthNorm, double shorteningVelocity){
		
		double aux = calculateForceParallelElement(lengthNorm) + 
		calculateForceViscousElement(shorteningVelocity) + 
		calculateForceActiveSType(t, lengthNorm, shorteningVelocity) + 
		calculateForceActiveFType(t, lengthNorm, shorteningVelocity);
		
		shorteningAcceleration = (Math.cos(pennationAngle) * aux - associatedMusculotendon.getTendon().getForceNorm()) / (massNorm * Math.cos(pennationAngle));
		
		return shorteningAcceleration;
	}
	
	
	public double calculateVelocity(double t){ 
		
		
		double k1_T = DvDt(t				, lengthNorm 						, shorteningVelocity);
		double k2_T = DvDt(t + stepNorm/2	, lengthNorm + (stepNorm/2) * k1_T	, shorteningVelocity  + (stepNorm/2) * k1_T);
		double k3_T = DvDt(t + stepNorm/2	, lengthNorm + (stepNorm/2) * k2_T	, shorteningVelocity  + (stepNorm/2) * k2_T);
		double k4_T = DvDt(t + stepNorm		, lengthNorm + stepNorm * k3_T		, shorteningVelocity  + stepNorm * k3_T);
		
		shorteningVelocity =  shorteningVelocity + (stepNorm * (k1_T + 2*k2_T + 2*k3_T + k4_T)/6);
		
		return shorteningVelocity;
	}
	
	private double DvDt(double t, double lengthNorm, double shorteningVelocity){
		return calculateAcceleration(t, lengthNorm, shorteningVelocity);
	}
	
	
	public double calculateForce(){
		
		forceNorm = associatedMusculotendon.getTendon().getForceNorm() / Math.cos(pennationAngle);
		return forceNorm;
		
	}
	
	
	@Override
	public double instantMuscleForce(double t) {
		force = forceNorm * maximumMuscleForce;
		return force;
	}

	@Override
	public double instantMotorUnitForce(String cdNeuron, double t) {
		// TODO Auto-generated method stub
		return 0;
	}

	public double getMaximumMuscleForce() {
		return maximumMuscleForce;
	}
	
	public void setMaximumMuscleForce(double maximumMuscleForce) {
		this.maximumMuscleForce = maximumMuscleForce;
	}

	public double getMass() {
		return mass;
	}

	public void setMass(double mass) {
		this.mass = mass;
	}
	*/

	@Override
	public void setInitialLengthNorm() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double instantMuscleForce(double t) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double instantMotorUnitForce(String cdNeuron, double t) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void atualize(double t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getMaximumMuscleForce() {
		// TODO Auto-generated method stub
		return 0;
	}
}

