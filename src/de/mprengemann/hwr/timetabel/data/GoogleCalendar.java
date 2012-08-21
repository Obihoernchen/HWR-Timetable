/*******************************************************************************
 * Copyright 2012 Marc Prengemann
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
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

	@Override
	public boolean equals(Object o) {

		if (o instanceof GoogleCalendar) {
			if (((GoogleCalendar) o).getId() == this.getId()) {
				return true;
			} else {
				return false;
			}
		}

		return false;
	}

	public String getAccountName() {
		return accountName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public long getId() {
		return id;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public GoogleCalendar setAccountName(String accountName) {
		this.accountName = accountName;
		return this;
	}

	public GoogleCalendar setDisplayName(String displayName) {
		this.displayName = displayName;
		return this;
	}

	public GoogleCalendar setId(long id) {
		this.id = id;
		return this;
	}

	public GoogleCalendar setOwnerName(String ownerName) {
		this.ownerName = ownerName;
		return this;
	}
}
