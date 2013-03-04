/**
 * Generic Module Loader
 * 
 * @author Ricardo F. Teixeira <ricardo.teixeira@caixamagica.pt>
 * @version 1.0
 * 
 */

package pt.caixamagica.gml;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;

public final class GenericModuleLoader {
	private static final class GenericModuleFactory {
		private final ConcurrentMap<String, WeakReference<Object>> modules;
		
		public GenericModuleFactory() { 
			modules = new ConcurrentHashMap<String, WeakReference<Object>>();
		}
		
		public Object instantiate(String name, Class<?> type) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
			JavaClass module = new ClassParser(name).parse();

			if (this.modules.containsKey(module.getClassName())) {
				return this.modules.get(module.getClassName());
			}
			
			JavaClass moduleInterface = Repository.lookupClass(type);
			
			for (JavaClass m : module.getInterfaces()) {
				if (m.implementationOf(moduleInterface)) {
					Class<?> obj = Class.forName(module.getClassName());
					
					if (obj != null) {
						for (Constructor<?> c : obj.getConstructors()) {
							if (c.getParameterTypes().length == 0) {
								WeakReference<Object> reference = new WeakReference<Object>(obj.newInstance());
								this.modules.put(module.getClassName(), reference);
								
								return reference.get();
							}
						}
					}
				}
			}
			
			return null;
		}
		
		public Object instantiate(String name) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
			JavaClass module = new ClassParser(name).parse();

			if (this.modules.containsKey(module.getClassName())) {
				return this.modules.get(module.getClassName());
			}
			
			Class<?> obj = Class.forName(module.getClassName());
			
			if (obj != null) {
				for (Constructor<?> c : obj.getConstructors()) {
					if (c.getParameterTypes().length == 0) {
						WeakReference<Object> reference = new WeakReference<Object>(obj.newInstance());
						this.modules.put(module.getClassName(), reference);
						
						return reference.get();
					}
				}
			}

			return null;
		}
	}
	
	private final GenericModuleFactory factory;
	
	public GenericModuleLoader() {
		factory = new GenericModuleFactory();
	}
	
	public int size() {
		return this.factory.modules.size();
	}
	
	public boolean isEmpty() {
		return this.factory.modules.isEmpty();
	}
	
	public synchronized boolean unload(Object module) {
		if (module == null) {
			return false;
		}

		for (WeakReference<Object> m : this.factory.modules.values()) {
			if (m.get().equals(module)) {
				Object remove = this.factory.modules.remove(m.get().getClass().getName());
				
				System.gc();
				
				return (remove != null);
			}
		}
		
		return false;
	}
	
	public synchronized boolean unload() {
		for (WeakReference<Object> m : this.factory.modules.values()) {
			this.factory.modules.remove(m.get().getClass().getName());
		}
		
		System.gc();
		
		return this.factory.modules.isEmpty();
	}

	public synchronized List<Object> load(String path) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
		if (path == null) {
			throw new IllegalArgumentException();
		}
		
		return load(new File(path), null);
	}
	
	public synchronized List<Object> load(String path, Class<?> type) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
		if (path == null) {
			throw new IllegalArgumentException();
		}
		
		return load(new File(path), type);
	}
	
	public synchronized List<Object> load(File path) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
		if (path == null) {
			throw new IllegalArgumentException();
		}
		
		return load(path, null);
	}

	public synchronized List<Object> load(File path, Class<?> type) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		if (path == null) {
			throw new IllegalArgumentException();
		}
		
		if (! path.exists() || path.isFile()) {
			throw new FileNotFoundException();
		}

		String[] plugins = path.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".class");
			}
		});


		List<Object> list = new LinkedList<Object>();

		for (int i = 0; i < plugins.length; i++) {
			String plugin = plugins[i].substring(0, plugins[i].length() - 6);

			if (plugin.indexOf('$') == -1) {
				String filename = path + File.separator + plugins[i];
				
				try (DataInputStream input = new DataInputStream(new FileInputStream(filename));) {
					Object module;
					
					if (type == null) {
						module = this.factory.instantiate(filename);
					}
					
					else {
						module = this.factory.instantiate(filename, type);
					}
					
					if (module != null) {
						list.add(module);
					}
				}
			}
		}

		return list;
	}
}
