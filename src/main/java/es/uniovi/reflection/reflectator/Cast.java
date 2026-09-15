package es.uniovi.reflection.reflectator;

import java.util.ArrayList;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;

public class Cast extends AbstractProcessor<CtExpression<?>> {
	public static int COUNT = 0;
	public static boolean LOG = false;
	public void process(CtExpression<?> element) {
        if (Main.skip(element.getParent(CtType.class).getQualifiedName().toString())) return;

		if(element.getTypeCasts().size() == 1)
		{
			CtTypeReference<?> typeCast = element.getTypeCasts().get(0);
			if(!typeCast.isPrimitive())
			{				
				if(LOG)	System.out.printf("> Processing cast (%s): %s.\n", element.getPosition(), element);
				Cast.COUNT++;
				CtExpression<?> cloned = element.clone();
				cloned.setTypeCasts(new ArrayList<CtTypeReference<?>>());
					
				CtFieldRead<Object> fieldRead = getFactory().createFieldRead();
				fieldRead.setTarget(getFactory().createTypeAccess(typeCast));
				CtFieldReference<Object> fieldReference = getFactory().createFieldReference();
				fieldReference.setSimpleName("class");
				fieldRead.setVariable(fieldReference);
				CtExecutableReference<Object> executableReference = getFactory().createExecutableReference();
				executableReference.setSimpleName("cast");
				CtInvocation<Object> invocation = getFactory().createInvocation(fieldRead,executableReference,cloned);
				element.replace(invocation);				
			}
		}
	}
}
