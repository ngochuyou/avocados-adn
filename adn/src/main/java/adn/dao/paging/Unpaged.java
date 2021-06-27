/**
 * 
 */
package adn.dao.paging;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * @author Ngoc Huy
 *
 */
public enum Unpaged implements Pageable {

	INSTANCE;

	@Override
	public int getPageNumber() {
		return 0;
	}

	@Override
	public int getPageSize() {
		return 1000;
	}

	@Override
	public long getOffset() {
		return 0;
	}

	@Override
	public Sort getSort() {
		return Sort.unsorted();
	}

	@Override
	public Pageable next() {
		throw new UnsupportedOperationException("Unable to scroll UNPAGED instance");
	}

	@Override
	public Pageable previousOrFirst() {
		throw new UnsupportedOperationException("Unable to scroll UNPAGED instance");
	}

	@Override
	public Pageable first() {
		throw new UnsupportedOperationException("Unable to scroll UNPAGED instance");
	}

	@Override
	public boolean hasPrevious() {
		return false;
	}

	@Override
	public boolean isPaged() {
		return false;
	}

}
