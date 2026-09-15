package es.uniovi.reflection.reflectator;
import spoon.reflect.code.*;
import spoon.processing.*;
import spoon.reflect.declaration.CtType;

public class ArrayRead extends AbstractProcessor<CtArrayRead<?>> {
	public static int COUNT = 0;
	public static boolean LOG = false;
	/**
	 * Add reflective access to an array read expression
	 */
	public void process(CtArrayRead<?> element) {
        if (Main.skip(element.getParent(CtType.class).getQualifiedName().toString())) return;

		if(LOG)	System.out.printf("> Processing array read (%s): %s.\n", element.getPosition(), element);
        ArrayRead.COUNT++;
		String expressionCode = String.format(
				"((%s)java.lang.reflect.Array.get(%s, %s))",
				element.getType(),
				element.getTarget(),
				element.getIndexExpression()
				);
		Helper.replaceExpression(element, expressionCode);
	}

}
