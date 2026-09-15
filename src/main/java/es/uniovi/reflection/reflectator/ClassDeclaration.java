package es.uniovi.reflection.reflectator;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtTypeReference;

public class ClassDeclaration extends AbstractProcessor<CtClass<?>> {
    public static int COUNT = 0;
    public static boolean LOG = false;

	public void process(CtClass<?> element) {
        if (Main.skip(element.getQualifiedName().toString())) return;

        if(LOG)	System.out.printf("> Processing class: %s.\n", element.getQualifiedName().toString());
        ClassDeclaration.COUNT++;

        //Helper methods
        element.addMethod(createSetInstanceFieldMethod());
        element.addMethod(createSetInstanceDeclaredFieldMethod());
        element.addMethod(createSetClassDeclaredFieldMethod());
        element.addMethod(createSetSuperFieldMethod());

        element.addMethod(createSetArrayMethod());

        element.addMethod(createUnaryInstanceFieldOperationMethod());
        element.addMethod(createUnaryInstanceDeclaredFieldOperationMethod());
        element.addMethod(createUnaryClassDeclaredFieldOperationMethod());
        element.addMethod(createUnarySuperFieldOperationMethod());

	}


	private CtMethod<?> createdMethod(String simpleName, List<Entry<String,Class<?>>> parameters, String code){
		CtMethod<Object> method = getFactory().Core().createMethod();		
		method.addModifier(ModifierKind.PRIVATE);
		method.addModifier(ModifierKind.STATIC);
		method.setSimpleName(simpleName);	

		List<CtParameter<?>> params = new ArrayList<CtParameter<?>>();

		for (Entry<String,Class<?>> entry : parameters) {			
			if(entry.getValue().equals(Class.class))
			{
				CtParameter<Class<?>> classParameter = getFactory().Core().createParameter();
				CtTypeReference<Class<?>> classTypeRef = getFactory().Code().createCtTypeReference(Class.class);		
				classTypeRef.addActualTypeArgument(getFactory().Type().createTypeParameterReference("?"));
				classParameter.setType(classTypeRef);				
				classParameter.setSimpleName(entry.getKey());
				params.add(classParameter);
			}
			else{
				CtParameter<?> parameter = getFactory().Core().createParameter();
				parameter.setType(getFactory().Code().createCtTypeReference(entry.getValue()));
				parameter.setSimpleName(entry.getKey());
				params.add(parameter);
			}			
		}
		
		method.setParameters(params);
		
		CtCodeSnippetStatement snippet = getFactory().Code().createCodeSnippetStatement(code);		
		CtBlock<?> body = getFactory().Core().createBlock();
		body.addStatement(snippet);
		method.setBody(body);

		return method;
	}

	private CtMethod<?> createSetInstanceFieldMethod(){
		String simpleName = "Object setInstanceField";	
		List<Entry<String,Class<?>>> parameters = new ArrayList<Entry<String,Class<?>>>();
		parameters.add(new AbstractMap.SimpleEntry<String,Class<?>>("obj", Object.class));
		parameters.add(new AbstractMap.SimpleEntry<String,Class<?>>("fieldName", String.class));
		parameters.add(new AbstractMap.SimpleEntry<String,Class<?>>("value", Object.class));
		String code = "obj.getClass().getField(fieldName).set(obj, value);return value";		
		return createdMethod(simpleName, parameters, code);
	}

	private CtMethod<?> createSetInstanceDeclaredFieldMethod(){
		String simpleName = "Object setInstanceDeclaredField";	
		List<Entry<String,Class<?>>> parameters = new ArrayList<Entry<String,Class<?>>>();
		parameters.add(new AbstractMap.SimpleEntry<String,Class<?>>("obj", Object.class));
		parameters.add(new AbstractMap.SimpleEntry<String,Class<?>>("fieldName", String.class));
		parameters.add(new AbstractMap.SimpleEntry<String,Class<?>>("value", Object.class));
		String code = "obj.getClass().getDeclaredField(fieldName).set(obj, value);return value";				
		return createdMethod(simpleName, parameters, code);
	}

