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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class VCFFilterDownload
 */
@WebServlet(description = "Takes in a VCF file name and downloads the file if it exists", urlPatterns = { "/api/download/filter/results" })
public class VCFFilterDownload extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public VCFFilterDownload() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String filterResultFile = request.getParameter("filename");
		String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename="+filterResultFile);
        response.setContentType("application/octet-stream");
        response.setHeader(headerKey, headerValue);
        File file = new File ("/tmp/"+filterResultFile);
    	response.setContentLength((int)file.length());
    	OutputStream out = response.getOutputStream();
    	FileInputStream in = new FileInputStream("/tmp/"+filterResultFile);
    	byte[] buffer = new byte[4096];
    	int length;
    	while ((length = in.read(buffer)) > 0){
    	    out.write(buffer, 0, length);
    	}
    	in.close();
    	out.flush();
		//response.getWriter().append("Served at: ").append(request.getContextPath());
	}

}
