package es.uniovi.reflection.reflectator;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtSuperAccess;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeReference;

public class FieldRead extends AbstractProcessor<CtFieldRead<?>> {
	public static int COUNT = 0;
	public static boolean LOG = false;

	/**
	 * Add reflective access to an object and class field
	 */
	public void process(CtFieldRead<?> element) {
        if (Main.skip(element.getParent(CtType.class).getQualifiedName().toString())) return;

		if (this.skip(element))
			return;
		if(LOG)	System.out.printf("> Processing field read (%s): %s.\n", element.getPosition(), element);
		FieldRead.COUNT++;
		if (element.getTarget() instanceof CtTypeAccess) 
			this.processClassFieldRead(element);
		else if(element.getTarget() instanceof CtSuperAccess)
			this.processSuperFieldRead(element);
		else
			this.processInstanceFieldRead(element);
	}

	private void processSuperFieldRead(CtFieldRead<?> fieldRead) {
		CtTypeReference<?> type = null;
		if(fieldRead.getTypeCasts().size() > 0)
			type = fieldRead.getTypeCasts().get(0);

		String code = String.format(                         
				"%s (%s)(java.lang.invoke.MethodHandles.lookup().findGetter(getClass().getSuperclass(), \"%s\", %s.class).invoke(this))",
				type!=null?"(" + type.toString() + ")":"",
				fieldRead.getType(),
				fieldRead.getVariable().getSimpleName(), 
				fieldRead.getType()); 
		Helper.replaceExpression(fieldRead, "(" + code + ")");
	}

	private boolean skip(CtFieldRead<?> element) {
		if (element.getVariable().toString().endsWith(".class"))
			return true; // int.class, String.class... should not be processed
		return false;
	}
	
	private void processClassFieldRead(CtFieldRead<?> element) {
		CtTypeReference<?> type = null;
		if(element.getTypeCasts().size() > 0)
			type = element.getTypeCasts().get(0);

		String expressionCode = String.format(
				"(%s (%s) ((__tempClass=Class.forName(\"%s\")).getDeclaredField(\"%s\").get(__tempClass)) )",
				type!=null?"(" + type.toString() + ")":"",
				element.getType(),
				element.getTarget(),				
				element.getVariable().getSimpleName());
		Helper.replaceExpression(element, expressionCode);
	}

	public void processInstanceFieldRead(CtFieldRead<?> element) {
		CtTypeReference<?> fieldDeclaringType = element.getVariable().getDeclaringType();
		CtTypeReference<?> targetType = element.getTarget().getType();
		CtTypeReference<?> type = null;
		if(element.getTypeCasts().size() > 0)
			type = element.getTypeCasts().get(0);

		String expressionCode = String.format(
				"(%s (%s) ((__tempObj=%s).getClass().%s(\"%s\").get(__tempObj)) )",
				type!=null?"(" + type.toString() + ")":"",
				element.getType(),
				element.getTarget().toString().isEmpty()?"this":element.getTarget(),
				fieldDeclaringType.equals(targetType)?"getDeclaredField":"getField",
				element.getVariable().getSimpleName());

		if(element.getTarget().getType() instanceof CtArrayTypeReference && element.getVariable().getSimpleName().equals("length")) {
            expressionCode = String.format(
                    "((%s) (java.lang.reflect.Array.getLength(__tempObj=%s)) )",
                    element.getType(),
                    element.getTarget().toString().isEmpty()?"this":element.getTarget());
        }

		Helper.replaceExpression(element, expressionCode);
	}
}
