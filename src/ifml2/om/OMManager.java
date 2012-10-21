package ifml2.om;

import com.sun.xml.internal.bind.IDResolver;
import ifml2.IFML2Exception;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.bind.*;
import javax.xml.bind.util.ValidationEventCollector;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;

public class OMManager
{
    public static final Logger LOG = Logger.getLogger(OMManager.class);

    /**
     * Loads story from xml file
     * @param xmlFile Full path to xml file with story.
     * @param toInitItemsStartLoc Provide true if items should be copied into start positions (inventory and locations).
     * It's necessary in Editor.
     * @return Wrapped result containing story and loaded inventory (see toInitItemsStartLoc param).
     * @throws IFML2Exception If some error has occurred during loading.
     */
    public static LoadStoryResult loadStoryFromXmlFile(String xmlFile, final boolean toInitItemsStartLoc) throws IFML2Exception
    {
        final Story story;
        final ArrayList<Item> inventory = new ArrayList<Item>();

		try
		{
			JAXBContext context = JAXBContext.newInstance(Story.class);
	        Unmarshaller unmarshaller = context.createUnmarshaller();
            unmarshaller.setProperty(IDResolver.class.getName(), new IFMLIDResolver());

            final HashMap<String, IFMLObject> ifmlObjectsHeap = new HashMap<String, IFMLObject>();

            unmarshaller.setListener(new Unmarshaller.Listener()
            {
                @Override
                public void afterUnmarshal(Object target, Object parent)
                {
                    // load all objects into objectsHeap
                    if (target instanceof IFMLObject)
                    {
                        IFMLObject ifmlObject = (IFMLObject) target;
                        ifmlObjectsHeap.put(ifmlObject.getId().toLowerCase(), ifmlObject);
                    }

                    // add item to inventory by starting position
                    if(target instanceof Item)
                    {
                        Item item = (Item) target;
                        if(toInitItemsStartLoc)
                        {
                            if(item.startingPosition.inventory)
                            {
                                inventory.add(item); //should it be original items
                            }
                        }
                    }
                }
            });

	        File file = new File(xmlFile);

            ValidationEventCollector validationEventCollector = new ValidationEventCollector();
            unmarshaller.setEventHandler(validationEventCollector);

            if(validationEventCollector.getEvents().length > 0)
            {
                throw new IFML2Exception("Ошибка при загрузке истории: ", (Object[]) validationEventCollector.getEvents());
            }

            System.out.println("before unmarshall");
            story = (Story) unmarshaller.unmarshal(file);
            System.out.println("after unmarshall");

            story.setObjectsHeap(ifmlObjectsHeap);

            if(toInitItemsStartLoc)
            {
                assignItemsToLocations(story);
            }
            assignLibRefs(story);
            assignLinksWordsToObjects(story);
		}
		catch (JAXBException e)
        {
            throw new IFML2Exception(e, "Ошибка при загрузке истории: {0}", e.getMessage());
        }

		return new LoadStoryResult(story, inventory);
	}

    private static void assignItemsToLocations(Story story)
    {
        for(Item item : story.getItems())
        {
            for(Location location : item.startingPosition.locations)
            {
                location.getItems().add(item);
            }
        }
    }

    private static void assignLinksWordsToObjects(Story story) throws IFML2Exception
    {
        for(IFMLObject ifmlObject : story.getObjectsHeap().values())
        {
            WordLinks wordLinks = ifmlObject.getWordLinks();

            if(wordLinks == null)
            {
                throw new IFML2Exception("Список ссылок на слова не задан у объекта {0}", ifmlObject);
            }

            /*if(wordLinks.mainWord == null)
            {
                throw new IFML2Exception("Основное слово не задано у объекта {0}", ifmlObject);
            }*/

            if(wordLinks.getMainWord() != null)
            {
                wordLinks.getMainWord().linkerObjects.add(ifmlObject);
            }

            for(Word word : wordLinks.getWords())
            {
                if(word == null)
                {
                    throw new IFML2Exception("Задана неверная ссылка на слово у объекта {0}", ifmlObject);
                }

                if(!word.linkerObjects.contains(ifmlObject))
                {
                    word.linkerObjects.add(ifmlObject);
                }
            }
        }
    }

