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

package com.rancho.transmartvcffilter.endpoints;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

import org.json.JSONArray;
import org.json.JSONObject;

import com.rancho.transmartvcffilter.utilities.Utilities;



/**
 * Servlet implementation class GetVCFInfoFields
 */
@WebServlet(description = "Gets the available INFO fields for the VCF of interest", urlPatterns = { "/api/vcf/infofields/get" })
public class GetVCFInfoFields extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static Logger logger =   LogManager.getLogger("com.rancho.transmartvcffilter");   
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetVCFInfoFields() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		logger.info("Generating info field options");
		JSONArray result = new JSONArray();
		Session session = Utilities.getSessionFactory().openSession();
		SQLQuery query = null;
		if (request.getParameter("study")!=null && !request.getParameter("study").trim().equals("") && !request.getParameter("study").trim().equals("BLANK")){
			query = session.createSQLQuery("SELECT * FROM deapp.de_variant_population_info WHERE dataset_id LIKE '%" + request.getParameter("study")+"%'");
			logger.info("Info field options sql query is " + "SELECT * FROM deapp.de_variant_population_info WHERE dataset_id LIKE '%" + request.getParameter("study")+"%'");
		}
		else if(request.getParameter("study")==null) {
			query = session.createSQLQuery("SELECT * FROM deapp.de_variant_population_info");
			logger.info("Info field options sql query is " + "SELECT * FROM deapp.de_variant_population_info");
		}
		else {
			response.getWriter().append(result.toString());
			session.close();
			return;
		}
		
		query.addScalar("info_name").addScalar("type");
		
		List<Object[]> queryResult = query.list();
		logger.info("Formatting query results");
		for (int i=0; i<queryResult.size(); i++){
			JSONObject vcfInfo = new JSONObject();
			for (int j=0; j<queryResult.get(i).length; j++){
				
				switch (j) {
				case 0:
					vcfInfo.put("info_name", queryResult.get(i)[j]);
					break;
				case 1:
					vcfInfo.put("info_type", queryResult.get(i)[j]);
					break;
				}
				
				
				
				
				
			}
			result.put(vcfInfo);
		}
		session.close();
		response.getWriter().append(result.toString());
	}

}
