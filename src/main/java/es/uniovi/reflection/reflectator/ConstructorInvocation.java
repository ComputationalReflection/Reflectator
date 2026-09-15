package es.uniovi.reflection.reflectator;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.declaration.CtType;

public class ConstructorInvocation extends AbstractProcessor<CtConstructorCall<?>> {
	public static int COUNT = 0;
	public static boolean LOG = false;
	public void process(CtConstructorCall<?> element) {
        if (Main.skip(element.getParent(CtType.class).getQualifiedName().toString())) return;

		if(LOG)	System.out.printf("> Processing constructor invocation (%s): %s.\n", element.getPosition(), element);
		ConstructorInvocation.COUNT++;

		String argumentsComma = "", argumentTypesComma = "";		
		for (int i=0; i<element.getExecutable().getParameters().size(); i++) {
			argumentTypesComma += element.getExecutable().getParameters().get(i).toString()+".class";
			if (i < element.getExecutable().getParameters().size()-1) {
				argumentTypesComma += ", ";				
			}
		}

		for (int i=0; i<element.getArguments().size(); i++) {
			argumentsComma += element.getArguments().get(i).toString();			
			if (i < element.getArguments().size()-1) {
				argumentsComma += ", ";				
			}
		}

		String expressionCode = "";
		if(!"".equals(argumentTypesComma))
		{		
			expressionCode = String.format(
					"( (%s)(Class.forName(\"%s\").getConstructor(%s).newInstance(%s)) )",
					element.getType(),
					element.getType(),
					argumentTypesComma,
					argumentsComma);
		}
		else{
			expressionCode = String.format(
					"( (%s)(Class.forName(\"%s\").newInstance()) )",
					element.getType(),
					element.getType());
		}
		Helper.replaceExpression(element, expressionCode);
	}
	
}
