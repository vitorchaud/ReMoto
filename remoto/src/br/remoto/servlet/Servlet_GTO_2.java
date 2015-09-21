/* -------------------------------
 * ServletDemo2ChartGenerator.java
 * -------------------------------
 * (C) Copyright 2002-2004, by Object Refinery Limited.
 *
 */

package br.remoto.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



/**
* A basic servlet that generates an HTML page that displays a chart generated by
* JFreeChart.
* <P>
* This servlet uses another servlet (ServletDemo2ChartGenerator) to create a PNG image
* for the embedded chart.
* <P>
* This class is described in the JFreeChart Developer Guide.
*/

public class Servlet_GTO_2 extends HttpServlet {
	
	
	
	public Servlet_GTO_2() {}
	
	/**
	* Processes a POST request.
	* <P>
	* The chart.html page contains a form for generating the first request, after that
	* the HTML returned by this servlet contains the same form for generating subsequent
	* requests.
	*
	* @param request the request.
	* @param response the response.
	*
	* @throws ServletException if there is a servlet related problem.
	* @throws IOException if there is an I/O problem.
	*/
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		
		
		
		PrintWriter out = new PrintWriter(response.getWriter());
		try {
			
		String param = request.getParameter("chart");
		
		
		//String initial_time = request.getParameter("initial_time");
		//String time_step = request.getParameter("time_step");
		//String final_time = request.getParameter("final_time");
		
		//String start_time = request.getParameter("start_time");
		//String end_time = request.getParameter("end_time");
		//String initial_value = request.getParameter("initial_value");
		//String final_value = request.getParameter("final_value");
		
		//String gamma_static = request.getParameter("gamma_static");
		//String gamma_dynamic = request.getParameter("gamma_dynamic");
		
		//Spindle spindle = new Spindle(initial_time, time_step, final_time, start_time, end_time, initial_value, final_value, gamma_static, gamma_dynamic);
		
		//spindle.resetSpindle(initial_time, time_step, final_time, start_time, end_time, initial_value, final_value, gamma_static, gamma_dynamic);
		
		//spindle.Simulation();
		
		response.setContentType("text/html");
		out.println("<HTML>");
		out.println("<HEAD>");
		out.println("<TITLE>Golgi Tendon Organ Simulation Results</TITLE>");
		out.println("</HEAD>");
		out.println("<BODY BGCOLOR=#FAFFFF>");
		out.println("<p class=MsoNormal><span lang=EN-US style='mso-ansi-language:EN-US'>&nbsp;<o:p></o:p></span></p>");
		out.println("<H1>Golgi Tendon Organ Simulation Results</H1>");
		out.println("<P>");
		out.println("Please choose a result type:");
		out.println("<FORM ACTION=\"/remoto/servlet/Servlet_GTO_2\" METHOD=POST>");
		String inputChecked = (param.equals("input") ? " CHECKED" : "");
		String IbChecked = (param.equals("afferent") ? " CHECKED" : "");
		//String primaryChecked = (param.equals("primary") ? " CHECKED" : "");
		//String secondaryChecked = (param.equals("secondary") ? " CHECKED" : "");
		out.println("<INPUT TYPE=\"radio\" NAME=\"chart\" VALUE=\"input\"" + inputChecked + "> Input (Fiber Tension)");
		out.println("<INPUT TYPE=\"radio\" NAME=\"chart\" VALUE=\"fusimotor\"" + IbChecked + "> Afferent Activity");
		//out.println("<INPUT TYPE=\"radio\" NAME=\"chart\" VALUE=\"primary\"" + primaryChecked + "> Primary Afferent Activity");
		//out.println("<INPUT TYPE=\"radio\" NAME=\"chart\" VALUE=\"secondary\"" + secondaryChecked + "> Secondary Afferent Activity");
		
		//out.println("<INPUT TYPE=\"hidden\" NAME=\"initial_time\" VALUE=" + initial_time + ">");
		//out.println("<INPUT TYPE=\"hidden\" NAME=\"time_step\" VALUE=" + time_step + ">");
		//out.println("<INPUT TYPE=\"hidden\" NAME=\"final_time\" VALUE=" + final_time + ">");
		
		//out.println("<INPUT TYPE=\"hidden\" NAME=\"start_time\" VALUE=" + start_time + ">");
		//out.println("<INPUT TYPE=\"hidden\" NAME=\"end_time\" VALUE=" + end_time + ">");
		//out.println("<INPUT TYPE=\"hidden\" NAME=\"initial_value\" VALUE=" + initial_value + ">");
		//out.println("<INPUT TYPE=\"hidden\" NAME=\"final_value\" VALUE=" + final_value + ">");
		
		
	
		
		//out.println("<P>");
		out.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<INPUT TYPE=\"submit\" VALUE=\"Plot Result\">");
		out.println("</FORM>");
		
		
		out.println("<P>");
		//out.println("<IMG SRC=\"ServletDemo2ChartGenerator?type=" + param + "\" BORDER=1 WIDTH=800 HEIGHT=600/>");
		out.println("<IMG SRC=\"ServletDemo2ChartGenerator?type=" + "PieChart" + "\" BORDER=1 WIDTH=800 HEIGHT=600/>");
		
		//System.out.println("<FORM ACTION=\"/remoto/servlet/ServletDemo2\" METHOD=POST>");
		
		
		out.println("</BODY>");
		out.println("</HTML>");
		out.flush();
		out.close();
		}
		catch (Exception e) {
		System.err.println(e.toString());
		}
		finally {
		out.close();
		}
	}
}