package com.erpapi.gzerp.dto;

import com.erpapi.gzerp.models.Partners;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class PartnersResponseDto {

	@NotNull
	@Size(min=10, max=20)
	private String cpf;

	@NotNull
	@Size(min=2, max=256)
	private String name;

	@NotNull
	@Email
	@Size(min=5, max=256)
	private String email;

	@NotNull
	@Size(min=5, max=256)
	private String phone;

	@Nullable
	private BigDecimal ownership;

	@Nullable
	private BigDecimal salary;

	public PartnersResponseDto() {
	}
	
	public PartnersResponseDto(Partners partner) {
		this.cpf = partner.getCpf();
		this.name = partner.getFullName();
		this.email = partner.getEmail();
		this.phone = partner.getPhone();
		this.ownership = partner.getOwnership();
		this.salary = partner.getSalary();
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	@Nullable
	public BigDecimal getOwnership() {
		return ownership;
	}

	public void setOwnership(@Nullable BigDecimal ownership) {
		this.ownership = ownership;
	}

	@Nullable
	public BigDecimal getSalary() {
		return salary;
	}

	public void setSalary(@Nullable BigDecimal salary) {
		this.salary = salary;
	}
}
