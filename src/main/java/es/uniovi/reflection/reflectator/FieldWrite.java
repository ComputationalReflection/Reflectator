package es.uniovi.reflection.reflectator;

import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtTypeReference;
import spoon.processing.*;

public class FieldWrite extends AbstractProcessor<CtFieldWrite<?>> {
	public static int COUNT = 0;
	public static boolean LOG = false;
	public void process(CtFieldWrite<?> element) {
        if (Main.skip(element.getParent(CtType.class).getQualifiedName().toString())) return;

		if (this.skip(element))
			return;
		if(LOG)	System.out.printf("> Processing field write (%s): %s.\n", element.getPosition(), element);
		FieldWrite.COUNT++;
		if (element.getTarget() instanceof CtTypeAccess) 
			if (isExpression(element.getParent()))
				this.processClassFieldWriteExpression(element);
			else
				this.processClassFieldWriteStatement(element);
		else if (element.getTarget() instanceof CtSuperAccess) 
			if (isExpression(element.getParent()))
				this.processSuperFieldWriteExpression(element);
			else
				this.processSuperFieldWriteStatement(element);		
		else
			if (isExpression(element.getParent()))
				this.processInstanceFieldWriteExpression(element);
			else 
				this.processInstanceFieldWriteStatement(element);
	}

	private boolean skip(CtFieldWrite<?> element) {
		if (element.getVariable().toString().endsWith(".class"))
			return true; // int.class, String.class... should not be processed		
		return false;
	}
	
	private void processSuperFieldWriteExpression(CtFieldWrite<?> element) {
		String expressionCode = String.format(
				"((%s) (setSuperField(getClass().getSuperclass(),this, \"%s\",%s.class,%s)))",
				element.getType(),				
				element.getVariable().getSimpleName(),
				element.getType(),
				this.getLvalue(element));
		Helper.replaceExpression((CtExpression<?>)element.getParent(), expressionCode);
	}

	private void processSuperFieldWriteStatement(CtFieldWrite<?> element) {
		String statementCode = String.format(
				"setSuperField(getClass().getSuperclass(),this, \"%s\",%s.class,%s)",								
				element.getVariable().getSimpleName(),
				element.getType(),
				this.getLvalue(element));
		Helper.replaceStatement((CtStatement)element.getParent(), statementCode);
	}

	private void processClassFieldWriteExpression(CtFieldWrite<?> element) {
		String expressionCode = String.format(
				"((%s) (setClassDeclaredField(Class.forName(\"%s\"), \"%s\", %s)))",
				element.getType(),
				element.getTarget(),
				element.getVariable().getSimpleName(),
				this.getLvalue(element));
		Helper.replaceExpression((CtExpression<?>)element.getParent(), expressionCode);
	}

	private void processClassFieldWriteStatement(CtFieldWrite<?> element) {
		String statementCode = String.format(
				"(__tempClass=Class.forName(\"%s\")).getDeclaredField(\"%s\").set(__tempClass, %s)",
				element.getTarget(),
				element.getVariable().getSimpleName(),
				this.getLvalue(element));
		Helper.replaceStatement((CtStatement)element.getParent(), statementCode);
	}

	private void processInstanceFieldWriteExpression(CtFieldWrite<?> element) {
		CtTypeReference<?> fieldDeclaringType = element.getVariable().getDeclaringType();
		CtTypeReference<?> targetType = element.getTarget().getType();

		String expressionCode = String.format(
				"( (%s)(%s(%s, \"%s\", %s)) )",
				element.getType(),
				fieldDeclaringType.equals(targetType)?"setInstanceDeclaredField":"setInstanceField",
				element.getTarget().toString().isEmpty()?"this":element.getTarget(),
				element.getVariable().getSimpleName(),
				this.getLvalue(element));
		Helper.replaceExpression((CtExpression<?>)element.getParent(), expressionCode);
	}
	
	private void processInstanceFieldWriteStatement(CtFieldWrite<?> element) {
		CtTypeReference<?> fieldDeclaringType = element.getVariable().getDeclaringType();
		CtTypeReference<?> targetType = element.getTarget().getType();
		String statementCode = String.format(
				"(__tempObj=%s).getClass().%s(\"%s\").set(__tempObj, %s)",
				element.getTarget().toString().isEmpty()?"this":element.getTarget(),
				fieldDeclaringType.equals(targetType)?"getDeclaredField":"getField",
				element.getVariable().getSimpleName(),
				this.getLvalue(element));
		Helper.replaceStatement((CtStatement)element.getParent(), statementCode);
	}
	
	private boolean isExpression(CtElement element) {
		if (element.getParent() instanceof CtBlock || element.getParent() instanceof CtFor)
			return false;
		return true;
	}
	
	private CtExpression<?> getLvalue(CtFieldWrite<?> element) {
		CtElement lValue = element.getParent();
		if(lValue instanceof CtAssignment<?,?>)
			return ((CtAssignment<?,?>)lValue).getAssignment();
		else if(lValue instanceof CtUnaryOperator<?>)
			return ((CtUnaryOperator<?>)lValue).getOperand();
		return null;
	}
	
}
