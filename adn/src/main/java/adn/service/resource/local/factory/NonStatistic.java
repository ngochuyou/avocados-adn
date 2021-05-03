/**
 * 
 */
package adn.service.resource.local.factory;

import org.hibernate.metamodel.model.domain.NavigableRole;
import org.hibernate.stat.CacheRegionStatistics;
import org.hibernate.stat.CollectionStatistics;
import org.hibernate.stat.EntityStatistics;
import org.hibernate.stat.NaturalIdCacheStatistics;
import org.hibernate.stat.NaturalIdStatistics;
import org.hibernate.stat.QueryStatistics;
import org.hibernate.stat.SecondLevelCacheStatistics;
import org.hibernate.stat.spi.StatisticsImplementor;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings({ "serial", "deprecation" })
public class NonStatistic implements StatisticsImplementor {

	@Override
	public boolean isStatisticsEnabled() {
		return false;
	}

	// @formatter:off
	@Override
	public void setStatisticsEnabled(boolean b) {}
	@Override
	public void clear() {}
	@Override
	public void logSummary() {}

	@Override
	public EntityStatistics getEntityStatistics(String entityName) {
		return null;
	}

	@Override
	public CollectionStatistics getCollectionStatistics(String role) {
		return null;
	}

	@Override
	public NaturalIdStatistics getNaturalIdStatistics(String entityName) {
		return null;
	}

	@Override
	public QueryStatistics getQueryStatistics(String queryString) {
		return null;
	}

	@Override
	public CacheRegionStatistics getDomainDataRegionStatistics(String regionName) {
		return null;
	}

	@Override
	public CacheRegionStatistics getQueryRegionStatistics(String regionName) {
		return null;
	}

	@Override
	public CacheRegionStatistics getCacheRegionStatistics(String regionName) {
		return null;
	}

	@Override
	public long getEntityDeleteCount() {
		return 0;
	}

	@Override
	public long getEntityInsertCount() {
		return 0;
	}

	@Override
	public long getEntityLoadCount() {
		return 0;
	}

	@Override
	public long getEntityFetchCount() {
		return 0;
	}

	@Override
	public long getEntityUpdateCount() {
		return 0;
	}

	@Override
	public long getQueryExecutionCount() {
		return 0;
	}

	@Override
	public long getQueryExecutionMaxTime() {
		return 0;
	}

	@Override
	public String getQueryExecutionMaxTimeQueryString() {
		return null;
	}

	@Override
	public long getQueryCacheHitCount() {
		return 0;
	}

	@Override
	public long getQueryCacheMissCount() {
		return 0;
	}

	@Override
	public long getQueryCachePutCount() {
		return 0;
	}

	@Override
	public long getNaturalIdQueryExecutionCount() {
		return 0;
	}

	@Override
	public long getNaturalIdQueryExecutionMaxTime() {
		return 0;
	}

	@Override
	public String getNaturalIdQueryExecutionMaxTimeRegion() {
		return null;
	}

	@Override
	public String getNaturalIdQueryExecutionMaxTimeEntity() {
		return null;
	}

	@Override
	public long getNaturalIdCacheHitCount() {
		return 0;
	}

	@Override
	public long getNaturalIdCacheMissCount() {
		return 0;
	}

	@Override
	public long getNaturalIdCachePutCount() {
		return 0;
	}

	@Override
	public long getUpdateTimestampsCacheHitCount() {
		return 0;
	}

	@Override
	public long getUpdateTimestampsCacheMissCount() {
		return 0;
	}

	@Override
	public long getUpdateTimestampsCachePutCount() {
		return 0;
	}

	@Override
	public long getFlushCount() {
		return 0;
	}

	@Override
	public long getConnectCount() {
		return 0;
	}

	@Override
	public long getSecondLevelCacheHitCount() {
		return 0;
	}

	@Override
	public long getSecondLevelCacheMissCount() {
		return 0;
	}

