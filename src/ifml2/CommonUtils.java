package ifml2;

public class CommonUtils
{
    private static String getCurrentDirectory()
    {
        return System.getProperty("user.dir");
    }

    public static String getSamplesDirectory()
    {
        return getCurrentDirectory() + CommonConstants.GAMES_DIRECTORY;
    }

    public static String getLibrariesDirectory()
    {
        return getCurrentDirectory() + CommonConstants.LIBRARIES_DIRECTORY;
    }

    public static String uppercaseFirstLetter(String s)
    {
        if(s == null || "".equals(s))
        {
            return "";
        }
        else
        {
            return s.substring(0, 1).toUpperCase() + s.substring(1);
        }
    }

    public static String getTestsDirectory()
    {
        return getCurrentDirectory() + CommonConstants.TESTS_DIRECTORY;
    }

    /*public static String getExtendedStackTrace(Throwable e)
    {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        StringBuilder stringBuilder = new StringBuilder(stringWriter.toString());
        Throwable cause = e;
        while(cause.getCause() != null && cause != cause.getCause())
        {
            cause = cause.getCause();
            stringBuilder.append("\n[Extended] Caused by:\n");
            cause.printStackTrace(printWriter);
            stringBuilder.append(stringWriter.toString());
        }
        return stringBuilder.toString();
    }*/
}