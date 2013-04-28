package uk.ac.soton.ecs.mobilesensors.sensor.coordination;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.Validate;

import uk.ac.soton.ecs.mobilesensors.Move;

public class RankMax {

	private static RankMax instance;

	public static RankMax getInstance() {
		if (instance == null) {
			instance = new RankMax();

		}

		return instance;
	}

	/**
	 * Returns the rank^th best moveoption
	 * 
	 * @param moveOptions
	 * @param utilities
	 * @param rank
	 * @return
	 */
	public Move select(final List<Move> moveOptions, final double[] utilities,
			int rank) {
		Validate.isTrue(rank >= 0);

		Collections.sort(moveOptions, new Comparator<Move>() {

			public int compare(Move o1, Move o2) {
				int index1 = moveOptions.indexOf(o1);
				int index2 = moveOptions.indexOf(o2);

				return -Double.compare(utilities[index1], utilities[index2]);
			}
		});

		if (moveOptions.size() - 1 < rank) {
			return moveOptions.get(moveOptions.size() - 1);
		}

		return moveOptions.get(rank);
	}

}
