package com.data.dao.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import com.data.constants.Constants;
import com.data.main.dao.CPUStatsDao;

public class DiskNumDaoImpl {
	Connection conn = null;
	public Connection connect(){
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();

			//conn = DriverManager.getConnection("jdbc:mysql://ec2-54-67-80-31.us-west-1.compute.amazonaws.com/catalog?autoReconnect=true&zeroDateTimeBehavior=convertToNull", "root", "root");
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/catalog", "root", "");
			if(!conn.isClosed()){
				//System.out.println("Connection Successful");
			}else{
				System.out.println("Connection Error!!");
			}
		}
			catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return conn;
		}

	//Host Implementation
	public String getHStats(CPUStatsDao hstats, String cp, String time) {

		
		try {
			
			
			
			Constants hgraph = new Constants();
			
			String linecolor = "#808080";
			String symbol = "Number";
			JSONArray list_timeStamp = new JSONArray();
			JSONArray list_cpu = new JSONArray();
			JSONArray list_vms = new JSONArray();
			String query2 = null;
			JSONArray list_series = new JSONArray();
			
			
			Date today = new Date(System.currentTimeMillis()); 
			Date prior = new Date(System.currentTimeMillis()-5*60*1000);
			Date prevday = new Date(System.currentTimeMillis()-24*60*60*1000);
			Date prevweek = new Date(System.currentTimeMillis()-7*24*60*60*1000);
			
			
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MONTH, -1);
			Date result = cal.getTime();
			
			
			
			Timestamp curr = new Timestamp(today.getTime());
			Timestamp minusfive = new Timestamp(prior.getTime());
			Timestamp onedayprior = new Timestamp(prevday.getTime());
			Timestamp weekprior = new Timestamp(prevweek.getTime());
			Timestamp monthprior = new Timestamp(result.getTime());
			
			

			Connection conn = connect();
			
			String query = "select distinct host_name from hostlog";
			PreparedStatement stmt = conn.prepareStatement(query);
			ResultSet rs = stmt.executeQuery(query);
			//JSONObject series_obj = new JSONObject();
			
			while(rs.next())
			{
			
			list_vms.put(rs.getString("host_name"));
			JSONObject series_obj = new JSONObject();
			
			String vmname = rs.getString("host_name");
			
			if(time=="real")//cpu ready
			{
				
			query2 = "select time_stamp,avg(disk_max_depth_queue) as avg,host_name from hostlog where host_name='"+vmname+"' and time_stamp < '"+curr+"' and time_stamp > '"+minusfive+"' group by time_stamp,host_name";//"+curr+"
			
				
			}
			else if(time=="day")
			{
				
				query2 = "select time_stamp,avg(disk_max_depth_queue) as avg,host_name from hostlog where host_name='"+vmname+"' and time_stamp < '"+curr+"' and time_stamp > '"+onedayprior+"' group by time_stamp,host_name";
				}
				
				else if(time=="week")
				{
					query2 = "select time_stamp,avg(disk_max_depth_queue) as avg,host_name from hostlog where host_name='"+vmname+"' and time_stamp < '"+curr+"' and time_stamp >'"+weekprior+"' group by time_stamp,host_name limit 30";
				}
				else if(time=="month")
				{
					
					query2 = "select time_stamp,avg(disk_max_depth_queue) as avg,host_name from hostlog where host_name='"+vmname+"' and time_stamp < '"+curr+"' and time_stamp >'"+monthprior+"' group by time_stamp,host_name limit 30";
				}
			
			
			System.out.println(query2);
			
				
			PreparedStatement stmt2 = conn.prepareStatement(query2);
			ResultSet rs2 = stmt2.executeQuery(query2);
			Timestamp afterfive = null;
			int flag=0;
			while(rs2.next())
			{
				
				
				
				
					
					list_timeStamp.put(rs2.getTimestamp("time_stamp"));
					list_cpu.put(rs2.getDouble("avg")/100);
					flag = 1;	
			
				
			
			}
			System.out.println(vmname);
			series_obj.put(hgraph.getName(),vmname );
			series_obj.put(hgraph.getData(),list_cpu );
			
			list_series.put(series_obj);
			list_cpu = new JSONArray(new ArrayList<String>());
			
			}
			
			//System.exit(0);
			//legend
			JSONObject legend_obj = new JSONObject();
			legend_obj.put(hgraph.getLayout(),hgraph.getHorz());
			legend_obj.put(hgraph.getAlign(),hgraph.getPosition());
			legend_obj.put(hgraph.getVerticalAlign(),hgraph.getBottom());
			legend_obj.put(hgraph.getBorderWidth(),"0" );
		
			
			//title
			JSONObject title_obj = new JSONObject();
			title_obj.put(hgraph.getText(),"VM CPU Usage(Mhz)" );
			title_obj.put("x","30" );
		
			
			//subtitle
			JSONObject subtitle_obj = new JSONObject();
			subtitle_obj.put(hgraph.getText(),"Usage Mhz" );
			subtitle_obj.put("x","30" );
		
			
			//percent
			JSONObject percent_obj = new JSONObject();
			percent_obj.put(hgraph.getText(),"Number");
		
			
			//plotLines
			JSONObject plotlines_obj = new JSONObject();
			plotlines_obj.put(hgraph.getValue(),"0" );
			plotlines_obj.put(hgraph.getWidth(),"1" );
			plotlines_obj.put(hgraph.getColor(),linecolor );
		
			JSONArray list_plot = new JSONArray();
			list_plot.put(plotlines_obj);
		
			//yAxis object
			JSONObject yAxis_obj = new JSONObject();
			yAxis_obj.put(hgraph.getTitlename(),percent_obj );
			yAxis_obj.put(hgraph.getPlotline(),list_plot );
		
			//tool
			JSONObject tool_obj = new JSONObject();
			tool_obj.put(hgraph.getValue_Suffix(),symbol );
			
			//xAxis
			JSONObject xAxis_obj = new JSONObject();
			xAxis_obj.put(hgraph.getCategory(),list_timeStamp );
			
			
			JSONObject final_obj = new JSONObject();
			final_obj.put(hgraph.getTitlename(), title_obj);
			final_obj.put(hgraph.getSubTitlename(), subtitle_obj);
			final_obj.put(hgraph.getxAxis(), xAxis_obj);
			final_obj.put(hgraph.getyAxis(), yAxis_obj);
			final_obj.put(hgraph.getTooltip(), tool_obj);
			final_obj.put(hgraph.getLegend(), legend_obj);
			final_obj.put(hgraph.getSeries(), list_series);
			String finalObj = final_obj.toString();
			System.out.println(finalObj);
			return finalObj;
			
			
	}catch(Exception e){ e.printStackTrace();return null;}
		
		
	}


}
