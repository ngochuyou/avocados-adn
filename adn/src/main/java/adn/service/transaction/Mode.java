package adn.service.transaction;

public enum Mode {
	// @formatter:off
	NON,
	COMMIT_ALL,
	CLEAR_SERVICE, ROLLBACK_SERVICE, COMMIT_SERVICE,
	CLEAR_HIBERNATE, COMMIT_HIBERNATE,
	CLEAR_ALL
	// @formatter:on
}
