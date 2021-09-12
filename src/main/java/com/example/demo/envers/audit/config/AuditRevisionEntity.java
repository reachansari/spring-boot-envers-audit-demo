package com.example.demo.envers.audit.config;


import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;

import javax.persistence.Entity;
import javax.validation.constraints.NotBlank;



@Entity
@RevisionEntity(AuditRevisionListener.class)
public class AuditRevisionEntity extends DefaultRevisionEntity {
	
	private static final long serialVersionUID = 4L;

    @NotBlank
    private String username;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public AuditRevisionEntity(String username) {
		super();
		this.username = username;
	}
	
	public AuditRevisionEntity() {
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AuditRevisionEntity other = (AuditRevisionEntity) obj;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}

}
