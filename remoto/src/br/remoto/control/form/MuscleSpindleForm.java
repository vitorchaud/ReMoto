/*
 * Created on 08/12/2011
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package br.remoto.control.form;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import br.remoto.model.Current;
import br.remoto.model.Nerve;


public class MuscleSpindleForm extends ActionForm
{
	private static final long serialVersionUID = 1L;
	
	private String cdType;
	private double gammaDynamic;
	private double gammaStatic;
	
	private double bag1Gain;
	private double bag2AndChainGain;

    public MuscleSpindleForm()
	{
		super();
		
	}

	public void reset(ActionMapping map, HttpServletRequest req)
	{
		super.reset(map, req);
        
	}
	
	public ActionErrors validate(ActionMapping map, HttpServletRequest req)
	{
		return super.validate(map, req);
	}

	public String getCdType() {
		return cdType;
	}

	public void setCdType(String cdType) {
		this.cdType = cdType;
	}

	public double getGammaDynamic() {
		return gammaDynamic;
	}

	public void setGammaDynamic(double gammaDynamic) {
		this.gammaDynamic = gammaDynamic;
	}

	public double getGammaStatic() {
		return gammaStatic;
	}

	public void setGammaStatic(double gammaStatic) {
		this.gammaStatic = gammaStatic;
	}

	public double getBag1Gain() {
		return bag1Gain;
	}

	public void setBag1Gain(double bag1Gain) {
		this.bag1Gain = bag1Gain;
	}

	public double getBag2AndChainGain() {
		return bag2AndChainGain;
	}

	public void setBag2AndChainGain(double bag2AndChainGain) {
		this.bag2AndChainGain = bag2AndChainGain;
	}
    
    
	
}
