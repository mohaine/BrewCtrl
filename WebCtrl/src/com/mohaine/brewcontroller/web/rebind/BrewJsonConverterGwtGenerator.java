/*
 Copyright 2009-2013 Michael Graessle

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.

 */

package com.mohaine.brewcontroller.web.rebind;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;
import com.mohaine.brewcontroller.BrewJsonConverterRefection;
import com.mohaine.brewcontroller.shared.json.JsonObjectHandler;
import com.mohaine.brewcontroller.shared.json.JsonObjectPropertyHandler;
import com.mohaine.brewcontroller.shared.json.ListType;
import com.mohaine.brewcontroller.web.client.BrewJsonConverterGwt;

public class BrewJsonConverterGwtGenerator extends Generator {

	private Map<String, String> primativeToNon = new HashMap<String, String>();

	private String className = null;

	private String packageName = null;

	@Override
	public String generate(TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {
		primativeToNon.put("boolean", "Boolean");
		primativeToNon.put("int", "Integer");
		primativeToNon.put("double", "Double");
		primativeToNon.put("float", "Float");
		TypeOracle typeOracle = context.getTypeOracle();

		try {

			// get classType and save instance variables
			JClassType classType = typeOracle.getType(typeName);
			packageName = classType.getPackage().getName();
			className = classType.getSimpleSourceName() + "Wrapper";
			generateClass(logger, context);

		} catch (Exception e) {

			// record to logger that Map generation threw an exception
			logger.log(TreeLogger.ERROR, "PropertyMap ERROR!!!", e);
			e.printStackTrace();

		}

		// return the fully qualifed name of the class generated
		String fullClassName = packageName + "." + className;

		System.out.println("Generated " + fullClassName);

		return fullClassName;
	}

	private void generateClass(TreeLogger logger, GeneratorContext context) throws Exception {
		PrintWriter printWriter = null;
		printWriter = context.tryCreate(logger, packageName, className);
		// print writer if null, source code has ALREADY been generated,return
		if (printWriter == null)
			return;

		ClassSourceFileComposerFactory composer = null;
		composer = new ClassSourceFileComposerFactory(packageName, className);
		BrewJsonConverterRefection bjcr = new BrewJsonConverterRefection();
		List<Class<?>> classesToSupport = bjcr.getClassesToSupport();

		composer.addImport(BrewJsonConverterGwt.class.getName());
		composer.addImport(List.class.getName());
		composer.addImport(ArrayList.class.getName());
		composer.addImport(JsonObjectHandler.class.getName());
		composer.addImport(JsonObjectPropertyHandler.class.getName());
		for (Class<?> jsonClass : classesToSupport) {
			composer.addImport(jsonClass.getName());
		}
		composer.setSuperclass(BrewJsonConverterGwt.class.getName());

		SourceWriter sourceWriter = null;

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PrintWriter subPrintWriter = new PrintWriter(out);

		sourceWriter = composer.createSourceWriter(context, subPrintWriter);

		for (Class<?> jsonClass : classesToSupport) {

			sourceWriter.println("{");
			sourceWriter.indent();

			String simpleName = jsonClass.getSimpleName();
			sourceWriter.println(String.format("JsonObjectHandler<%s> jsonObjectHandler = new JsonObjectHandler<%s>() {", simpleName, simpleName));
			sourceWriter.indent();
			sourceWriter.println("@Override");
			sourceWriter.println("public String getType() {");
			sourceWriter.indent();
			sourceWriter.println(String.format("return \"%s\";", simpleName));
			sourceWriter.outdent();
			sourceWriter.println("}");

			sourceWriter.println("@Override");
			sourceWriter.println("public boolean handlesType(Class<?> value) {");
			sourceWriter.indent();
			sourceWriter.println(String.format("return %s.class.equals(value);", simpleName));
			sourceWriter.outdent();
			sourceWriter.println("}");

			sourceWriter.println("@Override");
			sourceWriter.println(String.format("public List<JsonObjectPropertyHandler<%s, ?>> getPropertyHandlers() {", simpleName));
			sourceWriter.indent();
			sourceWriter.println(String.format("ArrayList<JsonObjectPropertyHandler<%s, ?>> handlers = new ArrayList<JsonObjectPropertyHandler<%s, ?>>();", simpleName, simpleName));

			addFields(sourceWriter, simpleName, jsonClass);

			sourceWriter.println("return handlers;");
			sourceWriter.outdent();
			sourceWriter.println("}");

			sourceWriter.println("@Override");
			sourceWriter.println(String.format("public %s createNewObject() throws Exception {", simpleName));
			sourceWriter.indent();
			sourceWriter.println(String.format("return new %s();", simpleName));
			sourceWriter.outdent();

			sourceWriter.println("}");
			sourceWriter.println("};");
			sourceWriter.println("jc.addHandler(jsonObjectHandler);");
			sourceWriter.outdent();
			sourceWriter.println("}");

		}

		sourceWriter.outdent();
		sourceWriter.println("}");

		subPrintWriter.flush();
		byte[] bytes = out.toByteArray();
		String classString = new String(bytes);

		// File file = new File(System.getProperty("user.home") + "/Desktop",
		// className + ".java");
		// System.out.println("file: " + file.getAbsolutePath());
		// FileUtils.writeToFile(classString, file);

		printWriter.write(classString);
		// commit generated class
		context.commit(logger, printWriter);
	}

	private void addFields(SourceWriter sourceWriter, String simpleName, Class<?> objClass) throws Exception {
		Class<?> superClass = objClass.getSuperclass();

		if (superClass != null) {
			addFields(sourceWriter, simpleName, superClass);
		}

		Field[] fields = objClass.getDeclaredFields();
		for (Field field : fields) {
			if (!field.isAccessible()) {
				field.setAccessible(true);
			}
			if (Modifier.isStatic(field.getModifiers())) {
				continue;
			}
			if (Modifier.isFinal(field.getModifiers())) {
				continue;
			}
			if (Modifier.isTransient(field.getModifiers())) {
				continue;
			}

			addField(sourceWriter, simpleName, objClass, field);
		}
	}

	private void addField(SourceWriter sourceWriter, String simpleName, Class<?> objClass, Field field) {
		String fieldType = field.getType().getSimpleName();
		String fieldName = field.getName();
		String fieldNameOnSetGet = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);

		if (primativeToNon.containsKey(fieldType)) {
			fieldType = primativeToNon.get(fieldType);
		}
		sourceWriter.println(String.format("handlers.add(new JsonObjectPropertyHandler<%s, %s>() {", simpleName, fieldType));
		sourceWriter.indent();

		sourceWriter.println("@Override");
		sourceWriter.println("public String getName() {");
		sourceWriter.indent();
		sourceWriter.println(String.format("return \"%s\";", fieldName));
		sourceWriter.outdent();
		sourceWriter.println("}");

		Method method = null;
		try {
			method = objClass.getMethod("get" + fieldNameOnSetGet, new Class[0]);
		} catch (NoSuchMethodException e) {
			try {
				method = objClass.getMethod("is" + fieldNameOnSetGet, new Class[0]);
			} catch (NoSuchMethodException e2) {
				throw new RuntimeException("unable to find getter for " + fieldNameOnSetGet + " on class " + objClass.getClass());
			}
		}

		sourceWriter.println("@Override");
		sourceWriter.println(String.format("public %s getValue(%s object) {", fieldType, simpleName));
		sourceWriter.indent();
		sourceWriter.println(String.format("return object.%s();", method.getName()));
		sourceWriter.outdent();
		sourceWriter.println("}");

		sourceWriter.println("@Override");
		sourceWriter.println(String.format("public void setValue(%s object, %s value) {", simpleName, fieldType));
		sourceWriter.indent();
		// sourceWriter.println(String.format("System.out.println( \"%s.%s Set Value \" + value );",
		// simpleName, fieldName));
		sourceWriter.println(String.format("object.set%s(convertValue(value, %s.class));", fieldNameOnSetGet, field.getType().getSimpleName()));
		sourceWriter.outdent();
		sourceWriter.println("}");
		String expectedType = fieldType;

		ListType annotation = field.getAnnotation(ListType.class);
		if (annotation != null) {
			Class<?>[] value = annotation.value();
			if (value != null && value.length == 1) {
				Class<?> class1 = value[0];
				expectedType = class1.getSimpleName();
			}
		}

		sourceWriter.println("@Override");
		sourceWriter.println("public Class<?> getExpectedType() {");
		sourceWriter.indent();
		sourceWriter.println(String.format("return  %s.class;", expectedType));
		sourceWriter.outdent();
		sourceWriter.println("}");
		sourceWriter.outdent();
		sourceWriter.println("});");
	}
}
