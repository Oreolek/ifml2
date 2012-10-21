package ifml2.om;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

public class Template
{
    @XmlElements({
            @XmlElement(name = "literalElement", type = LiteralTemplateElement.class),
            @XmlElement(name = "objectElement", type = ObjectTemplateElement.class)
    })
    public final EventList<TemplateElement> elements = new BasicEventList<TemplateElement>();

    public int size()
    {
        return elements.size();
    }

    public TemplateElement get(int index)
    {
        return elements.get(index);
    }
}
