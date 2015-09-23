package ini.cx3d.simulations.IJCNN;

import ini.cx3d.localBiology.CellElement;

import static ini.cx3d.simulations.IJCNN.IJCNNParameters.RESET_THRESHOLD;
import static ini.cx3d.simulations.IJCNN.IJCNNParameters.RETRACTION_THRESHOLD;

/**
 * An instance of this class is a Receptor.
 * It lives inside a IJCNNGrowthCone, and is thus associated
 * to a particular CellElement (usually a NeuriteElement).
 * <p/>
 * A Receptor is characterized by its ligand (the chemical they detect or secrete)
 * and its effect (FOLLOW, AVOID, RETRACT, BIFURCATE, BRANCH, SECRETE, RESET).
 * <p/>
 * At each time step, the growth cone it lives in calls the run() method.
 * The receptor then modifies the CellElement and/or the growth cone.
 *
 * @author fredericzubler
 */

public class IJCNNReceptor
{

    private IJCNNGrowthCone growthCone = null;
    private CellElement cellElement = null;
    private String ligand = "";
    private IJCNNEffects effect = null;

    public IJCNNReceptor(String ligand, IJCNNEffects effect)
    {
        this.ligand = ligand;
        this.effect = effect;
    }

    public void setGrowthCone(IJCNNGrowthCone growthCone)
    {
        this.growthCone = growthCone;
    }

    public void setCellElement(CellElement cellElement)
    {
        this.cellElement = cellElement;
    }

    // This method is called by the Growth cone
    public void run()
    {
        switch (effect) {
            case FOLLOW:
                follow();
                break;
            case AVOID:
                avoid();
                break;
            case RETRACT:
                retract();
                break;
            case BIFURCATE:
                bifurcate();
                break;
            case BRANCH:
                branch();
                break;
            case SECRETE:
                secrete();
                break;
            case RESET:
                reset();
                break;
            default:
                break;
        }
    }

    // FOLLOW:
    private void follow()
    {
        double[] direction = cellElement.getPhysical().getExtracellularGradient(ligand);
        growthCone.setMoving(true);                        //s.t. the GC moves at all
        growthCone.addPreferredDirection(direction);    // s.t. this receptor influences the direction
    }

    // AVOID:
    private void avoid()
    {
        double[] direction = cellElement.getPhysical().getExtracellularGradient(ligand);
        direction = new double[] {-direction[0], -direction[1], -direction[2]};    // avoid!!
        growthCone.setMoving(true);                        //s.t. the GC moves at all
        growthCone.addPreferredDirection(direction);    // s.t. this receptor influences the direction
    }

    // RETRACT:
    private void retract()
    {
        double concentration = cellElement.getPhysical().getExtracellularConcentration(ligand);
        if (concentration > RETRACTION_THRESHOLD) {
            growthCone.setRetracting(true);
        }
    }

    // BIFURCATE:
    private void bifurcate()
    {
        double concentration = cellElement.getPhysical().getExtracellularConcentration(ligand);
        growthCone.addBifurcatingProbability(concentration);
    }

    // BRANCH:
    private void branch()
    {
        double concentration = cellElement.getPhysical().getExtracellularConcentration(ligand);
        growthCone.addSideBranchingProbability(concentration);
    }

    // SECRETE:
    private void secrete()
    {
        double concentration = cellElement.getPhysical().getExtracellularConcentration(ligand);
        double desiredConcentration = 1;
        double maximumProductionRate = 10000;
        // 		a) difference between desired an actual value
        double diff = (concentration - desiredConcentration);
        //		b) sigmoid function (5 is the slope of the sigmoide, chosen s.t when |diff| = 1,
        //	       the rate is already max)
        double quantityChangePerTime = 1.0 / (1.0 + Math.exp(5 * diff));
        // 		c)	scale by to get the maximum value
        quantityChangePerTime *= maximumProductionRate;
        cellElement.getPhysical().modifyExtracellularQuantity(ligand, quantityChangePerTime);
    }

    // RESET:
    private void reset()
    {
        double concentration = cellElement.getPhysical().getExtracellularConcentration(ligand);
        if (concentration > RESET_THRESHOLD) {
            growthCone.translate();
        }
    }
}
