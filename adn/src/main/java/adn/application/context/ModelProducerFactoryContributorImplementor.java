/**
 * 
 */
package adn.application.context;

import static adn.service.internal.Role.ADMIN;
import static adn.service.internal.Role.CUSTOMER;
import static adn.service.internal.Role.PERSONNEL;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.BiFunction;

import adn.application.context.builders.DynamicMapModelProducerFactoryImpl.ModelProducerFactoryContributor;
import adn.helpers.Utils;
import adn.model.entities.Account;
import adn.model.factory.authentication.Arguments;
import adn.model.factory.authentication.Credential;
import adn.model.factory.authentication.ModelProducerFactoryBuilder;
import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
public class ModelProducerFactoryContributorImplementor implements ModelProducerFactoryContributor {

	public static final BiFunction<Arguments<?>, Credential, ?> LOCAL_DATE_FORMATTER = new BiFunction<>() {
		@Override
		public Object apply(Arguments<?> arg, Credential credential) {
			Object source = arg.getSource();

			return source != null ? Utils.localDate((LocalDate) source) : null;
		}
	};
	public static final BiFunction<Arguments<?>, Credential, ?> LOCAL_DATE_TIME_FORMATTER = new BiFunction<>() {
		@Override
		public Object apply(Arguments<?> arg, Credential credential) {
			return Utils.localDateTime((LocalDateTime) arg.getSource());
		}
	};

	@SuppressWarnings("unchecked")
	@Override
	public void contribute(ModelProducerFactoryBuilder builder) {
		final Role[] allRoles = Role.values();
		final Role[] personnels = new Role[] { PERSONNEL };
		final Role[] domained = new Role[] { ADMIN, CUSTOMER, PERSONNEL };
		BiFunction<Arguments<?>, Credential, ?>[] localDateFormatter = (BiFunction<Arguments<?>, Credential, ?>[]) Array
				.newInstance(BiFunction.class, 1);

		localDateFormatter[0] = LOCAL_DATE_FORMATTER;

		BiFunction<Arguments<?>, Credential, ?>[] localDateTimeFormatter = (BiFunction<Arguments<?>, Credential, ?>[]) Array
				.newInstance(BiFunction.class, 1);

		localDateTimeFormatter[0] = LOCAL_DATE_TIME_FORMATTER;

		// @formatter:off
		builder
			.type(Account.class)
				.roles(allRoles)
					.fields("id").use("username").publish()
					.fields("firstName", "lastName", "photo", "role", "gender", "active").publish()
					.anyFields().mask()
				.roles(personnels)
					.fields("address", "email", "phone").publish()
					.fields("createdDate", "deactivatedDate", "birthDate")
						.use(localDateFormatter)
					.fields("updatedDate")
						.use(localDateTimeFormatter)
					.anyFields().mask();
		// @formatter:on
	}

}
