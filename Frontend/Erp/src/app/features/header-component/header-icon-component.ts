import { Component, Input } from '@angular/core';

/**
 * Ícones SVG inline do header.
 *
 * Não usamos [innerHTML] porque o sanitizer do Angular remove o SVG.
 * Como os ícones são estáticos, mantê-los no template é a forma mais
 * segura e previsível de renderizá-los.
 */
@Component({
  selector: 'app-header-icon',
  standalone: true,
  template: `
    @switch (name) {
      @case ('home') {
        <img
          src="Icone-home.svg"
          [attr.width]="size"
          [attr.height]="size"
          alt=""
          aria-hidden="true"
        />
      }
      @case ('shopping-bag') {
        <svg [attr.width]="size" [attr.height]="size" viewBox="0 0 24 24" fill="none"
          stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
          aria-hidden="true" focusable="false">
          <path d="M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4z" />
          <path d="M3 6h18" />
          <path d="M16 10a4 4 0 01-8 0" />
        </svg>
      }
      @case ('shopping-cart') {
        <svg [attr.width]="size" [attr.height]="size" viewBox="0 0 24 24" fill="none"
          stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
          aria-hidden="true" focusable="false">
          <circle cx="9" cy="21" r="1" />
          <circle cx="20" cy="21" r="1" />
          <path d="M1 1h4l2.68 13.39a2 2 0 002 1.61h9.72a2 2 0 002-1.61L23 6H6" />
        </svg>
      }
      @case ('store') {
        <img
          src="marketplace-icon.svg"
          [attr.width]="size"
          [attr.height]="size"
          alt=""
          aria-hidden="true"
        />
      }
      @case ('users') {
        <svg [attr.width]="size" [attr.height]="size" viewBox="0 0 24 24" fill="none"
          stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
          aria-hidden="true" focusable="false">
          <path d="M16 21v-2a4 4 0 00-4-4H6a4 4 0 00-4 4v2" />
          <circle cx="9" cy="7" r="4" />
          <path d="M22 21v-2a4 4 0 00-3-3.87" />
          <path d="M16 3.13a4 4 0 010 7.75" />
        </svg>
      }
      @case ('user') {
        <svg [attr.width]="size" [attr.height]="size" viewBox="0 0 24 24" fill="none"
          stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
          aria-hidden="true" focusable="false">
          <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2" />
          <circle cx="12" cy="7" r="4" />
        </svg>
      }
      @case ('log-out') {
        <svg [attr.width]="size" [attr.height]="size" viewBox="0 0 24 24" fill="none"
          stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
          aria-hidden="true" focusable="false">
          <path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4" />
          <polyline points="16 17 21 12 16 7" />
          <line x1="21" y1="12" x2="9" y2="12" />
        </svg>
      }
      @case ('arrow-left') {
        <svg [attr.width]="size" [attr.height]="size" viewBox="0 0 24 24" fill="none"
          stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
          aria-hidden="true" focusable="false">
          <path d="M19 12H5" />
          <path d="M12 19l-7-7 7-7" />
        </svg>
      }
      @case ('arrow-right') {
        <svg [attr.width]="size" [attr.height]="size" viewBox="0 0 24 24" fill="none"
          stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
          aria-hidden="true" focusable="false">
          <path d="M5 12h14" />
          <path d="M12 5l7 7-7 7" />
        </svg>
      }
      @case ('pin') {
        <svg [attr.width]="size" [attr.height]="size" viewBox="0 0 24 24" fill="none"
          stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
          aria-hidden="true" focusable="false">
          <path d="M12 17v5" />
          <path d="M9 10.76a2 2 0 01-1.11 1.79l-1.78.9A2 2 0 005 15.24V16a1 1 0 001 1h12a1 1 0 001-1v-.76a2 2 0 00-1.11-1.79l-1.78-.9A2 2 0 0115 10.76V7a1 1 0 011-1 2 2 0 000-4H8a2 2 0 000 4 1 1 0 011 1z" />
        </svg>
      }
      @case ('pin-off') {
        <svg [attr.width]="size" [attr.height]="size" viewBox="0 0 24 24" fill="none"
          stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
          aria-hidden="true" focusable="false">
          <path d="M12 17v5" />
          <path d="M15 9.34V7a1 1 0 011-1 2 2 0 000-4H7.89" />
          <path d="m2 2 20 20" />
          <path d="M9 9v1.76a2 2 0 01-1.11 1.79l-1.78.9A2 2 0 005 15.24V16a1 1 0 001 1h11" />
        </svg>
      }
      @default {
        <svg [attr.width]="size" [attr.height]="size" viewBox="0 0 24 24" fill="none"
          stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
          aria-hidden="true" focusable="false">
          <path d="M3 10l9-7 9 7" />
          <path d="M5 10v11a1 1 0 001 1h12a1 1 0 001-1V10" />
        </svg>
      }
    }
  `,
  styles: `
    :host {
      display: inline-flex;
      align-items: center;
      justify-content: center;
    }

    svg,
    img {
      display: block;
    }
  `,
})
export class HeaderIconComponent {
  @Input({ required: true }) name!: string;
  @Input() size = 24;
}
