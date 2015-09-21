package br.remoto.model.Joint;


import java.util.ArrayList;

import br.remoto.model.ReMoto;
import br.remoto.model.Neuron.Miscellaneous;
import br.remoto.model.vo.JointVO;
import br.remoto.util.Signal;
import br.remoto.model.Joint.Ankle.AnkleSuperClass;
import br.remoto.model.Joint.Ankle.Models.AnkleIsometricModel;
import br.remoto.model.Musculotendon.MusculotendonSuperClass;
import br.remoto.model.Musculotendon.Muscle.MuscleSuperClass;
import br.remoto.model.Musculotendon.Muscle.ExtrafusalMuscle.ExtrafusalMuscleSuperClass;

public class Joint
{
	
	private static final long serialVersionUID = 1L;
	
	protected AnkleIsometricModel ankleModel;
	
	
	public Joint(){
		//System.out.println("Creating Joint");
	}
	
	public void atualize(double t){
		ankleModel.atualize(t);
	}

	public AnkleIsometricModel getAnkleModel() {
		return ankleModel;
	}

	public void setAnkleModel(AnkleIsometricModel ankleModel) {
		this.ankleModel = ankleModel;
	}
	
	public ArrayList getJointAngleStore(){
		return ankleModel.getJointAngleStore();
	}
	
	public ArrayList getJointTorqueStore(){
		return ankleModel.getJointTorqueStore();
	}

}
