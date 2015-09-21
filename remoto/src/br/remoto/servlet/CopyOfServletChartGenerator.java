/* -------------------------------
 * ServletDemo2ChartGenerator.java
 * -------------------------------
 * (C) Copyright 2002-2004, by Object Refinery Limited.
 *
 */

package br.remoto.servlet;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.remoto.model.Configuration;
import br.remoto.model.InputGraphicDisplay;
import br.remoto.model.ReMoto;
import br.remoto.model.ResultDisplay;
import br.remoto.model.Simulation;

/**
 * A servlet that returns one of three charts as a PNG image file.  This servlet is
 * referenced in the HTML generated by ServletDemo2.
 * <P>
 * Three different charts can be generated, controlled by the 'type' parameter.  The possible
 * values are 'pie', 'bar' and 'time' (for time series).
 * <P>
 * This class is described in the JFreeChart Developer Guide.
 */
public class CopyOfServletChartGenerator extends HttpServlet 
{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     * Default constructor.
     */
    public CopyOfServletChartGenerator() 
    {
        // nothing required
    }

    /**
     * Process a GET request.
     *
     * @param request  the request.
     * @param response  the response.
     *
     * @throws ServletException if there is a servlet related problem.
     * @throws IOException if there is an I/O problem.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
    {
    	OutputStream out = response.getOutputStream();
    	
		try
		{
			String cdSimulation = request.getParameter("cdSimulation");
			
			Simulation sim = (Simulation)ReMoto.simulations.get( cdSimulation );
			Configuration conf = (Configuration)request.getSession().getAttribute("configuration");
			
			//System.out.println("sim: " + sim);
			//System.out.println("conf: " + conf);
			//System.out.println("cdSimulation: " + cdSimulation);
			
			
			if( 	sim != null || 
					cdSimulation.equals("input") || 
					cdSimulation.equals("inputHist") || 
					cdSimulation.equals("stimulation") ||
					cdSimulation.equals("injCurrent")){
				
				InputGraphicDisplay inputDisplay;
				ResultDisplay results = new ResultDisplay(conf);
				
				if (	cdSimulation.equals("input") || 
						cdSimulation.equals("inputHist") || 
						cdSimulation.equals("stimulation") ||
						cdSimulation.equals("injCurrent")){
					inputDisplay = new InputGraphicDisplay(conf, cdSimulation);
					inputDisplay.generateResults(sim, response.getOutputStream());
				}
				else{
					results.generateResults(sim, response.getOutputStream());
				}
				
	            response.setContentType("image/jpeg");
	            response.setHeader("Content-disposition", "inline; filename=chart.jpg");
				response.getOutputStream().flush();
				
				inputDisplay = null;
				results = null;
				System.gc();
			}
        }
        catch (Exception e) 
        {
            System.err.println(e.toString());
        }
        finally 
        {
            out.close();
        }

    }

}
