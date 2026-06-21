package com.erpapi.gzerp.models;

import com.erpapi.gzerp.enums.Plans;
import com.erpapi.gzerp.enums.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.sql.Timestamp;
import java.util.ArrayList;

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
    private String razaoSocial;

    private String nomeFantasia;

    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private ArrayList<Partners> partners;

    @NotNull
    private Plans plan;

    @NotNull
    private Status status;
    private Boolean demo;
    private Timestamp register_at;
    private Timestamp update_at;


    private Boolean isAdmin;

    public Tenants() {}

    public Tenants(Long id, String cnpj, String email, String razaoSocial, String nomeFantasia, Plans plan, Status status, Boolean demo, Timestamp register_at, Timestamp update_at) {
        this.id = id;
        this.cnpj = cnpj;
        this.email = email;
        this.razaoSocial = razaoSocial;
        this.nomeFantasia = nomeFantasia;
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

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public void setRazaoSocial(String razao_social) {
        this.razaoSocial = razao_social;
    }

    public String getNomeFantasia() {
        return nomeFantasia;
    }

    public void setNomeFantasia(String nome_fantasia) {
        this.nomeFantasia = nome_fantasia;
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

