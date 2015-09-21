package br.remoto.model.Proprioceptors;

import java.util.ArrayList;

import br.remoto.model.Configuration;
import br.remoto.model.ReMoto;
import br.remoto.model.Musculotendon.Muscle.IntrafusalMuscle.Bag1.Models.Bag1_Mileusnic;
import br.remoto.model.Musculotendon.Muscle.IntrafusalMuscle.Bag2.Models.Bag2_Mileusnic;
import br.remoto.model.Musculotendon.Muscle.IntrafusalMuscle.Chain.Models.Chain_Mileusnic;
import br.remoto.model.Musculotendon.Muscle.IntrafusalMuscle.Lumped.Models.Lumped_Prochazka;
import br.remoto.model.Musculotendon.Tendon.Innerveted.Lumped.Models.Lumped_Crago;
import br.remoto.model.Neuron.Neuron;
import br.remoto.model.Neuron.SensoryFiber;
import br.remoto.servlet.SpindleSimulation;
import br.remoto.spindle_simulator.Input;
import br.remoto.util.Conversion;
import br.remoto.util.Sample;
import br.remoto.util.Signal;



public class MuscleSpindle {
	
	String cdNucleus;
	String spindle_model;
	
	Bag1_Mileusnic bag1;
	Bag2_Mileusnic bag2;
	Chain_Mileusnic chain;
	
	protected Sample samplerIaFiringRateStore;
	protected Sample samplerIIFiringRateStore;
	
	protected static final double S = 0.156;
	
	
	protected double gammaDynamicMNFiringRate;
	protected double gammaStaticMNFiringRate;
	
	protected double IaFiringRate;
	protected double IIFiringRate;
	
	protected ArrayList IaFiringRateStore = new ArrayList();
	protected ArrayList IIFiringRateStore = new ArrayList();
	
	protected Lumped_Prochazka lumpedProchazka;
	
	Configuration conf;

	public MuscleSpindle(String cdNucleus, Neuron neurons[][], Configuration conf, double gamma_dynamic, double gamma_static, String spindle_model) {
				
		this.cdNucleus = cdNucleus;
		this.conf = conf;
		this.spindle_model = spindle_model;
		
		samplerIaFiringRateStore = new Sample(conf.getDecimationFrequency(), 1000 / conf.getStep());
		samplerIIFiringRateStore = new Sample(conf.getDecimationFrequency(), 1000 / conf.getStep());
		
		this.gammaDynamicMNFiringRate = gamma_dynamic;
		this.gammaStaticMNFiringRate = gamma_static;
		
		if(spindle_model.equals(ReMoto.spindleModelProchazka)){
			this.lumpedProchazka = new Lumped_Prochazka(conf, cdNucleus);
		}
			
		else if(spindle_model.equals(ReMoto.spindleModelMileusnic)){
			bag1 	= new Bag1_Mileusnic(cdNucleus, conf, gammaDynamicMNFiringRate, gammaStaticMNFiringRate);
			bag2 	= new Bag2_Mileusnic(cdNucleus, conf, gammaDynamicMNFiringRate, gammaStaticMNFiringRate);
			chain 	= new Chain_Mileusnic(cdNucleus, conf, gammaDynamicMNFiringRate, gammaStaticMNFiringRate);
		}
		
		
		for(int x = ReMoto.indDT + 1; x < neurons.length; x++)
		{
			if( neurons[x] == null )
				continue;
			
			for(int y = 0; y < neurons[x].length; y++)
			{
				Neuron neu = neurons[x][y];
				
				if( neu.isActive() == false )
					continue;
				
				if( !neu.getCdNucleus().endsWith( cdNucleus ))
				{
					continue;
				}
				
				if( neu.getCategory().equals( ReMoto.AF ) )
				{
					((SensoryFiber)neu).setSpindle( this );	
				}
			}
		}
		
	}

	
	public void atualize(double t, double fascicleLength, double velocity, double acceleration){
		
		//System.out.println("fascicleLength: " + fascicleLength + "  velocity: " + velocity + "   acceleration: " + acceleration);
		
		if(spindle_model.equals(ReMoto.spindleModelProchazka)){
				
			lumpedProchazka.setLengthNorm(fascicleLength * 0.03 * 1000);
			lumpedProchazka.setStretchVelocity(Conversion.convertVelocityMillisecondsToSeconds(velocity)  * 0.03 * 1000);
			
			IaFiringRate = lumpedProchazka.calculateIaFiringRate();
			IIFiringRate = lumpedProchazka.calculateIIFiringRate();
			
		}
		else if(spindle_model.equals(ReMoto.spindleModelMileusnic)){
			

			bag1.setLengthNorm(fascicleLength);
			bag2.setLengthNorm(fascicleLength);
			chain.setLengthNorm(fascicleLength);
			
			bag1.setStretchVelocity(velocity);
			bag2.setStretchVelocity(velocity);
			chain.setStretchVelocity(velocity);
			
			bag1.setStretchAcceleration(acceleration);
			bag2.setStretchAcceleration(acceleration);
			chain.setStretchAcceleration(acceleration);
			
			bag1.calculateFusimotorActivation(Conversion.convertMillisecondsToSeconds(t)); 
			bag2.calculateFusimotorActivation(Conversion.convertMillisecondsToSeconds(t)); 
			chain.calculateFusimotorActivation(Conversion.convertMillisecondsToSeconds(t)); 
			
			bag1.calculateFiberTension(Conversion.convertMillisecondsToSeconds(t));  
			bag2.calculateFiberTension(Conversion.convertMillisecondsToSeconds(t));
			chain.calculateFiberTension(Conversion.convertMillisecondsToSeconds(t)); 
			
			
			IaFiringRate = calculateIaMileusnic(Conversion.convertMillisecondsToSeconds(t));
			IIFiringRate = calculateIIMileusnic(Conversion.convertMillisecondsToSeconds(t));
		}
		
		
		//IaFiringRateStore.add( new Signal( ReMoto.IaFiringRate, IaFiringRate, t ));
		//IIFiringRateStore.add( new Signal( ReMoto.IIFiringRate, IIFiringRate, t ));
		samplerIaFiringRateStore.sample(IaFiringRateStore, "IaFiringRate", t, IaFiringRate);
		samplerIIFiringRateStore.sample(IIFiringRateStore, "IIFiringRate", t, IIFiringRate);
		
		//System.out.println("Ia: " + IaFiringRate +  "   length: " + fascicleLength);
	}
	
	
	
	
	
