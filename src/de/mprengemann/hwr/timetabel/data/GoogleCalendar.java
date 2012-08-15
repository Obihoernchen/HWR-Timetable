package de.mprengemann.hwr.timetabel.data;

public class GoogleCalendar {

	private long id;
	private String displayName;
	private String accountName;
	private String ownerName;

	public GoogleCalendar(long id, String displayname) {
		this.id = id;
		this.displayName = displayname;
	}

	public GoogleCalendar(long id, String displayName, String accountName) {
		super();
		this.id = id;
		this.displayName = displayName;
		this.accountName = accountName;
	}

	public GoogleCalendar(long id, String displayName, String accountName,
			String ownerName) {
		super();
		this.id = id;
		this.displayName = displayName;
		this.accountName = accountName;
		this.ownerName = ownerName;
	}

	public long getId() {
		return id;
	}

	public GoogleCalendar setId(long id) {
		this.id = id;
		return this;
	}

	public String getDisplayName() {
		return displayName;
	}

	public GoogleCalendar setDisplayName(String displayName) {
		this.displayName = displayName;
		return this;
	}

	public String getAccountName() {
		return accountName;
	}

	public GoogleCalendar setAccountName(String accountName) {
		this.accountName = accountName;
		return this;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public GoogleCalendar setOwnerName(String ownerName) {
		this.ownerName = ownerName;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		
		if (o instanceof GoogleCalendar){
			if (((GoogleCalendar) o).getId() == this.getId()){
				return true;
			}else{
				return false;
			}
		}
		
		return false;
	}
}
