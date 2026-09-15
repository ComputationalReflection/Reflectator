package es.uniovi.reflection.reflectator;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtTry;
import spoon.reflect.declaration.CtAnonymousExecutable;
import spoon.reflect.declaration.CtType;

public class Block extends AbstractProcessor<CtBlock<?>> {
	public void process(CtBlock<?> element) {
        if (Main.skip(element.getParent(CtType.class).getQualifiedName().toString())) return;

		if(element.getParent() instanceof CtAnonymousExecutable)
		{			
			CtTry tryStatement = Helper.createTry(element.getStatements());
			element.getStatements().clear();
			element.insertEnd(tryStatement);
		}
	}
}
