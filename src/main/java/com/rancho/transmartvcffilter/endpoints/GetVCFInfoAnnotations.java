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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
 * Servlet implementation class GetVCFInfoAnnotations
 */
@WebServlet(description = "Given a VCF study, this endpoint gets all the available annotation values", urlPatterns = { "/api/vcf/annfields/get" })
public class GetVCFInfoAnnotations extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static Logger logger =   LogManager.getLogger("com.rancho.transmartvcffilter"); 
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetVCFInfoAnnotations() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		logger.info("Generating Annotation field options");
		String study = "";
		//String httpRequestPayload = Utilities.getHttpRequestPayload(request);
		//JSONObject requestJSON = new JSONObject(httpRequestPayload);
		HashMap <String, HashSet<String>> annotationHash = new HashMap <String, HashSet<String>>();
		if (request.getParameterMap().containsKey("study")){
			study = request.getParameter("study");
		}
		else {
			response.getWriter().append("No Study parameter selected");
			return;
		}
		if (study.equals("")){
			response.getWriter().append("The study parameter cannot be an empty string");
			return;
		}
		String sqlQuery= "SELECT distinct(text_value) ";
		sqlQuery = sqlQuery + "from deapp.de_variant_subject_summary a ";
		sqlQuery = sqlQuery + "left join deapp.de_variant_population_data b on a.chr=b.chr and a.pos=b.pos ";
		sqlQuery = sqlQuery + "left join deapp.de_subject_sample_mapping c on c.sample_cd=a.subject_id ";
		sqlQuery = sqlQuery + "where c.trial_name='"+study+"' and info_name='ANN';";
		Session session = Utilities.getSessionFactory().openSession();
    	
		SQLQuery query = session.createSQLQuery(sqlQuery);
		logger.info("Annotation field options sql query is " + sqlQuery);
		query.addScalar("text_value");
		
		List<String> queryResult = query.list();
		logger.info("Query complete...aggregating results");
		for (int i=0; i<queryResult.size(); i++){
			System.out.println(queryResult.get(i).toString());
			String[] annotationValues = queryResult.get(i).split("\\|");
			System.out.println( Arrays.toString(annotationValues));
			for (int j=0; j<annotationValues.length; j++){
				if (!annotationHash.containsKey("ANNOTATION_" + Integer.toString(j))){
					HashSet <String> annotationValueOptions = new HashSet <String>();
					annotationValueOptions.add(annotationValues[j]);
					annotationHash.put("ANNOTATION_" + Integer.toString(j), annotationValueOptions);
				}
				else {
					annotationHash.get("ANNOTATION_" + Integer.toString(j)).add(annotationValues[j]);
				}
			}
			
		}
		
		Iterator iter = annotationHash.entrySet().iterator();
		JSONArray result = new JSONArray();
		while (iter.hasNext()){
			JSONObject annotationDropdownObj = new JSONObject();
			JSONArray annotationDropdownOptions = new JSONArray();
			Map.Entry<String, HashSet<String>> entrySet = (Map.Entry<String, HashSet<String>>)iter.next();
			HashSet <String> annotationValueOptions = annotationHash.get(entrySet.getKey());
			Iterator iter2 = annotationValueOptions.iterator();
			while (iter2.hasNext()){
				annotationDropdownOptions.put(iter2.next());
			}
			annotationDropdownObj.put(entrySet.getKey(), annotationDropdownOptions);
			result.put(annotationDropdownObj);
		}
		logger.info("Aggregation complete");
		response.getWriter().append(result.toString());
		
	}

}
