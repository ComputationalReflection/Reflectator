package es.uniovi.reflection.reflectator;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;

public class TryCatch extends AbstractProcessor<CtTry> {

	@Override
	public void process(CtTry element) {
        if (Main.skip(element.getParent(CtType.class).getQualifiedName().toString())) return;

		Factory factory = element.getFactory();
		CtTry tryStatement = factory.Core().createTry();
		CtCatch catchStatement = factory.Core().createCatch();
		CtCatchVariable catchVariable = factory.Core().createCatchVariable();
		catchVariable.setSimpleName("e");
		catchVariable.setType(factory.Code().createCtTypeReference(Exception.class));
		catchStatement.setParameter(catchVariable);

		CtIf ifInvocationTargetException = catchStatement.getFactory().Core().createIf();
		ifInvocationTargetException.setCondition(catchStatement.getFactory().Code().createCodeSnippetExpression("e instanceof java.lang.reflect.InvocationTargetException"));
		CtBlock ifBlock  = factory.Core().createBlock();



		for(CtCatch cs: element.getCatchers())
			ifBlock.insertBegin(createIf(cs));
		ifInvocationTargetException.setThenStatement(ifBlock);
		ifInvocationTargetException.setElseStatement(catchStatement.getFactory().Code().createCodeSnippetStatement("throw e"));


		catchStatement.setBody(factory.Core().createBlock());
		catchStatement.getBody().insertBegin(ifInvocationTargetException);
		tryStatement.addCatcher(catchStatement);
		tryStatement.setBody(element.getBody().clone());
		element.getBody().getStatements().clear();
		element.getBody().insertBegin(tryStatement);
	}


	private CtIf createIf(CtCatch catchStatement){
		CtIf ifStatement = catchStatement.getFactory().Core().createIf();
		ifStatement.setCondition(catchStatement.getFactory().Code().createCodeSnippetExpression("((java.lang.reflect.InvocationTargetException)e).getTargetException() instanceof " + catchStatement.getParameter().getType().getQualifiedName()));
		ifStatement.setThenStatement(catchStatement.getFactory().Code().createCodeSnippetStatement("throw (" + catchStatement.getParameter().getType().getQualifiedName() + ")((java.lang.reflect.InvocationTargetException)e).getTargetException()"));
		return ifStatement;
	}
}
