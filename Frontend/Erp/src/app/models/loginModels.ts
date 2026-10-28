import { readonly } from "@angular/forms/signals";

export type LoginRequest = {

    identifier: string,
    password: string
}

export interface LoginResponse {
  token: string;
  type: "Bearer";
  expirationInSex: number;
  readonly publicUserId: string;
  userType: UserType;
  readonly publicTenantId: string;
}

export interface User {

  id: number;
  email: string;
  tenantId: number;
  userType: string;
}


type UserType = "PARTNER" | "EMPLOYEE" | "ADMIN"


