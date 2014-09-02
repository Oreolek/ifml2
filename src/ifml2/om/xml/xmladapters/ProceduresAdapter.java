package ifml2.om.xml.xmladapters;

import ifml2.om.Procedure;
import ifml2.om.xml.xmlobjects.XmlProcedures;
import org.apache.log4j.Logger;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.HashMap;

public class ProceduresAdapter extends XmlAdapter<XmlProcedures, HashMap<String, Procedure>>
{
    private static final Logger LOG = Logger.getLogger(ProceduresAdapter.class);

    @Override
    public XmlProcedures marshal(HashMap<String, Procedure> v) throws Exception
    {
        XmlProcedures xmlProcedures = new XmlProcedures();
        xmlProcedures.procedures = new ArrayList<Procedure>(v.values());
        return xmlProcedures;
    }

    @Override
    public HashMap<String, Procedure> unmarshal(XmlProcedures v) throws Exception
    {
        LOG.trace(String.format("unmarshal(XmlProcedures = %s)", v));

        HashMap<String, Procedure> procedures = new HashMap<String, Procedure>();
        for (Procedure procedure : v.procedures)
        {
            procedures.put(procedure.getName().toLowerCase(), procedure);
        }

        LOG.trace("unmarshal() :: END");
        return procedures;
    }
}
