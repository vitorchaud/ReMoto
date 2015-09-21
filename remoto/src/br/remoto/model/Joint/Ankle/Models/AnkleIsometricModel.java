package br.remoto.model.Joint.Ankle.Models;


import java.util.ArrayList;

import br.remoto.model.Configuration;
import br.remoto.model.ModulatingSignal;
import br.remoto.model.Joint.Ankle.AnkleSuperClass;
//import br.remoto.model.Musculotendon.Muscle.Muscle;
import br.remoto.model.Musculotendon.MusculotendonSuperClass;
import br.remoto.model.Neuron.Miscellaneous;
import br.remoto.util.ButterworthBilinear;
import br.remoto.util.ButterworthImpulseInvariance;
import br.remoto.util.Sample;
import br.remoto.util.Signal;


public class AnkleIsometricModel extends AnkleSuperClass
{
	private static final long serialVersionUID = 1L;
	
	private double angleArray[];
	int iteration;
	private ButterworthBilinear butterBilinear;
	
	double scaleFactorDorsiflexors;
	double scaleFactorPlantarflexors;
	
	MusculotendonSuperClass[] musculotendons;
	Configuration conf;
	
	public AnkleIsometricModel(Configuration conf, MusculotendonSuperClass[] musculotendons){
		super(conf.getJointVO());
		
		this.conf = conf;
		this.musculotendons = musculotendons;
		
		samplerJointAngleStore = new Sample(conf.getDecimationFrequency(), 1000 / conf.getStep());
		samplerJointTorqueStore = new Sample(conf.getDecimationFrequency(), 1000 / conf.getStep());
		
		scaleFactorDorsiflexors = 2;
		scaleFactorPlantarflexors = 1;
		
		iteration = 0;
		
		butterBilinear = new ButterworthBilinear();
		
		angleArray = new double[(int) (conf.getTFin() / conf.getStep())];
		
		for(int i = 0; i < angleArray.length; i++){
			
			double t = i * conf.getStep();
			
			/*
			ModulatingSignal signal = new ModulatingSignal();
			
			double amp = conf.getJointFinalAngle() - conf.getJointInitialAngle();
			
			double width = amp * 1000 / conf.getJointVelocity();
			
		    signal.setCdSignal("ramp");
			signal.setAmp(amp);
			signal.setTini(conf.getJointStimulusInitialTime());
			signal.setTfin(conf.getJointStimulusFinalTime());
			signal.setWidth(width);
			
			angleArray[i] = 0 + signal.value(t);
			*/
			angleArray[i] = 0;
		}
		
		kneeAngle = conf.getKneeAngle();
		
		//angleArray = butterBilinear.lowPass(angleArray, 100, conf.getStep() * 1e-3);
	}
	
	
	public void atualize(double t){
		
		samplerJointAngleStore.sample(jointAngleStore, "angle", t, angle);
		samplerJointTorqueStore.sample(jointTorqueStore, "torque", t, torque);
		
		angle = calculateAngle(t);
		
		if(angle >=0){
			torque = calculateTorque(t) - angle * 0.5 - 1;
		}
		else{
			torque = calculateTorque(t) - angle * 0.2 - 1;
		}
		
		
	}
	
	private double calculateAngle (double t){
		
		double filteredResult;
		
		filteredResult = angleArray[iteration];
		iteration++;
		
		return filteredResult;
	}
	
	private double calculateTorque (double t){
		
		double resultantTorque = 0;
		
		for(int i = 0; i < musculotendons.length; i++){
			
			if(musculotendons[i] != null){
				
				if(musculotendons[i].getCdMuscle().equals("TA")){
					resultantTorque += scaleFactorDorsiflexors * musculotendons[i].getForce() * musculotendons[i].getMomentArm();
				}
				else{
					resultantTorque += scaleFactorPlantarflexors * musculotendons[i].getForce() * musculotendons[i].getMomentArm();
				}
				
			}
		}
		return resultantTorque;
	}

}
