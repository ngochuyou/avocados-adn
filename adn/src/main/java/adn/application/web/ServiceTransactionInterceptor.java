package adn.application.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import adn.application.context.TransactionFactory;
import adn.controller.BaseController;
import adn.service.builder.FlushMode;
import adn.service.builder.ServiceTransaction;
import adn.utilities.Strings;

@Component
public class ServiceTransactionInterceptor implements HandlerInterceptor {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TransactionFactory transactionFactory;

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		// TODO Auto-generated method stub
		logger.debug("Processing afterCompletion");

		String flushHeader = response.getHeader(BaseController.serviceTransactionFlushHeader);
		
		if (Strings.isEmpty(flushHeader)) {
			HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
			
			return;
		}
		
		ServiceTransaction transaction = transactionFactory.getTransaction();

		if (flushHeader.equals(FlushMode.COMMIT.toString())) {
			logger.debug("Committing " + transaction.getClass().getName());
			transaction.commit();

			return;
		}

		if (flushHeader.equals(FlushMode.ROLLBACK.toString())) {
			logger.debug("Rolling back " + transaction.getClass().getName());
			transaction.rollback();

			return;
		}
		
		logger.debug("Clearing " + transaction.getClass().getName());
		transaction.clear();
	}
}
