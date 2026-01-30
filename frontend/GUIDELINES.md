# Angular Development Guidelines

This document provides coding standards and best practices for Angular development in this project.

## Technology Stack

- **Angular:** 21.1.0
- **TypeScript:** 5.9.2
- **RxJS:** 7.8.0
- **Tailwind CSS:** 4.1.12

## Development Philosophy

This project leverages the latest Angular features including:
- Signals for reactive state management
- Standalone components (no NgModules)
- New control flow syntax (`@if`, `@for`, `@switch`)
- Modern Angular APIs and patterns

## Component Example

Here's a modern Angular 21 component with signals:

```typescript
import { ChangeDetectionStrategy, Component, signal } from '@angular/core';

@Component({
  selector: 'app-example',
  templateUrl: './example.component.html',
  styleUrl: './example.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ExampleComponent {
  protected readonly isServerRunning = signal(true);
  
  toggleServerStatus() {
    this.isServerRunning.update(isRunning => !isRunning);
  }
}
```

```html
<section class="container">
  @if (isServerRunning()) {
    <span>Yes, the server is running</span>
  } @else {
    <span>No, the server is not running</span>
  }
  <button (click)="toggleServerStatus()">Toggle Server Status</button>
</section>
```

## TypeScript Best Practices

- ✅ Use strict type checking
- ✅ Prefer type inference when the type is obvious
- ❌ Avoid the `any` type; use `unknown` when type is uncertain

## Angular Best Practices

### Components

- ✅ Always use standalone components (NgModules are deprecated)
- ❌ Do NOT set `standalone: true` in decorators (it's the default)
- ✅ Use signals for state management
- ✅ Implement lazy loading for feature routes
- ✅ Use `input()` signal instead of `@Input()` decorator
- ✅ Use `output()` function instead of `@Output()` decorator
- ✅ Use `computed()` for derived state
- ✅ Set `changeDetection: ChangeDetectionStrategy.OnPush`
- ✅ Prefer inline templates for small components
- ✅ Prefer Reactive forms over Template-driven forms
- ❌ Do NOT use `@HostBinding` and `@HostListener` decorators
- ✅ Put host bindings inside the `host` object of `@Component` or `@Directive` decorator
- ✅ Use `NgOptimizedImage` for all static images (not for inline base64)
- ❌ Do NOT use `ngClass`, use `class` bindings instead
- ❌ Do NOT use `ngStyle`, use `style` bindings instead

### State Management

- ✅ Use signals for local component state
- ✅ Use `computed()` for derived state
- ✅ Keep state transformations pure and predictable
- ❌ Do NOT use `mutate` on signals, use `update` or `set` instead

### Templates

- ✅ Keep templates simple and avoid complex logic
- ✅ Use native control flow (`@if`, `@for`, `@switch`) instead of `*ngIf`, `*ngFor`, `*ngSwitch`
- ❌ Do not assume globals like `new Date()` are available
- ❌ Do not write arrow functions in templates (not supported)
- ✅ Use the async pipe to handle observables
- ✅ Use built-in pipes and import pipes when used in templates
- ✅ Use paths relative to the component TS file for external templates/styles

### Services

- ✅ Design services around a single responsibility
- ✅ Use `providedIn: 'root'` for singleton services
- ✅ Use `inject()` function instead of constructor injection

## Accessibility Requirements

All components must:
- ✅ Pass all AXE checks
- ✅ Follow WCAG AA minimums
- ✅ Implement proper focus management
- ✅ Maintain adequate color contrast
- ✅ Use appropriate ARIA attributes

## Resources

- [Angular Components](https://angular.dev/essentials/components)
- [Angular Signals](https://angular.dev/essentials/signals)
- [Angular Templates](https://angular.dev/essentials/templates)
- [Dependency Injection](https://angular.dev/essentials/dependency-injection)
- [Angular Style Guide](https://angular.dev/style-guide)
- [Component Inputs](https://angular.dev/guide/components/inputs)
- [Component Outputs](https://angular.dev/guide/components/outputs)
- [Template Pipes](https://angular.dev/guide/templates/pipes)
- [Template Bindings](https://angular.dev/guide/templates/binding)
