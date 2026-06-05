import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-module-placeholder',
  imports: [CommonModule],
  templateUrl: './module-placeholder.html',
  styleUrl: './module-placeholder.css',
})
export class ModulePlaceholder {
  title = 'Modulo';
  kicker = 'Modulo';
  description = 'Pantalla en construccion.';
  highlights: string[] = [];

  constructor(private route: ActivatedRoute) {
    this.title = this.route.snapshot.data['title'] ?? 'Modulo';
    this.kicker = this.route.snapshot.data['kicker'] ?? 'Modulo';
    this.description = this.route.snapshot.data['description'] ?? 'Pantalla en construccion.';
    this.highlights = this.route.snapshot.data['highlights'] ?? [];
  }
}
