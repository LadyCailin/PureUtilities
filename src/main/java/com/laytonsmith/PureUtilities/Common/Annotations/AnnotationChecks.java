package com.laytonsmith.PureUtilities.Common.Annotations;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscoveryCache;
import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscoveryURLCache;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.ClassMirror;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.ExhaustiveVisitor;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is run by maven at compile time, and checks to ensure that the various annotations referenced here are
 * checked, and fail if any of the parameters are missing.
 */
public class AnnotationChecks {
	
	public static void main(String[] args) throws Exception {
		File outputDir = new File(args[0]);
		File scanDir = new File(args[1]);
		if(outputDir.toString().startsWith("-classpath") || outputDir.toString().startsWith("-Xdebug")) {
			// This happens when running locally. In that case, this certainly isn't a release, and
			// we can skip this process.
			StreamUtils.GetSystemOut().println("Skipping annotation caching, running locally.");
			return;
		}
		StreamUtils.GetSystemOut().println("-- Caching annotations --");
		StreamUtils.GetSystemOut().println("Scanning for classes in " + scanDir.getAbsolutePath());
		StreamUtils.GetSystemOut().println("Outputting file to directory " + outputDir.getAbsolutePath());
		long start = System.currentTimeMillis();
		URL cacheFile = new URL("file:" + scanDir.getCanonicalPath());
		ClassDiscoveryURLCache cache = new ClassDiscoveryURLCache(cacheFile);
		cache.writeDescriptor(new FileOutputStream(new File(outputDir, ClassDiscoveryCache.OUTPUT_FILENAME)));
		StreamUtils.GetSystemOut().println("Done writing " + ClassDiscoveryCache.OUTPUT_FILENAME + ", which took " + (System.currentTimeMillis() - start) + " ms.");
		ClassDiscovery.getDefaultInstance().addPreCache(cacheFile, cache);
		ClassDiscovery.getDefaultInstance().addDiscoveryLocation(cacheFile);
		AnnotationChecks.checkAll();
	}
	
	/**
	 * This method should generally be run at compile time, which performs all the supported checks. If this is not
	 * desirable for some reason, the other methods may instead be called one by one.
	 * @throws Exception 
	 */
	public static void checkAll() throws Exception {
		AnnotationChecks.checkForceImplementation();
		AnnotationChecks.verifyExhaustiveVisitors();
		AnnotationChecks.verifyNonInheritImplements();
	}

	@SuppressWarnings("UnnecessaryLabelOnBreakStatement")
	public static void checkForceImplementation() throws Exception {
		Set<String> uhohs = new HashSet<>();
		Set<Constructor<?>> set = ClassDiscovery.getDefaultInstance().loadConstructorsWithAnnotation(ForceImplementation.class);
		for(Constructor<?> cons : set) {
			Class superClass = cons.getDeclaringClass();
			Set<Class> s = ClassDiscovery.getDefaultInstance().loadClassesThatExtend(superClass);
			checkImplements:
			for(Class c : s) {
				// c is the class we want to check to make sure it implements cons
				for(Constructor cCons : c.getDeclaredConstructors()) {
					if(Arrays.equals(cons.getParameterTypes(), cCons.getParameterTypes())) {
						continue checkImplements;
					}
				}
				if(c.isMemberClass() && (c.getModifiers() & Modifier.STATIC) == 0) {
					// Ok, so, an inner, non static class actually passes the super class's reference to the constructor as
					// the first parameter, at a byte code level. So this is a different type of error, or at least, a different
					// error message will be helpful.
					uhohs.add(c.getName() + " must be static.");
				} else {
					uhohs.add(c.getName() + " must implement the constructor with signature (" + getSignature(cons) + "), but doesn't.");
				}
			}
		}

		Set<Method> set2 = ClassDiscovery.getDefaultInstance().loadMethodsWithAnnotation(ForceImplementation.class);
		for(Method cons : set2) {
			Class superClass = cons.getDeclaringClass();
			@SuppressWarnings("unchecked")
			Set<Class<?>> s = ClassDiscovery.getDefaultInstance().loadClassesThatExtend(superClass);
			checkImplements:
			for(Class<?> c : s) {
				// First, check if maybe it has a InterfaceRunner for it
				findRunner:
				for(Class<?> ir : ClassDiscovery.getDefaultInstance().loadClassesWithAnnotation(InterfaceRunnerFor.class)) {
					InterfaceRunnerFor ira = ir.getAnnotation(InterfaceRunnerFor.class);
					if(ira.value() == c) {
						// Aha! It does. Set c to ir, then break this for loop.
						// The runner for this class will act in the stead of this
						// class.
						c = ir;
						break findRunner;
					}
				}
				// c is the class we want to check to make sure it implements cons
				for(Method cCons : c.getDeclaredMethods()) {
					if(cCons.getName().equals(cons.getName()) && Arrays.equals(cons.getParameterTypes(), cCons.getParameterTypes())) {
						continue checkImplements;
					}
				}
				uhohs.add(c.getName() + " must implement the method with signature " + cons.getName() + "(" + getSignature(cons) + "), but doesn't.");
			}
		}

		if(!uhohs.isEmpty()) {
			List<String> uhohsList = new ArrayList<>(uhohs);
			Collections.sort(uhohsList);
			throw new Exception("There " + StringUtils.PluralHelper(uhohs.size(), "error") + ". The following classes need to implement various methods:\n" + StringUtils.Join(uhohs, "\n"));
		}
	}