	public double calculateIaMileusnic(double t){
		
		double primary_afferent_bag1 = bag1.calculatePrimaryAfferentActivity(t);
		double primary_afferent_bag2 = bag2.calculatePrimaryAfferentActivity(t);
		double primary_afferent_chain = chain.calculatePrimaryAfferentActivity(t);
		
		//System.out.println("primary_afferent_bag1: " + primary_afferent_bag1);
		//System.out.println("primary_afferent_bag2: " + primary_afferent_bag2);
		//System.out.println("primary_afferent_chain: " + primary_afferent_chain);
		
		
		double smaller;
		double larger;
		
		if (primary_afferent_bag1 >= (primary_afferent_bag2 + primary_afferent_chain)){
			larger = primary_afferent_bag1;
			smaller = (primary_afferent_bag2 + primary_afferent_chain);
		}
		else {
			larger = (primary_afferent_bag2 + primary_afferent_chain);
			smaller = primary_afferent_bag1;
		}
		return S * smaller + larger;
	}
	
	
	
	public double calculateIIMileusnic(double t){
		double secondary_afferent_bag2 = bag2.calculateSecondaryAfferentActivity(t);
		double secondary_afferent_chain = chain.calculateSecondaryAfferentActivity(t);
		return secondary_afferent_bag2 + secondary_afferent_chain;
	}

	public String getCdNucleus() {
		return cdNucleus;
	}

	public void setCdNucleus(String cdNucleus) {
		this.cdNucleus = cdNucleus;
	}

	public ArrayList getIaFiringRateStore() {
		return IaFiringRateStore;
	}

	public void setIaFiringRateStore(ArrayList iaFiringRateStore) {
		IaFiringRateStore = iaFiringRateStore;
	}

	public ArrayList getIIFiringRateStore() {
		return IIFiringRateStore;
	}

	public void setIIFiringRateStore(ArrayList iIFiringRateStore) {
		IIFiringRateStore = iIFiringRateStore;
	}

	public double getIaFiringRate() {
		return IaFiringRate;
	}

	public void setIaFiringRate(double iaFiringRate) {
		IaFiringRate = iaFiringRate;
	}

	public double getIIFiringRate() {
		return IIFiringRate;
	}

	public void setIIFiringRate(double iIFiringRate) {
		IIFiringRate = iIFiringRate;
	}

	public String getSpindle_model() {
		return spindle_model;
	}
	
	public void setSpindle_model(String spindle_model) {
		this.spindle_model = spindle_model;
	}

	public Bag1_Mileusnic getBag1() {
		return bag1;
	}

	public Bag2_Mileusnic getBag2() {
		return bag2;
	}

	public Chain_Mileusnic getChain() {
		return chain;
	}


}