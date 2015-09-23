package ini.cx3d.simulations.IJCNN;

import ini.cx3d.cells.Cell;
import ini.cx3d.cells.CellFactory;
import ini.cx3d.localBiology.NeuriteElement;
import ini.cx3d.physics.Substance;
import ini.cx3d.simulations.ECM;
import ini.cx3d.simulations.Scheduler;
import ini.cx3d.utilities.Matrix;

import java.awt.Color;

/**
 * This class was used to generate the Figures 1 of the paper:
 * F. Zubler and R. Douglas, An instruction code for the explicit
 * programming of axonal growth patterns, IJCNN2010.
 *
 * @author fredericzubler
 */

public class IJCNNFig1
{

    public static void main(String[] args)
    {

        // 1. Setting the correct Parameters values for this simulation:
        IJCNNParameters.RETRACTION_THRESHOLD = 0.8;
        IJCNNParameters.RESET_THRESHOLD = 0.1;
        IJCNNParameters.BIFURCATION_THRESHOLD = 0.6;
        IJCNNParameters.MINIMUM_BRANCH_LENGTH = 24.0;
        IJCNNParameters.MAX_NUMBER_SIDE_BRANCH = 4;
        IJCNNParameters.BRANCH_DIAMETER_DECAY = 0.7;
        IJCNNParameters.CYLINDER_MINIMUM_EXTENSION_DIAMETER = 0.4;
        IJCNNParameters.CYLINDER_MINIMUM_BRANCHING_DIAMETER = 0.4;

        // 2. Define the environment:
        // 2.a. Create an ECM with artificial chemicals
        ECM ecm = ECM.getInstance();
        ecm.addArtificialGaussianConcentrationZ(new Substance("A", Color.red), 1, 300, 100);
        // 2.b. Add additional PhysicalNodes
        int nbOfAdditionalNodes = 600;
        for (int i = 0; i < nbOfAdditionalNodes; i++) {
            ecm.getPhysicalNodeInstance(Matrix.randomNoise(500, 3));
        }

        // 3. Generate a Cell
        Cell c = CellFactory.getCellInstance(new double[] {0, 0, 0});
        c.getSomaElement().getPhysical().setColor(Color.black);
        // 3.b with an axon
        NeuriteElement ne = c.getSomaElement().extendNewNeurite(new double[] {0, 0, 1});
        ne.getPhysicalCylinder().setDiameter(2.0);
        IJCNNGrowthCone gc = new IJCNNGrowthCone();
        ne.addLocalBiologyModule(gc);

        // 4. The genetic code (un-comment the line you want to execute !)
        String instruction;
//		instruction = "fA";
//		instruction = "fAkA";
//		instruction = "fAbA";
//		instruction = "fAbA{_}{_}";
        instruction = "fAbA{fZbA{_}{_}}{fZbA{_}{_}}";
//		instruction = "fAsA[fAbA{_}{_}]";   // an example of side.branch, not in the paper

        gc.setInstruction(instruction);
        gc.translate();

        // 5. Run the simulation
        Scheduler.simulate();
    }
}
