package br.remoto.model.Joint.Ankle;


import java.util.ArrayList;

import br.remoto.model.Musculotendon.Muscle.MuscleSuperClass;
import br.remoto.model.Neuron.Miscellaneous;
import br.remoto.model.vo.JointVO;
import br.remoto.util.Sample;
import br.remoto.util.Signal;

public abstract class AnkleSuperClass
{
	private static final long serialVersionUID = 1L;
	
	protected String cd;
	protected String name;
	protected int numNuclei;
	protected int numMotorNuclei;
	protected int numNerves;
	protected int ind;
	
	protected Sample samplerJointAngleStore;
	protected Sample samplerJointTorqueStore;
	
	protected ArrayList jointAngleStore = new ArrayList();
	protected ArrayList jointTorqueStore = new ArrayList();
	
	protected double angle;
	protected double torque;
	
	protected double kneeAngle;
	
	public AnkleSuperClass(JointVO vo){
		//System.out.println("Creating Ankle Joint");
		
		cd = vo.getCd();
		name = vo.getName();
		numNuclei = vo.getNumNuclei();
		numMotorNuclei = vo.getNumMotorNuclei();
		numNerves = vo.getNumNerves();
		ind = vo.getInd();
		
		angle = 0;
		torque = 0;
		
		kneeAngle = 0;
	}
	
	public abstract void atualize(double t);
	
	
	
	public double getAngle() {
		return angle;
	}

	public void setAngle(double angle) {
		this.angle = angle;
	}

	public double getTorque() {
		return torque;
	}

	public void setTorque(double torque) {
		this.torque = torque;
	}


	public ArrayList getJointAngleStore() {
		return jointAngleStore;
	}


	public void setJointAngleStore(ArrayList jointAngleStore) {
		this.jointAngleStore = jointAngleStore;
	}


	public ArrayList getJointTorqueStore() {
		return jointTorqueStore;
	}


	public void setJointTorqueStore(ArrayList jointTorqueStore) {
		this.jointTorqueStore = jointTorqueStore;
	}



	public String getCd() {
		return cd;
	}



	public void setCd(String cd) {
		this.cd = cd;
	}



	public String getName() {
		return name;
	}



	public void setName(String name) {
		this.name = name;
	}



	public int getNumNuclei() {
		return numNuclei;
	}



	public void setNumNuclei(int numNuclei) {
		this.numNuclei = numNuclei;
	}



	public int getNumMotorNuclei() {
		return numMotorNuclei;
	}



	public void setNumMotorNuclei(int numMotorNuclei) {
		this.numMotorNuclei = numMotorNuclei;
	}



	public int getNumNerves() {
		return numNerves;
	}



	public void setNumNerves(int numNerves) {
		this.numNerves = numNerves;
	}



	public int getInd() {
		return ind;
	}



	public void setInd(int ind) {
		this.ind = ind;
	}

	public double getKneeAngle() {
		return kneeAngle;
	}

	public void setKneeAngle(double kneeAngle) {
		this.kneeAngle = kneeAngle;
	}


}