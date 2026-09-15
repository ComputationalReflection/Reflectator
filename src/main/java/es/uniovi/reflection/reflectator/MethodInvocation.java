package es.uniovi.reflection.reflectator;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

public class MethodInvocation extends AbstractProcessor<CtInvocation<?>> {
	public static int COUNT = 0; 
	public static boolean LOG = false;   
	public void process(CtInvocation<?> element) {
        if (Main.skip(element.getParent(CtType.class).getQualifiedName().toString())) return;

		if (this.skip(element))
			return;
		if(LOG)	System.out.printf("> Processing method invocation (%s): %s.\n", element.getPosition(), element);
		MethodInvocation.COUNT++;
		String argumentsComma = "", argumentTypesComma = "";
		for(CtTypeReference<?> parameter : element.getExecutable().getParameters()){
			argumentTypesComma += ", " + parameter.toString()+".class";
		}

		for (int i=0; i<element.getArguments().size(); i++) {
			argumentsComma += ", " + element.getArguments().get(i).toString();			
		}

        CtTypeReference<?> castType = null;
        if(element.getTypeCasts().size() > 0)
            castType = element.getTypeCasts().get(0);

		if (element.getTarget() instanceof CtTypeAccess)
			if (this.isExpression(element))
				this.processClassExpression(element, argumentsComma, argumentTypesComma,castType);
			else
				this.processClassStatement(element, argumentsComma, argumentTypesComma,castType);
		else if (element.getTarget() instanceof CtSuperAccess)
				if (this.isExpression(element))
					this.processSuperExpression(element, argumentsComma, argumentTypesComma,castType);
				else
					this.processSuperStatement(element, argumentsComma, argumentTypesComma,castType);
		else
			if (this.isExpression(element))
				this.processInstanceExpression(element, argumentsComma, argumentTypesComma,castType);
			else
				this.processInstanceStatement(element, argumentsComma, argumentTypesComma,castType);
	}
	
	private void processSuperExpression(CtInvocation<?> element, String argumentsComma, String argumentTypesComma, CtTypeReference<?> castType) {
		String code = String.format(
			"(%s java.lang.invoke.MethodHandles.lookup().findSpecial(getClass().getSuperclass(), \"%s\", java.lang.invoke.MethodType.methodType(%s.class %s), getClass()).invoke(this %s) )",
            castType!=null?"(" + castType.toString() + ")":"",
			element.getExecutable().getSimpleName(), 
			element.getType(),				
			argumentTypesComma, 
			argumentsComma); 
		Helper.replaceExpression(element, code);
	}

	private void processSuperStatement(CtInvocation<?> element, String argumentsComma, String argumentTypesComma, CtTypeReference<?> castType) {
		String code = String.format(
			"%s java.lang.invoke.MethodHandles.lookup().findSpecial(getClass().getSuperclass(), \"%s\", java.lang.invoke.MethodType.methodType(%s.class %s), getClass()).invoke(this %s)",
            castType!=null?"(" + castType.toString() + ")":"",
			element.getExecutable().getSimpleName(), 
			element.getType(),				
			argumentTypesComma, 
			argumentsComma);             
		Helper.replaceStatement(element, code);
	}

	private void processClassExpression(CtInvocation<?> element, String argumentsComma, String argumentTypesComma, CtTypeReference<?> castType) {
		String expressionCode = String.format(
				"(%s (%s)((__tempClass=Class.forName(\"%s\")).getDeclaredMethod(\"%s\" %s).invoke(__tempClass %s)) )",
                castType!=null?"(" + castType.toString() + ")":"",
				element.getType(),
				element.getTarget(), 
				element.getExecutable().getSimpleName(), 
				argumentTypesComma, 
				argumentsComma); 
		Helper.replaceExpression(element, expressionCode);
	}

