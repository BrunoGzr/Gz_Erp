import {
  Component,
  ElementRef,
  HostListener,
  ViewChild,
  computed,
  inject,
  signal,
} from '@angular/core';
import { Router } from '@angular/router';
import { HeaderIconComponent } from './header-icon-component';

type TopbarPanel = 'search' | 'notifications' | 'help' | null;

type NotificationTone = 'blue' | 'amber' | 'green' | 'red' | 'violet';

interface TopbarPage {
  label: string;
  icon: string;
  route: string;
  available: boolean;
}

interface AppNotification {
  id: number;
  title: string;
  description: string;
  time: string;
  tone: NotificationTone;
  read: boolean;
}

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [HeaderIconComponent],
  templateUrl: './topbar-component.html',
  styleUrl: './topbar-component.css',
})
export class TopbarComponent {
  private readonly router = inject(Router);

  @ViewChild('searchInput') private searchInput?: ElementRef<HTMLInputElement>;

  readonly openPanel = signal<TopbarPanel>(null);
  readonly query = signal('');

  readonly pages: TopbarPage[] = [
    { label: 'Início', icon: 'home', route: '/dashboard', available: true },
    { label: 'Produtos', icon: 'shopping-bag', route: '/produtos', available: true },
    { label: 'Pedidos', icon: 'shopping-cart', route: '/pedidos', available: false },
    { label: 'Marketplaces', icon: 'store', route: '/marketplaces', available: false },
    { label: 'Equipe', icon: 'users', route: '/equipe', available: false },
  ];

  readonly filteredPages = computed(() => {
    const term = this.normalize(this.query().trim());

    if (!term) {
      return this.pages;
    }

    return this.pages.filter((page) =>
      this.normalize(`${page.label} ${page.route}`).includes(term),
    );
  });

  // MOCK: notificações do app inteiro até existir o endpoint/central de notificações.
  readonly notifications = signal<AppNotification[]>([
    {
      id: 1,
      title: 'Estoque baixo no catálogo',
      description: '12 produtos estão abaixo do mínimo.',
      time: 'há 5 min',
      tone: 'amber',
      read: false,
    },
    {
      id: 2,
      title: 'Ações a fazer',
      description: 'Anúncios com sugestões ou avisos dos marketplaces.',
      time: 'há 20 min',
      tone: 'violet',
      read: false,
    },
    {
      id: 3,
      title: 'Sincronização concluída',
      description: 'Anúncios do Mercado Livre atualizados.',
      time: 'há 1 h',
      tone: 'green',
      read: true,
    },
    {
      id: 4,
      title: 'Novo pedido recebido',
      description: 'Pedido #1042 aguardando processamento.',
      time: 'há 2 h',
      tone: 'blue',
      read: false,
    },
  ]);

  readonly unreadCount = computed(
    () => this.notifications().filter((notification) => !notification.read).length,
  );

  togglePanel(panel: Exclude<TopbarPanel, null>): void {
    this.openPanel.update((current) => (current === panel ? null : panel));
  }

  openSearch(): void {
    this.openPanel.set('search');
  }

  closePanels(): void {
    this.openPanel.set(null);
  }

  onSearchInput(event: Event): void {
    this.query.set((event.target as HTMLInputElement).value);
    this.openPanel.set('search');
  }

  onSearchKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter') {
      const firstAvailable = this.filteredPages().find((page) => page.available);

      if (firstAvailable) {
        this.navigateTo(firstAvailable);
      }
    }

    if (event.key === 'Escape') {
      this.closePanels();
      (event.target as HTMLInputElement).blur();
    }
  }

  navigateTo(page: TopbarPage): void {
    if (!page.available) {
      return;
    }

    this.router.navigateByUrl(page.route);
    this.query.set('');
    this.closePanels();
  }

  markRead(notification: AppNotification): void {
    this.notifications.update((list) =>
      list.map((item) => (item.id === notification.id ? { ...item, read: true } : item)),
    );
  }

  markAllRead(): void {
    this.notifications.update((list) => list.map((item) => ({ ...item, read: true })));
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement | null;

    if (!target?.closest('.topbar')) {
      this.closePanels();
    }
  }

  @HostListener('document:keydown', ['$event'])
  onGlobalKeydown(event: KeyboardEvent): void {
    if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'k') {
      event.preventDefault();
      this.focusSearch();
    }

    if (event.key === 'Escape') {
      this.closePanels();
    }
  }

  private focusSearch(): void {
    this.openPanel.set('search');

    setTimeout(() => {
      this.searchInput?.nativeElement.focus();
      this.searchInput?.nativeElement.select();
    }, 0);
  }

  private normalize(value: string): string {
    return value
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toLowerCase();
  }
}
