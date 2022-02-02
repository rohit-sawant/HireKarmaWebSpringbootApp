package com.hirekarma.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import com.sun.istack.NotNull;

import lombok.Data;
import lombok.ToString;

@Entity
@Table(name="INTERNSHIP_APPLY")
@Data
@ToString
public class InternshipApply implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@NotNull
	@Column(name = "INTERNSHIP_APPLY_ID")
	private Long internshipApplyId;
	
	@Column(name = "STUDENT_ID")
	private Long studentId;
	
	@Column(name = "CORPORATE_ID")
	private Long corporateId;
	
	@Column(name = "HIRE_REASON")
	private String hireReason;
	
	@Column(name = "COVER_LETTER")
	private String coverLetter;
	
	@Column(name = "EARLIEST_JOINING_DATE")
	private String earliestJoiningDate;
	
	@Column(name = "DELETE_STATUS")
	private Boolean deleteStatus;
	
	@Column(name = "APPLICATION_STATUS")
	private Boolean applicatinStatus;
	
	@CreationTimestamp
	@Column(name = "CREATED_ON")
	private Timestamp createdOn;
	
	@Column(name = "UPDATED_ON")
	private Timestamp updatedOn;

}
