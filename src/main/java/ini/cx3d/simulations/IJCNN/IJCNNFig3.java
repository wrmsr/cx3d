package ini.cx3d.simulations.IJCNN;

import ini.cx3d.cells.Cell;
import ini.cx3d.cells.CellFactory;
import ini.cx3d.localBiology.NeuriteElement;
import ini.cx3d.localBiology.SomaElement;
import ini.cx3d.physics.PhysicalSphere;
import ini.cx3d.simulations.ECM;
import ini.cx3d.simulations.Scheduler;
import ini.cx3d.synapses.TestSynapses;
import ini.cx3d.utilities.Matrix;
import ini.cx3d.utilities.export.Exporter;

import java.awt.Color;

import static ini.cx3d.utilities.Matrix.add;

/**
 * This file was use to produce the Fig 3 of the Paper:
 * F. Zubler and R. Douglas, An instruction code for the explicit
 * programming of axonal growth patterns, IJCNN2010.
 *
 * @author fredericzubler
 */
public class IJCNNFig3
{

    public static void main(String[] args)
    {

        // 1. Setting the correct Parameters values for this simulation:
        IJCNNParameters.RETRACTION_THRESHOLD = 0.8;
        IJCNNParameters.RESET_THRESHOLD = 0.1;
        IJCNNParameters.BIFURCATION_THRESHOLD = 0.001;
        IJCNNParameters.MINIMUM_BRANCH_LENGTH = 24.0;
        IJCNNParameters.MAX_NUMBER_SIDE_BRANCH = 4;
        IJCNNParameters.BRANCH_DIAMETER_DECAY = 0.7;
        IJCNNParameters.CYLINDER_MINIMUM_EXTENSION_DIAMETER = 0.3;
        IJCNNParameters.CYLINDER_MINIMUM_BRANCHING_DIAMETER = 0.3;

        // 2. Some variables I'll be using
        final double DISTANCE = 150;
        final double SPREAD = 50;
        final double NB_SECRETING_CELLS = 100;

        final Color RED = new Color(1f, 0.02f, 0.32f);
        final Color BLUE = new Color(.0f, 0f, 0.75f);
        final Color VIOLET = new Color(.73f, 0f, 0.97f);
        final Color YELLOW = new Color(1f, .83f, .12f);
        final Color GREEN = new Color(.16f, 1f, 0.15f);
        final Color GRAY = new Color(0.5f, 0.5f, 0.5f);

        // 3. Creating the Extra-cellular Matrix, defining the Substances
        // (s.t. they appear in the menu bar)
        ECM ecm = ECM.getInstance();
        ECM.setRandomSeed(0L);    // comment out this line for different outputs
        ecm.substanceInstance("A");
        ecm.substanceInstance("B");
        ecm.substanceInstance("C");
        ecm.substanceInstance("D");
        // add additional PhysicalNodes around the whole simulation
        int nbOfAdditionalNodes = 600;
        for (int i = 0; i < nbOfAdditionalNodes; i++) {
            ecm.getPhysicalNodeInstance(Matrix.randomNoise(500, 3));
        }

        // 4. Prepare the Guide post cells (with the code xA, xB, xC and xD)
        double[] cellOrigin;
        SomaElement soma;
        Cell cell;
        PhysicalSphere sphere;
        String somaInstruction;
        IJCNNGrowthCone gc;
        // 4.a. Guidepost cells secreting A
        for (int i = 0; i < NB_SECRETING_CELLS; i++) {
            double dx = -SPREAD + 2 * SPREAD * ECM.getRandomDouble();
            double dy = -SPREAD + 2 * SPREAD * ECM.getRandomDouble();
            cellOrigin = new double[] {-DISTANCE + dx, -30 + 60 * ECM.getRandomDouble(), -DISTANCE + dy};
            cell = CellFactory.getCellInstance(cellOrigin);
            soma = cell.getSomaElement();
            sphere = soma.getPhysicalSphere();
            sphere.setMass(0.2);
            sphere.setDiameter(20);
            sphere.setAdherence(0.1);
            sphere.setColor(RED);
            somaInstruction = "xA";
            gc = new IJCNNGrowthCone();
            soma.addLocalBiologyModule(gc);
            gc.setInstruction(somaInstruction);
            gc.translate();
        }

        // 4.b .Guidepost cells secreting B
        for (int i = 0; i < NB_SECRETING_CELLS; i++) {
            double dx = -SPREAD + 2 * SPREAD * ECM.getRandomDouble() - 60;
            double dy = -SPREAD + 2 * SPREAD * ECM.getRandomDouble();
            cellOrigin = new double[] {DISTANCE + dx, -30 + 60 * ECM.getRandomDouble(), -DISTANCE + dy};
            cell = CellFactory.getCellInstance(cellOrigin);
            soma = cell.getSomaElement();
            sphere = soma.getPhysicalSphere();
            sphere.setMass(0.2);
            sphere.setDiameter(20);
            sphere.setAdherence(0.1);
            sphere.setColor(VIOLET);
            somaInstruction = "xB";
            gc = new IJCNNGrowthCone();
            soma.addLocalBiologyModule(gc);
            gc.setInstruction(somaInstruction);
            gc.translate();
        }

        // 4.c. Guidepost cells secreting C
        for (int i = 0; i < NB_SECRETING_CELLS; i++) {
            double dx = -SPREAD + 2 * SPREAD * ECM.getRandomDouble() - 60;
            double dy = -SPREAD + 2 * SPREAD * ECM.getRandomDouble();
            cellOrigin = new double[] {DISTANCE + dx, -30 + 60 * ECM.getRandomDouble(), DISTANCE + dy};
            cell = CellFactory.getCellInstance(cellOrigin);
            soma = cell.getSomaElement();
            sphere = soma.getPhysicalSphere();
            sphere.setMass(0.2);
            sphere.setDiameter(20);
            sphere.setAdherence(0.1);
            sphere.setColor(BLUE);
            somaInstruction = "xC";
            gc = new IJCNNGrowthCone();
            soma.addLocalBiologyModule(gc);
            gc.setInstruction(somaInstruction);
            gc.translate();
        }

        // 4.d. Guidepost cells secreting D
        for (int i = 0; i < NB_SECRETING_CELLS; i++) {
            double dx = -SPREAD + 2 * SPREAD * ECM.getRandomDouble();
            double dy = -SPREAD + 2 * SPREAD * ECM.getRandomDouble();
            cellOrigin = new double[] {-DISTANCE + dx, -30 + 60 * ECM.getRandomDouble(), DISTANCE + dy};
            cell = CellFactory.getCellInstance(cellOrigin);
            soma = cell.getSomaElement();
            sphere = soma.getPhysicalSphere();
            sphere.setMass(0.2);
            sphere.setDiameter(20);
            sphere.setAdherence(0.1);
            sphere.setColor(YELLOW);
            somaInstruction = "xD";
            gc = new IJCNNGrowthCone();
            soma.addLocalBiologyModule(gc);
            gc.setInstruction(somaInstruction);
            gc.translate();
        }

        // 5. Run the simulation 100 times, to let
        // the chemicals diffuse, and set the gradients
        for (int i = 0; i < 100; i++) {
            System.out.println(i);
            Scheduler.runEveryBodyOnce(0);
        }

        // 6. Add the 4 growing neurons
        for (int i = 0; i < 4; i++) {
            // 6.a the cell.
            cellOrigin = new double[] {-DISTANCE, 0, 0};
            cellOrigin = add(cellOrigin, new double[] {-40 + i * 20, 3 * ECM.getRandomDouble(), -4 + 8 * ECM.getRandomDouble()});
            cell = CellFactory.getCellInstance(cellOrigin);
            soma = cell.getSomaElement();
            soma.getPhysical().setColor(GRAY);
            // 6.b. the axon (with its code)
            NeuriteElement axon = soma.extendNewNeurite(2, new double[] {0, 0, -1});
            axon.setIsAnAxon(true);
            String axonInstruction = "fAkA|fBkB|fCkC|fDkD|bD[]{fDbD{_}{_}}{fDbD{_}{_}}";
            gc = new IJCNNGrowthCone();
            axon.addLocalBiologyModule(gc);
            gc.setInstruction(axonInstruction);
            gc.translate();
            // 6.c. the apical dendrite (with its code)
            NeuriteElement apicalDendrite = soma.extendNewNeurite(2, new double[] {0, 0, 1});
            apicalDendrite.getPhysicalCylinder().setDiameter(axon.getPhysicalCylinder().getDiameter());
            apicalDendrite.getPhysical().setColor(Color.black);
            String apicalDendriteInstruction = "fDkD|fDbD[]{fDbD{_}{_}}{fDbD{_}{_}}";
            gc = new IJCNNGrowthCone();
            apicalDendrite.addLocalBiologyModule(gc);
            gc.setInstruction(apicalDendriteInstruction);
            gc.translate();
        }

        // 7. Run the Simulation
        for (int i = 0; i < 1000; i++) {
            System.out.println(i);
            Scheduler.runEveryBodyOnce(0);
        }

//		for (int i=0; i<ECM.getInstance().physicalSphereList.size();i++){
//			PhysicalSphere sp =ECM.getInstance().physicalSphereList.get(i);
//			if(sp.getColor() != GRAY){
//				ECM.getInstance().physicalSphereList.remove(sp);
//				i--;
//			}
//		}

        // 8. Making the synapses, exporting the conexions
        TestSynapses.extendExcressencesAndSynapseOnEveryNeuriteElement(0.2);
        Exporter.saveExport();
    }
}
