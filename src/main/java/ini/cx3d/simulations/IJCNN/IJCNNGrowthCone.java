package ini.cx3d.simulations.IJCNN;

import ini.cx3d.localBiology.CellElement;
import ini.cx3d.localBiology.LocalBiologyModule;
import ini.cx3d.localBiology.NeuriteElement;
import ini.cx3d.physics.PhysicalCylinder;
import ini.cx3d.simulations.ECM;
import ini.cx3d.utilities.Matrix;

import java.awt.Color;
import java.util.Vector;

import static ini.cx3d.simulations.IJCNN.IJCNNEffects.AVOID;
import static ini.cx3d.simulations.IJCNN.IJCNNEffects.BIFURCATE;
import static ini.cx3d.simulations.IJCNN.IJCNNEffects.BRANCH;
import static ini.cx3d.simulations.IJCNN.IJCNNEffects.FOLLOW;
import static ini.cx3d.simulations.IJCNN.IJCNNEffects.RESET;
import static ini.cx3d.simulations.IJCNN.IJCNNEffects.RETRACT;
import static ini.cx3d.simulations.IJCNN.IJCNNEffects.SECRETE;
import static ini.cx3d.simulations.IJCNN.IJCNNParameters.BRANCH_DIAMETER_DECAY;
import static ini.cx3d.simulations.IJCNN.IJCNNParameters.CYLINDER_MINIMUM_BRANCHING_DIAMETER;
import static ini.cx3d.simulations.IJCNN.IJCNNParameters.CYLINDER_MINIMUM_EXTENSION_DIAMETER;
import static ini.cx3d.simulations.IJCNN.IJCNNParameters.MAX_NUMBER_SIDE_BRANCH;
import static ini.cx3d.simulations.IJCNN.IJCNNParameters.MINIMUM_BRANCH_LENGTH;
import static ini.cx3d.utilities.Matrix.add;
import static ini.cx3d.utilities.Matrix.randomNoise;
import static ini.cx3d.utilities.Matrix.scalarMult;

/**
 * An instance of this class represents a Growth cone, and is
 * located inside particular CX3D CellElement (usually a terminal
 * NeuriteElement).
 * <p/>
 * It implements the LocalBiologyModule interface, and thus its
 * run() method is called at each time step. This method :
 * <p/>
 * 1. runs all the receptors, and thus modifies internal
 * variables such as preferredDirection, bifurcatingProbability, etc.
 * <p/>
 * 2. performs (or not) elongation, branching etc.
 *
 * @author fredericzubler
 */

