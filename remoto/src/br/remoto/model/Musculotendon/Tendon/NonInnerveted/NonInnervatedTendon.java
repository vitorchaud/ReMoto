package br.remoto.model.Musculotendon.Tendon.NonInnerveted;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

import br.remoto.model.Configuration;
import br.remoto.model.MotorUnit;
import br.remoto.model.ReMoto;
import br.remoto.model.Musculotendon.Tendon.TendonSuperClass;
import br.remoto.util.Sample;
import br.remoto.util.Signal;


public class NonInnervatedTendon extends TendonSuperClass
{
	private double length;
	private double force;
	private double lengthNorm;
	private double forceNorm = 0;
	
	private double muscleOptimalLength;
	private double muscleMaximumForce;
	
	protected ArrayList lengthStore = new ArrayList();
	protected ArrayList forceStore = new ArrayList();
	
	private double L_slack_T;
	private double eps_0_T;
	private double eps_toe_T;
	private double k_lin ;
	private double F_toe_T;
	private double k_toe_T;
	
	
	public NonInnervatedTendon(Configuration conf, double muscleOptimalLength, double muscleMaximumForce){
		super(conf);
		//this.slackLength = slackLength;
		//this.elasticCoeficient = elasticCoeficient;
		//slackLengthNorm = slackLength / muscleOptimalLength;
		
		// PROVISÓRIO
		//c = c_T;
		//k = k_T;
		//Lr = Lr_T;
		
		L_slack_T   = 0.275;
		eps_0_T     = 0.09;       // [L_slack_T]      
		eps_toe_T   = 0.609 * eps_0_T;              // [L_slack_T]  
		k_lin       = 1.712 / eps_0_T;              // [F_0_m / L_slack_T]
		F_toe_T     = 0.33;                         // [F_0_m]   
		k_toe_T     = 3;                            // exponencial shape factor
		
		sampler1 = new Sample(conf.getDecimationFrequency(), 1000 / conf.getStep());
		sampler2 = new Sample(conf.getDecimationFrequency(), 1000 / conf.getStep());
		
		lengthNorm = L_slack_T;
		this.muscleOptimalLength = muscleOptimalLength;
		this.muscleMaximumForce = muscleMaximumForce;
		
	}
	
	
	public double getSlackLengthNorm() {
		
		return L_slack_T;
	}
	
	public void setSlackLengthNorm(double slackLengthNorm) {
		this.L_slack_T = slackLengthNorm;
	}
	
	public double getLength() {
		return length;
	}
	public void setLength(double length) {
		this.length = length;
	}
	public double getForceNorm() {
		return forceNorm;
	}
	public void setForceNorm(double forceNorm) {
		this.forceNorm = forceNorm;
	}
	
	public void atualize(double t, double musculotendonLengthNorm, double muscleLengthNorm, double pennationAngle)
	{
		lengthNorm = musculotendonLengthNorm - muscleLengthNorm * Math.cos(pennationAngle);
		
		double tendonStrainNormLs = (lengthNorm * muscleOptimalLength - L_slack_T) / L_slack_T;
		
		if(tendonStrainNormLs > eps_toe_T){
			forceNorm = F_toe_T + k_lin * (tendonStrainNormLs - eps_toe_T);
		}
		else{
			forceNorm = (F_toe_T / (Math.exp(k_toe_T) - 1)) * (Math.exp(k_toe_T * tendonStrainNormLs / eps_toe_T) - 1);
		}
		
		length = lengthNorm * muscleOptimalLength;
		force = forceNorm * muscleMaximumForce;
		
		/*
		System.out.println("musculotendonLengthNorm: " + musculotendonLengthNorm +
				"tendonLengthNorm: " + lengthNorm +
				"muscleLengthNorm: " + muscleLengthNorm +
				"pennationAngle: " + pennationAngle +
				"Math.cos(pennationAngle): " + Math.cos(pennationAngle));
		*/
		
		//lengthStore.add( new Signal( ReMoto.tendonLength, length, t) );
		//forceStore.add( new Signal( ReMoto.tendonForce, force, t) );
		
		sampler1.sample(lengthStore, "tendonLength", t, length);
		sampler2.sample(forceStore, "tendonForce", t, force);
	}


	public double getLengthNorm() {
		return lengthNorm;
	}


	public void setLengthNorm(double lengthNorm) {
		this.lengthNorm = lengthNorm;
	}


	public ArrayList getLengthStore() {
		return lengthStore;
	}


	public ArrayList getForceStore() {
		return forceStore;
	}
}

