package com.erpapi.gzerp.models;

import com.erpapi.gzerp.enums.Plans;
import com.erpapi.gzerp.enums.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Tenants {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min = 10 , max = 20)
    private String cnpj;

    @NotNull
    @Email
    @Size(min = 5, max = 256)
    private String email;

    @NotEmpty
    @Size(min = 11, max = 15)
    @Column(name = "phone")
    private String phone;

    @NotNull
    private String razaoSocial;

    private String nomeFantasia;

    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Partners> partners = new ArrayList<>();

    @NotNull
    @Enumerated(EnumType.STRING)
    private Plans plan;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Status status;
    private Boolean demo;

    @Column(name = "register_at", insertable = false, updatable = false)
    private LocalDateTime registerAt;
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updateAt;

    @Column(name = "is_admin")
    private Boolean isAdmin;

    public Tenants() {}

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Boolean getAdmin() {
        return isAdmin;
    }

    public void setisAdmin(Boolean admin) {
        this.isAdmin = admin;
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

    public LocalDateTime getRegisterAt() {
        return registerAt;
    }

    public void setRegisterAt(LocalDateTime register_at) {
        this.registerAt = register_at;
    }

    public LocalDateTime getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(LocalDateTime update_at) {
        this.updateAt = update_at;
    }

    public void addPartner(Partners partner){
        this.partners.add(partner);
        partner.setTenant(this);
    }

    public ArrayList<Partners> getPartners(){
        return (ArrayList<Partners>) this.partners;
    }
}