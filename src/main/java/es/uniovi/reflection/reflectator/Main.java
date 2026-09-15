package es.uniovi.reflection.reflectator;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Main {

	private static Path SOURCE_PROJECT_PATH;
	private static Path SPOONED_PATH;
    public static List<String> REFLECTED_CLASSES = new ArrayList<>();
    public static List<String> EXCLUDED_CLASSES = new ArrayList<>();

    public static boolean skip(String className)
    {
        if (isExcluded(className))
            return true;
        return !REFLECTED_CLASSES.isEmpty() && !REFLECTED_CLASSES.contains(className);
    }

    private static boolean isExcluded(String className)
    {
        for (String excludedClass : EXCLUDED_CLASSES)
        {
            if (className.equals(excludedClass)
                    || className.endsWith("." + excludedClass)
                    || className.startsWith(excludedClass + ".")
                    || className.startsWith(excludedClass + "$")
                    || className.contains("." + excludedClass + ".")
                    || className.contains("." + excludedClass + "$"))
                return true;
        }
        return false;
    }

	private static void generateIntrospectionHelper(String filename) throws IOException {
		Path introspectorFolder = SPOONED_PATH.resolve("introspector");
		Files.createDirectories(introspectorFolder);

		try (InputStream in = Main.class.getResourceAsStream("/" + filename)) {
			if (in == null)
				throw new FileNotFoundException("Resource not found: " + filename);
			Files.copy(in, introspectorFolder.resolve(filename),
					java.nio.file.StandardCopyOption.REPLACE_EXISTING);
		}
	}

	private static void printStatistics() throws IOException {
		List<String> lines = new ArrayList<>();
        lines.add("Class: " + ClassDeclaration.COUNT);
		lines.add("Cast: " + Cast.COUNT);
		lines.add("FieldRead: " + FieldRead.COUNT);
		lines.add("FieldWrite: " + FieldWrite.COUNT);
		lines.add("ArrayRead: " + ArrayRead.COUNT);
		lines.add("ArrayWrite: " + ArrayWrite.COUNT);
		lines.add("MethodInvocation: " + MethodInvocation.COUNT);
		lines.add("ConstructorInvocation: " + ConstructorInvocation.COUNT);
		lines.add("NewArrayInvocation: " + NewArrayInvocation.COUNT);

		Path file = SPOONED_PATH.resolve("statistics.txt");
		Files.write(file, lines, Charset.forName("UTF-8"));
	}

	private static void setLog()
	{
        ClassDeclaration.LOG = false;
        Cast.LOG = false;
		UnaryExpression.LOG = false;
		FieldRead.LOG = false;
		FieldWrite.LOG = false;
		ArrayRead.LOG = false;
		ArrayWrite.LOG = false;
		MethodInvocation.LOG = false;
		ConstructorInvocation.LOG = false;
		NewArrayInvocation.LOG = false;
	}
	
	public static void main(String... args) throws Exception {
		setLog();
		ToolParameters parameters = ToolParameters.parseArguments(args);
		SOURCE_PROJECT_PATH = parameters.getSourceProjectPath();
		SPOONED_PATH = parameters.getSpoonedPath();
		REFLECTED_CLASSES.clear();
		REFLECTED_CLASSES.addAll(parameters.getReflectedClasses());
		EXCLUDED_CLASSES.clear();
		EXCLUDED_CLASSES.addAll(parameters.getExcludedClasses());
        Files.createDirectories(SPOONED_PATH);

		generateIntrospectionHelper("IntrospectorHelper.java");
		spoon.Launcher.main(new String[] {
				"-p", parameters.getProcessorsOption(),
				"-i", SOURCE_PROJECT_PATH.toString(),
				"-o", SPOONED_PATH.toString()
		});
		//printStatistics();
	}
}
