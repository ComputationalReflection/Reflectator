package es.uniovi.reflection.reflectator;

import spoon.reflect.code.*;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;

import java.util.List;

public class Helper {
    
	public static CtExpression<?> replaceExpression(CtExpression<?> expression, String code) {
		CtCodeSnippetExpression<?> snippet = expression.getFactory().Code().createCodeSnippetExpression(code);
		CtTypeReference type = expression.getType();
		snippet.setType(type);
		expression.replace(snippet);
		return expression;
	}
	
	public static CtStatement replaceStatement(CtStatement statement, String code) {
		CtCodeSnippetStatement snippet = statement.getFactory().Code().createCodeSnippetStatement(code);		
		statement.replace(snippet);
		return statement;
	}


	public static CtTry createTry(List<CtStatement> statements) {
		if(statements.size() == 0) return null;

		Factory factory = statements.get(0).getFactory();
		CtTry tryStatement = factory.Core().createTry();
		CtCatch catchStatement = factory.Core().createCatch();
		CtCatchVariable catchVariable = factory.Core().createCatchVariable();
		catchVariable.setSimpleName("ex");
		catchVariable.setType(factory.Code().createCtTypeReference(Throwable.class));
		catchStatement.setParameter(catchVariable);
		catchStatement.setBody(factory.Core().createBlock());
		catchStatement.getBody().insertBegin(factory.Code().createCodeSnippetStatement("throw new RuntimeException(ex)"));
		tryStatement.addCatcher(catchStatement);
		tryStatement.setBody(factory.Core().createBlock());

		CtLocalVariable<?> objectVariable = factory.createLocalVariable();
		objectVariable.setSimpleName("__tempObj");
		CtTypeReference objectTypeRef = factory.createCtTypeReference(Object.class);
		objectVariable.setType(objectTypeRef);
		tryStatement.getBody().insertBegin(objectVariable);

		CtLocalVariable<?> classVariable = factory.createLocalVariable();
		classVariable.setSimpleName("__tempClass");
		CtTypeReference classTypeRef = factory.createCtTypeReference(Class.class);
		classVariable.setType(classTypeRef);
		tryStatement.getBody().insertBegin(classVariable);

		for (CtStatement st  : statements)
			tryStatement.getBody().getStatements().add(st.clone());
		return tryStatement;
	}
}
