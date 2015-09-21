package br.remoto.model.Musculotendon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

import br.remoto.model.Configuration;
import br.remoto.model.MotorUnit;
import br.remoto.model.ReMoto;
import br.remoto.model.Joint.Ankle.Models.AnkleIsometricModel;
import br.remoto.model.Musculotendon.Muscle.ExtrafusalMuscle.ExtrafusalMuscleSuperClass;
import br.remoto.model.Musculotendon.Tendon.NonInnerveted.NonInnervatedTendon;
import br.remoto.model.Proprioceptors.GolgiTendonOrgan;
import br.remoto.model.Proprioceptors.MuscleSpindle;
import br.remoto.util.Conversion;
import br.remoto.util.Sample;
import br.remoto.util.Signal;



public class MusculotendonSuperClass
{
	
	protected Sample sampler1;
	protected Sample sampler2;
	
	protected ArrayList lengthStore = new ArrayList();
	protected ArrayList momentArmStore = new ArrayList();
	
	ExtrafusalMuscleSuperClass extrafusalMuscle;
	NonInnervatedTendon tendon;
	
	GolgiTendonOrgan gto;
	MuscleSpindle spindle;
	
	AnkleIsometricModel associatedJoint;
	
	private String cdMuscle;
	
	private double length;
	private double initialLength;
	private double velocity = 0;
	private double momentArm;
	
	private double force;
	
	
	public MusculotendonSuperClass(Configuration conf, ExtrafusalMuscleSuperClass extrafusalMuscle, NonInnervatedTendon tendon,
									MuscleSpindle spindle, GolgiTendonOrgan gto) {
		this.extrafusalMuscle = extrafusalMuscle;
		this.tendon = tendon;
		this.gto = gto;
		this.spindle = spindle;
		this.cdMuscle = extrafusalMuscle.getCdMuscle();
		
		sampler1 = new Sample(conf.getDecimationFrequency(), 1000 / conf.getStep());
		sampler2 = new Sample(conf.getDecimationFrequency(), 1000 / conf.getStep());
		
		//System.out.println("Creating musculotendon nucleus: " + extrafusalMuscle.getCdMuscle());
		
	}
	
	public void atualize(double t){
		
		
		calculateLength(cdMuscle, associatedJoint.getAngle(), associatedJoint.getKneeAngle(), 0);
		if (t == 0)	extrafusalMuscle.setInitialLengthNorm();
		calculateMomentArmAnkle(cdMuscle, associatedJoint.getAngle(),  associatedJoint.getKneeAngle(), 0);
		
		extrafusalMuscle.atualize(t);
		
		if(extrafusalMuscle.getMuscleModel().equals("hill")){
			tendon.atualize(t,
					length / extrafusalMuscle.getOptimalLength(),
					extrafusalMuscle.getLengthNorm(),
					extrafusalMuscle.getPennationAngle());
	
			force = tendon.getForceNorm() * extrafusalMuscle.getMaximumMuscleForce();
			gto.atualize(t, tendon.getForceNorm() * extrafusalMuscle.getMaximumMuscleForce());
			
		}
		else{
			force = extrafusalMuscle.getForce();
			gto.atualize(t, force);
		}
		
		spindle.atualize(t, extrafusalMuscle.getLengthNorm(), extrafusalMuscle.getStretchVelocity(), extrafusalMuscle.getStretchAcceleration() );
	
		//lengthStore.add( new Signal( "musculotendonLength", length, t) );
		//momentArmStore.add( new Signal( "musculotendonMomentArmStore", momentArm, t) );
		
		sampler1.sample(lengthStore, "length", t, length);
		sampler2.sample(momentArmStore, "momentArm", t, momentArm);
	}
	
	
	
