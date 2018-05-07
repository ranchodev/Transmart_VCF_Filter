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
 * Servlet implementation class GetVCFStudies
 */
@WebServlet(description = "Given a VCF study, this endpoint gets all the available annotation values", urlPatterns = { "/api/vcf/studies/get" })
public class GetVCFStudies extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static Logger logger =   LogManager.getLogger("com.rancho.transmartvcffilter");    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetVCFStudies() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		JSONArray result = new JSONArray();
		Session session = Utilities.getSessionFactory().openSession();
		
		SQLQuery query = session.createSQLQuery("select DISTINCT(dataset_id) from deapp.de_variant_dataset");
		
		
		query.addScalar("dataset_id");
		
		List<?> queryResult = query.list();
		logger.info("Formatting query results");
		logger.info("Data is " + queryResult.toString());
		JSONObject vcfStudyBlankEntry = new JSONObject();
		vcfStudyBlankEntry.put("dataset_id", "BLANK");
		result.put(vcfStudyBlankEntry);
		for (int i=0; i<queryResult.size(); i++){
			JSONObject vcfStudy = new JSONObject();
			String[] vcfStudyResult = queryResult.get(i).toString().split(":");
			if (vcfStudyResult.length>1){
				vcfStudy.put("dataset_id", vcfStudyResult[0]);
			}
			else{
				vcfStudy.put("dataset_id", "Error_"+i);
			}
				
			
			result.put(vcfStudy);
		}
		session.close();
		response.getWriter().append(result.toString());
	}

}
