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

import proj.zoie.service.api.SearchRequest;


public class ZoieServiceServlet extends HttpServlet {

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
			System.out.println("Problem initializing Velocity : " + e);
			return;
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
		String user_id = request.getParameter("user_id");
		String network_id = request.getParameter("network_id");

		// set content-type header before accessing the Writer
		response.setCharacterEncoding("UTF-8"); 
		response.setContentType("text/html,charset=UTF-8");
		response.setBufferSize(8192);
		VelocityContext context = new VelocityContext();
		PrintWriter out = response.getWriter();
		
		if (user_id!=null && network_id!=null && queryString != null && queryString.length() > 0) {
			try {

				SearchResult result = searcher.search(network_id,user_id,queryString);

				context.put("result", result);
				context.put("user_id", user_id);
				context.put("network_id", network_id);
				context.put("q",queryString);
				context.put("hits", result.getHitItems());

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		Template search_tmpl = Velocity.getTemplate("search.vm");
		search_tmpl.merge(context, out);

	}

	public String getServletInfo() {
		return "Zoie Service Loader Servlet.";
	}

}
