import { Component, OnDestroy, computed, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../service/auth-login-service';
import { HeaderIconComponent } from './header-icon-component';
import { TopbarComponent } from './topbar-component';

const SIDEBAR_HOVER_DELAY = 300;

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
export class HeaderComponent implements OnDestroy {
  private authService = inject(AuthService);
  private router = inject(Router);

  // --- Estado da sidebar ---
  isExpanded = signal(false);
  isPinned = signal(false);
  isHovered = signal(false);
  private hoverTimer: ReturnType<typeof setTimeout> | null = null;

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
    paths: 'subset',
    queryParams: 'ignored',
    matrixParams: 'ignored',
    fragment: 'ignored',
  } as const;

  // --- Expansão por hover (quando não está fixada) ---
  // O delay evita que a passagem rápida do mouse abra/fecha a sidebar sem intenção.
  onSidebarMouseEnter(): void {
    this.isHovered.set(true);
    this.clearHoverTimer();

    if (this.isPinned() || this.isExpanded()) {
      return;
    }

    this.hoverTimer = setTimeout(() => {
      this.hoverTimer = null;

      if (!this.isPinned() && this.isHovered()) {
        this.isExpanded.set(true);
      }
    }, SIDEBAR_HOVER_DELAY);
  }

  onSidebarMouseLeave(): void {
    this.isHovered.set(false);
    this.clearHoverTimer();

    if (this.isPinned() || !this.isExpanded()) {
      return;
    }

    this.hoverTimer = setTimeout(() => {
      this.hoverTimer = null;

      if (!this.isPinned() && !this.isHovered()) {
        this.isExpanded.set(false);
      }
    }, SIDEBAR_HOVER_DELAY);
  }

  // --- Expandir / recolher manualmente (botão central) ---
  toggleExpanded(): void {
    this.clearHoverTimer();
    this.isExpanded.update((expanded) => !expanded);
  }

  // --- Fixar / desafixar o comportamento de hover ---
  togglePin(): void {
    this.clearHoverTimer();

    if (this.isPinned()) {
      this.isPinned.set(false);

      // Ao desafixar, volta a respeitar o hover atual.
      this.isExpanded.set(this.isHovered());
      return;
    }

    this.isPinned.set(true);
    this.isExpanded.set(true);
  }

  ngOnDestroy(): void {
    this.clearHoverTimer();
  }

  private clearHoverTimer(): void {
    if (this.hoverTimer) {
      clearTimeout(this.hoverTimer);
      this.hoverTimer = null;
    }
  }

  // --- Logout ---
  doLogout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
