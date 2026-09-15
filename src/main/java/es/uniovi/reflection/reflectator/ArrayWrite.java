package es.uniovi.reflection.reflectator;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtType;

public class ArrayWrite extends AbstractProcessor<CtArrayWrite<?>> {
    public static int COUNT = 0;   
    public static boolean LOG = false; 
    public void process(CtArrayWrite<?> element) {
        if (Main.skip(element.getParent(CtType.class).getQualifiedName().toString())) return;

        if(LOG)	System.out.printf("> Processing array write (%s): %s.\n", element.getPosition(), element);
        ArrayWrite.COUNT++;
        if (isExpression(element.getParent()))
            processAssignmentExpression(element);
        else
            processAssignmentStatement(element);
    }

    private CtExpression<?> getRvalue(CtArrayWrite<?> element) {
        CtAssignment<?,?> assignment = (CtAssignment<?,?>)element.getParent();
        return assignment.getAssignment();
    }

    private boolean isExpression(CtElement element) {
        if (element.getParent() instanceof CtBlock)
            return false;
        return true;
    }

    private void processAssignmentStatement(CtArrayWrite<?> element){
        String statementCode = String.format(
                "java.lang.reflect.Array.set(%s, %s,%s)",
                element.getTarget(),
                element.getIndexExpression(),
                getRvalue(element)
        );
        Helper.replaceStatement((CtStatement)element.getParent(), statementCode);
    }

    private void processAssignmentExpression(CtArrayWrite<?> element){
        String expressionCode = String.format(
                "((%s)(setArray(%s, %s,%s)))",
                element.getType(),
                element.getTarget(),
                element.getIndexExpression(),
                getRvalue(element)
        );
        Helper.replaceExpression((CtExpression<?>) element.getParent(), expressionCode);
    }
}