	@Override
	public long getSecondLevelCachePutCount() {
		return 0;
	}

	@Override
	public long getSessionCloseCount() {
		return 0;
	}

	@Override
	public long getSessionOpenCount() {
		return 0;
	}

	@Override
	public long getCollectionLoadCount() {
		return 0;
	}

	@Override
	public long getCollectionFetchCount() {
		return 0;
	}

	@Override
	public long getCollectionUpdateCount() {
		return 0;
	}

	@Override
	public long getCollectionRemoveCount() {
		return 0;
	}

	@Override
	public long getCollectionRecreateCount() {
		return 0;
	}

	@Override
	public long getStartTime() {
		return 0;
	}

	@Override
	public String[] getQueries() {
		return null;
	}

	@Override
	public String[] getEntityNames() {
		return null;
	}

	@Override
	public String[] getCollectionRoleNames() {
		return null;
	}

	@Override
	public String[] getSecondLevelCacheRegionNames() {
		return null;
	}

	@Override
	public long getSuccessfulTransactionCount() {
		return 0;
	}

	@Override
	public long getTransactionCount() {
		return 0;
	}

	@Override
	public long getPrepareStatementCount() {
		return 0;
	}

	@Override
	public long getCloseStatementCount() {
		return 0;
	}

	@Override
	public long getOptimisticFailureCount() {
		return 0;
	}

	@Override
	public SecondLevelCacheStatistics getSecondLevelCacheStatistics(String regionName) {
		return null;
	}

	@Override
	public NaturalIdCacheStatistics getNaturalIdCacheStatistics(String regionName) {	
		return null;
	}

	@Override
	public void openSession() {}
	@Override
	public void closeSession() {}
	@Override
	public void flush() {}
	@Override
	public void connect() {}
	@Override
	public void prepareStatement() {}
	@Override
	public void closeStatement() {}
	@Override
	public void endTransaction(boolean success) {}
	@Override
	public void loadEntity(String entityName) {}
	@Override
	public void fetchEntity(String entityName) {}
	@Override
	public void updateEntity(String entityName) {}
	@Override
	public void insertEntity(String entityName) {}
	@Override
	public void deleteEntity(String entityName) {}
	@Override
	public void optimisticFailure(String entityName) {}
	@Override
	public void loadCollection(String role) {}
	@Override
	public void fetchCollection(String role) {}
	@Override
	public void updateCollection(String role) {}
	@Override
	public void recreateCollection(String role) {}
	@Override
	public void removeCollection(String role) {}
	@Override
	public void entityCachePut(NavigableRole entityName, String regionName) {}
	@Override
	public void entityCacheHit(NavigableRole entityName, String regionName) {}
	@Override
	public void entityCacheMiss(NavigableRole entityName, String regionName) {}
	@Override
	public void collectionCachePut(NavigableRole collectionRole, String regionName) {}
	@Override
	public void collectionCacheHit(NavigableRole collectionRole, String regionName) {}
	@Override
	public void collectionCacheMiss(NavigableRole collectionRole, String regionName) {}
	@Override
	public void naturalIdCachePut(NavigableRole rootEntityName, String regionName) {}
	@Override
	public void naturalIdCacheHit(NavigableRole rootEntityName, String regionName) {}
	@Override
	public void naturalIdCacheMiss(NavigableRole rootEntityName, String regionName) {}
	@Override
	public void naturalIdQueryExecuted(String rootEntityName, long executionTime) {}
	@Override
	public void queryCachePut(String hql, String regionName) {}
	@Override
	public void queryCacheHit(String hql, String regionName) {}
	@Override
	public void queryCacheMiss(String hql, String regionName) {}
	@Override
	public void queryExecuted(String hql, int rows, long time) {}
	@Override
	public void updateTimestampsCacheHit() {}
	@Override
	public void updateTimestampsCacheMiss() {}
	@Override
	public void updateTimestampsCachePut() {}	 
	// @formatter:on

}
