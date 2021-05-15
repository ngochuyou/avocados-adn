/**
 * 
 */
package adn.service.resource.engine;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.SessionFactoryObserver;
import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.service.Service;

import adn.service.resource.engine.query.Query;
import adn.service.resource.template.ResourceTemplate;

/**
 * @author Ngoc Huy
 *
 */
public interface LocalStorage extends Service {

	String LOCAL_DIRECTORY = "C:\\Users\\Ngoc Huy\\Documents\\avocados-adn\\";

	String IMAGE_FILE_DIRECTORY = LOCAL_DIRECTORY + "images\\";

	String DEFAULT_USER_PHOTO_NAME = "aad81c87bd8316705c4568e72577eb62476a.jpg";

	void registerTemplate(ResourceTemplate template) throws IllegalArgumentException;

	ResultSetImplementor select(Serializable identifier);

	ResultSetImplementor select(Serializable[] identifier);

	ResultSetImplementor query(Query query);

	void lock(Serializable identifier);

	public interface ResultSetImplementor extends ResultSet {

	}

	public interface ResultSetMetaDataImplementor extends ResultSetMetaData, Service, SessionFactoryObserver {

		public interface Access {

			void addColumn(String name) throws NoSuchFieldException, SecurityException;

			void addExplicitlyHydratedColumn(String name);

			void addSynthesizedColumn(String name);

		}

		Access getAccess() throws IllegalAccessException;

		void close();

		PropertyAccess getPropertyAccess(String name) throws IllegalArgumentException, SQLException;

		PropertyAccess getPropertyAccess(int index) throws IllegalArgumentException, SQLException;

		List<PropertyAccess> getPropertyAccessors();

		int getIndex(String name);

		@Override
		default void sessionFactoryCreated(SessionFactory factory) {
			close();
		}

	}

}
