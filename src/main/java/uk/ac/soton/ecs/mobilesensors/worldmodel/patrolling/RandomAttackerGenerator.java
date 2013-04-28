package uk.ac.soton.ecs.mobilesensors.worldmodel.patrolling;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Required;

import uk.ac.soton.ecs.mobilesensors.layout.Grid;

public class RandomAttackerGenerator implements AttackGenerator {
	private double attackProbability;

	public Collection<Attack> createAttacks(Grid grid) {
		Collection<Attack> result = new ArrayList<Attack>();
		for (Point2D point : grid) {
			if (Math.random() < attackProbability) {
				result.add(new Attack(point));
			}
		}

		return result;
	}

	@Required
	public void setAttackProbability(double attackProbability) {
		this.attackProbability = attackProbability;
	}
}
