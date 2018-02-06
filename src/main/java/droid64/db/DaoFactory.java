package droid64.db;

/**
 * Abstract DAO factory.
 * @author Henrik
 */
public abstract class DaoFactory {

	/**
	 * Get <code>Disk</code> DAO
	 * @return DiskDao
	 */
	public abstract DiskDao getDiskDao();
	
	/**
	 * Static method get get the MySQL DAO factory implementation
	 * @return DaoFactory
	 */
	public static DaoFactory getDaoFactory() {
		return new DaoFactoryImpl();
	}
}
