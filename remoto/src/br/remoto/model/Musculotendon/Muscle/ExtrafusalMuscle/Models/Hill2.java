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


public class Hill2 extends ExtrafusalMuscleSuperClass
{
	protected AlphaFunction[] activationFunction;
	private double maximumMuscleForce;
	private double mass;
	protected double massNorm;
	private double forceParallelElement;
	private double forceViscousElement;
	private double forceActiveSType;
	private double forceActiveFType;
	
	private double pennationAngleAtOptimalLength;
	
	private double L_slack_P;
	private double L_0_P;
	private double s_d_P2;
	private double s_k_P;
	private double s_d_P;
	
	double timeNorm;
	double stepNorm;
	
	double k1_l;
	double k2_l;
	double k3_l;
	double k4_l;
	
	double k1_v;
	double k2_v;
	double k3_v;
	double k4_v;
	
	
	public Hill2(Configuration conf, String cdMuscle, MotorUnit[] mu, String cdMuscleModel) {
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
		samplerPennationAngleStore 			= new Sample(conf.getDecimationFrequency(), 1000 / conf.getStep());
		
		stepNorm = conf.getStep() / 1000; // Convert from ms to s
		
		optimalLength = vo.getOptimalLength();
		
		pennationAngleAtOptimalLength = 28.3 * Math.PI / 180;
		//pennationAngleAtOptimalLength = vo.getPennationAngle() * Math.PI / 180;
		
		maximumMuscleForce = vo.getMaximumMuscleForce();
		mass = vo.getMuscleMass();
		
		massNorm = (mass * Math.pow(ReMoto.maximumVelocitySType * optimalLength,2)) / (optimalLength * maximumMuscleForce);
		
		L_slack_P = optimalLength;     	// [m]
		L_0_P = 1.5 * optimalLength;    // [m]
		s_d_P2 = 0.00001;               // [Ns/m] - Viscosity coef.
		s_k_P = 4.5;                    // [1/m] - Elastic coef.
		s_d_P = 1.28;                   // [Ns/m] - Viscosity coef.
		
	}
	
	
	public void setInitialLengthNorm(){
		lengthNorm = 1.03518;
	}
		
	private double motorUnitSaturationFunction(double shapeFactor){
		
		return 2 / (1 + Math.exp(-shapeFactor)) -1;
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
       
        double activation = activationFunction[mu.getIndex() - 1].getValue(iteration);
        
        return mu.getTetanicForce() * motorUnitSaturationFunction(activation * mu.getB());
        
	}
	
	protected void calculateActivation(double t){
		
		double outputI = 0;
		double outputII = 0;
		double maximumForce = 0;
		
		for(int n = 0; n < motorunits.length; n++)
		{
			maximumForce += motorunits[n].getTetanicForce();
			
			if(motorunits[n].getType().equals(ReMoto.S))
				outputI += instantMotorUnitActivation(motorunits[n].getCd(), t);
			else outputII += instantMotorUnitActivation(motorunits[n].getCd(), t);
			
		}
		
		activationNormSType = outputI / maximumForce;
		activationNormFType = outputII / maximumForce;
		
		//activationNormSType = 0;
		//activationNormFType = 0;
		
		
		activationNorm = activationNormSType + activationNormFType;
		
		super.atualizeActivation(t);
		
	}
	
	/*
	public double calculateLengthNorm(){
		
		double k1_T = - shorteningVelocity;
		double k2_T = - (shorteningVelocity + (stepNorm/2) * k1_T);
		double k3_T = - (shorteningVelocity + (stepNorm/2) * k2_T);
		double k4_T = - (shorteningVelocity + stepNorm * k3_T);
		
		lengthNorm =  lengthNorm + (stepNorm * (k1_T + 2*k2_T + 2*k3_T + k4_T)/6);
		return lengthNorm;
		
	}
	*/
	
	protected double calculatePennationAngle(double muscleLengthNorm){
		
		return Math.asin(Math.sin(pennationAngleAtOptimalLength) / muscleLengthNorm);
	}
	
	public void atualize(double t)
	{
		
		timeNorm = t / 1000; // Convert from ms to s
		
		calculateActivation(t);
		
		pennationAngle = calculatePennationAngle(lengthNorm); 	// [rad]
		
		//calculateLengthNorm();
		//calculateVelocity(timeNorm);
		
		calculateVelocityAndLength(timeNorm);
		
		length = lengthNorm * optimalLength;
		
		stretchVelocity = - shorteningVelocity  ;
		stretchAcceleration = - shorteningAcceleration ;
		
		samplerLengthStore.sample(lengthStore, "muscleLength", t, length);
		samplerLengthNormStore.sample(lengthNormStore, "muscleLengthNorm", t, lengthNorm);
		samplerVelocityStore.sample(velocityStore, "muscleVelocity", t, stretchVelocity);
		samplerAccelerationStore.sample(accelerationStore, "muscleAcceleration", t, stretchAcceleration);
		samplerForceParallelElementStore.sample(forceParallelElementStore, "muscleForceParallelElement", t, forceParallelElement);
		samplerForceViscousElementStore.sample(forceViscousElementStore, "muscleForceViscousElement", t, forceViscousElement);
		samplerForceActiveSTypeStore.sample(forceActiveSTypeStore, "forceActiveSType", t, forceActiveSType);
		samplerForceActiveFTypeStore.sample(forceActiveFTypeStore, "forceActiveFType", t, forceActiveFType);
		samplerPennationAngleStore.sample(pennationAngleStore, "pennationAngle", t, pennationAngle * 180 / Math.PI);
		
		force = instantMuscleForce(t);
		samplerForceStore.sample(forceStore, "muscleForce", t, force);
	}
	

