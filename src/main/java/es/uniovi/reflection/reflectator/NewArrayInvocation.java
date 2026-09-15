package es.uniovi.reflection.reflectator;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtNewArray;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeReference;

public class NewArrayInvocation extends AbstractProcessor<CtNewArray<?>> {
	public static int COUNT = 0;  
	public static boolean LOG = false;  
	public void process(CtNewArray<?> element) {
        if (Main.skip(element.getParent(CtType.class).getQualifiedName().toString())) return;

		if(element.getDimensionExpressions().size()==0) return;		
		if(LOG)	System.out.printf("> Processing array construction (%s): %s.\n", element.getPosition(), element);
		NewArrayInvocation.COUNT++;
		CtTypeReference<?> arrayType = element.getType();
		CtTypeReference<?> componentType = arrayType;
		while(componentType instanceof CtArrayTypeReference)
			componentType = ((CtArrayTypeReference<?>) componentType).getComponentType();

		String dimmensionsComma = "";
		for (int i=0; i<element.getDimensionExpressions().size(); i++) {
			dimmensionsComma += element.getDimensionExpressions().get(i);
			if (i < element.getDimensionExpressions().size() - 1) 
				dimmensionsComma += ", ";
		}
		String expressionCode = String.format(
				"( (%s)(java.lang.reflect.Array.newInstance(%s.class, %s)) )",
				arrayType,
				componentType,
				dimmensionsComma);
		Helper.replaceExpression(element, expressionCode);
	}
	
}

