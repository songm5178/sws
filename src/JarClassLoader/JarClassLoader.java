package JarClassLoader;
public class JarClassLoader extends MultiClassLoader {
	private JarResources jarResources;

	public JarClassLoader(String jarName) {
		jarResources = new JarResources(jarName);
	}

	protected byte[] loadClassBytes(String className) {
		className = formatClassName(className);
		return (jarResources.getResource(className));
	}
}