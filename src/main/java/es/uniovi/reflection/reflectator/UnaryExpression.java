package es.uniovi.reflection.reflectator;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtSuperAccess;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

public class UnaryExpression extends AbstractProcessor<CtUnaryOperator<?>> {
	public static int COUNT = 0;
	public static boolean LOG = false;
	
	public void process(CtUnaryOperator<?> element) {
        if (Main.skip(element.getParent(CtType.class).getQualifiedName().toString())) return;

		if (element.getOperand() instanceof CtFieldAccess<?>) 
		{			
			if(LOG)	System.out.printf("> Processing unary expression (%s): %s.\n", element.getPosition(), element);
			UnaryExpression.COUNT++;			
			if(((CtFieldAccess<?>)element.getOperand()).getTarget() instanceof CtSuperAccess<?>)
				processUnarySuperFieldOperation(element);
			else if(((CtFieldAccess<?>)element.getOperand()).getTarget() instanceof CtTypeAccess<?>)
				processUnaryClassFieldOperation(element);
			else
				processUnaryInstanceFieldOperation(element);
		}
	}	

	private void processUnaryInstanceFieldOperation(CtUnaryOperator<?> element) {
		CtFieldAccess<?> fieldRead = (CtFieldAccess<?>)element.getOperand();
		CtTypeReference<?> fieldDeclaringType = fieldRead.getVariable().getDeclaringType();
		CtTypeReference<?> targetType = fieldRead.getTarget().getType();

		if(element.getParent() instanceof CtBlock)
		{
			String statementCode = String.format(
				"%s(%s, \"%s\", \"%s\")",		
				fieldDeclaringType.equals(targetType)?"unaryInstanceDeclaredFieldOperation":"unaryInstanceFieldOperation",		
				fieldRead.getTarget().toString().isEmpty()?"this":fieldRead.getTarget(),
				fieldRead.getVariable().getSimpleName(),
				element.getKind());			
			Helper.replaceStatement((CtStatement)element, statementCode);		
		}
		else{
			String expressionCode = String.format(				
				"( (%s)(%s(%s, \"%s\", \"%s\")) )",
				element.getType(),		
				fieldDeclaringType.equals(targetType)?"unaryInstanceDeclaredFieldOperation":"unaryInstanceFieldOperation",				
				fieldRead.getTarget().toString().isEmpty()?"this":fieldRead.getTarget(),
				fieldRead.getVariable().getSimpleName(),
				element.getKind());
			Helper.replaceExpression((CtExpression<?>)element, expressionCode);		
		}
	}
	private void processUnarySuperFieldOperation(CtUnaryOperator<?> element) {
		CtFieldAccess<?> fieldRead = (CtFieldAccess<?>)element.getOperand();
		if(element.getParent() instanceof CtBlock)
		{
			String statementCode = String.format(				
				"unarySuperFieldOperation(getClass().getSuperclass(),this, \"%s\", %s.class, \"%s\")",								
				fieldRead.getVariable().getSimpleName(),
				element.getType(),
				element.getKind());			
			Helper.replaceStatement((CtStatement)element, statementCode);		
		}
		else{
			String expressionCode = String.format(
				"( (%s)(unarySuperFieldOperation(getClass().getSuperclass(),this, \"%s\", %s.class, \"%s\")) )",
				element.getType(),
				fieldRead.getVariable().getSimpleName(),
				element.getType(),
				element.getKind());
			Helper.replaceExpression((CtExpression<?>)element, expressionCode);		
		}
	}

	private void processUnaryClassFieldOperation(CtUnaryOperator<?> element) {
		CtFieldAccess<?> fieldRead = (CtFieldAccess<?>)element.getOperand();
		if(element.getParent() instanceof CtBlock)
		{
			String statementCode = String.format(				
				"unaryClassDeclaredFieldOperation(getClass(),\"%s\",\"%s\")",								
				fieldRead.getVariable().getSimpleName(),				
				element.getKind());			
			Helper.replaceStatement((CtStatement)element, statementCode);		
		}
		else{
			String expressionCode = String.format(
				"( (%s)(unaryClassDeclaredFieldOperation(getClass(),\"%s\",\"%s\")) )",
				element.getType(),
				fieldRead.getVariable().getSimpleName(),				
				element.getKind());
			Helper.replaceExpression((CtExpression<?>)element, expressionCode);		
		}
	}
	
}

