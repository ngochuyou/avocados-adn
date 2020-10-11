package adn.application.web;

import java.util.Map;
import java.util.function.Consumer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import adn.application.context.GlobalTransactionManager;
import adn.controller.BaseController;
import adn.service.transaction.Mode;

@Component
public class TransactionalHandlerInterceptor implements HandlerInterceptor {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	protected GlobalTransactionManager transactionFactory;

	@Autowired
	private SessionFactory sessionFactory;

	private Map<String, Consumer<HttpServletResponse>> postHandlers;

	@Autowired
	public TransactionalHandlerInterceptor() {
//		this.postHandlers = new HashMap<>();
//		this.postHandlers.put(FlushMode.COMMIT_ALL.toString(), response -> {
//			if (transactionFactory.getTransaction().commit()) {
//				Session session = sessionFactory.getCurrentSession();
//				
//				if (session != null) {
//					session.flush();
//				}
//			}
//		});
//		this.postHandlers.put(FlushMode.CLEAR_SERVICE.toString(), response -> transactionFactory.getTransaction().clear());
//		this.postHandlers.put(FlushMode.ROLLBACK_SERVICE.toString(), response -> transactionFactory.getTransaction().rollback());
//		this.postHandlers.put(FlushMode.COMMIT_SERVICE.toString(), response -> transactionFactory.getTransaction().commit());
//		this.postHandlers.put(FlushMode.COMMIT_HIBERNATE.toString(), response -> {
//			Session session = sessionFactory.getCurrentSession();
//			
//			if (session != null) {
//				session.flush();
//			}
//		});
//		this.postHandlers.put(FlushMode.CLEAR_HIBERNATE.toString(), response -> {
//			Session session = sessionFactory.getCurrentSession();
//			
//			if (session != null) {
//				session.clear();
//			}
//		});
//		this.postHandlers.put(FlushMode.CLEAR_ALL.toString(), response -> {
//			transactionFactory.getTransaction().clear();
//			
//			Session session = sessionFactory.getCurrentSession();
//			
//			if (session != null) {
//				session.clear();
//			}
//		});
	}

	@Override
	@Transactional
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		// TODO Auto-generated method stub
		response.setHeader(BaseController.serviceTransactionFlushHeader, Mode.NON.toString());
		sessionFactory.getCurrentSession().setHibernateFlushMode(org.hibernate.FlushMode.MANUAL);

		return HandlerInterceptor.super.preHandle(request, response, handler);
	}

	@Override
	@Transactional
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		// TODO Auto-generated method stub
		logger.debug("Processing postHandle");

		String flushHeader = response.getHeader(BaseController.serviceTransactionFlushHeader);
		System.out.println(flushHeader);
		if (!flushHeader.equals(Mode.NON.toString())) {
			for (String mode : flushHeader.split("\\|")) {
				this.postHandlers.get(mode).accept(response);
			}
		}

		HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		// TODO Auto-generated method stub
		response.setHeader(BaseController.serviceTransactionFlushHeader, null);
		HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
	}
}
