package ifml2.vm.instructions;

import ifml2.IFML2Exception;
import ifml2.om.Location;
import ifml2.vm.RunningContext;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "goToLoc")
public class GoToLocInstruction extends Instruction
{
    private String locationExpr = "";

    public static String getTitle()
    {
        return "Перейти в локацию ";
    }

    @Override
    public void run(RunningContext runningContext) throws IFML2Exception
    {
        Location location = getLocationFromExpression(locationExpr, runningContext, getTitle(), "Локация", true);

        if (location == null)
        {
            throw new IFML2Exception("Туда нельзя пройти.");
        }

        virtualMachine.setCurrentLocation(location);

        // run show loc name instruction
        virtualMachine.showLocName(location);
    }

    public String getLocationExpr()
    {
        return locationExpr;
    }

    @XmlAttribute(name = "location")
    public void setLocationExpr(String locationExpr)
    {
        this.locationExpr = locationExpr;
    }

    @Override
    public String toString()
    {
        return "Перейти в локацию: " + locationExpr;
    }
}
