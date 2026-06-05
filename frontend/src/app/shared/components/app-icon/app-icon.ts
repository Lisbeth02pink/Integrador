import { ChangeDetectionStrategy, Component, Input } from '@angular/core';

@Component({
  selector: 'app-icon',
  standalone: true,
  templateUrl: './app-icon.html',
  styleUrl: './app-icon.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppIconComponent {
  @Input() name = 'package';
  @Input() size = 18;
  @Input() strokeWidth = 1.9;
}
