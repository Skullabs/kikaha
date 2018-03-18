package kikaha.core.cdi;

import kikaha.core.cdi.helpers.*;
import kikaha.core.cdi.helpers.filter.Condition;
import kikaha.core.cdi.helpers.filter.Filter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@SuppressWarnings( { "rawtypes", "unchecked" } )
public class DefaultCDI implements CDI {

	final InjectionContext injectionContext = new InjectionContext();
	final DependencyMap dependencies;
	final ProducerFactoryMap producers;

	public DefaultCDI() {
		dependencies = new DependencyMap( createDefaultProvidedData() );
		injectionContext.setQualifierExtractor( loadInjectableDataExtractor() );
		producers = loadAllProducers();
	}

	private InjectableDataExtractor loadInjectableDataExtractor() {
		final Iterable<FieldQualifierExtractor> extractors = loadAll(FieldQualifierExtractor.class);
		return new InjectableDataExtractor( extractors );
	}

	public void loadAllCustomClassConstructors(){
		try {
			final List<CustomClassConstructor> customClassConstructors = new ArrayList<>();

			for ( CustomClassConstructor constructor : loadAll(CustomClassConstructor.class) )
				customClassConstructors.add( constructor );
			customClassConstructors.add( new DefaultClassConstructor() );

			injectionContext.setCustomClassConstructors( customClassConstructors );
		} catch ( Throwable cause ) {
			throw new ServiceProviderException( cause );
		}
	}

	protected Map<Class<?>, Iterable<?>> createDefaultProvidedData() {
		final Map<Class<?>, Iterable<?>> injectable = new HashMap<>();
		injectable.put( CDI.class, new SingleObjectIterable<>( this ) );
		return injectable;
	}

	protected ProducerFactoryMap loadAllProducers() {
		final Iterable<Class<ProducerFactory>> loadClassesImplementing = loadClassesImplementing( ProducerFactory.class );
		return ProducerFactoryMap.from( loadClassesImplementing );
	}

	public <T> Iterable<Class<T>> loadClassesImplementing( final Class<T> targetClass ) {
		return injectionContext.loadClassesImplementing( targetClass );
	}

	@Override
	public <T> T load(final Class<T> serviceClazz, final Condition<T> condition, final ProviderContext context )
			throws ServiceProviderException {
		while ( true )
			try { return fromInjector( i -> i.load( serviceClazz, condition, context ) ); }
			catch ( final DependencyMap.TemporarilyLockedException cause ) { LockSupport.parkNanos( 2l ); }
	}

	@Override
	public <T> Iterable<T> loadAll( final Class<T> serviceClazz, final ProviderContext context ) {
		while ( true )
			try { return fromInjector( i -> i.loadAll( serviceClazz, context ) ); }
			catch ( final DependencyMap.TemporarilyLockedException cause ) { LockSupport.parkNanos( 2l ); }
	}

	@Override
	public <T> void producerFor( final Class<T> serviceClazz, final ProducerFactory<T> provider ) {
		producers.memorizeProviderForClazz( provider, serviceClazz );
	}

	@Override
	public <T> void dependencyFor( final Class<T> serviceClazz, final T object ) {
		providerFor( serviceClazz, new SingleObjectIterable<>( object ) );
	}

	protected <T> void providerFor( final Class<T> serviceClazz, final Iterable<T> iterable ) {
		synchronized ( dependencies ) {
			dependencies.put( serviceClazz, iterable );
			dependencies.unlock( serviceClazz );
		}
	}

	@Override
	public <T> void injectOn( final Iterable<T> iterable ) {
		withInjector( i->i.loadDependenciesAndInjectInto( iterable ) );
	}

	@Override
	public void injectOn( final Object object ) {
		log.debug("Injecting on " + (object != null ? object.getClass().getCanonicalName() : "null"));
		withInjector( i->i.loadDependenciesAndInjectInto( object ) );
	}

	private <T> void withInjector( Consumer<DependencyInjector> callback ) {
		final DependencyInjector injector = new DependencyInjector();
		callback.accept( injector );
		injector.flush();
	}

	public <T> ProducerFactory<T> getProducerFor(final Class<T> serviceClazz ) {
		return fromInjector( i -> i.getProducerFor( serviceClazz ) );
	}

	private <T> T fromInjector( Function<DependencyInjector, T> callback ) {
		final DependencyInjector injector = new DependencyInjector();
		final T t = callback.apply( injector );
		injector.flush();
		return t;
	}

	public static CDI newInstance(){
		final DefaultCDI cdi = new DefaultCDI();
		cdi.loadAllCustomClassConstructors();
		return cdi;
	}

	/**
	 * A dependency injection context. This class was designed to allow
	 * reentrant injection to deal with recursive dependencies.
	 */
	final public class DependencyInjector {

		final Queue<Injectable> classesToBeConstructed = new ArrayDeque<>();
		final Queue<InjectableField> fieldToTryToInjectAgainLater = new ArrayDeque<>();

		public <T> T load( final Class<T> serviceClazz, Condition<T> condition, ProviderContext providerContext ) {
			final Reference<T> produced = produceFromFactory( serviceClazz, providerContext );
            return produced.getOrElse( () -> Filter.first( loadAll( serviceClazz, condition, providerContext ), condition ) );
		}

		private <T> Reference<T> produceFromFactory( final Class<T> serviceClazz, final ProviderContext context )
		{
			final ProducerFactory<T> provider = getProducerFor( serviceClazz );
			if ( provider != null ) {
                final T produced = provider.provide(context);
                final String optionalFailureMsg = "The ProducerFactory " + provider.getClass().getCanonicalName() + " returned a null object.";
                return Reference.mandatory( optionalFailureMsg, produced );
            }
			return Reference.optional();
		}

