/**
 * 
 */
package adn.service.resource.storage;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.hibernate.SessionFactory;
import org.hibernate.SessionFactoryObserver;
import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.service.Service;

/**
 * @author Ngoc Huy
 *
 */
public interface LocalResourceStorage extends Service {

	String LOCAL_DIRECTORY = "C:\\Users\\Ngoc Huy\\Documents\\avocados-adn\\";

	String IMAGE_FILE_DIRECTORY = LOCAL_DIRECTORY + "images\\";

	String DEFAULT_USER_PHOTO_NAME = "aad81c87bd8316705c4568e72577eb62476a.jpg";

	boolean isExists(String filename);

	ResultSetImplementor select(Serializable identifier);

	ResultSetImplementor select(Serializable[] identifier);

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

		int getIndex(String name);

		@Override
		default void sessionFactoryCreated(SessionFactory factory) {
			close();
		}

	}

}
