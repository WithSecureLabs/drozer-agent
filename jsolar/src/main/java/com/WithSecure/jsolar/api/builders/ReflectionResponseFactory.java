package com.WithSecure.jsolar.api.builders;

import com.google.protobuf.MessageOrBuilder;
import com.WithSecure.jsolar.api.Protobuf;
import com.WithSecure.jsolar.api.Protobuf.Message.ReflectionResponse;
import com.WithSecure.jsolar.reflection.types.ReflectedType;

public class ReflectionResponseFactory {

    private ReflectionResponse.Builder builder = null;

    public static ReflectionResponseFactory data(byte[] bytes) {
        return new ReflectionResponseFactory(ReflectionResponse.ResponseStatus.SUCCESS).setData(bytes);
    }

    public static ReflectionResponseFactory error(String message) {
        return new ReflectionResponseFactory(ReflectionResponse.ResponseStatus.ERROR).setErrorMessage(message);
    }

    public static ReflectionResponseFactory nullPointer() {
        return new ReflectionResponseFactory(ReflectionResponse.ResponseStatus.SUCCESS).setResult(Protobuf.Message.Argument.ArgumentType.NULL, null);
    }

    public static ReflectionResponseFactory object(int ref) {
        return new ReflectionResponseFactory(ReflectionResponse.ResponseStatus.SUCCESS).setObjectReference(ref);
    }

    public static ReflectionResponseFactory objectArray(Object[] objects) {
        return new ReflectionResponseFactory(ReflectionResponse.ResponseStatus.SUCCESS).setObjects(objects);
    }

    public static ReflectionResponseFactory primitive(Object primitive) {
        if(primitive == null) {
            return nullPointer();
        } else {
            return new ReflectionResponseFactory(ReflectionResponse.ResponseStatus.SUCCESS).setPrimitive(primitive);
        }
    }

    public static ReflectionResponseFactory primitiveArray(Object primitives) {
        return new ReflectionResponseFactory(ReflectionResponse.ResponseStatus.SUCCESS).setPrimitives(primitives);
    }

    public static ReflectionResponseFactory send(Object value) {
        if (value == null)
            return nullPointer();
        else if (value.getClass().equals(String.class))
            return string((String) value);
        else if(value.getClass().isArray() && value instanceof String[])
            return primitiveArray((String[])value);
        else if(value.getClass().isArray() && value instanceof Object[])
            return objectArray((Object[])value);
        else if(value.getClass().isArray() && value.getClass().getComponentType() == Byte.TYPE)
            return data((byte[])value);
        else if(value.getClass().isArray())
            return primitiveArray(value);
        else
            return object(value.hashCode());
    }

    public static ReflectionResponseFactory string(String value) {
        return new ReflectionResponseFactory(ReflectionResponse.ResponseStatus.SUCCESS).setString(value);
    }

    public static ReflectionResponseFactory success() {
        return new ReflectionResponseFactory(ReflectionResponse.ResponseStatus.SUCCESS);
    }

    private ReflectionResponseFactory(ReflectionResponse.ResponseStatus status) {
        this.builder = ReflectionResponse.newBuilder().setStatus(status);
    }

    public ReflectionResponse build() {
        return this.builder.build();
    }

    private Protobuf.Message.Argument buildArgument(String string) {
        return Protobuf.Message.Argument.newBuilder().setType(Protobuf.Message.Argument.ArgumentType.STRING).setString(string).build();
    }

    /*Important the Message or Builder here is the GOOGLE protobuf MessageOrBuilder, not our own!
      Again I have no idea why this is, we are deep into reflection voodoo magic*/
    private Protobuf.Message.Argument buildArgument(Protobuf.Message.Argument.ArgumentType type, MessageOrBuilder builder) {

        Protobuf.Message.Argument.Builder argument = Protobuf.Message.Argument.newBuilder().setType(type);

        ;
        switch(type) {
            case ARRAY:
                argument.setArray((Protobuf.Message.Array.Builder)  builder);
                break;

            case OBJECT:
                argument.setObject((Protobuf.Message.ObjectReference.Builder)builder);
                break;

            case PRIMITIVE:
                argument.setPrimitive((Protobuf.Message.Primitive)builder);
                break;

            case DATA:
            case NULL:
            case STRING:
            default:
                break;
        }

        return argument.build();
    }

