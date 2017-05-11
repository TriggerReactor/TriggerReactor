package io.github.wysohn.triggerreactor.manager.wrapper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

public abstract class Wrapper<T> {
    Wrapper(T target){

    }

    public Map<String, Object> toMap(){
        return extractMap(this);
    }

    @SuppressWarnings("rawtypes")
    private static Map<String, Object> extractMap(Object obj){
        Map<String, Object> map = new HashMap<String, Object>();

        Class<?> clazz = obj.getClass();
        for(Field field : getAllFields(new ArrayList<Field>(), clazz)){
            if((field.getModifiers() & Modifier.STATIC) == Modifier.STATIC)
                continue;

            field.setAccessible(true);
            if(Wrapper.class.isAssignableFrom(field.getType())){
                try {
                    Wrapper value = (Wrapper) field.get(obj);
                    if(value != null)
                        map.put(field.getName(), ((Wrapper) field.get(obj)).toMap());
                    else
                        map.put(field.getName(), "null");
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    ex.printStackTrace();
                }
            }else{
                try {
                    Object value = field.get(obj);
                    if(value == null){
                        map.put(field.getName(), "null");
                    }else{
                        map.put(field.getName(), value);
                    }
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    ex.printStackTrace();
                }
            }
        }

        return map;
    }

    /**
     * http://stackoverflow.com/questions/1042798/retrieving-the-inherited-attribute-names-values-using-java-reflection
     * @param fields
     * @param c
     * @return
     */
    private static List<Field> getAllFields(List<Field> fields, Class<?> c){
        fields.addAll(Arrays.asList(c.getDeclaredFields()));

        if(c.getSuperclass() != null){
            fields = getAllFields(fields, c.getSuperclass());
        }

        return fields;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Map<String, Object> wrapperToVariablesMap(Wrapper wrapper){
        Map<String, Object> vars = new HashMap<String, Object>();
        parseMap(new Stack<String>(), vars, wrapper.toMap());
        return vars;
    }

    @SuppressWarnings("unchecked")
    private static void parseMap(Stack<String> names, Map<String, Object> vars, Map<String, Object> wrapperMap){
        for(Entry<String, Object> entry : wrapperMap.entrySet()){
            String key = entry.getKey();
            Object value = entry.getValue();

            if(value instanceof Map){
                names.push(key);
                parseMap(names, vars, (Map<String, Object>) value);
                names.pop();
            }else{
                StringBuilder builder = new StringBuilder();
                for(int i = names.size() - 1; i >= 0; i--){
                    builder.append(names.get(i)+".");
                }
                vars.put(builder.toString()+key, value);
            }
        }
    }
}
