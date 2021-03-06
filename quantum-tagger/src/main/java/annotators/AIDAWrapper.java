package annotators;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import data.WebDocument;
import executer.RunTimeAnalysis;

public class AIDAWrapper {

	private static Logger slogger_ = LoggerFactory.getLogger(WebDocument.class);
	private static String url = "https://gate.d5.mpi-inf.mpg.de/aida/service/disambiguate";

	public String process(String text,String technique) {
		String result=null;
		try {
			long temp = System.nanoTime();
			result = disambiguate_mentions(text,technique);
			RunTimeAnalysis.webservice_call_time += System.nanoTime() - temp;
		} catch (Exception e) {
			slogger_.error(e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	public String disambiguate_mentions(String text, String technique) throws IOException {
		StringBuffer results = new StringBuffer();
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		try {
			HttpPost request = new HttpPost(url);
			StringEntity params = new StringEntity(
					"{ \"technique\" : \""+technique+"\", \"tagMode\" : \"manual\", "
							+ "\"nullMappingThreshold\" : 0.0, \"text\" : \""
							+ text + "\","+ "\"maxResults\" : \"20\",} ","UTF8");// + "\"maxResults\" : \"20\",} "
			request.addHeader("content-type", "application/json");
//			request.addHeader("Content-Type", "charset=UTF8");
			request.setEntity(params);
			
			HttpResponse response = httpClient.execute(request);
			//Read Response
			if (response.getStatusLine().getStatusCode() == 200) {
				BufferedReader in = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent(),"UTF8"));
				String inputLine;
				
				while ((inputLine = in.readLine()) != null) {
					results.append(inputLine);
				}
				in.close();
			}
		} catch (Exception ex) {
			slogger_.error(ex.getMessage());
			ex.printStackTrace();
		} finally {
			httpClient.close(); // Deprecated
		}
		return results.toString();
	}

//	private String callWebservice(String text) throws Exception {
//
//		String url_s = "https://gate.d5.mpi-inf.mpg.de/aida/service/disambiguate";
//		URL url = new URL(url_s);
//		HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
//
//		// add request header
//		con.setRequestMethod("POST");
//		con.setDoOutput(true);
//		con.setRequestProperty("Content-Type", "application/json");
//
//		// writing request parameter
//		DataOutputStream out_stream = new DataOutputStream(
//				con.getOutputStream());
//		StringBuilder post_data = new StringBuilder();
//		/*
//		 * post_data.append(URLEncoder.encode("text= \"", "UTF-8"));
//		 * post_data.append(URLEncoder.encode(text, "UTF-8"));
//		 * post_data.append(URLEncoder.encode("\"", "UTF-8"));
//		 */
//		post_data.append("text= \"");
//		post_data.append(text);
//		post_data.append("\"");
//		byte[] post_data_bytes = post_data.toString().getBytes();
//		out_stream.write(post_data_bytes);
//		out_stream.flush();
//		out_stream.close();
//		// set content length
//		//con.setRequestProperty("Content-Length",
//			//	Integer.toString(post_data_bytes.length));
//		int response_code = con.getResponseCode();
//		if (response_code != 200) {
//			slogger_.error("HTTP request error: "
//					+ String.valueOf(response_code));
//			return null;
//		}
//		BufferedReader in = new BufferedReader(new InputStreamReader(
//				con.getInputStream()));
//		String inputLine;
//		StringBuffer response = new StringBuffer();
//
//		while ((inputLine = in.readLine()) != null) {
//			response.append(inputLine);
//		}
//		in.close();
//
//		// print result
//		String result = response.toString();
//		return result;
//
//	}

}
