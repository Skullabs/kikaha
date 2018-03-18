package kikaha.urouting;

import kikaha.commons.Reflection;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@SuppressWarnings("rawtypes")
public class ContextProducerFactoryLoader {

	@Inject
	@Typed(ContextProducer.class)
	Iterable<ContextProducer> availableProducers;
	
	ContextProducerFactory factory;

	@PostConstruct
	public void onStartup() {
		final Map<Class, ContextProducer> producers = loadAllProducers();
		factory = new ContextProducerFactory(producers);
	}
	
	@Produces
	public ContextProducerFactory produceFactory(){
		return factory;
	}

	private Map<Class, ContextProducer> loadAllProducers() {
		final Map<Class, ContextProducer> producers = new HashMap<>();
		for ( final ContextProducer producer : availableProducers ){
			final Class<?> forClazz = Reflection.getFirstGenericTypeFrom( producer, ContextProducer.class );
			producers.put( forClazz, producer );
		}
		return producers;
	}
}
