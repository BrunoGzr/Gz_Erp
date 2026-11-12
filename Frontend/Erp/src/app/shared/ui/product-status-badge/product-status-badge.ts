import { Component, computed, input } from '@angular/core';
import { ProductStatus } from '../../../models/productModels';
import { statusLabel, statusTone } from '../../format/product-format';

@Component({
  selector: 'app-product-status-badge',
  template: `
    @if (status() || showUnknown()) {
      <span
        [class]="'status-badge ' + tone()"
        [attr.title]="'Status do produto: ' + label()"
      >
        <span class="status-dot" aria-hidden="true"></span>
        {{ label() }}
      </span>
    }
  `,
  styles: `
    :host {
      display: inline-flex;
      flex: 0 0 auto;
    }

    .status-badge {
      display: inline-flex;
      align-items: center;
      gap: 5px;
      padding: 2px 8px;
      border: 1px solid var(--gz-border);
      border-radius: var(--gz-radius-pill);
      background: var(--gz-surface);
      color: var(--gz-text-muted-2);
      font-size: var(--gz-font-size-xs);
      font-weight: 600;
      line-height: 1.5;
      white-space: nowrap;
    }

    .status-dot {
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background: currentColor;
    }

    .status-badge.active {
      border-color: rgba(74, 222, 128, 0.28);
      background: rgba(74, 222, 128, 0.1);
      color: var(--gz-success-text);
    }

    .status-badge.paused {
      border-color: rgba(251, 191, 36, 0.28);
      background: rgba(251, 191, 36, 0.1);
      color: var(--gz-warning-text);
    }

    .status-badge.inactive {
      border-color: rgba(248, 113, 113, 0.28);
      background: rgba(248, 113, 113, 0.1);
      color: var(--gz-danger-text);
    }

    .status-badge.unknown {
      color: var(--gz-text-muted);
    }
  `,
})
export class ProductStatusBadgeComponent {
  readonly status = input<ProductStatus | null>(null);
  readonly showUnknown = input(false);

  readonly label = computed(() => statusLabel(this.status()));
  readonly tone = computed(() => statusTone(this.status()));
}
