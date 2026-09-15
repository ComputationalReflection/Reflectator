package es.uniovi.reflection.reflectator;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;

public class MethodDeclaration extends AbstractProcessor<CtMethod<?>> {
	public void process(CtMethod<?> element) {
        if (Main.skip(element.getParent(CtType.class).getQualifiedName().toString())) return;

		if(element.getBody() == null) return;

		CtTry tryStatement = Helper.createTry(element.getBody().getStatements());
		element.getBody().getStatements().clear();
		element.getBody().insertBegin(tryStatement);
	}
}
