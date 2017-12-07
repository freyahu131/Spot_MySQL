package idv.ron.server.news;

import java.util.List;

public interface NewsDao {
	void insert(News news);
	void update(News news);
	void delete(int id);
	News findById(int id);
	List<News> getAll();
}