    private static void assignLibRefs(Story story) throws IFML2Exception
    {
        //  -- assign refs to attributes --

        //HashMap<String, Attribute> attributes = new HashMap<String, Attribute>();

        //TODO: adding story attributes to common HashMap
        //attributes.putAll(story.);
        // ^^^ NEED TO ADD attributes to story file ^^^

        // iterate libs for attribute definitions
        /*for(Library library : story.getLibraries())
        {
            for(Attribute attribute : library.getAttributes())
            {
                attributes.put(attribute.getName().toLowerCase(), attribute);
                //throw new NullPointerException(); // todo test exception throwing to top -- it cames without stacktrace
                //throw new MarshalException("test marshal exception");
            }
        }*/

        // iterate objects for attributes
        /*for(IFMLObject ifmlObject : story.getObjectsHeap().values())
        {
            List<Attribute> fakeAttributes = ifmlObject.getAttributes();
            for(Attribute fakeAttribute : fakeAttributes)
            {
                String loweredAttributeRef = fakeAttribute.getName().toLowerCase();
                if(attributes.containsKey(loweredAttributeRef))
                {
                    fakeAttributes.set(fakeAttributes.indexOf(fakeAttribute), attributes.get(loweredAttributeRef));
                }
                else
                {
                    throw new IFML2Exception("Признак \"{0}\" в {1} \"{2}\" не объявлен ни в одной из библиотек",
                            fakeAttribute,
                            (ifmlObject instanceof Location) ? "локации" : "предмете",
                            ifmlObject.getName());
                }
            }
        }*/

        // -- assign refs to actions in hooks
        
        HashMap<String, Action> actions = new HashMap<String, Action>();
        
        // copy all actions to HashMap
        for(Action action : story.getAllActions())
        {
            actions.put(action.getName().toLowerCase(), action);
        }
        
        // iterate items for hooks
        for(Item item : story.getItems())
        {
            for(Hook hook : item.getHooks())
            {
                String loweredActionRef = hook.getAction().getName().toLowerCase();
                if(actions.containsKey(loweredActionRef))
                {
                    hook.setAction(actions.get(loweredActionRef));
                }
                else
                {
                    throw new IFML2Exception("Действие \"{0}\" в \"{1}\" не объявлено ни в одной из библиотек",
                            hook.getAction().getName(), item.getName());
                }
            }
        }
    }

    public static Library loadLibrary(String libPath) throws IFML2Exception
	{
        //logs
        System.out.println("LOAD library " + libPath);

        Library library;

		try
		{
			JAXBContext context = JAXBContext.newInstance(Library.class);
	        Unmarshaller unmarshaller = context.createUnmarshaller();
            unmarshaller.setProperty(IDResolver.class.getName(), new IFMLIDResolver());

            // TODO: загрузка стандартных и прочих либ

	        String realRelativePath = "libs/" + libPath; // for JAR should be from root: "/libs/:

            //--Loading from JAR--Reader reader = new BufferedReader(new InputStreamReader(OMManager.class.getResourceAsStream(realRelativePath), "UTF-8"));

            File file = new File(realRelativePath);

	        if(!file.exists())
	        {
	        	throw new IFML2Exception("Файл " + file.getAbsolutePath() + " библиотеки не найдена");
	        }
	        
	        library = (Library) unmarshaller.unmarshal(file);
            library.path = libPath;
		}
		catch (JAXBException e)
		{
			throw new IFML2Exception(e);
		}

        //logs
        System.out.println("LOADING END");

		return library;
	}

    public static void saveStoryToXmlFile(String xmlFile, Story story) throws IFML2Exception
    {
        try
        {
            JAXBContext context = JAXBContext.newInstance(Story.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            File file = new File(xmlFile);

            marshaller.marshal(story, file);
        }
        catch (JAXBException e)
        {
            throw new IFML2Exception(e);
        }
    }

    public static class LoadStoryResult
    {
        private final Story story;
        private final ArrayList<Item> inventory;

        public LoadStoryResult(Story story, ArrayList<Item> inventory)
        {
            this.story = story;
            this.inventory = inventory;
        }

        public Story getStory()
        {
            return story;
        }

        public ArrayList<Item> getInventory()
        {
            return inventory;
        }
    }

    private static class IFMLIDResolver extends IDResolver
    {
        private HashMap<String, Object> bindings = new HashMap<String, Object>();
        private Story story;

        @Override
        public void startDocument(ValidationEventHandler validationEventHandler) throws SAXException
        {
            super.startDocument(validationEventHandler);
            bindings.clear();
            story = null;
        }

        @Override
        public void bind(String s, Object o) throws SAXException
        {
            bindings.put(s, o);

            // save link to story
            if(o instanceof Story)
            {
                story = (Story) o;
            }

            //logs
            System.out.println(String.format("binding %s -> %s (%s)", s, o.getClass().getName(), o));
        }

        @Override
        public Callable<?> resolve(final String s, final Class aClass) throws SAXException
        {
            return new Callable<Object>()
            {
                @Override
                public Object call() throws Exception
                {
                    //logs
                    //todo LOG4J!!!
                    LOG.debug(String.format("? resolve %s for %s", s, aClass));
                    //System.out.println(String.format("? resolve %s for %s", s, aClass));

                    if(bindings.containsKey(s))
                    {
                        System.out.println(String.format("   => binding %s", bindings.get(s)));
                        return bindings.get(s);
                    }
                    else
                    {
                        System.out.println("  trying to find in libs:");
                        // try to find key in libraries
                        if(story != null)
                        {
                            for (Library library : story.getLibraries())
                            {
                                System.out.println("  - lib " + library.getName() + ", class is " + aClass);

                                if(aClass == Attribute.class || aClass == Object.class) //todo: remove Object after JAXB fix
                                {
                                    System.out.println(String.format("  => searchnig Attribute %s", s));
                                    Attribute attribute = library.getAttributeByName(s);
                                    if(attribute != null)
                                    {
                                        return attribute;
                                    }
                                    /*todo из-за приходящего Object вместо нормального Attribute мы не сможем проверить,
                                      есть ли такой аттрибут вообще (не понятно, что запрашивает, атрибут или другой объект)
                                      */
                                }

                                //todo ANOTHER LINKS!
                            }
                        }
                    }
                    System.out.println("  -> NOT FOUND");
                    return null;
                }
            };
        }

        @Override
        public void endDocument() throws SAXException
        {
            super.endDocument();
        }
    }
}
