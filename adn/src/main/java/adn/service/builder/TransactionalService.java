package adn.service.builder;

import java.lang.reflect.Method;

public interface TransactionalService {

	boolean registerEvent(Object invoker, Method m, Object[] values);

}
