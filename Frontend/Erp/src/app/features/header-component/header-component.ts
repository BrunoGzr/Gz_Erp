import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../service/auth-login-service';
import { HeaderIconComponent } from './header-icon-component';
import { TopbarComponent } from './topbar-component';

export interface MenuItem {
  label: string;
  icon: string;
  route: string;
}

@Component({
  selector: 'app-header-component',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, RouterOutlet, HeaderIconComponent, TopbarComponent],
  templateUrl: './header-component.html',
  styleUrl: './header-component.css',
})
export class HeaderComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  // --- Estado da sidebar ---
  isExpanded = signal(false);
  isPinned = signal(false);
  isHovered = signal(false);

  // --- Dados do usuário (populados pelo AuthService a partir do JWT) ---
  userName = computed(() => {
    const user = this.authService.currentUser();
    return user?.name || user?.username || 'Usuário';
  });

  companyName = computed(() => {
    const user = this.authService.currentUser();
    return user?.companyName || 'Empresa';
  });

  userProfilePicture = computed(() => {
    return this.authService.currentUser()?.profilePicture || null;
  });

  // --- Menu de navegação ---
  readonly menuItems: MenuItem[] = [
    { label: 'Início', icon: 'home', route: '/dashboard' },
    { label: 'Produtos', icon: 'shopping-bag', route: '/produtos' },
    { label: 'Pedidos', icon: 'shopping-cart', route: '/pedidos' },
    { label: 'Marketplaces', icon: 'store', route: '/marketplaces' },
    { label: 'Equipe', icon: 'users', route: '/equipe' },
  ];

  // Mantém o item ativo quando a rota recebe query params (ex.: filtros do catálogo).
  readonly navLinkActiveOptions = {
    paths: 'exact',
    queryParams: 'ignored',
    matrixParams: 'ignored',
    fragment: 'ignored',
  } as const;

  // --- Expansão por hover (quando não está fixada) ---
  onSidebarMouseEnter(): void {
    this.isHovered.set(true);

    if (!this.isPinned()) {
      this.isExpanded.set(true);
    }
  }

  onSidebarMouseLeave(): void {
    this.isHovered.set(false);

    if (!this.isPinned()) {
      this.isExpanded.set(false);
    }
  }

  // --- Expandir / recolher manualmente (botão central) ---
  toggleExpanded(): void {
    this.isExpanded.update((expanded) => !expanded);
  }

  // --- Fixar / desafixar o comportamento de hover ---
  togglePin(): void {
    if (this.isPinned()) {
      this.isPinned.set(false);

      // Ao desafixar, volta a respeitar o hover atual.
      this.isExpanded.set(this.isHovered());
      return;
    }

    this.isPinned.set(true);
    this.isExpanded.set(true);
  }

  // --- Logout ---
  doLogout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