	private void processClassStatement(CtInvocation<?> element, String argumentsComma, String argumentTypesComma, CtTypeReference<?> castType) {
		String statementCode = String.format(
				"%s(__tempClass=Class.forName(\"%s\")).getDeclaredMethod(\"%s\" %s).invoke(__tempClass %s)",
                castType!=null?"(" + castType.toString() + ")":"",
				element.getTarget(),
				element.getExecutable().getSimpleName(), 
				argumentTypesComma, 
				argumentsComma); 
		Helper.replaceStatement(element, statementCode);
	}

	private void processInstanceExpression(CtInvocation<?> element, String argumentsComma, String argumentTypesComma, CtTypeReference<?> castType) {
		CtTypeReference<?> methodDeclaringType = element.getExecutable().getDeclaringType();
		CtTypeReference<?> targetType = element.getTarget().getType();

		String expressionCode = String.format(
				"(%s (%s)((__tempObj=%s).getClass().%s(\"%s\" %s).invoke(__tempObj %s)) )",
                castType!=null?"(" + castType.toString() + ")":"",
				element.getType(),
				element.getTarget().toString().isEmpty()?"this":element.getTarget(),
				methodDeclaringType.equals(targetType)?"getDeclaredMethod":"getMethod",
				element.getExecutable().getSimpleName(), 
				argumentTypesComma, 
				argumentsComma); 
		Helper.replaceExpression(element, expressionCode);
	}

	private void processInstanceStatement(CtInvocation<?> element, String argumentsComma, String argumentTypesComma, CtTypeReference<?> castType) {
		CtTypeReference<?> methodDeclaringType = element.getExecutable().getDeclaringType();
		CtTypeReference<?> targetType = element.getTarget().getType();

		String statementCode = String.format(
				"%s(__tempObj=%s).getClass().%s(\"%s\" %s).invoke(__tempObj %s)",
                castType!=null?"(" + castType.toString() + ")":"",
                element.getTarget().toString().isEmpty()?"this":element.getTarget(),
				methodDeclaringType.equals(targetType)?"getDeclaredMethod":"getMethod",
				element.getExecutable().getSimpleName(), 
				argumentTypesComma, 
				argumentsComma);
		Helper.replaceStatement(element, statementCode);		
	}
	
	private boolean isExpression(CtInvocation<?> element) {
		if (element.getParent() instanceof CtBlock || element.getParent() instanceof CtCase )
			return false;
		return true;
	}
	
	private boolean skip(CtInvocation<?> element) {		
		if (element.getTarget() == null)
			return true; // super() or this()
		if(element.getExecutable().getSimpleName().equals("setInstanceField")
			||element.getExecutable().getSimpleName().equals("setInstanceDeclaredField")
			||element.getExecutable().getSimpleName().equals("setClassDeclaredField")
			||element.getExecutable().getSimpleName().equals("setSuperField")
			||element.getExecutable().getSimpleName().equals("setArray")
			||element.getExecutable().getSimpleName().equals("unaryInstanceFieldOperation")
			||element.getExecutable().getSimpleName().equals("unaryInstanceDeclaredFieldOperation")
			||element.getExecutable().getSimpleName().equals("unaryClassDeclaredFieldOperation")
			||element.getExecutable().getSimpleName().equals("unarySuperFieldOperation"))
			return true; //Helper methods
		if (element.getTarget().getType() == null)
			return true; // if it is the code we added, there is no type inferred
		if (element.getTarget().toString().equals("java.lang.Class") && element.getExecutable().getSimpleName().equals("forName"))
			return true; // class.forName() is already reflective	
		if (element.getTarget().toString().equals("java.lang.invoke.MethodHandles") && element.getExecutable().getSimpleName().equals("lookup"))
			return true; // MethodHandles.lookup() is already reflective	
		if (element.getTarget().getType().toString().equals("java.lang.Class<?>") && element.getExecutable().getSimpleName().equals("getConstructor"))
			return true; 
		if (element.getTarget().getType().toString().equals("java.lang.reflect.Constructor<?>") && element.getExecutable().getSimpleName().equals("newInstance"))
			return true; 
		return false;
	}
}