public class IJCNNGrowthCone
        implements LocalBiologyModule
{

    /* The CellElement it lives in */
    private CellElement cellElement = null;

    /*The instruction String that this Growth cone has to execute*/
    private String instruction = "";

    /* in case of several instructions sets for the same branch, separated by the char '|',
     * this counter indicates which one has to be executed next (0 is the first one, 1 the second etc).*/
    private int instructionPhase = 0;

    /* The receptors expressed in this growth cone*/
    Vector<IJCNNReceptor> receptors = new Vector<IJCNNReceptor>();

    /* The values that are modified by the receptors, updated after each run*/
    double[] preferredDirection = {0, 0, 0};
    double bifurcatingProbability = 0;
    double sideBranchingProbability = 0;
    boolean retracting = false;
    boolean moving = false;

    /* How many ide branches have already been made.*/
    int nbSideBranches = 0;

    /* constant speed */
    double speed = 100;

    //---------------------------------------------------
    // Constructors
    //---------------------------------------------------
    public IJCNNGrowthCone() {}

    public IJCNNGrowthCone(String instruction) {this.instruction = instruction;}

    //---------------------------------------------------
    //	LocalBiologModule methods
    //---------------------------------------------------
    public CellElement getCellElement()
    {
        return cellElement;
    }

    @Override
    public void setCellElement(CellElement cellElement)
    {
        this.cellElement = cellElement;
        for (IJCNNReceptor receptor : receptors) {
            receptor.setCellElement(cellElement);
        }
    }

    @Override
    public LocalBiologyModule getCopy()
    {
        return new IJCNNGrowthCone();
    }

    @Override
    public boolean isCopiedWhenNeuriteBranches() {return false;}

    @Override
    public boolean isCopiedWhenNeuriteElongates() {return false;}

    @Override
    public boolean isCopiedWhenNeuriteExtendsFromSoma() {return false;}

    @Override
    public boolean isCopiedWhenSomaDivides() {return false;}

    @Override
    public boolean isDeletedAfterNeuriteHasBifurcated() {return true;}

    @Override
    public void run()
    {  // This method is called at each time step by the CellElement
        // 1. Run all receptors
        for (int i = 0; i < receptors.size(); i++) {
            receptors.get(i).run();
        }

        // 2. Do we retract?
        if (retracting == true) {
            PhysicalCylinder pc = ((NeuriteElement) cellElement).getPhysicalCylinder();
            pc.retractCylinder(speed);
            retracting = false;    // otherwise retracts the whole branch, even without ligand
            return;            // if retraction occurred, we don't do anything else.
        }
        // do we bifurcate
        //		if(ECM.getRandomDouble()<bifurcatingProbability){

//		if(cellElement instanceof NeuriteElement){
//			System.out.println("IJCNNGrowthCone.run() bifurcatingProbability: "+bifurcatingProbability);
//		}

        if (IJCNNParameters.BIFURCATION_THRESHOLD < bifurcatingProbability) {
            PhysicalCylinder pc = ((NeuriteElement) cellElement).getPhysicalCylinder();

            if (pc.getDaughterLeft() == null &&
                    pc.lengthToProximalBranchingPoint() > MINIMUM_BRANCH_LENGTH &&
                    pc.getDiameter() > CYLINDER_MINIMUM_BRANCHING_DIAMETER) {

                NeuriteElement[] daughters = ((NeuriteElement) cellElement).bifurcate();
                daughters[0].getPhysical().setDiameter(BRANCH_DIAMETER_DECAY * cellElement.getPhysical().getDiameter());
                daughters[1].getPhysical().setDiameter(BRANCH_DIAMETER_DECAY * cellElement.getPhysical().getDiameter());
                receptors.clear();
                // GC in first daughter branch
                String InstructionLeftDaughter = IJCNNStringUtilities.stringDaughterLeft(this.instruction);
                IJCNNGrowthCone growthConeLeft = new IJCNNGrowthCone(InstructionLeftDaughter);
                daughters[0].addLocalBiologyModule(growthConeLeft);
                growthConeLeft.translate();
                // GC in second daughter branch
                String InstructionRightDaughter = IJCNNStringUtilities.stringDaughterRight(this.instruction);
                IJCNNGrowthCone growthConeRight = new IJCNNGrowthCone(InstructionRightDaughter);
                daughters[1].addLocalBiologyModule(growthConeRight);
                growthConeRight.translate();
                return;            // if bifurcation occurred, we don't do anything else.
            }
        }

        // do we side-branch
        if (ECM.getRandomDouble() < sideBranchingProbability) {
            PhysicalCylinder pc = ((NeuriteElement) cellElement).getPhysicalCylinder();

            if (pc.getDaughterLeft() == null &&
                    pc.getDaughterRight() == null &&
                    pc.lengthToProximalBranchingPoint() > MINIMUM_BRANCH_LENGTH &&
                    nbSideBranches < MAX_NUMBER_SIDE_BRANCH &&
                    pc.getDiameter() > CYLINDER_MINIMUM_BRANCHING_DIAMETER) {

                NeuriteElement daughter = ((NeuriteElement) cellElement).branch();
                daughter.getPhysical().setDiameter(BRANCH_DIAMETER_DECAY * cellElement.getPhysical().getDiameter());
                daughter.getPhysical().setColor(Color.pink);
                // GC in first daughter branch
                String InstructionSideBranch = IJCNNStringUtilities.stringSideBranch(this.instruction);
                IJCNNGrowthCone growthConeSide = new IJCNNGrowthCone(InstructionSideBranch);
                daughter.addLocalBiologyModule(growthConeSide);
                growthConeSide.translate();
                nbSideBranches++;
            }
        }

        // 3. Do we move? ("follow" or "avoid")
        if (moving == true) {
            PhysicalCylinder pc = ((NeuriteElement) cellElement).getPhysicalCylinder();
            // direction = weighted sum of previous direction, desired direction, noise
            double[] growthDirection = pc.getUnitaryAxisDirectionVector();
            growthDirection = add(growthDirection, randomNoise(0.3, 3));
            growthDirection = add(growthDirection, scalarMult(0.3, Matrix.normalize(preferredDirection)));
            pc.extendCylinder(speed, growthDirection);

            if (pc.getDiameter() < CYLINDER_MINIMUM_EXTENSION_DIAMETER) { // is too small, stops elongation
                cellElement.removeLocalBiologyModule(this);
            }
        }
        // 3. We reset the preferred direction and the elongation
        preferredDirection[0] = 0;
        preferredDirection[1] = 0;
        preferredDirection[2] = 0;
        moving = false;
        bifurcatingProbability = 0;
    }

    //---------------------------------------------------
    //	Communication with the Receptors
    //---------------------------------------------------

    /* Not a simple getter, but adds up the direction,
     * s.t we can get a direction from several receptors.*/
    public void addPreferredDirection(double[] preferredDirection)
    {
        this.preferredDirection = add(this.preferredDirection, preferredDirection);
    }

    public void addSideBranchingProbability(double sideBranchingProbability)
    {
        this.sideBranchingProbability += sideBranchingProbability;
    }

    public void addBifurcatingProbability(double bifurcatingProbability)
    {
        this.bifurcatingProbability += bifurcatingProbability;
    }

    public void setMoving(boolean moving)
    {
        this.moving = moving;
    }

    public void setRetracting(boolean retracting)
    {
        this.retracting = retracting;
    }

    //---------------------------------------------------
    //	Parse a branch instruction, and instantiate receptors
    //---------------------------------------------------

    public void setInstruction(String instruction)
    {
        this.instruction = instruction;
    }

    public void translate()
    {

        if (instruction.isEmpty()) {
            return;
        }

        // 1. cleaning the list of receptors currently expressed
        receptors = new Vector<IJCNNReceptor>();

        // 2. parsing the instructions
        int lengthOfTheString = instruction.length();
        int counter = 0; // to find the right instruction phase (between two '|'s)
        for (int index = 0; index < lengthOfTheString; index++) {
            char c = instruction.charAt(index);
            System.out.println("IJCNNGrowtCone.parse_level(), c = " + c);
            if (c == '[' || c == '{' || c == ']' || c == '}') {
                break;
            }
            // to ensure we only translate the piece of code corresponding to the right phase
            if (c == '|') {
                counter++;
                if (counter > instructionPhase) {
                    break;
                }
                if (counter == instructionPhase) {
                    continue;
                }
            }
            if (counter < instructionPhase) {
                continue;
            }

            // 3. Instantiating the Receptors
            IJCNNEffects effect = null;
            String ligand = null;

            // 3.a Effect
            if (c == 'f') {
                effect = FOLLOW;
            }
            else if (c == 'a') {
                effect = AVOID;
            }
            else if (c == 'r') {
                effect = RETRACT;
            }
            else if (c == 'b') {
                effect = BIFURCATE;
            }
            else if (c == 's') {  // "s" is for Side-branching
                effect = BRANCH;
            }
            else if (c == 'x') {
                effect = SECRETE;
            }
            else if (c == 'k') {
                effect = RESET;
            }
            // 3.b Ligand
            index++;
            c = instruction.charAt(index);
            ligand = "" + c;
            System.out.println("** We construct a receptor with " + effect + " if " + ligand);
            IJCNNReceptor r = new IJCNNReceptor(ligand, effect);
            this.addReceptor(r);
        }
        // IMPORTANT: next time, we will move to the next set of instructions.
        instructionPhase++;
    }

    public void addReceptor(IJCNNReceptor receptor)
    {
        receptor.setCellElement(this.cellElement);
        receptor.setGrowthCone(this);
        receptors.add(receptor);
    }
}
