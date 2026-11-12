import {
  AfterViewInit,
  Component,
  ElementRef,
  OnDestroy,
  ViewChild,
  inject,
  input,
  output,
  signal,
} from '@angular/core';
import { DOCUMENT } from '@angular/common';

@Component({
  selector: 'app-confirm-dialog',
  template: `
    <div class="dialog-backdrop" (click)="onBackdrop($event)">
      <section
        #dialog
        class="confirm-dialog"
        role="alertdialog"
        aria-modal="true"
        aria-labelledby="confirm-dialog-title"
        aria-describedby="confirm-dialog-message"
        tabindex="-1"
        (keydown)="onKeydown($event)"
      >
        <header class="dialog-head">
          <span class="dialog-kicker">{{ step() === 1 ? 'Confirmação' : 'Etapa final' }}</span>
          <h2 id="confirm-dialog-title">{{ title() }}</h2>
        </header>

        <div class="dialog-body">
          <p id="confirm-dialog-message">{{ message() }}</p>

          @if (warning()) {
            <p class="dialog-warning">{{ warning() }}</p>
          }

          @if (step() === 2) {
            <p class="dialog-final">
              Esta ação é definitiva nesta tela. Revise antes de confirmar.
            </p>
          }

          @if (error()) {
            <p class="dialog-error" role="alert">{{ error() }}</p>
          }
        </div>

        <footer class="dialog-foot">
          @if (step() === 1) {
            <button
              #initialFocus
              class="btn"
              type="button"
              [disabled]="loading()"
              (click)="cancel()"
            >
              Cancelar
            </button>
            <button class="btn btn-danger" type="button" [disabled]="loading()" (click)="advance()">
              Continuar
            </button>
          } @else {
            <button class="btn" type="button" [disabled]="loading()" (click)="back()">
              Voltar
            </button>
            <button
              class="btn btn-danger final-confirm"
              type="button"
              [disabled]="loading()"
              (click)="advance()"
            >
              @if (loading()) {
                <span class="button-spinner" aria-hidden="true"></span>
              }
              {{ confirmLabel() }}
            </button>
          }
        </footer>
      </section>
    </div>
  `,
  styles: `
    :host {
      position: fixed;
      inset: 0;
      z-index: var(--gz-z-overlay-content);
      display: block;
    }

    .dialog-backdrop {
      position: fixed;
      inset: 0;
      z-index: var(--gz-z-overlay);
      display: grid;
      place-items: center;
      padding: 20px;
      background: rgba(0, 0, 0, 0.62);
    }

    .confirm-dialog {
      position: relative;
      z-index: var(--gz-z-overlay-content);
      width: min(480px, calc(100vw - 32px));
      max-height: min(90vh, 720px);
      overflow: auto;
      padding: 22px;
      border: 1px solid var(--gz-border);
      border-radius: var(--gz-radius-2xl);
      background: var(--gz-panel);
      box-shadow: var(--gz-shadow-xl);
      color: var(--gz-text);
    }

    .dialog-head {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .dialog-kicker {
      color: var(--gz-danger-text);
      font-size: var(--gz-font-size-xs);
      font-weight: 700;
      letter-spacing: 0.1em;
      text-transform: uppercase;
    }

    h2 {
      margin: 0;
      color: var(--gz-text-strong);
      font-size: var(--gz-font-size-xl);
      letter-spacing: -0.02em;
    }

    .dialog-body {
      display: flex;
      flex-direction: column;
      gap: 10px;
      margin-top: 14px;
    }

    p {
      margin: 0;
      color: var(--gz-text-muted-2);
      font-size: var(--gz-font-size-base);
      line-height: 1.55;
    }

    .dialog-warning {
      padding: 10px 12px;
      border: 1px solid rgba(251, 191, 36, 0.3);
      border-radius: var(--gz-radius-md);
      background: rgba(251, 191, 36, 0.08);
      color: var(--gz-warning-text);
    }

    .dialog-final {
      color: var(--gz-text);
      font-weight: 600;
    }

    .dialog-error {
      padding: 10px 12px;
      border: 1px solid rgba(248, 113, 113, 0.35);
      border-radius: var(--gz-radius-md);
      background: rgba(248, 113, 113, 0.08);
      color: var(--gz-danger-text);
    }

    .dialog-foot {
      display: flex;
      justify-content: flex-end;
      gap: 8px;
      flex-wrap: wrap;
      margin-top: 20px;
    }

    .btn {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 7px;
      height: 40px;
      padding: 0 16px;
      border: 1px solid var(--gz-border);
      border-radius: var(--gz-radius-pill);
      background: var(--gz-surface);
      color: var(--gz-text);
      font: inherit;
      font-size: var(--gz-font-size-md);
      font-weight: 600;
      cursor: pointer;
    }

    .btn:hover:not(:disabled) {
      background: var(--gz-surface-hover);
    }

    .btn-danger {
      border-color: rgba(248, 113, 113, 0.4);
      background: rgba(248, 113, 113, 0.12);
      color: var(--gz-danger-text);
    }

    .btn:disabled {
      opacity: 0.55;
      cursor: not-allowed;
    }

    .button-spinner {
      width: 14px;
      height: 14px;
      border: 2px solid rgba(255, 255, 255, 0.3);
      border-top-color: currentColor;
      border-radius: 50%;
      animation: spin 0.7s linear infinite;
    }

    @keyframes spin {
      to {
        transform: rotate(360deg);
      }
    }

    @media (prefers-reduced-motion: reduce) {
      .button-spinner {
        animation-duration: 0.001s;
      }
    }
  `,
})
export class ConfirmDialogComponent implements AfterViewInit, OnDestroy {
  private readonly document = inject(DOCUMENT);