	private void calculateLength(String cdMuscle, double ankleAngle, double kneeAngle, double subtalarAngle){
		
		double a1 = 0;
		double a2 = 0;
		double a3 = 0;
		double a4 = 0;
		double a5 = 0;
		double a6 = 0;
		double a7 = 0;
		double a8 = 0;
		double a9 = 0;
		
		if(cdMuscle.equals("SOL")){
			/*
			a1 = 0.29074;
			a2 = 0.0007155;
			a3 = -0.000063703;
			a4 = -0.0000021727;
			a5 = 0.0000024219;
			a6 = -0.000000030692;
			a7 = 0.0000000037;
			
			length = a1 + a2 * ankleAngle + a3 * subtalarAngle + a4 * Math.pow(ankleAngle, 2) +
			 		a5 * Math.pow(subtalarAngle, 2) + a6 * Math.pow(ankleAngle, 3) + a7 * Math.pow(subtalarAngle, 3);
			*/
			length = 	9.27 * Math.pow(10, -11) * Math.pow(ankleAngle, 4) - 
						3.15 * Math.pow(10, -8) * Math.pow(ankleAngle, 3) - 
						2.24 * Math.pow(10, -6) * Math.pow(ankleAngle, 2) + 
						0.000722 * ankleAngle + 
						0.323;
		}
		else if(cdMuscle.equals("MG")){
			a1 = 0.45108;
			a2 = 0.00026319;
			a3 = 0.00072473;
			a4 = -0.000026504;
			a5 = -0.00000016841;
			a6 = -0.0000015958;
			a7 = 0.0000025733;
			a8 = -0.0000000020153;
			a9 = -0.00000003344;
			
			length = a1 + a2 * kneeAngle + a3 * ankleAngle + a4 * subtalarAngle + 
					a5 * Math.pow(kneeAngle, 2) + a6 * Math.pow(ankleAngle, 2) + a7 * Math.pow(subtalarAngle, 2) + 
					a8 * Math.pow(kneeAngle, 3) + a9 * Math.pow(ankleAngle, 3);
		}
		else if(cdMuscle.equals("LG")){
			a1 = 0.44815;
			a2 = 0.00029199;
			a3 = 0.00074275;
			a4 = -0.000077552;
			a5 = -0.00000016253;
			a6 = -0.000001717;
			a8 = -0.0000000044908;
			a9 = -0.000000034054;
			
			length =a1 + a2 * kneeAngle + a3 * ankleAngle + a4 * subtalarAngle + 
					a5 * Math.pow(kneeAngle, 2) + a6 * Math.pow(ankleAngle, 2) + a7 * Math.pow(subtalarAngle, 2) + 
					a8 * Math.pow(kneeAngle, 3) + a9 * Math.pow(ankleAngle, 3);
			
		}
		else if(cdMuscle.equals("TA")){
			a1 = 0.30372;
			a2 = -0.00070653;
			a4 = -0.0000011404;
			
			length = a1 + a2 * ankleAngle + a4 * Math.pow(ankleAngle, 2);
			
		}
		
	}
	
	/*
	private void calculateVelocity(double jointAngle, double jointAngularVelocity){
		
		double a1_SOL = 0.29074;
		double a2_SOL = 0.0007155;
		//double a3_SOL = -0.000063703;
		double a4_SOL = -0.0000021727;
		//double a5_SOL = 0.0000024219;
		double a6_SOL = -0.000000030692;
		//double a7_SOL = 0.0000000037025;
		
		velocity = a1_SOL + a2_SOL * jointAngularVelocity + 2* a4_SOL * jointAngle * jointAngularVelocity + 3 * a6_SOL * Math.pow(jointAngle, 2) * jointAngularVelocity;
		
	}
	*/
	
