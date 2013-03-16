package ifml2.editor.gui.instructions;

import ca.odell.glazedlists.swing.DefaultEventListModel;
import ifml2.GUIUtils;
import ifml2.editor.IFML2EditorException;
import ifml2.editor.gui.EditorUtils;
import ifml2.editor.gui.InstructionsEditor;
import ifml2.om.InstructionList;
import ifml2.vm.instructions.IfInstruction;
import ifml2.vm.instructions.Instruction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class IfInstrEditor extends AbstractInstrEditor
{
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField conditionText;
    private JButton editThenButton;
    private JButton editElseButton;
    private JList thenInstructionsList;
    private JList elseInstructionsList;

    private static final String IF_INSTR_EDITOR_TITLE = "Проверка условия";
    private InstructionList thenInstructionsClone;
    private InstructionList elseInstructionsClone;

    public IfInstrEditor(Window owner, @NotNull IfInstruction instruction)
    {
        super(owner);
        init();

        // -- init local events

        MouseAdapter mouseAdapter = new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    JList source = (JList) e.getSource();
                    Instruction instruction = (Instruction) source.getSelectedValue();
                    if (instruction != null)
                    {
                        EditorUtils.showAssociatedEditor(IfInstrEditor.this, instruction);
                    }
                }
            }
        };
        thenInstructionsList.addMouseListener(mouseAdapter);
        elseInstructionsList.addMouseListener(mouseAdapter);

        // -- init actions --
        editThenButton.setAction(new AbstractAction("Редактировать инструкции...", GUIUtils.EDIT_ELEMENT_ICON)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                InstructionsEditor instructionsEditor = new InstructionsEditor(IfInstrEditor.this, thenInstructionsClone);
                if(instructionsEditor.showDialog())
                {
                    instructionsEditor.getData(thenInstructionsClone);
                }
            }
        });
        editElseButton.setAction(new AbstractAction("Редактировать инструкции...", GUIUtils.EDIT_ELEMENT_ICON)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                InstructionsEditor instructionsEditor = new InstructionsEditor(IfInstrEditor.this, elseInstructionsClone);
                if(instructionsEditor.showDialog())
                {
                    instructionsEditor.getData(elseInstructionsClone);
                }
            }
        });

        // -- clone data --
        try
        {
            thenInstructionsClone = instruction.getThenInstructions().clone();
            elseInstructionsClone = instruction.getElseInstructions().clone();
        }
        catch (CloneNotSupportedException e)
        {
            GUIUtils.showErrorMessage(this, e);
        }

        // -- init form --
        conditionText.setText(instruction.getCondition());
        thenInstructionsList.setModel(new DefaultEventListModel<Instruction>(thenInstructionsClone.getInstructions()));
        elseInstructionsList.setModel(new DefaultEventListModel<Instruction>(elseInstructionsClone.getInstructions()));
    }

    @Override
    protected JButton getButtonCancel()
    {
        return buttonCancel;
    }

    @Override
    protected JButton getButtonOK()
    {
        return buttonOK;
    }

    @Override
    protected JPanel getEditorContentPane()
    {
        return contentPane;
    }

    @Override
    protected String getEditorTitle()
    {
        return IF_INSTR_EDITOR_TITLE;
    }

    @Override
    protected Class<? extends Instruction> getInstrClass()
    {
        return IfInstruction.class;
    }

    @Override
    public void getData(@NotNull Instruction instruction) throws IFML2EditorException
    {
        super.getData(instruction);

        IfInstruction ifInstruction = (IfInstruction) instruction;

        ifInstruction.setCondition(conditionText.getText());
        ifInstruction.setThenInstructions(thenInstructionsClone);
        ifInstruction.setElseInstructions(elseInstructionsClone);
    }
}
