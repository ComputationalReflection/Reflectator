package introspector;

import java.util.ArrayList;
import java.util.List;

public class IntrospectorHelper {
    public static long CAST_COUNTER;
    public static long FIELD_READ_COUNTER;
    public static long FIELD_WRITE_COUNTER;
    public static long ARRAY_READ_COUNTER;
    public static long ARRAY_WRITE_COUNTER;
    public static long NEW_ARRAY_COUNTER;
    public static long METHOD_INVOCATION_COUNTER;
    public static long CONSTRUCTOR_INVOCATION_COUNTER;

    public static void showCounters(){
        List<String> lines = new ArrayList<>();
        lines.add("ArrayRead: " + IntrospectorHelper.ARRAY_READ_COUNTER);
        lines.add("\nArrayWrite: " + IntrospectorHelper.ARRAY_WRITE_COUNTER);
        lines.add("\nCast: " + IntrospectorHelper.CAST_COUNTER);
        lines.add("\nConstructorInvocation: " + IntrospectorHelper.CONSTRUCTOR_INVOCATION_COUNTER);
        lines.add("\nFieldRead: " + IntrospectorHelper.FIELD_READ_COUNTER);
        lines.add("\nFieldWrite: " + IntrospectorHelper.FIELD_WRITE_COUNTER);
        lines.add("\nMethodInvocation: " + IntrospectorHelper.METHOD_INVOCATION_COUNTER);
        lines.add("\nNewArrayInvocation: " + IntrospectorHelper.NEW_ARRAY_COUNTER);
        for(String line:lines)
            System.out.print(line);
    }

	public static Number increment(Number number) {
		if(number instanceof Double ) {
			return number.doubleValue() + 1;
		} else if(number instanceof Float) {
			return number.floatValue() + 1;
		} else if(number instanceof Long ) {
			return number.longValue() + 1;
		} else {
			return number.intValue() + 1;
		}
	}
	public static Number decrement(Number number) {
		if(number instanceof Double ) {
			return number.doubleValue() - 1;
		} else if(number instanceof Float) {
			return number.floatValue() - 1;
		} else if(number instanceof Long ) {
			return number.longValue() - 1;
		} else {
			return number.intValue() - 1;
		}
	}
	public static Number pos(Number number) {
		if(number instanceof Double ) {
			return 0 + number.doubleValue();
		} else if(number instanceof Float) {
			return 0 + number.floatValue();
		} else if(number instanceof Long ) {
			return 0 + number.longValue();
		} else {
			return 0 + number.intValue();
		}
	}	
	public static Number neg(Number number) {
		if(number instanceof Double ) {
			return 0 - number.doubleValue();
		} else if(number instanceof Float) {
			return 0 - number.floatValue();
		} else if(number instanceof Long ) {
			return 0 - number.longValue();
		} else {
			return 0 - number.intValue();
		}
	}		
}
	