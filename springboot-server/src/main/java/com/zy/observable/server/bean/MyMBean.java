package com.zy.observable.server.bean;

import com.zy.observable.server.ibean.MyMBeanProxy;

import javax.management.*;
import java.lang.management.*;

public class MyMBean implements DynamicMBean, MyMBeanProxy {
    private int myField;

    public MyMBean(int myField) {
        this.myField = myField;
    }

    public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
        if (attribute.equals("myField")) {
            return myField;
        } else {
            throw new AttributeNotFoundException(attribute);
        }
    }

    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        if (attribute.getName().equals("myField")) {
            myField = (int) attribute.getValue();
        } else {
            throw new AttributeNotFoundException(attribute.getName());
        }
    }

    public AttributeList getAttributes(String[] attributes) {
        AttributeList list = new AttributeList();
        for (String attribute : attributes) {
            try {
                Object value = getAttribute(attribute);
                list.add(new Attribute(attribute, value));
            } catch (Exception e) {
                // Ignore unknown attributes
            }
        }
        return list;
    }

    public AttributeList setAttributes(AttributeList attributes) {
        AttributeList list = new AttributeList();
        for (Attribute attribute : attributes.asList()) {
            try {
                setAttribute(attribute);
                list.add(attribute);
            } catch (Exception e) {
                // Ignore unknown attributes or invalid values
            }
        }
        return list;
    }

    public MBeanInfo getMBeanInfo() {
        MBeanAttributeInfo[] attrs = {
                new MBeanAttributeInfo("myField", "int", "My field", true, true, false)
        };
        MBeanOperationInfo[] ops = {
                new MBeanOperationInfo("printHello", "Prints the string 'Hello, world!'", null, "void", MBeanOperationInfo.ACTION)
        };
        return new MBeanInfo(getClass().getName(), "My MBean", attrs, null, ops, null);
    }

    public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
        if (actionName.equals("printHello")) {
            System.out.println("Hello, world!");
            return null;
        } else {
            throw new ReflectionException(new NoSuchMethodException(actionName), "Cannot find operation " + actionName);
        }
    }

    public int getmyField() {
        return myField;
    }

    public void setmyField(int i) {
        myField = i;
    }
}