		public <T> ProducerFactory<T> getProducerFor(final Class<T> serviceClazz ) {
			if ( producers == null ) {
			    log.debug( "Premature invocation of this method. Ignoring!" );
                return null;
            }
			return (ProducerFactory<T>)producers.get( serviceClazz, this );
		}

		public <T> Iterable<T> loadAll( final Class<T> serviceClazz, Condition<T> condition, ProviderContext providerContext ) {
			return Filter.filter( loadAll( serviceClazz, providerContext ), condition );
		}

		public <T> Iterable<T> loadAll( final Class<T> serviceClazz, final ProviderContext providerContext ) {
			Iterable<?> instances = dependencies.get( serviceClazz );
			if ( instances == null )
				synchronized ( dependencies ) {
					instances = dependencies.get( serviceClazz );
					if ( instances == null )
						instances = loadServicesFor( serviceClazz, providerContext );
				}
			return (Iterable<T>)instances;
		}

		private <T> Iterable<T> loadServicesFor(@NonNull final Class<T> serviceClazz, final ProviderContext providerContext ) {
			final List<Class<T>> iterableInterfaces = injectionContext.loadClassesImplementing( serviceClazz );
			Iterable<T> instances = null;
			if ( !iterableInterfaces.isEmpty() ) {
				instances = injectionContext.instantiate( iterableInterfaces, providerContext );
				dependencies.put( serviceClazz, instances );
			} else {
				final T instance = injectionContext.instantiate( serviceClazz, providerContext );
				instances = instance == null ? Collections.emptyList() : new SingleObjectIterable<>( instance );
			}
			loadDependenciesAndInjectInto( instances );
			dependencies.unlock( serviceClazz );
			return instances;
		}

		public void loadDependenciesAndInjectInto( Iterable<?> objs ) {
			for ( final Object obj : objs )
				loadDependenciesAndInjectInto( obj );
		}

		public void loadDependenciesAndInjectInto( Object obj ) {
			log.debug("Loading dependencies for " + (obj != null ? obj.getClass().getCanonicalName() : "null"));
			final ProvidableClass<?> providableClass = injectionContext.retrieveProvidableClass( obj.getClass() );
			tryInjectFields( obj, providableClass );
			tryPostConstructClass( obj, providableClass );
		}

		private void tryInjectFields( Object obj, final ProvidableClass<?> providableClass ) {
			for ( final ProvidableField field : providableClass.fields() )
				try {
					field.provide( obj, this );
				} catch ( final Throwable e ) {
					log.debug( e.getMessage(), e );
					fieldToTryToInjectAgainLater.add( new InjectableField( field, obj ) );
				}
		}

		private void tryPostConstructClass( Object obj, final ProvidableClass<?> providableClass ) {
			final Injectable injectable = new Injectable( providableClass, obj );
			try {
				injectable.postConstruct();
			} catch ( final Throwable cause ) {
				classesToBeConstructed.add( injectable );
			}
		}

		public void flush() {
			int availableRetries = 5;
			while ( availableRetries > 0 && isNotClean() ) {
				tryPostConstructClasses();
				tryInjectFailedFields();
				availableRetries--;
			}

			injectFailedFields();
			postConstructClasses();
		}

		private boolean isNotClean() {
			return !fieldToTryToInjectAgainLater.isEmpty() || !classesToBeConstructed.isEmpty();
		}

		private void tryPostConstructClasses() {
			final List<Injectable> failed = new ArrayList<>();

			while ( !classesToBeConstructed.isEmpty() ) {
				final Injectable injectable = classesToBeConstructed.poll();
				try { injectable.postConstruct(); } 
				catch ( final Throwable cause ) { failed.add( injectable ); }
			}

			classesToBeConstructed.addAll( failed );
		}

		private void postConstructClasses() {
			while ( !classesToBeConstructed.isEmpty() ) {
				final Injectable injectable = classesToBeConstructed.poll();
				try { injectable.postConstruct(); }
				catch ( final Throwable cause ) {
					throw new ServiceProviderException( "Could not Post Construct the class "
						+ injectable.instance.getClass().getCanonicalName(), cause );
				}
			}
		}

		private void tryInjectFailedFields() {
			final List<InjectableField> failed = new ArrayList<>();

			while ( !fieldToTryToInjectAgainLater.isEmpty() ) {
				final InjectableField field = fieldToTryToInjectAgainLater.poll();
				try { field.structure.provide( field.instance, this ); }
				catch ( final Throwable e ) { failed.add( field ); }
			}

			fieldToTryToInjectAgainLater.addAll( failed );
		}

		private void injectFailedFields() {
			while ( !fieldToTryToInjectAgainLater.isEmpty() ) {
				final InjectableField field = fieldToTryToInjectAgainLater.poll();
				try { field.structure.provide( field.instance, this ); }
				catch ( final Throwable e ) { handleFieldInjectionError( field, e ); }
			}
		}

		private void handleFieldInjectionError( final InjectableField field, final Throwable e ) {
			log.error( "Failed to provide data on " + field.structure, e );
		}
	}

	@RequiredArgsConstructor
	class Injectable {

		final ProvidableClass<?> structure;
		final Object instance;

		public void postConstruct() {
			structure.postConstructor().accept( instance );
		}
	}
}

@RequiredArgsConstructor
class InjectableField {
	final ProvidableField structure;
	final Object instance;
}