	private double calculateForceParallelElement(double lengthNorm){
		
		forceParallelElement =  Math.exp(s_k_P * ((lengthNorm * optimalLength - L_slack_P) / (L_0_P - L_slack_P)))/Math.exp(s_k_P);
		
		return forceParallelElement;
	}
	
	private double calculateForceViscousElement(double shorteningVelocity, double lengthNorm){
		forceViscousElement = -shorteningVelocity * 
								(s_d_P2 + s_d_P * Math.exp(s_k_P * ((lengthNorm * optimalLength - L_slack_P) / (L_0_P - L_slack_P)))/Math.exp(s_k_P));
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
		double velocity = -shorteningVelocity;
		
		if(velocity > 0){ // eccentric contraction
			if(fiberType.equals(ReMoto.S)){
				num = ReMoto.bv_SType - 
						(ReMoto.av0_SType + ReMoto.av1_SType * lengthNorm + ReMoto.av2_SType * Math.pow(lengthNorm, 2)) *
						velocity;
				den = ReMoto.bv_SType + velocity;
			}
			else if(fiberType.equals(ReMoto.F)){
				num = ReMoto.bv_FType -
				(ReMoto.av0_FType + ReMoto.av1_FType * lengthNorm + ReMoto.av2_FType * Math.pow(lengthNorm, 2)) *
				velocity;
				den = ReMoto.bv_FType + velocity;
			}
		}
		else{ // concentric contraction
			if(fiberType.equals(ReMoto.S)){
				num = ReMoto.maximumVelocitySType - velocity;
				den = ReMoto.maximumVelocitySType + (velocity * (ReMoto.cv0_SType + ReMoto.cv1_SType * lengthNorm));
			}
			else if(fiberType.equals(ReMoto.F)){
				num = ReMoto.maximumVelocityFType - velocity;
				den = ReMoto.maximumVelocityFType + (velocity * (ReMoto.cv0_FType + ReMoto.cv1_FType * lengthNorm));
			}
		}
		
		return num/den;
		
	}
	
	
	public double calculateAcceleration(double t, double lengthNorm, double shorteningVelocity){
		
		forceNorm = calculateForceParallelElement(lengthNorm) + 
		calculateForceViscousElement(shorteningVelocity, lengthNorm) + 
		calculateForceActiveSType(t, lengthNorm, shorteningVelocity) + 
		calculateForceActiveFType(t, lengthNorm, shorteningVelocity);
		
		//shorteningAcceleration = (forceNorm  * Math.cos(pennationAngle) - associatedMusculotendon.getTendon().getForceNorm()) / (massNorm * Math.cos(pennationAngle));
		shorteningAcceleration = (forceNorm  * Math.cos(pennationAngle)* Math.cos(pennationAngle) - associatedMusculotendon.getTendon().getForceNorm() * Math.cos(pennationAngle)) / massNorm;
		
		return shorteningAcceleration;
	}
	
	/*
	public double calculateVelocity(double t){ 
		
		
		double k1_T = DvDt(t				, lengthNorm 						, shorteningVelocity);
		double k2_T = DvDt(t + stepNorm/2	, lengthNorm + (stepNorm/2) * k1_T	, shorteningVelocity  + (stepNorm/2) * k1_T);
		double k3_T = DvDt(t + stepNorm/2	, lengthNorm + (stepNorm/2) * k2_T	, shorteningVelocity  + (stepNorm/2) * k2_T);
		double k4_T = DvDt(t + stepNorm		, lengthNorm + 	stepNorm * k3_T		, shorteningVelocity  + 	stepNorm * k3_T);
		
		shorteningVelocity =  shorteningVelocity + (stepNorm * (k1_T + 2*k2_T + 2*k3_T + k4_T)/6);
		
		return shorteningVelocity;
	}
	*/
	
	public void calculateVelocityAndLength(double t){ 
		
		k1_l = - shorteningVelocity;
		
		k1_v = DvDt(t				, lengthNorm 						, shorteningVelocity);
		
		k2_l = - (shorteningVelocity + (stepNorm/2) * k1_v);
		
		k2_v = DvDt(t + stepNorm/2	, lengthNorm + (stepNorm/2) * k1_l	, shorteningVelocity  + (stepNorm/2) * k1_v);
		
		k3_l = - (shorteningVelocity + (stepNorm/2) * k2_v);
		
		k3_v = DvDt(t + stepNorm/2	, lengthNorm + (stepNorm/2) * k2_l	, shorteningVelocity  + (stepNorm/2) * k2_v);
		
		k4_l = - (shorteningVelocity + stepNorm * k3_v);
		
		k4_v = DvDt(t + stepNorm		, lengthNorm + 	stepNorm * k3_l		, shorteningVelocity  + 	stepNorm * k3_v);
		
		lengthNorm =  lengthNorm + (stepNorm * (k1_l + 2*k2_l + 2*k3_l + k4_l)/6);
		
		shorteningVelocity =  shorteningVelocity + (stepNorm * (k1_v + 2*k2_v + 2*k3_v + k4_v)/6);
		
	}
	
	
	private double DvDt(double t, double lengthNorm, double shorteningVelocity){
		return calculateAcceleration(t, lengthNorm, shorteningVelocity);
	}
	
	
	
	
	@Override
	public double instantMuscleForce(double t) {
		force = forceNorm * maximumMuscleForce;
		//force = forceNorm;
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
	
}

