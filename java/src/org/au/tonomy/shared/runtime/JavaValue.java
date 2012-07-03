package org.au.tonomy.shared.runtime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.au.tonomy.shared.util.Exceptions;
/**
 * A wrapper around a java object that invokes methods using reflection.
 */
public class JavaValue implements IValue {

  private final Object value;

  public JavaValue(Object value) {
    this.value = value;
  }

  @Override
  public IValue invoke(String name, List<IValue> argList, IScope scope) {
    if (name.startsWith("."))
      name = name.substring(1);
    Class<?>[] types = new Class<?>[argList.size() + 1];
    Object[] args = new Object[argList.size() + 1];
    types[0] = IScope.class;
    args[0] = scope;
    for (int i = 0; i < argList.size(); i++) {
      types[i + 1] = IValue.class;
      args[i + 1] = argList.get(i);
    }
    try {
      Method method = value.getClass().getMethod(name, types);
      return (IValue) method.invoke(value, args);
    } catch (NoSuchMethodException nsme) {
      throw Exceptions.propagate(nsme);
    } catch (InvocationTargetException ite) {
      throw Exceptions.propagate(ite);
    } catch (IllegalAccessException iae) {
      throw Exceptions.propagate(iae);
    }
  }

}
