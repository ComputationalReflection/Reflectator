package es.uniovi.reflection.reflectator;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtType;

public class OperatorAssignement extends AbstractProcessor<CtOperatorAssignment<?,?>> {	
	public void process(CtOperatorAssignment<?,?> element) {
        if (Main.skip(element.getParent(CtType.class).getQualifiedName().toString())) return;

        if(element.getAssigned() instanceof CtFieldWrite){            
            CtFieldWrite fieldWrite = (CtFieldWrite<?>)element.getAssigned();

            CtFieldRead<?> fieldRead = getFactory().Core().createFieldRead();
            fieldRead.setTarget(fieldWrite.getTarget());
            fieldRead.setVariable(fieldWrite.getVariable());
            fieldRead.setType(fieldWrite.getType());

            CtBinaryOperator binaryOperator = getFactory().Code().createBinaryOperator(fieldRead, element.getAssignment(), element.getKind());
                                    
            CtAssignment assignment = getFactory().Core().createAssignment();
            assignment.setAssigned(element.getAssigned());
            assignment.setAssignment(binaryOperator);
            assignment.setType(element.getType());

            element.replace((CtStatement) assignment);
        }

        else if(element.getAssigned() instanceof CtArrayWrite){            
            CtArrayWrite arrayWrite = (CtArrayWrite<?>)element.getAssigned();

            CtArrayRead<?> arrayRead = getFactory().Core().createArrayRead();
            arrayRead.setTarget(arrayWrite.getTarget());
            arrayRead.setIndexExpression(arrayWrite.getIndexExpression());
            arrayRead.setType(arrayWrite.getType());

            CtBinaryOperator binaryOperator = getFactory().Code().createBinaryOperator(arrayRead, element.getAssignment(), element.getKind());
                                    
            CtAssignment assignment = getFactory().Core().createAssignment();
            assignment.setAssigned(element.getAssigned());
            assignment.setAssignment(binaryOperator);
            assignment.setType(element.getType());

            element.replace((CtStatement) assignment);
        }
	}	
}