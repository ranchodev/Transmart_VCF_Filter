/*************************************************************************
*   This file is part of Transmart_VCF_Filter.
*
*   Transmart_VCF_Filter is free software: you can redistribute it and/or modify
*   it under the terms of the GNU Lesser General Public License as published by
*   the Free Software Foundation, either version 3 of the License, or
*   (at your option) any later version.
*
*   Transmart_VCF_Filter is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU Lesser General Public License for more details.
*
*   You should have received a copy of the GNU Lesser General Public License
*   along with Transmart_VCF_Filter.  If not, see <http://www.gnu.org/licenses/>.
**************************************************************************/

package com.rancho.transmartvcffilter.utilities;

import java.io.BufferedReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public final class Utilities {
	private static Configuration cfg = new Configuration().configure("hibernate.cfg.xml");
	
	private static SessionFactory factory = cfg.buildSessionFactory();
	private Utilities (){
		
	}
	
	public static SessionFactory getSessionFactory(){
		return factory;
	}
	
	public static String getHttpRequestPayload(HttpServletRequest request) throws UnsupportedEncodingException{
		StringBuilder sb = new StringBuilder();
    	String line;
    	try {
    		BufferedReader reader = request.getReader();
    		while ((line = reader.readLine()) != null){
    			sb.append(line);
    		}
    	} catch (Exception e) {
    		System.out.println(e.getMessage());
    	}
    	//termParamter = URLEncoder.encode(request.getParameter("term"), "UTF-8").replace("+", "%20");
    	String result = URLDecoder.decode(sb.toString().replace("+", " "), "UTF-8");
    	//System.out.println(URLDecoder.decode(result, "UTF-8"));
    	return  result;
	}
}
