package es.uniovi.reflection.reflectator;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ToolParameters {
    private static final String COPYRIGHT_MESSAGE =
            "Reflectator 1.0.0 - Computational Reflection Research Group (University of Oviedo)\n";

    public static final String HELP_MESSAGE = COPYRIGHT_MESSAGE + "\nOptions:\n" +
            "\t-help\n\t\tDisplays this usage message. Short form: -?.\n" +
            "\t-source <source_folder>\n\t\tSource folder to process. Short form: -s. Also accepts -source=<source_folder>.\n" +
            "\t-out <output_folder>\n\t\tBase output folder. Reflectator writes into an RF* subfolder. Short form: -o.\n" +
            "\t-exclude <class_name>\n\t\tClass or package to exclude from reflection. Short form: -x. Can be repeated.\n" +
            "\t-c\n\t\tReflect casts.\n" +
            "\t-fr\n\t\tReflect field reads.\n" +
            "\t-fw\n\t\tReflect field writes.\n" +
            "\t-ar\n\t\tReflect array reads.\n" +
            "\t-aw\n\t\tReflect array writes.\n" +
            "\t-na\n\t\tReflect new array invocations.\n" +
            "\t-ci\n\t\tReflect constructor invocations.\n" +
            "\t-mi\n\t\tReflect method invocations.\n" +
            "\t-full\n\t\tEnables all optional transformations.\n" +
            "\t<class_name> [<class_name> ...]\n\t\tOnly reflect the listed classes. Use fully qualified names or .java file names.\n";

    private static final String PACKAGE_NAME = "es.uniovi.reflection.reflectator.";

    private Path sourceProjectPath;
    private Path spoonedPath;
    private final List<String> reflectedClasses = new ArrayList<>();
    private final List<String> excludedClasses = new ArrayList<>();
    private final List<String> processors = new ArrayList<>();
    private String folderName = "";

    private ToolParameters() {
        addProcessor("ClassDeclaration");
        addProcessor("OperatorAssignement");
        addProcessor("UnaryExpression");
        addProcessor("TryCatch");
    }

    public static ToolParameters parseArguments(String[] args) {
        ToolParameters parameters = new ToolParameters();
        try {
            parameters.parse(args);
            parameters.validate();
            parameters.addProcessor("MethodDeclaration");
            parameters.addProcessor("ConstructorDeclaration");
            parameters.addProcessor("Block");
            parameters.spoonedPath = parameters.spoonedPath.resolve("RF" + parameters.folderName);
            return parameters;
        } catch (ParameterException e) {
            System.err.println(e.getMessage());
            System.err.println();
            System.err.println(HELP_MESSAGE);
            System.exit(2);
            return parameters;
        }
    }

    public Path getSourceProjectPath() {
        return sourceProjectPath;
    }

    public Path getSpoonedPath() {
        return spoonedPath;
    }

    public List<String> getReflectedClasses() {
        return Collections.unmodifiableList(reflectedClasses);
    }

    public List<String> getExcludedClasses() {
        return Collections.unmodifiableList(excludedClasses);
    }

    public String getProcessorsOption() {
        return String.join(File.pathSeparator, processors);
    }

    private void parse(String[] args) {
        for (int i = 0; i < args.length; i++) {
            ParsedArgument argument = split(args[i]);
            String option = argument.name.toLowerCase(Locale.ROOT);
            switch (option) {
                case "-help":
                case "--help":
                case "-?":
                    System.out.println(HELP_MESSAGE);
                    System.exit(0);
                    break;
                case "-source":
                case "--source":
                case "-s":
                    sourceProjectPath = Paths.get(readValue(argument, args, i));
                    if (argument.value == null) {
                        i++;
                    }
                    break;
                case "-out":
                case "--out":
                case "-o":
                    spoonedPath = Paths.get(readValue(argument, args, i));
                    if (argument.value == null) {
                        i++;
                    }
                    break;
                case "-exclude":
                case "--exclude":
                case "-x":
                    excludedClasses.add(normalizeClassName(readValue(argument, args, i)));
                    if (argument.value == null) {
                        i++;
                    }
                    break;
                case "-c":
                    addOptionalProcessor("Cast", argument.name);
                    break;
                case "-fr":
                    addOptionalProcessor("FieldRead", argument.name);
                    break;
                case "-fw":
                    addOptionalProcessor("FieldWrite", argument.name);
                    break;
                case "-ar":
                    addOptionalProcessor("ArrayRead", argument.name);
                    break;
                case "-aw":
                    addOptionalProcessor("ArrayWrite", argument.name);
                    break;
                case "-na":
                    addOptionalProcessor("NewArrayInvocation", argument.name);
                    break;
                case "-ci":
                    addOptionalProcessor("ConstructorInvocation", argument.name);
                    break;
                case "-mi":
                    addOptionalProcessor("MethodInvocation", argument.name);
                    break;
                case "-full":
                case "--full":
                    addFullProcessors(argument.name);
                    break;
                default:
                    if (argument.name.startsWith("-")) {
                        throw new ParameterException("Unknown option: " + argument.name);
                    }
                    String className = normalizeClassName(argument.name);
                    reflectedClasses.add(className);
                    folderName += "_" + className;
                    System.out.println("Class " + className + " will be reflected.");
            }
        }
    }

    private void validate() {
        if (sourceProjectPath == null) {
            throw new ParameterException("Missing required option: -source <source_folder>");
        }
        if (spoonedPath == null) {
            throw new ParameterException("Missing required option: -out <output_folder>");
        }
    }

    private void addProcessor(String simpleName) {
        String processor = PACKAGE_NAME + simpleName;
        if (!processors.contains(processor)) {
            processors.add(processor);
        }
    }

    private void addOptionalProcessor(String simpleName, String folderSuffix) {
        addProcessor(simpleName);
        folderName += folderSuffix;
    }

    private void addFullProcessors(String folderSuffix) {
        addOptionalProcessor("Cast", "");
        addOptionalProcessor("FieldRead", "");
        addOptionalProcessor("FieldWrite", "");
        addOptionalProcessor("ArrayRead", "");
        addOptionalProcessor("ArrayWrite", "");
        addOptionalProcessor("NewArrayInvocation", "");
        addOptionalProcessor("ConstructorInvocation", "");
        addOptionalProcessor("MethodInvocation", "");
        folderName += folderSuffix;
    }

    private String readValue(ParsedArgument argument, String[] args, int currentIndex) {
        if (argument.value != null) {
            if (argument.value.isBlank()) {
                throw new ParameterException("Missing value for option: " + argument.name);
            }
            return argument.value;
        }
        int valueIndex = currentIndex + 1;
        if (valueIndex >= args.length || args[valueIndex].startsWith("-")) {
            throw new ParameterException("Missing value for option: " + argument.name);
        }
        return args[valueIndex];
    }

    private ParsedArgument split(String parameter) {
        int separatorIndex = parameter.indexOf('=');
        if (separatorIndex < 0) {
            return new ParsedArgument(parameter, null);
        }
        return new ParsedArgument(
                parameter.substring(0, separatorIndex),
                parameter.substring(separatorIndex + 1));
    }

    private String normalizeClassName(String className) {
        return className.endsWith(".java")
                ? className.substring(0, className.length() - ".java".length())
                : className;
    }

    private static class ParsedArgument {
        private final String name;
        private final String value;

        private ParsedArgument(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    private static class ParameterException extends RuntimeException {
        private ParameterException(String message) {
            super(message);
        }
    }
}
