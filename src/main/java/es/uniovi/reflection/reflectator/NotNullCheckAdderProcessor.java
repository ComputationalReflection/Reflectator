package es.uniovi.reflection.reflectator;

import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.processing.*;

public class NotNullCheckAdderProcessor extends
		AbstractProcessor<CtParameter<?>> {
	@Override
	public boolean isToBeProcessed(CtParameter<?> element) {
		return !element.getType().isPrimitive();// only for objects
	}

	public void process(CtParameter<?> element) {
        if (Main.skip(element.getParent(CtType.class).getQualifiedName().toString())) return;

		// we declare a new snippet of code to be inserted
		CtCodeSnippetStatement snippet = getFactory().Core().createCodeSnippetStatement();

		// this snippet contains an if check
		snippet.setValue("if("
				+ element.getSimpleName()
				+ " == null "
				+ ") throw new IllegalArgumentException(\"[Spoon inserted check] null passed as parameter\");");

		// we insert the snippet at the beginning of the method boby
		if (element.getParent(CtExecutable.class).getBody() != null) {
			System.out.println("\tAdding snippet");
			element.getParent(CtExecutable.class).getBody()
					.insertBegin(snippet);
		}
	}

	public static void main(String... args) throws Exception {
		spoon.Launcher.main(new String[] { 
				"-p", "NotNullCheckAdderProcessor", 
				"-i", "/Users/ortin/Documents/workspace/prueba/src" });
	}
}
