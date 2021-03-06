package model;

public class AccessGranted {
	public static final long ACCESS_TIMEOUT = 1 * 60 * 60 * 1000; //1 hour
	public String packageName;
	public long tstamp;

	public AccessGranted(String packageName) {
		this.packageName = packageName;
		tstamp = System.currentTimeMillis();
	}

	@Override
	public int hashCode() {
		return packageName.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof AccessGranted) {
			return ((AccessGranted) o).packageName.equals(packageName);
		} else
			return false;
	}

	public boolean isExpired() {
		System.out.println(""+(System.currentTimeMillis() - tstamp));
		return (System.currentTimeMillis() - tstamp) > ACCESS_TIMEOUT;
	}

}
