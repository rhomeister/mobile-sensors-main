package uk.ac.soton.ecs.mobilesensors.worldmodel.patrolling;

import java.util.Collection;

import uk.ac.soton.ecs.mobilesensors.layout.Grid;

public interface AttackGenerator {

	Collection<Attack> createAttacks(Grid grid);

}