	private static String getSignature(Member executable) {
		List<String> l = new ArrayList<>();
//		for(Class cc : executable.getParameterTypes()){
//			l.add(cc.getName());
//		}
		if(executable instanceof Method) {
			for(Class cc : ((Method) executable).getParameterTypes()) {
				l.add(cc.getName());
			}
		} else if(executable instanceof Constructor) {
			for(Class cc : ((Constructor) executable).getParameterTypes()) {
				l.add(cc.getName());
			}
		} else {
			throw new Error("Unexpected executable type");
		}
		return StringUtils.Join(l, ", ");
	}

	public static void verifyExhaustiveVisitors() throws ClassNotFoundException {
		Set<ClassMirror<ExhaustiveVisitor>> toVerify;
		toVerify = ClassDiscovery.getDefaultInstance()
				.getClassesThatExtend(ExhaustiveVisitor.class);
		for(ClassMirror<ExhaustiveVisitor> c : toVerify) {
			ExhaustiveVisitor.verify(c);
		}
	}

	public static void verifyNonInheritImplements() throws ClassNotFoundException {
		Set<ClassMirror<?>> toVerify;
		toVerify = ClassDiscovery.getDefaultInstance()
				.getClassesWithAnnotation(NonInheritImplements.class);
		Set<String> uhohs = new HashSet<>();
		for(ClassMirror<?> c : toVerify) {
			Class<?> iface = Class.forName(c.getAnnotation(NonInheritImplements.class).getValue("value").toString());
			if(!iface.isInterface()) {
				uhohs.add("The class given to @NonInheritImplements, tagged on " + c.getClassName() + " is not an interface, and must be.");
				continue;
			}
			// It's an interface, so go through all the methods it has, and make sure that the class c contains all the
			// methods.
			for(Method im : iface.getDeclaredMethods()) {
				try {
					c.getMethod(im.getName(), im.getParameterTypes());
				} catch (NoSuchMethodException ex) {
					String msg = "The class " + c.getClassName() + " implements " + iface.getSimpleName() + " but does not"
							+ " implement the method public " + im.getReturnType().getSimpleName() + " " + im.getName() + "(";
					List<String> params = new ArrayList<>();
					msg += StringUtils.Join(im.getParameters(), ", ", ", ", ", ", "", (Object item) -> {
						Parameter ci = (Parameter) item;
						return ci.getType().getSimpleName() + " " + ci.getName();
					});
					msg += ") {}";
					uhohs.add(msg);
				}
			}
		}
		if(!uhohs.isEmpty()) {
			String error = StringUtils.Join(uhohs, "\n");
			throw new Error(error);
		}
	}

}
