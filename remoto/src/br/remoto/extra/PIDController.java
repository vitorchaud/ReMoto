/*
 *  Player Java Client 2 - PIDController.java
 *  Copyright (C) 2002-2006 Radu Bogdan Rusu, Maxim Batalin
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * $Id: PIDController.java,v 1.3 2006/03/06 08:33:31 veedee Exp $
 *
 */
package br.remoto.extra;

/**
 * Proportional-Integral-Derivative controller implementation.
 * @author Radu Bogdan Rusu
 */
public class PIDController extends Controller {

    /** Proportional constant */
    protected float kp;
    /** Integral constant */
    protected float ki;
    /** Derivative constant */
    protected float kd;
    
    /**
     * Constructor for PIDController.
     * @param Kp the proportional constant
     * @param Ki the integral constant
     * @param Kd the derivative constant
     */
    public PIDController (float Kp, float Ki, float Kd) {
    	this.kp = Kp;
        this.ki = Ki;
        this.kd = Kd;
    }
    
    /**
     * Calculate and return the controller's command for the controlled system.
     * @param currentOutput the current output of the system
     * @return the new calculated command for the system
     */
    public float getCommand (float currentOutput) {
    	this.currE = this.goal - currentOutput;
        eSum += currE;
        
        lastE = currE;
        float Pgain = this.kp * currE;
        float Igain = this.ki * eSum;
        float Dgain = this.kd * deltaE ();
        
        return Pgain + Igain + Dgain;
    }
    
    /**
     * Get the current value of the proportional constant.
     * @return Kp as a double
     */
    public float getKp () {
        return this.kp;
    }
    
    /**
     * Set a new value for the proportional constant.
     * @param newKp the new value for Kp
     */
    public void setKp (float newKp) {
        this.kp = newKp;
    }
    
    /**
     * Get the current value of the integral constant.
     * @return Ki as a double
     */
    public float getKi () {
        return this.ki;
    }
    
    /**
     * Set a new value for the integral constant.
     * @param newKi the new value for Ki
     */
    public void setKi (float newKi) {
        this.ki = newKi;
    }
    
    /**
     * Get the current value of the derivative constant.
     * @return Kd as a double
     */
    public double getKd () {
        return this.kd;
    }
    
    /**
     * Set a new value for the derivative constant.
     * @param newKd the new value for Kd
     */
    public void setKd (float newKd) {
        this.kd = newKd;
    }
}