    private Protobuf.Message.Primitive buildPrimitive(Protobuf.Message.Primitive.PrimitiveType type, Object value) {
        Protobuf.Message.Primitive.Builder primitive = Protobuf.Message.Primitive.newBuilder().setType(type);

        switch(type) {
            case BOOL:
                primitive.setBool((Boolean)value);
                break;

            case BYTE:
                primitive.setByte((Byte)value);
                break;

            case DOUBLE:
                primitive.setDouble((Double)value);
                break;

            case FLOAT:
                primitive.setFloat((Float)value);
                break;

            case CHAR:
            case INT:
                primitive.setInt((Integer)value);
                break;

            case LONG:
                primitive.setLong((Long)value);
                break;

            case SHORT:
                primitive.setShort((Short)value);
                break;
        }

        return primitive.build();
    }

    public ReflectionResponseFactory isError() {
        this.builder.setStatus(ReflectionResponse.ResponseStatus.ERROR);

        return this;
    }

    public ReflectionResponseFactory isSuccess() {
        this.builder.setStatus(ReflectionResponse.ResponseStatus.SUCCESS);

        return this;
    }

    public ReflectionResponseFactory setData(byte[] bytes) {
        this.builder.setResult(ReflectedType.fromNative(bytes).getArgument());

        return this;
    }

    public ReflectionResponseFactory setErrorMessage(String error_message) {
        this.builder.setErrormessage(error_message);

        return this;
    }

    public ReflectionResponseFactory setObjectReference(int ref) {
        return this.setResult(Protobuf.Message.Argument.ArgumentType.OBJECT, Protobuf.Message.ObjectReference.newBuilder().setReference(ref));
    }

    public ReflectionResponseFactory setObjects(Object[] objects) {
        Protobuf.Message.Array.Builder array_builder = Protobuf.Message.Array.newBuilder().setType(Protobuf.Message.Array.ArrayType.OBJECT);

        for(Object object : objects)
            array_builder.addElement(buildArgument(Protobuf.Message.Argument.ArgumentType.OBJECT, Protobuf.Message.ObjectReference.newBuilder().setReference(object.hashCode())));

        return this.setResult(Protobuf.Message.Argument.ArgumentType.ARRAY, array_builder);
    }

    public ReflectionResponseFactory setPrimitive(Object primitive) {
        if(primitive instanceof Boolean)
            return this.setResult(Protobuf.Message.Argument.ArgumentType.PRIMITIVE, buildPrimitive(Protobuf.Message.Primitive.PrimitiveType.BOOL, primitive));
        else if(primitive instanceof Byte)
            return this.setResult(Protobuf.Message.Argument.ArgumentType.PRIMITIVE, buildPrimitive(Protobuf.Message.Primitive.PrimitiveType.BYTE, primitive));
        else if(primitive instanceof Character)
            return this.setResult(Protobuf.Message.Argument.ArgumentType.PRIMITIVE, buildPrimitive(Protobuf.Message.Primitive.PrimitiveType.CHAR, primitive));
        else if(primitive instanceof Double)
            return this.setResult(Protobuf.Message.Argument.ArgumentType.PRIMITIVE, buildPrimitive(Protobuf.Message.Primitive.PrimitiveType.DOUBLE, primitive));
        else if(primitive instanceof Float)
            return this.setResult(Protobuf.Message.Argument.ArgumentType.PRIMITIVE, buildPrimitive(Protobuf.Message.Primitive.PrimitiveType.FLOAT, primitive));
        else if(primitive instanceof Integer)
            return this.setResult(Protobuf.Message.Argument.ArgumentType.PRIMITIVE, buildPrimitive(Protobuf.Message.Primitive.PrimitiveType.INT, primitive));
        else if(primitive instanceof Long)
            return this.setResult(Protobuf.Message.Argument.ArgumentType.PRIMITIVE, buildPrimitive(Protobuf.Message.Primitive.PrimitiveType.LONG, primitive));
        else if(primitive instanceof Short)
            return this.setResult(Protobuf.Message.Argument.ArgumentType.PRIMITIVE, buildPrimitive(Protobuf.Message.Primitive.PrimitiveType.SHORT, primitive));
        else
            return null;
    }

