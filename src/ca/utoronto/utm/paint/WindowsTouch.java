package ca.utoronto.utm.paint;

import javax.swing.*;
import java.awt.*;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class WindowsTouch {
	static {
		System.out.println(System.getProperty("user.dir"));
		System.load(System.getProperty("user.dir")+"\\Assignment2\\JNI.dll");
	}

	private static native void Init(long hWnd);
	private static WindowsTouch instance;
	public static void main(String[] args){
		JFrame jf = new JFrame();
		JButton b = new JButton();
		b.addActionListener(e ->  {
			//System.out.println(Thread.currentThread().getId());
		});
		jf.setMinimumSize(new Dimension(300,200));
		jf.setDefaultCloseOperation(3);
		jf.add(b);
		jf.pack();
		Init(getHWnd(jf));
		jf.setVisible(true);

		try {
			NativeLibraries n = new NativeLibraries();
			System.out.println(n.getLoadedLibraries());
		} catch(NoSuchFieldException e) {
			e.printStackTrace();
		}

		//JOptionPane.showMessageDialog(jf,"hahaha");
	}

	private static long pointDown(){
		System.out.println("down!");
		//b.setText(String.valueOf(Math.random()));
		return 100;
	}

	public static WindowsTouch getInstance() {
		if(instance==null)
			instance=new WindowsTouch();
		return instance;
	}

	private static long getHWnd(Frame component){
		//noinspection deprecation
		Object peer = component.getPeer();
		Class c = peer.getClass();
		try {
			for (Method m : c.getMethods()) {
				if (m.getName().equals("getHWnd")) {
					return (Long)m.invoke(peer);
				}
			}
		} catch(IllegalAccessException|InvocationTargetException e) {
			e.printStackTrace();
		}
		throw new RuntimeException("No HWND found for "+ c);
	}
}

/**
 * Helper functions for native libraries.
 * <p/>
 * @author Gili Tzabari
 */
class NativeLibraries
{
	private final Field loadedLibraryNames;
	private final Field systemNativeLibraries;
	private final Field nativeLibraries;
	private final Field nativeLibraryFromClass;
	private final Field nativeLibraryName;

	/**
	 * Creates a new NativeLibraries.
	 * <p/>
	 * @throws NoSuchFieldException if one of ClassLoader's fields cannot be found
	 */
	public NativeLibraries() throws NoSuchFieldException
	{
		this.loadedLibraryNames = ClassLoader.class.getDeclaredField("loadedLibraryNames");
		loadedLibraryNames.setAccessible(true);

		this.systemNativeLibraries = ClassLoader.class.getDeclaredField("systemNativeLibraries");
		systemNativeLibraries.setAccessible(true);

		this.nativeLibraries = ClassLoader.class.getDeclaredField("nativeLibraries");
		nativeLibraries.setAccessible(true);

		Class<?> nativeLibrary = null;
		for (Class<?> nested : ClassLoader.class.getDeclaredClasses())
		{
			if (nested.getSimpleName().equals("NativeLibrary"))
			{
				nativeLibrary = nested;
				break;
			}
		}
		this.nativeLibraryFromClass = nativeLibrary.getDeclaredField("fromClass");
		nativeLibraryFromClass.setAccessible(true);

		this.nativeLibraryName = nativeLibrary.getDeclaredField("name");
		nativeLibraryName.setAccessible(true);
	}

	/**
	 * Returns the names of native libraries loaded across all class loaders.
	 * <p/>
	 * @return a list of native libraries loaded
	 */
	public List<String> getLoadedLibraries()
	{
		try
		{
			@SuppressWarnings("UseOfObsoleteCollectionType")
			final Vector<String> result = (Vector<String>) loadedLibraryNames.get(null);
			return result;
		}
		catch (IllegalArgumentException | IllegalAccessException e)
		{
			throw new AssertionError(e);
		}
	}

	/**
	 * Returns the native libraries loaded by the system class loader.
	 * <p/>
	 * @return a Map from the names of native libraries to the classes that loaded them
	 */
	public Map<String, Class<?>> getSystemNativeLibraries()
	{
		try
		{
			Map<String, Class<?>> result = new HashMap<>();
			@SuppressWarnings("UseOfObsoleteCollectionType")
			final Vector<Object> libraries = (Vector<Object>) systemNativeLibraries.get(null);
			for (Object nativeLibrary : libraries)
			{
				String libraryName = (String) nativeLibraryName.get(nativeLibrary);
				Class<?> fromClass = (Class<?>) nativeLibraryFromClass.get(nativeLibrary);
				result.put(libraryName, fromClass);
			}
			return result;
		}
		catch (IllegalArgumentException | IllegalAccessException e)
		{
			throw new AssertionError(e);
		}
	}

	/**
	 * Returns a Map from the names of native libraries to the classes that loaded them.
	 * <p/>
	 * @param loader the ClassLoader that loaded the libraries
	 * @return an empty Map if no native libraries were loaded
	 */
	public Map<String, Class<?>> getNativeLibraries(final ClassLoader loader)
	{
		try
		{
			Map<String, Class<?>> result = new HashMap<>();
			@SuppressWarnings("UseOfObsoleteCollectionType")
			final Vector<Object> libraries = (Vector<Object>) nativeLibraries.get(loader);
			for (Object nativeLibrary : libraries)
			{
				String libraryName = (String) nativeLibraryName.get(nativeLibrary);
				Class<?> fromClass = (Class<?>) nativeLibraryFromClass.get(nativeLibrary);
				result.put(libraryName, fromClass);
			}
			return result;
		}
		catch (IllegalArgumentException | IllegalAccessException e)
		{
			throw new AssertionError(e);
		}
	}

	/**
	 * The same as {@code #getNativeLibraries()} except that all ancestor classloaders are processed
	 * as well.
	 * <p/>
	 * @param loader the ClassLoader that loaded (or whose ancestors loaded) the libraries
	 * @return an empty Map if no native libraries were loaded
	 */
	public Map<String, Class<?>> getTransitiveNativeLibraries(final ClassLoader loader)
	{
		Map<String, Class<?>> result = new HashMap<>();
		ClassLoader parent = loader.getParent();
		while (parent != null)
		{
			result.putAll(getTransitiveNativeLibraries(parent));
			parent = parent.getParent();
		}
		result.putAll(getNativeLibraries(loader));
		return result;
	}

	/**
	 * Converts a map of library names to the classes that loaded them to a map of library names to
	 * the classloaders that loaded them.
	 * <p/>
	 * @param libraryToClass a map of library names to the classes that loaded them
	 * @return a map of library names to the classloaders that loaded them
	 */
	public Map<String, ClassLoader> getLibraryClassLoaders(Map<String, Class<?>> libraryToClass)
	{
		Map<String, ClassLoader> result = new HashMap<>();
		for (Map.Entry<String, Class<?>> entry : libraryToClass.entrySet())
			result.put(entry.getKey(), entry.getValue().getClassLoader());
		return result;
	}

	/**
	 * Returns a list containing the classloader and its ancestors.
	 * <p/>
	 * @param loader the classloader
	 * @return a list containing the classloader, its parent, and so on
	 */
	public static List<ClassLoader> getTransitiveClassLoaders(ClassLoader loader)
	{
		List<ClassLoader> result = new ArrayList<>();
		ClassLoader parent = loader.getParent();
		result.add(loader);
		while (parent != null)
		{
			result.add(parent);
			parent = parent.getParent();
		}
		return result;
	}
}
