package es.uniovi.reflection.reflectator;

import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.processing.*;

import java.util.ArrayList;
import java.util.List;

public class ConstructorDeclaration extends AbstractProcessor<CtConstructor<?>> {
	public void process(CtConstructor<?> element) {
        if (Main.skip(element.getParent(CtType.class).getQualifiedName().toString())) return;

		if(element.getBody() == null) return;


		List<CtStatement> currentBody = element.getBody().getStatements();
		List<CtStatement> tryBody = new ArrayList<>();
		List<CtStatement> newBody = new ArrayList<>();

		int i = 0;
		while(i < currentBody.size())
		{
			if(!(currentBody.get(i) instanceof CtInvocation))
				tryBody.add(currentBody.get(i));
			else if(currentBody.get(i) instanceof CtInvocation && ((CtInvocation)currentBody.get(i)).getTarget() != null)
				tryBody.add(currentBody.get(i));
			else
				newBody.add(currentBody.get(i));
			i++;
		}


		CtTry tryStatement = Helper.createTry(tryBody);
		element.getBody().getStatements().clear();
		for(CtStatement st : newBody)
			element.getBody().insertEnd(st);
		element.getBody().insertEnd(tryStatement);
	}
}