	private void calculateMomentArmAnkle(String cdMuscle, double ankleAngle, double kneeAngle, double subtalarAngle){
		
		double a1 = 0;
		double a2 = 0;
		double a3 = 0;
		double a4 = 0;
		double a5 = 0;
		double a6 = 0;
		double a7 = 0;
		double a8 = 0;
		
		if(cdMuscle.equals("SOL")){
			
			a1 = -0.041314;
			a2 = 0.00024505;
			a3 = 0.000038018;
			a4 = 0.0000052709;
			a5 = 0.0000021702;
			a6 = -0.00000214;
			
			momentArm = a1 + 
						a2 * ankleAngle +
						a3 * subtalarAngle +
						a4 * Math.pow(ankleAngle, 2) +
						a5 * Math.pow(subtalarAngle, 2) +
						a6 * ankleAngle * subtalarAngle;
		}
		else if(cdMuscle.equals("MG")){
			
			a1 = -0.041809;
			a2 = 0;
			a3 = 0.00017968;
			a4 = 0.0000501;
			a5 = 0;
			a6 = 0.0000057323;
			a7 = 0.0000019531;
			a8 = 0.000000025714;
			
				
			momentArm = a1 + 
						a2 * kneeAngle +
						a3 * ankleAngle +
						a4 * subtalarAngle +
						a5 * Math.pow(kneeAngle, 2) +
						a6 * Math.pow(ankleAngle, 2) +
						a7 * Math.pow(subtalarAngle, 2) +
						a8 * kneeAngle * ankleAngle * subtalarAngle;
		}
		else if(cdMuscle.equals("LG")){
			
			a1 = -0.042863;
			a2 = 0;
			a3 = 0.00019335;
			a4 = 0.000045168;
			a5 = 0;
			a6 = 0.0000058387;
			a7 = 0.000002031;
			a8 = 0.00000002599;
			
				
			momentArm = a1 + 
						a2 * kneeAngle +
						a3 * ankleAngle +
						a4 * subtalarAngle +
						a5 * Math.pow(kneeAngle, 2) +
						a6 * Math.pow(ankleAngle, 2) +
						a7 * Math.pow(subtalarAngle, 2) +
						a8 * kneeAngle * ankleAngle * subtalarAngle;
		}
		else if(cdMuscle.equals("TA")){
			a1 = 0.042111;
			a2 = 0.00012308;
			a4 = -0.0000043648;
			
			momentArm = a1 + a2 * ankleAngle + a4 * Math.pow(ankleAngle, 2);
		}
	}
	
	
	
	public ExtrafusalMuscleSuperClass getMuscle(){
		return extrafusalMuscle;
	}
	
	public void setMuscle(ExtrafusalMuscleSuperClass muscle) {
		// TODO Auto-generated method stub
		extrafusalMuscle = muscle;
	}

	public NonInnervatedTendon getTendon() {
		return tendon;
	}

	public void setTendon(NonInnervatedTendon tendon) {
		this.tendon = tendon;
	}



	public String getCdMuscle() {
		return cdMuscle;
	}



	public void setCdMuscle(String cdMuscle) {
		this.cdMuscle = cdMuscle;
	}



	public GolgiTendonOrgan getGto() {
		return gto;
	}



	public void setGto(GolgiTendonOrgan gto) {
		this.gto = gto;
	}



	public MuscleSpindle getSpindle() {
		return spindle;
	}



	public void setSpindle(MuscleSpindle spindle) {
		this.spindle = spindle;
	}



	public double getLength() {
		return length;
	}



	public void setLength(double length) {
		this.length = length;
	}



	public double getInitialLength() {
		return initialLength;
	}



	public void setInitialLength(double initialLength) {
		this.initialLength = initialLength;
	}



	public double getVelocity() {
		return velocity;
	}
	
	public void setVelocity(double velocity) {
		this.velocity = velocity;
	}

	/*
	public double getVelocityNorm() {
		return velocity * (extrafusalMuscle.getTimeScale() / extrafusalMuscle.getOptimalLength());
	}
	*/

	public double getMomentArm() {
		return momentArm;
	}

	
	public void setMomentArm(double momentArm) {
		this.momentArm = momentArm;
	}



	public double getForce() {
		return force;
	}



	public void setForce(double force) {
		this.force = force;
	}



	public AnkleIsometricModel getAssociatedJoint() {
		return associatedJoint;
	}



	public void setAssociatedJoint(AnkleIsometricModel associatedJoint) {
		this.associatedJoint = associatedJoint;
	}

	public ArrayList getLengthStore() {
		return lengthStore;
	}

	public void setLengthStore(ArrayList lengthStore) {
		this.lengthStore = lengthStore;
	}

	public ArrayList getMomentArmStore() {
		return momentArmStore;
	}

	public void setMomentArmStore(ArrayList momentArmStore) {
		this.momentArmStore = momentArmStore;
	}
	
	
	
}

