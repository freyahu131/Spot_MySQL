package idv.ron.server.spots;

import idv.ron.server.main.ImageUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Base64;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@SuppressWarnings("serial")
@WebServlet("/SpotServlet")
public class SpotServlet extends HttpServlet {
	private final static String CONTENT_TYPE = "text/html; charset=utf-8";

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		Gson gson = new Gson();
		BufferedReader br = request.getReader();
		StringBuffer jsonIn = new StringBuffer();
		String line = null;
		while ((line = br.readLine()) != null) {
			jsonIn.append(line);
		}
		System.out.println("input: " + jsonIn);
		
		JsonObject jsonObject = gson.fromJson(jsonIn.toString(),
				JsonObject.class);
		SpotDao spotDao = new SpotDaoMySqlImpl();
		String action = jsonObject.get("action").getAsString();

		if (action.equals("getAll")) {
			List<Spot> spots = spotDao.getAll();
			writeText(response, gson.toJson(spots));
		} else if (action.equals("getImage")) {
			OutputStream os = response.getOutputStream();
			int id = jsonObject.get("id").getAsInt();
			int imageSize = jsonObject.get("imageSize").getAsInt();
			byte[] image = spotDao.getImage(id);
			if (image != null) {
				image = ImageUtil.shrink(image, imageSize);
				response.setContentType("image/jpeg");
				response.setContentLength(image.length);
			}
			os.write(image);
		} else if (action.equals("spotInsert") || action.equals("spotUpdate")) {
			String spotJson = jsonObject.get("spot").getAsString();
			Spot spot = gson.fromJson(spotJson, Spot.class);
			String imageBase64 = jsonObject.get("imageBase64").getAsString();
			byte[] image = Base64.getMimeDecoder().decode(imageBase64);;
			int count = 0;
			if (action.equals("spotInsert")) {
				count = spotDao.insert(spot, image);
			} else if (action.equals("spotUpdate")) {
				count = spotDao.update(spot, image);
			}
			writeText(response, String.valueOf(count));
		} else if (action.equals("spotDelete")) {
			String spotJson = jsonObject.get("spot").getAsString();
			Spot spot = gson.fromJson(spotJson, Spot.class);
			int count = spotDao.delete(spot.getId());
			writeText(response, String.valueOf(count));
		} else if (action.equals("findById")) {
			int id = jsonObject.get("id").getAsInt();
			Spot spot = spotDao.findById(id);
			writeText(response, gson.toJson(spot));
		} else {
			writeText(response, "");
		}
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		SpotDao spotDAO = new SpotDaoMySqlImpl();
		List<Spot> spots = spotDAO.getAll();
		writeText(response, new Gson().toJson(spots));
	}

	private void writeText(HttpServletResponse response, String outText)
			throws IOException {
		response.setContentType(CONTENT_TYPE);
		PrintWriter out = response.getWriter();
		// System.out.println("outText: " + outText);
		out.print(outText);
		System.out.println("output: " + outText);
	}
}



