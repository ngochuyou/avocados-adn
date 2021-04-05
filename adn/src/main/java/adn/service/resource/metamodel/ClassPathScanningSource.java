/**
 * 
 */
package adn.service.resource.metamodel;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Formula;
import org.hibernate.mapping.Value;
import org.hibernate.tuple.GenerationTiming;
import org.hibernate.tuple.ValueGeneration;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import adn.service.resource.local.ContextBuildingService;
import adn.service.resource.local.NamingStrategy;
import adn.utilities.StringHelper;
import javassist.Modifier;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public class ClassPathScanningSource implements Metadata {

	private final ContextBuildingService contextService;

	public static final String MODEL_PACKAGE = "adn.service.resource.models";
	private final Set<Class<?>> modelClasses = new HashSet<>();

	private final Map<String, ResourceClass<?>> processedResourceClassMap = new HashMap<>();
	private final Set<String> processedNames = new HashSet<>();
	// @formatter:off
	private final Map<ValueGenerationEntryKey, ? extends ValueGeneration> valueGenerationMap = Map.of(
			ValueGenerationEntryKey.IDENTIFIER_INSERT, new ResourceIdentifierValueGeneration(GenerationTiming.INSERT)
	);
	// @formatter:on
	public ClassPathScanningSource(ContextBuildingService contextService) {
		this.contextService = contextService;
	}

	@Override
	public void process() {
		prepareModelClasses();

		for (Class<?> clazz : modelClasses) {
			processModel(clazz);
		}

		processedResourceClassMap.values().forEach(this::processProperty);
	}

	private <X> void processProperty(ResourceClass<X> rc) {
		Class<X> clazz = rc.getType();

		for (Field f : clazz.getDeclaredFields()) {
			rc.addProperty(createProperty(rc, f));
		}
	}

	private <X> ResourceProperty<X> createProperty(ResourceClass<X> owner, Field f) {
		ResourceProperty<X> property = new ResourceProperty<>();

		property.setLazy(false);
		property.setCascade(CascadeType.ALL.toString());
		property.setName(f.getName());

		Column colAnno = f.getDeclaredAnnotation(Column.class);
		// @formatter:off
		property.setInsertable(
				(colAnno == null || (colAnno != null && colAnno.insertable()))
					&& f.getDeclaredAnnotation(Formula.class) == null);
		property.setUpdateable(
				(colAnno != null && colAnno.updatable())
				&& !Modifier.isFinal(f.getModifiers()));
		// @formatter:on
		property.setValue(resolveValue(f));
		property.setValueGenerationStrategy(resolveValueGeneration(property.getValue(), f));

		return null;
	}

	private Value resolveValue(Field f) {
		if (f.getDeclaredAnnotation(Id.class) != null) {
//			return new SimpleValue(identifierTypeMap.get(f.getType()), false, true, false, false);
		}

		if (f.getDeclaredAnnotation(Version.class) != null) {

		}

		return null;
	}

	private ValueGeneration resolveValueGeneration(Value value, Field f) {
		return null;
	}

	private void prepareModelClasses() {
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);

		scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
		scanner.findCandidateComponents(MODEL_PACKAGE).forEach(bean -> {
			try {
				modelClasses.add(Class.forName(bean.getBeanClassName()));
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}

	@SuppressWarnings("unchecked")
	private <X> ResourceClass<X> processModel(Class<X> clazz) {
		if (!modelClasses.contains(clazz)) {
			return null;
		}

		Entity anno = clazz.getDeclaredAnnotation(Entity.class);
		String name = StringHelper.hasLength(anno.name()) ? anno.name()
				: contextService.getService(NamingStrategy.class).getName(clazz);

		if (processedResourceClassMap.containsKey(name)) {
			return (ResourceClass<X>) processedResourceClassMap.get(name);
		}

		ResourceClass<X> newResourceClass = new ResourceClass<>();

		newResourceClass.setType(clazz);
		newResourceClass.setResourceName(name);
		newResourceClass.setIsAbstract(Modifier.isAbstract(clazz.getModifiers()));

		if (clazz.getSuperclass() != null) {
			newResourceClass.setSuperClass(processModel(clazz.getSuperclass()));
		}

		processedResourceClassMap.put(name, newResourceClass);
		processedNames.add(name);

		return newResourceClass;
	}

	private void postProcess() {

	}

	@SuppressWarnings("unchecked")
	@Override
	public <X> ResourceClass<X> getResourceClass(String name) {
		// TODO Auto-generated method stub
		return (ResourceClass<X>) processedResourceClassMap.get(name);
	}

	@Override
	public Set<String> getImports() {
		// TODO Auto-generated method stub
		return processedNames;
	}

	private static class ValueGenerationEntryKey {

		private static final ValueGenerationEntryKey IDENTIFIER_INSERT = new ValueGenerationEntryKey(
				GenerationTiming.INSERT, ResourceIdentifierValueGeneration.class);

		GenerationTiming timing;

		Class<? extends ValueGeneration> strategy;

		ValueGenerationEntryKey(GenerationTiming timing, Class<? extends ValueGeneration> strategy) {
			super();
			this.timing = timing;
			this.strategy = strategy;
		}

		@Override
		public boolean equals(Object other) {
			// TODO Auto-generated method stub
			if (other instanceof ValueGenerationEntryKey) {
				return false;
			}

			ValueGenerationEntryKey otherKey = (ValueGenerationEntryKey) other;

			return otherKey.timing == timing && strategy.equals(otherKey.strategy);
		}

	}

}
