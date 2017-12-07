package idv.ron.server.news;

import idv.ron.server.main.Common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NewsDaoMySqlImpl implements NewsDao {

	public NewsDaoMySqlImpl() {
		super();
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void insert(News news) {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(News news) {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(int id) {
		// TODO Auto-generated method stub

	}

	@Override
	public News findById(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<News> getAll() {
		String sql = "SELECT id, title, detail, timestamp FROM News;";
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = DriverManager.getConnection(Common.URL, Common.USER,
					Common.PASSWORD);
			ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			List<News> newsList = new ArrayList<News>();
			while (rs.next()) {
				int id = rs.getInt(1);
				String title = rs.getString(2);
				String detail = rs.getString(3);
				long date = rs.getTimestamp(4).getTime();
				News news = new News(id, title, detail, date);
				newsList.add(news);
			}
			return newsList;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				ps.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
