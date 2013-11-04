package ifml2.om;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ifml2.vm.instructions.*;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

public class InstructionList implements Cloneable
{
    private EventList<Instruction> instructions = new BasicEventList<Instruction>(); // InstructionList controls its instructions

    @XmlElements({
            @XmlElement(name = "goToLoc", type = GoToLocInstruction.class),
            @XmlElement(name = "showMessage", type = ShowMessageInstr.class),
            @XmlElement(name = "if", type = IfInstruction.class),
            @XmlElement(name = "loop", type = LoopInstruction.class),
            @XmlElement(name = "var", type = SetVarInstruction.class),
            @XmlElement(name = "return", type = ReturnInstruction.class),
            @XmlElement(name = "setProperty", type = SetPropertyInstruction.class),
            @XmlElement(name = "moveItem", type = MoveItemInstruction.class),
            @XmlElement(name = "startTimer", type=StartTimerInstruction.class)
    })
    public EventList<Instruction> getInstructions()
    {
        return instructions;
    }

    @Override
    public InstructionList clone() throws CloneNotSupportedException
    {
        InstructionList clone = (InstructionList) super.clone();
        clone.instructions = new BasicEventList<Instruction>();
        for (Instruction instruction : instructions)
        {
            clone.instructions.add(instruction.clone());
        }
        return clone;
    }

    public void rewriteInstructions(InstructionList instructionList)
    {
        instructions.clear();
        instructions.addAll(instructionList.instructions);
    }
}