	private CtMethod<?> createSetClassDeclaredFieldMethod(){
		String simpleName = "Object setClassDeclaredField";	
		List<Entry<String,Class<?>>> parameters = new ArrayList<Entry<String,Class<?>>>();
		parameters.add(new AbstractMap.SimpleEntry<String,Class<?>>("klass", Class.class));
		parameters.add(new AbstractMap.SimpleEntry<String,Class<?>>("fieldName", String.class));
		parameters.add(new AbstractMap.SimpleEntry<String,Class<?>>("value", Object.class));
		String code = "klass.getDeclaredField(fieldName).set(klass, value);return value";		
		return createdMethod(simpleName, parameters, code);
	}

	private CtMethod<?> createSetSuperFieldMethod(){
		String simpleName = "Object setSuperField";	
		List<Entry<String,Class<?>>> parameters = new ArrayList<Entry<String,Class<?>>>();
		parameters.add(new AbstractMap.SimpleEntry<String,Class<?>>("superClass", Class.class));
		parameters.add(new AbstractMap.SimpleEntry<String,Class<?>>("obj", Object.class));
		parameters.add(new AbstractMap.SimpleEntry<String,Class<?>>("fieldName", String.class));
		parameters.add(new AbstractMap.SimpleEntry<String,Class<?>>("fieldClass", Class.class));
		parameters.add(new AbstractMap.SimpleEntry<String,Class<?>>("value", Object.class));
		String code = "java.lang.invoke.MethodHandles.lookup().findSetter(superClass, fieldName, fieldClass).invoke(obj,value);return value";	
		return createdMethod(simpleName, parameters, code);
	}

	private CtMethod<?> createSetArrayMethod(){
		String simpleName = "Object setArray";	
		List<Entry<String,Class<?>>> parameters = new ArrayList<Entry<String,Class<?>>>();
		parameters.add(new AbstractMap.SimpleEntry<String,Class<?>>("array", Object.class));
		parameters.add(new AbstractMap.SimpleEntry<String,Class<?>>("index", int.class));
		parameters.add(new AbstractMap.SimpleEntry<String,Class<?>>("value", Object.class));
		String code = "java.lang.reflect.Array.set(array,index,value);return value";		
		return createdMethod(simpleName, parameters, code);
	}

	private CtMethod<?> createUnaryInstanceFieldOperationMethod(){
		String simpleName = "Object unaryInstanceFieldOperation";	
		List<Entry<String,Class<?>>> parameters = new ArrayList<Entry<String,Class<?>>>();
		parameters.add(new AbstractMap.SimpleEntry<String,Class<?>>("obj", Object.class));
		parameters.add(new AbstractMap.SimpleEntry<String,Class<?>>("fieldName", String.class));
		parameters.add(new AbstractMap.SimpleEntry<String,Class<?>>("operation", String.class));
		String code = "" +
		"Object value = obj.getClass().getField(fieldName).get(obj);" +
		"switch(operation){" +
			"case \"PREINC\":{return setInstanceField(obj, fieldName,introspector.IntrospectorHelper.increment((Number)value));}" +
			"case \"POSTINC\":{setInstanceField(obj, fieldName,introspector.IntrospectorHelper.increment((Number)value));return value;}" +
			"case \"PREDEC\":{return setInstanceField(obj, fieldName,introspector.IntrospectorHelper.decrement((Number)value));} " +
			"case \"POSTDEC\":{setInstanceField(obj, fieldName,introspector.IntrospectorHelper.decrement((Number)value));return value;}" + 
			"case \"POS\":{return introspector.IntrospectorHelper.pos((Number)value);}" +
			"case \"NEG\":{return introspector.IntrospectorHelper.neg((Number)value);}" +		
			"case \"NOT\":{return  !((boolean)value);} " +			
		"}return value";		
		return createdMethod(simpleName, parameters, code);
	}


	private CtMethod<?> createUnaryInstanceDeclaredFieldOperationMethod(){
		String simpleName = "Object unaryInstanceDeclaredFieldOperation";	
		List<Entry<String,Class<?>>> parameters = new ArrayList<Entry<String,Class<?>>>();
		parameters.add(new AbstractMap.SimpleEntry<String,Class<?>>("obj", Object.class));
		parameters.add(new AbstractMap.SimpleEntry<String,Class<?>>("fieldName", String.class));
		parameters.add(new AbstractMap.SimpleEntry<String,Class<?>>("operation", String.class));
		String code = "" +
		"Object value = obj.getClass().getDeclaredField(fieldName).get(obj);" +
		"switch(operation){" +
			"case \"PREINC\":{return setInstanceDeclaredField(obj, fieldName,introspector.IntrospectorHelper.increment((Number)value));}" +
			"case \"POSTINC\":{setInstanceDeclaredField(obj, fieldName,introspector.IntrospectorHelper.increment((Number)value));return value;}" +
			"case \"PREDEC\":{return setInstanceDeclaredField(obj, fieldName,introspector.IntrospectorHelper.decrement((Number)value));} " +
			"case \"POSTDEC\":{setInstanceDeclaredField(obj, fieldName,introspector.IntrospectorHelper.decrement((Number)value));return value;}" + 
			"case \"POS\":{return introspector.IntrospectorHelper.pos((Number)value);}" +
			"case \"NEG\":{return introspector.IntrospectorHelper.neg((Number)value);}" +		
			"case \"NOT\":{return  !((boolean)value);} " +			
		"}return value";		
		return createdMethod(simpleName, parameters, code);
	}


