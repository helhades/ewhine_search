package com.ewhine.search;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import cn.gov.cbrc.wh.log.Log;
import cn.gov.cbrc.wh.log.LogFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ZoieServiceServlet extends HttpServlet {

	final private static Log log = LogFactory.getLog(ZoieServiceServlet.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	EwhineZoieSearchService searcher = null;
	ZoieIndexService indexer = null;
	private IndexServer indexServer;

	public ZoieServiceServlet() {
		// TODO Auto-generated constructor stub
	}

	public void init() throws ServletException {

		indexer = new ZoieIndexService();
		indexServer = new IndexServer(indexer);

		EwhineIndexReaderFactory factory = new EwhineIndexReaderFactory(indexer);

		searcher = new EwhineZoieSearchService(factory);

		indexer.start();
		indexServer.start();

		try {
			Properties p = new Properties();

			p.setProperty(RuntimeConstants.RESOURCE_LOADER, "class");
			p.setProperty("class.resource.loader.class",
					ClasspathResourceLoader.class.getName());
			p.setProperty("output.encoding", "UTF-8");
			p.setProperty("input.encoding", "UTF-8");

			Velocity.init(p);
		} catch (Exception e) {
			if (log.isErrorEnabled()) {
				log.error("Problem initializing Velocity.", e);
			}		
			return;
		}
		
		if (log.isInfoEnabled()) {
			
		}

		System.out.println("index server is starting.");

	}

	public void destroy() {

		if (indexServer != null) {
			System.out.print("index server is stopping...");
			indexServer.stop();
			System.out.println("done.");
		}
		if (indexer != null) {
			System.out.print("indexer service is stopping...");
			indexer.stop();
			System.out.println("done.");
		}

	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		
		request.setCharacterEncoding("UTF-8");
		String queryString = request.getParameter("q");
		String type_id = request.getParameter("type_id");
		String user_id = request.getParameter("user_id");
		String format = request.getParameter("format");

		// set content-type header before accessing the Writer

		SearchResult result = null;
		if (user_id != null && queryString != null && queryString.length() > 0) {
			try {
				long u_id = Long.parseLong(user_id);
				result = searcher.search(u_id, queryString,type_id);

			} catch (Exception e) {
				if (log.isErrorEnabled()) {
					log.error("Search q:" + queryString + ",user_id:" + user_id+" error.",e);
				}
			}

		}

		if ("json".equals(format)) {
			response.setCharacterEncoding("UTF-8");
			response.setContentType("application/json");
			response.setBufferSize(8192);

			PrintWriter out = response.getWriter();
			Gson gson = new GsonBuilder().setFieldNamingPolicy(
					FieldNamingPolicy.LOWER_CASE_WITH_DASHES).create();
			out.println(gson.toJson(result));
			out.flush();
			out.close();

		} else {
			response.setCharacterEncoding("UTF-8");
			response.setContentType("text/html");
			response.setBufferSize(8192);
			VelocityContext context = new VelocityContext();
			PrintWriter out = response.getWriter();
			Template search_tmpl = Velocity.getTemplate("search.vm");
			context.put("result", result);
			context.put("user_id", user_id);
			context.put("q", queryString);
			if (result != null) {
				context.put("hits", result.getHitItems());
			}
			search_tmpl.merge(context, out);
			out.flush();
			out.close();
		}

	}

	public String getServletInfo() {
		return "Zoie Service Loader Servlet.";
	}

}
