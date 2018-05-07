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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.json.JSONArray;
import org.json.JSONObject;

import com.rancho.transmartvcffilter.utilities.Utilities;

/**
 * Servlet implementation class VCFFilterDownload
 */
@WebServlet(description = "Performs VCF Filtering and returns the result as a CSV file", urlPatterns = { "/api/vcf/filter/download" })
public class VCFFilterPrepareDownload extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static Logger logger =   LogManager.getLogger("com.rancho.transmartvcffilterr");   
    /**
     * @see HttpServlet#HttpServlet()
     */
    public VCFFilterPrepareDownload() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    private JSONArray getSampleIdsForCohort (String cohortOperator, JSONArray cohortFilterParams){
    	JSONArray result = new JSONArray();
    	logger.info("Getting sample ids for selected cohort");
    	String queryString = convertCohortParamsToSQLQuery(cohortOperator, cohortFilterParams, false);
    	logger.info("Cohort query string is " + queryString);
    	if (queryString.startsWith("Error:")){
    		return result;
    		
    	}
    	logger.debug("query string is " + queryString);
    	/*if (!queryString.startsWith("SELECT d.sample_cd FROM")){
    		result.put(queryString);
    		return result;
    	}*/
    
    	Session session = Utilities.getSessionFactory().openSession();
    	
		SQLQuery query = session.createSQLQuery(queryString);
		query.addScalar("sample_cd");
		
		List<String> queryResult = query.list();
		for (int i=0; i<queryResult.size(); i++){
			result.put(queryResult.get(i));
		}
		session.close();
		return result;
    	
    }
    
    private String convertCohortParamsToSQLQuery (String cohortOperator, JSONArray cohortFilterParams, boolean applyEndSemiColon){
    	logger.info("Generating cohort selection sql string");
    	String sqlQuery = "";
    	
    	ArrayList<String> comparatorOptions = new ArrayList<String>();
    	comparatorOptions.add("<");
    	comparatorOptions.add("<=");
		comparatorOptions.add("=");
		comparatorOptions.add(">");
		comparatorOptions.add(">=");
    	for (int i=0; i<cohortFilterParams.length(); i++){
    		//String comparator = null;
    		JSONObject cohortFilterParam = cohortFilterParams.getJSONObject(i);
    		if (!cohortFilterParam.has("conceptPath")){
    			logger.error("Error: No conceptPath was found for one of the  cohortParams");
    			return "Error: No conceptPath was found for one of the  cohortParams";
    			
    		}
    		if (!cohortFilterParam.has("comparator")){
    			logger.error("Error: No comparator was found for one of the  cohortParams");
    			return "Error: No comparator was found for one of the  cohortParams";
    			
    		}
    		if (!cohortFilterParam.has("value")){
    			logger.error("Error: No value was found for one of the  cohortParams");
    			return "Error: No value was found for one of the  cohortParams";
    			
    		}
    		if (cohortFilterParam.getString("conceptPath").trim().equals("")){
    			logger.error("Error: conceptPath must be non empty");
    			return "Error: conceptPath must be non empty";
			}
    		if (cohortFilterParam.getString("comparator").trim().equals("")){
    			logger.error("Error: comparator must be non empty");
    			return "Error: comparator must be non empty";
			}
    		if (cohortFilterParam.getString("value").trim().equals("")){
    			logger.error("Error: value must be non empty");
    			return "Error: value must be non empty";
			}
    		
    		sqlQuery=sqlQuery + "SELECT d.sample_cd FROM i2b2demodata.concept_dimension a ";
    		sqlQuery=sqlQuery + "LEFT JOIN i2b2demodata.observation_fact b ON a.concept_cd=b.concept_cd ";
    		sqlQuery=sqlQuery + "LEFT JOIN i2b2demodata.patient_dimension c ON b.patient_num=c.patient_num ";
    		sqlQuery=sqlQuery + "LEFT JOIN deapp.de_subject_sample_mapping d ON b.patient_num=d.patient_id ";
    		sqlQuery=sqlQuery + "WHERE ";
    		sqlQuery = sqlQuery + "(concept_path = '" + cohortFilterParam.getString("conceptPath") + "' AND ";
    		if (NumberUtils.isNumber(cohortFilterParam.getString("value"))){
    			sqlQuery = sqlQuery + "nval_num " + cohortFilterParam.getString("comparator") + "'" + cohortFilterParam.getString("value") +"') ";
    		}
    		else {
    			sqlQuery = sqlQuery + "tval_char " + cohortFilterParam.getString("comparator") + "'" + cohortFilterParam.getString("value") +"') ";
    		}
    		if (i<cohortFilterParams.length()-1){
    			if (cohortOperator.equals("and")){
    				sqlQuery = sqlQuery + "INTERSECT ";
    			}
    			else if (cohortOperator.equals("or")) {
    				sqlQuery = sqlQuery + "UNION ";
    			}
    		}
    		else if (applyEndSemiColon){
    			sqlQuery = sqlQuery + ";";
    		}
    		
    	}
    	
    	return sqlQuery;
    	
    }
    
    private String convertVariantFilterParamsToSQLQuery (JSONArray cohortFilterParams, String cohortOperator, JSONArray variantInfoFilterParams, String variantOperator, String subjectGenotype, String study, String filterParam, String zygosity, Integer limit, Integer offset){
    	logger.info("Generating variant sql query");
    	String sqlQuery = "";
    	boolean considerCohortsFlag=false;
    	
    	if (cohortFilterParams.length()>0){
    		JSONArray cohortSampleIdsFound = getSampleIdsForCohort (cohortOperator, cohortFilterParams);
    		logger.debug("we found the following cohort sample ids " + cohortSampleIdsFound.toString());
    		if (cohortSampleIdsFound.length()==0){
    			considerCohortsFlag=true;
    		}
    		else {
    			for (int i=0; i<cohortSampleIdsFound.length(); i++){
    				if (!cohortSampleIdsFound.isNull(i)){
    					considerCohortsFlag=true;
    				}
    			}
    		}
    	}
    	ArrayList<String> comparatorOptions = new ArrayList<String>();
    	comparatorOptions.add("<");
    	comparatorOptions.add("<=");
		comparatorOptions.add("=");
		comparatorOptions.add(">");
		comparatorOptions.add(">=");
		
		ArrayList<String> zygosityOptions = new ArrayList<String>();
		zygosityOptions.add("NA");
		zygosityOptions.add("homo ref");
		zygosityOptions.add("homo alt");
		zygosityOptions.add("het");
		
		sqlQuery=sqlQuery + "select * from (";
		for (int i=0; i<variantInfoFilterParams.length(); i++){
			JSONObject variantInfoFilterParam = variantInfoFilterParams.getJSONObject(i);
			if (!variantInfoFilterParam.has("infoField")){
				logger.error("Error: No infoField found for one of the  variantInfoFilterParams");
				return "Error: No infoField found for one of the  variantInfoFilterParams";
			}
			if (!variantInfoFilterParam.has("comparator")){
				logger.error("Error: No comparator was found for one of the  variantInfoFilterParams");
				return "Error: No comparator was found for one of the  variantInfoFilterParams";
			}
			if (!variantInfoFilterParam.has("value")){
				logger.error("Error: No value was found for one of the  variantInfoFilterParams");
				return "Error: No value was found for one of the  variantInfoFilterParams";
			}
			if (variantInfoFilterParam.getString("infoField").trim().equals("")){
				logger.error("Error: infoField cannot be empty");
				return "Error: infoField cannot be empty";
			}
			if (variantInfoFilterParam.getString("comparator").trim().equals("")){
				logger.error("Error: comparator cannot be empty");
				return "Error: comparator cannot be empty";
			}
			if (variantInfoFilterParam.getString("value").trim().equals("")){
				logger.error("Error: value cannot be empty");
				return "Error: value cannot be empty";
			}
			if (!comparatorOptions.contains(variantInfoFilterParam.getString("comparator"))){
				logger.error("Error: The comparator in the varientInfoFilterParam must either be '<', '>' or '='");
				return "Error: The comparator in the varientInfoFilterParam must either be '<', '>' or '='";
			}
			if (!zygosityOptions.contains(zygosity) && !zygosity.trim().equals("")){
				logger.error("Error: The zygosity in the varientInfoFilterParam must either be 'het', 'homo' or 'NA'");
				return "Error: The zygosity in the varientInfoFilterParam must either be 'het', 'homo' or 'NA'";
			}
			
			sqlQuery=sqlQuery + "select c.trial_name, c.platform, c.subject_id, c.sample_cd, a.chr, a.pos, ";
			sqlQuery=sqlQuery + "a.allele1, a.allele2, d.rs_id, d.\"ref\", d.alt, d.qual, d.\"filter\", ";
			sqlQuery=sqlQuery + "d.info, d.format, d.variant_value, e.\"position\" ";
			sqlQuery=sqlQuery + "from deapp.de_variant_subject_summary a ";
			sqlQuery=sqlQuery + "left join deapp.de_variant_population_data b on a.chr=b.chr and a.pos=b.pos ";
			sqlQuery=sqlQuery + "left join deapp.de_subject_sample_mapping c on c.sample_cd=a.subject_id ";
			sqlQuery=sqlQuery + "left join deapp.de_variant_subject_detail d on b.chr=d.chr and b.pos=d.pos ";
			sqlQuery=sqlQuery + "left join deapp.de_variant_subject_idx e on e.subject_id=a.subject_id ";
			sqlQuery=sqlQuery + "where  c.trial_name='"+ study +"' and ";
			if (!filterParam.trim().equals("")){
				sqlQuery=sqlQuery + "d.filter='"+ filterParam +"' and ";
			}
			if (zygosity.equals("het")){
				sqlQuery=sqlQuery + "(a.allele1!=a.allele2) and ";
			}
			else if (zygosity.equals("homo ref")){
				sqlQuery=sqlQuery + "(a.allele1=0 and a.allele2=0) and ";
			}
			else if (zygosity.equals("homo alt")){
				sqlQuery=sqlQuery + "(a.allele1=1 and a.allele2=1) and ";
			}
			else if (zygosity.equals("NA")){
				sqlQuery=sqlQuery + "(a.allele1 is null and a.allele2 is null) and ";
			}
			sqlQuery=sqlQuery + "c.platform='VCF' ";
			String cohortSelectionQuery = convertCohortParamsToSQLQuery(cohortOperator, cohortFilterParams, false);
			if (cohortSelectionQuery.startsWith("Error:")){
				return cohortSelectionQuery;
			}
			if (considerCohortsFlag){
				sqlQuery=sqlQuery + "and c.sample_cd in ("+cohortSelectionQuery+") ";
			}
			else {
				logger.info("no valid cohort sample ids found.  We will not use cohorts in the filter");
			}
			sqlQuery=sqlQuery + " and (b.info_name='" + variantInfoFilterParam.getString("infoField") + "' and ";
			if (variantInfoFilterParam.getString("infoField").equals("ANN")){
				sqlQuery=sqlQuery + "b.text_value ~* '(^|\\|)" + variantInfoFilterParam.getString("value") + "\\|.*') ";
			}
			else {
				//sqlQuery=sqlQuery + " and (b.info_name='" + variantInfoFilterParam.getString("infoField") + "' and ";
				if (NumberUtils.isNumber(variantInfoFilterParam.getString("value"))){
					try{
						Integer.parseInt(variantInfoFilterParam.getString("value"));
						// is an integer!
						sqlQuery=sqlQuery + "b.integer_value " + variantInfoFilterParam.getString("comparator") + " " + variantInfoFilterParam.getString("value") + ") ";

					} catch (NumberFormatException e) {
						// not an integer!
						sqlQuery=sqlQuery + "b.float_value " +  variantInfoFilterParam.getString("comparator") + " " + variantInfoFilterParam.getString("value") + ") ";
					}
				}
				else {
					sqlQuery=sqlQuery + "b.text_value = '" + variantInfoFilterParam.getString("value") + "') ";
				}
			}
			
			
			if (i<variantInfoFilterParams.length()-1){
    			if (variantOperator.equals("and")){
    				sqlQuery = sqlQuery + " INTERSECT ";
    			}
    			else if (variantOperator.equals("or")) {
    				sqlQuery = sqlQuery + " UNION ";
    			}
    		}
    		/*else {
    			sqlQuery = sqlQuery + ";";
    		}*/
			
			
			
		}
		if (limit == 0){
			sqlQuery = sqlQuery + ") l ORDER BY l.sample_cd, l.chr, l.pos LIMIT ALL;" ;
		}
		else {
			sqlQuery = sqlQuery + ") l ORDER BY l.sample_cd, l.chr, l.pos LIMIT " + limit + " OFFSET " + offset + ";";
			
		}
		
		
    	return sqlQuery;
    }
    
    private String filterVCF (JSONArray cohortFilterParams, String cohortOperator, JSONArray variantInfoFilterParams, String variantOperator, String subjectGenotype, String study, String filterParam, String zygosity, Integer limit, Integer offset){
    	logger.info("Filtering VCF");
    	String result = "";
    	//HashMap <String, JSONArray> resultAggregator = new HashMap <String, JSONArray>();
    	String vcfFilterQuery = convertVariantFilterParamsToSQLQuery(cohortFilterParams, cohortOperator, variantInfoFilterParams, variantOperator, subjectGenotype, study, filterParam, zygosity, limit, offset);
    	String cohortSearchString = "";
    	for (int i=0; i<cohortFilterParams.length(); i++){
    		cohortSearchString = cohortSearchString + cohortFilterParams.getJSONObject(i).getString("conceptPath") + " ";
    		cohortSearchString = cohortSearchString + cohortFilterParams.getJSONObject(i).getString("comparator") + " ";
    		cohortSearchString = cohortSearchString + cohortFilterParams.getJSONObject(i).getString("value");
    		
    		if (i<cohortFilterParams.length()-1){
    			cohortSearchString = cohortSearchString + " " + cohortOperator + " ";
    		}
    	}
    	logger.info("Final VCF filter sql query is " + vcfFilterQuery);
    	if (vcfFilterQuery.startsWith("Error:")){
    		result=vcfFilterQuery;
    		return result;
    	}
    	//System.out.println("VCF query is " + vcfFilterQuery);
    	
    	Session session = Utilities.getSessionFactory().openSession();
    	
		SQLQuery query = session.createSQLQuery(vcfFilterQuery);
		query.addScalar("trial_name");
		query.addScalar("platform");
		query.addScalar("subject_id");
		query.addScalar("sample_cd");
		query.addScalar("chr");
		query.addScalar("pos");
		query.addScalar("allele1");
		query.addScalar("allele2");
		query.addScalar("rs_id");
		query.addScalar("ref");
		query.addScalar("alt");
		query.addScalar("qual");
		query.addScalar("filter");
		query.addScalar("info");
		query.addScalar("format");
		query.addScalar("variant_value");
		query.addScalar("position");
		
		List<Object[]> queryResult = query.list();
		logger.info("Query complete....generating the CSV");
		String csvString = "SUBJECT ID\tSAMPLE ID\tCHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\tFORMAT\tSAMPLE DATA\tCOHORT SEARCH PARAMS\n";
		for (int i=0; i<queryResult.size(); i++){
			csvString =csvString + queryResult.get(i)[2].toString() + "\t";
			csvString =csvString + queryResult.get(i)[3].toString() + "\t";
			csvString =csvString + queryResult.get(i)[4].toString() + "\t";
			csvString =csvString + queryResult.get(i)[5].toString() + "\t";
			csvString =csvString + queryResult.get(i)[8].toString() + "\t";
			csvString =csvString + queryResult.get(i)[9].toString() + "\t";
			csvString =csvString + queryResult.get(i)[10].toString() + "\t";
			csvString =csvString + queryResult.get(i)[11].toString() + "\t";
			csvString =csvString + queryResult.get(i)[12].toString() + "\t";
			csvString =csvString + queryResult.get(i)[13].toString() + "\t";
			csvString =csvString + queryResult.get(i)[14].toString() + "\t";
			String[] variantValueData = queryResult.get(i)[15].toString().split("\\t");
			csvString =csvString + variantValueData[Integer.parseInt(queryResult.get(i)[16].toString())-1] + "\t";
			if (cohortSearchString.equals("")){
				csvString =csvString + "NO COHORT FILTER\n";
			}
			else {
				csvString =csvString + cohortSearchString + "\n";
			}
			
			
			
			
			
			
			
		}
		
		
    	session.close();
    	return csvString;
    	
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String httpRequestPayload = Utilities.getHttpRequestPayload(request);
		logger.debug("PAYLOAD IS " + httpRequestPayload);
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date();
		String filterResultFile = "vcffilterresults_"+dateFormat.format(date)+".tsv";
		
		ArrayList<String> operatorOptions = new ArrayList<String>();
		operatorOptions.add("or");
		operatorOptions.add("and");
 		String filterParam = "";
		String cohortOperator = "or";
		String study = "";
		String zygosity = "";
		Integer limit = 0;
		Integer offset = 0;
		JSONArray cohortParams = new JSONArray();
		String subjectGenotype = "";
		String variantOperator = "or";
		JSONArray variantInfoFilterParams = new JSONArray();
		JSONObject requestJSON = new JSONObject(httpRequestPayload);
		
		logger.debug("here 1");
		if (requestJSON.has("cohortOperator")){
			if (operatorOptions.contains(requestJSON.getString("cohortOperator").toLowerCase())){
				cohortOperator = requestJSON.getString("cohortOperator");
			}
		}
		
		if (requestJSON.has("study")){
			study = requestJSON.getString("study");
		}
		
		if (requestJSON.has("zygosity")){
			zygosity = requestJSON.getString("zygosity");
		}
		
		if (requestJSON.has("filterParam")){
			filterParam = requestJSON.getString("filterParam");
		}
		
		if (requestJSON.has("cohortParams")){
			cohortParams = requestJSON.getJSONArray("cohortParams");
		}
		
		if (requestJSON.has("subjectGenotype")){
			subjectGenotype = requestJSON.getString("subjectGenotype");
		}
		
		if (requestJSON.has("variantOperator")){
			if (operatorOptions.contains(requestJSON.getString("variantOperator").toLowerCase())){
				variantOperator = requestJSON.getString("variantOperator");
			}
			
		}
		
		if (requestJSON.has("variantInfoFilterParams")){
			variantInfoFilterParams = requestJSON.getJSONArray("variantInfoFilterParams");
		}
		
		if (requestJSON.has("limit")){
			if (NumberUtils.isNumber(requestJSON.getString("limit"))){
				limit = Integer.parseInt(requestJSON.getString("limit"));
			}
		}
		
		if (requestJSON.has("offset")){
			if (NumberUtils.isNumber(requestJSON.getString("offset"))){
				offset = Integer.parseInt(requestJSON.getString("offset"));
			}
		}
		
		logger.debug("here 2");
		//JSONArray cohortSampleIds = getSampleIdsForCohort(cohortOperator, cohortParams);
		logger.debug("here 3");
		//System.out.println("cohort sample ids are " + cohortSampleIds);
		
		String result = filterVCF(cohortParams, cohortOperator, variantInfoFilterParams, variantOperator, subjectGenotype, study, filterParam, zygosity, limit, offset);
		
    	Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream("/tmp/"+filterResultFile), "utf-8"));
    	
    	writer.write(result);
    	writer.close();
    	
    	response.getWriter().append(filterResultFile);
    	/*String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename="+filterResultFile);
        response.setContentType("application/octet-stream");
        response.setHeader(headerKey, headerValue);
        File file = new File ("/tmp/vcffilterresults.csv");
    	response.setContentLength((int)file.length());
    	OutputStream out = response.getOutputStream();
    	FileInputStream in = new FileInputStream("/tmp/"+filterResultFile);
    	byte[] buffer = new byte[4096];
    	int length;
    	while ((length = in.read(buffer)) > 0){
    	    out.write(buffer, 0, length);
    	}
    	in.close();
    	out.flush();*/
	}

}
