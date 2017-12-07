package idv.ron.server.spots;

import java.util.List;

public interface SpotDao {
	int insert(Spot spot, byte[] image);

	int update(Spot spot, byte[] image);

	int delete(int id);

	Spot findById(int id);

	List<Spot> getAll();

	byte[] getImage(int id);
}
