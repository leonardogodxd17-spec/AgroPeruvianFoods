import { Component, signal, OnInit, OnDestroy, inject } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { DatePipe, SlicePipe } from '@angular/common';
import { AuthService } from '../auth/auth-service';

@Component({
  selector: 'app-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, DatePipe, SlicePipe],
  templateUrl: './layout.html',
  styleUrl: './layout.css',
})
export class Layout implements OnInit, OnDestroy {
  protected readonly auth = inject(AuthService);
  protected readonly currentDateTime = signal(new Date());
  protected readonly activeTraceId = signal(crypto.randomUUID());
  private clockInterval: any;

  ngOnInit(): void {
    this.clockInterval = setInterval(() => {
      this.currentDateTime.set(new Date());
    }, 1000);
  }

  ngOnDestroy(): void {
    if (this.clockInterval) {
      clearInterval(this.clockInterval);
    }
  }
}