    public ReflectionResponseFactory setPrimitives(Object primitiveArray) {
        Protobuf.Message.Array.Builder array_builder = Protobuf.Message.Array.newBuilder().setType(Protobuf.Message.Array.ArrayType.PRIMITIVE);

        if(primitiveArray instanceof boolean[]) {
            for(boolean b : (boolean[])primitiveArray)
                array_builder.addElement(buildArgument(Protobuf.Message.Argument.ArgumentType.PRIMITIVE, buildPrimitive(Protobuf.Message.Primitive.PrimitiveType.BOOL, b)));
        }
        else if(primitiveArray instanceof byte[]) {
            for(byte b : (byte[])primitiveArray)
                array_builder.addElement(buildArgument(Protobuf.Message.Argument.ArgumentType.PRIMITIVE, buildPrimitive(Protobuf.Message.Primitive.PrimitiveType.BYTE, b)));
        }
        else if(primitiveArray instanceof char[]) {
            for(char c : (char[])primitiveArray)
                array_builder.addElement(buildArgument(Protobuf.Message.Argument.ArgumentType.PRIMITIVE, buildPrimitive(Protobuf.Message.Primitive.PrimitiveType.CHAR, c)));
        }
        else if(primitiveArray instanceof double[]) {
            for(double d : (double[])primitiveArray)
                array_builder.addElement(buildArgument(Protobuf.Message.Argument.ArgumentType.PRIMITIVE, buildPrimitive(Protobuf.Message.Primitive.PrimitiveType.DOUBLE, d)));
        }
        else if(primitiveArray instanceof float[]) {
            for(float f : (float[])primitiveArray)
                array_builder.addElement(buildArgument(Protobuf.Message.Argument.ArgumentType.PRIMITIVE, buildPrimitive(Protobuf.Message.Primitive.PrimitiveType.FLOAT, f)));
        }
        else if(primitiveArray instanceof int[]) {
            for(int i : (int[])primitiveArray)
                array_builder.addElement(buildArgument(Protobuf.Message.Argument.ArgumentType.PRIMITIVE, buildPrimitive(Protobuf.Message.Primitive.PrimitiveType.INT, i)));
        }
        else if(primitiveArray instanceof long[]) {
            for(long l : (long[])primitiveArray)
                array_builder.addElement(buildArgument(Protobuf.Message.Argument.ArgumentType.PRIMITIVE, buildPrimitive(Protobuf.Message.Primitive.PrimitiveType.LONG, l)));
        }
        else if(primitiveArray instanceof short[]) {
            for(short s : (short[])primitiveArray)
                array_builder.addElement(buildArgument(Protobuf.Message.Argument.ArgumentType.PRIMITIVE, buildPrimitive(Protobuf.Message.Primitive.PrimitiveType.SHORT, s)));
        }
        else if(primitiveArray instanceof String[]) {
            for(String s : (String[])primitiveArray)
                array_builder.addElement(Protobuf.Message.Argument.newBuilder().setType(Protobuf.Message.Argument.ArgumentType.STRING).setString(s));
        }

        return this.setResult(Protobuf.Message.Argument.ArgumentType.ARRAY, array_builder);
    }

    public ReflectionResponseFactory setResult(Protobuf.Message.Argument.ArgumentType type, MessageOrBuilder builder) {
        this.builder.setResult(this.buildArgument(type, builder));

        return this;
    }

    public ReflectionResponseFactory setSessionId(String session_id) {
        this.builder.setSessionId(session_id);

        return this;
    }

    public ReflectionResponseFactory setString(String string) {
        this.builder.setResult(this.buildArgument(string));

        return this;
    }
}
