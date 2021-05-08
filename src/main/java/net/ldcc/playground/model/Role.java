package net.ldcc.playground.model;

public enum Role {

	USER("ROLE_USER", "사용자"), ADMIN("ROLE_ADMIN", "관리자");

	private final String authority;
	private final String desc;

	private Role(String authority, String desc) {
		this.authority = authority;
		this.desc = desc;
	}

	public String getAuthority() {
		return authority;
	}

	public String getDesc() {
		return desc;
	}

}
