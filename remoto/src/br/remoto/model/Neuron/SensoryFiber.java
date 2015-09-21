/*
 * Created on 27/01/2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package br.remoto.model.Neuron;

import java.util.ArrayList;

import br.remoto.model.Nerve;
import br.remoto.model.ReMoto;
import br.remoto.model.Proprioceptors.GolgiTendonOrgan;
import br.remoto.model.Proprioceptors.MuscleSpindle;
import br.remoto.model.vo.NeuronVO;
import br.remoto.util.Distribution;


public class SensoryFiber extends Neuron
{
	private static final long serialVersionUID = 1L;

	protected double tAxonSpike;

	protected double nextSpike;
	protected int indexSpike;

	//protected double axonThreshold;
	protected double latencyStimulusSpinal;
	protected double latencyStimulusEndPlate;
	
	protected ArrayList axonSpikeTrain = new ArrayList();

	protected Nerve nerve;

	protected GolgiTendonOrgan gto;
	protected MuscleSpindle spindle;
	
	private int index;
	
	protected double recruitmentThreshold;
	protected double gammaOrderIa = 15;
	protected double thresIa;
	protected double y_intIa = 0;
	
	
	
	public SensoryFiber()
	{
	}
	
	
	public SensoryFiber(NeuronVO neu, int index, Miscellaneous misc)
	{
		super(neu, index, misc);
		this.index = index;
		
		reset();
	}
	
	
	public void reset()
	{
		super.reset();
		
		thresIa = Distribution.gammaPoint(1/gammaOrderIa, gammaOrderIa);

		
		if( axonSpikeTrain != null )
			axonSpikeTrain.clear();
		else
			axonSpikeTrain = new ArrayList();

		tAxonSpike = -ReMoto.T_MAX;

		nextSpike = -ReMoto.T_MAX;
		indexSpike = 0;
	}
		
	
	public void ensureCapacity(int size)
	{
		super.ensureCapacity(size);
		
		axonSpikeTrain.ensureCapacity(size);
	}
	

	public void atualize(double t) 
	{
		
		try
		{
			// Propagate spike to all post-synaptic neurons
			if( indexSpike < axonSpikeTrain.size() )
			{
				nextSpike = ((Double)axonSpikeTrain.get(indexSpike)).doubleValue() + latencyStimulusSpinal;
	
				if( t > nextSpike )
				{
					propagateSpike(t);
					indexSpike++;
					
					terminalSpikeTrain.add( new Double(t) );
				}
			}
	
			// If stimulus is greater than threshold and fiber is not in refractory period
			if( nerve.getIntensity() > axonThreshold && t - tAxonSpike > miscellaneous.getAfAxonRefractoryPeriod() )
			{
				tAxonSpike = t;
				axonSpikeTrain.add( new Double(t) );
			}
			
			if(getType().equals(ReMoto.Ib) && t - tAxonSpike > miscellaneous.getAfAxonRefractoryPeriod()){
				
				double IbFiringRate = gto.getIbFiringRate();
				
				double lambdaIb = IbFiringRate * ( miscellaneous.getStep() / 1000);
				double probIb = Math.exp(-lambdaIb) * lambdaIb;
				double aux1 = Math.random();
				
				if(aux1 <= probIb){
					tAxonSpike = t  + latencyStimulusEndPlate;
					axonSpikeTrain.add( new Double(tAxonSpike) );
					
					//System.out.println("tAxonSpike: " + tAxonSpike);
				}
				
				
				
			}
			
			/*
			if(getType().equals(ReMoto.Ia) && 
					t - tAxonSpike > miscellaneous.getAfAxonRefractoryPeriod()){
				
				
				
				
				double lambdaIa = IaFiringRate * ( miscellaneous.getStep() / 1000 );
				double probIa = Math.exp(-lambdaIa) * lambdaIa;
				double aux2 = Math.random();
								
				if(aux2 <= probIa){
					tAxonSpike = t + latencyStimulusEndPlate;
					axonSpikeTrain.add( new Double(tAxonSpike) );
				}
				
			}
			*/
			/*
			if(getType().equals(ReMoto.Ia) && 
					t - tAxonSpike > miscellaneous.getAfAxonRefractoryPeriod()){
				
				double IaFiringRate = spindle.getIaFiringRate();
				
				if(IaFiringRate > this.recruitmentThreshold){
					double lambdaIa = (IaFiringRate - recruitmentThreshold) * ( miscellaneous.getStep() / 1000 );
					double probIa = Math.exp(-lambdaIa) * lambdaIa;
					double aux2 = Math.random();
									
					if(aux2 <= probIa){
						tAxonSpike = t + latencyStimulusEndPlate;
						axonSpikeTrain.add( new Double(tAxonSpike) );
					}
				}
			}
			*/
			
			if(getType().equals(ReMoto.Ia) && t - tAxonSpike > miscellaneous.getAfAxonRefractoryPeriod()){
				
				double IaFiringRate = spindle.getIaFiringRate();
				
				double minimalFiring = 5;
				
				if(IaFiringRate > this.recruitmentThreshold){
					double lambdaIa = (IaFiringRate - recruitmentThreshold) + Distribution.gaussianPoint(minimalFiring, 0.1*minimalFiring);
					
					y_intIa = y_intIa + (lambdaIa * (miscellaneous.getStep() / 1000));
					
					//Algorithm proposed in Mathematics for Neuroscientists (Gabbiani and Cox, 2010)
					if(y_intIa >= thresIa){
						tAxonSpike = t + latencyStimulusEndPlate;
						y_intIa = 0;
						thresIa = Distribution.gammaPoint(1/gammaOrderIa, gammaOrderIa);
						axonSpikeTrain.add(new Double(tAxonSpike));
						}
					}		
			}
			
			
		}
		catch (Exception e) 
		{
			System.out.println( "Error while atualizing AF: " + cd );
		}
	}
	
	public void setLatency(double axonVelocity, double stimulusSpinalDistance, double stimulusEndPlateDistance) 
	{
		if( axonVelocity < 0.01 )
			axonVelocity = 0.01;
		
		latencyStimulusSpinal = (stimulusSpinalDistance/axonVelocity) * 1000.0; 
		latencyStimulusEndPlate = (stimulusEndPlateDistance/axonVelocity) * 1000.0;
	
		latencyStimulusSpinal = Math.round(latencyStimulusSpinal/miscellaneous.getStep())* miscellaneous.getStep();
		latencyStimulusEndPlate = Math.round(latencyStimulusEndPlate/miscellaneous.getStep())* miscellaneous.getStep();
		
	}


	public int getIndexSpike() {
		return indexSpike;
	}


	public void setIndexSpike(int indexSpike) {
		this.indexSpike = indexSpike;
	}


	public double getLatencyStimulusSpinal() {
		return latencyStimulusSpinal;
	}


	public void setLatencyStimulusSpinal(double latencyStimulusSpinal) {
		this.latencyStimulusSpinal = latencyStimulusSpinal;
	}


	public double getNextSpike() {
		return nextSpike;
	}


	public void setNextSpike(double nextSpike) {
		this.nextSpike = nextSpike;
	}


	public ArrayList getAxonSpikeTrain() {
		return axonSpikeTrain;
	}


	public void setAxonSpikeTrain(ArrayList axonSpikeTrain) {
		this.axonSpikeTrain = axonSpikeTrain;
	}


	


	public Nerve getNerve() {
		return nerve;
	}


	public void setNerve(Nerve nerve) {
		this.nerve = nerve;
	}


	public void setGTO(GolgiTendonOrgan golgiTendonOrgan) {
		gto = golgiTendonOrgan;
	}


	public MuscleSpindle getSpindle() {
		return spindle;
	}


	public void setSpindle(MuscleSpindle spindle) {
		this.spindle = spindle;
	}


	public double getRecruitmentThreshold() {
		return recruitmentThreshold;
	}


	public void setRecruitmentThreshold(double recruitmentThreshold) {
		this.recruitmentThreshold = recruitmentThreshold;
	}

	/*
	public double getAxonConductionVelocity() {
		double stimulusSpinalDistance = nerve.getStimulusSpinalEntry();
		return (stimulusSpinalDistance/latencyStimulusSpinal) * 1000.0; 
	}
	*/
}
