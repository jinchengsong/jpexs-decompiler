/*
 *  Copyright (C) 2010-2014 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui.generictageditors;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class ReflectionTools {

    public static Object getValue(Object obj, Field field, int index) throws IllegalArgumentException, IllegalAccessException {
        Object value = field.get(obj);
        if (List.class.isAssignableFrom(field.getType())) {
            return ((List) value).get(index);
        }

        if (field.getType().isArray()) {
            return Array.get(value, index);
        }

        return value;
    }

    public static boolean needsIndex(Field field) {
        if (List.class.isAssignableFrom(field.getType())) {
            return true;
        } else if (field.getType().isArray()) {
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public static void setValue(Object obj, Field field, int index, Object newValue) throws IllegalArgumentException, IllegalAccessException {
        Object value = field.get(obj);
        if (List.class.isAssignableFrom(field.getType())) {
            ((List) value).set(index, newValue);
        } else if (field.getType().isArray()) {
            Array.set(value, index, newValue);
        } else {
            field.set(obj, newValue);
        }
    }

    public static Object newInstanceOf(Class cls) throws InstantiationException, IllegalAccessException {        
        if (cls == Integer.class || cls == int.class) {
            return new Integer(0);
        } else if (cls == Float.class || cls == float.class) {
            return new Float(0.0f);
        } else if (cls == Double.class || cls == double.class ) {
            return new Double(0);
        } else if(cls == Long.class || cls == long.class){
            return new Long(0L);
        }
        return cls.newInstance();
    }

    @SuppressWarnings("unchecked")
    public static boolean enlargeList(Object object, Field field) {
        if (!List.class.isAssignableFrom(field.getType())) {
            return false;
        }
        List list;
        try {
            list = (List) field.get(object);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(ReflectionTools.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        ParameterizedType listType = (ParameterizedType) field.getGenericType();
        Class<?> parameterClass = (Class<?>) listType.getActualTypeArguments()[0];
        try {
            Object val = newInstanceOf(parameterClass);
            list.add(val);
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(ReflectionTools.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    public static boolean enlargeArray(Object object, Field field, boolean notnull) {
        if (!field.getType().isArray()) {
            return false;
        }
        Object arrValue;
        try {
            arrValue = field.get(object);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(ReflectionTools.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        Class componentClass = arrValue.getClass().getComponentType();
        int originalSize = Array.getLength(arrValue);
        Object copy = Array.newInstance(componentClass, originalSize + 1);
        for (int i = 0; i < originalSize; i++) {
            Array.set(copy, i, Array.get(arrValue, i));
        }
        if (!componentClass.isPrimitive() && notnull) {
            try {
                Array.set(copy, originalSize, componentClass.newInstance());
            } catch (InstantiationException | IllegalAccessException ex) {
                Logger.getLogger(ReflectionTools.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
        try {
            field.set(object, copy);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(ReflectionTools.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    public static boolean enlargeField(Object object, Field field, boolean notnull) {
        if (List.class.isAssignableFrom(field.getType())) {
            return enlargeList(object, field);
        }

        if (field.getType().isArray()) {
            return enlargeArray(object, field, notnull);
        }
        return false;
    }
    
    public static boolean removeFromField(Object object, Field field,int index){
        if (List.class.isAssignableFrom(field.getType())) {
            return removeFromList(object, field,index);
        }
        
        if (field.getType().isArray()) {
            return removeFromArray(object, field, index);
        }
        return false;
    }
    
    public static boolean removeFromList(Object object, Field field,int index){
        List list;
        try {        
            list=(List)field.get(object);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(ReflectionTools.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        if(index<0 || index>=list.size()){
            return false;
        }
        list.remove(index);        
        return true;
    }
    
    public static boolean removeFromArray(Object object, Field field, int index){
        Object arrValue;
        try {
            arrValue = field.get(object);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(ReflectionTools.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        Class componentClass = arrValue.getClass().getComponentType();
        int originalSize = Array.getLength(arrValue);
        Object copy = Array.newInstance(componentClass, originalSize - 1);
        int pos=0;
        //copy all before index
        for (int i = 0; i < index; i++) {
            Array.set(copy, pos, Array.get(arrValue, i));
            pos++;
        }
        //copy all after index
        for (int i = index+1; i <originalSize ; i++) {
            Array.set(copy, pos, Array.get(arrValue, i));
            pos++;
        }
        try {
            field.set(object, copy);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(ReflectionTools.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }
}
