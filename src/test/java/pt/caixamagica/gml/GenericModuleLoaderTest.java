/**
 * Generic Module Loader
 * 
 * @author Ricardo F. Teixeira <ricardo.teixeira@caixamagica.pt>
 * @version 1.0
 * 
 */

package pt.caixamagica.gml;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import junit.framework.TestCase;

import org.junit.Test;

import pt.caixamagica.gml.GenericModuleLoader;
import pt.caixamagica.gml.modules.IModule;

public class GenericModuleLoaderTest extends TestCase {
	public GenericModuleLoaderTest(String name) {
		super(name);
	}
	
	@Test
	public void testLoadModulesWhoImplementIModuleInterface() {
		String path = "build/test-classes/pt/caixamagica/gml/modules";
		GenericModuleLoader loader = new GenericModuleLoader();
		
		try {
			List<Object> modules = loader.load(path, IModule.class);

			assertTrue(modules.size() == 1);
			assertTrue(modules.get(0) instanceof IModule);
		}

		catch (InstantiationException | IllegalAccessException | ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testLoadAllModules() {
		String path = "build/test-classes/pt/caixamagica/gml/modules";
		GenericModuleLoader loader = new GenericModuleLoader();
		
		try {
			List<Object> modules = loader.load(path);
			
			assertTrue(modules.size() == 2);
		}

		catch (InstantiationException | IllegalAccessException | ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testExpectingFileNotLoadedException() throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		String path = UUID.randomUUID().toString();
		GenericModuleLoader loader = new GenericModuleLoader();
		
		try {
			List<Object> modules = loader.load(path);
			
			assertTrue(modules.size() == 2);
		}

		catch (FileNotFoundException e) {
			assertTrue(e instanceof FileNotFoundException);
		}
	}

	
	@Test
	public void testExpectingEmptyList() {
		GenericModuleLoader loader = new GenericModuleLoader();
		
		try {
			Path path = Files.createTempDirectory(UUID.randomUUID().toString());
			
			List<Object> modules = loader.load(path.toFile().getAbsolutePath());
			
			assertTrue(modules.isEmpty());
			
			path.toFile().delete();
		}

		catch (InstantiationException | IllegalAccessException | ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testUnloadModule1() {
		String path = "build/test-classes/pt/caixamagica/gml/modules";
		GenericModuleLoader loader = new GenericModuleLoader();
		
		try {
			List<Object> modules = loader.load(path, IModule.class);
			
			loader.unload(modules.get(0));
			
			assertTrue(loader.isEmpty());
		}

		catch (InstantiationException | IllegalAccessException | ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}
}