	private CtMethod<?> createUnaryClassDeclaredFieldOperationMethod(){
		String simpleName = "Object unaryClassDeclaredFieldOperation";	
		List<Entry<String,Class<?>>> parameters = new ArrayList<Entry<String,Class<?>>>();
		parameters.add(new AbstractMap.SimpleEntry<String,Class<?>>("klass", Class.class));
		parameters.add(new AbstractMap.SimpleEntry<String,Class<?>>("fieldName", String.class));
		parameters.add(new AbstractMap.SimpleEntry<String,Class<?>>("operation", String.class));
		String code = "" +
		"Object value = klass.getDeclaredField(fieldName).get(klass);" +
		"switch(operation){" +
			"case \"PREINC\":{return setClassDeclaredField(klass, fieldName,introspector.IntrospectorHelper.increment((Number)value));}" +
			"case \"POSTINC\":{setClassDeclaredField(klass, fieldName,introspector.IntrospectorHelper.increment((Number)value));return value;}" +
			"case \"PREDEC\":{return setClassDeclaredField(klass, fieldName,introspector.IntrospectorHelper.decrement((Number)value));} " +
			"case \"POSTDEC\":{setClassDeclaredField(klass, fieldName,introspector.IntrospectorHelper.decrement((Number)value));return value;}" + 
			"case \"POS\":{return introspector.IntrospectorHelper.pos((Number)value);}" +
			"case \"NEG\":{return introspector.IntrospectorHelper.neg((Number)value);}" +		
			"case \"NOT\":{return  !((boolean)value);} " +			
		"}return value";		
		return createdMethod(simpleName, parameters, code);
	}

	private CtMethod<?> createUnarySuperFieldOperationMethod(){
		String simpleName = "Object unarySuperFieldOperation";	
		List<Entry<String,Class<?>>> parameters = new ArrayList<Entry<String,Class<?>>>();
		parameters.add(new AbstractMap.SimpleEntry<String,Class<?>>("superClass", Class.class));
		parameters.add(new AbstractMap.SimpleEntry<String,Class<?>>("obj", Object.class));
		parameters.add(new AbstractMap.SimpleEntry<String,Class<?>>("fieldName", String.class));
		parameters.add(new AbstractMap.SimpleEntry<String,Class<?>>("fieldClass", Class.class));
		parameters.add(new AbstractMap.SimpleEntry<String,Class<?>>("operation", String.class));
		String code = "" +
		"Object value = java.lang.invoke.MethodHandles.lookup().findGetter(superClass, fieldName, fieldClass).invoke(obj);" +
		"switch(operation){" +
			"case \"PREINC\":{return setSuperField(superClass,obj,fieldName,fieldClass,introspector.IntrospectorHelper.increment((Number)value));}" +
			"case \"POSTINC\":{setSuperField(superClass,obj,fieldName,fieldClass,introspector.IntrospectorHelper.increment((Number)value));return value;}" +
			"case \"PREDEC\":{return setSuperField(superClass,obj,fieldName,fieldClass,introspector.IntrospectorHelper.decrement((Number)value));} " +
			"case \"POSTDEC\":{setSuperField(superClass,obj,fieldName,fieldClass,introspector.IntrospectorHelper.decrement((Number)value));return value;}" + 
			"case \"POS\":{return introspector.IntrospectorHelper.pos((Number)value);}" +
			"case \"NEG\":{return introspector.IntrospectorHelper.neg((Number)value);}" +		
			"case \"NOT\":{return  !((boolean)value);} " +			
		"}return value";		
		return createdMethod(simpleName, parameters, code);
	}
}
