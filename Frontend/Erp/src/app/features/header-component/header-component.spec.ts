import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { vi } from 'vitest';
import { HeaderComponent } from './header-component';

describe('HeaderComponent', () => {
  let component: HeaderComponent;
  let fixture: ComponentFixture<HeaderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HeaderComponent],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(HeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should expand after the hover delay and collapse after the leave delay', () => {
    expect(component.isExpanded()).toBe(false);

    component.onSidebarMouseEnter();
    vi.advanceTimersByTime(299);
    expect(component.isExpanded()).toBe(false);

    vi.advanceTimersByTime(1);
    expect(component.isExpanded()).toBe(true);

    component.onSidebarMouseLeave();
    vi.advanceTimersByTime(299);
    expect(component.isExpanded()).toBe(true);

    vi.advanceTimersByTime(1);
    expect(component.isExpanded()).toBe(false);
  });

  it('should not open on a quick mouse pass', () => {
    component.onSidebarMouseEnter();
    vi.advanceTimersByTime(100);
    component.onSidebarMouseLeave();
    vi.advanceTimersByTime(500);

    expect(component.isExpanded()).toBe(false);
  });

  it('should ignore hover while pinned and only toggle with the center button', () => {
    component.togglePin();

    expect(component.isPinned()).toBe(true);
    expect(component.isExpanded()).toBe(true);

    component.onSidebarMouseLeave();
    vi.advanceTimersByTime(500);
    expect(component.isExpanded()).toBe(true);

    component.toggleExpanded();
    expect(component.isExpanded()).toBe(false);

    component.onSidebarMouseEnter();
    vi.advanceTimersByTime(500);
    expect(component.isExpanded()).toBe(false);

    component.toggleExpanded();
    expect(component.isExpanded()).toBe(true);
  });

  it('should collapse when unpinned while the pointer is outside', () => {
    component.togglePin();
    component.onSidebarMouseLeave();

    component.togglePin();

    expect(component.isPinned()).toBe(false);
    expect(component.isExpanded()).toBe(false);
  });

  it('should stay expanded when unpinned while the pointer is over the sidebar', () => {
    component.togglePin();
    component.onSidebarMouseEnter();

    component.togglePin();

    expect(component.isPinned()).toBe(false);
    expect(component.isExpanded()).toBe(true);
  });
});
