package com.erpapi.gzerp.models;

import com.erpapi.gzerp.enums.Plans;
import com.erpapi.gzerp.enums.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.sql.Timestamp;

@Entity
public class Tenants {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min = 14 , max = 14)
    private String cnpj;

    @NotNull
    @Email
    @Size(min = 5, max = 256)
    private String email;

    @NotNull
    private String razao_social;

    @NotNull
    private String nome_fantasia;

    private String associates;

    @NotNull
    private Plans plan;

    @NotNull
    private Status status;
    private Boolean demo;
    private Timestamp register_at;
    private Timestamp update_at;

    public Tenants() {}

    public Tenants(Long id, String cnpj, String email, String razao_social, String nome_fantasia, String associates, Plans plan, Status status, Boolean demo, Timestamp register_at, Timestamp update_at) {
        this.id = id;
        this.cnpj = cnpj;
        this.email = email;
        this.razao_social = razao_social;
        this.nome_fantasia = nome_fantasia;
        this.associates = associates;
        this.plan = plan;
        this.status = status;
        this.demo = demo;
        this.register_at = register_at;
        this.update_at = update_at;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRazao_social() {
        return razao_social;
    }

    public void setRazao_social(String razao_social) {
        this.razao_social = razao_social;
    }

    public String getNome_fantasia() {
        return nome_fantasia;
    }

    public void setNome_fantasia(String nome_fantasia) {
        this.nome_fantasia = nome_fantasia;
    }

    public String getAssociates() {
        return associates;
    }

    public void setAssociates(String associates) {
        this.associates = associates;
    }

    public Plans getPlan() {
        return plan;
    }

    public void setPlan(Plans plan) {
        this.plan = plan;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Boolean getDemo() {
        return demo;
    }

    public void setDemo(Boolean demo) {
        this.demo = demo;
    }

    public Timestamp getRegister_at() {
        return register_at;
    }

    public void setRegister_at(Timestamp register_at) {
        this.register_at = register_at;
    }

    public Timestamp getUpdate_at() {
        return update_at;
    }

    public void setUpdate_at(Timestamp update_at) {
        this.update_at = update_at;
    }
}