  readonly title = input('Confirmar ação');
  readonly message = input('');
  readonly warning = input<string | null>(null);
  readonly confirmLabel = input('Confirmar');
  readonly loading = input(false);
  readonly error = input<string | null>(null);

  readonly confirmed = output<void>();
  readonly cancelled = output<void>();

  readonly step = signal<1 | 2>(1);

  @ViewChild('dialog') private dialog?: ElementRef<HTMLElement>;
  @ViewChild('initialFocus') private initialFocus?: ElementRef<HTMLButtonElement>;

  private previousFocus: HTMLElement | null = null;
  private previousOverflow = '';

  ngAfterViewInit(): void {
    this.previousFocus =
      this.document.activeElement instanceof HTMLElement ? this.document.activeElement : null;
    this.previousOverflow = this.document.body.style.overflow;
    this.document.body.style.overflow = 'hidden';

    setTimeout(() => this.initialFocus?.nativeElement.focus(), 0);
  }

  ngOnDestroy(): void {
    this.document.body.style.overflow = this.previousOverflow;

    if (this.previousFocus?.isConnected) {
      this.previousFocus.focus();
    }
  }

  advance(): void {
    if (this.loading()) {
      return;
    }

    if (this.step() === 1) {
      this.step.set(2);
      setTimeout(
        () =>
          this.dialog?.nativeElement
            .querySelector<HTMLButtonElement>('.final-confirm')
            ?.focus(),
        0,
      );
      return;
    }

    this.confirmed.emit();
  }

  back(): void {
    if (!this.loading()) {
      this.step.set(1);
      setTimeout(() => this.initialFocus?.nativeElement.focus(), 0);
    }
  }

  cancel(): void {
    if (!this.loading()) {
      this.cancelled.emit();
    }
  }

  onBackdrop(event: MouseEvent): void {
    if (event.target === event.currentTarget) {
      this.cancel();
    }
  }

  onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Escape') {
      event.preventDefault();
      event.stopPropagation();
      this.cancel();
      return;
    }

    if (event.key !== 'Tab') {
      return;
    }

    const container = this.dialog?.nativeElement;

    if (!container) {
      return;
    }

    const focusable = Array.from(
      container.querySelectorAll<HTMLElement>(
        'button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), a[href], [tabindex]:not([tabindex="-1"])',
      ),
    ).filter((element) => element.getClientRects().length > 0);

    if (focusable.length === 0) {
      return;
    }

    const first = focusable[0];
    const last = focusable[focusable.length - 1];

    if (event.shiftKey && this.document.activeElement === first) {
      event.preventDefault();
      last.focus();
    } else if (!event.shiftKey && this.document.activeElement === last) {
      event.preventDefault();
      first.focus();
    }
  }
}
